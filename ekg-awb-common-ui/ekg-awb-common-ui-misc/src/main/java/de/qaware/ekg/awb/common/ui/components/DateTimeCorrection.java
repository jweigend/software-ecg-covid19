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
package de.qaware.ekg.awb.common.ui.components;

import de.qaware.ekg.awb.common.ui.bindings.Bindings;
import de.qaware.ekg.awb.commons.beans.BeanProvider;
import de.qaware.ekg.awb.sdk.core.lookup.EkgLookup;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.TimeZone;

/**
 * Control to set Date and Time Correction
 */
public class DateTimeCorrection extends GridPane implements Initializable {
    private final DoubleProperty labelWidth = new SimpleDoubleProperty(this, "labelWidth");
    private final IntegerProperty yearCorrectionProperty = new SimpleIntegerProperty(this, "yearCorrection");
    private final BooleanProperty timeZoneVisible = new SimpleBooleanProperty(this, "timeZoneVisible", true);
    private final BooleanProperty yearCorrectionVisible = new SimpleBooleanProperty(this, "yearCorrectionVisible", true);
    private final ObjectProperty<TimeZone> selectedTimeZone = new SimpleObjectProperty<>(this, "selectedTimeZone");

    @FXML
    private Spinner<Integer> yearCorrection;
    @FXML
    private ComboBox<String> cbxTimeZone;

    /**
     * Initialize a new Date Time Correction configuration pane
     */
    public DateTimeCorrection() {
        try {
            BeanProvider.injectFields(this);
            FXMLLoader fxmlLoader = EkgLookup.lookup(FXMLLoader.class);
            fxmlLoader.setLocation(DateTimeCorrection.class.getResource("DateTimeCorrection.fxml"));
            fxmlLoader.setRoot(this);
            fxmlLoader.setController(this);
            fxmlLoader.load();
        } catch (IOException e) {
            throw new IllegalStateException("Could not load fxml file for DateTimeCorrection", e);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        yearCorrectionProperty.bind(yearCorrection.valueProperty());
        //The visibility of the spinner for the year correction can't be binded in the fxml
        yearCorrection.visibleProperty().bindBidirectional(yearCorrectionVisible);
        cbxTimeZone.getItems().addAll(TimeZone.getAvailableIDs());
        Bindings.bindBidirectional(cbxTimeZone.valueProperty(), selectedTimeZone, TimeZone::getTimeZone, TimeZone::getID);
        cbxTimeZone.getSelectionModel().select("UTC");
    }

    /**
     * Get the factory for the year correction spinner. The factory is able to set the value of the spinner.
     *
     * @return the factory for the year correction spinner.
     */
    public SpinnerValueFactory<Integer> getYearCorrectionFactory() {
        return yearCorrection.getValueFactory();
    }

    /**
     * Get the year for the correction.
     *
     * @return the year for the correction.
     */
    public int getYearCorrection() {
        return yearCorrectionProperty.get();
    }

    /**
     * Set the year for the correction.
     *
     * @param yearCorrection the year for the correction.
     */
    public void setYearCorrection(int yearCorrection) {
        this.yearCorrection.getValueFactory().setValue(yearCorrection);
    }

    /**
     * Get the year correction property as read-only.
     *
     * @return the year correction property as read-only.
     */
    public ReadOnlyIntegerProperty yearCorrectionProperty() {
        return yearCorrectionProperty;
    }

    /**
     * Get the selected time zone.
     *
     * @return the selected time zone.
     */
    public TimeZone getSelectedTimeZone() {
        return selectedTimeZone.get();
    }

    /**
     * Set the selected time zone.
     *
     * @param selectedTimeZone the selected time zone.
     */
    public void setSelectedTimeZone(TimeZone selectedTimeZone) {
        this.selectedTimeZone.set(selectedTimeZone);
    }

    /**
     * Get the selected time zone property.
     *
     * @return the selected time zone property.
     */
    public ObjectProperty<TimeZone> selectedTimeZoneProperty() {
        return selectedTimeZone;
    }

    /**
     * Get the width of the labels.
     *
     * @return the label width.
     */
    public double getLabelWidth() {
        return labelWidth.get();
    }

    /**
     * Set the width of the labels.
     *
     * @param labelWidth the width of the label.
     */
    public void setLabelWidth(double labelWidth) {
        this.labelWidth.set(labelWidth);
    }

    /**
     * Get the the width property of the labels.
     *
     * @return the width property of the label.
     */
    public DoubleProperty labelWidthProperty() {
        return labelWidth;
    }

    /**
     * Get time zone field visibility.
     *
     * @return time zone field visibility.
     */
    public boolean isTimeZoneVisible() {
        return timeZoneVisible.get();
    }

    /**
     * Set time zone field visibility.
     *
     * @param timeZoneVisible time zone field visibility.
     */
    public void setTimeZoneVisible(boolean timeZoneVisible) {
        this.timeZoneVisible.set(timeZoneVisible);
    }

    /**
     * Get time zone field visibility property.
     *
     * @return time zone field visibility property.
     */
    public BooleanProperty timeZoneVisibleProperty() {
        return timeZoneVisible;
    }

    /**
     * Get year correction field visibility.
     *
     * @return year correction field visibility.
     */
    public boolean isYearCorrectionVisible() {
        return yearCorrectionVisible.get();
    }

    /**
     * Set year correction field visibility.
     *
     * @param yearCorrectionVisible year correction field visibility.
     */
    public void setYearCorrectionVisible(boolean yearCorrectionVisible) {
        this.yearCorrectionVisible.set(yearCorrectionVisible);
    }

    /**
     * Get year correction visibility property.
     *
     * @return year correction visibility property.
     */
    public BooleanProperty yearCorrectionVisibleProperty() {
        return yearCorrectionVisible;
    }
}
