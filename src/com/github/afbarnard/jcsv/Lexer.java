/*
 * Copyright (c) 2015 Aubrey Barnard.  This is free software.  See
 * LICENSE for details.
 */

package com.github.afbarnard.jcsv;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

// TODO should "position" start at 1 or 0? If zero, rename to "offset"?

public class Lexer implements Iterator<Token>, Iterable<Token> {

    private Dialect dialect;
    private Reader reader;
    private StreamBufferChar buffer;
    private StreamBuffer<Token> tokens;

    public Lexer(Dialect dialect, Reader reader, int bufferSize, int queueSize) {
        this.dialect = dialect;
        this.reader = reader;
        buffer = new StreamBufferChar(bufferSize);
        tokens = new StreamBuffer<Token>(queueSize, () -> new Token());
    }

    public Lexer(Dialect dialect, Reader reader) {
        this(dialect, reader, 1000, 100);
    }

    public Iterator<Token> iterator() {
        // Initialize the iterator by trying to read input
        try {
            readTokens();
        } catch (IOException e) {
            // FIXME
        }
        return this;
    }

    /**
     * @inheritdoc
     *
     * This is a fast, O(1) operation as it should be.
     */
    public boolean hasNext() {
        // Queue could be empty with more input, so also check for EOF
        return tokens.size() > 0 || charCode != -1;
    }

