package de.qaware.ekg.awb.importer.owidcovidonline.events;

import lombok.Getter;

import java.util.EventObject;

/**
 * Class representing that an error occurred during the download of the covid data.
 */
@Getter
public class DownloadErrorEvent extends EventObject {
    private static final long serialVersionUID = -6264362752346556867L;

    private final String message;

    /**
     * Constructor.
     *
     * @param message error message
     * @param source  object on which the event occurs
     */
    public DownloadErrorEvent(String message, Object source) {
        super(source);
        this.message = message;
    }

}
