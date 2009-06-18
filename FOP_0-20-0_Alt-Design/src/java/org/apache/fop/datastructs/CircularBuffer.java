/*
 * CircularBuffer.java
 * $Id$
 * Created: Tue Nov  6 10:19:03 2001
 * 
 * 
 *
 * Copyright 1999-2003 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *  
 *
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
package org.apache.fop.datastructs;

import java.util.NoSuchElementException;

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
