/*
 * ============================================================================
 *                   The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of  source code must  retain the above copyright  notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include  the following  acknowledgment:  "This product includes  software
 *    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
 *    Alternately, this  acknowledgment may  appear in the software itself,  if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and  "Apache Software Foundation"  must not be used to
 *    endorse  or promote  products derived  from this  software without  prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products  derived from this software may not  be called "Apache", nor may
 *    "Apache" appear  in their name,  without prior written permission  of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 * APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 * ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 * (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * This software  consists of voluntary contributions made  by many individuals
 * on  behalf of the Apache Software  Foundation and was  originally created by
 * James Tauber <jtauber@jtauber.com>. For more  information on the Apache 
 * Software Foundation, please see <http://www.apache.org/>.
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
