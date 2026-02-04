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
package de.qaware.ekg.awb.common.ui.components;


import de.qaware.ekg.awb.sdk.core.lookup.EkgLookup;
import javafx.beans.InvalidationListener;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A read only text area that only load the currently shown lines.
 */
public class LazyTextArea extends Pane implements Initializable {
    private static final Logger LOGGER = LoggerFactory.getLogger(LazyTextArea.class);
    private final ObjectProperty<Content> content = new SimpleObjectProperty<>(this, "content");
    private final LongProperty previewStartLine = new SimpleLongProperty(this, "previewStartLine");
    private final DoubleProperty scrollPosition = new SimpleDoubleProperty(this, "scrollPosition");
    private final IntegerProperty visibleLines = new SimpleIntegerProperty(this, "visibleLines");
    @FXML
    private ScrollPane areaScroll;
    @FXML
    private AnchorPane areaContentHolder;
    @FXML
    private TextFlow areaContent;

    /**
     * Initialize the lazy textarea without content.
     */
    public LazyTextArea() {
        try {
            this.content.addListener(observable -> bindContentSpecificProperties());
            FXMLLoader fxmlLoader = EkgLookup.lookup(FXMLLoader.class);
            fxmlLoader.setLocation(LazyTextArea.class.getResource("LazyTextArea.fxml"));
            fxmlLoader.setRoot(this);
            fxmlLoader.setController(this);
            fxmlLoader.load();
        } catch (IOException e) {
            throw new IllegalStateException("Can not load fxml file for LazyTextArea", e);
        }
    }

    /**
     * Initialize the lazy text area with a default content
     *
     * @param content the default content
     */
    public LazyTextArea(Content content) {
        this();
        this.content.set(content);
    }

    private static <T> void rebind(Property<T> property, ObservableValue<? extends T> toProperty) {
        property.unbind();
        property.bind(toProperty);
    }

    /**
     * Get the content.
     *
     * @return the content
     */
    public Content getContent() {
        return content.get();
    }

    /**
     * set the content
     *
     * @param content the content
     */
    public void setContent(Content content) {
        this.content.set(content);
    }

    /**
     * Get the content property.
     *
     * @return the content property.
     */
    public ObjectProperty<Content> contentProperty() {
        return content;
    }

    /**
     * set the vvalue
     *
     * @param value value
     */
    public void setVvalue(double value) {
        areaScroll.setVvalue(value);
    }

    /**
     * get the vvalue
     *
     * @return the vvalue
     */
    public double getVvalue() {
        return areaScroll.getVvalue();
    }

    /**
     * the vvalue property
     *
     * @return the vvalue property
     */
    public DoubleProperty vvalueProperty() {
        return areaScroll.vvalueProperty();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        areaScroll.prefHeightProperty().bind(heightProperty());
        areaScroll.minHeightProperty().bind(heightProperty());
        areaScroll.maxHeightProperty().bind(heightProperty());
        areaScroll.prefWidthProperty().bind(widthProperty());
        areaScroll.minWidthProperty().bind(widthProperty());
        areaScroll.maxWidthProperty().bind(widthProperty());

        areaScroll.heightProperty().addListener(observable -> calcVisibleLines());
        areaScroll.vvalueProperty().addListener((o, old, newv) -> scrolling());
        scrollPosition.addListener((observable1, oldValue, newValue) -> {
            AnchorPane.setTopAnchor(areaContent, newValue.doubleValue());
        });
        previewStartLine.addListener(observable -> addTextContent());
        visibleLines.addListener(observable -> addTextContent());
    }

