/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 *
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Id$
 */

package org.apache.fop.fo;

import java.lang.Character;

// Only for tree property set partitions
import java.util.BitSet;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FObjectNames;
import org.apache.fop.fo.PropertySets;
import org.apache.fop.fo.PropNames;
import org.apache.fop.datastructs.ROBitSet;

/**
 * Dummy data class relating sets of properties to Flow Objects.
 * These sets of properties are being migrated into the individual FOs as they are
 * created.
 */

public class FOPropSets {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    public static final String XSLNamespace =
                                        "http://www.w3.org/1999/XSL/Format";

    public static final String packageNamePrefix = "org.apache.fop";


    /**
     * A array of <tt>ROBitSet</tt>s indexed by the integer <i>FO</i>
     * element constants.
     * Each <tt>ROBitSet</tt> contains the set of <i>properties</i> that apply
     * to the corresponding formatting object..  This array, and each
     * <tt>ROBitSet</tt> within it, is intialized in a static initializer.
     */
    private static final ROBitSet[] foPropertyLists;

    /**
     * A Bitmap representing all of the Properties for use in building
     * the partition sets of the properties.
     */

    static {
        foPropertyLists = new ROBitSet[FObjectNames.LAST_FO + 1];
        
        //basic-link
        
        //bidi-override

        //block

        //block-container

        //character

        //color-profile

        //conditional-page-master-reference

        //declarations

        //external-graphic

        //float

        //flow

        //footnote

        //footnote-body

        //initial-property-set

        //inline

        //inline-container

        //instream-foreign-object

        //layout-master-set

        //leader

        //list-block

        //list-item

        //list-item-body

        //list-item-label

        //marker

        //multi-case

        //multi-properties

        //multi-property-set

        //multi-switch

        //multi-toggle

        //page-number

        //page-number-citation

        //page-sequence

        //page-sequence-master

        //region-after

        //region-before

        //region-body

        //region-end

        //region-start

        //repeatable-page-master-alternatives

        //repeatable-page-master-reference

        //retrieve-marker

        //root

        //simple-page-master

        //single-page-master-reference

        //static-content

        //table

        //table-and-caption

        //table-body

        //table-caption

        //table-cell

        //table-column

        //table-footer

        //table-header

        //table-row

        //title

        //wrapper

    }

}

