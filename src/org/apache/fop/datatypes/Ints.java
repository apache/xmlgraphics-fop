/*
 * Ints.java
 * $Id$
 *
 * Created: Sun Nov  4 13:24:25 2001
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */

package org.apache.fop.datatypes;

import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.FObjectNames;
import org.apache.fop.datastructs.ROIntegerArray;

/**
 * Data class of pre-initialised Integer objects.
 */

public class Ints {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";
    /**
     * An <tt>ROIntegerArray</tt> of Integer object constants corresponding
     * to the set of property index values.
     * @see org.apache.fop.fo.PropNames
     */
    public static final ROIntegerArray consts;
    /**
     * An <tt>ROIntegerArray</tt> of Integer object constants corresponding
     * to the set of flow object index values.
     * @see org.apache.fop.fo.FObjectNames
     */
    public static final ROIntegerArray foconsts;
    private static final Integer[] constsAr;
    private static final Integer[] foconstsAr;
    static {
        int range = PropNames.LAST_PROPERTY_INDEX >= FObjectNames.LAST_FO ?
                PropNames.LAST_PROPERTY_INDEX : FObjectNames.LAST_FO;
        constsAr = new Integer[PropNames.LAST_PROPERTY_INDEX + 1];
        for (int i = 0; i <= PropNames.LAST_PROPERTY_INDEX; i++) {
            constsAr[i] = new Integer(i);
        }
        consts = new ROIntegerArray(constsAr);
        foconstsAr = new Integer[FObjectNames.LAST_FO + 1];
        if (PropNames.LAST_PROPERTY_INDEX >= FObjectNames.LAST_FO) {
            System.arraycopy(constsAr, 0, foconstsAr, 0,
                             FObjectNames.LAST_FO + 1);
        }
        else {
            System.arraycopy(constsAr, 0, foconstsAr, 0,
                             PropNames.LAST_PROPERTY_INDEX + 1);
            for (int i = PropNames.LAST_PROPERTY_INDEX + 1;
                 i <= FObjectNames.LAST_FO; i++) {
                foconstsAr[i] = new Integer(i);
            }
        }
        foconsts = new ROIntegerArray(foconstsAr);
    }
    
    private Ints (){}
}