    private void bindContentSpecificProperties() {
        rebind(areaContentHolder.minHeightProperty(), content.get().contentHeightProperty());
        rebind(areaContentHolder.prefHeightProperty(), content.get().contentHeightProperty());
        rebind(areaContentHolder.maxHeightProperty(), content.get().contentHeightProperty());

        rebind(areaContentHolder.minWidthProperty(), content.get().maxLineWidthProperty());
        rebind(areaContentHolder.prefWidthProperty(), content.get().maxLineWidthProperty());
        rebind(areaContentHolder.maxWidthProperty(), content.get().maxLineWidthProperty());

        rebind(areaContent.minWidthProperty(), content.get().maxLineWidthProperty());
        rebind(areaContent.prefWidthProperty(), content.get().maxLineWidthProperty());
        rebind(areaContent.maxWidthProperty(), content.get().maxLineWidthProperty());
        content.get().lineHeightProperty().addListener(observable -> calcVisibleLines());
        content.get().numberOfLinesProperty().addListener(observable -> calcVisibleLines());
        content.get().maxLineWidthProperty().addListener(observable -> calcVisibleLines());
        content.get().addListener(observable -> addTextContent());
    }

    private void calcVisibleLines() {
        double lines = 0;
        if (content.get() != null && content.get().getLineHeight() != 0) {
            lines = areaScroll.getViewportBounds().getHeight() / content.get().getLineHeight();
        }
        visibleLines.setValue(lines);
    }

    @FXML
    private void scrolling() {
        long startLine = (long) (content.get().getNumberOfLines() * areaScroll.getVvalue()
                - visibleLines.get() * areaScroll.getVvalue());

        double startPosition = content.get().getContentHeight() * areaScroll.getVvalue()
                - areaScroll.getViewportBounds().getHeight() * areaScroll.getVvalue();

        if (startPosition >= 0 && startLine <= content.get().getNumberOfLines() - visibleLines.get()) {
            scrollPosition.setValue(startPosition);
            previewStartLine.setValue(startLine);
        }
    }

    private void addTextContent() {
        areaContent.getChildren().clear();
        areaContent.getChildren().addAll(content.get().getLines(previewStartLine.get(), visibleLines.get()));
        LOGGER.trace("Visible lines {} to {}; count={}", previewStartLine, previewStartLine.get() + visibleLines.get(),
                areaContent.getChildren().size());
    }

    /**
     * The Content interface defines how the textarea should load the content.
     * <p>
     * See {@link LazyTextArea.ListStringContent} for an example implementation.
     */
    public interface Content extends javafx.beans.Observable {
        /**
         * Get the current line hight.
         *
         * @return the current line hight
         */
        default double getLineHeight() {
            return lineHeightProperty().get();
        }

        /**
         * Get the width of the longest line, depending on the formatting of text.
         *
         * @return teh maximum line width.
         */
        default double getMaxLineWidth() {
            return maxLineWidthProperty().get();
        }

        /**
         * Get the current height of all possible lines to show. (line height * number of lines)
         *
         * @return the current content height.
         */
        default double getContentHeight() {
            return contentHeightProperty().get();
        }

        /**
         * Get the current number of all lines.
         *
         * @return the number of lines.
         */
        default long getNumberOfLines() {
            return numberOfLinesProperty().get();
        }

        /**
         * Get the <code>limit</code> lines from <code>start</code> as text.
         *
         * @param start the number of the first line
         * @param limit the maximum returned elements
         * @return a list with the texts
         */
        List<Text> getLines(long start, int limit);

        /**
         * Get the line height property
         *
         * @return the line height property
         */
        ReadOnlyDoubleProperty lineHeightProperty();

        /**
         * the max line width property.
         *
         * @return the line width property
         */
        ReadOnlyDoubleProperty maxLineWidthProperty();

        /**
         * the content height property.
         *
         * @return the content height property.
         */
        ReadOnlyDoubleProperty contentHeightProperty();

        /**
         * The number of lines.
         *
         * @return the number of lines.
         */
        ReadOnlyLongProperty numberOfLinesProperty();
    }

    /**
     * A simple implementation of {@link LazyTextArea.Content} that holds a list with
     * the lines to show.
     */
    public static class ListStringContent implements Content {

