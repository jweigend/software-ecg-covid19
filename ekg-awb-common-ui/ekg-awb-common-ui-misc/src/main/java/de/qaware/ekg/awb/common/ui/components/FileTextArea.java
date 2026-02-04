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

import javafx.beans.InvalidationListener;
import javafx.beans.property.*;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * Read only text area for viewing a file.
 */
@SuppressWarnings("unused")
public class FileTextArea extends LazyTextArea {
    private final ObjectProperty<File> file = new SimpleObjectProperty<>(this, "file");
    private final ObjectProperty<Font> font = new SimpleObjectProperty<>(this, "font");
    private final ReadOnlyLongWrapper fileLineCount = new ReadOnlyLongWrapper(this, "fileLineCount");
    private final ReadOnlyStringWrapper filePreview = new ReadOnlyStringWrapper(this, "filePreview");

    private final StringProperty longestLine = new SimpleStringProperty(this, "longestLine");
    private final ReadOnlyDoubleWrapper lineHeight = new ReadOnlyDoubleWrapper(this, "lineHeight");
    private final ReadOnlyDoubleWrapper maxLineWidth = new ReadOnlyDoubleWrapper(this, "maxLineWidth");
    private final ReadOnlyDoubleWrapper contentHeight = new ReadOnlyDoubleWrapper(this, "contentHeight");

    private RandomAccessFile fileAccess;
    private List<Long> linePosition = new ArrayList<>();
    private File tmpFile;
    private Service<FileInspectorResult> inspectFileService = new Service<FileInspectorResult>() {
        @Override
        protected Task<FileInspectorResult> createTask() {
            return new ListTask();
        }
    };

    /**
     * Creates a new FileTextArea.
     */
    public FileTextArea() {
        super();
        setContent(new Content());
        init();
    }

    /**
     * Get the current file in preview.
     *
     * @return the file
     */
    public File getFile() {
        return file.get();
    }

    /**
     * Get the number of lines within the current file.
     *
     * @return the number of lines
     */
    public long getFileLineCount() {
        return fileLineCount.get();
    }

    /**
     * Get the text currently shown within the preview.
     *
     * @return the shown part of file.
     */
    public String getFilePreview() {
        return filePreview.get();
    }

    /**
     * Set the backing file.
     *
     * @param file shown file within the element.
     */
    public void setFile(File file) {
        this.file.set(file);
    }

    /**
     * Get the file property.
     *
     * @return the file property.
     */
    public ObjectProperty<File> fileProperty() {
        return file;
    }

    /**
     * Get the property for the file preview content.
     *
     * @return the property for the file preview content.
     */
    public ReadOnlyStringWrapper filePreviewProperty() {
        return filePreview;
    }

    /**
     * Get the font.
     *
     * @return the font
     */
    public Font getFont() {
        return font.get();
    }

    /**
     * Get the font property.
     *
     * @return the font property
     */
    public ObjectProperty<Font> fontProperty() {
        return font;
    }

    /**
     * set the font
     *
     * @param font the font.
     */
    public void setFont(Font font) {
        this.font.set(font);
    }

    private void init() {
        font.set(Font.font("Monospaced", 11));
        font.addListener(o -> contentSizeChanged());
        file.addListener(observable -> fileChanged());
        fileLineCount.addListener(o -> contentSizeChanged());
        inspectFileService.setOnSucceeded(e -> {
            FileInspectorResult results = inspectFileService.getValue();
            fileAccess = results.fileAccess;
            tmpFile = results.tmpFile;
            linePosition = results.positions;
            fileLineCount.setValue(results.fileLineCount);
            disableProperty().set(false);
            vvalueProperty().set(0);
        });
    }

    private void contentSizeChanged() {
        Text text = formatText(longestLine.get());
        lineHeight.set(text.getBoundsInLocal().getHeight());
        maxLineWidth.set(text.getBoundsInLocal().getWidth());
        contentHeight.set(lineHeight.get() * fileLineCount.get());
    }

    /**
     * Called when the file has changed.
     */
    private void fileChanged() {
        if (file.get() == null) {
            linePosition.clear();
            fileLineCount.setValue(0);
            setVvalue(0);
            return;
        }
        try {
            cleanUp();
            disableProperty().set(true);
            inspectFileService.restart();
        } catch (IOException e) {
            throw new IllegalArgumentException("Can not read file", e);
        }
    }

    private boolean cleanUp() throws IOException {
        if (fileAccess != null) {
            fileAccess.close();
        }
        return tmpFile != null && tmpFile.delete();
    }

    private Text formatText(String text) {
        Text txt = new Text(text);
        txt.setFont(font.getValue());
        return txt;
    }

    private class Content implements LazyTextArea.Content {

