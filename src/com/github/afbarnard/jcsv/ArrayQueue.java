/*
 * Copyright (c) 2015 Aubrey Barnard.  This is free software.  See
 * LICENSE for details.
 */

package com.github.afbarnard.jcsv;

import java.util.NoSuchElementException;

/**
 * A fast, lightweight, fixed-capacity queue implemented as a circular
 * buffer.  It is similar in functionality to {@link
 * java.util.concurrent.ArrayBlockingQueue} but is just intended for
 * basic use, not concurrent use.
 */
public class ArrayQueue<E> {
    /*
     * In order to distinguish between an empty queue and a full queue
     * without extra machinery, there will always be at least one unused
     * array element between tailIndex and headIndex.  This means that
     * the queue is empty if headIndex == tailIndex.  headIndex can
     * catch up to tailIndex, but tailIndex cannot catch up to
     * headIndex.
     */

    /** Storage for elements of the queue. */
    private Object[] queue;

    /** Index of the head element. */
    private int headIndex = 0;

    /**
     * Index of where to put the next tail element.  This means the
     * current tail element is actually in the previous array location.
     */
    private int tailIndex = 0;

    public ArrayQueue(int capacity) {
        queue = new Object[capacity + 1];
    }

    public ArrayQueue() {
        this(100);
    }

    public int size() {
        return (tailIndex + queue.length - headIndex) % queue.length;
    }

    public int capacity() {
        return queue.length - 1;
    }

    public boolean put(E element) {
        int nextTailIndex = (tailIndex + 1) % queue.length;
        // Check if the queue is full
        if (nextTailIndex == headIndex)
            return false;
        queue[tailIndex] = element;
        tailIndex = nextTailIndex;
        return true;
    }

    @SuppressWarnings("unchecked")
    public E get() {
        if (tailIndex == headIndex)
            throw new NoSuchElementException();
        E element = (E) queue[headIndex];
        headIndex = (headIndex + 1) % queue.length;
        return element;
    }
}
