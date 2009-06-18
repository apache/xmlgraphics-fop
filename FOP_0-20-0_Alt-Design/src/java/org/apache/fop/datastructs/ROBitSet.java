/*
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
