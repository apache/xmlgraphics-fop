package org.apache.fop.datastructs;

/**
 * Provides a Read-Only String array.
 *
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 *
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
public class ROStringArray {
    
    private String[] sarray;

    /**
     * The length of the array; analogous to the <i>length</i> field of an
     * array.
     */
    public final int length;

    public ROStringArray(String[] sarray) {
        this.sarray = sarray;
        length = sarray.length;
    }

    /**
     * @param index an <tt>int</tt>; the offset of the value to retrieve
     * @return the <tt>String</tt> at the <i>index</i> offset
     */
    public String get(int index) {
        return sarray[index];
    }
}
