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
package de.qaware.ekg.awb.common.ui.view;

import de.qaware.ekg.awb.sdk.core.lookup.EkgLookup;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

/**
 * This is the default implementation of a window management view. It loads the view from an fxml file and defines the
 * other needed values.
 *
 * @param <C> Defines the type of the controller class.
 */
public final class EkgView<C> {
    private final String id;
    private final String title;
    private final Position defaultPosition;
    private final Parent rootPane;
    private final String toolTipInfo;
    private final double viewAreaSize;
    private final C controller;
    private final URL viewImagePath;

    EkgView(String id, String title, Position defaultPosition, Parent rootPane, String toolTipInfo,
            double viewAreaSize, C controller, URL viewImagePath) {
        this.id = id;
        this.title = title;
        this.defaultPosition = defaultPosition;
        this.rootPane = rootPane;
        this.toolTipInfo = toolTipInfo;
        this.viewAreaSize = viewAreaSize;
        this.controller = controller;
        this.viewImagePath = viewImagePath;
    }

    /**
     * Returns the view id
     * @return the view id
     */
    public String getViewId() {
        return id;
    }

    /**
     * Returns the title
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the tooltip info
     * @return the tooltip info
     */
    public String getToolTipInfo() {
        return toolTipInfo;
    }

    /**
     * Returns the default position
     * @return the default position
     */
    public Position getDefaultPosition() {
        return defaultPosition;
    }

    /**
     * Returns the parent node
     * @return the parent node
     */
    public Parent getRootNode() {
        return rootPane;
    }

    /**
     * Get the view area size. This will be a number between 0 and 1 which defines the percentage space of this view
     * within the surrounding area.
     *
     * @return The view area size.
     */
    public double getViewAreaSize() {
        return this.viewAreaSize;
    }

    /**
     * Get the controller instance.
     *
     * @return The controller instance.
     */
    public C getController() {
        return controller;
    }

    /**
     * Returns the url for the image
     * @return the url for the image
     */
    public URL getViewImagePath() {
        return viewImagePath;
    }

    @Override
    public String toString() {
        return "FXMLView{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", defaultPosition=" + defaultPosition +
                ", viewAreaSize=" + viewAreaSize +
                ", controller=" + controller +
                '}';
    }

    /**
     * The possible positions to place a view within the a window.
     */
    public enum Position {

        /**
         * Place the view on the top side.
         */
        TOP,

        /**
         * Place the view on the left side.
         */
        LEFT,

        /**
         * Place the view within the center.
         */
        CENTER,

        /**
         * Place the window on the right side.
         */
        RIGHT,

        /**
         * Place the window on the bottom.
         */
        BOTTOM
    }

    /**
     * Builder for new FXML views.
     *
     * @param <C> The type of the controller.
     */
    public static class Builder<C> {
        private String id;
        private String title;
        private Position pos;
        private URL file;
        private String toolTipInfo;
        private double viewAreaSize;
        private ClassLoader classLoader;
        private URL viewImage;

        /**
         * Set the builder value "id"
         *
         * @param id the id
         * @return fluent builder interface
         */
        public Builder<C> withId(String id) {
            this.id = id;
            return this;
        }

        /**
         * Set the builder value "title"
         *
         * @param title title
         * @return fluent builder interface
         */
        public Builder<C> withTitle(String title) {
            this.title = title;
            return this;
        }

        /**
         * Set the builder value "pos"
         *
         * @param pos position
         * @return fluent builder interface
         */
        public Builder<C> withPos(Position pos) {
            this.pos = pos;
            return this;
        }

        /**
         * Set the builder value "file"
         *
         * @param file file
         * @return fluent builder interface
         */
        public Builder<C> withFile(URL file) {
            this.file = file;
            return this;
        }

        /**
         * Set the builder value "file"
         *
         * @param file file
         * @return fluent builder interface
         */
        public Builder<C> withFile(String file) {
            Objects.requireNonNull(classLoader);
            this.file = classLoader.getResource(file);
            return this;
        }

        /**
         * Set the builder value "toolTipInfo"
         *
         * @param toolTipInfo tooltip
         * @return fluent builder interface
         */
        public Builder<C> withToolTipInfo(String toolTipInfo) {
            this.toolTipInfo = toolTipInfo;
            return this;
        }

        /**
         * Set the builder value "viewAreaSize"
         *
         * @param viewAreaSize viewAreaSize
         * @return fluent builder interface
         */
        public Builder<C> withViewAreaSize(double viewAreaSize) {
            this.viewAreaSize = viewAreaSize;
            return this;
        }

        /**
         * Set the builder value "classLoader"
         *
         * @param classLoader classloader
         * @return fluent builder interface
         */
        public Builder<C> withClassLoader(ClassLoader classLoader) {
            this.classLoader = classLoader;
            return this;
        }

        /**
         * Set the builder value "viewImagePath"
         *
         * @param viewImage view image
         * @return fluent builder interface
         */
        public Builder<C> withViewImage(URL viewImage) {
            this.viewImage = viewImage;
            return this;
        }

        /**
         * Set the builder value "viewImagePath"
         *
         * @param viewImage view image
         * @return fluent builder interface
         */
        public Builder<C> withViewImage(String viewImage) {
            Objects.requireNonNull(classLoader);
            this.viewImage = classLoader.getResource(viewImage);
            return this;
        }

        /**
         * Build the real {@link EkgView}.
         *
         * @return The new initialized view.
         * @throws IOException In case of the fxml can not be read.
         */
        public EkgView<C> build() throws IOException {
            Objects.requireNonNull(id, "A view must have an unique id");
            Objects.requireNonNull(title, "A view must have a title");
            Objects.requireNonNull(pos, "The initial position must be set");
            Objects.requireNonNull(file, "Can not initialize a FXMLView without a FXML file.");

            FXMLLoader loader = EkgLookup.lookup(FXMLLoader.class);
            loader.setLocation(file);
            if (classLoader != null) {
                loader.setClassLoader(classLoader);
            }
            Parent rootPane = loader.load();
            C controller = loader.getController();

            return new EkgView<>(id, title, pos, rootPane, toolTipInfo, viewAreaSize, controller, viewImage);
        }
    }
}
