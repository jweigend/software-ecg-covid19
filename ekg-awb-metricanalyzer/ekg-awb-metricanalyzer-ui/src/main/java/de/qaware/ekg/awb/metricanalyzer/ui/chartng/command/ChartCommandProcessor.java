package de.qaware.ekg.awb.metricanalyzer.ui.chartng.command;

import de.qaware.ekg.awb.common.ui.chartng.ChartType;
import de.qaware.ekg.awb.common.ui.chartng.StackedChartController;
import de.qaware.ekg.awb.common.ui.chartng.ZoomableStackedChart;
import de.qaware.ekg.awb.sdk.core.events.EkgEventBus;
import de.qaware.ekg.awb.sdk.core.events.ProgressEvent;
import de.qaware.ekg.awb.sdk.core.log.EkgLogger;
import de.qaware.ekg.awb.sdk.core.utils.SleepUtil;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.scene.paint.Color;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The ChartCommandProcessor is the single point of chart command execution.
 * Each command that changes the data, the structure or the style of the chart has to pass this processor.
 *
 * Getting each command this processor will be responsible for the following topics:
 * - log each command and it's call arguments via CommandProtocolManager
 * - delegate commands to the according target controllers or services
 * - replay a given command protocol and ensure that it works with the asynchronous JavaFX API
 */
public class ChartCommandProcessor {

    /**
     * The logger used by this class to log errors or special events
     */
    private static final Logger LOGGER = EkgLogger.get();

    /**
     * The service that will used to fetch the chart data and that will called
     * by it then a according command will executed.
     */
    private ChartDataService fillChartDataService;

    /**
     * The controller of the StackedChart that retrieves the delegated commands of this
     * process to change the state of the chart.
     */
    private StackedChartController chartController;

    /**
     * The protocol manager that will used to log the commands like a Logger implementation
     * that writes a log file.
     */
    private CommandProtocolManager commandProtocolManager = new CommandProtocolManager(new CommandProtocol());

    /**
     * An single threaded executor that is used to replay a given
     * command protocol in an asynchronous way.
     */
    private ExecutorService protocolExecutorService = Executors.newSingleThreadExecutor();

    /**
     * A thread safe boolean that will used to indicate that is service calls to
     * retrieve new chart data is in progress and all other actions has to wait for it.
     */
    private AtomicBoolean loadChartTaskIsRunning = new AtomicBoolean(false);

    /**
     * This is a very similar property to {@link this#loadChartTaskIsRunning} but maintained
     * by JavaFX itself and used to double check if the service is still loading or not.
     */
    private ReadOnlyBooleanProperty chartDataServiceRunningProperty = null;

    /**
     * This integer is used to unsure that the JavaFX Platform thread never run's into a recursive
     * call endless loop. It will increment than the platform thread executes a command and decrement
     * then it's done. Second or more calls to command execution will return immediately without do anything.
     */
    private AtomicInteger asyncCallDepth = new AtomicInteger(0);

    /**
     * The Software-EKG event bus that will used to notify others about the progress
     * of loading chart data (only interesting on very huge data sets).
     */
    @Inject
    private EkgEventBus eventBus;

    //================================================================================================================
    // command delegating function
    //================================================================================================================

