/*
 * Copyright (c) 2015 Aubrey Barnard.  This is free software.  See
 * LICENSE for details.
 */

package com.github.afbarnard.jcsv;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

/**
 * A sliding view of a sequence (or stream) with random access to the
 * contents implemented as a dynamic circular buffer.  In other words, a
 * dynamic queue that indexes its elements and provides random access to
 * its contents.
 */
public class StreamBuffer<E> {

    /** Array that stores the contents of the buffer. */
    private Object[] buffer;

    /**
     * The buffer index that corresponds to the lower sequence index.
     */
    private int lowerIndex = 0;

    /**
     * The sequence index of the head of the queue, the lower bound on
     * the buffer content.
     */
    private long lower = 0;

    /**
     * The sequence index of the first available spot in the buffer, the
     * tail (plus one) of the queue, the upper bound on the buffer
     * content (in the sense that the content occupies indices
     * [lower,upper)).
     */
    private long upper = 0;

    /** An allocator for buffer elements. */
    private Supplier<E> allocator;

    public boolean debug = false;

    public StreamBuffer(int initialCapacity, Supplier<E> allocator) {
        buffer = new Object[initialCapacity];
        this.allocator = allocator;
        // All the number fields start at zero
    }

    public StreamBuffer(int initialCapacity) {
        this(initialCapacity, null);
    }

    public StreamBuffer(Supplier<E> allocator) {
        this(1000, allocator);
    }

    public StreamBuffer() {
        this(1000, null);
    }

    public int capacity() {
        return buffer.length;
    }

    public int size() {
        return (int)(upper - lower);
    }

    //public int freeSize() {
    //    return buffer.length - (int)(upper - lower);
    //}

    public long lower() {
        return lower;
    }

    public long upper() {
        return upper;
    }

    public void put(E element) {
        debug("put(E)", upper);
        // Make sure there is room
        if ((int)(upper - lower) >= buffer.length) {
            growBuffer(buffer.length + 1);
        }

        // Add the element
        buffer[bufferIndex(upper)] = element;
        upper++;
    }

    // start reference types only
    public E put() {
        debug("put()", upper);
        // Make sure there is room
        if ((int)(upper - lower) >= buffer.length) {
            growBuffer(buffer.length + 1);
        }
        // Create an element if needed
        int upperIndex = bufferIndex(upper);
        if (buffer[upperIndex] == null && allocator != null) {
            buffer[upperIndex] = allocator.get();
        }
        upper++;
        // Return the element
        return buffer(upperIndex);
    }
    // end reference types only

    /**
     * Gets the element with the lowest sequence index and removes it
     * from the buffer (frees its location).  Equivalent to
     * <code>getAt(lower())</code> followed by
     * <code>free(lower())</code>.
     */
    public E get() {
        debug("get()", lower);
        if (lower >= upper)
            throw new NoSuchElementException();
        E element = buffer(bufferIndex(lower));
        internalFree(lower);
        return element;
    }

    public E getAt(long position) {
        debug("getAt()", position);
        checkPosition(position);
        return buffer(bufferIndex(position));
    }

    public void free(long position) {
        debug("free()", position);
        // Only free valid positions, ignore any previously freed
        // positions
        if (position >= lower) {
            checkPosition(position);
            internalFree(position);
        }
    }

    ////////////////////////////////////////
    // Internal

    private void checkPosition(long position) {
        if (position < lower || position >= upper) {
            throw new NoSuchElementException(
                String.format("Position %d is not in [%d,%d)",
                              position, lower, upper)
            );
        }
    }

    private int bufferIndex(long position) {
        return (lowerIndex + (int)(position - lower)) % buffer.length;
    }

    // Do type<E> access to the array the same way as in ArrayList
    @SuppressWarnings("unchecked")
    private E buffer(int index) {
        return (E) buffer[index];
    }

    private void internalFree(long position) {
        // How much to free? (includes 'position')
        int amount = (int)(position - lower) + 1;
        // Move up lower buffer index limit (with wrap-around)
        lowerIndex = (lowerIndex + amount) % buffer.length;
        // Move up lower sequence index limit
        lower = position + 1;
    }

    private void growBuffer(int minimumCapacity) {
        // Grow the buffer by at least a power of two
        int capacity = Math.max(buffer.length * 2, minimumCapacity);

        // Allocate a new buffer
        Object[] newBuffer = new Object[capacity];

        // Copy the contents of the old buffer to the start of the new
        // buffer discarding any circularity.  Only copy contents if the
        // buffer is not empty.  If lowerIndex == upperIndex then the
        // buffer is either full or empty, so use the positions to
        // discriminate.
        int upperIndex = bufferIndex(upper);
        if (lowerIndex < upperIndex) {
            // The buffer has content that is linear.  Do a single
            // linear copy.
            System.arraycopy(buffer, lowerIndex, newBuffer, 0,
                             (upperIndex - lowerIndex));
        } else if (lowerIndex > upperIndex || lower < upper) {
            // The buffer has content that is wrapped.  Do two linear
            // copies for the two chunks.
            int chunkSize = buffer.length - lowerIndex;
            System.arraycopy(buffer, lowerIndex, newBuffer, 0,
                             chunkSize);
            // Protect against empty copies
            if (upperIndex > 0) {
                System.arraycopy(buffer, 0, newBuffer, chunkSize,
                                 upperIndex);
            }
        }
        // Else the buffer is empty and no copy is needed

        // Use the new buffer
        buffer = newBuffer;

        // Reset the buffer offset of 'lower'
        lowerIndex = 0;
    }

    private void debug(String source, long position) {
        if (debug) {
            System.out.println(String.format("%s: @%d: %s", source, position, toDebugString()));
        }
    }

    private String toDebugString() {
        return String.format("[%d,%d) -> [%d,%d); %s", lower, upper, lowerIndex, bufferIndex(upper), Arrays.toString(buffer));
    }
}
