package de.qaware.ekg.awb.importer.owidcovidonline.events;

import java.io.File;

/**
 * Event to request a manual CSV file import.
 * This event is published by the UI and consumed by the importer module.
 */
public class ManualCsvImportRequestEvent {

    private final Object source;
    private final File csvFile;

    /**
     * Constructor.
     *
     * @param source The source of the event
     * @param csvFile The CSV file to import
     */
    public ManualCsvImportRequestEvent(Object source, File csvFile) {
        this.source = source;
        this.csvFile = csvFile;
    }

    /**
     * Gets the source of the event.
     *
     * @return The source
     */
    public Object getSource() {
        return source;
    }

    /**
     * Gets the CSV file to import.
     *
     * @return The CSV file
     */
    public File getCsvFile() {
        return csvFile;
    }
}