    /**
     * Executes the given command on the target chart that can (optional) specified by the
     * targetChartId parameter. According to the command type additional arguments will required
     * and used than execute it.
     *
     * In contrast to the {@link ChartCommandProcessor#executeObjectArgCommand(ChartCommand, String, Object...)}
     * method this one accepts only String parameters. Every domain object or specific data type has to specify
     * in a appropriate representation. For example a JavaFX Color class should given as the #xxxxxx color code.
     *
     * Each call of this method will written to the command protocol and can playback any time.
     *
     * @param command a single {@link ChartCommand} to execute by this processor
     * @param targetChartId (optional) the id of the chart
     * @param argument (optional) one or more String-arguments that will used then execute the command
     */
    public void executeStringArgCommand(ChartCommand command, String targetChartId, String ... argument) {

        if (Platform.isFxApplicationThread()) {
            if (asyncCallDepth.get() > 1) {
                return;
            }

            asyncCallDepth.incrementAndGet();
        }

        try {

            switch (command) {

                case LOAD_BASE_CHART_DATA:
                    CommandValidator.checkMultipleValueArray(command, argument, 2,
                            "Serialized:" + ChartDataFetchParameters.class.getName() + ", Boolean(Block) ");
                    loadChartData(CommandProtocolManager.deserializeChartFetchParams(argument[0]), Boolean.valueOf(argument[1]));
                    commandProtocolManager.logStrCommand(command, targetChartId, argument);
                    break;

                case PUSH_BASE_TO_BG:
                    CommandValidator.checkSingleValueArray(command, argument, "newChartId");
                    pushToBackground(argument[0]);
                    commandProtocolManager.logStrCommand(command, targetChartId, argument);
                    break;

                case CHANGE_CHART_COLOR:
                    CommandValidator.checkSingleValueArray(command, argument, "rgbColorCode");
                    CommandValidator.checkTargetChartId(command, targetChartId);
                    // color code must have 2-6 hex chars
                    changeChartColor(targetChartId, Color.web(argument[0]));
                    commandProtocolManager.logStrCommand(command, targetChartId, argument);
                    break;

                case CHANGE_SERIES_COLOR:
                    CommandValidator.checkMultipleValueArray(command, argument, 2,
                            "SeriesName," + Color.class.getName());
                    changeSeriesColor(targetChartId, argument[0], Color.web(argument[1]));
                    commandProtocolManager.logStrCommand(command, targetChartId, argument);
                    break;

                case CLEAR_ALL:
                    CommandValidator.checkIsEmpty(command, argument);
                    clearAll();
                    commandProtocolManager.logCommand(command);
                    break;

                case SET_CHART_VISIBLE:
                    CommandValidator.checkSingleValueArray(command, argument,   "isVisible");
                    setChartVisible(targetChartId, Boolean.valueOf(argument[0]));
                    commandProtocolManager.logStrCommand(command, targetChartId, argument);
                    break;

                case DELETE_BG_CHART:
                    CommandValidator.checkIsEmpty(command, argument);
                    CommandValidator.checkTargetChartId(command, targetChartId);
                    deleteBackgroundChart(targetChartId);
                    commandProtocolManager.logStrCommand(command, targetChartId);
                    break;

                case DELETE_ALL_BG_CHARTS:
                    CommandValidator.checkIsEmpty(command, argument);
                    deleteAllBackgroundCharts();
                    commandProtocolManager.logCommand(command);
                    break;

                case RELATIVE_COMBINE_TO_BASE:
                    CommandValidator.checkSingleValueArray(command, argument, "interpolate(boolean)");
                    combineBgChartsRelativeToBase(Boolean.valueOf(argument[0]));
                    forceAlignYAxis();
                    commandProtocolManager.logStrCommand(command, null, Boolean.valueOf(argument[0]).toString());
                    break;

                case CHANGE_CHART_TYPE:
                    CommandValidator.checkSingleValueArray(command, argument, ChartType.class.getName());
                    CommandValidator.checkTargetChartId(command, targetChartId);
                    changeChartType(targetChartId, ChartType.valueOf(argument[0]));
                    commandProtocolManager.logStrCommand(command, targetChartId, argument);
                    break;

                case FORCE_ALIGN_Y_AXIS:
                    CommandValidator.checkIsEmpty(command, argument);
                    forceAlignYAxis();
                    commandProtocolManager.logCommand(command);
                    break;

                case FREE_ALIGN_Y_AXIS:
                    CommandValidator.checkIsEmpty(command, argument);
                    freeAlignYAxis();
                    commandProtocolManager.logCommand(command);
                    break;

                default:
                    throw new IllegalArgumentException("Unsupported command " + command);
            }

        } catch (IllegalArgumentException e) {
            LOGGER.error("Error occurred trying to execute the command " + command, e);
            throw e;

        } finally {
            if (Platform.isFxApplicationThread()) {
                asyncCallDepth.decrementAndGet();
            }
        }
    }

    /**
     * The shortcut for {@link ChartCommandProcessor#executeObjectArgCommand} that need less
     * arguments to call and is a more convince way to execute simple plain command without
     * additional arguments.
     *
     * @param command a single {@link ChartCommand} to execute by this processor
     */
    public void executeCommand(ChartCommand command) {
        executeObjectArgCommand(command, null, (Object[]) null);
    }

