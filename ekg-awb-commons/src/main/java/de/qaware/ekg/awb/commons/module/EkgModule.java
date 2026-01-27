package de.qaware.ekg.awb.commons.module;

/**
 * Module interface to implement the initialization and the shutdown
 * of a module while the platform starts and stops.
 */
public interface EkgModule {

    /**
     * Preload the module while starting the application.
     * <p>
     * It will be executed in an separate thread while showing the splash screen.
     *
     * @throws ModuleException in case of any error while preloading the module.
     */
    void preload() throws ModuleException;

    /**
     * Finally start the application.
     * <p>
     * It is called from the java fx platform thread in an non specific order, while the platform is initializing the
     * main application window. This includes that all modules have executed there preload phase.
     */
    void start();

    /**
     * Stop the module.
     * <p>
     * This method will be called while platform shutdown.
     */
    void stop();
}