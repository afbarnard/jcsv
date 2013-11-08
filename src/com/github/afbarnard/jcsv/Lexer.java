package com.github.afbarnard.jcsv;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class Lexer implements Iterator<Token>, Iterable<Token> {

    private Dialect dialect;
    private Reader reader;
    private StreamBufferChar buffer;
    private Deque<Token> tokenPool;

    private long position = 0;  // The position is the number of the current character
    private Token.Type charType = Token.Type.NONE;

    private long lastNewlinePosition = 0;

    private long tokenPosition = 1;
    private long line = 1;
    private long tokenLine = 1;
    private int tokenColumn = 1;
    private Token.Type tokenType = Token.Type.NONE;

    private Token nextToken;

    public Lexer(Dialect dialect, Reader reader, int bufferSize) {
        this.dialect = dialect;
        this.reader = reader;
        buffer = new StreamBufferChar(bufferSize);
        tokenPool = new ArrayDeque<Token>(100);
    }

    public Lexer(Dialect dialect, Reader reader) {
        this(dialect, reader, 1000);
    }

    public Iterator<Token> iterator() {
        return this;
    }

    public boolean hasNext() {
        // Try to read if nothing has been read yet
        if (position == 0) {
            nextToken = readToken();
            return nextToken != null;
        }
        return charType != Token.Type.EOF;
    }

    public Token next() {
        // Check a next token exists
        if (!hasNext())
            throw new NoSuchElementException("No next token.");

        // Return a token if one has already been read
        if (nextToken != null) {
            Token token = nextToken;
            nextToken = null;
            return token;
        }

        // Return a fresh token
        return readToken();
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public Token readToken() {
        // Return null if at EOF
        if (charType == Token.Type.EOF) {
            return null;
        }

        // Locals
        int charCode = 0;
        char thisChar;
        Token.Type thisCharType;

        // Read characters until a complete token has been formed.  This
        // loop is do-while style because a character always needs to be
        // read in order to complete a token.
        boolean tokenComplete = false;
        while (!tokenComplete) {
            // Read the next character
            try {
                charCode = reader.read();
            } catch (IOException e) {
                // FIXME
            }
            position++;

            // Process the character if not EOF
            if (charCode >= 0) {
                // Record the character
                thisChar = (char) charCode;
                buffer.put(thisChar);

                // Determine the type of character.  The cases are
                // ordered with the (expected) most frequent first.
                switch (thisChar) {
                case ' ':
                case '\t':
                case '\u000b':  // Vertical tab
                case '\f':
                case '\u00a0':
                    thisCharType = Token.Type.SPACE;
                    break;
                case '\n':
                case '\r':
                    thisCharType = Token.Type.NEWLINE;
                    break;
                default:
                    if (thisChar == dialect.delimiter)
                        thisCharType = Token.Type.DELIMITER;
                    else if (thisChar == dialect.quote)
                        thisCharType = Token.Type.QUOTE;
                    else if (thisChar == dialect.escape)
                        thisCharType = Token.Type.ESCAPE;
                    else if (thisChar == dialect.comment)
                        thisCharType = Token.Type.COMMENT;
                    else
                        thisCharType = Token.Type.CONTENT;
                }

                // Determine if this character starts a new token and
                // therefore a token is complete.  A token is complete
                // if this character is a single-character token
                // (automatically separating it from the previous token)
                // or if this character is content or space and the
                // previous character was a different type.  A token is
                // not complete if this is the first character.
                if (charType == Token.Type.NONE) {
                    // 'tokenComplete' stays false.  Initialize the
                    // token type.
                    tokenType = thisCharType;
                } else {
                    switch (thisCharType) {
                    case CONTENT:
                    case SPACE:
                        tokenComplete = (charType != thisCharType);
                        break;
                    case DELIMITER:
                    case NEWLINE:
                    case QUOTE:
                    case ESCAPE:
                    case COMMENT:
                        tokenComplete = true;
                        break;
                    }
                }

                // Bring the character type at position up to date now
                // that prev/next comparison complete
                charType = thisCharType;
            } else {
                // Record EOF
                charType = Token.Type.EOF;
                tokenComplete = true;
            }
        }

        // Check for empty input and return null rather than creating a
        // zero-length token
        if (charType == Token.Type.EOF && position == 1) {
            return null;
        }

        // A complete, non-trivial token is in the buffer.  Make a token
        // object for it.
        Token token = makeToken();
        token.position = tokenPosition;
        token.line = tokenLine;
        token.column = tokenColumn;
        token.type = tokenType;
        token.length = (int)(position - tokenPosition);

        // Keep track of lines in a way that works with the
        // one-character lookahead scheme
        if (tokenType == Token.Type.NEWLINE) {
            line++;
            lastNewlinePosition = position - 1;
        }

        // This character is the start of the next token
        tokenPosition = position;
        tokenLine = line;
        tokenColumn = (int)(position - lastNewlinePosition);
        tokenType = charType;

        // Return the token
        return token;
    }

    private Token makeToken() {
        if (tokenPool.isEmpty()) {
            return new Token();
        } else {
            return tokenPool.pop();
        }
    }

    public void free(Token token) {
        tokenPool.push(token);
        free(token.position + token.length - 1);
    }

    public void free(long position) {
        buffer.free(position - 1);
    }

    public String getString(Token token) {
        return getString(token.position, token.length);
    }

    public String getString(long position, int length) {
        char[] characters = new char[length];
        for (int offset = 0; offset < length; offset++) {
            // Subtract 1 because StreamBuffer is zero-indexed
            characters[offset] = buffer.getAt(position + offset - 1);
        }
        return new String(characters);
    }

    public static void main(String[] args) throws Exception {
        FileReader fileReader = new FileReader(args[0]);
        Reader reader = new BufferedReader(fileReader);

        Lexer lexer = new Lexer(Dialect.LOOSE, reader);
        //Token token;
        String text;

        //while ((token = lexer.readToken()) != null) {
        for (Token token : lexer) {
            text = lexer.getString(token.position, token.length);
            System.out.println(String.format("%s: '%s' @(%d,%d)", token.type, text, token.position, token.length));
        }
    }
}
