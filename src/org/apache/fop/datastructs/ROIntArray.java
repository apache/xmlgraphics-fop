// $Id$
package org.apache.fop.datastructs;

/**
 * Provides a Read-Only <tt>int</tt> array.
 *
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 *
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
public class ROIntArray {
    
    private int[] iarray;

    /**
     * The length of the array; analogous to the <i>length</i> field of an
     * array.
     */
    public final int length;

    /**
     * Initialise with single integer array.  N.B. the ROIntArray is a
     * reference to the initialising array.  Any subsequent changes to the
     * contents of the initialising array will be reflected in the
     * ROIntArray.
     * @param iarray an <tt>int[]</tt>
     */
    public ROIntArray(int[] iarray) {
        this.iarray = iarray;
        length = iarray.length;
    }

    /**
     * Initialise with an array of arrays.  The elements of the argument
     * arrays are copied, in order, into the ROIntArray.
     * @param iarrays an <tt>int[][]</tt>
     */
    public ROIntArray(int[][] iarrays) {
        int i, j, k;
        i = 0;
        for (j = 0; j < iarrays.length; j++)
            for (k = 0; k < iarrays[j].length; k++)
                iarray[i++] = iarrays[j][k]; 
        length = iarray.length;
    }

    /**
     * @param index an <tt>int</tt>; the offset of the value to retrieve
     * @return the <tt>int</tt> at the <i>index</i> offset
     */
    public int get(int index) {
        return iarray[index];
    }
}
