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
import de.qaware.ekg.awb.sdk.awbapi.repository.FilterDimension;
import de.qaware.ekg.awb.sdk.awbapi.repository.SeriesImportService;
import de.qaware.ekg.awb.sdk.core.events.EkgEventBus;
import de.qaware.ekg.awb.sdk.core.log.EkgLogger;
import de.qaware.ekg.awb.sdk.core.lookup.EkgLookup;
import de.qaware.ekg.awb.sdk.core.lookup.ServiceDiscovery;
import de.qaware.ekg.awb.sdk.importer.tasks.LocalProjectImportTask;
import javafx.concurrent.Task;
import lombok.SneakyThrows;
import org.slf4j.Logger;

import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Task to manually import a CSV file selected by the user.
 * This task imports data from a local CSV file in the OWID COVID format.
 */
public class ManualCsvImportTask extends Task<Void> {

    private static final String PROJECT_PREFIX_NAME = "CSV Import - ";

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    .withLocale(Locale.getDefault())
                    .withZone(ZoneId.systemDefault());

    private static final Logger LOGGER = EkgLogger.get();

    private final File csvFile;
    private final EkgEventBus eventBus = EkgLookup.lookup(EkgEventBus.class);

    /**
     * Constructor.
     *
     * @param csvFile The CSV file to import
     */
    public ManualCsvImportTask(File csvFile) {
        this.csvFile = csvFile;
    }

    /**
     * Starting the Task in a thread.
     */
    public void start() {
        new Thread(this).start();
    }

    @Override
    protected Void call() {
        try {
            importData();
        } catch (Exception e) {
            LOGGER.error("Error during CSV import", e);
            eventBus.publish(new ImportErrorEvent(this, "Error during CSV import: " + e.getMessage()));
        } finally {
            eventBus.publish(new DownloadAndImportFinishedEvent(this));
        }
        return null;
    }

    /**
     * Importing the CSV data from the selected file.
     */
    @SneakyThrows
    private void importData() {
        String filePath = csvFile.getAbsolutePath();
        LocalDate date = CsvHelper.readNewestDate(csvFile);
        if (date.equals(CsvHelper.DEFAULT_DATE)) {
            date = LocalDate.now();
        }

        eventBus.publish(new ImportProgressEvent(this, String.format("Importing data from '%s'", csvFile.getName())));

        // Create the project
        Project project = recreateProject(date, csvFile.getName());

        OwidCovidOnlineImportStrategy importStrategy = new OwidCovidOnlineImportStrategy(List.of(filePath));
        LocalProjectImportTask task = importStrategy.createImportTask(
                ServiceDiscovery.lookup(SeriesImportService.class,
                        EkgLookup.lookup(SolrEmbeddedRepository.class)),
                new ProjectConfiguration(project, true)
        );
        task.setOnFailed(v -> eventBus.publish(new ImportErrorEvent(this, "Error during data import.")));
        task.start();

        // Wait until task is finished
        task.get();

        eventBus.publish(new ImportProgressEvent(this, "Finished CSV data import"));
    }

    /**
     * Creates a new project for the imported data.
     *
     * @param date The date from the CSV data
     * @param fileName The name of the imported file
     * @return The new project
     */
    private Project recreateProject(LocalDate date, String fileName) {
        ProjectDataAccessService service = EkgLookup.lookup(ProjectDataAccessService.class);
        service.initializeService(EkgLookup.lookup(SolrEmbeddedRepository.class).getRepositoryClient());

        String projectName = getProjectName(date, fileName);
        String projectPrefixName = getProjectPrefixName(fileName);

        // Setup Project object
        Project project = new Project(
                projectName, "", ProjectFlavor.CLASSIC.toString(), CloudPlatformType.NONE.toString(), false);
        project.setImporterId("CSV");
        replaceDimensionNaming(project);

        // Delete existing project with same prefix and create new one
        // Also cache bookmarks, so that the new project can use the bookmarks
        List<MetricBookmark> bookmarks = loadBookmarks(projectPrefixName);
        deleteProjectWithPrefix(service, projectPrefixName);
        service.persistProject(project);
        saveBookmarks(bookmarks, project.getName());

        return project;
    }

    private String getProjectName(LocalDate date, String fileName) {
        return getProjectPrefixName(fileName) + DATE_FORMATTER.format(date);
    }

    private String getProjectPrefixName(String fileName) {
        // Remove .csv.gz or .csv extension if present
        String baseName = fileName;
        if (baseName.toLowerCase().endsWith(".csv.gz")) {
            baseName = baseName.substring(0, baseName.length() - 7);
        } else if (baseName.toLowerCase().endsWith(".csv")) {
            baseName = baseName.substring(0, baseName.length() - 4);
        }
        return PROJECT_PREFIX_NAME + baseName + " - ";
    }

    private void replaceDimensionNaming(Project project) {
        // Set the alias dimensions for OWID COVID data format
        project.setDimensionAliasHostGroup("Continent");
        project.setDimensionAliasHost("Location");
        project.setDimensionAliasMetricGroup("Metric Group");
        project.setDimensionAliasMetricName("Metric Name");
    }

    private List<MetricBookmark> loadBookmarks(String projectPrefixName) {
        List<MetricBookmark> results = new ArrayList<>();

        MetricsBookmarkService service = EkgLookup.lookup(MetricsBookmarkService.class);
        service.initializeService(EkgLookup.lookup(SolrEmbeddedRepository.class).getRepositoryClient());

        List<String> groupIds = service.getBookmarkGroups().stream()
                .map(BookmarkGroup::getBookmarkGroupId)
                .collect(Collectors.toList());
        groupIds.add(BookmarkGroup.DEFAULT_GLOBAL_GROUP_ID);
        
        for (String groupId : groupIds) {
            List<MetricBookmark> bookmarks = service.getMetricBookmarks(groupId);
            bookmarks.removeIf(b -> !b.getProjectName().startsWith(projectPrefixName));
            if (!bookmarks.isEmpty()) {
                results.addAll(bookmarks);
            }
        }
        return results;
    }

    private void saveBookmarks(List<MetricBookmark> bookmarks, String projectName) {
        MetricsBookmarkService service = EkgLookup.lookup(MetricsBookmarkService.class);
        service.initializeService(EkgLookup.lookup(SolrEmbeddedRepository.class).getRepositoryClient());

        bookmarks.forEach(bookmark -> {
            MetricBookmark copiedBookmark = new MetricBookmark.Builder()
                    .withProjectName(projectName)
                    .withCommandProtocol(bookmark.getSerializedCommandProtocol())
                    .withName(bookmark.getName())
                    .build();
            copiedBookmark.setBookmarkGroupId(bookmark.getBookmarkGroupId());
            service.persistNewBookmark(copiedBookmark);
        });
    }

    private void deleteProjectWithPrefix(ProjectDataAccessService service, String prefix) {
        List<String> toDelete = new ArrayList<>();
        for (Project project : service.listProjects()) {
            if (project.getName().startsWith(prefix)) {
                toDelete.add(project.getName());
            }
        }
        toDelete.forEach(service::deleteProjectByName);
    }
}
