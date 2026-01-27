package de.qaware.ekg.awb.repository.api;

/**
 * Provider that creates and manages {@link RepositoryClient}s and {@link SearchIndexAdminClient}s.
 */
public interface SearchIndexClientProvider {

    /**
     * Returns a {@link RepositoryClient} bound to the given collection. Make sure to
     * {@link RepositoryClient#close()} the client after usage.
     *
     * @param collection the collection
     * @return the {@link RepositoryClient}
     */
    RepositoryClient getClient(String collection);

    /**
     * Returns a {@link SearchIndexAdminClient}. Make sure to {@link SearchIndexAdminClient#close()} the client after
     * usage.
     *
     * @return the {@link SearchIndexAdminClient}.
     */
    SearchIndexAdminClient getAdminClient();
}
