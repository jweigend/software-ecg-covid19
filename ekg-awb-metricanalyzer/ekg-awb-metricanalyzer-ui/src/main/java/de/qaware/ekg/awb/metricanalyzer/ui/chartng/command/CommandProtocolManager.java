package de.qaware.ekg.awb.metricanalyzer.ui.chartng.command;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import de.qaware.ekg.awb.sdk.core.log.EkgLogger;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static com.fasterxml.jackson.databind.DeserializationFeature.READ_ENUMS_USING_TO_STRING;
import static de.qaware.ekg.awb.metricanalyzer.ui.chartng.command.ChartCommand.*;

/**
 * A manager that holds the CommandProtocol and write all proceed commands to it.
 * The main task of this manager is to keep the protocol as clear as possible.
 * So it implement a lot of business logic to resolve older records that become obsolete by
 * the execution of new commands and remove or invalidates this records so it will no
 * longer included in the serialized log.
 */
public class CommandProtocolManager {

    private static final Logger LOGGER = EkgLogger.get();

    private CommandProtocol commandProtocol;

    private int amountOfBackgroundCharts = 0;

    private int amountOfCombines = 0;

    private static final Set<ChartCommand> CHANGE_STYLE_COMMANDS = Set.of(
            CHANGE_CHART_COLOR, CHANGE_SERIES_COLOR, SET_CHART_VISIBLE, CHANGE_CHART_TYPE, FORCE_ALIGN_Y_AXIS, FREE_ALIGN_Y_AXIS
    );

    private static final Set<ChartCommand> RESET_BASE_DATA_COMMANDS = Set.of(
            LOAD_BASE_CHART_DATA
    );

    private static final Set<ChartCommand> RESET_ALL_DATA_COMMANDS = Set.of(
            CLEAR_ALL
    );

    private static final Set<ChartCommand> MODIFY_BG_CHARTS_COMMANDS = Set.of(
            DELETE_BG_CHART, DELETE_ALL_BG_CHARTS, RELATIVE_COMBINE_TO_BASE, ABSOLUTE_COMBINE_TO_BASE
    );



    //=================================================================================================================
    // various constructors
    //=================================================================================================================

    public CommandProtocolManager() {
        this.commandProtocol = new CommandProtocol();
    }

    public CommandProtocolManager(CommandProtocol commandProtocol) {
        this.commandProtocol = commandProtocol;
    }

    //=================================================================================================================
    // public API of the CommandProtocolManager class
    //=================================================================================================================

    /**
     * Writes the given ChartCommand, its target and additional parameters to the command protocol
     * and invalidate or remove all older records (actions) that get obsolete by this new command.
     *
     * @param command a ChartCommand action that will executed
     * @param targetChartId an optional id of the chart the command relates to
     * @param arguments optional arguments that are required to execute the command
     */
    public void logStrCommand(ChartCommand command, String targetChartId, String ... arguments ) {
        updateHistory(command, targetChartId);

        if (command == DELETE_BG_CHART) {
            return;
        }

        commandProtocol.logStrCommand(command, targetChartId, arguments);
    }

    /**
     * Writes the given ChartCommand to the logged command
     * history in the protocol and invalidate or remove all older records (actions)
     * that get obsolete by this new command.
     *
     * @param command a ChartCommand action that will executed without specific target chart
     */
    public void logCommand(ChartCommand command) {
        logStrCommand(command, null);
    }

    /**
     * Returns the CommandProtocol that stores the whole history of the
     * executed commands since the last total reset or initial opening of the analytic workspace.
     *
     * @return the CommandProtocol with the logged history of actions
     */
    public CommandProtocol getCommandProtocol() {
        return commandProtocol;
    }

