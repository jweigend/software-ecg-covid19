package de.qaware.ekg.awb.repository.api.dataobject.expr;

import de.qaware.ekg.awb.repository.api.schema.Field;
import org.apache.commons.lang3.Validate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Factory to create and nest {@link Expression}s that can be traversed to generate a filter query.
 */
public final class ExprFactory {

    private ExprFactory() {
        throw new UnsupportedOperationException();
    }

    /**
     * Filters the given {@link Field} with the given String values. In a search, only those documents match where the
     * field contains at least one value that exactly matches to one of the given values. The characters of values are
     * interpreted exactly, i.e. never as wildcards!
     * <p>
     * Note on types: The caller must convert their type to String. For convenience, the type {@link LocalDate} is
     * supported natively with {@link #exactFilter(Field, LocalDate...)}.
     *
     * @param field  the field
     * @param values the values
     * @return the filter expression
     */
    public static Expression exactFilter(Field field, String... values) {
        Validate.notNull(field);
        Validate.notEmpty(values);
        return new ExactFilterExpression(field, Arrays.asList(values));
    }

    /**
     * Filters the given {@link Field} with the given {@link LocalDate} values. In a search, only those documents match
     * where the field contains at least one value that exactly matches to one of the given values.
     *
     * @param field  the field
     * @param values the values
     * @return the filter expression
     */
    public static Expression exactFilter(Field field, LocalDate... values) {
        Validate.notNull(field);
        Validate.notEmpty(values);
        return new ExactFilterExpression(field, convertLocalDatesToStrings(values));
    }

    /**
     * Filters the given {@link Field} with the given String values. In a search, only those documents match where the
     * field contains at least one value that matches to one of the given values. The following characters are
     * interpreted as wildcards:
     * <ul>
     * <li>?  Single character wildcard (filled with one character) </li>
     * <li>*  Multiple character wildcard (filled with 0 or more characters)</li>
     * </ul>
     *
     * @param field  the field
     * @param values the values
     * @return the filter expression
     */
    public static Expression wildcardFilter(Field field, String... values) {
        Validate.notNull(field);
        Validate.notEmpty(values);
        return new WildcardFilterExpression(field, Arrays.asList(values));
    }

    /**
     * Filters the given {@link Field} with the given String value. In a search, only those documents match where the
     * field's values contain the whitespace-separated parts of the given value. The following characters are
     * interpreted as wildcards:
     * <ul>
     * <li>?  Single character wildcard (filled with one character) </li>
     * <li>*  Multiple character wildcard (filled with 0 or more characters)</li>
     * </ul>
     *
     * @param field the field
     * @param value the value
     * @return the filter expression
     */
    public static Expression fullTextFilter(Field field, String value) {
        Validate.notNull(field);
        Validate.notEmpty(value);
        return new FullTextFilterExpression(field, value);
    }

    /**
     * Filters the given {@link Field} with the given number range. In a search, only those documents match where the
     * field contains at least one value that is within the lower and upper bounds (both inclusive) of the given
     * range. Bounds may be null to indicate an open-ended bound.
     *
     * @param field      the field
     * @param lowerBound the range's lower bound (may be null to indicate an open-ended lower bound)
     * @param upperBound the range's upper bound (may be null to indicate an open-ended upper bound)
     * @return the filter expression
     */
    public static Expression numberRangeFilter(Field field, Number lowerBound, Number upperBound) {
        Validate.notNull(field);
        return new RangeFilterExpression(field, Objects.toString(lowerBound, null),
                Objects.toString(upperBound, null));
    }

    /**
     * Filters the given {@link Field} with the given date range. In a search, only those documents match where the
     * field contains at least one value that is within the lower and upper bounds (both inclusive) of the given
     * range. Bounds may be null to indicate an open-ended bound.
     *
     * @param field  the field
     * @param lowerBound the range's lower bound (may be null to indicate an open-ended lower bound)
     * @param upperBound the range's upper bound (may be null to indicate an open-ended upper bound)
     * @return the filter expression
     */
    public static Expression dateRangeFilter(Field field, LocalDate lowerBound, LocalDate upperBound) {
        Validate.notNull(field);
        return new RangeFilterExpression(field, convertLocalDateToString(lowerBound),
                convertLocalDateToString(upperBound));
    }


    public static Expression dateTimeRangeFilter(Field field, Instant lowerBound, Instant upperBound) {
        Validate.notNull(field);
        return new RangeFilterExpression(field,
                convertLocalDateTimeToString(lowerBound),
                convertLocalDateTimeToString(upperBound));
    }


    /**
     * Returns a new {@link Expression} combining the given {@link Expression}s with boolean AND (conjunction).
     *
     * @param expressions the expressions to combine
     * @return the new {@link Expression}
     */
    public static Expression and(Expression... expressions) {
        validateExpressionVarArgs(expressions);
        if (expressions.length == 1) {
            return expressions[0];
        } else {
            return new AndExpression(Arrays.asList(expressions));
        }
    }

    /**
     * Returns a new {@link Expression} combining the given {@link Expression}s with boolean OR (disjunction).
     *
     * @param expressions the expressions to combine
     * @return the new {@link Expression}
     */
    public static Expression or(Expression... expressions) {
        validateExpressionVarArgs(expressions);
        if (expressions.length == 1) {
            return expressions[0];
        } else {
            return new OrExpression(Arrays.asList(expressions));
        }
    }

    /**
     * Returns a new {@link Expression} negating the given expression.
     *
     * @param expression the expression to negate
     * @return the new {@link Expression}
     */
    public static Expression not(Expression expression) {
        Validate.notNull(expression);
        return new NotExpression(expression);
    }

    /**
     * Validates the given expressions by checking that the array is not null and contains no null expressions.
     *
     * @param expressions the expressions
     */
    private static void validateExpressionVarArgs(Expression[] expressions) {
        Validate.notEmpty(expressions);
        for (Expression expression : expressions) {
            Validate.notNull(expression);
        }
    }

    /**
     * Converts the given {@link LocalDate}s to date strings. null dates will be mapped to null strings.
     *
     * @return the converted dates
     */
    private static List<String> convertLocalDatesToStrings(LocalDate[] dates) {
        return Arrays.stream(dates)
                .map(ExprFactory::convertLocalDateToString)
                .collect(Collectors.toList());
    }

    /**
     * Converts the given {@link LocalDate} to a date string. null dates will be mapped to null strings.
     *
     * @param date the date
     * @return the converted date
     */
    private static String convertLocalDateToString(LocalDate date) {
        if (date == null) {
            return null; // allow to search for date attributes with value null
        } else {
            // assumption: the search index saves Dates as Strings in ISO-8601 format
            return DateTimeFormatter.ISO_INSTANT.format(date.atStartOfDay(ZoneId.of("UTC")));
        }
    }

    private static String convertLocalDateTimeToString(Instant dateTime) {
        if (dateTime == null) {
            return null; // allow to search for date attributes with value null
        } else {
            return dateTime.toString();
        }
    }
}
