package de.qaware.ekg.awb.importer.owidcovidonline.events;

import java.util.EventObject;

/**
 * Class representing the event when the download of the covid data is finished and imported into the workbench.
 */
public class DownloadAndImportFinishedEvent extends EventObject {
    private static final long serialVersionUID = -319725831410080806L;

    /**
     * Constructor.
     *
     * @param source object on which the event occurs
     */
    public DownloadAndImportFinishedEvent(Object source) {
        super(source);
    }
}
