package de.qaware.ekg.awb.project.ui.projectbar;

import de.qaware.ekg.awb.sdk.core.NamedEnum;
import javafx.beans.property.BooleanProperty;
import javafx.scene.Node;
import javafx.scene.control.Cell;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.Skin;
import javafx.scene.control.skin.CellSkinBase;
import javafx.scene.control.skin.ComboBoxListViewSkin;

import java.util.Map;

/**
 * Skin class that styles ComboBox instances that
 * show the project view flavor options in the ProjectBar.
 */
public class ViewFlavorComboBoxSkin<T extends NamedEnum> extends ComboBoxListViewSkin<T> {

    private  Map<T, BooleanProperty> disablePropertyMap;

    /**
     * Construct a new ViewFlavorComboBoxSkin instance that will use the
     * given properties for styling the combobox.
     *
     * @param comboBox the combobox to skins
     * @param disablePropertyMap a map with a combobox value an an according disable property
     */
    public ViewFlavorComboBoxSkin(ComboBox<T> comboBox, Map<T, BooleanProperty> disablePropertyMap) {
        super(comboBox);

        this.disablePropertyMap = disablePropertyMap;
        comboBox.getStyleClass().addAll("viewflavor-selector-combobox");
        comboBox.setCellFactory(value -> new EkgListCell<>(null, disablePropertyMap));
    }

    /**
     * This method should return a Node that will be positioned within the
     * ComboBox 'button' area.
     * @return the node that will be positioned within the ComboBox 'button' area
     */
    @Override
    public Node getDisplayNode() {
        Node node = super.getDisplayNode();

        node.getStyleClass().add("comboBoxIconButtonCell");

        // we do nothing if no selection available
        if (getSkinnable().getValue() == null) {
            return node;
        }

        //noinspection unchecked
        return new EkgButtonCell(getSkinnable().getValue(), disablePropertyMap);
    }


    //================================================================================================================
    //  inner classes
    //================================================================================================================


    private static class EkgButtonCell<T extends NamedEnum> extends Cell<T> {

        private Map<T, BooleanProperty> disablePropertyMap;

        public EkgButtonCell(T initialValue, Map<T, BooleanProperty> disablePropertyMap) {
            super.getStyleClass().add("ekgComboboxButtonCell");
            this.disablePropertyMap = disablePropertyMap;
            this.updateItem(initialValue, false);
        }

        @Override
        protected void updateItem(T selectedValue, boolean empty) {
            super.updateItem(selectedValue, empty);

            if (selectedValue != null) {
                setText(" " + selectedValue.getName());
                disableProperty().bind(disablePropertyMap.get(selectedValue));
            } else {
                disableProperty().unbind();
            }
        }

        /** {@inheritDoc} */
        @Override protected Skin<?> createDefaultSkin() {
            return new CellSkinBase<>(this);
        }
    }


    private static class EkgListCell<T extends NamedEnum> extends ListCell<T> {

        private  Map<T, BooleanProperty> disablePropertyMap;

        public EkgListCell(T initialValue, Map<T, BooleanProperty> disablePropertyMap) {
            super.getStyleClass().add("projectFlavorComboboxListCell");
            this.disablePropertyMap = disablePropertyMap;
            updateItem(initialValue, false);
        }

        @Override
        protected void updateItem(T selectedValue, boolean empty) {
            super.updateItem(selectedValue, empty);

            if (selectedValue != null) {
                setText(" " + selectedValue.getName());
                disableProperty().bind(disablePropertyMap.get(selectedValue));
            } else {
                disableProperty().unbind();
            }
        }
    }
}