    /**
     * The shortcut for {@link ChartCommandProcessor#executeObjectArgCommand} that need less
     * arguments to call and is a more convince way to execute simple plain command without
     * additional arguments.
     *
     * @param command a single {@link ChartCommand} to execute by this processor
     * @param targetChartId the id of the chart
     */
    public void executeCommand(ChartCommand command, String targetChartId) {
        executeObjectArgCommand(command, targetChartId, (Object[]) null);
    }

    /**
     * Executes the given command on the target chart that can (optional) specified by the
     * targetChartId parameter. According to the command type additional arguments will required
     * and used than execute it.
     *
     * In contrast to the {@link ChartCommandProcessor#executeStringArgCommand(ChartCommand, String, String...)}
     * method this one accepts any type of parameters that can be domain objects or native data types.
     *
     * Each call of this method will written to the command protocol and can playback any time.
     *
     * @param command a single {@link ChartCommand} to execute by this processor
     * @param targetChartId (optional) the id of the chart
     * @param argument (optional) additional arguments that will used then execute the command
     */
    public void executeObjectArgCommand(ChartCommand command, String targetChartId, Object ... argument) {

        if (Platform.isFxApplicationThread()) {
            if (asyncCallDepth.get() > 1) {
                return;
            }

            asyncCallDepth.incrementAndGet();
        }

        try {

            switch (command) {

                case LOAD_BASE_CHART_DATA:
                    CommandValidator.checkMultipleValueArray(command, argument, 2,
                            ChartDataFetchParameters.class.getName() + ", boolean (block until executed)");

                    CommandValidator.checkType(command, ChartDataFetchParameters.class, argument[0]);
                    loadChartData((ChartDataFetchParameters) argument[0], (boolean) argument[1]);
                    String param = CommandProtocolManager.serializeQueryParams((ChartDataFetchParameters) argument[0]);
                    commandProtocolManager.logStrCommand(command, targetChartId, param, argument[1].toString());
                    break;

                case PUSH_BASE_TO_BG:
                    CommandValidator.checkSingleValueArray(command, argument, "newChartId");
                    CommandValidator.checkType(command, String.class, argument[0]);
                    pushToBackground(argument[0].toString());
                    commandProtocolManager.logStrCommand(command, targetChartId, argument[0].toString());
                    break;

                case CHANGE_CHART_COLOR:
                    CommandValidator.checkSingleValueArray(command, argument, Color.class.getName());
                    CommandValidator.checkTargetChartId(command, targetChartId);
                    CommandValidator.checkType(command, Color.class, argument[0]);
                    changeChartColor(targetChartId, (Color) argument[0]);
                    commandProtocolManager.logStrCommand(command, targetChartId, argument[0].toString());
                    break;

                case CHANGE_SERIES_COLOR:
                    CommandValidator.checkMultipleValueArray(command, argument, 2,
                            "SeriesName," + Color.class.getName());
                    CommandValidator.checkType(command, String.class, argument[0]);
                    CommandValidator.checkType(command, Color.class, argument[1]);
                    changeSeriesColor(targetChartId, argument[0].toString(), (Color) argument[1]);
                    commandProtocolManager.logStrCommand(command, targetChartId, argument[0].toString(), argument[1].toString());
                    break;

                case CLEAR_ALL:
                    CommandValidator.checkIsEmpty(command, argument);
                    clearAll();
                    commandProtocolManager.logCommand(command);
                    break;

                case SET_CHART_VISIBLE:
                    CommandValidator.checkSingleValueArray(command, argument, "isVisible");
                    CommandValidator.checkType(command, Boolean.class, argument[0]);
                    setChartVisible(targetChartId, (boolean) argument[0]);
                    commandProtocolManager.logStrCommand(command, targetChartId, Boolean.valueOf((boolean) argument[0]).toString());
                    break;

                case DELETE_ALL_BG_CHARTS:
                    CommandValidator.checkIsEmpty(command, argument);
                    deleteAllBackgroundCharts();
                    commandProtocolManager.logCommand(command);
                    break;

                case DELETE_BG_CHART:
                    CommandValidator.checkIsEmpty(command, argument);
                    CommandValidator.checkTargetChartId(command, targetChartId);
                    deleteBackgroundChart(targetChartId);
                    commandProtocolManager.logStrCommand(command, targetChartId);
                    break;

                case RELATIVE_COMBINE_TO_BASE:
                    CommandValidator.checkSingleValueArray(command, argument, "interpolate");
                    CommandValidator.checkType(command, Boolean.class, argument[0]);
                    combineBgChartsRelativeToBase((Boolean) argument[0]);
                    commandProtocolManager.logStrCommand(command, null, Boolean.valueOf((boolean) argument[0]).toString());
                    break;

                case ABSOLUTE_COMBINE_TO_BASE:
                    combineBgChartsAbsoluteToBase();
                    commandProtocolManager.logCommand(command);
                    break;

                case CHANGE_CHART_TYPE:
                    CommandValidator.checkSingleValueArray(command, argument, ChartType.class.getName());
                    CommandValidator.checkTargetChartId(command, targetChartId);
                    CommandValidator.checkType(command, ChartType.class, argument[0]);
                    changeChartType(targetChartId, (ChartType) argument[0]);
                    commandProtocolManager.logStrCommand(command, targetChartId, argument[0].toString());
                    break;

                case FORCE_ALIGN_Y_AXIS:
                    CommandValidator.checkIsEmpty(command, argument);
                    forceAlignYAxis();
                    commandProtocolManager.logCommand(command);
                    break;

                case FREE_ALIGN_Y_AXIS:
                    CommandValidator.checkIsEmpty(command, argument);
                    freeAlignYAxis();
                    commandProtocolManager.logCommand(command);
                    break;

                default:
                    throw new IllegalArgumentException("Unsupported command " + command);
            }

        } finally {
            if (Platform.isFxApplicationThread()) {
                asyncCallDepth.decrementAndGet();
            }
        }
    }

