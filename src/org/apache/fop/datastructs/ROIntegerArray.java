// $Id$
package org.apache.fop.datastructs;

import java.util.List;
import java.util.Collections;
import java.util.Arrays;

/**
 * Provides a Read-Only <tt>Integer</tt> array.
 *
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 *
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
public class ROIntegerArray {
    
    private Integer[] iarray;

    /**
     * The length of the array; analogous to the <i>length</i> field of an
     * array.
     */
    public final int length;

    public ROIntegerArray(Integer[] iarray) {
        this.iarray = iarray;
        length = iarray.length;
    }

    /**
     * @param index an <tt>int</tt>; the offset of the value to retrieve
     * @return the <tt>Integer</tt> at the <i>index</i> offset
     */
    public Integer get(int index) {
        return iarray[index];
    }

    public List immutableList () {
        return Collections.unmodifiableList(Arrays.asList(iarray));
    }
}
