package org.apache.fop.datastructs;

import java.util.NoSuchElementException;
import java.lang.IndexOutOfBoundsException;

/*
 * SyncedCircularBuffer.java
 * $Id$
 * 
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
 * A general synchronized circular buffer class.
 * It stores and returns <tt>Object</tt>
 * references.  The buffer operations <b><i>are</i></b> synchronized and it
 * behaves as a synchronized producer/consumer buffer.
 * </p><p>
 * <b>Warning</b>: if the producer or consumer thread dies unexpectedly,
 * without interrupting the complementary thread's <tt>wait()</tt>, that
 * process will hang on the <tt>wait()</tt>.
 */
public class SyncedCircularBuffer {

    private final static int DEFAULTBUFSIZE = 128;

    private Object[] buf;
    private int size = 0;
    private int getptr = 0;
    private int putptr = 0;
    private boolean flush = false;
    private Object pushBackBuf = null;

    /**
     * No-argument constructor sets up a buffer with the default number of
     * elements.
     * The producer and consumer <tt>Thread</tt>s default to the current
     * thread at the time of instantiation.
     */
    public SyncedCircularBuffer() throws IllegalArgumentException {
        this(DEFAULTBUFSIZE);
    }

    /**
     * Constructor taking a single argument; the size of the buffer.
     * @param size the size of the buffer.  Must be > 1.
     */
    public SyncedCircularBuffer(int size) throws IllegalArgumentException {
        if (size < 1) throw new IllegalArgumentException
                              ("SyncedCircularBuffer size less than 1.");
        buf = new Object[size];
        this.size = size;
    }

    /**
     * @return true if the buffer is full; i.e. if incrementing the
     * <tt>put</tt> pointer would result in a spurious
     * <tt>isEmpty()</tt> condition.
     */
    public boolean isFull() {
        return ((putptr + 1) % size) == getptr;
    }

    /**
     * @return true if the buffer is empty; i.e. if the <tt>put</tt>
     * pointer is equal to the <tt>get</tt> pointer.
     */
    public boolean isEmpty() {
        return putptr == getptr;
    }

    /**
     * Push back an object into the buffer; generally this will be an
     * object previously obtaiined by a <tt>get</tt> from the buffer.
     * <p>This implementation supports a single entry pushback buffer.</p>
     * @param obj and <tt>Object</tt>
     */
    synchronized public void pushBack (Object obj)
        throws IndexOutOfBoundsException {
        if (pushBackBuf != null)
            throw new IndexOutOfBoundsException("pushBack buffer is full");
        pushBackBuf = obj;
    }

    /**
     * <tt>get</tt> returns the next object from the buffer.
     * @exception NoSuchElementException if the buffer is empty.
     * @exception InterruptedException if the wait() is interrupted.
     */
    public Object get() throws NoSuchElementException, InterruptedException {
        // Assume that an InterruptedException is a message to kill the
        // consumer, sent if the producer terminates.  Just die.
        synchronized (this) {
            Object obj;
            if (Thread.interrupted()) {
                throw new InterruptedException("Consumer interrupted");
            }
            if (pushBackBuf != null) {
                obj = pushBackBuf;
                pushBackBuf = null;
                return obj;
            }

            while (isEmpty()) {
                // wait for the producer
                this.wait();
            }
            
            int tmpptr = getptr++;
            if (getptr == size) getptr = 0;
            obj = buf[tmpptr];
            buf[tmpptr] = null;
            if (isEmpty()) notifyAll();
            return obj;
        }
    }

    /**
     * <tt>put</tt> adds an object to the buffer.
     * If the buffer fills after this <tt>put</tt>, notifyAll().
     * Then while the <tt>consumer</tt> thread is still alive and the
     * buffer has not emptied, <tt>wait()</tt> for the consumer.
     *
     * @param the Object to append to the buffer
     * @exception NoSuchElementException if the buffer is full.
     * @exception InterruptedException if the wait() is interrupted.
     */
    public void put(Object thing)
        throws NoSuchElementException, InterruptedException
    {
        // Assume that an InterruptedException is a message to kill the
        // producer, sent if the consumer terminates.  Just die.
        synchronized (this) {
            if (isFull()) {
                throw new NoSuchElementException(
                        "SyncedCircularBuffer is full.");
            }
            if (Thread.interrupted()) {
                throw new InterruptedException("Producer interrupted");
            }
            
            buf[putptr++] = thing;
            if (putptr == size) putptr = 0;
            // If the buffer is full
            // notify the consumer that something is available
            if (isFull() || flush) {
                notifyAll();
                while (! isEmpty()) {
                    // Wait for the consumer
                    this.wait();
                }
                flush = false;
            }
        }
    }

    /**
     * Notifies the consumer.  Allows for processing of the buffer before
     * it fills.
     */
    public void flushBuffer() {
        synchronized (this) {
            if (! isEmpty()) {
                flush = true;
                notifyAll();
            }
        }
    }

}
