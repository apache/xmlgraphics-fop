/*
 * 
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
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
// $Id$
package org.apache.fop.datastructs;

import java.util.BitSet;


/**
 * Implements a Read-Only <tt>BitSet</tt>. The set is created as a copy of
 * the set provided in the constructor, and no subsequent changes can occur.
 * All read operations from <tt>BitSet</tt> are preserved.
 */
public class ROBitSet extends BitSet {

    /**
     * Initialise with <tt>BitSet</tt>.  This is the only operation that
     * modifies the bit set.
     * @param bitset the unmodifiable <tt>Bitset</tt>
     */
    public ROBitSet(BitSet bitset) {
        super(bitset.length());
        super.or(bitset);
    }

    /**
     * Unsupported operation. Overrides <tt>BitSet</tt> method.
     * @param set <tt>BitSet</tt>
     * @exception UnsupportedOperationException
     */
    public void and(BitSet set) {
        throw new UnsupportedOperationException("and invalid in ROBitSet");
    }

    /**
     * Unsupported operation. Overrides <tt>BitSet</tt> method.
     * @param set <tt>BitSet</tt>
     * @exception UnsupportedOperationException
     */
    public void andNot(BitSet set) {
        throw new UnsupportedOperationException("andNot invalid in ROBitSet");
    }

    /**
     * Unsupported operation. Overrides <tt>BitSet</tt> method.
     * @exception UnsupportedOperationException
     */
    public void clear() {
        throw new UnsupportedOperationException("clear invalid in ROBitSet");
    }

    /**
     * Unsupported operation. Overrides <tt>BitSet</tt> method.
     * @param bitIndex <tt>int</tt>
     * @exception UnsupportedOperationException
     */
    public void clear(int bitIndex) {
        throw new UnsupportedOperationException("clear invalid in ROBitSet");
    }

    /**
     * Unsupported operation. Overrides <tt>BitSet</tt> method.
     * @param fromIndex <tt>int</tt>
     * @param toIndex <tt>int</tt>
     * @exception UnsupportedOperationException
     */
    public void clear(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException("clear invalid in ROBitSet");
    }

    /**
     * Unsupported operation. Overrides <tt>BitSet</tt> method.
     * @param bitIndex <tt>int</tt>
     * @exception UnsupportedOperationException
     */
    public void flip(int bitIndex) {
        throw new UnsupportedOperationException("flip invalid in ROBitSet");
    }

    /**
     * Unsupported operation. Overrides <tt>BitSet</tt> method.
     * @param fromIndex <tt>int</tt>
     * @param toIndex <tt>int</tt>
     * @exception UnsupportedOperationException
     */
    public void flip(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException("flip invalid in ROBitSet");
    }

    /**
     * Unsupported operation. Overrides <tt>BitSet</tt> method.
     * @param set <tt>BitSet</tt>
     * @exception UnsupportedOperationException
     */
    public void or(BitSet set) {
        throw new UnsupportedOperationException("or invalid in ROBitSet");
    }

    /**
     * Unsupported operation. Overrides <tt>BitSet</tt> method.
     * @param bitIndex <tt>int</tt>
     * @exception UnsupportedOperationException
     */
    public void set(int bitIndex) {
        throw new UnsupportedOperationException("set invalid in ROBitSet");
    }

    /**
     * Unsupported operation. Overrides <tt>BitSet</tt> method.
     * @param bitIndex <tt>int</tt>
     * @param value <tt>boolean</tt>
     * @exception UnsupportedOperationException
     */
    public void set(int bitIndex, boolean value) {
        throw new UnsupportedOperationException("set invalid in ROBitSet");
    }

    /**
     * Unsupported operation. Overrides <tt>BitSet</tt> method.
     * @param fromIndex <tt>int</tt>
     * @param toIndex <tt>int</tt>
     * @exception UnsupportedOperationException
     */
    public void set(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException("set invalid in ROBitSet");
    }

    /**
     * Unsupported operation. Overrides <tt>BitSet</tt> method.
     * @param fromIndex <tt>int</tt>
     * @param toIndex <tt>int</tt>
     * @param value <tt>boolean</tt>
     * @exception UnsupportedOperationException
     */
    public void set(int fromIndex, int toIndex, boolean value) {
        throw new UnsupportedOperationException("set invalid in ROBitSet");
    }

    /**
     * Unsupported operation. Overrides <tt>BitSet</tt> method.
     * @param set <tt>BitSet</tt>
     * @exception UnsupportedOperationException
     */
    public void xor(BitSet set) {
        throw new UnsupportedOperationException("xor invalid in ROBitSet");
    }

}
