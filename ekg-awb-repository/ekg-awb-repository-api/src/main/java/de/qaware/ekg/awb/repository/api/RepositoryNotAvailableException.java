package de.qaware.ekg.awb.repository.api;

import de.qaware.ekg.awb.sdk.awbapi.repository.RepositoryException;

/**
 * Specialized type of the {@link RepositoryException} that will thrown
 * than the repository that is the base for a query/request isn't reachable
 * at the moment.
 */
public class RepositoryNotAvailableException extends RepositoryException {

    private Cause cause;

    /**
     * Constructs a new instance of RepositoryNotAvailableException
     *
     * @param cause an enum that details the cause why the repository isn't available
     * @param rootException the root cause like java.net.ConnectException
     */
    public RepositoryNotAvailableException(Cause cause, Throwable rootException) {
        super(rootException);

        this.cause = cause;
    }

    public Cause getNotAvailableCause() {
        return cause;
    }

    @Override
    public String getMessage() {
        if (cause == Cause.INDEX_NOT_AVAILABLE) {
            return "Repository not found.";
        } else {
            return super.getMessage();
        }
    }


    public enum Cause {

        HOST_NOT_AVAILABLE,

        INDEX_NOT_AVAILABLE
    }
}
