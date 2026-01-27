package de.qaware.ekg.awb.common.ui.components;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;


/**
 * Generic ComboBox component that provides build-in support for auto-completion and
 * filtering on the item list.
 * In addition to that the box provides some convenience functionality like automatically
 * show item list on arrow down key action.
 *
 * The implementation of this component needs a valid StringConverter that is able
 * to transform every item type to the correct string representation used for filtering
 * and completion.
 */
@SuppressWarnings({"unused", "unchecked", "rawtypes"}) // the class used via FXML / reflection
public class FilterableComboBox<T> extends ComboBox<T> {

    private ObjectProperty<ObservableList<T>> noneFilteredItems =
            new SimpleObjectProperty<>(this, "noneFilteredItems", FXCollections.observableArrayList());

    private List<EventHandler<? super Event>> selectionCompletedHandler = new ArrayList<>();

    private List<Consumer<String>> typedTextConsumer = new ArrayList<>();

    private boolean isHighlighted = false;

    private boolean ignoreItemsChanged = false;

    private boolean customTextInUse = false;

    private int lastAmountOfComboBoxItems = 0;

    private String lastEditorText = "";

    private int lastSelectedIndex = -1;

    private int lastCaretPosBeforeUpdate = -1;

    private T lastSelectedItem = null;



    //================================================================================================================
    // constructors
    //================================================================================================================

    /**
     * Creates a default ComboBox instance with an empty
     * {@link #itemsProperty() items} list and default
     * {@link #selectionModelProperty() selection model}.
     */
    public FilterableComboBox() {
        super();
        initStyle();
        initHandler();
    }

    /**
     * Creates a default ComboBox instance with the provided items list and
     * a default {@link #selectionModelProperty() selection model}.
     * @param items the list of items
     */
    public FilterableComboBox(ObservableList<T> items) {
        super(items);
        initStyle();
        initHandler();
    }

    //================================================================================================================
    // overwritten and extended public API of this control
    //================================================================================================================

    public void setOnSelectionCompleted (EventHandler<? super Event> handler) {
        selectionCompletedHandler.add(handler);
    }

    public ObjectProperty<ObservableList<T>> noneFilteredItemsProperty() {
        return noneFilteredItems;
    }

    public void reset() {

    }

    //================================================================================================================
    //  initializing logic
    //================================================================================================================

    private void initStyle() {
        setEditable(true);
        setSkin(new ComboBoxListViewSkin<>(this));
    }

    /**
     * Initialize the behavior of the ComboBox by binding
     * event handlers for auto completion and filtering support
     */
    private void initHandler()  {

        initItemsChangedHandler();

        initSelectionCompletedBehavior();

        initComboboxKeyEventHandler();

        disableJfxDefaultSpaceKeyEventHandler();

        initFocusHandler();

        initStringConverterChangedHandler();
    }


    //================================================================================================================
    //  event handler logic
    //================================================================================================================

    private void setupCaret() {

        if (!isFocused()) {
            return;
        }

        TextField editor = getEditor();
        int lastPos = editor.getText() == null ? 0 : editor.getText().length();
        editor.positionCaret(lastPos);
        lastCaretPosBeforeUpdate = editor.getCaretPosition();
    }

