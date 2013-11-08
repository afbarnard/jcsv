package com.github.afbarnard.jcsv;

public class Token {

    public static enum Type {
        NONE,
        CONTENT,
        DELIMITER,
        NEWLINE,
        SPACE,  // Non-newline space agreeing with regex \s
        QUOTE,
        ESCAPE,
        COMMENT,
        EOF  // Treat EOF as a token
    }

    public Type type;
    public long position;
    public int length;
    public long line;
    public int column;
}