    /**
     * @inheritdoc
     *
     * Pops and returns a token from the token queue.  This is a fast,
     * O(1) operation if a token is available in the queue.  Otherwise
     * {@link #readTokens()} is invoked.  You may invoke {@code
     * readTokens()} yourself at opportune times if you need to
     * guarantee O(1) behavior for this method.
     */
    public Token next() {
        // Read more tokens if needed
        if (tokens.size() <= 0) { // TODO change to a buffer proportion?
            try {
                readTokens();
            } catch (IOException e) {
                // FIXME
            }
        }
        return tokens.get();
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    /*
     * The Lexing Algorithm
     * --------------------
     *
     * The general idea of the lexing algorithm is to break the input
     * into atomic pieces (tokens).  (It is then the parser's job to
     * assemble those tokens into meaningful pieces.)  The atoms are the
     * single character tokens (delimiter, quote, escape, comment),
     * newlines (which may be one or two characters long), and white
     * space and content (which may be arbitrary runs of characters).
     *
     * The idea of the lexing algorithm is to accumulate characters
     * until a longest-possible token is formed, yield that token, and
     * repeat until there is no more input.
     *
     * The specific lexing algorithm follows.
     *
     * do:
     *     read a character
     *     if at EOF:
     *         continue
     *     put the character in the buffer
     *     determine the type of character
     *     if the type of the token being formed is none:
     *         this is the first character, so continue
     *     else if the type of the token being formed is the same as the type of the character:
     *         if the token type is single-character (incl. newline)
     *         +or the two characters being considered make a newline:
     *             yield the formed token and start the next token
     *         else continue to accumulate
     *     else the type is different and a token has been formed:
     *         yield the token and start the next token
     * loop while not at EOF
     *
     * This algorithm must maintain a one-character read-ahead in order
     * to find the longest possible tokens.  As a result, any token
     * yielded during the current iteration does not include the current
     * character being processed.  This means that the current character
     * either extends or terminates the current token.  Consequently,
     * and as a matter of consistency, even if the current character is
     * a one-character token it will not be yielded until the following
     * iteration.
     *
     * While the description above is a convenient way to think of the
     * lexing algorithm, and indeed it could be implemented as above in
     * some languages, this class implements the algorithm (essentially)
     * as an iterator and so needs to keep state in order to enter and
     * exit the loop correctly.  Unfortunately that complicates things a
     * bit and makes this description especially beneficial.
     */

    /*
     * These variables are the "iterator" state for readTokens().  They
     * are here so they are separated from the other class members.
     *
     * To maintain the token processing state, there are essentially two
     * places in the input to keep track of, the beginning of the
     * current token and the current character.  Their types also must
     * be tracked.
     *
     * Keep track of the input location in terms of lines and columns.
     * However, compute the column numbers so input location state only
     * needs to be updated upon encountering a newline.
     */

    private long line = 1;
    private long lineStartPosition = 0;
    private int charCode = -2;  // -2: nothing has been read yet
    private long tokenPosition = 0;
    private long charPosition = -1;
    private char tokenChar;
    private char thisChar;
    private Token.Type tokenType = Token.Type.NONE;
    private Token.Type charType;

    /**
     * Fills the token queue with tokens.  Quits when the queue is full
     * or at EOF.  Call again to read more tokens.  Do not call this
     * method unless you need {@link #next()} to be a guaranteed O(1)
     * operation.
     */
    public void readTokens() throws IOException {
        // Quit if at EOF or if there is no space in the token queue
        if (charCode == -1 || tokens.freeSize() <= 0) {
            return;
        }
        // OK, there are characters to be read and space to record the
        // tokens they make

        // Only read a character before the loop at the very beginning
        // of input.  Otherwise an unprocessed character already exists.
        // Really this is a do-while situation (read character, process
        // it), but it has to be turned into a while loop for Java.
        // Taking this approach allows a single conditional, here,
        // outside the loop.
        if (charCode == -2) {
            charCode = reader.read();
        }

        // Loop to process characters into tokens until the queue is
        // full or EOF
        while (charCode >= 0 && tokens.freeSize() > 0) {
            // Convert the code and put the character in the buffer
            thisChar = (char) charCode;
            buffer.put(thisChar);
            charPosition++;

            // Determine the type of character.  The cases are ordered
            // (as much as possible) with the (expected) most frequent
            // first.
            switch (thisChar) {
            case ' ':
            case '\t':
            case '\u000b':  // Vertical tab
            case '\f':
            case '\u00a0':  // Non-breaking space
                charType = Token.Type.SPACE;
                break;
            case '\n':
            case '\r':
                charType = Token.Type.NEWLINE;
                break;
            default:
                if (thisChar == dialect.delimiter)
                    charType = Token.Type.DELIMITER;
                else if (thisChar == dialect.quote)
                    charType = Token.Type.QUOTE;
                else if (thisChar == dialect.escape)
                    charType = Token.Type.ESCAPE;
                else if (thisChar == dialect.comment)
                    charType = Token.Type.COMMENT;
                else
                    charType = Token.Type.CONTENT;
            }

            // Determine if a token has been formed
            switch (tokenType) {
            case DELIMITER:
            case QUOTE:
            case ESCAPE:
            case COMMENT:
                // Single-character tokens
                processToken();
                break;
            case NEWLINE:
                // Single- or double-character token
                if (tokenType != charType || tokenChar != '\r' || thisChar != '\n') {
                    processToken();
                }
                break;
            case NONE:
                // First token
                tokenChar = thisChar;
                tokenType = charType;
                break;
            default:
                // Arbitrary-length tokens (space or content)
                if (tokenType != charType) {
                    processToken();
                }
            }

            // Get the next character
            charCode = reader.read();
        }
        // Either the token queue is full or EOF

        // If EOF, process the last token.  An unprocessed token exists
        // if the input was not empty.
        if (charCode == -1 && charPosition > -1) {
            charPosition++;
            processToken();
        }
    }

    /**
     * Adds a completed token to the queue and updates necessary state.
     */
    private void processToken() {
        // Get a token to use
        Token token = tokens.put();
        // Populate the token
        token.type = tokenType;
        token.position = tokenPosition;
        token.length = (int)(charPosition - tokenPosition);
        token.line = line;
        token.column = (int)(tokenPosition - lineStartPosition + 1);
        // Update input location
        if (tokenType == Token.Type.NEWLINE) {
            line++;
            lineStartPosition = charPosition;
        }
        // Update token state
        tokenChar = thisChar;
        tokenType = charType;
        tokenPosition = charPosition;
    }

    public Token readToken() {
        // Read more tokens if needed
        if (tokens.size() <= 0) {
            try {
                readTokens();
            } catch (IOException e) {
                // FIXME
            }
        }
        // Return the next token or null if none
        if (tokens.size() > 0) {
            return tokens.get();
        } else {
            return null;
        }
    }

    public void free(Token token) {
        free(token.position + token.length - 1);
    }

    public void free(long position) {
        buffer.free(position);
    }

    public String getString(Token token) {
        return getString(token.position, token.length);
    }

    public String getString(long position, int length) {
        char[] characters = new char[length];
        for (int offset = 0; offset < length; offset++) {
            characters[offset] = buffer.getAt(position + offset);
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