    private void initFocusHandler() {
        TextField editor = getEditor();
        editor.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                setupCaret();
            }
        });
    }

    private void initComboboxKeyEventHandler() {

        setOnKeyReleased(e -> {

            TextField textField = getEditor();
            String fullText = textField.getText();
            int caretPosition = textField.getCaretPosition();
            lastCaretPosBeforeUpdate = caretPosition;

            // ensure that the item list will shown than the user pressed ARROW_DOWN key
            if (handleArrowDown(e)) {
                isHighlighted = false;
            }

            if ((e.isShiftDown() || e.isControlDown()) && (e.getCode() == KeyCode.DOWN || e.getCode() == KeyCode.UP)) {
                isHighlighted = false;

                if (getSelectionModel().getSelectedIndex() != lastSelectedIndex) {
                    notifySelectionCompleteListener(new Object[]{fullText}, e);
                }

                lastSelectedIndex = getSelectionModel().getSelectedIndex();
                return;
            }

            if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.ESCAPE) {
                isHighlighted = false;
                hide();
                lastSelectedIndex = getSelectionModel().getSelectedIndex();

                boolean isCustom = customTextInUse;
                notifySelectionCompleteListener(new Object[]{fullText}, e);
                customTextInUse = isCustom;
                return;
            }

            if (isShowing() && (handleCaretMove(e) || handleSelectAll(e))) {
                isHighlighted = false;
                return;
            }

            if (isTextEditingKeyType(e)) {

                customTextInUse = true;

                lastSelectedIndex = getSelectionModel().getSelectedIndex();

                String typedText = getTypedText(textField, caretPosition);
                final StringConverter<T> converter = getNoneNullConverter();

                if ((e.getCode() == KeyCode.BACK_SPACE || e.getCode() == KeyCode.DELETE) &&
                        (isHighlighted || lastEditorText.isBlank())) {
                    isHighlighted = false;
                    lastEditorText = fullText;
                    return;
                } else {
                    isHighlighted = false;
                    lastEditorText = fullText;
                }

                updateWithFilteredItems(typedText, converter);

                // do not do any autocompletion or fulltext search action than the editor is empty
                if (lastEditorText.isBlank()) {
                    return;
                }

                boolean hasMatch = false;

                // first loop - try to autocomplete with an item that BEGINS with
                // the typed character sequence
                for (T item : super.getItems()) {
                    String itemText = converter.toString(item);
                    if (!itemText.toLowerCase().startsWith(typedText.toLowerCase())) {
                        continue;
                    }

                    getSelectionModel().select(item);
                    textField.setText(itemText);
                    textField.selectEnd();
                    textField.selectRange(textField.getCaretPosition(), caretPosition);

                    isHighlighted = true;

                    if (!isShowing()) {
                        show();
                    }

                    hasMatch = true;
                    break;
                }

                if (!hasMatch) {

                    // if the filtered item list has more than the (default) '*' value than show the list
                    if ((getItems().size() > 1 || (getItems().size() == 1
                            && !"*".equals(converter.toString(getItems().get(0))))) && !isShowing()) {
                        show();
                    }

                    textField.positionCaret(caretPosition);
                }
            }
        });
    }

    private void initStringConverterChangedHandler() {
        ObjectProperty<StringConverter<T>> converterProperty = converterProperty();
        converterProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue instanceof FilterableComboBoxStringConverter) {
                FilterableComboBoxStringConverter<T> converter = (FilterableComboBoxStringConverter) newValue;
                converter.setFilteredListProperty(this.noneFilteredItemsProperty());
            }
        });
    }

    private String getTypedText(TextField textField, int caretPosition) {

        if (isHighlighted) {
            textField.getText(0, caretPosition);
        }

        return  textField.getText();
    }

    private boolean handleArrowDown(KeyEvent e) {

        if (e.getCode() != KeyCode.DOWN) {
            return false;
        }

        if (!isShowing()) {
            show();
        }

        return true;
    }

    private boolean handleSelectAll(KeyEvent e) {

        if (e.isControlDown() && e.getCode() == KeyCode.A) {
            getEditor().selectAll();
            return true;
        }

        return false;
    }

    private boolean handleCaretMove(KeyEvent e) {

        TextField textField = getEditor();
        String fullText = textField.getText();
        int caretPosition = textField.getCaretPosition();

        if (e.getCode() == KeyCode.RIGHT) {
            if (fullText.length() > caretPosition) {
                textField.positionCaret(caretPosition + 1);
            }

            return true;
        }

        if (e.getCode() == KeyCode.LEFT) {
            if (caretPosition > 0) {
                textField.positionCaret(caretPosition - 1);
            }

            return true;
        }
        return false;
    }

    private void disableJfxDefaultSpaceKeyEventHandler() {
        getDropDownList().addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.SPACE) {
                e.consume();
            }
        });
    }


    //================================================================================================================
    //  various internal logic
    //================================================================================================================

    private ListView getDropDownList() {
        return (ListView)((ComboBoxListViewSkin)getSkin()).getPopupContent();
    }

    private void updateWithFilteredItems(String typedText, StringConverter<T> converter) {

        String lowerCaseTypedText = typedText.toLowerCase();

        ignoreItemsChanged = true;

        Pattern searchPattern;
        if (typedText.contains("*") || typedText.contains("?") || typedText.contains("|")) {
            searchPattern = Pattern.compile(typedText.replaceAll("\\*", ".*"), Pattern.CASE_INSENSITIVE);
        } else {
            searchPattern = null;
        }

        FilteredList<T> filteredList = (noneFilteredItems.get().filtered(item -> {

            if (item == null) {
                return false;
            }

            String valueText = converter.toString(item).toLowerCase();

            if ("*".equals(valueText)) {
                return true;
            }

            if (searchPattern != null) {
                return searchPattern.matcher(valueText).matches();
            }

            return valueText.contains(lowerCaseTypedText);
        }));

        getSelectionModel().clearSelection();

        if (filteredList.isEmpty()) {
            super.getItems().clear();
        } else {
            super.getItems().setAll(new ArrayList<>(filteredList));
        }

        getEditor().setText(typedText);

        ListView listView = (ListView)((ComboBoxListViewSkin)getSkin()).getPopupContent();
        listView.getFocusModel().focus(-1);

        if (isShowing() && lastAmountOfComboBoxItems != filteredList.size() && (filteredList.size() < getVisibleRowCount()
                || lastAmountOfComboBoxItems < getVisibleRowCount())) {
            listView.autosize();
        }

        lastAmountOfComboBoxItems = filteredList.size();

        ignoreItemsChanged = false;
    }

    private boolean isTextEditingKeyType(KeyEvent e) {

        if (e.isControlDown() ||e.isAltDown() ||e.isMetaDown()) {
            return false;
        }

        return e.getCode() == KeyCode.BACK_SPACE || e.getCode() == KeyCode.DELETE || e.getText().matches("\\p{Print}");
    }

    private void initItemsChangedHandler() {

        //noinspection unchecked
        super.getItems().addListener((ListChangeListener) c -> {
            if (ignoreItemsChanged) {
                return;
            }

            lastSelectedIndex = -1;

            //noinspection unchecked
            noneFilteredItems.set(FXCollections.observableArrayList(c.getList()));

            if (lastSelectedItem != null) {

                if (noneFilteredItems.get().contains(lastSelectedItem)) {
                    getSelectionModel().select(lastSelectedItem);
                    lastSelectedIndex = getSelectionModel().getSelectedIndex();

                } else if (customTextInUse) {
                    updateWithFilteredItems(getEditor().getText(), getNoneNullConverter());

                } else {
                    resetBox();
                }

            } else {
                resetBox();
            }

            if (lastCaretPosBeforeUpdate != 0) {
                setupCaret();
            }
        });
    }

    public void resetBox() {
        getSelectionModel().clearAndSelect(0);
        lastSelectedIndex = getSelectionModel().getSelectedIndex();
        lastSelectedItem = getSelectionModel().getSelectedItem();

        String itemText = getNoneNullConverter().toString(lastSelectedItem);
        getEditor().setText(itemText);
    }

    private void initSelectionCompletedBehavior() {
        // we need a little bit more sophisticated algorithm to update the graph, otherwise it will
        // update after the new value loaded (triggered at onShowing) that is to early.
        final Object[] selectedVal = new Object[1];

        setOnShowing(event -> selectedVal[0] = getSelectionModel().getSelectedItem());

        // fire than item list get hidden and the item selection has changed
        setOnHidden(event -> {
            if (selectedVal[0] != getSelectionModel().getSelectedItem()) {
                lastSelectedItem = getSelectionModel().getSelectedItem();
                selectionCompletedHandler.forEach(eventHandler -> eventHandler.handle(event));
            }
        });

        setOnKeyReleased(event -> {

            // fire every time the user pushed ENTER key
            if (event.getCode() == KeyCode.ENTER) {
                selectionCompletedHandler.forEach(eventHandler -> eventHandler.handle(event));
                return;
            }

            // fire than the user uses the up/down arrow keys and the value has really changed
            if (isShowing() && (event.getCode() == KeyCode.DOWN || event.getCode() == KeyCode.UP)) {
                notifySelectionCompleteListener(selectedVal, event);
            }
        });

        valueProperty().addListener((observable, oldValue, newValue) -> {

            if (newValue == null) {
                return;
            }

            lastSelectedItem = getSelectionModel().getSelectedItem();
        });
    }

    private void notifySelectionCompleteListener(Object[] selectedVal, KeyEvent event) {

        customTextInUse = false;

        if (selectedVal[0] != getSelectionModel().getSelectedItem()) {
            selectionCompletedHandler.forEach(eventHandler -> eventHandler.handle(event));
            selectedVal[0] = getSelectionModel().getSelectedItem();
        }
    }

    private StringConverter<T> getNoneNullConverter() {

        StringConverter<T> converter = getConverter();

        if (converter == null) {
            converter = new StringConverter<>() {

                @Override
                public String toString(T object) {
                    return object == null ? "" : object.toString();
                }

                @Override
                public T fromString(String string) {
                    return null;
                }
            };
        }

        return converter;
    }
}
