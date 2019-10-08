package net.yudichev.jiotty.common.rest;

import net.yudichev.jiotty.common.lang.MoreThrowables;
import okhttp3.*;
import okio.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.net.HttpHeaders.LOCATION;

//Until https://github.com/square/okhttp/issues/3111 is done, here's the ugly workaround for redirects.
final class RedirectSupportInterceptor implements Interceptor {
    private static final Logger logger = LoggerFactory.getLogger(RedirectSupportInterceptor.class);
    private final Supplier<OkHttpClient> clientProvider;

    RedirectSupportInterceptor(Supplier<OkHttpClient> clientProvider) {
        this.clientProvider = checkNotNull(clientProvider);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        long t1 = System.nanoTime();
        logger.debug(String.format("Sending request %s %s%n%s",
                request.url(), request.headers(), new Object() {
                    @Override
                    public String toString() {
                        return bodyToString(request);
                    }
                }));

        Response response = chain.proceed(request);

        long t2 = System.nanoTime();
        logger.debug(String.format("Received response for %s in %.1fms%n%s",
                response.request().url(), (t2 - t1) / 1.0e6d, response.headers()));

        if ((response.code() == 307) || (response.code() == 308)) {
            String location = response.header(LOCATION);
            if (location != null) {
                Request redirectedRequest = request.newBuilder().url(location).build();
                return clientProvider.get().newCall(redirectedRequest).execute();
            }
        }
        return response;
    }

    @SuppressWarnings({"resource", "IOResourceOpenedButNotSafelyClosed"}) // code copied from somewhere, not 100% adhering to our style
    private static String bodyToString(Request request) {
        Buffer sink = new Buffer();
        RequestBody requestBody = request.body();
        if (requestBody == null) {
            return "";
        } else {
            return MoreThrowables.getAsUnchecked(() -> {
                requestBody.writeTo(sink);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                sink.copyTo(bos);
                return new String(bos.toByteArray());
            });
        }
    }
}
