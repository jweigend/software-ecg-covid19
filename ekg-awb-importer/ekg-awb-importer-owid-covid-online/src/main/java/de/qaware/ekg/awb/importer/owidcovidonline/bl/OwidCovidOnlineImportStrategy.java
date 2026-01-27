package de.qaware.ekg.awb.importer.owidcovidonline.bl;

import de.qaware.ekg.awb.sdk.core.events.ProgressEvent;
import de.qaware.ekg.awb.sdk.core.log.EkgLogger;
import de.qaware.ekg.awb.sdk.datamodel.RawSeriesData;
import de.qaware.ekg.awb.sdk.importer.tasks.LocalProjectImportStrategy;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * An import strategy deriving from LocalProjectImportStrategy that parses the data of OWID Covid data to
 * RawSeriesData and provides all further steps instances to proceed this series.
 */
public class OwidCovidOnlineImportStrategy extends LocalProjectImportStrategy {

    private static final Logger LOGGER = EkgLogger.get();

    private final List<String> paths;

    /**
     * Constructs a new instance of the ImportStrategy
     *
     * @param paths a list of paths to the import file locations
     */
    public OwidCovidOnlineImportStrategy(List<String> paths) {
        this.paths = Collections.unmodifiableList(paths);
    }

    @Override
    public Stream<RawSeriesData> readRawSeriesData() {

        // notify other components that the import process begins
        String startMessage = "Importing COVID-19 CSV from '" + paths + "'";
        notifyImportStarted(startMessage);
        LOGGER.info(startMessage);

        // collect all csv files in the source directory and it's sub directories (recursive search)
        List<File> inputFiles = new ArrayList<>();
        paths.forEach(path -> {
            File file = new File(path);

            if (file.isDirectory()) {
                 inputFiles.addAll(FileUtils.listFiles(file, new String[]{"csv", "CSV", "csv.gz", "CSV.GZ"}, true));

            } else if (file.isFile() && (path.toLowerCase(Locale.getDefault()).endsWith("csv") || path.toLowerCase(Locale.getDefault()).endsWith("csv.gz"))) {
                 inputFiles.add(file);
            }
        });

        int numberOfFiles = inputFiles.size();
        AtomicInteger importedFiles = new AtomicInteger(0);

        return inputFiles
                .stream()
                .flatMap(file -> {
                    // notify the progress
                    int fileNumber = importedFiles.incrementAndGet();
                    double progress = fileNumber / (double) numberOfFiles;
                    publish(new ProgressEvent("Importing file " + fileNumber + " of "
                            + numberOfFiles + "...", progress, this));
 
                    return OwidCovidOnlineMetricParser.parseToRawSeriesStream(file);
                });
    }
}