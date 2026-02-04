package de.qaware.ekg.awb.importer.owidcovidonline;

import de.qaware.ekg.awb.importer.owidcovidonline.bl.OwidCovidOnlineImportStrategy;
import de.qaware.ekg.awb.importer.owidcovidonline.events.*;
import de.qaware.ekg.awb.metricanalyzer.bl.bookmarks.MetricsBookmarkService;
import de.qaware.ekg.awb.metricanalyzer.bl.bookmarks.et.BookmarkGroup;
import de.qaware.ekg.awb.metricanalyzer.bl.bookmarks.et.MetricBookmark;
import de.qaware.ekg.awb.project.api.ProjectConfiguration;
import de.qaware.ekg.awb.project.api.ProjectDataAccessService;
import de.qaware.ekg.awb.project.api.model.Project;
import de.qaware.ekg.awb.repository.bl.repositories.SolrEmbeddedRepository;
import de.qaware.ekg.awb.sdk.awbapi.project.CloudPlatformType;
import de.qaware.ekg.awb.sdk.awbapi.project.ProjectFlavor;
import de.qaware.ekg.awb.sdk.awbapi.repository.AliasMappingDefinition;
import de.qaware.ekg.awb.sdk.awbapi.repository.FilterDimension;
import de.qaware.ekg.awb.sdk.awbapi.repository.SeriesImportService;
import de.qaware.ekg.awb.sdk.core.events.AwbErrorEvent;
import de.qaware.ekg.awb.sdk.core.events.EkgEventBus;
import de.qaware.ekg.awb.sdk.core.log.EkgLogger;
import de.qaware.ekg.awb.sdk.core.lookup.EkgLookup;
import de.qaware.ekg.awb.sdk.core.lookup.ServiceDiscovery;
import de.qaware.ekg.awb.sdk.importer.tasks.LocalProjectImportTask;
import javafx.concurrent.Task;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

import java.io.*;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Download the owid data from github and start the importing of these data.
 */
public class DownloadAndImportTask extends Task<Void> {

    private static final int CONNECTION_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 8000;

    // OWID COVID data will be downloaded to and imported from the execution directory (uncompressed)
    private static final String COVID_CSV_FILENAME = "covid-data.csv";

    //Fallback data are stored in the data directory (compressed)
    private static final String COVID_CSV_FALLBACK_FILENAME = "data/covid-fallback-data.csv.gz";

    // The download link of the OWID COVID data (GitHub raw URL)
    private static final String DOWNLOAD_URL = "https://raw.githubusercontent.com/owid/covid-19-data/master/public/data/owid-covid-data.csv";

    // Date formatter to set the date of the project
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    .withLocale(Locale.getDefault())
                    .withZone(ZoneId.systemDefault());

    private static final Logger LOGGER = EkgLogger.get();

    private final OwidCovidOnlineImportModule importModule;

    private final EkgEventBus eventBus = EkgLookup.lookup(EkgEventBus.class);

    /**
     * Constructor.
     *
     * @param importModule module containing information to recreate a project
     */
    public DownloadAndImportTask(OwidCovidOnlineImportModule importModule) {
        this.importModule = importModule;
    }

    /**
     * Starting the Task in a thread.
     */
    public void start() {
        new Thread(this).start();
    }

    /**
     * Download and import the owid covid data.
     */
    @Override
    protected Void call() {
        // starts the download of the covid data
        boolean successfully = download();

        if (successfully) {
            importData(COVID_CSV_FILENAME, LocalDate.now());
        } else {
            LOGGER.warn("Could not download the COVID-19 Data");

            if (existsProject(getProjectPrefixName())) {
                eventBus.publish(new DownloadErrorEvent(
                        "Download of latest COVID-19 data has failed. " +
                                "You can keep working with already imported data.",
                        this));
            } else {
                if (new File(COVID_CSV_FALLBACK_FILENAME).exists()) {
                    LocalDate lastEntryDate = CsvHelper.readNewestDate(new File(COVID_CSV_FALLBACK_FILENAME));
                    eventBus.publish(new DownloadErrorEvent(String.format(
                            "Download of latest COVID-19 data has failed. " +
                                    "A local data set from %s will be imported", DATE_FORMATTER.format(lastEntryDate)),
                            this));

                    importData(COVID_CSV_FALLBACK_FILENAME, lastEntryDate);
                } else {
                    Exception e = new FileNotFoundException(String.format("Could not find the fallback CSV file '%s'", COVID_CSV_FALLBACK_FILENAME));
                    EkgLogger.get().error("Error on import", e);
                    eventBus.publish(new AwbErrorEvent(this, "Could not find the fallback CSV file.", e));
                }
            }
        }

        publishFinishEvent();
        return null;
    }

