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
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
// $Id$
package org.apache.fop.datastructs;

/**
 * Provides a Read-Only <tt>int</tt> array.
 * 
 * @author pbw
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
                i++;
        length = i;
        iarray = new int[length];
        i = 0;
        for (j = 0; j < iarrays.length; j++)
            for (k = 0; k < iarrays[j].length; k++)
                iarray[i++] = iarrays[j][k];
    }

    /**
     * @param index an <tt>int</tt>; the offset of the value to retrieve
     * @return the <tt>int</tt> at the <i>index</i> offset
     */
    public int get(int index) {
        return iarray[index];
    }
    
    /**
     * @return a copy of the array
     */
    public int[] copyarray() {
        int[] tmp = null;
        System.arraycopy(iarray, 0, tmp, 0, iarray.length);
        return tmp;
    }
}
