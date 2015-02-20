/*
 * Copyright (c) 2015 Aubrey Barnard.  This is free software.  See
 * LICENSE for details.
 */

package com.github.afbarnard.jcsv;

import java.util.Arrays;

public class TestText {
    // Basic content for each type of token

    /** Empty content. */
    public static final String empty = "";

    /**
     * Various numbers of delimiters on each line with no other content.
     */
    public static final String delimiters = ",,,\n\n,,\n,\n";

    /**
     * <p>This is a list of all the various types of newlines according
     * to Wikipedia ({@link https://en.wikipedia.org/wiki/Newline}).</p>
     *
     * <ul>
     * <li>\u000a: line feed (LF, \n) (Unix, Linux, OSX)</li>
     * <li>\u000b: vertical tab (VT)</li>
     * <li>\u000c: form feed, page break (FF, \f)</li>
     * <li>\u000d: carriage return (CR, \r) (old Mac)</li>
     * <li>\u000d\u000a: CR+LF (Microsoft)</li>
     * <li>\u0085: next line (NEL)</li>
     * <li>\u2028: line separator (LS)</li>
     * <li>\u2029: paragraph separator (PS)</li>
     * </ul>
     *
     * <p>It would be possible to treat all of these as newlines, but,
     * arguably, VT and FF can be considered space as they do not
     * necessarily imply a new record.  (Also, VT and FF are matched by
     * the regular expression for space, '\s'.)  Further, programming
     * language grammars tend to be conservative and specific and adopt
     * a narrow interpretation.  Accordingly, this software only
     * considers LF, CR, and CR+LF as newlines.  What to do about the
     * unicode separators has not been decided.</p>
     *
     * <p>In Java, one must use '\n' and '\r' for the character literals
     * rather than their Unicode versions (else they get interpreted as
     * actual line feeds and carriage returns rather than character
     * literals).  The following string is three newlines.</p>
     */
    public static final String newlines = "\n\r\r\n";

    /**
     * <p>All the various types of whitespace that match the regular
     * expression '\s'.  This includes VT and FF since they are not
     * considered {@link newlines} for the purposes of regular
     * expressions.  Non-breaking spaces is my addition, and it could
     * arguably be considered content.</p>
     *
     * <ul>
     * <li>\u0009: horizontal tab (HT, \t)</li>
     * <li>\u000b: vertical tab (VT)</li>
     * <li>\u000c: form feed, page break (FF, \f)</li>
     * <li>\u0020: space</li>
     * <li>\u00a0: non-breaking space</li>
     * </ul>
     */
    public static final String space = "\t\u000b\f \u00a0";

    /** A bunch of quotes and escapes. */
    public static final String quotesAndEscapes =
        "'\\'\\'\\\\,''''',\"\\\"\\\",\"\"\"\"\"";

    /** Comments. */
    public static final String comment =
        "### heading\ndata#comment\n# ##\tcomment \ndata\n";

    /** No delimiters and no newlines. */
    public static final String singleData = "single-data";

    /** A multi-line field. */
    public static final String multilineField =
        "one 1, \"two\n2\", three 3\n";

    /** Very basic "normal" input, a 3-by-3 magic square. */
    public static final String magicSquare3x3 = "4,9,2\n3,5,7\n8,1,6\n";

    // Richer content

    /**
     * All the basic characters including the non-Unicode newline
     * variants, all the escape sequences, and the non-breaking space.
     */
    public static final String allBasicCharacters =
        "`1234567890-=\tqwertyuiop[]\\asdfghjkl;'\n" +
        "zxcvbnm,./ \r\n" +
        "~!@#$%^&*()_+\bQWERTYUIOP{}|ASDFGHJKL:\"\n" +
        "ZXCVBNM<>?\f\n\r\t\u00a0";

    /** Basic but assorted input. */
    public static final String poem128 =
        "one, two, space for you  ,\n" +
        "three,four, who wants more?, ???\n" +
        " five, six, call it quits! , ...\n" +
        "seven, eight,but wait:\n" +
        "9,10,again!\n";

    public static final String longToken00001 = makeString(1, '0');
    public static final String longToken00010 = makeString(10, '1');
    public static final String longToken00100 = makeString(100, '2');
    public static final String longToken01000 = makeString(1000, '3');
    public static final String longToken10000 = makeString(10000, '4');

    public static String makeString(int length, char fill) {
        char[] array = new char[length];
        Arrays.fill(array, fill);
        return new String(array);
    }
}
