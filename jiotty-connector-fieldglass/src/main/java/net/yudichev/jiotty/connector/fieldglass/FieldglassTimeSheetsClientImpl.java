package net.yudichev.jiotty.connector.fieldglass;

import com.opencsv.bean.CsvToBeanBuilder;
import net.yudichev.jiotty.connector.webclient.Web;
import org.htmlunit.Page;
import org.htmlunit.WebResponse;
import org.htmlunit.html.DomElement;
import org.htmlunit.html.HtmlForm;
import org.htmlunit.html.HtmlPage;

import java.io.StringReader;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.google.common.base.Preconditions.checkState;
import static org.htmlunit.html.DomElement.ATTRIBUTE_NOT_DEFINED;

final class FieldglassTimeSheetsClientImpl implements FieldglassTimeSheetsClient {
    private static final String EXPECTED_CSV_MIME_TYPE = "application/msexcel";

    @Override
    public CompletableFuture<List<TimeSheet>> getTimeSheetTable(String username, String password) {
        return CompletableFuture.supplyAsync(() -> Web.executeWebScript(webClient -> {
            HtmlPage page = webClient.getPage("https://www.fieldglass.net");

            HtmlForm form = page.getFormByName("loginForm");
            form.getInputByName("username").type(username);
            form.getInputByName("password").type(password);
            Page landingPage = form.getButtonByName("action").click();

            checkState(landingPage.isHtmlPage(), "Landing page is not an HTML page: %s", landingPage.getUrl());
            Page timeSheetsPage = getElementById((HtmlPage) landingPage, "timeSheet").click();

            checkState(timeSheetsPage.isHtmlPage(), "Time Sheets page is not an HTML page: %s", timeSheetsPage.getUrl());
            HtmlPage timeSheetsPageHtml = (HtmlPage) timeSheetsPage;
            String csvLinkElementId = "download_timeSheet.worker.list";
            String dataUrlAttributeName = "data-url";
            String csvUrl = getElementById(timeSheetsPageHtml, csvLinkElementId)
                    .getAttribute(dataUrlAttributeName);
            checkState(!ATTRIBUTE_NOT_DEFINED.equals(csvUrl),
                       "No attribute %s in element %s on page %s", dataUrlAttributeName, csvLinkElementId, timeSheetsPageHtml.getUrl());

            Page csvPage = webClient.getPage(new URL(timeSheetsPageHtml.getBaseURL(), csvUrl));
            WebResponse webResponse = csvPage.getWebResponse();
            String contentType = webResponse.getContentType();
            checkState(EXPECTED_CSV_MIME_TYPE.equals(contentType),
                       "Content type in response to %s is not %s but %s", csvPage.getUrl(), EXPECTED_CSV_MIME_TYPE, contentType);
            String csvString = webResponse.getContentAsString();
            checkState(csvString != null, "No data in response to %s", csvPage.getUrl());

            return new CsvToBeanBuilder<TimeSheet>(new StringReader(csvString.trim()))
                    .withType(TimeSheet.class)
                    .build()
                    .parse();
        }));
    }

    private static DomElement getElementById(HtmlPage page, String elementId) {
        DomElement element = page.getElementById(elementId);
        checkState(element != null, "No element with id %s on page %s", elementId, page.getUrl());
        return element;
    }
}
