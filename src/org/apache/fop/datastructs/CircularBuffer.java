package org.apache.fop.datastructs;

import java.util.NoSuchElementException;
import java.lang.IndexOutOfBoundsException;

/*
 * CircularBuffer.java
 * $Id$
 * Created: Tue Nov  6 10:19:03 2001
 * 
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 *
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
/**
 * A general circular buffer class.  It stores and returns <tt>Object</tt>
 * references.  The buffer operations are <b><i>not</i></b> synchronized.
 */

public class CircularBuffer {

    private final static int DEFAULTBUFSIZE = 32;

    private Object[] buf;
    private int size = 0;
    private int getptr = 0;
    private int putptr = 0;

    /**
     * Constructor taking a single argument; the size of the buffer.
     */
    public CircularBuffer(int size) {
        buf = new Object[size];
        this.size = size;
    }

    /**
     * No-argument constructor sets up a buffer with the default number of
     * elements.
     */
    public CircularBuffer()
        throws IllegalArgumentException {
        this(DEFAULTBUFSIZE);
    }

    public boolean isFull() {
        return ((putptr + 1) % size) == getptr;
    }

    public boolean isEmpty() {
        return putptr == getptr;
    }

    /**
     * <tt>get</tt> returns the next object from the buffer.
     * Throws a NoSuchElementException if the buffer is empty.
     */
    public Object get()
        throws NoSuchElementException {
        if (isEmpty()) {
            throw new NoSuchElementException(
                    "CircularBuffer is empty.");
        }
        int tmpptr = getptr++;
        if (getptr == size) getptr = 0;
        return buf[tmpptr];
    }

    /**
     * <tt>put</tt> adds an object to the buffer.
     * Throws a NoSuchElementException if the buffer is full.
     */
    public void put(Object thing) throws NoSuchElementException {
        if (isFull()) {
            throw new NoSuchElementException(
                    "CircularBuffer is full.");
        }
        buf[putptr++] = thing;
        if (putptr == size) putptr = 0;
    }

    /**
     * <tt>oldest</tt> returns the next object that will be overwritten
     * by a put.  If the buffer is full, returns null.
     */
    public Object oldest() {
        if (isFull()) {
            return null;
        }
        return buf[putptr];
    }

}
