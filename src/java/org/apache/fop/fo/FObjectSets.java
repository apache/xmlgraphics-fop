/*
 * $Id$
 * 
 * ============================================================================
 *                   The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of  source code must  retain the above copyright  notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include  the following  acknowledgment:  "This product includes  software
 *    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
 *    Alternately, this  acknowledgment may  appear in the software itself,  if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and  "Apache Software Foundation"  must not be used to
 *    endorse  or promote  products derived  from this  software without  prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products  derived from this software may not  be called "Apache", nor may
 *    "Apache" appear  in their name,  without prior written permission  of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 * APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 * ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 * (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * This software  consists of voluntary contributions made  by many individuals
 * on  behalf of the Apache Software  Foundation and was  originally created by
 * James Tauber <jtauber@jtauber.com>. For more  information on the Apache 
 * Software Foundation, please see <http://www.apache.org/>.
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
        // Moved FOOTNOTE here because it may occur in static-content
        inline.set(FObjectNames.FOOTNOTE);
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
        // Removed FOOTNOTE because it may occur in static-content
        //normalPcdataInline.set(FObjectNames.FOOTNOTE);
        normalPcdataInlineSet = new ROBitSet(normalPcdataInline);
    }

    private FObjectSets() {}

}
