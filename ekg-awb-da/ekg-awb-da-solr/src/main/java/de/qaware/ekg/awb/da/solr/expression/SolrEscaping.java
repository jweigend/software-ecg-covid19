package de.qaware.ekg.awb.da.solr.expression;

/**
 * Provides functions to escape special characters in Solr queries.
 */
/* package-private */ final class SolrEscaping {

    private SolrEscaping() {
        throw new UnsupportedOperationException();
    }

    /**
     * Escapes characters with special meaning in Solr queries.
     *
     * @param field the field name
     * @param value the value
     * @param keepWildcards If true, the wildcard characters '*' and '?' will not be escaped, i.e. keep their special
     *                      meaning. If false, they will be treated as ordinary characters.
     * @param keepWhitespace If true, {@link Character#isWhitespace(char) whitespace characters} will not be escaped,
     *                      i.e. keep their special meaning. If false, they will be treated as ordinary characters.
     * @return the value with all special characters escaped
     */
    static String escape(String field, String value, boolean keepWildcards, boolean keepWhitespace) {
        // Special treatment for null or empty
        if (value == null) {
            return "(*:* NOT " + field + ":*)";
        } else if (value.isEmpty()) {
            return "\"\"";
        } else {
            return escapeQueryChars(value, keepWildcards, keepWhitespace);
        }
    }

    /**
     * Escapes all special characters of the given value, optionally excluding wildcard and/or whitespace characters.
     *
     * @param value the value
     * @param keepWildcards If true, the wildcard characters '*' and '?' will not be escaped, i.e. keep their special
     *                      meaning. If false, they will be treated as ordinary characters.
     * @param keepWhitespace If true, {@link Character#isWhitespace(char) whitespace characters} will not be escaped,
     *                      i.e. keep their special meaning. If false, they will be treated as ordinary characters.
     * @return the value with all special characters escaped
     */
    private static String escapeQueryChars(String value, boolean keepWildcards, boolean keepWhitespace) {
        // Note: This implementation is based on ClientUtils.escapeQueryChars with optional handling of
        // the wildcard characters * and ? or the whitespace characters
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            // These characters are part of the query syntax and must be escaped
            if (escapeAsQuerySyntax(c) || escapeAsWildcard(c, keepWildcards) || escapeAsWhitespace(c, keepWhitespace)) {
                sb.append('\\');
            }
            sb.append(c);
        }
        return sb.toString();
    }

    @SuppressWarnings({"squid:MethodCyclomaticComplexity", "squid:S1067"}) // not really complex, just many characters
    private static boolean escapeAsQuerySyntax(char c) {
        return c == '\\' || c == '+' || c == '-' || c == '!' || c == '(' || c == ')' || c == ':'
                || c == '^' || c == '[' || c == ']' || c == '\"' || c == '{' || c == '}' || c == '~'
                || c == '|' || c == '&' || c == ';' || c == '/';
    }

    private static boolean escapeAsWildcard(char c, boolean keepWildcards) {
        return !keepWildcards && (c == '*' || c == '?');
    }

    private static boolean escapeAsWhitespace(char c, boolean keepWhitespace) {
        return !keepWhitespace && Character.isWhitespace(c);
    }
}
