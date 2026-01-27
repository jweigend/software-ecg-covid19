package de.qaware.ekg.awb.da.solr.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to support logging of Solr requests and responses.
 */
public final class LoggingUtils {

    /**
     * A regex that matches a single PN_GUID.
     */
    private static final Pattern REGEX_PN_GUID = Pattern.compile("[A-F0-9]{32}");

    /**
     * A regex that matches a parentheses-enclosed, whitespace- (or {@code "+"}-) separated
     * sequence of at least two PN_GUIDs.
     * <p/>
     * Note the use of an atomic group ({@code "(?>...)"}) to capture the PN_GUIDs. Otherwise,
     * the regex automaton will run into a StackOverflowError when applied to the long sequences
     * of PN_GUIDs that occur in PSMG's log messages.
     */
    private static final Pattern REGEX_SEQ_PN_GUID =
            Pattern.compile("\\(((?>" + REGEX_PN_GUID + "[+\\s]+)+" + REGEX_PN_GUID + ")\\)");

    private static final int KEEP_FRONT = 2;
    private static final int MAX_LENGTH = 2048;

    /**
     * Prevents instantiation of this utility class.
     */
    private LoggingUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the {@link Object#toString() string representation} of the given object
     * with long sequences of PN_GUIDs shortened and abbreviated to a maximum length of
     * {@value #MAX_LENGTH} characters for logging.
     *
     * @param object The object or {@code null}.
     * @return The shortened string representation.
     */
    public static String getShortenedStringForLogging(Object object) {
        if (object == null) {
            return "null";
        }

        String shortened = shortenAllSeqsOfPnGuids(object.toString());
        return StringUtils.abbreviate(shortened, MAX_LENGTH);
    }

    /**
     * Shortens each parentheses-enclosed, whitespace- (or {@code "+"}-) separated sequence of more than
     * {@value #KEEP_FRONT} PN_GUIDs by keeping the first {@value #KEEP_FRONT} and replacing all remaining
     * with the string {@code "... and N more ..."}. Each sequence MUST be enclosed in parentheses.
     *
     * @param input The input string.
     * @return The output string.
     */
    private static String shortenAllSeqsOfPnGuids(String input) {
        // find sequences of PN_GUIDs
        Matcher matcher = REGEX_SEQ_PN_GUID.matcher(input);
        if (!matcher.find()) {
            return input;
        }

        // shorten each sequence to at most 3 PN_GUIDs
        StringBuilder sb = new StringBuilder();
        int currIndex = 0;
        do {
            String replacement = shortenSeqOfPnGuids(matcher.group(), KEEP_FRONT);
            sb.append(input, currIndex, matcher.start())
                    .append(replacement);
            currIndex = matcher.end();
        } while (matcher.find());

        // copy remaining characters
        sb.append(input, currIndex, input.length());

        return sb.toString();
    }

    /**
     * Shortens a sequence of PN_GUIDs by keeping the first {@code keepFront} PN_GUIDs and
     * replacing all further PN_GUIDs with the text {@code "... and N more ..."}.
     * <p/>
     * Everything up to the {@code (keepFront + 1)}. PN_GUID is kept in the result string
     * and everything after the last PN_GUID. The remainder of the string is replaced with
     * the replacement text. The PN_GUIDs may be separated by any kind of string content.
     *
     * @param seq       The input string containing the sequence of PN_GUIDs.
     * @param keepFront The number of PN_GUIDs to keep in the output.
     * @return The output string.
     */
    private static String shortenSeqOfPnGuids(String seq, int keepFront) {
        // keep first PN_GUIDs and count remaining PN_GUIDs
        Matcher matcher = REGEX_PN_GUID.matcher(seq);
        StringBuilder sb = new StringBuilder();
        int lastIndex = 0;
        int currMatch;
        for (currMatch = 0; matcher.find(); currMatch++) {
            if (currMatch < keepFront) {
                sb.append(seq, lastIndex, matcher.end());
            } else if (currMatch == keepFront) {
                sb.append(seq, lastIndex, matcher.start());
            }
            lastIndex = matcher.end();
        }

        // replace remaining PN_GUIDs with replacement string
        int remaining = currMatch - keepFront;
        if (remaining > 0) {
            sb.append(String.format(Locale.ENGLISH, "... and %,d more ...", remaining));
        }

        // append remaining characters
        sb.append(seq, lastIndex, seq.length());

        return sb.toString();
    }
}
