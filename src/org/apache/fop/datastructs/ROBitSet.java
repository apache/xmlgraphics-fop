// $Id$
package org.apache.fop.datastructs;

import java.util.BitSet;

/*
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 *
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */

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
     * @exception <tt>UnsupportedOperationException</tt>
     */
    public void and(BitSet set) {
        throw new UnsupportedOperationException("and invalid in ROBitSet");
    }

    /**
     * Unsupported operation. Overrides <tt>BitSet</tt> method.
     * @param set <tt>BitSet</tt>
     * @exception <tt>UnsupportedOperationException</tt>
     */
    public void andNot(BitSet set) {
        throw new UnsupportedOperationException("andNot invalid in ROBitSet");
    }

    /**
     * Unsupported operation. Overrides <tt>BitSet</tt> method.
     * @exception <tt>UnsupportedOperationException</tt>
     */
    public void clear() {
        throw new UnsupportedOperationException("clear invalid in ROBitSet");
    }

    /**
     * Unsupported operation. Overrides <tt>BitSet</tt> method.
     * @param bitIndex <tt>int</tt>
     * @exception <tt>UnsupportedOperationException</tt>
     */
    public void clear(int bitIndex) {
        throw new UnsupportedOperationException("clear invalid in ROBitSet");
    }

    /**
     * Unsupported operation. Overrides <tt>BitSet</tt> method.
     * @param fromIndex <tt>int</tt>
     * @param toIndex <tt>int</tt>
     * @exception <tt>UnsupportedOperationException</tt>
     */
    public void clear(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException("clear invalid in ROBitSet");
    }

    /**
     * Unsupported operation. Overrides <tt>BitSet</tt> method.
     * @param bitIndex <tt>int</tt>
     * @exception <tt>UnsupportedOperationException</tt>
     */
    public void flip(int bitIndex) {
        throw new UnsupportedOperationException("flip invalid in ROBitSet");
    }

    /**
     * Unsupported operation. Overrides <tt>BitSet</tt> method.
     * @param fromIndex <tt>int</tt>
     * @param toIndex <tt>int</tt>
     * @exception <tt>UnsupportedOperationException</tt>
     */
    public void flip(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException("flip invalid in ROBitSet");
    }

    /**
     * Unsupported operation. Overrides <tt>BitSet</tt> method.
     * @param set <tt>BitSet</tt>
     * @exception <tt>UnsupportedOperationException</tt>
     */
    public void or(BitSet set) {
        throw new UnsupportedOperationException("or invalid in ROBitSet");
    }

    /**
     * Unsupported operation. Overrides <tt>BitSet</tt> method.
     * @param bitIndex <tt>int</tt>
     * @exception <tt>UnsupportedOperationException</tt>
     */
    public void set(int bitIndex) {
        throw new UnsupportedOperationException("set invalid in ROBitSet");
    }

    /**
     * Unsupported operation. Overrides <tt>BitSet</tt> method.
     * @param bitIndex <tt>int</tt>
     * @param value <tt>boolean</tt>
     * @exception <tt>UnsupportedOperationException</tt>
     */
    public void set(int bitIndex, boolean value) {
        throw new UnsupportedOperationException("set invalid in ROBitSet");
    }

    /**
     * Unsupported operation. Overrides <tt>BitSet</tt> method.
     * @param fromIndex <tt>int</tt>
     * @param toIndex <tt>int</tt>
     * @exception <tt>UnsupportedOperationException</tt>
     */
    public void set(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException("set invalid in ROBitSet");
    }

    /**
     * Unsupported operation. Overrides <tt>BitSet</tt> method.
     * @param fromIndex <tt>int</tt>
     * @param toIndex <tt>int</tt>
     * @param value <tt>boolean</tt>
     * @exception <tt>UnsupportedOperationException</tt>
     */
    public void set(int fromIndex, int toIndex, boolean value) {
        throw new UnsupportedOperationException("set invalid in ROBitSet");
    }

    /**
     * Unsupported operation. Overrides <tt>BitSet</tt> method.
     * @param set <tt>BitSet</tt>
     * @exception <tt>UnsupportedOperationException</tt>
     */
    public void xor(BitSet set) {
        throw new UnsupportedOperationException("xor invalid in ROBitSet");
    }

}
