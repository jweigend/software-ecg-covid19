//______________________________________________________________________________
//
//                  Project:    Software EKG
//______________________________________________________________________________
//
//                   Author:    QAware GmbH 2009 - 2021
//______________________________________________________________________________
//
// Notice: This piece of software was created, designed and implemented by
// experienced craftsmen and innovators in Munich, Germany.
// Changes should be done with respect to the original design.
//______________________________________________________________________________
package de.qaware.ekg.awb.common.ui.components;

import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;

/**
 * This class helps you saving images from Regions.
 */
public class ScreenshotAssistant {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScreenshotAssistant.class);

    /**
     * This is the chart we take the
     * snapshots and images from.
     */
    private Region region;

    /**
     * Constructor for the ImageSaving Assistant.
     *
     * @param region - The region to take the snapshots from
     */
    public ScreenshotAssistant(Region region) {
        this.region = region;
    }

    /**
     * Saves the region as a picture into a file. The file will be stored with the
     * PNG method of ImageIO, but the format can be chosen by the user. It is
     * suggested to use the PNG format!
     */
    public void saveChartAsPicture() {
        File target = targetFileChoosing();
        try {
            RenderedImage renderedImage = null;
            ImageIO.write(renderedImage, "png", target);
        } catch (IOException e) {
            LOGGER.warn("Can not save chart as image.", e);
        }
    }

    /**
     * Creates a BufferedImage, ready to be saved to a file.
     *
     * @return A snapshot from the chart in the format of a WritableImage.
     */
    private WritableImage createImage() {
        WritableImage writable = new WritableImage((int) Math.floor(region.getWidth()),
                (int) Math.floor(region.getHeight()));
        return region.snapshot(new SnapshotParameters(), writable);
    }

    /**
     * User can choose via a FileChooser where the user wants to save the current region.
     *
     * @return target where the file should be saved.
     */
    File targetFileChoosing() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose folder where to save the graph");

        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Picture files", "*.png", "*.jpg", ".bmp"),
                new FileChooser.ExtensionFilter("All files", "*")
        );

        return chooser.showSaveDialog(null);
    }
}