    private void publishFinishEvent() {
        eventBus.publish(new DownloadAndImportFinishedEvent(this));
    }

    /**
     * Importing the Covid data from the given file. It creates also the project and if an old project exists, it removes the old project.
     *
     * @param file The path to the covid data file.
     * @param date The date from which the covid data is. It is added to the project name.
     */
    @SneakyThrows
    private void importData(String file, LocalDate date) {
        eventBus.publish(new ImportProgressEvent(this, String.format("Importing OWID data from '%s'", file)));

        if (!CsvHelper.validateCsvHeaders(new File(file), CsvHelper.EXPECTED_HEADERS)) {
            eventBus.publish(new ImportErrorEvent(this, "Aborted importing, because of invalid csv. Maybe the format of the CSV has changed!"));
            return;
        }

        // delete and create the project.
        Project project = recreateProject(date);

        OwidCovidOnlineImportStrategy owidCovidImportStrategy = new OwidCovidOnlineImportStrategy(List.of(file));
        LocalProjectImportTask task = owidCovidImportStrategy.createImportTask(
                ServiceDiscovery.lookup(SeriesImportService.class,
                        EkgLookup.lookup(SolrEmbeddedRepository.class)),
                new ProjectConfiguration(project, true)
        );
        task.setOnFailed(v -> eventBus.publish(new ImportErrorEvent(this, "We had an error during the import of the data.")));
        task.start();

        // wait until task is finished, so we stay in the execution "flow" of the code.
        task.get();

        eventBus.publish((new ImportProgressEvent(this, "Finished OWID data import")));
    }

    /**
     * Deletes the old project and creates a new empty project, where we can add the import data.
     * The project name will be postfixed with a date.
     *
     * @param date The date for the project. It will be added as a postfix to the project name.
     * @return The new created project.
     */
    private Project recreateProject(LocalDate date) {
        // fetch and initialize the object to delete and store project data.
        ProjectDataAccessService service = EkgLookup.lookup(ProjectDataAccessService.class);
        service.initializeService(EkgLookup.lookup(SolrEmbeddedRepository.class).getRepositoryClient());

        // setup Project object
        Project project = new Project(
                getProjectName(date), "", ProjectFlavor.CLASSIC.toString(), CloudPlatformType.NONE.toString(), false);
        project.setImporterId(importModule.getImporterID());
        replaceDimensionNaming(project);

        // delete project and create new one. Also cache bookmarks, so that the new project can use the bookmarks
        // on deleting of the project, all associated bookmarks are deleted.
        List<MetricBookmark> bookmarks = loadBookmarks(getProjectPrefixName());
        deleteProjectWithPrefix(service, getProjectPrefixName());
        service.persistProject(project);
        saveBookmarks(bookmarks, project.getName());

        return project;
    }

    /**
     * Loads all bookmarks for the project with the given prefix.
     *
     * @param projectPrefixName The project name without the date, to find all bookmarks which belongs to the project.
     * @return All projects which belongs to the project
     */
    private List<MetricBookmark> loadBookmarks(String projectPrefixName) {
        List<MetricBookmark> results = new ArrayList<>();

        MetricsBookmarkService service = EkgLookup.lookup(MetricsBookmarkService.class);
        service.initializeService(EkgLookup.lookup(SolrEmbeddedRepository.class).getRepositoryClient());

        List<String> groupIds = service.getBookmarkGroups().stream()
                .map(BookmarkGroup::getBookmarkGroupId)
                .collect(Collectors.toList());
        // Default Group is not returned by the bookmark groups
        groupIds.add(BookmarkGroup.DEFAULT_GLOBAL_GROUP_ID);
        for (String groupId : groupIds) {
            List<MetricBookmark> bookmarks = service.getMetricBookmarks(groupId);
            // don't add bookmarks, which does not belong to the project
            bookmarks.removeIf(b -> !b.getProjectName().startsWith(projectPrefixName));
            if (!bookmarks.isEmpty()) {
                results.addAll(bookmarks);
            }
        }
        return results;
    }

