package org.apache.fop.datastructs;

import java.lang.Class;

/**
 * Provides a Read-Only array of <tt>Class</tt>.
 * $Id$
 *
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 *
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 *
 */
public class ROClassArray {
    
    private Class[] carray;

    /**
     * The length of the array; analogous to the <i>length</i> field of an
     * array.
     */
    public final int length;

    public ROClassArray(Class[] carray) {
        this.carray = carray;
        length = carray.length;
    }

    /**
     * @param index an <tt>int</tt>; the offset of the value to retrieve
     * @return the <tt>Class</tt> at the <i>index</i> offset
     */
    public Class get(int index) {
        return carray[index];
    }
}
