package de.qaware.ekg.awb.importer.owidcovidonline.events;

import lombok.Getter;

import java.util.EventObject;

/**
 * Class representing the event when the import of the covid data into the workbench failed.
 */
@Getter
public class ImportErrorEvent extends EventObject {
    private static final long serialVersionUID = 1133006206179140445L;

    private final String message;

    /**
     * Constructor.
     *
     * @param source  object on which the event occurs
     * @param message error message
     */
    public ImportErrorEvent(Object source, String message) {
        super(source);
        this.message = message;
    }

}

