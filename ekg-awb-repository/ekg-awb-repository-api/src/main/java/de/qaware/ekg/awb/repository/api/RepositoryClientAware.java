package de.qaware.ekg.awb.repository.api;

/**
 * Interface implemented by all services that need an EKG repository client
 * to work properly.
 */
public interface RepositoryClientAware {

    void initializeService(RepositoryClient client);
}
