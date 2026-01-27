package de.qaware.ekg.awb.importer.owidcovidonline.events;

import lombok.Getter;

import java.util.EventObject;

/**
 * Class representing the download progress, in particular the start and end of the download.
 */
@Getter
public class DownloadProgressEvent extends EventObject {
    private static final long serialVersionUID = -2352299490696909666L;

    private final String message;

    private final String shortMessage;

    /**
     * Constructor.
     *
     * @param source  object on which the event occurs
     * @param message long message
     */
    public DownloadProgressEvent(Object source, String message) {
        super(source);
        this.message = message;
        this.shortMessage = message;
    }

    /**
     * Constructor.
     *
     * @param source       object on which the event occurs
     * @param message      long message
     * @param shortMessage short message
     */
    public DownloadProgressEvent(Object source, String message, String shortMessage) {
        super(source);
        this.message = message;
        this.shortMessage = shortMessage;
    }
}
