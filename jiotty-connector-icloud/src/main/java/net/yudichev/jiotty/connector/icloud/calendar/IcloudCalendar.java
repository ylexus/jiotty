package net.yudichev.jiotty.connector.icloud.calendar;

import com.github.caldav4j.CalDAVCollection;
import com.github.caldav4j.methods.CalDAV4JMethodFactory;
import com.github.caldav4j.model.request.CalendarData;
import com.github.caldav4j.model.request.CalendarQuery;
import com.github.caldav4j.model.request.CompFilter;
import com.github.caldav4j.model.request.TimeRange;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Content;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;
import net.yudichev.jiotty.common.async.SchedulingExecutor;
import net.yudichev.jiotty.common.lang.MoreThrowables;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.google.common.base.Preconditions.checkNotNull;

final class IcloudCalendar implements Calendar {
    private static final Logger logger = LoggerFactory.getLogger(IcloudCalendar.class);

    private final String id;
    private final String name;
    private final SchedulingExecutor executor;
    private final Supplier<CloseableHttpClient> httpClientFactory;
    private final CalDAVCollection calDAVCollection;

    IcloudCalendar(String calendarHomerUrl, String href, String name, SchedulingExecutor executor, Supplier<CloseableHttpClient> httpClientFactory) {
        id = checkNotNull(href);
        this.name = checkNotNull(name);
        this.executor = checkNotNull(executor);
        this.httpClientFactory = checkNotNull(httpClientFactory);

        calDAVCollection = new CalDAVCollection(replacePath(calendarHomerUrl, href));
        var methodFactory = new CalDAV4JMethodFactory();
        calDAVCollection.setMethodFactory(methodFactory);
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public CompletableFuture<List<CalendarEvent>> fetchEvents(Instant from, Instant to) {
        return executor.submit(() -> {
            logger.info("Calendar {}: fetching events for {}...{}", name, from, to);
            List<net.fortuna.ical4j.model.Calendar> cals;
            try (var httpClient = httpClientFactory.get()) {

                // Build time range: now -> next 24h
                var start = new DateTime(java.util.Date.from(from));
                start.setUtc(true);
                var end = new DateTime(java.util.Date.from(to));
                end.setUtc(true);

                // Build comp-filters for VCALENDAR/VEVENT
                var eventFilter = new CompFilter("VEVENT");
                eventFilter.setTimeRange(new TimeRange(start, end));
                var calendarFilter = new CompFilter("VCALENDAR");
                calendarFilter.addCompFilter(eventFilter);

                // Request full properties including calendar data
                var calData = new CalendarData(CalendarData.EXPAND, start, end, null);
                var query = new CalendarQuery(calendarFilter,
                                              calData,
                                              false,  // allprop = false
                                              false   /* propName = false */);

                // Execute REPORT
                if (logger.isDebugEnabled()) {
                    Document doc = query.createNewDocument();
                    var sw = new StringWriter();
                    Transformer tf = TransformerFactory.newInstance().newTransformer();
                    tf.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                    tf.setOutputProperty(OutputKeys.INDENT, "no");
                    tf.transform(new DOMSource(doc), new StreamResult(sw));
                    logger.debug("Executing query {}", sw);
                }
                cals = calDAVCollection.queryCalendars(httpClient, query);
            }

            // Iterate VEVENTs
            var resultBuilder = ImmutableList.<CalendarEvent>builder();
            for (net.fortuna.ical4j.model.Calendar cal : cals) {
                for (Component comp : cal.getComponents(Component.VEVENT)) {
                    VEvent evt = (VEvent) comp;
                    resultBuilder.add(CalendarEvent.builder()
                                                   .setStart(evt.getDateTimeStart().getDate())
                                                   .setEnd(evt.getDateTimeEnd().getDate())
                                                   .setSummary(evt.getSummary().getValue())
                                                   .setDescription(evt.getProperty(Property.DESCRIPTION).map(Content::getValue))
                                                   .setLocation(evt.getProperty(Property.LOCATION).map(Content::getValue))
                                                   .build());
                }
            }
            var result = resultBuilder.build();
            logger.info("Calendar {}: fetched {} events", name, result.size());
            return result;
        });
    }

    /**
     * Build a new URL string by replacing the path portion of the given URL.
     */
    private static String replacePath(String originalUrl, String newPath) {
        return MoreThrowables.getAsUnchecked(() -> {
            URI uri = new URI(originalUrl);
            URI replaced = new URI(uri.getScheme(), uri.getAuthority(), newPath, null, null);
            return replaced.toString();
        });
    }

    @Override
    public String toString() {
        var sb = new StringBuilder("IcloudCalendar{");
        sb.append("id='").append(id).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
