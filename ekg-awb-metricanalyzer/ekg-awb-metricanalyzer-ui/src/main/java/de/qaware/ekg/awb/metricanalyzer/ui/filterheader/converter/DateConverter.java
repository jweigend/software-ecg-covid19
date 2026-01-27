//______________________________________________________________________________
//
//          ProjectConfiguration:    Software EKG
//______________________________________________________________________________
//
//         Author:      QAware GmbH 2009 - 2021
//______________________________________________________________________________
//
// Notice: This piece of software was created, designed and implemented by
// experienced craftsmen and innovators in Munich, Germany.
// Changes should be done with respect to the original design.
//______________________________________________________________________________
package de.qaware.ekg.awb.metricanalyzer.ui.filterheader.converter;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * This class converts Instant objects to LocalDate and vice versa.
 * <p>
 * The original object itself is never changed.
 */
public final class DateConverter {

    private static final int MILLIS_PER_SECOND = 1000;
    private static final int MILLIS_PER_DAY = 86400 * MILLIS_PER_SECOND;

    /**
     * Utility-Class should not be instantiated.
     */
    private DateConverter() {
    }

    /**
     * Converts an Instant to LocalDate.
     *
     * @param start the Instant object to be converted
     * @return a new LocalDate object
     */
    public static LocalDate convertDateToLocalDate(Instant start) {
        if (start == null) {
            return LocalDate.now();
        }
        //CET is not correct if we are in Germany
        LocalDateTime localDateTime = LocalDateTime.ofInstant(start, ZoneOffset.UTC);

        return localDateTime.toLocalDate();
    }

    /**
     * Converts a LocalDate to Instant.
     *
     * @param start the LocalDate object to be converted, may be null.
     *              If null, the current date will be returned.
     * @return a new Instant object
     */
    public static Instant convertLocalDateToDate(LocalDate start) {
        if (start == null) {
            return Instant.now();
        }
        return Instant.ofEpochMilli(start.toEpochDay() * MILLIS_PER_DAY);
    }

    /**
     * Takes an Instant and sets the last second of the current day for it.
     *
     * @param date the Instant object to take
     * @return a new Instant with the last second of the day
     */
    public static Instant setDateToEndOfDay(Instant date) {
        Calendar c = new GregorianCalendar();
        c.setTimeZone(TimeZone.getTimeZone("CET"));//Important: Don't use default TimeZone!
        c.setTime(Date.from(date));
        c.set(Calendar.HOUR_OF_DAY, 23);//Important: Use 24-Hour-Format
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);

        Instant r = c.toInstant();
        //Delete millis
        return Instant.ofEpochMilli(r.toEpochMilli() - (r.toEpochMilli() % MILLIS_PER_SECOND));
    }
}
