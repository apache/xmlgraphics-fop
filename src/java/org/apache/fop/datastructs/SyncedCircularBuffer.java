/*
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
 * $Id$
 * Created: Tue Nov  6 10:19:03 2001
 */
package org.apache.fop.datastructs;

import java.util.NoSuchElementException;

/**
 * A general synchronized circular buffer class.
 * It stores and returns <tt>Object</tt>
 * references.  The buffer operations <b><i>are</i></b> synchronized and it
 * behaves as a synchronized producer/consumer buffer.
 * </p><p>
 * <b>Warning</b>: if the producer or consumer thread dies unexpectedly,
 * without interrupting the complementary thread's <tt>wait()</tt>, that
 * process will hang on the <tt>wait()</tt>.
 * 
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
public class SyncedCircularBuffer {

    public final static int DEFAULTBUFSIZE = 64;

    private Object[] buf;
    private int size = 0;
    private int getptr = 0;
    private int putptr = 0;
    private boolean flush = false;
    private boolean producerFinished = false;
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
    synchronized public void pushBack (Object obj) {
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
            if (producerFinished && isEmpty()) {
                throw new NoSuchElementException("Producer is finished.");
            }
            if (pushBackBuf != null) {
                obj = pushBackBuf;
                pushBackBuf = null;
                return obj;
            }

            while (isEmpty()) {
                // wait for the producer
                // N.B. InterruptedException is propagated
                // N.B. Because of synchronisation, producerFinished cannot
                // become true while isEmpty() remains true, so just check for
                // isEmpty().  In other circumstances, the
                // InterruptedException will be propagated
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
     * @param thing the Object to append to the buffer
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
            if (producerFinished) {
                throw new RuntimeException(
                        "SyncedCircularBuffer is finished.");
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
                    // N.B. InterruptedException is propagated
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

    /**
     * Notifies the consumer that the producer has terminated.
     * The <tt>notifyAll()</tt> call allows for processing of the buffer before
     * it fills.
     */
    public void producerExhausted() {
        synchronized (this) {
            if (! isEmpty()) {
                flush = true;
                producerFinished = true;
                notifyAll();
            }
        }
    }
    
    /**
     * Is this source of XmlEvents exhausted?
     * @return true if the last event from the input stream (END_DOCUMENT)
     * has been processed, and the buffer is empty.
     */
    public boolean isExhausted() {
        synchronized (this) {
            return producerFinished && isEmpty(); 
        }
    }

}
