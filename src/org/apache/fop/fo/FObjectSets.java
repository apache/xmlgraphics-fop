/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 *
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Rev$ $Name$
 */

package org.apache.fop.fo;

import org.apache.fop.fo.FObjectNames;
import org.apache.fop.datastructs.ROBitSet;

import java.util.BitSet;

/**
 * Data class containing sets of Flow Objects included in certain FO set
 * specifications.
 */

public class FObjectSets {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    /**  The set of FOs comprising the block entity.
     *    See 6.14 Formatting Object Content. */
    private static final BitSet block;
    /** The publicly accessible block entity set.
     *    See 6.14 Formatting Object Content. */
    public static final ROBitSet blockEntity;
    static {
        block = new BitSet();
        block.set(FObjectNames.TABLE_AND_CAPTION);
        block.set(FObjectNames.TABLE);
        block.set(FObjectNames.BLOCK);
        block.set(FObjectNames.BLOCK_CONTAINER);
        block.set(FObjectNames.LIST_BLOCK);
        blockEntity = new ROBitSet(block);
    }

    /** The set of FOs comprising the inline entity.
     *    See 6.14 Formatting Object Content. */
    private static final BitSet inline;

    /** The publicly accessible inline entity set.
     *    See 6.14 Formatting Object Content. */
    public static final ROBitSet inlineEntity;
    static {
        inline = new BitSet();
        inline.set(FObjectNames.PAGE_NUMBER_CITATION);
        inline.set(FObjectNames.PAGE_NUMBER);
        inline.set(FObjectNames.BIDI_OVERRIDE);
        inline.set(FObjectNames.CHARACTER);
        inline.set(FObjectNames.EXTERNAL_GRAPHIC);
        inline.set(FObjectNames.INSTREAM_FOREIGN_OBJECT);
        inline.set(FObjectNames.INLINE);
        inline.set(FObjectNames.INLINE_CONTAINER);
        inline.set(FObjectNames.LEADER);
        inline.set(FObjectNames.BASIC_LINK);
        inline.set(FObjectNames.MULTI_TOGGLE);
        inlineEntity = new ROBitSet(inline);
    }

    /** The set of FOs available wherever %block; is allowed
     * including within descendents of out-of-line FOs.
     *    See 6.14 Formatting Object Content. */
    private static final BitSet outOfLineBlock;

    /** The publicly accessible set of FOs available wherever
     * %block; is allowed, including within descendents from
     * out-of-line FOs.
     *    See 6.14 Formatting Object Content. */
    public static final ROBitSet outOfLineBlockSet;
    static {
        outOfLineBlock = (BitSet)(block.clone());
        outOfLineBlock.set(FObjectNames.FLOAT);
        outOfLineBlockSet = new ROBitSet(outOfLineBlock);
    }

    /** The set of FOs available wherever #PCDATA|%block;|%inline; is allowed
     * including within descendents of out-of-line FOs.
     *    See 6.14 Formatting Object Content. */
    private static final BitSet outOfLinePcdataBlockInline;

    /** The publicly accessible set of FOs available wherever
     * #PCDATA|%block;|%inline; is allowed, including within descendents from
     * out-of-line FOs.
     *    See 6.14 Formatting Object Content. */
    public static final ROBitSet outOfLinePcdataBlockInlineSet;
    static {
        outOfLinePcdataBlockInline = new BitSet();
        outOfLinePcdataBlockInline.set(FObjectNames.WRAPPER);
        outOfLinePcdataBlockInline.set(FObjectNames.RETRIEVE_MARKER);
        outOfLinePcdataBlockInline.set(FObjectNames.MULTI_SWITCH);
        outOfLinePcdataBlockInline.set(FObjectNames.MULTI_PROPERTIES);
        outOfLinePcdataBlockInline.or(block);
        outOfLinePcdataBlockInline.or(inline);
        outOfLinePcdataBlockInlineSet =
                                    new ROBitSet(outOfLinePcdataBlockInline);
    }

    /**
     * The set of FOs available wherever #PCDATA|%block;|%inline; is allowed
     * except within descendents of out-of-line FOs.
     *     See 6.14 Formatting Object Content. */
    private static final BitSet normalPcdataBlockInline;

    /** The publicly accessible set of FOs available wherever
     * #PCDATA|%block;|%inline; is allowed except within descendents of
     * out-of-line FOs.  See 6.14 Formatting Object Content. */
    public static final ROBitSet normalPcdataBlockInlineSet;
    static {
        normalPcdataBlockInline = new BitSet();
        normalPcdataBlockInline.or(outOfLinePcdataBlockInline);
        normalPcdataBlockInline.set(FObjectNames.FLOAT);
        normalPcdataBlockInlineSet =
                                    new ROBitSet(normalPcdataBlockInline);
    }

    /**
     * The set of FOs available wherever #PCDATA|%inline; is allowed
     * except within descendents of out-of-line FOs.
     *     See 6.14 Formatting Object Content. */
    private static final BitSet normalPcdataInline;

    /** The publicly accessible set of FOs available wherever
     * #PCDATA|%inline; is allowed except within descendents of
     * out-of-line FOs.  See 6.14 Formatting Object Content. */
    public static final ROBitSet normalPcdataInlineSet;
    static {
        normalPcdataInline = new BitSet();
        normalPcdataInline.or(inline);
        normalPcdataInline.set(FObjectNames.FLOAT);
        normalPcdataInline.set(FObjectNames.FOOTNOTE);
        normalPcdataInlineSet = new ROBitSet(normalPcdataInline);
    }

    private FObjectSets() {}

}
