package net.jiotty.connector.fieldglass;

import com.opencsv.bean.CsvToBeanBuilder;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.List;

import static net.jiotty.connector.fieldglass.TimeSheet.Status.Approved;
import static net.jiotty.connector.fieldglass.TimeSheet.Status.PendingApproval;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

class TimeSheetTest {
    @Test
    void decodesTimeSheet() {
        List<TimeSheet> timeSheets = new CsvToBeanBuilder<TimeSheet>(new InputStreamReader(getClass().getResourceAsStream("/timesheets.csv")))
                .withType(TimeSheet.class)
                .build()
                .parse();
        assertThat(timeSheets, contains(
                new TimeSheet(Approved, "id1", LocalDate.of(2019, 7, 22), LocalDate.of(2019, 7, 28), 5, 1),
                new TimeSheet(PendingApproval, "id2", LocalDate.of(2019, 7, 29), LocalDate.of(2019, 8, 4), 4, 0)
        ));
    }
}