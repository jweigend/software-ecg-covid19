package de.qaware.ekg.awb.common.ui.chartng;

import de.qaware.ekg.awb.common.ui.chartng.axis.DateAxis;
import de.qaware.ekg.awb.common.ui.chartng.axis.MetricAxis;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import org.apache.commons.lang3.time.StopWatch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class ChartTestApplication extends Application {

    public static void main(String[] argc) throws Exception {
       launch(argc);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        Scene scene = new Scene(getLineChart() , 1400, 1000);

        primaryStage.setTitle("Charts Baby!");
        primaryStage.setScene(scene);
        primaryStage.show();

    }


    private LineChart<Long, Double> getLineChart() {

        //defining the axes
        final DateAxis xAxis = new DateAxis();
        final MetricAxis yAxis = new MetricAxis(0, 100, 1);
        xAxis.setLabel("Number of Month");


        //creating the chart
        final LineChart<Long, Double> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setAnimated(false);
        lineChart.setCreateSymbols(false);
        lineChart.setTitle("Stock Monitoring, 2010");
        lineChart.getStylesheets().add(ChartTestApplication.class.getResource("/ChartStyle.css").toExternalForm());

        //defining a series
        XYChart.Series<Long, Double> series = new XYChart.Series<>();
        series.setName("My portfolio");

        AtomicInteger counter = new AtomicInteger(1);

        getSeriesStream().forEach(pseries -> {

            new Thread(() -> {
                try {
                    Thread.sleep(2000L * counter.getAndIncrement());
                    Platform.runLater(() -> {
                        lineChart.getData().add(pseries);
                    });

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();


        });


        return lineChart;
    }

    private static final int AMOUNT_SERIES = 4;
    private static final int AMOUNT_SERIES_INTERVALS = 100;
    private static final int AMOUNT_SERIES_POINTS_PER_INTERVAL = 500;

    private Stream<XYChart.Series<Long, Double>> getSeriesStream() {

        StopWatch stopWatch = StopWatch.createStarted();

        String[] groupArray = new String[] {"Group_1", "Group_2", "Group_3", "Group_4"};


        List<XYChart.Series<Long, Double>> seriesList = new ArrayList<>();

        for (int n=0; n < AMOUNT_SERIES; n++) {

            XYChart.Series<Long, Double> series = new XYChart.Series<>();
            series.setName(groupArray[n]);
            Collection<XYChart.Data<Long, Double>> dataList = new ArrayList<>(AMOUNT_SERIES_INTERVALS * AMOUNT_SERIES_POINTS_PER_INTERVAL);

            for (int m=1; m < AMOUNT_SERIES_INTERVALS; m++) {

                int start = AMOUNT_SERIES_POINTS_PER_INTERVAL * (m - 1);
                int max = AMOUNT_SERIES_POINTS_PER_INTERVAL * m;

                double lastValue = 0;
                for (int i = start; i < max; i++) {
                    lastValue = ((Math.random() * 100) + 5 * lastValue) / 6;
                    dataList.add(new XYChart.Data<>((long) i, lastValue));
                }
            }

            series.getData().addAll(dataList);
            seriesList.add(series);
        }

        System.out.println("finished generating data after " + stopWatch);

        return seriesList.stream();
    }
}
