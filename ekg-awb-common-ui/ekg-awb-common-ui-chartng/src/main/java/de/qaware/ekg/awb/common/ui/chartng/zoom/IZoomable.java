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
package de.qaware.ekg.awb.common.ui.chartng.zoom;

import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.chart.XYChart;
import javafx.scene.input.MouseEvent;

/**
 * Interface for a zoomable Chart
 *
 * @param <X> X axis type
 * @param <Y> Y axis type
 */
public interface IZoomable<X extends Long, Y> {

    /**
     * Sets the preferred width of the decorated chart
     * @param size new preferred width
     */
    void setPrefWidth(double size);

    /**
     * Sets the preferred height of the decorated chart
     * @param size new preferred height
     */
    void setPrefHeight(double size);

    /**
     * Sets the maximal width of the decorated chart
     * @param size new maximal width
     */
    void setMaxWidth(double size);

    /**
     * Sets the maximal height of the decorated chart
     * @param size new maximal height
     */
    void setMaxHeight(double size);

    /**
     * Returns the Series of the decorated chart
     * @return the Series of the decorated chart
     */
    ObservableList<XYChart.Series<Long, Y>> getData();

    /**
     * Sets the Series of the decorated chart
     * @param data the Series of the decorated chart
     */
    void setData(ObservableList<XYChart.Series<Long, Y>> data);

    /**
     * Sets the title for the chart
     * @param title new title for the chart
     */
    void setTitle(String title);

    /**
     * Returns the initial bounds of the chart
     * @return the initial bounds of the chart
     */
    double[] getStartingBounds();

    /**
     * Sets the initial bounds of the chart
     * Normally used inside the zoomable
     * @param bounds the initial bounds of the chart
     */
    void setStartingBounds(double[] bounds);

    /**
     * Sets a handler which is informed when mouse moves in
     * @param enteredHandler handler
     */
    void setOnMoveEntered(EventHandler<MouseEvent> enteredHandler);

    /**
     * Sets a handler which is informed when mouse moves out
     * @param exitHandler handler
     */
    void setOnMoveExited(EventHandler<MouseEvent> exitHandler);

    /**
     * sets a handler which is informed when the zoom has changed
     * @param zoomChangedHandler handler
     */
    void setOnZoomChanged(EventHandler<ZoomEvent> zoomChangedHandler);

    /**
     * ZoomEvent with coordinates of the new bounds
     */
    class ZoomEvent extends Event {

        private double xLowerBound;
        private double xUpperBound;
        private double yLowerBound;
        private double yUpperBound;
        private boolean isZoomIn;

        /**
         * Constructs an empty ZoomEvent without coordinates
         */
        public ZoomEvent() {
            super(EventType.ROOT);
        }

        /**
         * Constructs a ZoomEvent with coordinates and the type of the zoom
         *
         * @param xLowerBound new x lower bound of zoom
         * @param xUpperBound new x upper bound of zoom
         * @param yLowerBound new y lower bound of zoom
         * @param yUpperBound new y upper bound of zoom
         * @param isZoomIn boolean flag that indicates if the type of the zoom is zoom-in or zoom-out
         */
        public ZoomEvent(double xLowerBound, double xUpperBound, double yLowerBound,
                         double yUpperBound, boolean isZoomIn) {
            this();
            this.xLowerBound = xLowerBound;
            this.xUpperBound = xUpperBound;
            this.yLowerBound = yLowerBound;
            this.yUpperBound = yUpperBound;
            this.isZoomIn = isZoomIn;
        }

        /**
         * Returns a boolean flag that indicates if the
         * type of the zoom is zoom-in or zoom-out
         *
         * @return the zoom-in/out indicating boolean flag
         */
        public boolean isZoomIn() {
            return isZoomIn;
        }

        /**
         * Returns the x lower bound of the zoom
         * @return the x lower bound of the zoom
         */
        public double getXAxisLowerBound() {
            return xLowerBound;
        }

        /**
         * Returns the x upper bound of the zoom
         * @return the x upper bound of the zoom
         */
        public double getXAxisUpperBound() {
            return xUpperBound;
        }

        /**
         * Returns the y lower bound of the zoom
         * @return the y lower bound of the zoom
         */
        public double getYAxisLowerBound() {
            return yLowerBound;
        }

        /**
         * Returns the y upper bound of the zoom
         * @return the y upper bound of the zoom
         */
        public double getYAxisUpperBound() {
            return yUpperBound;
        }

    }
}
