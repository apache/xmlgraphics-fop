package org.apache.fop.datastructs;

import java.lang.reflect.Method;

/**
 * Provides a Read-Only array of <tt>Method</tt>.
 * $Id$
 *
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 *
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
public class ROMethodArray {
    
    private Method[] marray;

    /**
     * The length of the array; analogous to the <i>length</i> field of an
     * array.
     */
    public final int length;

    public ROMethodArray(Method[] marray) {
        this.marray = marray;
        length = marray.length;
    }

    /**
     * @param index an <tt>int</tt>; the offset of the value to retrieve
     * @return the <tt>Method</tt> at the <i>index</i> offset
     */
    public Method get(int index) {
        return marray[index];
    }
}