    /**
     * Executes the given command protocol by replay each single command in the protocol log.
     * The execution will reset the whole chart state first (synchronous) and then executes
     * the commands asynchronously. So the method call will return immediately.
     *
     * The replay of the command protocol will logged by the current protocol in use. So if
     * further change will done the effective new log is the old one + the following actions.
     *
     * @param commandProtocol a CommandProtocol instance to replay
     * @param callback a callback that will executed then the execution of the protocol has finished
     */
    public void executeProtocol(CommandProtocol commandProtocol, Runnable callback) {

        // reset the chart state so it is virgin
        chartController.clearAll();

        // delete the current command protocol because it is obsolete
        getCommandProtocol().reset();

        // delegate the execution of the log to a dedicated thread
        protocolExecutorService.submit(() -> {
            try {
                AtomicBoolean blockSync = new AtomicBoolean(false);

                for (ProtocolRecord record : commandProtocol.getAllRecords()) {

                    // sometimes the continuous execution of commands cause failure in the chart
                    // the reasons are unclear but add a little sleep here prevent this in the most cases
                    SleepUtil.sleep(100);

                    if (record.getCommand() == ChartCommand.LOAD_BASE_CHART_DATA) {

                        while (blockSync.get()) {
                            SleepUtil.sleep(100);
                        }

                        executeStringArgCommand(record.getCommand(), record.getTargetChartId(),
                                record.getArgument()[0], Boolean.TRUE.toString());

                    } else {
                        while (blockSync.get()) {
                            SleepUtil.sleep(100);
                        }

                        blockSync.set(true);
                        Platform.runLater(() -> {
                            try {
                                blockSync.set(true);
                                executeStringArgCommand(record.getCommand(), record.getTargetChartId(), record.getArgument());
                            } finally {
                                blockSync.set(false);
                            }
                        });
                    }
                }

                callback.run();

            } catch (Throwable e) {
                LOGGER.error("Exception occurred during replay of command protocol", e);
            }
        });
    }


    //================================================================================================================
    // accessors as part of the public API
    //================================================================================================================

    /**
     * Returns the underlying {@link CommandProtocol} instance that will updated by this processor
     * then commands are executed or read then the protocol itself will executed to playback previous actions.
     *
     * @return the underlying command protocol hold by this processor.
     */
    public CommandProtocol getCommandProtocol() {
        return commandProtocolManager.getCommandProtocol();
    }

    /**
     * Sets the whole StateChart that will bind to this command processor and is the
     * target for all command actions executed by it.
     *
     * @param zoomableStackedChart the ZoomableStackedChart that will controlled by this processor
     */
    public void setStackedChart(ZoomableStackedChart zoomableStackedChart) {
        this.chartController = zoomableStackedChart.getController();
    }

