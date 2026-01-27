package de.qaware.ekg.awb.importer.owidcovidonline.events;

import lombok.Getter;

import java.util.EventObject;

/**
 * Class representing the event when the import started or finished.
 */
@Getter
public class ImportProgressEvent extends EventObject {
    private static final long serialVersionUID = -5001999913368907220L;

    private final String message;
    private final String shortMessage;

    /**
     * Constructor.
     *
     * @param source  object on which the event occurs
     * @param message progress message
     */
    public ImportProgressEvent(Object source, String message) {
        super(source);
        this.message = message;
        this.shortMessage = message;
    }
}
