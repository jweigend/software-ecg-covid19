//______________________________________________________________________________
//
//                  ProjectConfiguration:    Software EKG
//______________________________________________________________________________
//
//                   Author:    Weigend AM GmbH & Co KG 2009 - 2025
//______________________________________________________________________________
//
// Notice: This piece of software was created, designed and implemented by
// experienced craftsmen and innovators in Munich, Germany.
// Changes should be done with respect to the original design.
//______________________________________________________________________________
package de.qaware.ekg.awb.metricanalyzer.ui.filterheader.converter;

import de.qaware.ekg.awb.common.ui.converter.Converter;

import java.time.*;
import java.util.Objects;

/**
 * A converter that converts an {@link Instant} from and to {@link LocalDate}.
 * <p>
 * By default converting from {@link LocalDate} to {@link Instant} will return an {@link Instant} object at CET midnight
 * of the given day. If want to set a different time use the {@link InstantToUtcDateConverter#InstantToUtcDateConverter(LocalTime)}
 * constructor to initialize the converter with a custom defined target time.
 * <p>
 * Any conversions are based by the CET time zone.
 */
public class InstantToUtcDateConverter implements Converter<Instant, LocalDate> {
    public static final ZoneId UTC = ZoneId.of("UTC");
    private final LocalTime targetTime;

    /**
     * Initialize a new InstantToLocalDateConverter with midnight as target time.
     */
    public InstantToUtcDateConverter() {
        targetTime = LocalTime.MIN;
    }

    /**
     * Initialize a new InstantToLocalDateConverter with a custom defined target time.
     *
     * @param targetTime The target time when converting from {@link LocalDate} to {@link Instant}.
     */
    public InstantToUtcDateConverter(LocalTime targetTime) {
        Objects.requireNonNull(targetTime, "target time must not be null!");
        this.targetTime = targetTime;
    }

    @Override
    public LocalDate fromFirst(Instant first) {
        if (first == null) {
            return null;
        }
        return LocalDateTime.ofInstant(first, UTC).toLocalDate();
    }

    @Override
    public Instant fromSecond(LocalDate second) {
        if (second == null) {
            return null;
        }
        return second.atTime(targetTime).atZone(UTC).toInstant();
    }
}
