//______________________________________________________________________________
//
//                  Project:    Software EKG
//______________________________________________________________________________
//
//                   Author:    Weigend AM GmbH & Co KG 2009 - 2025
//______________________________________________________________________________
//
// Notice: This piece of software was created, designed and implemented by
// experienced craftsmen and innovators in Munich, Germany.
// Changes should be done with respect to the original design.
//______________________________________________________________________________
package de.qaware.ekg.awb.explorer.ui;

import de.qaware.ekg.awb.common.ui.explorer.ExplorerController;
import de.qaware.ekg.awb.common.ui.explorer.RootItem;
import de.qaware.ekg.awb.common.ui.explorer.api.AbstractItem;
import de.qaware.ekg.awb.common.ui.explorer.api.ItemBuilderRegistry;
import de.qaware.ekg.awb.common.ui.view.EkgView;
import de.qaware.ekg.awb.commons.module.EkgModule;
import de.qaware.ekg.awb.commons.module.ModuleException;
import de.qaware.ekg.awb.explorer.ui.items.CommonItemBuilder;
import de.qaware.ekg.awb.explorer.ui.items.RepositoryItemsTask;
import de.qaware.ekg.awb.explorer.ui.items.common.RepositoryItem;
import de.qaware.ekg.awb.sdk.core.lookup.EkgLookup;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

/**
 * Register the explorerView view.
 */
public class ExplorerModule implements EkgModule {

    protected EkgView<ExplorerController> explorerView;

    protected ExplorerExtendedController explorerExtController;

    //=================================================================================================================
    //   EkgModule API
    //=================================================================================================================

    /**
     * Preloads the explorer tree on startup of EKG workbench
     *
     * @throws ModuleException thrown than the FXML view couldn't load correctly
     */
    @Override
    public void preload() throws ModuleException {
        try {
            // create builder for repository and project tree items and register it
            registerDefaultTreeItemBuilders();

            // init the explorer view including the common controller
            ExplorerController controller = initExplorerView();

            // create a extended tree controller that do the Workbench specific stuff
            explorerExtController = EkgLookup.lookup(ExplorerExtendedController.class);
            explorerExtController.initWithCommonController(controller);

        } catch (IOException e) {
            throw new ModuleException(e);
        }
    }

    @Override
    public void start() {
        // no op
    }

    @Override
    public void stop() {
        // no op
    }

    //=================================================================================================================
    //   private logic that do the initializing stuff
    //=================================================================================================================

    private ExplorerController initExplorerView() throws IOException {
        explorerView = new EkgView.Builder<ExplorerController>()
                .withId("explorerView")
                .withTitle("Explorer")
                .withPos(EkgView.Position.LEFT)
                .withFile(ExplorerController.class.getResource("ExplorerView.fxml"))
                .withViewAreaSize(0.10)
                .build();

        ExplorerController controller = explorerView.getController();
        controller.setRootItem(EkgLookup.lookup(RootItem.class));
        return controller;
    }


    private void registerDefaultTreeItemBuilders() {
        // retrieve central builder registry
        ItemBuilderRegistry builderRegistry = EkgLookup.lookup(ItemBuilderRegistry.class);

        // static bind of item builder to different parent items
        // by using the central builder registry
        registerRepositoryItemBuilder(builderRegistry); // builder for repository
        registerProjectItemBuilder(builderRegistry);  // builder for projects
    }



    /**
     * Register an item builder for RepositoryItem instances for RootItem parent
     * at the given ItemBuilderRegistry
     *
     * @param builderRegistry the builder registry that should used to register the builders
     */
    private void registerRepositoryItemBuilder(ItemBuilderRegistry builderRegistry) {

        Function<RootItem, List<? extends AbstractItem>> builder =
                rootItem -> EkgLookup.lookup(RepositoryItemsTask.class).getRepositoryItems();

        builderRegistry.registerItemBuilder(RootItem.class, builder);
    }

    /**
     * Register an item builder for ProjectItem instances for RepositoryItem parents
     *  at the given ItemBuilderRegistry
     *
     * @param builderRegistry the builder registry that should used to register the builders
     */
    private void registerProjectItemBuilder(ItemBuilderRegistry builderRegistry) {
        builderRegistry.registerItemBuilder(RepositoryItem.class, CommonItemBuilder::buildProjectItems);
    }
}
