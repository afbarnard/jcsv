/*
 * Copyright (c) 2015 Aubrey Barnard.  This is free software.  See
 * LICENSE for details.
 */

package com.github.afbarnard.jcsv;

import java.util.NoSuchElementException;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class StreamBufferTest {

    static final int BUFFER_SIZE = 10;

    static final Integer[] sequence = {
        58, 76, 21, 29,  6, 88, 65, 55, 73, 92,
        15, 33, 77, 24,  5,  9, 12, 49, 46, 50,
         4, 45, 36, 11, 70, 40, 39, 47, 48, 61,
        13, 66, 67, 52, 81, 72, 10, 94, 60, 44,
        56, 20, 17, 87, 63, 25, 97, 74, 80, 69,
        31, 71, 95, 62, 41, 53, 30, 78, 82, 16,
        83, 93, 14, 91, 90, 51, 26, 32, 22,  2,
        23, 57,  3, 42, 27,  8, 43, 79, 96, 99,
        68, 54,  7, 37, 34, 75, 59, 89, 64,  1,
        28, 18, 85, 19, 35, 38, 98, 86,  0, 84
    };

    StreamBuffer<Integer> buffer;

    // Helper to load content into buffer
    private void putIntoBuffer(int amount) {
        for (int i = 0; i < amount; i++) {
            buffer.put(sequence[i]);
        }
    }

    private void checkSizeCapacity(int size, int capacity) {
        assertEquals(size, buffer.size());
        assertEquals(capacity, buffer.capacity());
    }

    private void checkLowerUpper(long lower, long upper) {
        assertEquals(lower, buffer.lower());
        assertEquals(upper, buffer.upper());
    }

    @Before public void setUp() {
        buffer = new StreamBuffer<Integer>(BUFFER_SIZE);
    }

    ////////////////////////////////////////
    // Basics

    @Test public void newSizeCapacity() {
        checkSizeCapacity(0, BUFFER_SIZE);
    }

    @Test public void newPositions() {
        checkLowerUpper(0, 0);
    }

    @Test(expected=NoSuchElementException.class)
    public void noSuchElement() {
        buffer.get();
    }

    @Test public void putGet() {
        Integer i = Integer.valueOf(647);
        buffer.put(i);
        assertEquals(i, buffer.get());
        checkSizeCapacity(0, BUFFER_SIZE);
        checkLowerUpper(1, 1);
    }

    @Test public void putGetAt() {
        Integer i = Integer.valueOf(128);
        buffer.put(i);
        assertEquals(i, buffer.getAt(0));
        checkSizeCapacity(1, BUFFER_SIZE);
        checkLowerUpper(0, 1);
    }

    @Test public void putFree() {
        Integer i = Integer.valueOf(263);
        buffer.put(i);
        buffer.free(0);
        checkSizeCapacity(0, BUFFER_SIZE);
        checkLowerUpper(1, 1);
    }

    @Test public void freeHalf() {
        int full = BUFFER_SIZE;
        int half = full / 2;
        Object[][] pattern = {{'w', full}, {'f', half}};
        checkAccess(sequence, pattern, BUFFER_SIZE);
        checkSizeCapacity(half, BUFFER_SIZE);
        checkLowerUpper(half, full);
    }

    @Test public void freeAll() {
        int amount = 5;
        Object[][] pattern = {{'w', amount}, {'f', amount}};
        checkAccess(sequence, pattern, BUFFER_SIZE);
        checkSizeCapacity(0, BUFFER_SIZE);
        checkLowerUpper(amount, amount);
    }

    @Test public void growCapacityAfterGet() {
        // Insert enough to cause a resize after writing and reading
        // initial content
        int amount = BUFFER_SIZE * 2;
        Object[][] pattern = {{'w', 1}, {'r', 1}, {'w', amount}};
        checkAccess(sequence, pattern, BUFFER_SIZE);
        checkSizeCapacity(amount, BUFFER_SIZE * 2);
    }

    @Test public void growCapacityAfterFree() {
        // Insert enough to cause a resize after writing and freeing
        // initial content
        int amount = BUFFER_SIZE + 1;
        Object[][] pattern = {{'w', 1}, {'f', 1}, {'w', amount}};
        checkAccess(sequence, pattern, BUFFER_SIZE);
        checkSizeCapacity(amount, BUFFER_SIZE * 2);
    }

    @Test public void freeOld() {
        Integer i = Integer.valueOf(410);
        buffer.put(i);
        buffer.put(i);
        buffer.free(0);
        buffer.free(1);
        buffer.free(0);
        buffer.free(1);
        checkSizeCapacity(0, BUFFER_SIZE);
        checkLowerUpper(2, 2);
    }

    ////////////////////////////////////////
    // Access patterns

    @Test public void checkAccess_oneByOne() {
        Object[][] pattern = {{'w', 1}, {'r', 1}, {'w', 1}, {'f', 1}};
        checkAccess(sequence, pattern, BUFFER_SIZE);
        assertEquals(0, buffer.size());
    }

    @Test public void checkAccess_oneByOneFull() {
        // Make sure the buffer goes around at least once
        Object[][] pattern =
            {{'w', BUFFER_SIZE}, {'r', 1}, {'w', 1}, {'f', 1},
             {'w', 1}, {'r', 1}, {'w', 1}, {'f', 1}, {'w', 1}, {'f', 1},
             {'w', 1}, {'r', 1}, {'w', 1}, {'r', 1}, {'w', 1}, {'f', 1},
             {'w', 1}, {'r', 1}, {'w', 1}, {'f', 1}, {'w', 1}, {'r', 1},
             {'w', 1}, {'r', 1}, {'w', 1}, {'r', 1}};
        checkAccess(sequence, pattern, BUFFER_SIZE);
        assertEquals(BUFFER_SIZE - 1, buffer.size());
    }

    @Test public void checkAccess_chunksLessCapacity() {
        int amount = 3 * BUFFER_SIZE / 4;
        Object[][] pattern =
            {{'w', amount}, {'r', amount},
             {'w', amount}, {'f', amount},
             {'w', amount}, {'f', amount},
             {'w', amount}, {'r', amount}};
        checkAccess(sequence, pattern, BUFFER_SIZE);
        assertEquals(0, buffer.size());
    }

    @Test public void checkAccess_chunksCapacity() {
        int amount = BUFFER_SIZE;
        Object[][] pattern =
            {{'w', amount}, {'f', amount},
             {'w', amount}, {'r', amount},
             {'w', amount}, {'r', amount},
             {'w', amount}, {'f', amount}};
        checkAccess(sequence, pattern, BUFFER_SIZE);
        assertEquals(0, buffer.size());
    }

    @Test public void checkAccess_chunksMoreCapacity() {
        int amount = BUFFER_SIZE + 1;
        Object[][] pattern =
            {{'w', amount}, {'r', amount},
             {'w', amount}, {'f', amount},
             {'w', amount}, {'r', amount},
             {'w', amount}, {'r', amount}};
        checkAccess(sequence, pattern, BUFFER_SIZE);
        assertEquals(0, buffer.size());
    }

    @Test public void checkAccess_queueLessCapacity() {
        int amount = BUFFER_SIZE / 2;
        Object[][] pattern =
            {{'w', amount - 1},
             {'w', amount}, {'r', amount},
             {'w', amount}, {'r', amount},
             {'w', amount}, {'f', amount},
             {'w', amount}, {'r', amount},
             {'w', amount}, {'f', amount},
             {'r', amount - 1}};
        checkAccess(sequence, pattern, BUFFER_SIZE);
        assertEquals(0, buffer.size());
    }

    @Test public void checkAccess_queueCapacity() {
        int amount = BUFFER_SIZE;
        Object[][] pattern =
            {{'w', amount},
             {'w', amount}, {'f', amount},
             {'w', amount}, {'r', amount},
             {'w', amount}, {'f', amount},
             {'w', amount}, {'f', amount},
             {'r', amount}};
        checkAccess(sequence, pattern, BUFFER_SIZE);
        assertEquals(0, buffer.size());
    }

    @Test public void checkAccess_queueMoreCapacity() {
        int amount = 3 * BUFFER_SIZE / 2;
        Object[][] pattern =
            {{'w', amount},
             {'w', amount}, {'r', amount},
             {'w', amount}, {'r', amount},
             {'r', amount}};
        checkAccess(sequence, pattern, BUFFER_SIZE);
        assertEquals(0, buffer.size());
    }

    @Test public void checkAccess_pattern01() {
        Object[][] pattern =
            {{'w',  8}, {'f',  6}, {'w', 12}, {'r', 14}, // 20, 0
             {'w',  4}, {'w', 13}, {'f', 12}, {'w', 14}, // 51, 19
             {'w',  6}, {'f',  7}, {'w',  4}, {'f', 14}, // 61, 8
             {'r',  8}};
        checkAccess(sequence, pattern, BUFFER_SIZE);
        assertEquals(0, buffer.size());
    }

    @Test public void checkAccess_pattern02() {
        Object[][] pattern =
            {{'w', 17}, {'r',  6}, {'w', 10}, {'w', 18}, // 45, 39
             {'w', 13}, {'w', 14}, {'w',  6}, {'f',  3}, // 78, 69
             {'w', 14}, {'w',  5}, {'f', 17}, {'w',  3}, // 100, 74
             {'f',  6}, {'r',  7}, {'f',  6}, {'r', 20}, // 100, 35
             {'r',  9}, {'r', 17}};                      // 100, 9
        checkAccess(sequence, pattern, BUFFER_SIZE);
        assertEquals(9, buffer.size());
    }

    @Test public void checkAccess_pattern03() {
        Object[][] pattern =
            {{'w',  4}, {'f',  4}, {'w', 15}, {'f', 12}, // 19, 3
             {'w',  1}, {'w', 11}, {'w',  3}, {'f',  2}, // 34, 16
             {'w', 10}, {'w',  7}, {'w',  6}, {'w', 13}, // 70, 52
             {'r', 13}, {'r', 13}, {'f', 13}};           // 70, 13
        checkAccess(sequence, pattern, BUFFER_SIZE);
        assertEquals(13, buffer.size());
    }

    @Test public void checkAccess_pattern04() {
        Object[][] pattern =
            {{'w',  8}, {'w',  9}, {'w', 45}, {'f', 44}, // 62, 18
             {'w', 17}, {'f', 23}, {'w',  6}, {'r',  7}  // 85, 11
            };
        checkAccess(sequence, pattern, BUFFER_SIZE);
        assertEquals(11, buffer.size());
    }

    /**
     * Simulates and checks various access patterns according to the
     * given schedule.
     */
    private void checkAccess(Integer[] sequence,
                             Object[][] schedule,
                             int initialCapacity) {
        int lower = 0;
        int upper = 0;
        int size = 0;
        int capacity = initialCapacity;

        // Check empty
        checkSizeCapacity(size, capacity);
        checkLowerUpper(lower, upper);

        // Write and read chunks according to the schedule
        for (Object[] pair : schedule) {
            char access = ((Character) pair[0]).charValue();
            int amount = ((Integer) pair[1]).intValue();

            // Read, write, or free a chunk
            switch (access) {
            case 'r':
                for (int offset = 0; offset < amount; offset++) {
                    assertEquals(sequence[lower + offset], buffer.get());
                }
                lower += amount;
                size -= amount;
                break;
            case 'w':
                for (int offset = 0; offset < amount; offset++) {
                    buffer.put(sequence[upper + offset]);
                }
                upper += amount;
                size += amount;
                // Increase capacity if needed
                while (size > capacity)
                    capacity *= 2;
                break;
            case 'f':
                buffer.free(lower + amount - 1);
                lower += amount;
                size -= amount;
                break;
            }

            // Check the buffer
            checkSizeCapacity(size, capacity);
            checkLowerUpper(lower, upper);
            for (int position = lower; position < upper; position++) {
                assertEquals(sequence[position], buffer.getAt(position));
            }
        }
    }
}
