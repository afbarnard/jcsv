package com.github.afbarnard.jcsv;

import java.io.StringReader;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class LexerTest {

    private Lexer lexer;

    public Lexer makeLexer(String input) {
        return new Lexer(Dialect.LOOSE, new StringReader(input));
    }

    public void checkToken(String text, Token.Type type, long position,
                           int length, long line, int column,
                           Token token, int tokenIndex) {
        tokenIndex++;
        String errorFormat = "%s mismatch for \"%s\" at token %s:";
        assertEquals(String.format(errorFormat, "Text", text,
                                   tokenIndex),
                     text, lexer.getString(token));
        assertEquals(String.format(errorFormat, "Type", text,
                                   tokenIndex),
                     type, token.type);
        assertEquals(String.format(errorFormat, "Position", text,
                                   tokenIndex),
                     position, token.position);
        assertEquals(String.format(errorFormat, "Length", text,
                                   tokenIndex),
                     length, token.length);
        assertEquals(String.format(errorFormat, "Line", text,
                                   tokenIndex),
                     line, token.line);
        assertEquals(String.format(errorFormat, "Column", text,
                                   tokenIndex),
                     column, token.column);
    }

    public void checkTokenStream(Object[][] tokenStream) {
        long position = 1;
        long line = 1;
        int column = 1;

        int tokenIndex = 0;
        //for (Token token : lexer) {
        Token token = lexer.readToken();
        while (token != null && tokenIndex < tokenStream.length) {
            String text = (String) tokenStream[tokenIndex][0];
            Token.Type type = (Token.Type) tokenStream[tokenIndex][1];
            checkToken(text, type, position, text.length(),
                       line, column, token, tokenIndex);
            position += text.length();
            if (type == Token.Type.NEWLINE) {
                line++;
                column = 1;
            } else {
                column += text.length();
            }
            tokenIndex++;
            token = lexer.readToken();
        }
        assertEquals(tokenStream.length, tokenIndex);
        assertNull(token);
    }

    // These tests line up with TestText.java.

    @Test public void readToken_empty() {
        lexer = makeLexer(TestText.empty);

        assertNull(lexer.readToken());
    }

    @Test public void readToken_delimiters() {
        Object[][] tokens = {
            {",", Token.Type.DELIMITER},
            {",", Token.Type.DELIMITER},
            {",", Token.Type.DELIMITER},
            {"\n", Token.Type.NEWLINE},
            {"\n", Token.Type.NEWLINE},
            {",", Token.Type.DELIMITER},
            {",", Token.Type.DELIMITER},
            {"\n", Token.Type.NEWLINE},
            {",", Token.Type.DELIMITER},
            {"\n", Token.Type.NEWLINE},
        };
        lexer = makeLexer(TestText.delimiters);
        checkTokenStream(tokens);
    }

    @Test public void readToken_newlines() {
        Object[][] tokens = {
            {"\n", Token.Type.NEWLINE},
            {"\r", Token.Type.NEWLINE},
            {"\r", Token.Type.NEWLINE},
            {"\n", Token.Type.NEWLINE},  // FIXME
        };
        lexer = makeLexer(TestText.newlines);
        checkTokenStream(tokens);
    }

    @Test public void readToken_space() {
        lexer = makeLexer(TestText.space);
        checkToken("\t\u000b\f \u00a0", Token.Type.SPACE, 1, 5, 1, 1,
                   lexer.readToken(), 0);
        assertNull(lexer.readToken());
    }

    @Test public void readToken_quotesAndEscapes() {
        Object[][] tokens = {
            {"'", Token.Type.CONTENT},
            {"\\", Token.Type.ESCAPE},
            {"'", Token.Type.CONTENT},
            {"\\", Token.Type.ESCAPE},
            {"'", Token.Type.CONTENT},
            {"\\", Token.Type.ESCAPE},
            {"\\", Token.Type.ESCAPE},
            {",", Token.Type.DELIMITER},
            {"'''''", Token.Type.CONTENT},
            {",", Token.Type.DELIMITER},
            {"\"", Token.Type.QUOTE},
            {"\\", Token.Type.ESCAPE},
            {"\"", Token.Type.QUOTE},
            {"\\", Token.Type.ESCAPE},
            {"\"", Token.Type.QUOTE},
            {",", Token.Type.DELIMITER},
            {"\"", Token.Type.QUOTE},
            {"\"", Token.Type.QUOTE},
            {"\"", Token.Type.QUOTE},
            {"\"", Token.Type.QUOTE},
            {"\"", Token.Type.QUOTE},
        };
        lexer = makeLexer(TestText.quotesAndEscapes);
        checkTokenStream(tokens);
    }

    @Test public void readToken_comment() {
        Object[][] tokens = {
            {"#", Token.Type.COMMENT},
            {"#", Token.Type.COMMENT},
            {"#", Token.Type.COMMENT},
            {" ", Token.Type.SPACE},
            {"heading", Token.Type.CONTENT},
            {"\n", Token.Type.NEWLINE},
            {"data", Token.Type.CONTENT},
            {"#", Token.Type.COMMENT},
            {"comment", Token.Type.CONTENT},
            {"\n", Token.Type.NEWLINE},
            {"#", Token.Type.COMMENT},
            {" ", Token.Type.SPACE},
            {"#", Token.Type.COMMENT},
            {"#", Token.Type.COMMENT},
            {"\t", Token.Type.SPACE},
            {"comment", Token.Type.CONTENT},
            {" ", Token.Type.SPACE},
            {"\n", Token.Type.NEWLINE},
            {"data", Token.Type.CONTENT},
            {"\n", Token.Type.NEWLINE},
        };
        lexer = makeLexer(TestText.comment);
        checkTokenStream(tokens);
    }

    @Test public void readToken_singleData() {
        lexer = makeLexer(TestText.singleData);

        checkToken("single-data", Token.Type.CONTENT, 1, 11, 1, 1,
                   lexer.readToken(), 0);
        assertNull(lexer.readToken());
    }

    @Test public void readToken_multilineField() {
        Object[][] tokens = {
            {"one", Token.Type.CONTENT},
            {" ", Token.Type.SPACE},
            {"1", Token.Type.CONTENT},
            {",", Token.Type.DELIMITER},
            {" ", Token.Type.SPACE},
            {"\"", Token.Type.QUOTE},
            {"two", Token.Type.CONTENT},
            {"\n", Token.Type.NEWLINE},
            {"2", Token.Type.CONTENT},
            {"\"", Token.Type.QUOTE},
            {",", Token.Type.DELIMITER},
            {" ", Token.Type.SPACE},
            {"three", Token.Type.CONTENT},
            {" ", Token.Type.SPACE},
            {"3", Token.Type.CONTENT},
            {"\n", Token.Type.NEWLINE},
        };
        lexer = makeLexer(TestText.multilineField);
        checkTokenStream(tokens);
    }

    @Test public void readToken_magicSquare3x3() {
        Object[][] tokens = {
            {"4", Token.Type.CONTENT},
            {",", Token.Type.DELIMITER},
            {"9", Token.Type.CONTENT},
            {",", Token.Type.DELIMITER},
            {"2", Token.Type.CONTENT},
            {"\n", Token.Type.NEWLINE},
            {"3", Token.Type.CONTENT},
            {",", Token.Type.DELIMITER},
            {"5", Token.Type.CONTENT},
            {",", Token.Type.DELIMITER},
            {"7", Token.Type.CONTENT},
            {"\n", Token.Type.NEWLINE},
            {"8", Token.Type.CONTENT},
            {",", Token.Type.DELIMITER},
            {"1", Token.Type.CONTENT},
            {",", Token.Type.DELIMITER},
            {"6", Token.Type.CONTENT},
            {"\n", Token.Type.NEWLINE},
        };
        lexer = makeLexer(TestText.magicSquare3x3);
        checkTokenStream(tokens);
    }

    @Test public void readToken_allBasicCharacters() {
        Object[][] tokens = {
            {"`1234567890-=", Token.Type.CONTENT},
            {"\t", Token.Type.SPACE},
            {"qwertyuiop[]", Token.Type.CONTENT},
            {"\\", Token.Type.ESCAPE},
            {"asdfghjkl;'", Token.Type.CONTENT},
            {"\n", Token.Type.NEWLINE},
            {"zxcvbnm", Token.Type.CONTENT},
            {",", Token.Type.DELIMITER},
            {"./", Token.Type.CONTENT},
            {" ", Token.Type.SPACE},
            {"\r", Token.Type.NEWLINE},
            {"\n", Token.Type.NEWLINE},  // FIXME
            {"~!@", Token.Type.CONTENT},
            {"#", Token.Type.COMMENT},
            {"$%^&*()_+\bQWERTYUIOP{}|ASDFGHJKL:", Token.Type.CONTENT},
            {"\"", Token.Type.QUOTE},
            {"\n", Token.Type.NEWLINE},
            {"ZXCVBNM<>?", Token.Type.CONTENT},
            {"\f", Token.Type.SPACE},
            {"\n", Token.Type.NEWLINE},
            {"\r", Token.Type.NEWLINE},
            {"\t\u00a0", Token.Type.SPACE},
        };
        lexer = makeLexer(TestText.allBasicCharacters);
        checkTokenStream(tokens);
    }

    @Test public void readToken_longTokens() {
        Object[][] tokens = {
            {",", Token.Type.DELIMITER},
            {TestText.longToken00001, Token.Type.CONTENT},
            {",", Token.Type.DELIMITER},
            {TestText.longToken00010, Token.Type.CONTENT},
            {",", Token.Type.DELIMITER},
            {TestText.longToken00100, Token.Type.CONTENT},
            {",", Token.Type.DELIMITER},
            {TestText.longToken01000, Token.Type.CONTENT},
            {",", Token.Type.DELIMITER},
            {TestText.longToken10000, Token.Type.CONTENT},
            {"\n", Token.Type.NEWLINE},
        };
        String text =
            String.format(",%s,%s,%s,%s,%s\n",
                          TestText.longToken00001,
                          TestText.longToken00010,
                          TestText.longToken00100,
                          TestText.longToken01000,
                          TestText.longToken10000);
        lexer = makeLexer(text);
        checkTokenStream(tokens);
    }
}
