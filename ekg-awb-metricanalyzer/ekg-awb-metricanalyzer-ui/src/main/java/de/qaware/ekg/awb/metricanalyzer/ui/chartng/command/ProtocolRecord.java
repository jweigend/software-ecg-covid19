package de.qaware.ekg.awb.metricanalyzer.ui.chartng.command;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Record that represents a single chart action like
 * push base chart to background with the target and
 * optional parameters in string representation.
 */
public class ProtocolRecord {

    private ChartCommand command;

    private String targetChartId;

    private String[] argument;

    @JsonProperty("isInvalidated")
    private boolean isInvalidated = false;

    /**
     * Default constructor used for instantiation of this class via
     * reflection that is used by Jackson ObjectMapper
     */
    @SuppressWarnings("unused")
    public ProtocolRecord() {
        // NoOp
    }


    public ProtocolRecord(ChartCommand command, String targetChartId, String[] argument) {
        this.command = command;
        this.targetChartId = targetChartId;
        this.argument = argument;
    }

    public ChartCommand getCommand() {
        return command;
    }

    public String getTargetChartId() {
        return targetChartId;
    }

    public String[] getArgument() {
        return argument;
    }

    @JsonIgnore
    public void invalidate() {
        isInvalidated = true;
    }

    @JsonIgnore
    public boolean isValid() {
        return !isInvalidated;
    }
}