    /**
     * Sets the service to this CommandProcessor that will used to fetch the chart data
     * and that will called by it then a according command will executed.
     * This processor will bind event handler to the service and use it's states for it's
     * own control-flow of business logic.
     *
     * @param service the date service that provides the time series data for the chart
     */
    public void setChartDataService(ChartDataService service) {
        fillChartDataService = service;

        // make sure progress events will sent to the progress bar
        fillChartDataService.progressProperty().addListener((o, ov, nv) -> eventBus.publish(
                new ProgressEvent(fillChartDataService.getMessage(), fillChartDataService.getProgress(), this))
        );

        fillChartDataService.setOnCancelled(event -> loadChartTaskIsRunning.set(false));
        fillChartDataService.setOnFailed(event -> loadChartTaskIsRunning.set(false));
        fillChartDataService.setOnSucceeded(event -> loadChartTaskIsRunning.set(false));
    }


    //==================================================================================================================
    // internal set of methods the will represents the various command types and executed then the command is delegated
    //==================================================================================================================

    /**
     * Executes a service call that loads new data into the chart based on
     * the given query & compute parameter tuple.
     *
     * @param chartDataFetchParameters a query & compute parameter tuple wrapped in a ChartDataFetchParameters instance
     * @param blockUntilCompleted true if the caller thread should wait until the service call has finished
     */
    private void loadChartData(ChartDataFetchParameters chartDataFetchParameters, boolean blockUntilCompleted) {

        // we need the running property to prevent simultaneous service executions
        if (chartDataServiceRunningProperty == null) {
            Platform.runLater(() -> chartDataServiceRunningProperty = fillChartDataService.runningProperty());
        }

        // sleep until a we don't have the possibility to check if a service call is already running
        while (!Platform.isFxApplicationThread() && chartDataServiceRunningProperty == null) {
            SleepUtil.sleep(20);
        }

        //  we pass all conditions to make a service call, so set the state for it
        loadChartTaskIsRunning.set(true);
        SleepUtil.sleep(20);

        // execute the service calls using the JavaFX platform runner (because it will update the chart)
        Platform.runLater(() -> {
            fillChartDataService.setQueryComputeParams(chartDataFetchParameters.getQueryComputeParams());
            fillChartDataService.setQueryFilterParams(chartDataFetchParameters.getQueryFilterParams());
            fillChartDataService.setResetChartAxis(chartDataFetchParameters.doResetChartAxis());
            fillChartDataService.restart();
        });

        // as the non platform thread that has triggered the FxPlatform thread we have to wait until it has finished
        while (!Platform.isFxApplicationThread() && blockUntilCompleted
                && (chartDataServiceRunningProperty.get() || loadChartTaskIsRunning.get())) {
            SleepUtil.sleep(20);
        }
    }

    private void clearAll() {
        chartController.clearAll();
    }

    private void freeAlignYAxis() {
        chartController.alignYAxis(false);
    }

    private void forceAlignYAxis() {
        chartController.alignYAxis(true);
    }

    private void combineBgChartsRelativeToBase(boolean interpolate) {
        chartController.proportionCombineBackgroundChartsToBase(interpolate);
        freeAlignYAxis();
        forceAlignYAxis();
    }

    private void combineBgChartsAbsoluteToBase() {
        chartController.absoluteCombineBackgroundChartsToBase();
        freeAlignYAxis();
        forceAlignYAxis();
    }

    private void pushToBackground(String newChartId) {
        chartController.pushBaseToBackgroundChart(newChartId);
    }

    private void deleteAllBackgroundCharts() {
        chartController.deleteAllBackgroundCharts(false);
    }

    private void setChartVisible(String chartId, boolean isVisible) {
        chartController.getChartById(chartId).setVisible(isVisible);
    }

    private void deleteBackgroundChart(String chartId) {
        chartController.deleteBackgroundChart(chartId);
    }

    private void changeChartType(String chartId, ChartType newChartType) {
        chartController.changeChartType(chartId, newChartType);
    }

    private void changeChartColor(String chartId, Color newColor) {
        chartController.getChartById(chartId).setChartColor(newColor);
    }

    private void changeSeriesColor(String chartId, String seriesName, Color newColor) {
        chartController.getChartById(chartId).setSeriesColor(seriesName, newColor);
    }
}