    /**
     * Writing back the bookmarks for the given project.
     * It also fix the command, that the command is accessing the right project. Therefore the full projectname is needed as an parameter.
     *
     * @param bookmarks   The previously loaded bookmarks.
     * @param projectName The full projectname (with date), to fix the command.
     */
    private void saveBookmarks(List<MetricBookmark> bookmarks, String projectName) {
        MetricsBookmarkService service = EkgLookup.lookup(MetricsBookmarkService.class);
        service.initializeService(EkgLookup.lookup(SolrEmbeddedRepository.class).getRepositoryClient());

        bookmarks.forEach(bookmark -> {
            // we must copy the bookmark, because it is not possible to set the project name, also we must fix the SerializedCommand to use the new project name
            String fixedSerializedCommand = bookmark.getSerializedCommandProtocol()
                    .replaceAll(getProjectPrefixName() + "[0-9]{4}-[0-9]{2}-[0-9]{2}", projectName);
            MetricBookmark copiedBookmark = new MetricBookmark.Builder()
                    .withProjectName(projectName)
                    .withCommandProtocol(fixedSerializedCommand)
                    .withName(bookmark.getName())
                    .build();
            // bug in the builder, it doesn't set the bookmark group id
            copiedBookmark.setBookmarkGroupId(bookmark.getBookmarkGroupId());

            service.persistNewBookmark(copiedBookmark);
        });
    }

    /**
     * Deletes the projects which starts with the prefix
     *
     * @param service The {@link ProjectDataAccessService}, to access the list of projects.
     * @param prefix  The name of the project without the date.
     */
    private void deleteProjectWithPrefix(ProjectDataAccessService service, String prefix) {
        List<String> toDelete = new ArrayList<>();
        for (Project project : service.listProjects()) {
            if (project.getName().startsWith(prefix)) {
                toDelete.add(project.getName());
            }
        }

        toDelete.forEach(service::deleteProjectByName);
    }

    /**
     * Checking whether a project exists with the given prefix. The prefix is the project name without the date.
     *
     * @param prefix The name of the project without date information.
     * @return <code>True</code> if the project exist, otherwise <code>false</code>
     */
    private boolean existsProject(String prefix) {
        ProjectDataAccessService service = EkgLookup.lookup(ProjectDataAccessService.class);
        service.initializeService(EkgLookup.lookup(SolrEmbeddedRepository.class).getRepositoryClient());

        for (Project p : service.listProjects()) {
            if (p.getName().startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sets the alias dimension for the given project.
     * Normally it would be set by the EKG-AWB-Code, but we do the creating of the project and importing of the data
     * automatically on the startup, so we must add this information here.
     *
     * @param project The project object, where to set the alias dimension
     */
    private void replaceDimensionNaming(Project project) {
        AliasMappingDefinition definition = importModule.getAliasMappingDefinition();
        project.setDimensionAliasHostGroup(definition.getAliasLabel(FilterDimension.HOST_GROUP));
        project.setDimensionAliasHost(definition.getAliasLabel(FilterDimension.HOST));
        project.setDimensionAliasNamespace(definition.getAliasLabel(FilterDimension.NAMESPACE));
        project.setDimensionAliasService(definition.getAliasLabel(FilterDimension.SERVICE));
        project.setDimensionAliasPod(definition.getAliasLabel(FilterDimension.POD));
        project.setDimensionAliasContainer(definition.getAliasLabel(FilterDimension.CONTAINER));
        project.setDimensionAliasMeasurement(definition.getAliasLabel(FilterDimension.MEASUREMENT));
        project.setDimensionAliasProcess(definition.getAliasLabel(FilterDimension.PROCESS));
        project.setDimensionAliasMetricGroup(definition.getAliasLabel(FilterDimension.METRIC_GROUP));
        project.setDimensionAliasMetricName(definition.getAliasLabel(FilterDimension.METRIC_NAME));
    }

    /**
     * Downloads the covid owid data and stores it uncompressed.
     */
    private boolean download() {
        try {
            eventBus.publish(new DownloadProgressEvent(this, String.format("Start downloading OWID-Data from '%s'", DOWNLOAD_URL), "Downloading OWID-Data"));

            URL downloadURL = new URL(DOWNLOAD_URL);
            File destination = new File(COVID_CSV_FILENAME);
            
            // Download the CSV file directly without compression
            try (InputStream inputStream = downloadURL.openStream();
                 FileOutputStream fileOutputStream = new FileOutputStream(destination)) {
                
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead);
                }
            }

            eventBus.publish(new DownloadProgressEvent(this, String.format("Download finished! Stored file to '%s'", COVID_CSV_FILENAME)));
            return true;
        } catch (IOException e) {
            LOGGER.error("Error while downloading file from {}", DOWNLOAD_URL, e);
            return false;
        }
    }

    /**
     * Gets the project name with the given date.
     *
     * @param date The date which should be added to the project name.
     * @return The project name
     */
    private String getProjectName(LocalDate date) {
        return getProjectPrefixName() + DATE_FORMATTER.format(date);
    }

    /**
     * Creates the project prefix. The project name has the format "OWID - {DATE}".
     *
     * @return The project prefix without "{DATE}".
     */
    private String getProjectPrefixName() {
        return "OWID - ";
    }
}

