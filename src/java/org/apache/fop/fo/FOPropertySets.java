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
 * $Id$
 */

package org.apache.fop.fo;

import java.util.BitSet;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.Fop;
import org.apache.fop.datastructs.ROBitSet;

/**
 * Data class relating sets of properties to Flow Objects.
 * 
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Id$
 */

public class FOPropertySets {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    public static final String packageNamePrefix = Fop.fopPackage;

    public static String getAttrSetName(int ancestry) throws FOPException {
        if ((ancestry & FONode.MC_MARKER) != 0)
            return "MARKER";
        if ((ancestry & FONode.FLOW) != 0)
            return "FLOW";
        if ((ancestry & FONode.STATIC) != 0)
            return "STATIC";
        if ((ancestry & FONode.TITLE) != 0)
            return "TITLE";
        if ((ancestry & FONode.PAGESEQ) != 0)
            return "PAGESEQ";
        if ((ancestry & FONode.SEQ_MASTER) != 0)
            return "SEQ_MASTER";
        if ((ancestry & FONode.LAYOUT) != 0)
            return "LAYOUT_MASTER";
        if ((ancestry & FONode.DECLARATIONS) != 0)
            return "DECLARATIONS";
        if ((ancestry & FONode.ROOT) != 0)
            return "ROOT";
        throw new FOPException("Invalid attribute set: " + ancestry);
    }

    public static ROBitSet getAttrROBitSet(int ancestry)
            throws FOPException
    {
        if ((ancestry & FONode.MC_MARKER) != 0)
            return markerAllSet;
        if ((ancestry & FONode.FLOW) != 0)
            return flowAllSet;
        if ((ancestry & FONode.STATIC) != 0)
            return staticAllSet;
        if ((ancestry & FONode.TITLE) != 0)
            return titleAllSet;
        if ((ancestry & FONode.PAGESEQ) != 0)
            return pageSeqSet;
        if ((ancestry & FONode.SEQ_MASTER) != 0)
            return seqMasterSet;
        if ((ancestry & FONode.LAYOUT) != 0)
            return layoutMasterSet;
        if ((ancestry & FONode.DECLARATIONS) != 0)
            return declarationsAll;
        if ((ancestry & FONode.ROOT) != 0)
            return allProps;
        throw new FOPException("Invalid attribute set: " + ancestry);
    }

    /**
     * Set of all properties available at fo:root.
     */
    public static final ROBitSet allProps;

    /**
     * set of all properties which are
     * usable within the declarations subtree.
     */
    public static final ROBitSet declarationsAll;

    /**
     * set of all properties which are
     * usable within the page-sequence-master-set subtree.
     */
    public static final ROBitSet seqMasterSet;

    /**
     * set of all properties which are
     * usable within the layout-master-set subtree.
     */
    public static final ROBitSet layoutMasterSet;

    /**
     * set of all properties which are
     * usable within the page sequence subtree.
     */
    public static final ROBitSet pageSeqSet;

    /**
     * set of all properties which are
     * usable within the fo:flow subtree.
     */
    public static final ROBitSet flowAllSet;

    /**
     * set of all properties which are
     * usable <i>within</i> the fo:marker subtree.
     */
    public static final ROBitSet markerAllSet;

    /**
     * set of all properties which are
     * usable within the fo:static-content subtree.
     */
    public static final ROBitSet staticAllSet;

    /**
     * set of all properties which are
     * usable within the fo:title subtree.
     */
    public static final ROBitSet titleAllSet;

