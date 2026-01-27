package de.qaware.ekg.awb.metricanalyzer.bl.bookmarks;

import de.qaware.ekg.awb.metricanalyzer.bl.bookmarks.et.Bookmark;
import de.qaware.ekg.awb.metricanalyzer.bl.bookmarks.et.BookmarkGroup;
import de.qaware.ekg.awb.metricanalyzer.bl.bookmarks.et.MetricBookmark;
import de.qaware.ekg.awb.repository.api.RepositoryClient;
import de.qaware.ekg.awb.repository.api.dataobject.delete.DeleteParams;
import de.qaware.ekg.awb.repository.api.dataobject.expr.ExprFactory;
import de.qaware.ekg.awb.repository.api.dataobject.search.SearchParams;
import de.qaware.ekg.awb.repository.api.dataobject.search.SearchResult;
import de.qaware.ekg.awb.repository.api.dataobject.search.SortField;
import de.qaware.ekg.awb.repository.api.schema.DocumentType;
import de.qaware.ekg.awb.repository.api.schema.EkgSchemaField;
import de.qaware.ekg.awb.sdk.awbapi.repository.RepositoryException;
import de.qaware.ekg.awb.sdk.core.log.EkgLogger;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import javax.enterprise.inject.Default;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static de.qaware.ekg.awb.repository.api.dataobject.expr.ExprFactory.exactFilter;

/**
 * The stub for the metrics bookmark service that has to implement
 */
@Default
public class MetricsBookmarkServiceImpl implements MetricsBookmarkService {

    private Logger LOGGER = EkgLogger.get();

    private RepositoryClient repositoryClient;

    @Override
    public List<MetricBookmark> getMetricBookmarks(String bookmarkGroupId) {

        List<MetricBookmark> result = new ArrayList<>();

        try {
            SearchParams searchParams = new SearchParams()
                    .withFilterQueries(exactFilter(EkgSchemaField.DOC_TYPE, DocumentType.METRIC_BOOKMARK.toString()))
                    .withSortField(EkgSchemaField.BOOKMARK_NAME, SortField.SortMode.DESC);

            if (StringUtils.isNotBlank(bookmarkGroupId)) {
                searchParams.withFilterQueries(ExprFactory.exactFilter(EkgSchemaField.BOOKMARK_GROUP_ID, bookmarkGroupId));
            }

            SearchResult<MetricBookmark> searchResult = repositoryClient.search(MetricBookmark.class, searchParams);

            result = searchResult.getRows().stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

        } catch (RepositoryException e) {
            LOGGER.error("Exception raised while getting the types from solr.", e);
        }

        return result;
    }

    @Override
    public void persistNewBookmark(Bookmark bookmark) {
        try {
            repositoryClient.add(bookmark);
            repositoryClient.commit();
        } catch (RepositoryException ex) {
            LOGGER.error("Exception raised at saving the bookmark", ex);
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public void updateBookmark(Bookmark bookmark) {

    }

    @Override
    public void deleteBookmark(Bookmark bookmark) {
        try {
            DeleteParams deleteParams = new DeleteParams()
                    .addFilter(EkgSchemaField.ID, bookmark.getId())
                    .addFilter(EkgSchemaField.DOC_TYPE, DocumentType.METRIC_BOOKMARK.toString());

            repositoryClient.delete(deleteParams);
            repositoryClient.commit();

        } catch (RepositoryException ex) {
            LOGGER.error("Exception raised while getting the types from EKG repository.", ex);
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public List<BookmarkGroup> getBookmarkGroups() {
         try {
            SearchParams searchParams = new SearchParams()
                    .withFilterQueries(exactFilter(EkgSchemaField.DOC_TYPE, DocumentType.BOOKMARK_GROUP.toString()))
                    .withSortField(EkgSchemaField.BOOKMARK_NAME, SortField.SortMode.ASC);

            SearchResult<BookmarkGroup> searchResult = repositoryClient.search(BookmarkGroup.class, searchParams);

            return searchResult.getRows().stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

        } catch (RepositoryException ex) {
            LOGGER.error("Exception raised while getting the bookmark groups from the EKG repository.", ex);
            throw new IllegalStateException(ex);

        }
    }

    @Override
    public void persistNewBookmarkGroup(BookmarkGroup bookmarkGroup) {
        try {
            repositoryClient.add(bookmarkGroup);
            repositoryClient.commit();
        } catch (RepositoryException ex) {
            LOGGER.error("Exception raised at saving the bookmark group", ex);
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public void updateBookmark(BookmarkGroup bookmarkGroup) {
        try {
            DeleteParams deleteParams = new DeleteParams()
                    .addFilter(EkgSchemaField.ID, bookmarkGroup.getId())
                    .addFilter(EkgSchemaField.DOC_TYPE, DocumentType.BOOKMARK_GROUP.toString());

            repositoryClient.delete(deleteParams);
            repositoryClient.add(bookmarkGroup);
            repositoryClient.commit();
        } catch (RepositoryException ex) {
            LOGGER.error("Exception raised at saving the bookmark group", ex);
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public void deleteBookmarkGroup(String bookmarkId) {
        try {
            DeleteParams deleteParams = new DeleteParams()
                    .addFilter(EkgSchemaField.BOOKMARK_GROUP_ID, bookmarkId)
                    .addFilter(EkgSchemaField.DOC_TYPE, DocumentType.BOOKMARK_GROUP.toString());

            repositoryClient.delete(deleteParams);

            deleteParams = new DeleteParams()
                    .addFilter(EkgSchemaField.BOOKMARK_GROUP_ID, bookmarkId)
                    .addFilter(EkgSchemaField.DOC_TYPE, DocumentType.METRIC_BOOKMARK.toString());

            repositoryClient.delete(deleteParams);

            repositoryClient.commit();
        } catch (RepositoryException ex) {
            LOGGER.error("Exception raised at saving the bookmark group", ex);
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public void initializeService(RepositoryClient client) {
        this.repositoryClient = client;
    }
}
