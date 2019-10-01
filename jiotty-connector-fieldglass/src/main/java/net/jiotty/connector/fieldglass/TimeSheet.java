package net.jiotty.connector.fieldglass;

import com.google.common.base.MoreObjects;
import com.opencsv.bean.AbstractBeanField;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvCustomBindByName;
import com.opencsv.exceptions.CsvDataTypeMismatchException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

public final class TimeSheet {
    @CsvCustomBindByName(column = "Status", required = true, converter = Status.Converter.class)
    private Status status;
    @CsvBindByName(column = "ID", required = true)
    private String id;
    @CsvCustomBindByName(column = "Start Date", required = true, converter = LocalDateConverter.class)
    private LocalDate startDate;
    @CsvCustomBindByName(column = "End", required = true, converter = LocalDateConverter.class)
    private LocalDate endDate;
    @CsvBindByName(column = "ST", required = true)
    private double standardDays;
    @CsvBindByName(column = "Others", required = true)
    private double otherDays;

    public TimeSheet() {
    }

    // Unit testing
    public TimeSheet(Status status, String id, LocalDate startDate, LocalDate endDate, double standardDays, double otherDays) {
        this.status = checkNotNull(status);
        this.id = checkNotNull(id);
        this.startDate = checkNotNull(startDate);
        this.endDate = checkNotNull(endDate);
        this.standardDays = standardDays;
        this.otherDays = otherDays;
    }

    public Status getStatus() {
        return status;
    }

    public String getId() {
        return id;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public double getStandardDays() {
        return standardDays;
    }

    public double getOtherDays() {
        return otherDays;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeSheet timeSheet = (TimeSheet) o;
        return standardDays == timeSheet.standardDays &&
                otherDays == timeSheet.otherDays &&
                status == timeSheet.status &&
                id.equals(timeSheet.id) &&
                startDate.equals(timeSheet.startDate) &&
                endDate.equals(timeSheet.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, id, startDate, endDate, standardDays, otherDays);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("status", status)
                .add("id", id)
                .add("startDate", startDate)
                .add("endDate", endDate)
                .add("standardDays", standardDays)
                .add("otherDays", otherDays)
                .toString();
    }

    public enum Status {
        Approved, PendingApproval, Draft;

        public static final class Converter extends AbstractBeanField<Status> {
            @Override
            protected Object convert(String value) throws CsvDataTypeMismatchException {
                switch (value) {
                    case "Approved":
                        return Approved;
                    case "Pending Approval":
                        return PendingApproval;
                    case "Draft":
                        return Draft;
                    default:
                        throw new CsvDataTypeMismatchException(value, Status.class);
                }
            }
        }
    }

    public static class LocalDateConverter extends AbstractBeanField<LocalDate> {
        @Override
        protected Object convert(String value) throws CsvDataTypeMismatchException {
            try {
                return LocalDate.parse(value);
            } catch (DateTimeParseException e) {
                throw new CsvDataTypeMismatchException(value, LocalDate.class, e.getMessage());
            }
        }
    }
}