        private final ObjectProperty<Font> font = new SimpleObjectProperty<>();
        private final ObservableList<String> texts = new SimpleListProperty<>(FXCollections.observableArrayList());
        private final ReadOnlyDoubleWrapper lineHeight = new ReadOnlyDoubleWrapper();
        private final ReadOnlyDoubleWrapper maxLineWidth = new ReadOnlyDoubleWrapper();
        private final ReadOnlyDoubleWrapper contentHeight = new ReadOnlyDoubleWrapper();
        private final ReadOnlyLongWrapper numberOfLines = new ReadOnlyLongWrapper();

        /**
         * initialize the content
         */
        public ListStringContent() {
            font.addListener(observable -> {
                calcLineHeight();
                calcMaxLineWidth();
                calcContentHeight();
            });
            texts.addListener((InvalidationListener) c -> {
                calcNumberOfLines();
                calcMaxLineWidth();
                calcContentHeight();
            });
            this.font.set(Font.font("Monospaced", 11.0));
        }

        /**
         * Get the available strings to show
         *
         * @return the texts
         */
        public ObservableList<String> getTexts() {
            return texts;
        }

        /**
         * Get the font
         *
         * @return the font
         */
        public Font getFont() {
            return font.get();
        }

        /**
         * set the new font
         *
         * @param font the font
         */
        public void setFont(Font font) {
            this.font.set(font);
        }

        /**
         * get the font property.
         *
         * @return teh font property.
         */
        public ObjectProperty<Font> fontProperty() {
            return font;
        }

        private void calcLineHeight() {
            Text yY = formatText("yY");
            lineHeight.set(yY.getBoundsInLocal().getHeight());
        }

        private void calcContentHeight() {
            contentHeight.set(lineHeight.get() * numberOfLines.get());
        }

        private void calcNumberOfLines() {
            numberOfLines.set(texts.parallelStream().flatMap(s -> Arrays.stream(s.split("\r\n|\n|\r"))).count());
        }

        private void calcMaxLineWidth() {
            Optional<Text> maxText = texts.parallelStream().map(this::formatText)
                    .max(Comparator.comparingDouble(text -> text.getBoundsInLocal().getWidth()));
            if (maxText.isPresent()) {
                maxLineWidth.set(maxText.get().getBoundsInLocal().getWidth());
            } else {
                maxLineWidth.set(0);
            }
        }

        private Text formatText(String text) {
            Text txt = new Text(text);
            txt.setFont(font.getValue());
            return txt;
        }

        @Override
        public List<Text> getLines(long start, int limit) {
            return texts.parallelStream()
                    .flatMap(s -> Arrays.stream(s.split("\r\n|\n|\r")))
                    .skip(start)
                    .filter(Objects::nonNull)
                    .limit(limit)
                    .map(s -> s + "\n")
                    .map(this::formatText)
                    .collect(Collectors.toList());
        }

        @Override
        public ReadOnlyDoubleProperty lineHeightProperty() {
            return lineHeight.getReadOnlyProperty();
        }

        @Override
        public ReadOnlyDoubleProperty maxLineWidthProperty() {
            return maxLineWidth.getReadOnlyProperty();
        }

        @Override
        public ReadOnlyDoubleProperty contentHeightProperty() {
            return contentHeight.getReadOnlyProperty();
        }

        @Override
        public ReadOnlyLongProperty numberOfLinesProperty() {
            return numberOfLines.getReadOnlyProperty();
        }

        /**
         * Adds an {@link InvalidationListener} which will be notified whenever the
         * {@code Observable} becomes invalid.
         *
         * @param listener The listener to register
         * @throws NullPointerException if the listener is null
         * @see #removeListener(InvalidationListener)
         */
        @Override
        public void addListener(InvalidationListener listener) {
            texts.addListener(listener);
        }

        /**
         * Removes the given listener from the list of listeners, that are notified
         * whenever the value of the {@code Observable} becomes invalid.
         *
         * @param listener The listener to remove
         * @throws NullPointerException if the listener is null
         * @see #addListener(InvalidationListener)
         */
        @Override
        public void removeListener(InvalidationListener listener) {
            texts.removeListener(listener);
        }
    }
}