    /**
     * Serialize the given ChartDataFetchParameters object to
     * an JSON String representation.
     *
     * @param queryParams the ChartDataFetchParameters containing a tuple of compute & filter query parameters
     * @return the serialized JSON representation of the object
     */
    public static String serializeQueryParams(ChartDataFetchParameters queryParams) {
        try {
            return new ObjectMapper().writeValueAsString(queryParams);

        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Deserialize the JSON serialized query parameters and return it as
     * ChartDataFetchParameters object.
     * This entity is a protocol argument of the last LOAD_BASE_CHART_DATA command and
     * not the protocol itself.
     *
     * @param serializedQueryParamTuple the JSON serialized filter and compute parameters of a metric query
     * @return the parameter as ChartDataFetchParameters instance
     */
    public static ChartDataFetchParameters deserializeChartFetchParams(String serializedQueryParamTuple) {

        try {
            final ObjectReader jacksonReader = new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .readerFor(ChartDataFetchParameters.class)
                    .with(READ_ENUMS_USING_TO_STRING);

            return jacksonReader.readValue(serializedQueryParamTuple);

        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }


    //=================================================================================================================
    //  internal business logic
    //=================================================================================================================

    /**
     * Prepares the command protocol to remove entries that won't cause
     * an effective change in the view then the newer command will executed.
     *
     * For example if command 1 change chart style to 'area chart' and command 2
     * change it to 'line chart' the first command become obsolete.
     *
     * @param command the chart command that will executed as next
     * @param targetChartId the id of the target chart (can be null) to that will included in the check
     */
    private void updateHistory(ChartCommand command, String targetChartId) {

        // if protocol is empty we have nothing to do because there is no history to update
        if (commandProtocol.getAllRecords().isEmpty()) {
            return;
        }

        if (CHANGE_STYLE_COMMANDS.contains(command)) {
            // change styles commands can invalidate older equal commands that targeting the same chart
            handleChangeStyleCommand(command, targetChartId);

        } else if (RESET_BASE_DATA_COMMANDS.contains(command)) {
            // in this case every command that relates to the base chart since the last push
            // gets obsolete and can invalidated
            if (amountOfBackgroundCharts > 0) {
                invalidateBaseChartCommandsAfterLastPush();
            } else {
                commandProtocol.reset();
            }

        } else if (RESET_ALL_DATA_COMMANDS.contains(command)) {
            // in case of this command type everything gets obsolete and we can clear the whole protocol
            commandProtocol.reset();
            amountOfBackgroundCharts = 0;
            amountOfCombines = 0;

        } else if (MODIFY_BG_CHARTS_COMMANDS.contains(command)) {
            handleBackgroundChartOccurrenceChanged(command, targetChartId);

        } else if (command == PUSH_BASE_TO_BG) {
            amountOfBackgroundCharts++;
        }
    }

    /**
     * Invalidates all protocol records that affects background charts
     * that are removed by the the current command.
     * If the command is of type combine just the according counter will affected.
     *
     * @param command the ChartCommand to proceed
     * @param targetChartId the id of the background chart the command relates to.
     */
    private void handleBackgroundChartOccurrenceChanged(ChartCommand command, String targetChartId) {

        // nothing to do in case of combining chart (maybe style changes on base chart but this isn't a big deal)
        if (command == RELATIVE_COMBINE_TO_BASE || command == ABSOLUTE_COMBINE_TO_BASE) {
            amountOfCombines++;
            return;
        }

        List<ProtocolRecord> protocolRecords = commandProtocol.getAllRecords();

        if (command == DELETE_ALL_BG_CHARTS) {
            amountOfCombines = 0;
            amountOfBackgroundCharts = 0;

            int indexOfLastPush = searchLastIndexOf(PUSH_BASE_TO_BG, null);

            if (indexOfLastPush < 0) {
                return;
            }

            // invalidate every action that affects the background charts until the latest push
            for (int index = 0; index <= indexOfLastPush; index++) {
                protocolRecords.get(index).invalidate();
            }

            // no further actions after the last push so we can stop here
            if (indexOfLastPush + 1 == protocolRecords.size()) {
                return;
            }

            // invalidate very action that changes the style of background charts after the latest push
            for (int index = indexOfLastPush + 1; index < protocolRecords.size(); index++) {

                String optionalTargetId = protocolRecords.get(index).getTargetChartId();

                if (StringUtils.isNotBlank(optionalTargetId) && !"baseChart".equals(optionalTargetId)) {
                    protocolRecords.get(index).invalidate();
                }
            }

        } else if (command == DELETE_BG_CHART && StringUtils.isNotBlank(targetChartId)) {

            if (amountOfCombines == 0 && amountOfBackgroundCharts > 0) {
                amountOfBackgroundCharts--;
            }

            // move backward in the history and do three steps
            // 1. invalidate the youngest entries that relating the the background chart and
            //    find the push command and invalidate this
            // 2. invalidate all actions on the base chart between the push before/beginning and
            //    the push of the bg chart to delete

            int step = 1;
            for (int index = protocolRecords.size() - 1; index >= 0; index--) {

                ProtocolRecord record = protocolRecords.get(index);

                if (step == 1) {
                    if (record.getCommand() != PUSH_BASE_TO_BG) {

                        // we will miss to switch to step 2 than the background chart is create by a combine command
                        // we have to accept this

                        if (targetChartId.equals(record.getTargetChartId())) {
                            record.invalidate();
                        }

                    } else if (targetChartId.equals(record.getArgument()[0])) {
                        record.invalidate();
                        step = 2;
                    }

                } else {
                    if (record.getCommand() != PUSH_BASE_TO_BG) {
                        if ("baseChart".equals(record.getTargetChartId()) || StringUtils.isBlank(record.getTargetChartId())) {
                            record.invalidate();
                        }
                    } else {
                        break;
                    }
                }
            }
        }
    }

    /**
     * Invalidate all commands that relate to the base chart and occurred after
     * the latest chart background-push.
     * This is similar to undo every change on the base chart since last background push.
     */
    private void invalidateBaseChartCommandsAfterLastPush() {

        List<ProtocolRecord> protocolRecords = commandProtocol.getAllRecords();

        // these three command will push base-chart data to background in different ways
        int effectiveIndex1 = searchLastIndexOf(PUSH_BASE_TO_BG, null);
        int effectiveIndex2 = searchLastIndexOf(RELATIVE_COMBINE_TO_BASE, null);
        int effectiveIndex3 = searchLastIndexOf(ABSOLUTE_COMBINE_TO_BASE, null);

        int effectiveIndex = effectiveIndex1 > effectiveIndex2 ? effectiveIndex1 : effectiveIndex2;
        effectiveIndex = effectiveIndex3 > effectiveIndex ? effectiveIndex3 : effectiveIndex;

        if (effectiveIndex >= 0) {

            // if index == last element in the list we have nothing to do
            if (effectiveIndex == protocolRecords.size() - 1) {
                return;
            }

            // we will start after last hit
            effectiveIndex++;

        } else {
            // let us begin at first position because there was no push/combine
            effectiveIndex = 0;
        }

        for (int index = effectiveIndex; index < protocolRecords.size(); index++) {

            String targetChartId = protocolRecords.get(index).getTargetChartId();

            if (StringUtils.isBlank(targetChartId) || "baseChart".equals(targetChartId)) {
                protocolRecords.get(index).invalidate();
            }
        }
    }

    /**
     * Invalidates the last command in the command protocol that get's obsolete
     * by the given ChartCommand.
     * This method should only used with commands that change the chart style otherwise
     * it will result is errors.
     *
     * @param command the ChartCommand to proceed
     * @param targetChartId the optional id of the target chart the command relates to
     */
    private void handleChangeStyleCommand(ChartCommand command, String targetChartId) {

        int index;

        if (command == FORCE_ALIGN_Y_AXIS) {
            index = searchLastIndexOf(FREE_ALIGN_Y_AXIS, null);

        } else if (command == FREE_ALIGN_Y_AXIS) {
            index = searchLastIndexOf(FORCE_ALIGN_Y_AXIS, null);

        } else if (StringUtils.isNotBlank(targetChartId)) {
            index = searchLastIndexOf(command, targetChartId);

        } else {
            LOGGER.error("Retrieve an command that will change the style of an specific chart but the chart isn't defined");
            return;
        }

        if (index >= 0) {
            commandProtocol.invalidateRecord(index);
        }
    }

    /**
     * Searches for the last occurrence of the specified command in the CommandProtocol hold by
     * this manager instance. If the targetChartId is not blank it will included in the search
     * and a protocol record must match to both parameters.
     *
     * @param command the command to search for in the protocol
     * @param targetChartId the id of the chart the command relates to. If NULL it won't included in the search.
     * @return the index of the last occurrence a record matches to the search parameters. Otherwise -1 in case of no hit.
     */
    private int searchLastIndexOf(ChartCommand command, String targetChartId) {

        List<ProtocolRecord> protocolRecords = commandProtocol.getAllRecords();

        if (StringUtils.isBlank(targetChartId)) {
            for (int index = protocolRecords.size() - 1; index >= 0; index--) {

                if (protocolRecords.get(index).getCommand() == command) {
                    return index;
                }
            }
        } else {
            for (int index = protocolRecords.size() - 1; index >= 0; index--) {

                if (protocolRecords.get(index).getCommand() == command &&
                        targetChartId.equals(protocolRecords.get(index).getTargetChartId())) {
                    return index;
                }
            }
        }

        return -1;
    }
}
