package de.qaware.ekg.awb.metricanalyzer.ui.chartng.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.type.CollectionType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.databind.DeserializationFeature.READ_ENUMS_USING_TO_STRING;

/**
 * The command protocol is a record of all common actions that proceed in the chart view.
 * It will used to recreate a specific view state that needs both: the data/settings and
 * the chronological order. The second one for example is very important the order of background
 * charts or function like proportional combine.
 *
 * The CommandProtocol stores all actions as a list of records and provides some useful functions
 * on it especially the serialization/deserialization of the records.
 */
public class CommandProtocol {

    private List<ProtocolRecord> protocolRecords = new ArrayList<>();

    //=================================================================================================================
    // Serialization API of this class
    //=================================================================================================================

    /**
     * Restores a filled CommandProtocol from the given serialized protocol string.
     * The given serialized protocol have to be in JSON format.
     * Use {@link CommandProtocol#toSerializedString} method to serialize the protocol and ensure the
     * JSON format matches the requirements.
     *
     * @param serializedProtocol the JSON serialized command protocol
     * @return the POJO representation of the serialized command protocol
     */
    public static CommandProtocol createFromString(String serializedProtocol) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            CollectionType type = mapper.getTypeFactory().constructCollectionType(List.class, ProtocolRecord.class);

            final ObjectReader jacksonReader = new ObjectMapper()
                    .readerFor(type)
                    .with(READ_ENUMS_USING_TO_STRING);

            List<ProtocolRecord> records = jacksonReader.readValue(serializedProtocol);

            CommandProtocol protocol = new CommandProtocol();
            protocol.protocolRecords = records;
            return protocol;

        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Returns a JSON serialized version of this CommandProtocol.
     *
     * Use {@link CommandProtocol#createFromString(String)} method to transform/deserialize the
     * CommandProtocol back from the JSON string.
     *
     * @return the serialized representation of this CommandProtocol
     */
    public String toSerializedString() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(getEffectiveRecords());

        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    //=================================================================================================================
    // Accessors and control methods that operates on the protocol itself
    //=================================================================================================================

    /**
     * Clears the complete protocol.
     * The protocol will be empty after this call returns.
     */
    public void reset() {
        protocolRecords.clear();
    }

    /**
     * Add a new protocol record to the command protocol that represents
     * the action the user proceed in the chart panel.
     *
     * This log method accepts command arguments in a string representation. If the caller as other object
     * types like JavaFX Color class or more complex ones, the parameter has to serialized before given
     * to this method.
     *
     * @param command a enum that defines the command that was executed (like push the base chart to background)
     * @param targetChartId the optional id of the target chart that is addressed by the executed command
     * @param argument an empty or filled array with string based commands.
     */
    public void logStrCommand(ChartCommand command, String targetChartId, String[] argument) {
        protocolRecords.add(new ProtocolRecord(command, targetChartId, argument));
    }

    /**
     * Returns the whole list of records this
     * command protocol contains.
     *
     * @return a list of ProtocolRecord instances
     */
    public List<ProtocolRecord> getAllRecords() {
        return protocolRecords;
    }

    /**
     * Returns a list of all records that are not invalidated
     * and will applied to the serialized protocol.
     * All others will lost at serialization and are not effective for the outcome.
     *
     * @return the valid records that are effective for the protocol
     */
    public List<ProtocolRecord> getEffectiveRecords() {
        return getAllRecords()
                .stream()
                .filter(ProtocolRecord::isValid)
                .collect(Collectors.toList());
    }

    /**
     * Returns the string representation of the {@link ChartDataFetchParameters} bean
     * that was add with the latest {@link ChartCommand#LOAD_BASE_CHART_DATA} command to the protocol.
     *
     * If no entry exists in the protocol null will returned.
     *
     * @return the ChartDataFetchParameters as string or null if not exists
     */
    public String getLastSerializedChartLoadParameter() {

        for (int index = protocolRecords.size() - 1; index >= 0; index--) {
            if (protocolRecords.get(index).getCommand() == ChartCommand.LOAD_BASE_CHART_DATA &&
                    protocolRecords.get(index).isValid()) {
                return protocolRecords.get(index).getArgument()[0];
            }
        }

        return null;
    }

    /**
     * Removes all records in the protocol that are removed after index = lastPushCommandIndex.
     * This means older records will reserved, younger records removed.
     *
     * @param lastPushCommandIndex the index of the first record that will removed
     */
    public void clearLoadCommandsAfter(int lastPushCommandIndex) {
        ProtocolRecord[] recordArray = protocolRecords.toArray(new ProtocolRecord[0]);

        protocolRecords = new ArrayList<>(Arrays.asList(recordArray).subList(0, lastPushCommandIndex));
        protocolRecords.addAll(Arrays.asList(recordArray).subList(lastPushCommandIndex, recordArray.length));
    }

    public int getNextInsertIndex() {
        return protocolRecords.size();
    }

    public void invalidateRecord(int index) {
        if (index >= 0 && index < protocolRecords.size()) {
            protocolRecords.get(index).invalidate();
        }
    }
}
