/*
 * $Id$
 * 
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
 * @version $Rev$ $Name$
 */

package org.apache.fop.fo;

import java.util.BitSet;

import org.apache.fop.datastructs.ROBitSet;

/**
 * Data class containing sets of Flow Objects included in certain FO set
 * specifications.
 */

public class FObjectSets {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    /**  The set of FOs comprising the block entity.
     *    See 6.14 Formatting Object Content.
     *   Nullified when initialization complete. */
    private static BitSet block;
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
     *    See 6.14 Formatting Object Content.
     *  Nullified when initialization complete. */
    private static BitSet inline;

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
        // Moved FOOTNOTE here because it may occur in static-content
        inline.set(FObjectNames.FOOTNOTE);
        inlineEntity = new ROBitSet(inline);
    }

    /** The set of FOs available wherever %block; is allowed
     * including within descendents of out-of-line FOs.
     *    See 6.14 Formatting Object Content.
     *  Nullified when initialization complete. */
    private static BitSet outOfLineBlock;

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
     *    See 6.14 Formatting Object Content.
     *  Nullified when initialization complete. */
    private static BitSet outOfLinePcdataBlockInline;

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
     *    See 6.14 Formatting Object Content.
     *  Nullified when initialization complete. */
    private static BitSet normalPcdataBlockInline;

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
     *    See 6.14 Formatting Object Content.
     *  Nullified when initialization complete. */
    private static BitSet normalPcdataInline;

    /** The publicly accessible set of FOs available wherever
     * #PCDATA|%inline; is allowed except within descendents of
     * out-of-line FOs.  See 6.14 Formatting Object Content. */
    public static final ROBitSet normalPcdataInlineSet;
    static {
        normalPcdataInline = new BitSet();
        normalPcdataInline.or(inline);
        normalPcdataInline.set(FObjectNames.FLOAT);
        // Removed FOOTNOTE because it may occur in static-content
        //normalPcdataInline.set(FObjectNames.FOOTNOTE);
        normalPcdataInlineSet = new ROBitSet(normalPcdataInline);
    }
    
    static {
        block = null;
        inline = null;
        outOfLineBlock = null;
        outOfLinePcdataBlockInline = null;
        normalPcdataBlockInline = null;
        normalPcdataInline = null;
    }

    private FObjectSets() {}

}