        @Override
        public List<Text> getLines(long start, int limit) {
            if (linePosition.isEmpty()) {
                return new ArrayList<>();
            }
            List<Text> texts = new ArrayList<>();
            try {
                fileAccess.seek(linePosition.get((int) start));

                StringBuilder preview = new StringBuilder();
                long lastLine = Math.min(fileLineCount.get() + 1, start + limit);
                for (long i = start; i < lastLine; i++) {
                    String text = fileAccess.readLine() + "\n";
                    preview.append(text);
                    texts.add(formatText(text));
                }
                filePreview.set(preview.toString());
            } catch (IOException e) {
                throw new IllegalStateException("Can not read from file", e);
            }
            return texts;
        }

        @Override
        public ReadOnlyDoubleProperty lineHeightProperty() {
            return lineHeight;
        }

        @Override
        public ReadOnlyDoubleProperty maxLineWidthProperty() {
            return maxLineWidth;
        }

        @Override
        public ReadOnlyDoubleProperty contentHeightProperty() {
            return contentHeight;
        }

        @Override
        public ReadOnlyLongProperty numberOfLinesProperty() {
            return fileLineCount;
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
            file.addListener(listener);
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
            file.removeListener(listener);
        }
    }

    /**
     * Task for detect line starts and count lines.
     */
    private class ListTask extends Task<FileInspectorResult> {

        public static final int READ_BUFFER_SIZE = 512 * 1024;
        private List<Long> positions;
        private long lastLine = 0;
        private long longestLineStart = 0;
        private long longestLineLength = 0;

        // randomAccessFile is not closed here
        @SuppressWarnings("java:S2095")
        @Override
        protected FileInspectorResult call() throws IOException {
            OutputStream os = null;
            InputStream is = new FileInputStream(file.get());
            try {
                positions = new ArrayList<>(100000);
                positions.add(0L);
                long fileSize = getFile().length();
                File tmp = null;
                RandomAccessFile randomAccessFile;

                if (file.get().toString().endsWith(".gz")) {
                    tmp = File.createTempFile("extracted_", ".log.tmp");
                    tmp.deleteOnExit();
                    randomAccessFile = new RandomAccessFile(tmp, "r");
                    is = new GZIPInputStream(is);
                    os = new BufferedOutputStream(new FileOutputStream(tmp));
                } else {
                    randomAccessFile = new RandomAccessFile(file.get(), "r");
                }

                try (BufferedInputStream stream = new BufferedInputStream(is, READ_BUFFER_SIZE)) {
                    boolean eol = false;
                    long filePointer = 1;
                    while (!eol && !isCancelled()) {
                        switch (readAndCopy(stream, os)) {
                            case -1:
                                eol = true;
                                break;
                            case '\r':
                                int read = readAndCopy(stream, os);
                                if (read == '\n') {
                                    addPosition(++filePointer);
                                } else if (read == '\r') {
                                    addPosition(filePointer);
                                    addPosition(filePointer++);
                                } else {
                                    addPosition(filePointer++);
                                }
                                updateProgress(filePointer++, fileSize);
                                break;
                            case '\n':
                                addPosition(filePointer++);
                                updateProgress(filePointer, fileSize);
                                break;
                            default:
                                filePointer++;
                                break;
                        }
                    }
                }
                randomAccessFile.seek(longestLineStart);
                longestLine.set(randomAccessFile.readLine());
                return new FileInspectorResult(positions, randomAccessFile, tmp);
            } finally {
                if (os != null) {
                    os.close();
                }

                is.close();
            }
        }

        private int readAndCopy(InputStream is, OutputStream os) throws IOException {
            int val = is.read();
            if (os != null) {
                os.write(val);
            }
            return val;
        }

        /**
         * Add position as new line and detect longest line
         *
         * @param pointer the current position.
         */
        private void addPosition(long pointer) {
            positions.add(pointer);
            long length = pointer - lastLine;
            if (length > longestLineLength) {
                longestLineStart = lastLine;
                longestLineLength = length;
            }
            lastLine = pointer;
        }
    }

    private static class FileInspectorResult {
        private final List<Long> positions;
        private final RandomAccessFile fileAccess;
        private final int fileLineCount;
        private final File tmpFile;

        /**
         * Create a new file inspection result. Used to transfer the results from the backend
         * task to the text area.
         *
         * @param positions  the line end positions.
         * @param fileAccess the {@link RandomAccessFile} object for the uncompressed file
         * @param tmpFile    the path to a possible temporary file (if file needs to be uncompressed)
         */
        public FileInspectorResult(List<Long> positions, RandomAccessFile fileAccess, File tmpFile) {
            this.positions = positions;
            this.fileAccess = fileAccess;
            this.fileLineCount = positions.size();
            this.tmpFile = tmpFile;
        }
    }
}
