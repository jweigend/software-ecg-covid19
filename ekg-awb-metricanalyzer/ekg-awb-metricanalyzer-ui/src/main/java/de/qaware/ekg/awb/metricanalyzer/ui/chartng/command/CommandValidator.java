package de.qaware.ekg.awb.metricanalyzer.ui.chartng.command;

import org.apache.commons.lang3.Validate;

import java.util.Objects;

/**
 * A helper class that will used to validate the
 * call arguments then {@link ChartCommand} will executed.
 */
public class CommandValidator {

    public static void checkType(ChartCommand command, Class expectedType, Object parameterValue) {

        Validate.isTrue(parameterValue.getClass() == expectedType,
                "The type of the given argument doesn't match the expected '" + expectedType
                        + "' that is required to execute command '" + command + "'");
    }


    public static void checkTargetChartId(ChartCommand command, String targetChartId) {
        Validate.notNull(targetChartId, "The target chartId must specified to execute command '"
                + command + "'");
    }

    public static void checkIsEmpty(ChartCommand command, Object[] argument) {
        if (!(argument == null || argument.length == 0)) {
            throw new IllegalArgumentException("No additional parameter expected for this command '" + command
                    + "', but '" + Objects.toString(argument) + "' is given.");
        }
    }

    @SuppressWarnings("SameParameterValue")
    public static void checkMultipleValueArray(ChartCommand command, Object[] argument, int expectedParameterAmount,
                                         String expectedParameter) {
        Validate.notNull(argument, "Object argument is null, but parameter '"
                + expectedParameter + "' are expected to execute '" + command+ "'");

        if (argument.length > expectedParameterAmount) {
            throw new IllegalArgumentException("More than expected object arguments given. Only the parameter '"
                    + expectedParameter + "' are expected to execute '" + command+ "'");
        }

        if (argument.length < expectedParameterAmount) {
            throw new IllegalArgumentException("Less than the expected amount of object arguments given. Parameter '"
                    + expectedParameter + "' are expected to execute '" + command+ "'");
        }
    }


    public static void checkSingleValueArray(ChartCommand command, Object[] argument, String expectedParameter) {
        Validate.notNull(argument, "Object argument is null, but a single parameter '"
                + expectedParameter + "' is expected to execute command '" + command+ "'");

        if (argument.length > 1) {
            throw new IllegalArgumentException("Multiple object arguments given, but only a single parameter '"
                    + expectedParameter + "' is expected to execute command '" + command+ "'");
        }

        if (argument.length == 0) {
            throw new IllegalArgumentException("Object arguments are empty, but a single parameter '"
                    + expectedParameter + "' is expected to execute command '" + command+ "'");
        }

        Validate.notNull(argument[0], "The additional parameter is null, but '" + expectedParameter
                + "' is expected to execute command " + command);

    }
}
