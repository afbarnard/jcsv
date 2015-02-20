/*
 * Copyright (c) 2015 Aubrey Barnard.  This is free software.  See
 * LICENSE for details.
 */

package com.github.afbarnard.jcsv;

// TODO what about no quoting?
// TODO what about multi-character delimiters?
// TODO support multiple line terminators?

public class Dialect {
    public static enum QuoteEscapeStyle {
        DOUBLED,
        ESCAPED,
        EITHER
    }

    char delimiter;
    char quote;
    char escape;
    char comment;
    QuoteEscapeStyle quoteEscapeStyle;
    boolean trimSpace;
    boolean allowBlankLines;
    boolean allowComments;
    boolean allowVariableLengthRecords;

    public Dialect(char delimiter,
                   char quote,
                   char escape,
                   char comment,
                   QuoteEscapeStyle quoteEscapeStyle,
                   boolean trimSpace,
                   boolean allowBlankLines,
                   boolean allowComments,
                   boolean allowVariableLengthRecords
                   ) {
        this.delimiter = delimiter;
        this.quote = quote;
        this.escape = escape;
        this.comment = comment;
        this.quoteEscapeStyle = quoteEscapeStyle;
        this.trimSpace = trimSpace;
        this.allowBlankLines = allowBlankLines;
        this.allowComments = allowComments;
        this.allowVariableLengthRecords = allowVariableLengthRecords;
    }

    public static final Dialect LOOSE =
        new Dialect(',', '"', '\\', '#',
                    QuoteEscapeStyle.EITHER,
                    true, // trim space
                    true, // allow blank lines
                    true, // allow comments
                    true  // allow variable-length records
                    );
}
