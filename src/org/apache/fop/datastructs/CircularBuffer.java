package org.apache.fop.datastructs;

import java.util.NoSuchElementException;
import java.lang.IndexOutOfBoundsException;

/*
 * CircularBuffer.java
 * $Id$
 * Created: Tue Nov  6 10:19:03 2001
 * 
 * 
 *  ============================================================================
 *                    The Apache Software License, Version 1.1
 *  ============================================================================
 *  
 *  Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 *  
 *  Redistribution and use in source and binary forms, with or without modifica-
 *  tion, are permitted provided that the following conditions are met:
 *  
 *  1. Redistributions of  source code must  retain the above copyright  notice,
 *     this list of conditions and the following disclaimer.
 *  
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *  
 *  3. The end-user documentation included with the redistribution, if any, must
 *     include  the following  acknowledgment:  "This product includes  software
 *     developed  by the  Apache Software Foundation  (http://www.apache.org/)."
 *     Alternately, this  acknowledgment may  appear in the software itself,  if
 *     and wherever such third-party acknowledgments normally appear.
 *  
 *  4. The names "FOP" and  "Apache Software Foundation"  must not be used to
 *     endorse  or promote  products derived  from this  software without  prior
 *     written permission. For written permission, please contact
 *     apache@apache.org.
 *  
 *  5. Products  derived from this software may not  be called "Apache", nor may
 *     "Apache" appear  in their name,  without prior written permission  of the
 *     Apache Software Foundation.
 *  
 *  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 *  INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 *  APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 *  INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 *  DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 *  OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 *  ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 *  (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 *  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 *  This software  consists of voluntary contributions made  by many individuals
 *  on  behalf of the Apache Software  Foundation and was  originally created by
 *  James Tauber <jtauber@jtauber.com>. For more  information on the Apache 
 *  Software Foundation, please see <http://www.apache.org/>.
 *  
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
