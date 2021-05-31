package net.yudichev.jiotty.connector.webclient;

import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.IncorrectnessListener;
import com.gargoylesoftware.htmlunit.ScriptException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.parser.HTMLParserListener;
import com.gargoylesoftware.htmlunit.javascript.JavaScriptErrorListener;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

public final class Web {
    private static final Logger logger = LoggerFactory.getLogger(Web.class);

    private Web() {
    }

    public static <T> T executeWebScript(Script<T> script) {
        ProblemAccumulator problemAccumulator = new ProblemAccumulator();
        try (WebClient webClient = new WebClient()) {
            webClient.setIncorrectnessListener(problemAccumulator);
            webClient.setHTMLParserListener(problemAccumulator);
            webClient.setJavaScriptErrorListener(problemAccumulator);
            return script.execute(webClient);
        } catch (Exception e) {
            problemAccumulator.log();
            throw new RuntimeException("Failed running web script, problems logged", e);
        }
    }

    public static <T> T waitForElement(WebClient webClient, String searchSubjectDescription, Supplier<Optional<T>> elementRetriever) {
        Optional<T> element = Optional.empty();
        Instant end = Instant.now().plusSeconds(10);
        do {
            try {
                element = elementRetriever.get();
            } catch (ElementNotFoundException ignored) {
            }
            if (element.isEmpty()) {
                webClient.waitForBackgroundJavaScript(100);
            }
        } while (element.isEmpty() && Instant.now().isBefore(end));
        return element.orElseThrow(() -> new IllegalStateException("Timed out waiting for javascript: cannot find " + searchSubjectDescription));
    }

    private static class ProblemAccumulator implements IncorrectnessListener, HTMLParserListener, JavaScriptErrorListener {
        private final ImmutableList.Builder<Problem> problemBuilder = ImmutableList.builder();

        @Override
        public void notify(String message, Object origin) {
        }

        @Override
        public void error(String message, URL url, String html, int line, int column, String key) {
            problemBuilder.add(new Problem("HTML error on {} at {}:{}, key {}, snippet: {}: {}",
                    url, line, column, key, html, message));
        }

        @Override
        public void warning(String message, URL url, String html, int line, int column, String key) {
            problemBuilder.add(new Problem("HTML warning on {} at {}:{}, key {}, snippet: {}: {}",
                    url, line, column, key, html, message));
        }

        @Override
        public void scriptException(HtmlPage page, ScriptException scriptException) {
            problemBuilder.add(new Problem("Script error on {}: {}", page.getUrl(), scriptException));
        }

        @Override
        public void timeoutError(HtmlPage page, long allowedTime, long executionTime) {
            problemBuilder.add(new Problem("Script timeout error on {}: allowed {}ms, took {}ms", page.getUrl(), allowedTime, executionTime));
        }

        @Override
        public void malformedScriptURL(HtmlPage page, String url, MalformedURLException malformedURLException) {
            problemBuilder.add(new Problem("Script malformed error on {}, URL {}: {}", page.getUrl(), url, malformedURLException));
        }

        @Override
        public void loadScriptError(HtmlPage page, URL scriptUrl, Exception exception) {
            problemBuilder.add(new Problem("Script load error on {}, URL {}: {}", page.getUrl(), scriptUrl, exception));
        }

        @Override
        public void warn(String message, String sourceName, int line, String lineSource, int lineOffset) {
            problemBuilder.add(new Problem("Script warning source {}, line {}, line source {}, line offset: {}",
                    sourceName, line, lineSource, lineOffset, message));
        }

        void log() {
            problemBuilder.build().forEach(Problem::log);
        }
    }

    private static final class Problem {
        private final String pattern;
        private final Object[] arguments;

        private Problem(String pattern, Object... arguments) {
            this.pattern = checkNotNull(pattern);
            this.arguments = checkNotNull(arguments);
        }

        void log() {
            logger.info(pattern, arguments);
        }
    }
}