    static {

        // fill the BitSet of all properties
        BitSet allprops = new BitSet(PropNames.LAST_PROPERTY_INDEX + 1);
        allprops.set(1, PropNames.LAST_PROPERTY_INDEX);

        allProps = new ROBitSet(allprops);

        //root only set of properties - properties for exclusive use on the
        // root element
        BitSet rootonly = new BitSet(PropNames.MEDIA_USAGE + 1);
        rootonly.set(PropNames.MEDIA_USAGE);

        //declarations only set of properties - properties for exclusive use
        // in the declarations SUBTREE
        BitSet declarationsonly = new BitSet();
        declarationsonly.set(PropNames.COLOR_PROFILE_NAME);
        declarationsonly.set(PropNames.RENDERING_INTENT);

        // set of all declarations properties - properties which may be
        // used in the declarations SUBTREE
        BitSet declarationsall = (BitSet)declarationsonly.clone();
        declarationsall.set(PropNames.SRC);

        declarationsAll = new ROBitSet(declarationsall);

        // seq-master-only set of properties for exclusive use within
        // the page-sequence-master subtree
        BitSet seqmasteronly = new BitSet();
        seqmasteronly.set(PropNames.MAXIMUM_REPEATS);
        seqmasteronly.set(PropNames.PAGE_POSITION);
        seqmasteronly.set(PropNames.ODD_OR_EVEN);
        seqmasteronly.set(PropNames.BLANK_OR_NOT_BLANK);

        // seq-master-set set of properties for use within
        // the page-sequence-master subtree
        BitSet seqmasterset = (BitSet)seqmasteronly.clone();
        seqmasterset.set(PropNames.MASTER_NAME);
        seqmasterset.set(PropNames.MASTER_REFERENCE);

        seqMasterSet = new ROBitSet(seqmasterset);

        //layout-master-set only set of properties - properties for exclusive
        // use within the layout-master-set SUBTREE
        BitSet layoutmasteronly = (BitSet)seqmasteronly.clone();
        layoutmasteronly.set(PropNames.MASTER_NAME);
        layoutmasteronly.set(PropNames.PAGE_HEIGHT);
        layoutmasteronly.set(PropNames.PAGE_WIDTH);
        layoutmasteronly.set(PropNames.COLUMN_COUNT);
        layoutmasteronly.set(PropNames.COLUMN_GAP);
        layoutmasteronly.set(PropNames.REGION_NAME);
        layoutmasteronly.set(PropNames.EXTENT);
        layoutmasteronly.set(PropNames.PRECEDENCE);

        // set of all layout-master-set properties - properties which may be
        // used in the layout-master-set SUBTREE
        // Add the layout-master-set exclusive properties
        BitSet layoutmasterset = (BitSet)layoutmasteronly.clone();

        layoutmasterset.set(PropNames.MASTER_REFERENCE);
        layoutmasterset.set(PropNames.REFERENCE_ORIENTATION);
        layoutmasterset.set(PropNames.WRITING_MODE);
        layoutmasterset.set(PropNames.CLIP);
        layoutmasterset.set(PropNames.DISPLAY_ALIGN);
        layoutmasterset.set(PropNames.OVERFLOW);

        // Add the common margin properties - block
	layoutmasterset.or(PropertySets.marginBlockSet);
        // Add the common border properties
	layoutmasterset.or(PropertySets.borderSet);
        // Add the common padding properties
	layoutmasterset.or(PropertySets.paddingSet);
        // Add the common background properties
	layoutmasterset.or(PropertySets.backgroundSet);
        layoutMasterSet = new ROBitSet(layoutmasterset);

        BitSet flowonlyset = new BitSet();
        flowonlyset.set(PropNames.MARKER_CLASS_NAME);

        BitSet staticonlyset = new BitSet();
        staticonlyset.set(PropNames.RETRIEVE_CLASS_NAME);
        staticonlyset.set(PropNames.RETRIEVE_POSITION);
        staticonlyset.set(PropNames.RETRIEVE_BOUNDARY);

        // pageseqonly contains the properties which are exclusive to
        // fo:pagesequence
        BitSet pageseqonly = new BitSet();
        pageseqonly.set(PropNames.FORMAT);
        pageseqonly.set(PropNames.LETTER_VALUE);
        pageseqonly.set(PropNames.GROUPING_SEPARATOR);
        pageseqonly.set(PropNames.GROUPING_SIZE);
        pageseqonly.set(PropNames.INITIAL_PAGE_NUMBER);
        pageseqonly.set(PropNames.FORCE_PAGE_COUNT);

        // pageseqset may contain any of the exclusive elements of the
        // flow set or the static-content set, which may be accessed by
        // the from-nearest-specified-property() function.
        BitSet pageseqset = (BitSet)allprops.clone();
        pageseqset.andNot(rootonly);
        pageseqset.andNot(declarationsonly);
        pageseqset.andNot(layoutmasteronly);
        pageSeqSet = new ROBitSet(pageseqset);

        BitSet flowallset = (BitSet)pageseqset.clone();
        flowallset.andNot(pageseqonly);
        flowallset.andNot(staticonlyset);

        flowAllSet = new ROBitSet(flowallset);

        BitSet staticallset = (BitSet)pageseqset.clone();
        staticallset.andNot(pageseqonly);
        staticallset.andNot(flowonlyset);

        staticAllSet = new ROBitSet(staticallset);

        BitSet markerallset = (BitSet)flowallset.clone();
        markerallset.clear(PropNames.MARKER_CLASS_NAME);

        markerAllSet = new ROBitSet(markerallset);

        // markers are not allowed within fo:title
        titleAllSet = markerAllSet;
    }

    /**
     * ReferenceArea trait mappings.  Immutable BitSet of FOs for which
     * the <tt>reference-area</tt> trait is true.
     */
    public static final ROBitSet isReferenceArea;
    static {
        BitSet refareas = new BitSet(FObjectNames.LAST_FO + 1);
        refareas.set(FObjectNames.SIMPLE_PAGE_MASTER);
        refareas.set(FObjectNames.REGION_AFTER);
        refareas.set(FObjectNames.REGION_BEFORE);
        refareas.set(FObjectNames.REGION_BODY);
        refareas.set(FObjectNames.REGION_END);
        refareas.set(FObjectNames.REGION_START);
        refareas.set(FObjectNames.BLOCK_CONTAINER);
        refareas.set(FObjectNames.INLINE_CONTAINER);
        refareas.set(FObjectNames.TABLE);
        refareas.set(FObjectNames.TABLE_CAPTION);
        refareas.set(FObjectNames.TABLE_CELL);
        refareas.set(FObjectNames.TITLE);

        isReferenceArea = new ROBitSet(refareas);
    }

}

