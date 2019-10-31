package net.yudichev.jiotty.connector.google.photos;

import com.google.api.gax.paging.Page;
import com.google.api.gax.paging.PagedListResponse;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.emptyToNull;

// Although google photos java API natively supports pages, in my case operations that return thousands of items fail with 502 Bad Gateway
// but work fine if pagination is implemented manually, which is what this class does
final class PagedRequest<T> {
    @SuppressWarnings("NonConstantLogger") // as designed
    private final Logger logger;
    private final Function<Optional<String>, PagedListResponse<T>> requestInvoker;

    PagedRequest(Logger logger, Function<Optional<String>, PagedListResponse<T>> requestInvoker) {
        this.logger = checkNotNull(logger);
        this.requestInvoker = checkNotNull(requestInvoker);
    }

    Stream<T> getAll() {
        String pageToken = null;
        Stream.Builder<T> streamBuilder = Stream.builder();
        do {
            PagedListResponse<T> listResponse = requestInvoker.apply(Optional.ofNullable(pageToken));
            logger.debug("Requesting list, page token [{}]", pageToken);
            Page<T> page = listResponse.getPage();
            logger.debug("Received response to list request, page token [{}], next page token [{}]",
                    pageToken, page.getNextPageToken());
            page.getValues().forEach(streamBuilder::add);
            pageToken = emptyToNull(listResponse.getNextPageToken());
        } while (pageToken != null);
        return streamBuilder.build();
    }
}
