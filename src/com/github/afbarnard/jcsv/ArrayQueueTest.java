package com.github.afbarnard.jcsv;

import java.util.NoSuchElementException;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class ArrayQueueTest {

    static final int QUEUE_SIZE = 10;

    ArrayQueue<Integer> queue;

    // Utility functions

    private void multiPut(int[] elements) {
        for (int element: elements) {
            queue.put(Integer.valueOf(element));
        }
    }

    private void multiGet(int count) {
        for (int i = 0; i < count; i++) {
            queue.get();
        }
    }

    private void multiGetCheck(int[] elements) {
        for (int element: elements) {
            assertEquals(element, queue.get().intValue());
        }
    }

    private void checkSizes(int size, int free) {
        assertEquals(size, queue.size());
        assertEquals(free, queue.freeSize());
    }

    // Tests

    @Before public void setUp() {
        queue = new ArrayQueue<Integer>(QUEUE_SIZE);
    }

    @Test public void newSizes() {
        checkSizes(0, QUEUE_SIZE);
    }

    @Test public void initialUseSizes() {
        int[] ints = {-83, -72, 22};
        multiPut(ints);
        checkSizes(ints.length, QUEUE_SIZE - ints.length);
    }

    @Test public void fullSizes() {
        int[] ints = {-75, 76, 9, -53, -53, 49, 29, 76, 74, -44};
        multiPut(ints);
        checkSizes(QUEUE_SIZE, 0);
    }

    @Test public void wrapAroundSizes() {
        int[] ints = {13, -53, -32, -92, 94, 32, 51};
        multiPut(ints);
        multiGet(ints.length);
        multiPut(ints);
        checkSizes(ints.length, QUEUE_SIZE - ints.length);
        multiGet(ints.length);
        multiPut(ints);
        checkSizes(ints.length, QUEUE_SIZE - ints.length);
        multiGet(ints.length);
        multiPut(ints);
        checkSizes(ints.length, QUEUE_SIZE - ints.length);
    }

    @Test public void emptyAfterUseSizes() {
        int[] ints1 = {-8, -46, -82, 94, 81, -45, 66};
        int[] ints2 = {-86, -46, -13, -34, -87, -46};
        multiPut(ints1);
        multiGet(ints1.length);
        checkSizes(0, QUEUE_SIZE);
        multiPut(ints2);
        multiGet(ints2.length);
        checkSizes(0, QUEUE_SIZE);
    }

    @Test public void fullAfterUseSizes() {
        int[] ints1 = {10, 16, -82, -61, 20, 32, 14, -84, 92};
        int[] ints2 = {15, 87, -69, -77, -35, 38, -70, 78, -68, 7};
        multiPut(ints1);
        multiGet(ints1.length);
        multiPut(ints1);
        multiGet(ints1.length);
        multiPut(ints2);
        checkSizes(QUEUE_SIZE, 0);
    }

    @Test public void putGet() {
        int[] ints1 = {-74, 82, -68, -82, -71};
        int[] ints2 = {-60, -78, 91, 48, -37, -90, 7};
        multiPut(ints1);
        multiGetCheck(ints1);
        multiPut(ints2);
        multiGetCheck(ints2);
    }

    @Test public void putGetInterleaved() {
        int[] ints = {
            -91, 66, -47, 52, 81, 47, 29, 97, 68, 35,
            -54, -99, 39, -55, 52, -65, 66, -91, -50, 15,
        };

        queue.put(ints[0]);
        queue.put(ints[1]);
        queue.put(ints[2]);
        checkSizes(3, 7);

        assertEquals(ints[0], queue.get().intValue());
        assertEquals(ints[1], queue.get().intValue());
        checkSizes(1, 9);

        queue.put(ints[3]);
        queue.put(ints[4]);
        queue.put(ints[5]);
        queue.put(ints[6]);
        queue.put(ints[7]);
        queue.put(ints[8]);
        queue.put(ints[9]);
        queue.put(ints[10]);
        queue.put(ints[11]);
        checkSizes(10, 0);

        assertEquals(ints[2], queue.get().intValue());
        checkSizes(9, 1);

        queue.put(ints[12]);
        checkSizes(10, 0);

        assertEquals(ints[3], queue.get().intValue());
        assertEquals(ints[4], queue.get().intValue());
        assertEquals(ints[5], queue.get().intValue());
        assertEquals(ints[6], queue.get().intValue());
        assertEquals(ints[7], queue.get().intValue());
        assertEquals(ints[8], queue.get().intValue());
        assertEquals(ints[9], queue.get().intValue());
        assertEquals(ints[10], queue.get().intValue());
        checkSizes(2, 8);

        queue.put(ints[13]);
        queue.put(ints[14]);
        queue.put(ints[15]);
        checkSizes(5, 5);

        assertEquals(ints[11], queue.get().intValue());
        assertEquals(ints[12], queue.get().intValue());
        assertEquals(ints[13], queue.get().intValue());
        assertEquals(ints[14], queue.get().intValue());
        checkSizes(1, 9);
    }

    @Test public void putFull() {
        int[] ints = {-5, 45, 76, -64, -78, 75, 65, -59, -12, 7};
        multiPut(ints);
        // Do the try/catch manually (as opposed to expecting the
        // exception in the annotation) so we are sure it fails on the
        // extra element
        try {
            queue.put(21);
        } catch (IllegalStateException e) {
        }
    }

    @Test(expected=NoSuchElementException.class)
    public void getEmpty() {
        queue.get();
    }
}
