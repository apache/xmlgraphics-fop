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

import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;

// Only for tree property set partitions
import java.util.BitSet;
import java.util.Iterator;

import org.apache.fop.fo.FObjectNames;
import org.apache.fop.fo.PropertySets;
import org.apache.fop.fo.PropNames;
import org.apache.fop.datatypes.Ints;
import org.apache.fop.datastructs.ROIntArray;

/**
 * Data class relating sets of properties to Flow Objects.
 */

public class FObjects {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    public static final String XSLNamespace =
                                        "http://www.w3.org/1999/XSL/Format";

    private static final String packageName = "org.apache.fop.fo";
    //private static final String fobsPackageName = packageName + ".fobs";
    private static final String fobsPackageName = packageName;

    public static int getFoIndex(String name) {
        return ((Integer)(foToIndex.get(name))).intValue();
    }

    public static String getClassName(int foIndex) {
        return foClassNames[foIndex];
    }

    public static Class getClass(int foIndex) {
        return foClasses[foIndex];
    }

    public static BitSet getLayoutMasterSet() {
        return (BitSet)(layoutMasterSet.clone());
    }

    public static BitSet getPageFlowSet() {
        return (BitSet)(pageFlowSet.clone());
    }

    /**
     * A String[] array of the fo class names.  This array is
     * effectively 1-based, with the first element being unused.
     * The array is initialized in a static initializer by converting the
     * fo names from the array FObjectNames.foLocalNames into class names by
     * converting the first character of every component word to upper case,
     * removing all punctuation characters and prepending the prefix 'Fo'.
     *  It can be indexed by the fo name constants defined in this file.
     */
    private static final String[] foClassNames;

    /**
     * An Class[] array containing Class objects corresponding to each of the
     * class names in the foClassNames array.  It is initialized in a static
     * initializer in parallel with the creation of the class names in the
     * foClassNames array.  It can be indexed by the class name constants
     * defined in this file.
     *
     * It is not guaranteed that there exists a class corresponding to each of
     * the FlowObjects defined in this file.
     */
    private static final Class[] foClasses;

    /**
     * A HashMap whose elements are an integer index value keyed by an
     * fo local name.  The index value is the index of the fo local name in
     * the FObjectNames.foLocalNames[] array.
     * It is initialized in a static initializer.
     */
    private static final HashMap foToIndex;

    /**
     * A HashMap whose elements are an integer index value keyed by the name
     * of a fo class.  The index value is the index of the fo
     * class name in the foClassNames[] array.  It is initialized in a
     * static initializer.
     */
    private static final HashMap foClassToIndex;

    /**
     * A array of <tt>HashSet</tt>s indexed by the integer <i>FO</i>
     * element constants.
     * Each <tt>HashSet</tt> contains the set of <i>properties</i> that apply
     * to the corresponding formatting object..  This array, and each
     * <tt>HashSet</tt> within it, is intialized in a static initializer.
     */
    private static final HashSet[] foPropertyLists;

    /**
     * A Bitmap representing all of the Properties for use in building
     * the partition sets of the properties.
     */

    static {
        foPropertyLists = new HashSet[FObjectNames.LAST_FO + 1];
        
        foPropertyLists[FObjectNames.NO_FO] = new HashSet(1);
        foPropertyLists[FObjectNames.NO_FO].
                	    add(Ints.consts.get(PropNames.NO_PROPERTY));

        //basic-link
        foPropertyLists[FObjectNames.BASIC_LINK] = new HashSet(
                                PropertySets.accessibilityPropsSize +
                                PropertySets.auralPropsSize +
                                PropertySets.backgroundPropsSize +
                                PropertySets.borderPropsSize +
                                PropertySets.paddingPropsSize +
                                PropertySets.marginInlinePropsSize +
                                PropertySets.relativePositionPropsSize +
                                18 );
        foPropertyLists[FObjectNames.BASIC_LINK].
                            addAll(PropertySets.accessibilitySet);
        foPropertyLists[FObjectNames.BASIC_LINK].
                            addAll(PropertySets.auralSet);
        foPropertyLists[FObjectNames.BASIC_LINK].
                            addAll(PropertySets.backgroundSet);
        foPropertyLists[FObjectNames.BASIC_LINK].
                            addAll(PropertySets.borderSet);
        foPropertyLists[FObjectNames.BASIC_LINK].
                            addAll(PropertySets.paddingSet);
        foPropertyLists[FObjectNames.BASIC_LINK].
                            addAll(PropertySets.marginInlineSet);
        foPropertyLists[FObjectNames.BASIC_LINK].
                            addAll(PropertySets.relativePositionSet);
        foPropertyLists[FObjectNames.BASIC_LINK].
                        add(Ints.consts.get(PropNames.ALIGNMENT_ADJUST));
        foPropertyLists[FObjectNames.BASIC_LINK].
                        add(Ints.consts.get(PropNames.ALIGNMENT_BASELINE));
        foPropertyLists[FObjectNames.BASIC_LINK].
                        add(Ints.consts.get(PropNames.BASELINE_SHIFT));
        foPropertyLists[FObjectNames.BASIC_LINK].
                add(Ints.consts.get(PropNames.DESTINATION_PLACEMENT_OFFSET));
        foPropertyLists[FObjectNames.BASIC_LINK].
                        add(Ints.consts.get(PropNames.DOMINANT_BASELINE));
        foPropertyLists[FObjectNames.BASIC_LINK].
                        add(Ints.consts.get(PropNames.EXTERNAL_DESTINATION));
        foPropertyLists[FObjectNames.BASIC_LINK].
                        add(Ints.consts.get(PropNames.ID));
        foPropertyLists[FObjectNames.BASIC_LINK].
                        add(Ints.consts.get(PropNames.INDICATE_DESTINATION));
        foPropertyLists[FObjectNames.BASIC_LINK].
                        add(Ints.consts.get(PropNames.INTERNAL_DESTINATION));
        foPropertyLists[FObjectNames.BASIC_LINK].
                        add(Ints.consts.get(PropNames.KEEP_TOGETHER));
        foPropertyLists[FObjectNames.BASIC_LINK].
                        add(Ints.consts.get(PropNames.KEEP_WITH_NEXT));
        foPropertyLists[FObjectNames.BASIC_LINK].
                        add(Ints.consts.get(PropNames.KEEP_WITH_PREVIOUS));
        foPropertyLists[FObjectNames.BASIC_LINK].
                        add(Ints.consts.get(PropNames.LINE_HEIGHT));
        foPropertyLists[FObjectNames.BASIC_LINK].
                        add(Ints.consts.get(PropNames.SHOW_DESTINATION));
        foPropertyLists[FObjectNames.BASIC_LINK].
                    add(Ints.consts.get(PropNames.TARGET_PROCESSING_CONTEXT));
        foPropertyLists[FObjectNames.BASIC_LINK].
                add(Ints.consts.get(PropNames.TARGET_PRESENTATION_CONTEXT));
        foPropertyLists[FObjectNames.BASIC_LINK].
                        add(Ints.consts.get(PropNames.TARGET_STYLESHEET));
        
        //bidi-override
        foPropertyLists[FObjectNames.BIDI_OVERRIDE] = new HashSet(
                                PropertySets.auralPropsSize +
                                PropertySets.fontPropsSize +
                                PropertySets.relativePositionPropsSize +
                                1);
        foPropertyLists[FObjectNames.BIDI_OVERRIDE].
                            addAll(PropertySets.auralSet);
        foPropertyLists[FObjectNames.BIDI_OVERRIDE].
                            addAll(PropertySets.fontSet);
        foPropertyLists[FObjectNames.BIDI_OVERRIDE].
                            addAll(PropertySets.relativePositionSet);
        foPropertyLists[FObjectNames.BIDI_OVERRIDE].
                        add(Ints.consts.get(PropNames.COLOR));
        foPropertyLists[FObjectNames.BIDI_OVERRIDE].
                        add(Ints.consts.get(PropNames.DIRECTION));
        foPropertyLists[FObjectNames.BIDI_OVERRIDE].
                        add(Ints.consts.get(PropNames.ID));
        foPropertyLists[FObjectNames.BIDI_OVERRIDE].
                        add(Ints.consts.get(PropNames.LETTER_SPACING));
        foPropertyLists[FObjectNames.BIDI_OVERRIDE].
                        add(Ints.consts.get(PropNames.LINE_HEIGHT));
        foPropertyLists[FObjectNames.BIDI_OVERRIDE].
                        add(Ints.consts.get(PropNames.SCORE_SPACES));
        foPropertyLists[FObjectNames.BIDI_OVERRIDE].
                        add(Ints.consts.get(PropNames.UNICODE_BIDI));
        foPropertyLists[FObjectNames.BIDI_OVERRIDE].
                        add(Ints.consts.get(PropNames.WORD_SPACING));

        //block
        foPropertyLists[FObjectNames.BLOCK] = new HashSet(
                                PropertySets.accessibilityPropsSize +
                                PropertySets.auralPropsSize +
                                PropertySets.backgroundPropsSize +
                                PropertySets.borderPropsSize +
                                PropertySets.fontPropsSize +
                                PropertySets.hyphenationPropsSize +
                                PropertySets.marginBlockPropsSize +
                                PropertySets.paddingPropsSize +
                                PropertySets.relativePositionPropsSize +
                                27);
        foPropertyLists[FObjectNames.BLOCK].
                            addAll(PropertySets.accessibilitySet);
        foPropertyLists[FObjectNames.BLOCK].
                            addAll(PropertySets.auralSet);
        foPropertyLists[FObjectNames.BLOCK].
                            addAll(PropertySets.backgroundSet);
        foPropertyLists[FObjectNames.BLOCK].
                            addAll(PropertySets.borderSet);
        foPropertyLists[FObjectNames.BLOCK].
                            addAll(PropertySets.fontSet);
        foPropertyLists[FObjectNames.BLOCK].
                            addAll(PropertySets.hyphenationSet);
        foPropertyLists[FObjectNames.BLOCK].
                            addAll(PropertySets.marginBlockSet);
        foPropertyLists[FObjectNames.BLOCK].
                            addAll(PropertySets.paddingSet);
        foPropertyLists[FObjectNames.BLOCK].
                            addAll(PropertySets.relativePositionSet);
        foPropertyLists[FObjectNames.BLOCK].
                        add(Ints.consts.get(PropNames.BREAK_AFTER));
        foPropertyLists[FObjectNames.BLOCK].
                        add(Ints.consts.get(PropNames.BREAK_BEFORE));
        foPropertyLists[FObjectNames.BLOCK].
                        add(Ints.consts.get(PropNames.COLOR));
        foPropertyLists[FObjectNames.BLOCK].
                        add(Ints.consts.get(PropNames.TEXT_DEPTH));
        foPropertyLists[FObjectNames.BLOCK].
                        add(Ints.consts.get(PropNames.TEXT_ALTITUDE));
        foPropertyLists[FObjectNames.BLOCK].
                        add(Ints.consts.get(PropNames.HYPHENATION_KEEP));
        foPropertyLists[FObjectNames.BLOCK].
                    add(Ints.consts.get(PropNames.HYPHENATION_LADDER_COUNT));
        foPropertyLists[FObjectNames.BLOCK].
                        add(Ints.consts.get(PropNames.ID));
        foPropertyLists[FObjectNames.BLOCK].
                        add(Ints.consts.get(PropNames.INTRUSION_DISPLACE));
        foPropertyLists[FObjectNames.BLOCK].
                        add(Ints.consts.get(PropNames.KEEP_TOGETHER));
        foPropertyLists[FObjectNames.BLOCK].
                        add(Ints.consts.get(PropNames.KEEP_WITH_NEXT));
        foPropertyLists[FObjectNames.BLOCK].
                        add(Ints.consts.get(PropNames.KEEP_WITH_PREVIOUS));
        foPropertyLists[FObjectNames.BLOCK].
                        add(Ints.consts.get(PropNames.LAST_LINE_END_INDENT));
        foPropertyLists[FObjectNames.BLOCK].
                        add(Ints.consts.get(PropNames.LINEFEED_TREATMENT));
        foPropertyLists[FObjectNames.BLOCK].
                        add(Ints.consts.get(PropNames.LINE_HEIGHT));
        foPropertyLists[FObjectNames.BLOCK].
                add(Ints.consts.get(PropNames.LINE_HEIGHT_SHIFT_ADJUSTMENT));
        foPropertyLists[FObjectNames.BLOCK].
                    add(Ints.consts.get(PropNames.LINE_STACKING_STRATEGY));
        foPropertyLists[FObjectNames.BLOCK].
                        add(Ints.consts.get(PropNames.ORPHANS));
        foPropertyLists[FObjectNames.BLOCK].
                        add(Ints.consts.get(PropNames.WHITE_SPACE_TREATMENT));
        foPropertyLists[FObjectNames.BLOCK].
                        add(Ints.consts.get(PropNames.SPAN));
        foPropertyLists[FObjectNames.BLOCK].
                        add(Ints.consts.get(PropNames.TEXT_ALIGN));
        foPropertyLists[FObjectNames.BLOCK].
                        add(Ints.consts.get(PropNames.TEXT_ALIGN_LAST));
        foPropertyLists[FObjectNames.BLOCK].
                        add(Ints.consts.get(PropNames.TEXT_INDENT));
        foPropertyLists[FObjectNames.BLOCK].
                        add(Ints.consts.get(PropNames.VISIBILITY));
        foPropertyLists[FObjectNames.BLOCK].
                        add(Ints.consts.get(PropNames.WHITE_SPACE_COLLAPSE));
        foPropertyLists[FObjectNames.BLOCK].
                        add(Ints.consts.get(PropNames.WIDOWS));
        foPropertyLists[FObjectNames.BLOCK].
                        add(Ints.consts.get(PropNames.WRAP_OPTION));

        //block-container
        foPropertyLists[FObjectNames.BLOCK_CONTAINER] = new HashSet(
                                PropertySets.absolutePositionPropsSize +
                                PropertySets.backgroundPropsSize +
                                PropertySets.borderPropsSize +
                                PropertySets.marginBlockPropsSize +
                                PropertySets.paddingPropsSize +
                                18);
        foPropertyLists[FObjectNames.BLOCK_CONTAINER].
                            addAll(PropertySets.absolutePositionSet);
        foPropertyLists[FObjectNames.BLOCK_CONTAINER].
                            addAll(PropertySets.backgroundSet);
        foPropertyLists[FObjectNames.BLOCK_CONTAINER].
                            addAll(PropertySets.borderSet);
        foPropertyLists[FObjectNames.BLOCK_CONTAINER].
                            addAll(PropertySets.marginBlockSet);
        foPropertyLists[FObjectNames.BLOCK_CONTAINER].
                            addAll(PropertySets.paddingSet);
        foPropertyLists[FObjectNames.BLOCK_CONTAINER].
                add(Ints.consts.get(PropNames.BLOCK_PROGRESSION_DIMENSION));
        foPropertyLists[FObjectNames.BLOCK_CONTAINER].
                        add(Ints.consts.get(PropNames.BREAK_AFTER));
        foPropertyLists[FObjectNames.BLOCK_CONTAINER].
                        add(Ints.consts.get(PropNames.BREAK_BEFORE));
        foPropertyLists[FObjectNames.BLOCK_CONTAINER].
                        add(Ints.consts.get(PropNames.CLIP));
        foPropertyLists[FObjectNames.BLOCK_CONTAINER].
                        add(Ints.consts.get(PropNames.DISPLAY_ALIGN));
        foPropertyLists[FObjectNames.BLOCK_CONTAINER].
                        add(Ints.consts.get(PropNames.HEIGHT));
        foPropertyLists[FObjectNames.BLOCK_CONTAINER].
                        add(Ints.consts.get(PropNames.ID));
        foPropertyLists[FObjectNames.BLOCK_CONTAINER].
                add(Ints.consts.get(PropNames.INLINE_PROGRESSION_DIMENSION));
        foPropertyLists[FObjectNames.BLOCK_CONTAINER].
                        add(Ints.consts.get(PropNames.INTRUSION_DISPLACE));
        foPropertyLists[FObjectNames.BLOCK_CONTAINER].
                        add(Ints.consts.get(PropNames.KEEP_TOGETHER));
        foPropertyLists[FObjectNames.BLOCK_CONTAINER].
                        add(Ints.consts.get(PropNames.KEEP_WITH_NEXT));
        foPropertyLists[FObjectNames.BLOCK_CONTAINER].
                        add(Ints.consts.get(PropNames.KEEP_WITH_PREVIOUS));
        foPropertyLists[FObjectNames.BLOCK_CONTAINER].
                        add(Ints.consts.get(PropNames.OVERFLOW));
        foPropertyLists[FObjectNames.BLOCK_CONTAINER].
                        add(Ints.consts.get(PropNames.REFERENCE_ORIENTATION));
        foPropertyLists[FObjectNames.BLOCK_CONTAINER].
                        add(Ints.consts.get(PropNames.SPAN));
        foPropertyLists[FObjectNames.BLOCK_CONTAINER].
                        add(Ints.consts.get(PropNames.WIDTH));
        foPropertyLists[FObjectNames.BLOCK_CONTAINER].
                        add(Ints.consts.get(PropNames.WRITING_MODE));
        foPropertyLists[FObjectNames.BLOCK_CONTAINER].
                        add(Ints.consts.get(PropNames.Z_INDEX));

        //character
        foPropertyLists[FObjectNames.CHARACTER] = new HashSet(
                                PropertySets.auralPropsSize +
                                PropertySets.backgroundPropsSize +
                                PropertySets.borderPropsSize +
                                PropertySets.fontPropsSize +
                                PropertySets.hyphenationPropsSize +
                                PropertySets.marginInlinePropsSize +
                                PropertySets.paddingPropsSize +
                                PropertySets.relativePositionPropsSize +
                                23);
        foPropertyLists[FObjectNames.CHARACTER].
                            addAll(PropertySets.auralSet);
        foPropertyLists[FObjectNames.CHARACTER].
                            addAll(PropertySets.backgroundSet);
        foPropertyLists[FObjectNames.CHARACTER].
                            addAll(PropertySets.borderSet);
        foPropertyLists[FObjectNames.CHARACTER].
                            addAll(PropertySets.fontSet);
        foPropertyLists[FObjectNames.CHARACTER].
                            addAll(PropertySets.hyphenationSet);
        foPropertyLists[FObjectNames.CHARACTER].
                            addAll(PropertySets.marginInlineSet);
        foPropertyLists[FObjectNames.CHARACTER].
                            addAll(PropertySets.paddingSet);
        foPropertyLists[FObjectNames.CHARACTER].
                            addAll(PropertySets.relativePositionSet);
        foPropertyLists[FObjectNames.CHARACTER].
                        add(Ints.consts.get(PropNames.ALIGNMENT_ADJUST));
        foPropertyLists[FObjectNames.CHARACTER].
                        add(Ints.consts.get(PropNames.TREAT_AS_WORD_SPACE));
        foPropertyLists[FObjectNames.CHARACTER].
                        add(Ints.consts.get(PropNames.ALIGNMENT_BASELINE));
        foPropertyLists[FObjectNames.CHARACTER].
                        add(Ints.consts.get(PropNames.BASELINE_SHIFT));
        foPropertyLists[FObjectNames.CHARACTER].
                        add(Ints.consts.get(PropNames.CHARACTER));
        foPropertyLists[FObjectNames.CHARACTER].
                        add(Ints.consts.get(PropNames.COLOR));
        foPropertyLists[FObjectNames.CHARACTER].
                        add(Ints.consts.get(PropNames.DOMINANT_BASELINE));
        foPropertyLists[FObjectNames.CHARACTER].
                        add(Ints.consts.get(PropNames.TEXT_DEPTH));
        foPropertyLists[FObjectNames.CHARACTER].
                        add(Ints.consts.get(PropNames.TEXT_ALTITUDE));
        foPropertyLists[FObjectNames.CHARACTER].
                add(Ints.consts.get(PropNames.GLYPH_ORIENTATION_HORIZONTAL));
        foPropertyLists[FObjectNames.CHARACTER].
                add(Ints.consts.get(PropNames.GLYPH_ORIENTATION_VERTICAL));
        foPropertyLists[FObjectNames.CHARACTER].
                        add(Ints.consts.get(PropNames.ID));
        foPropertyLists[FObjectNames.CHARACTER].
                        add(Ints.consts.get(PropNames.KEEP_WITH_NEXT));
        foPropertyLists[FObjectNames.CHARACTER].
                        add(Ints.consts.get(PropNames.KEEP_WITH_PREVIOUS));
        foPropertyLists[FObjectNames.CHARACTER].
                        add(Ints.consts.get(PropNames.LETTER_SPACING));
        foPropertyLists[FObjectNames.CHARACTER].
                        add(Ints.consts.get(PropNames.LINE_HEIGHT));
        foPropertyLists[FObjectNames.CHARACTER].
                        add(Ints.consts.get(PropNames.SCORE_SPACES));
        foPropertyLists[FObjectNames.CHARACTER].
                        add(Ints.consts.get(PropNames.SUPPRESS_AT_LINE_BREAK));
        foPropertyLists[FObjectNames.CHARACTER].
                        add(Ints.consts.get(PropNames.TEXT_DECORATION));
        foPropertyLists[FObjectNames.CHARACTER].
                        add(Ints.consts.get(PropNames.TEXT_SHADOW));
        foPropertyLists[FObjectNames.CHARACTER].
                        add(Ints.consts.get(PropNames.TEXT_TRANSFORM));
        foPropertyLists[FObjectNames.CHARACTER].
                        add(Ints.consts.get(PropNames.VISIBILITY));
        foPropertyLists[FObjectNames.CHARACTER].
                        add(Ints.consts.get(PropNames.WORD_SPACING));

        //color-profile
        foPropertyLists[FObjectNames.COLOR_PROFILE] = new HashSet(3);
        foPropertyLists[FObjectNames.COLOR_PROFILE].
                        add(Ints.consts.get(PropNames.COLOR_PROFILE_NAME));
        foPropertyLists[FObjectNames.COLOR_PROFILE].
                        add(Ints.consts.get(PropNames.RENDERING_INTENT));
        foPropertyLists[FObjectNames.COLOR_PROFILE].
                        add(Ints.consts.get(PropNames.SRC));

        //conditional-page-master-reference
        foPropertyLists[FObjectNames.CONDITIONAL_PAGE_MASTER_REFERENCE]
                        = new HashSet(4);
        foPropertyLists[FObjectNames.CONDITIONAL_PAGE_MASTER_REFERENCE].
                        add(Ints.consts.get(PropNames.MASTER_REFERENCE));
        foPropertyLists[FObjectNames.CONDITIONAL_PAGE_MASTER_REFERENCE].
                        add(Ints.consts.get(PropNames.PAGE_POSITION));
        foPropertyLists[FObjectNames.CONDITIONAL_PAGE_MASTER_REFERENCE].
                        add(Ints.consts.get(PropNames.ODD_OR_EVEN));
        foPropertyLists[FObjectNames.CONDITIONAL_PAGE_MASTER_REFERENCE].
                        add(Ints.consts.get(PropNames.BLANK_OR_NOT_BLANK));

        //declarations
        foPropertyLists[FObjectNames.DECLARATIONS] = null;

        //external-graphic
        foPropertyLists[FObjectNames.EXTERNAL_GRAPHIC] = new HashSet(
                                PropertySets.accessibilityPropsSize +
                                PropertySets.auralPropsSize +
                                PropertySets.backgroundPropsSize +
                                PropertySets.borderPropsSize +
                                PropertySets.marginInlinePropsSize +
                                PropertySets.paddingPropsSize +
                                PropertySets.relativePositionPropsSize +
                                22);
        foPropertyLists[FObjectNames.EXTERNAL_GRAPHIC].
                            addAll(PropertySets.accessibilitySet);
        foPropertyLists[FObjectNames.EXTERNAL_GRAPHIC].
                            addAll(PropertySets.auralSet);
        foPropertyLists[FObjectNames.EXTERNAL_GRAPHIC].
                            addAll(PropertySets.backgroundSet);
        foPropertyLists[FObjectNames.EXTERNAL_GRAPHIC].
                            addAll(PropertySets.borderSet);
        foPropertyLists[FObjectNames.EXTERNAL_GRAPHIC].
                            addAll(PropertySets.marginInlineSet);
        foPropertyLists[FObjectNames.EXTERNAL_GRAPHIC].
                            addAll(PropertySets.paddingSet);
        foPropertyLists[FObjectNames.EXTERNAL_GRAPHIC].
                            addAll(PropertySets.relativePositionSet);
        foPropertyLists[FObjectNames.EXTERNAL_GRAPHIC].
                        add(Ints.consts.get(PropNames.ALIGNMENT_ADJUST));
        foPropertyLists[FObjectNames.EXTERNAL_GRAPHIC].
                        add(Ints.consts.get(PropNames.ALIGNMENT_BASELINE));
        foPropertyLists[FObjectNames.EXTERNAL_GRAPHIC].
                        add(Ints.consts.get(PropNames.BASELINE_SHIFT));
        foPropertyLists[FObjectNames.EXTERNAL_GRAPHIC].
                add(Ints.consts.get(PropNames.BLOCK_PROGRESSION_DIMENSION));
        foPropertyLists[FObjectNames.EXTERNAL_GRAPHIC].
                        add(Ints.consts.get(PropNames.CLIP));
        foPropertyLists[FObjectNames.EXTERNAL_GRAPHIC].
                        add(Ints.consts.get(PropNames.CONTENT_HEIGHT));
        foPropertyLists[FObjectNames.EXTERNAL_GRAPHIC].
                        add(Ints.consts.get(PropNames.CONTENT_TYPE));
        foPropertyLists[FObjectNames.EXTERNAL_GRAPHIC].
                        add(Ints.consts.get(PropNames.CONTENT_WIDTH));
        foPropertyLists[FObjectNames.EXTERNAL_GRAPHIC].
                        add(Ints.consts.get(PropNames.DISPLAY_ALIGN));
        foPropertyLists[FObjectNames.EXTERNAL_GRAPHIC].
                        add(Ints.consts.get(PropNames.DOMINANT_BASELINE));
        foPropertyLists[FObjectNames.EXTERNAL_GRAPHIC].
                        add(Ints.consts.get(PropNames.HEIGHT));
        foPropertyLists[FObjectNames.EXTERNAL_GRAPHIC].
                        add(Ints.consts.get(PropNames.ID));
        foPropertyLists[FObjectNames.EXTERNAL_GRAPHIC].
                add(Ints.consts.get(PropNames.INLINE_PROGRESSION_DIMENSION));
        foPropertyLists[FObjectNames.EXTERNAL_GRAPHIC].
                        add(Ints.consts.get(PropNames.KEEP_WITH_NEXT));
        foPropertyLists[FObjectNames.EXTERNAL_GRAPHIC].
                        add(Ints.consts.get(PropNames.KEEP_WITH_PREVIOUS));
        foPropertyLists[FObjectNames.EXTERNAL_GRAPHIC].
                        add(Ints.consts.get(PropNames.LINE_HEIGHT));
        foPropertyLists[FObjectNames.EXTERNAL_GRAPHIC].
                        add(Ints.consts.get(PropNames.OVERFLOW));
        foPropertyLists[FObjectNames.EXTERNAL_GRAPHIC].
                        add(Ints.consts.get(PropNames.SCALING));
        foPropertyLists[FObjectNames.EXTERNAL_GRAPHIC].
                        add(Ints.consts.get(PropNames.SCALING_METHOD));
        foPropertyLists[FObjectNames.EXTERNAL_GRAPHIC].
                        add(Ints.consts.get(PropNames.SRC));
        foPropertyLists[FObjectNames.EXTERNAL_GRAPHIC].
                        add(Ints.consts.get(PropNames.TEXT_ALIGN));
        foPropertyLists[FObjectNames.EXTERNAL_GRAPHIC].
                        add(Ints.consts.get(PropNames.WIDTH));

        //float
        foPropertyLists[FObjectNames.FLOAT] = new HashSet(2);
        foPropertyLists[FObjectNames.FLOAT].
                        add(Ints.consts.get(PropNames.CLEAR));
        foPropertyLists[FObjectNames.FLOAT].
                        add(Ints.consts.get(PropNames.FLOAT));

        //flow
        foPropertyLists[FObjectNames.FLOW] = new HashSet(1);
        foPropertyLists[FObjectNames.FLOW].
                        add(Ints.consts.get(PropNames.FLOW_NAME));

        //footnote
        foPropertyLists[FObjectNames.FOOTNOTE] = new HashSet(
                                PropertySets.accessibilityPropsSize);
        foPropertyLists[FObjectNames.FOOTNOTE].
                            addAll(PropertySets.accessibilitySet);

        //footnote-body
        foPropertyLists[FObjectNames.FOOTNOTE_BODY] = new HashSet(
                                PropertySets.accessibilityPropsSize);
        foPropertyLists[FObjectNames.FOOTNOTE_BODY].
                            addAll(PropertySets.accessibilitySet);

        //initial-property-set
        foPropertyLists[FObjectNames.INITIAL_PROPERTY_SET] = new HashSet(
                                PropertySets.accessibilityPropsSize +
                                PropertySets.auralPropsSize +
                                PropertySets.backgroundPropsSize +
                                PropertySets.borderPropsSize +
                                PropertySets.fontPropsSize +
                                PropertySets.paddingPropsSize +
                                PropertySets.relativePositionPropsSize +
                                1);
        foPropertyLists[FObjectNames.INITIAL_PROPERTY_SET].
                            addAll(PropertySets.accessibilitySet);
        foPropertyLists[FObjectNames.INITIAL_PROPERTY_SET].
                            addAll(PropertySets.auralSet);
        foPropertyLists[FObjectNames.INITIAL_PROPERTY_SET].
                            addAll(PropertySets.backgroundSet);
        foPropertyLists[FObjectNames.INITIAL_PROPERTY_SET].
                            addAll(PropertySets.borderSet);
        foPropertyLists[FObjectNames.INITIAL_PROPERTY_SET].
                            addAll(PropertySets.fontSet);
        foPropertyLists[FObjectNames.INITIAL_PROPERTY_SET].
                            addAll(PropertySets.paddingSet);
        foPropertyLists[FObjectNames.INITIAL_PROPERTY_SET].
                            addAll(PropertySets.relativePositionSet);
        foPropertyLists[FObjectNames.INITIAL_PROPERTY_SET].
                        add(Ints.consts.get(PropNames.COLOR));
        foPropertyLists[FObjectNames.INITIAL_PROPERTY_SET].
                        add(Ints.consts.get(PropNames.ID));
        foPropertyLists[FObjectNames.INITIAL_PROPERTY_SET].
                        add(Ints.consts.get(PropNames.LETTER_SPACING));
        foPropertyLists[FObjectNames.INITIAL_PROPERTY_SET].
                        add(Ints.consts.get(PropNames.LINE_HEIGHT));
        foPropertyLists[FObjectNames.INITIAL_PROPERTY_SET].
                        add(Ints.consts.get(PropNames.SCORE_SPACES));
        foPropertyLists[FObjectNames.INITIAL_PROPERTY_SET].
                        add(Ints.consts.get(PropNames.TEXT_DECORATION));
        foPropertyLists[FObjectNames.INITIAL_PROPERTY_SET].
                        add(Ints.consts.get(PropNames.TEXT_SHADOW));
        foPropertyLists[FObjectNames.INITIAL_PROPERTY_SET].
                        add(Ints.consts.get(PropNames.TEXT_TRANSFORM));
        foPropertyLists[FObjectNames.INITIAL_PROPERTY_SET].
                        add(Ints.consts.get(PropNames.WORD_SPACING));

        //inline
        foPropertyLists[FObjectNames.INLINE] = new HashSet(
                                PropertySets.accessibilityPropsSize +
                                PropertySets.auralPropsSize +
                                PropertySets.backgroundPropsSize +
                                PropertySets.borderPropsSize +
                                PropertySets.fontPropsSize +
                                PropertySets.marginInlinePropsSize +
                                PropertySets.paddingPropsSize +
                                PropertySets.relativePositionPropsSize +
                                17);
        foPropertyLists[FObjectNames.INLINE].
                            addAll(PropertySets.accessibilitySet);
        foPropertyLists[FObjectNames.INLINE].
                            addAll(PropertySets.auralSet);
        foPropertyLists[FObjectNames.INLINE].
                            addAll(PropertySets.backgroundSet);
        foPropertyLists[FObjectNames.INLINE].
                            addAll(PropertySets.borderSet);
        foPropertyLists[FObjectNames.INLINE].
                            addAll(PropertySets.fontSet);
        foPropertyLists[FObjectNames.INLINE].
                            addAll(PropertySets.marginInlineSet);
        foPropertyLists[FObjectNames.INLINE].
                            addAll(PropertySets.paddingSet);
        foPropertyLists[FObjectNames.INLINE].
                            addAll(PropertySets.relativePositionSet);
        foPropertyLists[FObjectNames.INLINE].
                        add(Ints.consts.get(PropNames.ALIGNMENT_ADJUST));
        foPropertyLists[FObjectNames.INLINE].
                        add(Ints.consts.get(PropNames.ALIGNMENT_BASELINE));
        foPropertyLists[FObjectNames.INLINE].
                        add(Ints.consts.get(PropNames.BASELINE_SHIFT));
        foPropertyLists[FObjectNames.INLINE].
                    add(Ints.consts.get(PropNames.BLOCK_PROGRESSION_DIMENSION));
        foPropertyLists[FObjectNames.INLINE].
                        add(Ints.consts.get(PropNames.COLOR));
        foPropertyLists[FObjectNames.INLINE].
                        add(Ints.consts.get(PropNames.DOMINANT_BASELINE));
        foPropertyLists[FObjectNames.INLINE].
                        add(Ints.consts.get(PropNames.HEIGHT));
        foPropertyLists[FObjectNames.INLINE].
                        add(Ints.consts.get(PropNames.ID));
        foPropertyLists[FObjectNames.INLINE].
                    add(Ints.consts.get(PropNames.INLINE_PROGRESSION_DIMENSION));
        foPropertyLists[FObjectNames.INLINE].
                        add(Ints.consts.get(PropNames.KEEP_TOGETHER));
        foPropertyLists[FObjectNames.INLINE].
                        add(Ints.consts.get(PropNames.KEEP_WITH_NEXT));
        foPropertyLists[FObjectNames.INLINE].
                        add(Ints.consts.get(PropNames.KEEP_WITH_PREVIOUS));
        foPropertyLists[FObjectNames.INLINE].
                        add(Ints.consts.get(PropNames.LINE_HEIGHT));
        foPropertyLists[FObjectNames.INLINE].
                        add(Ints.consts.get(PropNames.TEXT_DECORATION));
        foPropertyLists[FObjectNames.INLINE].
                        add(Ints.consts.get(PropNames.VISIBILITY));
        foPropertyLists[FObjectNames.INLINE].
                        add(Ints.consts.get(PropNames.WIDTH));
        foPropertyLists[FObjectNames.INLINE].
                        add(Ints.consts.get(PropNames.WRAP_OPTION));

        //inline-container
        foPropertyLists[FObjectNames.INLINE_CONTAINER] = new HashSet(
                                PropertySets.backgroundPropsSize +
                                PropertySets.borderPropsSize +
                                PropertySets.marginInlinePropsSize +
                                PropertySets.paddingPropsSize +
                                PropertySets.relativePositionPropsSize +
                                18);
        foPropertyLists[FObjectNames.INLINE_CONTAINER].
                            addAll(PropertySets.backgroundSet);
        foPropertyLists[FObjectNames.INLINE_CONTAINER].
                            addAll(PropertySets.borderSet);
        foPropertyLists[FObjectNames.INLINE_CONTAINER].
                            addAll(PropertySets.marginInlineSet);
        foPropertyLists[FObjectNames.INLINE_CONTAINER].
                            addAll(PropertySets.paddingSet);
        foPropertyLists[FObjectNames.INLINE_CONTAINER].
                            addAll(PropertySets.relativePositionSet);
        foPropertyLists[FObjectNames.INLINE_CONTAINER].
                        add(Ints.consts.get(PropNames.ALIGNMENT_ADJUST));
        foPropertyLists[FObjectNames.INLINE_CONTAINER].
                        add(Ints.consts.get(PropNames.ALIGNMENT_BASELINE));
        foPropertyLists[FObjectNames.INLINE_CONTAINER].
                        add(Ints.consts.get(PropNames.BASELINE_SHIFT));
        foPropertyLists[FObjectNames.INLINE_CONTAINER].
                    add(Ints.consts.get(PropNames.BLOCK_PROGRESSION_DIMENSION));
        foPropertyLists[FObjectNames.INLINE_CONTAINER].
                        add(Ints.consts.get(PropNames.CLIP));
        foPropertyLists[FObjectNames.INLINE_CONTAINER].
                        add(Ints.consts.get(PropNames.DISPLAY_ALIGN));
        foPropertyLists[FObjectNames.INLINE_CONTAINER].
                        add(Ints.consts.get(PropNames.DOMINANT_BASELINE));
        foPropertyLists[FObjectNames.INLINE_CONTAINER].
                        add(Ints.consts.get(PropNames.HEIGHT));
        foPropertyLists[FObjectNames.INLINE_CONTAINER].
                        add(Ints.consts.get(PropNames.ID));
        foPropertyLists[FObjectNames.INLINE_CONTAINER].
                    add(Ints.consts.get(PropNames.INLINE_PROGRESSION_DIMENSION));
        foPropertyLists[FObjectNames.INLINE_CONTAINER].
                        add(Ints.consts.get(PropNames.KEEP_TOGETHER));
        foPropertyLists[FObjectNames.INLINE_CONTAINER].
                        add(Ints.consts.get(PropNames.KEEP_WITH_NEXT));
        foPropertyLists[FObjectNames.INLINE_CONTAINER].
                        add(Ints.consts.get(PropNames.KEEP_WITH_PREVIOUS));
        foPropertyLists[FObjectNames.INLINE_CONTAINER].
                        add(Ints.consts.get(PropNames.LINE_HEIGHT));
        foPropertyLists[FObjectNames.INLINE_CONTAINER].
                        add(Ints.consts.get(PropNames.OVERFLOW));
        foPropertyLists[FObjectNames.INLINE_CONTAINER].
                        add(Ints.consts.get(PropNames.REFERENCE_ORIENTATION));
        foPropertyLists[FObjectNames.INLINE_CONTAINER].
                        add(Ints.consts.get(PropNames.WIDTH));
        foPropertyLists[FObjectNames.INLINE_CONTAINER].
                        add(Ints.consts.get(PropNames.WRITING_MODE));


        //instream-foreign-object
        foPropertyLists[FObjectNames.INSTREAM_FOREIGN_OBJECT] = new HashSet(
                                PropertySets.accessibilityPropsSize +
                                PropertySets.auralPropsSize +
                                PropertySets.backgroundPropsSize +
                                PropertySets.borderPropsSize +
                                PropertySets.marginInlinePropsSize +
                                PropertySets.paddingPropsSize +
                                PropertySets.relativePositionPropsSize +
                                21);
        foPropertyLists[FObjectNames.INSTREAM_FOREIGN_OBJECT].
                            addAll(PropertySets.accessibilitySet);
        foPropertyLists[FObjectNames.INSTREAM_FOREIGN_OBJECT].
                            addAll(PropertySets.auralSet);
        foPropertyLists[FObjectNames.INSTREAM_FOREIGN_OBJECT].
                            addAll(PropertySets.backgroundSet);
        foPropertyLists[FObjectNames.INSTREAM_FOREIGN_OBJECT].
                            addAll(PropertySets.borderSet);
        foPropertyLists[FObjectNames.INSTREAM_FOREIGN_OBJECT].
                            addAll(PropertySets.marginInlineSet);
        foPropertyLists[FObjectNames.INSTREAM_FOREIGN_OBJECT].
                            addAll(PropertySets.paddingSet);
        foPropertyLists[FObjectNames.INSTREAM_FOREIGN_OBJECT].
                            addAll(PropertySets.relativePositionSet);
        foPropertyLists[FObjectNames.INSTREAM_FOREIGN_OBJECT].
                        add(Ints.consts.get(PropNames.ALIGNMENT_ADJUST));
        foPropertyLists[FObjectNames.INSTREAM_FOREIGN_OBJECT].
                        add(Ints.consts.get(PropNames.ALIGNMENT_BASELINE));
        foPropertyLists[FObjectNames.INSTREAM_FOREIGN_OBJECT].
                        add(Ints.consts.get(PropNames.BASELINE_SHIFT));
        foPropertyLists[FObjectNames.INSTREAM_FOREIGN_OBJECT].
                    add(Ints.consts.get(PropNames.BLOCK_PROGRESSION_DIMENSION));
        foPropertyLists[FObjectNames.INSTREAM_FOREIGN_OBJECT].
                        add(Ints.consts.get(PropNames.CLIP));
        foPropertyLists[FObjectNames.INSTREAM_FOREIGN_OBJECT].
                        add(Ints.consts.get(PropNames.CONTENT_HEIGHT));
        foPropertyLists[FObjectNames.INSTREAM_FOREIGN_OBJECT].
                        add(Ints.consts.get(PropNames.CONTENT_TYPE));
        foPropertyLists[FObjectNames.INSTREAM_FOREIGN_OBJECT].
                        add(Ints.consts.get(PropNames.CONTENT_WIDTH));
        foPropertyLists[FObjectNames.INSTREAM_FOREIGN_OBJECT].
                        add(Ints.consts.get(PropNames.DISPLAY_ALIGN));
        foPropertyLists[FObjectNames.INSTREAM_FOREIGN_OBJECT].
                        add(Ints.consts.get(PropNames.DOMINANT_BASELINE));
        foPropertyLists[FObjectNames.INSTREAM_FOREIGN_OBJECT].
                        add(Ints.consts.get(PropNames.HEIGHT));
        foPropertyLists[FObjectNames.INSTREAM_FOREIGN_OBJECT].
                        add(Ints.consts.get(PropNames.ID));
        foPropertyLists[FObjectNames.INSTREAM_FOREIGN_OBJECT].
                    add(Ints.consts.get(PropNames.INLINE_PROGRESSION_DIMENSION));
        foPropertyLists[FObjectNames.INSTREAM_FOREIGN_OBJECT].
                        add(Ints.consts.get(PropNames.KEEP_WITH_NEXT));
        foPropertyLists[FObjectNames.INSTREAM_FOREIGN_OBJECT].
                        add(Ints.consts.get(PropNames.KEEP_WITH_PREVIOUS));
        foPropertyLists[FObjectNames.INSTREAM_FOREIGN_OBJECT].
                        add(Ints.consts.get(PropNames.LINE_HEIGHT));
        foPropertyLists[FObjectNames.INSTREAM_FOREIGN_OBJECT].
                        add(Ints.consts.get(PropNames.OVERFLOW));
        foPropertyLists[FObjectNames.INSTREAM_FOREIGN_OBJECT].
                        add(Ints.consts.get(PropNames.SCALING));
        foPropertyLists[FObjectNames.INSTREAM_FOREIGN_OBJECT].
                        add(Ints.consts.get(PropNames.SCALING_METHOD));
        foPropertyLists[FObjectNames.INSTREAM_FOREIGN_OBJECT].
                        add(Ints.consts.get(PropNames.TEXT_ALIGN));
        foPropertyLists[FObjectNames.INSTREAM_FOREIGN_OBJECT].
                        add(Ints.consts.get(PropNames.WIDTH));

        //layout-master-set
        foPropertyLists[FObjectNames.LAYOUT_MASTER_SET] = null;

        //leader
        foPropertyLists[FObjectNames.LEADER] = new HashSet(
                                PropertySets.accessibilityPropsSize +
                                PropertySets.auralPropsSize +
                                PropertySets.backgroundPropsSize +
                                PropertySets.borderPropsSize +
                                PropertySets.fontPropsSize +
                                PropertySets.marginInlinePropsSize +
                                PropertySets.paddingPropsSize +
                                PropertySets.relativePositionPropsSize +
                                21);
        foPropertyLists[FObjectNames.LEADER].
                            addAll(PropertySets.accessibilitySet);
        foPropertyLists[FObjectNames.LEADER].
                            addAll(PropertySets.auralSet);
        foPropertyLists[FObjectNames.LEADER].
                            addAll(PropertySets.backgroundSet);
        foPropertyLists[FObjectNames.LEADER].
                            addAll(PropertySets.borderSet);
        foPropertyLists[FObjectNames.LEADER].
                            addAll(PropertySets.fontSet);
        foPropertyLists[FObjectNames.LEADER].
                            addAll(PropertySets.marginInlineSet);
        foPropertyLists[FObjectNames.LEADER].
                            addAll(PropertySets.paddingSet);
        foPropertyLists[FObjectNames.LEADER].
                            addAll(PropertySets.relativePositionSet);
        foPropertyLists[FObjectNames.LEADER].
                        add(Ints.consts.get(PropNames.ALIGNMENT_ADJUST));
        foPropertyLists[FObjectNames.LEADER].
                        add(Ints.consts.get(PropNames.ALIGNMENT_BASELINE));
        foPropertyLists[FObjectNames.LEADER].
                        add(Ints.consts.get(PropNames.BASELINE_SHIFT));
        foPropertyLists[FObjectNames.LEADER].
                        add(Ints.consts.get(PropNames.COLOR));
        foPropertyLists[FObjectNames.LEADER].
                        add(Ints.consts.get(PropNames.DOMINANT_BASELINE));
        foPropertyLists[FObjectNames.LEADER].
                        add(Ints.consts.get(PropNames.TEXT_DEPTH));
        foPropertyLists[FObjectNames.LEADER].
                        add(Ints.consts.get(PropNames.TEXT_ALTITUDE));
        foPropertyLists[FObjectNames.LEADER].
                        add(Ints.consts.get(PropNames.ID));
        foPropertyLists[FObjectNames.LEADER].
                        add(Ints.consts.get(PropNames.KEEP_WITH_NEXT));
        foPropertyLists[FObjectNames.LEADER].
                        add(Ints.consts.get(PropNames.KEEP_WITH_PREVIOUS));
        foPropertyLists[FObjectNames.LEADER].
                        add(Ints.consts.get(PropNames.LEADER_ALIGNMENT));
        foPropertyLists[FObjectNames.LEADER].
                        add(Ints.consts.get(PropNames.LEADER_LENGTH));
        foPropertyLists[FObjectNames.LEADER].
                        add(Ints.consts.get(PropNames.LEADER_PATTERN));
        foPropertyLists[FObjectNames.LEADER].
                        add(Ints.consts.get(PropNames.LEADER_PATTERN_WIDTH));
        foPropertyLists[FObjectNames.LEADER].
                        add(Ints.consts.get(PropNames.RULE_STYLE));
        foPropertyLists[FObjectNames.LEADER].
                        add(Ints.consts.get(PropNames.RULE_THICKNESS));
        foPropertyLists[FObjectNames.LEADER].
                        add(Ints.consts.get(PropNames.LETTER_SPACING));
        foPropertyLists[FObjectNames.LEADER].
                        add(Ints.consts.get(PropNames.LINE_HEIGHT));
        foPropertyLists[FObjectNames.LEADER].
                        add(Ints.consts.get(PropNames.TEXT_SHADOW));
        foPropertyLists[FObjectNames.LEADER].
                        add(Ints.consts.get(PropNames.VISIBILITY));
        foPropertyLists[FObjectNames.LEADER].
                        add(Ints.consts.get(PropNames.WORD_SPACING));

        //list-block
        foPropertyLists[FObjectNames.LIST_BLOCK] = new HashSet(
                                PropertySets.accessibilityPropsSize +
                                PropertySets.auralPropsSize +
                                PropertySets.backgroundPropsSize +
                                PropertySets.borderPropsSize +
                                PropertySets.marginBlockPropsSize +
                                PropertySets.paddingPropsSize +
                                PropertySets.relativePositionPropsSize +
                                9);
        foPropertyLists[FObjectNames.LIST_BLOCK].
                            addAll(PropertySets.accessibilitySet);
        foPropertyLists[FObjectNames.LIST_BLOCK].
                            addAll(PropertySets.auralSet);
        foPropertyLists[FObjectNames.LIST_BLOCK].
                            addAll(PropertySets.backgroundSet);
        foPropertyLists[FObjectNames.LIST_BLOCK].
                            addAll(PropertySets.borderSet);
        foPropertyLists[FObjectNames.LIST_BLOCK].
                            addAll(PropertySets.marginBlockSet);
        foPropertyLists[FObjectNames.LIST_BLOCK].
                            addAll(PropertySets.paddingSet);
        foPropertyLists[FObjectNames.LIST_BLOCK].
                            addAll(PropertySets.relativePositionSet);
        foPropertyLists[FObjectNames.LIST_BLOCK].
                        add(Ints.consts.get(PropNames.BREAK_AFTER));
        foPropertyLists[FObjectNames.LIST_BLOCK].
                        add(Ints.consts.get(PropNames.BREAK_BEFORE));
        foPropertyLists[FObjectNames.LIST_BLOCK].
                        add(Ints.consts.get(PropNames.ID));
        foPropertyLists[FObjectNames.LIST_BLOCK].
                        add(Ints.consts.get(PropNames.INTRUSION_DISPLACE));
        foPropertyLists[FObjectNames.LIST_BLOCK].
                        add(Ints.consts.get(PropNames.KEEP_TOGETHER));
        foPropertyLists[FObjectNames.LIST_BLOCK].
                        add(Ints.consts.get(PropNames.KEEP_WITH_NEXT));
        foPropertyLists[FObjectNames.LIST_BLOCK].
                        add(Ints.consts.get(PropNames.KEEP_WITH_PREVIOUS));
        foPropertyLists[FObjectNames.LIST_BLOCK].
            add(Ints.consts.get(PropNames.PROVISIONAL_DISTANCE_BETWEEN_STARTS));
        foPropertyLists[FObjectNames.LIST_BLOCK].
                    add(Ints.consts.get(PropNames.PROVISIONAL_LABEL_SEPARATION));

        //list-item
        foPropertyLists[FObjectNames.LIST_ITEM] = new HashSet(
                                PropertySets.accessibilityPropsSize +
                                PropertySets.auralPropsSize +
                                PropertySets.backgroundPropsSize +
                                PropertySets.borderPropsSize +
                                PropertySets.marginBlockPropsSize +
                                PropertySets.paddingPropsSize +
                                PropertySets.relativePositionPropsSize +
                                8);
        foPropertyLists[FObjectNames.LIST_ITEM].
                            addAll(PropertySets.accessibilitySet);
        foPropertyLists[FObjectNames.LIST_ITEM].
                            addAll(PropertySets.auralSet);
        foPropertyLists[FObjectNames.LIST_ITEM].
                            addAll(PropertySets.backgroundSet);
        foPropertyLists[FObjectNames.LIST_ITEM].
                            addAll(PropertySets.borderSet);
        foPropertyLists[FObjectNames.LIST_ITEM].
                            addAll(PropertySets.marginBlockSet);
        foPropertyLists[FObjectNames.LIST_ITEM].
                            addAll(PropertySets.paddingSet);
        foPropertyLists[FObjectNames.LIST_ITEM].
                            addAll(PropertySets.relativePositionSet);
        foPropertyLists[FObjectNames.LIST_ITEM].
                        add(Ints.consts.get(PropNames.BREAK_AFTER));
        foPropertyLists[FObjectNames.LIST_ITEM].
                        add(Ints.consts.get(PropNames.BREAK_BEFORE));
        foPropertyLists[FObjectNames.LIST_ITEM].
                        add(Ints.consts.get(PropNames.ID));
        foPropertyLists[FObjectNames.LIST_ITEM].
                        add(Ints.consts.get(PropNames.INTRUSION_DISPLACE));
        foPropertyLists[FObjectNames.LIST_ITEM].
                        add(Ints.consts.get(PropNames.KEEP_TOGETHER));
        foPropertyLists[FObjectNames.LIST_ITEM].
                        add(Ints.consts.get(PropNames.KEEP_WITH_NEXT));
        foPropertyLists[FObjectNames.LIST_ITEM].
                        add(Ints.consts.get(PropNames.KEEP_WITH_PREVIOUS));
        foPropertyLists[FObjectNames.LIST_ITEM].
                        add(Ints.consts.get(PropNames.RELATIVE_ALIGN));

        //list-item-body
        foPropertyLists[FObjectNames.LIST_ITEM_BODY] = new HashSet(
                                PropertySets.accessibilityPropsSize +
                                2);
        foPropertyLists[FObjectNames.LIST_ITEM_BODY].
                            addAll(PropertySets.accessibilitySet);
        foPropertyLists[FObjectNames.LIST_ITEM_BODY].
                        add(Ints.consts.get(PropNames.ID));
        foPropertyLists[FObjectNames.LIST_ITEM_BODY].
                        add(Ints.consts.get(PropNames.KEEP_TOGETHER));

        //list-item-label
        foPropertyLists[FObjectNames.LIST_ITEM_LABEL] = new HashSet(
                                PropertySets.accessibilityPropsSize +
                                2);
        foPropertyLists[FObjectNames.LIST_ITEM_LABEL].
                            addAll(PropertySets.accessibilitySet);
        foPropertyLists[FObjectNames.LIST_ITEM_LABEL].
                        add(Ints.consts.get(PropNames.ID));
        foPropertyLists[FObjectNames.LIST_ITEM_LABEL].
                        add(Ints.consts.get(PropNames.KEEP_TOGETHER));

        //marker
        foPropertyLists[FObjectNames.MARKER] = new HashSet(1);
        foPropertyLists[FObjectNames.MARKER].
                        add(Ints.consts.get(PropNames.MARKER_CLASS_NAME));

        //multi-case
        foPropertyLists[FObjectNames.MULTI_CASE] = new HashSet(
                                PropertySets.accessibilityPropsSize +
                                4);
        foPropertyLists[FObjectNames.MULTI_CASE].
                            addAll(PropertySets.accessibilitySet);
        foPropertyLists[FObjectNames.MULTI_CASE].
                        add(Ints.consts.get(PropNames.CASE_NAME));
        foPropertyLists[FObjectNames.MULTI_CASE].
                        add(Ints.consts.get(PropNames.CASE_TITLE));
        foPropertyLists[FObjectNames.MULTI_CASE].
                        add(Ints.consts.get(PropNames.ID));
        foPropertyLists[FObjectNames.MULTI_CASE].
                        add(Ints.consts.get(PropNames.STARTING_STATE));

        //multi-properties
        foPropertyLists[FObjectNames.MULTI_PROPERTIES]
                = new HashSet(FObjectNames.LAST_FO + 1);
        foPropertyLists[FObjectNames.MULTI_PROPERTIES] = new HashSet(
                                PropertySets.accessibilityPropsSize +
                                1);
        foPropertyLists[FObjectNames.MULTI_PROPERTIES].
                            addAll(PropertySets.accessibilitySet);
        foPropertyLists[FObjectNames.MULTI_PROPERTIES].
                        add(Ints.consts.get(PropNames.ID));

        //multi-property-set
        foPropertyLists[FObjectNames.MULTI_PROPERTY_SET] = new HashSet(2);
        foPropertyLists[FObjectNames.MULTI_PROPERTIES].
                        add(Ints.consts.get(PropNames.ACTIVE_STATE));
        foPropertyLists[FObjectNames.MULTI_PROPERTIES].
                        add(Ints.consts.get(PropNames.ID));

        //multi-switch
        foPropertyLists[FObjectNames.MULTI_SWITCH] = new HashSet(
                                PropertySets.accessibilityPropsSize +
                                2);
        foPropertyLists[FObjectNames.MULTI_SWITCH].
                            addAll(PropertySets.accessibilitySet);
        foPropertyLists[FObjectNames.MULTI_SWITCH].
                        add(Ints.consts.get(PropNames.AUTO_RESTORE));
        foPropertyLists[FObjectNames.MULTI_SWITCH].
                        add(Ints.consts.get(PropNames.ID));

        //multi-toggle
        foPropertyLists[FObjectNames.MULTI_TOGGLE] = new HashSet(
                                PropertySets.accessibilityPropsSize +
                                2);
        foPropertyLists[FObjectNames.MULTI_TOGGLE].
                            addAll(PropertySets.accessibilitySet);
        foPropertyLists[FObjectNames.MULTI_TOGGLE].
                        add(Ints.consts.get(PropNames.ID));
        foPropertyLists[FObjectNames.MULTI_TOGGLE].
                        add(Ints.consts.get(PropNames.SWITCH_TO));

        //page-number
        foPropertyLists[FObjectNames.PAGE_NUMBER] = new HashSet(
                                PropertySets.accessibilityPropsSize +
                                PropertySets.auralPropsSize +
                                PropertySets.backgroundPropsSize +
                                PropertySets.borderPropsSize +
                                PropertySets.fontPropsSize +
                                PropertySets.marginInlinePropsSize +
                                PropertySets.paddingPropsSize +
                                PropertySets.relativePositionPropsSize +
                                18);
        foPropertyLists[FObjectNames.PAGE_NUMBER].
                            addAll(PropertySets.accessibilitySet);
        foPropertyLists[FObjectNames.PAGE_NUMBER].
                            addAll(PropertySets.auralSet);
        foPropertyLists[FObjectNames.PAGE_NUMBER].
                            addAll(PropertySets.backgroundSet);
        foPropertyLists[FObjectNames.PAGE_NUMBER].
                            addAll(PropertySets.borderSet);
        foPropertyLists[FObjectNames.PAGE_NUMBER].
                            addAll(PropertySets.fontSet);
        foPropertyLists[FObjectNames.PAGE_NUMBER].
                            addAll(PropertySets.marginInlineSet);
        foPropertyLists[FObjectNames.PAGE_NUMBER].
                            addAll(PropertySets.paddingSet);
        foPropertyLists[FObjectNames.PAGE_NUMBER].
                            addAll(PropertySets.relativePositionSet);
        foPropertyLists[FObjectNames.PAGE_NUMBER].
                        add(Ints.consts.get(PropNames.ALIGNMENT_ADJUST));
        foPropertyLists[FObjectNames.PAGE_NUMBER].
                        add(Ints.consts.get(PropNames.ALIGNMENT_BASELINE));
        foPropertyLists[FObjectNames.PAGE_NUMBER].
                        add(Ints.consts.get(PropNames.BASELINE_SHIFT));
        foPropertyLists[FObjectNames.PAGE_NUMBER].
                        add(Ints.consts.get(PropNames.DOMINANT_BASELINE));
        foPropertyLists[FObjectNames.PAGE_NUMBER].
                        add(Ints.consts.get(PropNames.ID));
        foPropertyLists[FObjectNames.PAGE_NUMBER].
                        add(Ints.consts.get(PropNames.KEEP_WITH_NEXT));
        foPropertyLists[FObjectNames.PAGE_NUMBER].
                        add(Ints.consts.get(PropNames.KEEP_WITH_PREVIOUS));
        foPropertyLists[FObjectNames.PAGE_NUMBER].
                        add(Ints.consts.get(PropNames.LETTER_SPACING));
        foPropertyLists[FObjectNames.PAGE_NUMBER].
                        add(Ints.consts.get(PropNames.LINE_HEIGHT));
        foPropertyLists[FObjectNames.PAGE_NUMBER].
                        add(Ints.consts.get(PropNames.SCORE_SPACES));
        foPropertyLists[FObjectNames.PAGE_NUMBER].
                        add(Ints.consts.get(PropNames.TEXT_ALTITUDE));
        foPropertyLists[FObjectNames.PAGE_NUMBER].
                        add(Ints.consts.get(PropNames.TEXT_DECORATION));
        foPropertyLists[FObjectNames.PAGE_NUMBER].
                        add(Ints.consts.get(PropNames.TEXT_DEPTH));
        foPropertyLists[FObjectNames.PAGE_NUMBER].
                        add(Ints.consts.get(PropNames.TEXT_SHADOW));
        foPropertyLists[FObjectNames.PAGE_NUMBER].
                        add(Ints.consts.get(PropNames.TEXT_TRANSFORM));
        foPropertyLists[FObjectNames.PAGE_NUMBER].
                        add(Ints.consts.get(PropNames.VISIBILITY));
        foPropertyLists[FObjectNames.PAGE_NUMBER].
                        add(Ints.consts.get(PropNames.WORD_SPACING));
        foPropertyLists[FObjectNames.PAGE_NUMBER].
                        add(Ints.consts.get(PropNames.WRAP_OPTION));

        //page-number-citation
        foPropertyLists[FObjectNames.PAGE_NUMBER_CITATION] = new HashSet(
                                PropertySets.accessibilityPropsSize +
                                PropertySets.auralPropsSize +
                                PropertySets.backgroundPropsSize +
                                PropertySets.borderPropsSize +
                                PropertySets.fontPropsSize +
                                PropertySets.marginInlinePropsSize +
                                PropertySets.paddingPropsSize +
                                PropertySets.relativePositionPropsSize +
                                19);
        foPropertyLists[FObjectNames.PAGE_NUMBER_CITATION].
                            addAll(PropertySets.accessibilitySet);
        foPropertyLists[FObjectNames.PAGE_NUMBER_CITATION].
                            addAll(PropertySets.auralSet);
        foPropertyLists[FObjectNames.PAGE_NUMBER_CITATION].
                            addAll(PropertySets.backgroundSet);
        foPropertyLists[FObjectNames.PAGE_NUMBER_CITATION].
                            addAll(PropertySets.borderSet);
        foPropertyLists[FObjectNames.PAGE_NUMBER_CITATION].
                            addAll(PropertySets.fontSet);
        foPropertyLists[FObjectNames.PAGE_NUMBER_CITATION].
                            addAll(PropertySets.marginInlineSet);
        foPropertyLists[FObjectNames.PAGE_NUMBER_CITATION].
                            addAll(PropertySets.paddingSet);
        foPropertyLists[FObjectNames.PAGE_NUMBER_CITATION].
                            addAll(PropertySets.relativePositionSet);
        foPropertyLists[FObjectNames.PAGE_NUMBER_CITATION].
                        add(Ints.consts.get(PropNames.ALIGNMENT_ADJUST));
        foPropertyLists[FObjectNames.PAGE_NUMBER_CITATION].
                        add(Ints.consts.get(PropNames.ALIGNMENT_BASELINE));
        foPropertyLists[FObjectNames.PAGE_NUMBER_CITATION].
                        add(Ints.consts.get(PropNames.BASELINE_SHIFT));
        foPropertyLists[FObjectNames.PAGE_NUMBER_CITATION].
                        add(Ints.consts.get(PropNames.DOMINANT_BASELINE));
        foPropertyLists[FObjectNames.PAGE_NUMBER_CITATION].
                        add(Ints.consts.get(PropNames.ID));
        foPropertyLists[FObjectNames.PAGE_NUMBER_CITATION].
                        add(Ints.consts.get(PropNames.KEEP_WITH_NEXT));
        foPropertyLists[FObjectNames.PAGE_NUMBER_CITATION].
                        add(Ints.consts.get(PropNames.KEEP_WITH_PREVIOUS));
        foPropertyLists[FObjectNames.PAGE_NUMBER_CITATION].
                        add(Ints.consts.get(PropNames.LETTER_SPACING));
        foPropertyLists[FObjectNames.PAGE_NUMBER_CITATION].
                        add(Ints.consts.get(PropNames.LINE_HEIGHT));
        foPropertyLists[FObjectNames.PAGE_NUMBER_CITATION].
                        add(Ints.consts.get(PropNames.REF_ID));
        foPropertyLists[FObjectNames.PAGE_NUMBER_CITATION].
                        add(Ints.consts.get(PropNames.SCORE_SPACES));
        foPropertyLists[FObjectNames.PAGE_NUMBER_CITATION].
                        add(Ints.consts.get(PropNames.TEXT_ALTITUDE));
        foPropertyLists[FObjectNames.PAGE_NUMBER_CITATION].
                        add(Ints.consts.get(PropNames.TEXT_DECORATION));
        foPropertyLists[FObjectNames.PAGE_NUMBER_CITATION].
                        add(Ints.consts.get(PropNames.TEXT_DEPTH));
        foPropertyLists[FObjectNames.PAGE_NUMBER_CITATION].
                        add(Ints.consts.get(PropNames.TEXT_SHADOW));
        foPropertyLists[FObjectNames.PAGE_NUMBER_CITATION].
                        add(Ints.consts.get(PropNames.TEXT_TRANSFORM));
        foPropertyLists[FObjectNames.PAGE_NUMBER_CITATION].
                        add(Ints.consts.get(PropNames.VISIBILITY));
        foPropertyLists[FObjectNames.PAGE_NUMBER_CITATION].
                        add(Ints.consts.get(PropNames.WORD_SPACING));
        foPropertyLists[FObjectNames.PAGE_NUMBER_CITATION].
                        add(Ints.consts.get(PropNames.WRAP_OPTION));

        //page-sequence
        foPropertyLists[FObjectNames.PAGE_SEQUENCE] = new HashSet(10);
        foPropertyLists[FObjectNames.PAGE_SEQUENCE].
                        add(Ints.consts.get(PropNames.COUNTRY));
        foPropertyLists[FObjectNames.PAGE_SEQUENCE].
                        add(Ints.consts.get(PropNames.FORMAT));
        foPropertyLists[FObjectNames.PAGE_SEQUENCE].
                        add(Ints.consts.get(PropNames.LANGUAGE));
        foPropertyLists[FObjectNames.PAGE_SEQUENCE].
                        add(Ints.consts.get(PropNames.LETTER_VALUE));
        foPropertyLists[FObjectNames.PAGE_SEQUENCE].
                        add(Ints.consts.get(PropNames.GROUPING_SEPARATOR));
        foPropertyLists[FObjectNames.PAGE_SEQUENCE].
                        add(Ints.consts.get(PropNames.GROUPING_SIZE));
        foPropertyLists[FObjectNames.PAGE_SEQUENCE].
                        add(Ints.consts.get(PropNames.ID));
        foPropertyLists[FObjectNames.PAGE_SEQUENCE].
                        add(Ints.consts.get(PropNames.INITIAL_PAGE_NUMBER));
        foPropertyLists[FObjectNames.PAGE_SEQUENCE].
                        add(Ints.consts.get(PropNames.FORCE_PAGE_COUNT));
        foPropertyLists[FObjectNames.PAGE_SEQUENCE].
                        add(Ints.consts.get(PropNames.MASTER_REFERENCE));

        //page-sequence-master
        foPropertyLists[FObjectNames.PAGE_SEQUENCE_MASTER] = new HashSet(1);
        foPropertyLists[FObjectNames.PAGE_SEQUENCE_MASTER].
                        add(Ints.consts.get(PropNames.MASTER_NAME));
        
        //region-after
        foPropertyLists[FObjectNames.REGION_AFTER] = new HashSet(
                                PropertySets.backgroundPropsSize +
                                PropertySets.borderPropsSize +
                                PropertySets.paddingPropsSize +
                                8);
        foPropertyLists[FObjectNames.REGION_AFTER].
                            addAll(PropertySets.backgroundSet);
        foPropertyLists[FObjectNames.REGION_AFTER].
                            addAll(PropertySets.borderSet);
        foPropertyLists[FObjectNames.REGION_AFTER].
                            addAll(PropertySets.paddingSet);
        foPropertyLists[FObjectNames.REGION_AFTER].
                        add(Ints.consts.get(PropNames.CLIP));
        foPropertyLists[FObjectNames.REGION_AFTER].
                        add(Ints.consts.get(PropNames.DISPLAY_ALIGN));
        foPropertyLists[FObjectNames.REGION_AFTER].
                        add(Ints.consts.get(PropNames.EXTENT));
        foPropertyLists[FObjectNames.REGION_AFTER].
                        add(Ints.consts.get(PropNames.OVERFLOW));
        foPropertyLists[FObjectNames.REGION_AFTER].
                        add(Ints.consts.get(PropNames.PRECEDENCE));
        foPropertyLists[FObjectNames.REGION_AFTER].
                        add(Ints.consts.get(PropNames.REGION_NAME));
        foPropertyLists[FObjectNames.REGION_AFTER].
                        add(Ints.consts.get(PropNames.REFERENCE_ORIENTATION));
        foPropertyLists[FObjectNames.REGION_AFTER].
                        add(Ints.consts.get(PropNames.WRITING_MODE));

        //region-before
        foPropertyLists[FObjectNames.REGION_BEFORE] = new HashSet(
                                PropertySets.backgroundPropsSize +
                                PropertySets.borderPropsSize +
                                PropertySets.paddingPropsSize +
                                8);
        foPropertyLists[FObjectNames.REGION_BEFORE].
                            addAll(PropertySets.backgroundSet);
        foPropertyLists[FObjectNames.REGION_BEFORE].
                            addAll(PropertySets.borderSet);
        foPropertyLists[FObjectNames.REGION_BEFORE].
                            addAll(PropertySets.paddingSet);
        foPropertyLists[FObjectNames.REGION_BEFORE].
                        add(Ints.consts.get(PropNames.CLIP));
        foPropertyLists[FObjectNames.REGION_BEFORE].
                        add(Ints.consts.get(PropNames.DISPLAY_ALIGN));
        foPropertyLists[FObjectNames.REGION_BEFORE].
                        add(Ints.consts.get(PropNames.EXTENT));
        foPropertyLists[FObjectNames.REGION_BEFORE].
                        add(Ints.consts.get(PropNames.OVERFLOW));
        foPropertyLists[FObjectNames.REGION_BEFORE].
                        add(Ints.consts.get(PropNames.PRECEDENCE));
        foPropertyLists[FObjectNames.REGION_BEFORE].
                        add(Ints.consts.get(PropNames.REGION_NAME));
        foPropertyLists[FObjectNames.REGION_BEFORE].
                        add(Ints.consts.get(PropNames.REFERENCE_ORIENTATION));
        foPropertyLists[FObjectNames.REGION_BEFORE].
                        add(Ints.consts.get(PropNames.WRITING_MODE));

        //region-body
        foPropertyLists[FObjectNames.REGION_BODY] = new HashSet(
                                PropertySets.backgroundPropsSize +
                                PropertySets.borderPropsSize +
                                PropertySets.paddingPropsSize +
                                PropertySets.marginBlockPropsSize +
                                8);
        foPropertyLists[FObjectNames.REGION_BODY].
                            addAll(PropertySets.backgroundSet);
        foPropertyLists[FObjectNames.REGION_BODY].
                            addAll(PropertySets.borderSet);
        foPropertyLists[FObjectNames.REGION_BODY].
                            addAll(PropertySets.paddingSet);
        foPropertyLists[FObjectNames.REGION_BODY].
                            addAll(PropertySets.marginBlockSet);
        foPropertyLists[FObjectNames.REGION_BODY].
                        add(Ints.consts.get(PropNames.CLIP));
        foPropertyLists[FObjectNames.REGION_BODY].
                        add(Ints.consts.get(PropNames.COLUMN_COUNT));
        foPropertyLists[FObjectNames.REGION_BODY].
                        add(Ints.consts.get(PropNames.COLUMN_GAP));
        foPropertyLists[FObjectNames.REGION_BODY].
                        add(Ints.consts.get(PropNames.DISPLAY_ALIGN));
        foPropertyLists[FObjectNames.REGION_BODY].
                        add(Ints.consts.get(PropNames.OVERFLOW));
        foPropertyLists[FObjectNames.REGION_BODY].
                        add(Ints.consts.get(PropNames.REGION_NAME));
        foPropertyLists[FObjectNames.REGION_BODY].
                        add(Ints.consts.get(PropNames.REFERENCE_ORIENTATION));
        foPropertyLists[FObjectNames.REGION_BODY].
                        add(Ints.consts.get(PropNames.WRITING_MODE));

        //region-end
        foPropertyLists[FObjectNames.REGION_END] = new HashSet(
                                PropertySets.backgroundPropsSize +
                                PropertySets.borderPropsSize +
                                PropertySets.paddingPropsSize +
                                7);
        foPropertyLists[FObjectNames.REGION_END].
                            addAll(PropertySets.backgroundSet);
        foPropertyLists[FObjectNames.REGION_END].
                            addAll(PropertySets.borderSet);
        foPropertyLists[FObjectNames.REGION_END].
                            addAll(PropertySets.paddingSet);
        foPropertyLists[FObjectNames.REGION_END].
                        add(Ints.consts.get(PropNames.CLIP));
        foPropertyLists[FObjectNames.REGION_END].
                        add(Ints.consts.get(PropNames.DISPLAY_ALIGN));
        foPropertyLists[FObjectNames.REGION_END].
                        add(Ints.consts.get(PropNames.EXTENT));
        foPropertyLists[FObjectNames.REGION_END].
                        add(Ints.consts.get(PropNames.OVERFLOW));
        foPropertyLists[FObjectNames.REGION_END].
                        add(Ints.consts.get(PropNames.REGION_NAME));
        foPropertyLists[FObjectNames.REGION_END].
                        add(Ints.consts.get(PropNames.REFERENCE_ORIENTATION));
        foPropertyLists[FObjectNames.REGION_END].
                        add(Ints.consts.get(PropNames.WRITING_MODE));

        //region-start
        foPropertyLists[FObjectNames.REGION_START] = new HashSet(
                                PropertySets.backgroundPropsSize +
                                PropertySets.borderPropsSize +
                                PropertySets.paddingPropsSize +
                                7);
        foPropertyLists[FObjectNames.REGION_START].
                            addAll(PropertySets.backgroundSet);
        foPropertyLists[FObjectNames.REGION_START].
                            addAll(PropertySets.borderSet);
        foPropertyLists[FObjectNames.REGION_START].
                            addAll(PropertySets.paddingSet);
        foPropertyLists[FObjectNames.REGION_START].
                        add(Ints.consts.get(PropNames.CLIP));
        foPropertyLists[FObjectNames.REGION_START].
                        add(Ints.consts.get(PropNames.DISPLAY_ALIGN));
        foPropertyLists[FObjectNames.REGION_START].
                        add(Ints.consts.get(PropNames.EXTENT));
        foPropertyLists[FObjectNames.REGION_START].
                        add(Ints.consts.get(PropNames.OVERFLOW));
        foPropertyLists[FObjectNames.REGION_START].
                        add(Ints.consts.get(PropNames.REGION_NAME));
        foPropertyLists[FObjectNames.REGION_START].
                        add(Ints.consts.get(PropNames.REFERENCE_ORIENTATION));
        foPropertyLists[FObjectNames.REGION_START].
                        add(Ints.consts.get(PropNames.WRITING_MODE));

        //repeatable-page-master-alternatives
        foPropertyLists[FObjectNames.REPEATABLE_PAGE_MASTER_ALTERNATIVES]
                = new HashSet(1);
        foPropertyLists[FObjectNames.REPEATABLE_PAGE_MASTER_ALTERNATIVES].
                        add(Ints.consts.get(PropNames.MAXIMUM_REPEATS));
        
        //repeatable-page-master-reference
        foPropertyLists[FObjectNames.REPEATABLE_PAGE_MASTER_REFERENCE]
                = new HashSet(2);
        foPropertyLists[FObjectNames.REPEATABLE_PAGE_MASTER_REFERENCE].
                        add(Ints.consts.get(PropNames.MASTER_REFERENCE));
        foPropertyLists[FObjectNames.REPEATABLE_PAGE_MASTER_REFERENCE].
                        add(Ints.consts.get(PropNames.MAXIMUM_REPEATS));

        //retrieve-marker
        foPropertyLists[FObjectNames.RETRIEVE_MARKER] = new HashSet(3);
        foPropertyLists[FObjectNames.RETRIEVE_MARKER].
                        add(Ints.consts.get(PropNames.RETRIEVE_BOUNDARY));
        foPropertyLists[FObjectNames.RETRIEVE_MARKER].
                        add(Ints.consts.get(PropNames.RETRIEVE_CLASS_NAME));
        foPropertyLists[FObjectNames.RETRIEVE_MARKER].
                        add(Ints.consts.get(PropNames.RETRIEVE_POSITION));

        //root
        foPropertyLists[FObjectNames.ROOT] = new HashSet(1);
        foPropertyLists[FObjectNames.ROOT].
                        add(Ints.consts.get(PropNames.MEDIA_USAGE));

        //simple-page-master
        foPropertyLists[FObjectNames.SIMPLE_PAGE_MASTER] = new HashSet(
                                PropertySets.marginBlockPropsSize +
                                5);
        foPropertyLists[FObjectNames.SIMPLE_PAGE_MASTER].
                            addAll(PropertySets.marginBlockSet);
        foPropertyLists[FObjectNames.SIMPLE_PAGE_MASTER].
                        add(Ints.consts.get(PropNames.MASTER_NAME));
        foPropertyLists[FObjectNames.SIMPLE_PAGE_MASTER].
                        add(Ints.consts.get(PropNames.PAGE_HEIGHT));
        foPropertyLists[FObjectNames.SIMPLE_PAGE_MASTER].
                        add(Ints.consts.get(PropNames.PAGE_WIDTH));
        foPropertyLists[FObjectNames.SIMPLE_PAGE_MASTER].
                        add(Ints.consts.get(PropNames.REFERENCE_ORIENTATION));
        foPropertyLists[FObjectNames.SIMPLE_PAGE_MASTER].
                        add(Ints.consts.get(PropNames.WRITING_MODE));
        
        //single-page-master-reference
        foPropertyLists[FObjectNames.SINGLE_PAGE_MASTER_REFERENCE]
                = new HashSet(1);
        foPropertyLists[FObjectNames.SINGLE_PAGE_MASTER_REFERENCE].
                        add(Ints.consts.get(PropNames.MASTER_REFERENCE));

        //static-content
        foPropertyLists[FObjectNames.STATIC_CONTENT] = new HashSet(1);
        foPropertyLists[FObjectNames.STATIC_CONTENT].
                        add(Ints.consts.get(PropNames.FLOW_NAME));

        //table
        foPropertyLists[FObjectNames.TABLE] = new HashSet(
                                PropertySets.accessibilityPropsSize +
                                PropertySets.auralPropsSize +
                                PropertySets.backgroundPropsSize +
                                PropertySets.borderPropsSize +
                                PropertySets.marginBlockPropsSize +
                                PropertySets.paddingPropsSize +
                                PropertySets.relativePositionPropsSize +
                                21);
        foPropertyLists[FObjectNames.TABLE].
                            addAll(PropertySets.accessibilitySet);
        foPropertyLists[FObjectNames.TABLE].
                            addAll(PropertySets.auralSet);
        foPropertyLists[FObjectNames.TABLE].
                            addAll(PropertySets.backgroundSet);
        foPropertyLists[FObjectNames.TABLE].
                            addAll(PropertySets.borderSet);
        foPropertyLists[FObjectNames.TABLE].
                            addAll(PropertySets.marginBlockSet);
        foPropertyLists[FObjectNames.TABLE].
                            addAll(PropertySets.paddingSet);
        foPropertyLists[FObjectNames.TABLE].
                            addAll(PropertySets.relativePositionSet);
        foPropertyLists[FObjectNames.TABLE].
                add(Ints.consts.get(PropNames.BLOCK_PROGRESSION_DIMENSION));
        foPropertyLists[FObjectNames.TABLE].
                    add(Ints.consts.get(PropNames.BORDER_AFTER_PRECEDENCE));
        foPropertyLists[FObjectNames.TABLE].
                    add(Ints.consts.get(PropNames.BORDER_BEFORE_PRECEDENCE));
        foPropertyLists[FObjectNames.TABLE].
                        add(Ints.consts.get(PropNames.BORDER_COLLAPSE));
        foPropertyLists[FObjectNames.TABLE].
                        add(Ints.consts.get(PropNames.BORDER_END_PRECEDENCE));
        foPropertyLists[FObjectNames.TABLE].
                        add(Ints.consts.get(PropNames.BORDER_SEPARATION));
        foPropertyLists[FObjectNames.TABLE].
                    add(Ints.consts.get(PropNames.BORDER_START_PRECEDENCE));
        foPropertyLists[FObjectNames.TABLE].
                        add(Ints.consts.get(PropNames.BREAK_AFTER));
        foPropertyLists[FObjectNames.TABLE].
                        add(Ints.consts.get(PropNames.BREAK_BEFORE));
        foPropertyLists[FObjectNames.TABLE].
                        add(Ints.consts.get(PropNames.ID));
        foPropertyLists[FObjectNames.TABLE].
                add(Ints.consts.get(PropNames.INLINE_PROGRESSION_DIMENSION));
        foPropertyLists[FObjectNames.TABLE].
                        add(Ints.consts.get(PropNames.INTRUSION_DISPLACE));
        foPropertyLists[FObjectNames.TABLE].
                        add(Ints.consts.get(PropNames.HEIGHT));
        foPropertyLists[FObjectNames.TABLE].
                        add(Ints.consts.get(PropNames.KEEP_TOGETHER));
        foPropertyLists[FObjectNames.TABLE].
                        add(Ints.consts.get(PropNames.KEEP_WITH_NEXT));
        foPropertyLists[FObjectNames.TABLE].
                        add(Ints.consts.get(PropNames.KEEP_WITH_PREVIOUS));
        foPropertyLists[FObjectNames.TABLE].
                        add(Ints.consts.get(PropNames.TABLE_LAYOUT));
        foPropertyLists[FObjectNames.TABLE].
                    add(Ints.consts.get(PropNames.TABLE_OMIT_FOOTER_AT_BREAK));
        foPropertyLists[FObjectNames.TABLE].
                    add(Ints.consts.get(PropNames.TABLE_OMIT_HEADER_AT_BREAK));
        foPropertyLists[FObjectNames.TABLE].
                        add(Ints.consts.get(PropNames.WIDTH));
        foPropertyLists[FObjectNames.TABLE].
                        add(Ints.consts.get(PropNames.WRITING_MODE));

        //table-and-caption
        foPropertyLists[FObjectNames.TABLE_AND_CAPTION] = new HashSet(
                                PropertySets.accessibilityPropsSize +
                                PropertySets.auralPropsSize +
                                PropertySets.backgroundPropsSize +
                                PropertySets.borderPropsSize +
                                PropertySets.marginBlockPropsSize +
                                PropertySets.paddingPropsSize +
                                PropertySets.relativePositionPropsSize +
                                9);
        foPropertyLists[FObjectNames.TABLE_AND_CAPTION].
                            addAll(PropertySets.accessibilitySet);
        foPropertyLists[FObjectNames.TABLE_AND_CAPTION].
                            addAll(PropertySets.auralSet);
        foPropertyLists[FObjectNames.TABLE_AND_CAPTION].
                            addAll(PropertySets.backgroundSet);
        foPropertyLists[FObjectNames.TABLE_AND_CAPTION].
                            addAll(PropertySets.borderSet);
        foPropertyLists[FObjectNames.TABLE_AND_CAPTION].
                            addAll(PropertySets.marginBlockSet);
        foPropertyLists[FObjectNames.TABLE_AND_CAPTION].
                            addAll(PropertySets.paddingSet);
        foPropertyLists[FObjectNames.TABLE_AND_CAPTION].
                            addAll(PropertySets.relativePositionSet);
        foPropertyLists[FObjectNames.TABLE_AND_CAPTION].
                        add(Ints.consts.get(PropNames.BREAK_AFTER));
        foPropertyLists[FObjectNames.TABLE_AND_CAPTION].
                        add(Ints.consts.get(PropNames.BREAK_BEFORE));
        foPropertyLists[FObjectNames.TABLE_AND_CAPTION].
                        add(Ints.consts.get(PropNames.CAPTION_SIDE));
        foPropertyLists[FObjectNames.TABLE_AND_CAPTION].
                        add(Ints.consts.get(PropNames.ID));
        foPropertyLists[FObjectNames.TABLE_AND_CAPTION].
                        add(Ints.consts.get(PropNames.INTRUSION_DISPLACE));
        foPropertyLists[FObjectNames.TABLE_AND_CAPTION].
                        add(Ints.consts.get(PropNames.KEEP_TOGETHER));
        foPropertyLists[FObjectNames.TABLE_AND_CAPTION].
                        add(Ints.consts.get(PropNames.KEEP_WITH_NEXT));
        foPropertyLists[FObjectNames.TABLE_AND_CAPTION].
                        add(Ints.consts.get(PropNames.KEEP_WITH_PREVIOUS));
        foPropertyLists[FObjectNames.TABLE_AND_CAPTION].
                        add(Ints.consts.get(PropNames.TEXT_ALIGN));

        //table-body
        foPropertyLists[FObjectNames.TABLE_BODY] = new HashSet(
                                PropertySets.accessibilityPropsSize +
                                PropertySets.auralPropsSize +
                                PropertySets.backgroundPropsSize +
                                PropertySets.borderPropsSize +
                                PropertySets.relativePositionPropsSize +
                                6);
        foPropertyLists[FObjectNames.TABLE_BODY].
                            addAll(PropertySets.accessibilitySet);
        foPropertyLists[FObjectNames.TABLE_BODY].
                            addAll(PropertySets.auralSet);
        foPropertyLists[FObjectNames.TABLE_BODY].
                            addAll(PropertySets.backgroundSet);
        foPropertyLists[FObjectNames.TABLE_BODY].
                            addAll(PropertySets.borderSet);
        foPropertyLists[FObjectNames.TABLE_BODY].
                            addAll(PropertySets.relativePositionSet);
        foPropertyLists[FObjectNames.TABLE_BODY].
                    add(Ints.consts.get(PropNames.BORDER_AFTER_PRECEDENCE));
        foPropertyLists[FObjectNames.TABLE_BODY].
                    add(Ints.consts.get(PropNames.BORDER_BEFORE_PRECEDENCE));
        foPropertyLists[FObjectNames.TABLE_BODY].
                        add(Ints.consts.get(PropNames.BORDER_END_PRECEDENCE));
        foPropertyLists[FObjectNames.TABLE_BODY].
                    add(Ints.consts.get(PropNames.BORDER_START_PRECEDENCE));
        foPropertyLists[FObjectNames.TABLE_BODY].
                        add(Ints.consts.get(PropNames.ID));
        foPropertyLists[FObjectNames.TABLE_BODY].
                        add(Ints.consts.get(PropNames.VISIBILITY));

        //table-caption
        foPropertyLists[FObjectNames.TABLE_CAPTION] = new HashSet(
                                PropertySets.accessibilityPropsSize +
                                PropertySets.auralPropsSize +
                                PropertySets.backgroundPropsSize +
                                PropertySets.borderPropsSize +
                                PropertySets.paddingPropsSize +
                                PropertySets.relativePositionPropsSize +
                                7);
        foPropertyLists[FObjectNames.TABLE_CAPTION].
                            addAll(PropertySets.accessibilitySet);
        foPropertyLists[FObjectNames.TABLE_CAPTION].
                            addAll(PropertySets.auralSet);
        foPropertyLists[FObjectNames.TABLE_CAPTION].
                            addAll(PropertySets.backgroundSet);
        foPropertyLists[FObjectNames.TABLE_CAPTION].
                            addAll(PropertySets.borderSet);
        foPropertyLists[FObjectNames.TABLE_CAPTION].
                            addAll(PropertySets.paddingSet);
        foPropertyLists[FObjectNames.TABLE_CAPTION].
                            addAll(PropertySets.relativePositionSet);
        foPropertyLists[FObjectNames.TABLE_CAPTION].
                add(Ints.consts.get(PropNames.BLOCK_PROGRESSION_DIMENSION));
        foPropertyLists[FObjectNames.TABLE_CAPTION].
                        add(Ints.consts.get(PropNames.HEIGHT));
        foPropertyLists[FObjectNames.TABLE_CAPTION].
                        add(Ints.consts.get(PropNames.ID));
        foPropertyLists[FObjectNames.TABLE_CAPTION].
                add(Ints.consts.get(PropNames.INLINE_PROGRESSION_DIMENSION));
        foPropertyLists[FObjectNames.TABLE_CAPTION].
                        add(Ints.consts.get(PropNames.INTRUSION_DISPLACE));
        foPropertyLists[FObjectNames.TABLE_CAPTION].
                        add(Ints.consts.get(PropNames.KEEP_TOGETHER));
        foPropertyLists[FObjectNames.TABLE_CAPTION].
                        add(Ints.consts.get(PropNames.WIDTH));

        //table-cell
        foPropertyLists[FObjectNames.TABLE_CELL]
                = new HashSet(FObjectNames.LAST_FO + 1);
        foPropertyLists[FObjectNames.TABLE_CELL] = new HashSet(
                                PropertySets.accessibilityPropsSize +
                                PropertySets.auralPropsSize +
                                PropertySets.backgroundPropsSize +
                                PropertySets.borderPropsSize +
                                PropertySets.paddingPropsSize +
                                PropertySets.relativePositionPropsSize +
                                17);
        foPropertyLists[FObjectNames.TABLE_CELL].
                            addAll(PropertySets.accessibilitySet);
        foPropertyLists[FObjectNames.TABLE_CELL].
                            addAll(PropertySets.auralSet);
        foPropertyLists[FObjectNames.TABLE_CELL].
                            addAll(PropertySets.backgroundSet);
        foPropertyLists[FObjectNames.TABLE_CELL].
                            addAll(PropertySets.borderSet);
        foPropertyLists[FObjectNames.TABLE_CELL].
                            addAll(PropertySets.paddingSet);
        foPropertyLists[FObjectNames.TABLE_CELL].
                            addAll(PropertySets.relativePositionSet);
        foPropertyLists[FObjectNames.TABLE_CELL].
                    add(Ints.consts.get(PropNames.BORDER_AFTER_PRECEDENCE));
        foPropertyLists[FObjectNames.TABLE_CELL].
                    add(Ints.consts.get(PropNames.BORDER_BEFORE_PRECEDENCE));
        foPropertyLists[FObjectNames.TABLE_CELL].
                        add(Ints.consts.get(PropNames.BORDER_END_PRECEDENCE));
        foPropertyLists[FObjectNames.TABLE_CELL].
                    add(Ints.consts.get(PropNames.BORDER_START_PRECEDENCE));
        foPropertyLists[FObjectNames.TABLE_CELL].
                add(Ints.consts.get(PropNames.BLOCK_PROGRESSION_DIMENSION));
        foPropertyLists[FObjectNames.TABLE_CELL].
                        add(Ints.consts.get(PropNames.COLUMN_NUMBER));
        foPropertyLists[FObjectNames.TABLE_CELL].
                        add(Ints.consts.get(PropNames.DISPLAY_ALIGN));
        foPropertyLists[FObjectNames.TABLE_CELL].
                        add(Ints.consts.get(PropNames.RELATIVE_ALIGN));
        foPropertyLists[FObjectNames.TABLE_CELL].
                        add(Ints.consts.get(PropNames.EMPTY_CELLS));
        foPropertyLists[FObjectNames.TABLE_CELL].
                        add(Ints.consts.get(PropNames.ENDS_ROW));
        foPropertyLists[FObjectNames.TABLE_CELL].
                        add(Ints.consts.get(PropNames.HEIGHT));
        foPropertyLists[FObjectNames.TABLE_CELL].
                        add(Ints.consts.get(PropNames.ID));
        foPropertyLists[FObjectNames.TABLE_CELL].
                add(Ints.consts.get(PropNames.INLINE_PROGRESSION_DIMENSION));
        foPropertyLists[FObjectNames.TABLE_CELL].
                    add(Ints.consts.get(PropNames.NUMBER_COLUMNS_SPANNED));
        foPropertyLists[FObjectNames.TABLE_CELL].
                        add(Ints.consts.get(PropNames.NUMBER_ROWS_SPANNED));
        foPropertyLists[FObjectNames.TABLE_CELL].
                        add(Ints.consts.get(PropNames.STARTS_ROW));
        foPropertyLists[FObjectNames.TABLE_CELL].
                        add(Ints.consts.get(PropNames.WIDTH));

        //table-column
        foPropertyLists[FObjectNames.TABLE_COLUMN] = new HashSet(
                                PropertySets.backgroundPropsSize +
                                PropertySets.borderPropsSize +
                                9);
        foPropertyLists[FObjectNames.TABLE_COLUMN].
                            addAll(PropertySets.backgroundSet);
        foPropertyLists[FObjectNames.TABLE_COLUMN].
                            addAll(PropertySets.borderSet);
        foPropertyLists[FObjectNames.TABLE_COLUMN].
                    add(Ints.consts.get(PropNames.BORDER_AFTER_PRECEDENCE));
        foPropertyLists[FObjectNames.TABLE_COLUMN].
                    add(Ints.consts.get(PropNames.BORDER_BEFORE_PRECEDENCE));
        foPropertyLists[FObjectNames.TABLE_COLUMN].
                        add(Ints.consts.get(PropNames.BORDER_END_PRECEDENCE));
        foPropertyLists[FObjectNames.TABLE_COLUMN].
                    add(Ints.consts.get(PropNames.BORDER_START_PRECEDENCE));
        foPropertyLists[FObjectNames.TABLE_COLUMN].
                        add(Ints.consts.get(PropNames.COLUMN_NUMBER));
        foPropertyLists[FObjectNames.TABLE_COLUMN].
                        add(Ints.consts.get(PropNames.COLUMN_WIDTH));
        foPropertyLists[FObjectNames.TABLE_COLUMN].
                    add(Ints.consts.get(PropNames.NUMBER_COLUMNS_REPEATED));
        foPropertyLists[FObjectNames.TABLE_COLUMN].
                    add(Ints.consts.get(PropNames.NUMBER_COLUMNS_SPANNED));
        foPropertyLists[FObjectNames.TABLE_COLUMN].
                        add(Ints.consts.get(PropNames.VISIBILITY));

        //table-footer
        foPropertyLists[FObjectNames.TABLE_FOOTER] = new HashSet(
                                PropertySets.accessibilityPropsSize +
                                PropertySets.auralPropsSize +
                                PropertySets.backgroundPropsSize +
                                PropertySets.borderPropsSize +
                                PropertySets.relativePositionPropsSize +
                                6);
        foPropertyLists[FObjectNames.TABLE_FOOTER].
                            addAll(PropertySets.accessibilitySet);
        foPropertyLists[FObjectNames.TABLE_FOOTER].
                            addAll(PropertySets.auralSet);
        foPropertyLists[FObjectNames.TABLE_FOOTER].
                            addAll(PropertySets.backgroundSet);
        foPropertyLists[FObjectNames.TABLE_FOOTER].
                            addAll(PropertySets.borderSet);
        foPropertyLists[FObjectNames.TABLE_FOOTER].
                            addAll(PropertySets.relativePositionSet);
        foPropertyLists[FObjectNames.TABLE_FOOTER].
                    add(Ints.consts.get(PropNames.BORDER_AFTER_PRECEDENCE));
        foPropertyLists[FObjectNames.TABLE_FOOTER].
                    add(Ints.consts.get(PropNames.BORDER_BEFORE_PRECEDENCE));
        foPropertyLists[FObjectNames.TABLE_FOOTER].
                        add(Ints.consts.get(PropNames.BORDER_END_PRECEDENCE));
        foPropertyLists[FObjectNames.TABLE_FOOTER].
                    add(Ints.consts.get(PropNames.BORDER_START_PRECEDENCE));
        foPropertyLists[FObjectNames.TABLE_FOOTER].
                        add(Ints.consts.get(PropNames.ID));
        foPropertyLists[FObjectNames.TABLE_FOOTER].
                        add(Ints.consts.get(PropNames.VISIBILITY));

        //table-header
        foPropertyLists[FObjectNames.TABLE_HEADER] = new HashSet(
                                PropertySets.accessibilityPropsSize +
                                PropertySets.auralPropsSize +
                                PropertySets.backgroundPropsSize +
                                PropertySets.borderPropsSize +
                                PropertySets.relativePositionPropsSize +
                                6);
        foPropertyLists[FObjectNames.TABLE_HEADER].
                            addAll(PropertySets.accessibilitySet);
        foPropertyLists[FObjectNames.TABLE_HEADER].
                            addAll(PropertySets.auralSet);
        foPropertyLists[FObjectNames.TABLE_HEADER].
                            addAll(PropertySets.backgroundSet);
        foPropertyLists[FObjectNames.TABLE_HEADER].
                            addAll(PropertySets.borderSet);
        foPropertyLists[FObjectNames.TABLE_HEADER].
                            addAll(PropertySets.relativePositionSet);
        foPropertyLists[FObjectNames.TABLE_HEADER].
                    add(Ints.consts.get(PropNames.BORDER_AFTER_PRECEDENCE));
        foPropertyLists[FObjectNames.TABLE_HEADER].
                    add(Ints.consts.get(PropNames.BORDER_BEFORE_PRECEDENCE));
        foPropertyLists[FObjectNames.TABLE_HEADER].
                        add(Ints.consts.get(PropNames.BORDER_END_PRECEDENCE));
        foPropertyLists[FObjectNames.TABLE_HEADER].
                    add(Ints.consts.get(PropNames.BORDER_START_PRECEDENCE));
        foPropertyLists[FObjectNames.TABLE_HEADER].
                        add(Ints.consts.get(PropNames.ID));
        foPropertyLists[FObjectNames.TABLE_HEADER].
                        add(Ints.consts.get(PropNames.VISIBILITY));

        //table-row
        foPropertyLists[FObjectNames.TABLE_ROW] = new HashSet(
                                PropertySets.accessibilityPropsSize +
                                PropertySets.auralPropsSize +
                                PropertySets.backgroundPropsSize +
                                PropertySets.borderPropsSize +
                                PropertySets.relativePositionPropsSize +
                                13);
        foPropertyLists[FObjectNames.TABLE_ROW].
                            addAll(PropertySets.accessibilitySet);
        foPropertyLists[FObjectNames.TABLE_ROW].
                            addAll(PropertySets.auralSet);
        foPropertyLists[FObjectNames.TABLE_ROW].
                            addAll(PropertySets.backgroundSet);
        foPropertyLists[FObjectNames.TABLE_ROW].
                            addAll(PropertySets.borderSet);
        foPropertyLists[FObjectNames.TABLE_ROW].
                            addAll(PropertySets.relativePositionSet);
        foPropertyLists[FObjectNames.TABLE_ROW].
                add(Ints.consts.get(PropNames.BLOCK_PROGRESSION_DIMENSION));
        foPropertyLists[FObjectNames.TABLE_ROW].
                    add(Ints.consts.get(PropNames.BORDER_AFTER_PRECEDENCE));
        foPropertyLists[FObjectNames.TABLE_ROW].
                    add(Ints.consts.get(PropNames.BORDER_BEFORE_PRECEDENCE));
        foPropertyLists[FObjectNames.TABLE_ROW].
                        add(Ints.consts.get(PropNames.BORDER_END_PRECEDENCE));
        foPropertyLists[FObjectNames.TABLE_ROW].
                    add(Ints.consts.get(PropNames.BORDER_START_PRECEDENCE));
        foPropertyLists[FObjectNames.TABLE_ROW].
                        add(Ints.consts.get(PropNames.BREAK_AFTER));
        foPropertyLists[FObjectNames.TABLE_ROW].
                        add(Ints.consts.get(PropNames.BREAK_BEFORE));
        foPropertyLists[FObjectNames.TABLE_ROW].
                        add(Ints.consts.get(PropNames.ID));
        foPropertyLists[FObjectNames.TABLE_ROW].
                        add(Ints.consts.get(PropNames.HEIGHT));
        foPropertyLists[FObjectNames.TABLE_ROW].
                        add(Ints.consts.get(PropNames.KEEP_TOGETHER));
        foPropertyLists[FObjectNames.TABLE_ROW].
                        add(Ints.consts.get(PropNames.KEEP_WITH_NEXT));
        foPropertyLists[FObjectNames.TABLE_ROW].
                        add(Ints.consts.get(PropNames.KEEP_WITH_PREVIOUS));
        foPropertyLists[FObjectNames.TABLE_ROW].
                        add(Ints.consts.get(PropNames.VISIBILITY));

        //title
        foPropertyLists[FObjectNames.TITLE] = new HashSet(
                                PropertySets.accessibilityPropsSize +
                                PropertySets.auralPropsSize +
                                PropertySets.backgroundPropsSize +
                                PropertySets.borderPropsSize +
                                PropertySets.paddingPropsSize +
                                PropertySets.fontPropsSize +
                                PropertySets.marginInlinePropsSize +
                                3);
        foPropertyLists[FObjectNames.TITLE].
                            addAll(PropertySets.accessibilitySet);
        foPropertyLists[FObjectNames.TITLE].
                            addAll(PropertySets.auralSet);
        foPropertyLists[FObjectNames.TITLE].
                            addAll(PropertySets.backgroundSet);
        foPropertyLists[FObjectNames.TITLE].
                            addAll(PropertySets.borderSet);
        foPropertyLists[FObjectNames.TITLE].
                            addAll(PropertySets.paddingSet);
        foPropertyLists[FObjectNames.TITLE].
                            addAll(PropertySets.fontSet);
        foPropertyLists[FObjectNames.TITLE].
                            addAll(PropertySets.marginInlineSet);
        foPropertyLists[FObjectNames.TITLE].
                        add(Ints.consts.get(PropNames.COLOR));
        foPropertyLists[FObjectNames.TITLE].
                        add(Ints.consts.get(PropNames.LINE_HEIGHT));
        foPropertyLists[FObjectNames.TITLE].
                        add(Ints.consts.get(PropNames.VISIBILITY));

        //wrapper
        foPropertyLists[FObjectNames.WRAPPER] = new HashSet(1);
        foPropertyLists[FObjectNames.WRAPPER].
                        add(Ints.consts.get(PropNames.ID));

    }

    // Following are the sets of properties which apply to particular
    // subtrees of the FO Tree.  This whole section is probably redundant.
    // If it is restored to full functioning, the public BitSet objects
    // must be replaced with unmodifiableSets.

    private static final BitSet allProps;

    /**
     * root only set of properties - properties for exclusive use on the
     * root element.  These properties make no sense anywhere below the
     * root element.
     */
    private static final BitSet rootOnly;

    /**
     * declarations only set of properties - properties for exclusive
     * use within the declarations subtree.  These properties make no
     * sense in or under layout-master-set or page-sequences.
     */
    private static final BitSet declarationsOnly;

    /**
     * set of all declarations properties - properties which are
     * usable within the declarations subtree.
     */
    private static final BitSet declarationsAll;

    /**
     * layout-master-set only set of properties - properties for exclusive
     * use within the layout-master-set subtree.  These properties make no
     * sense in or under declarations or page-sequences.
     */
    private static final BitSet layoutMasterOnly;

    /**
     * set of all layout-master-set properties - properties which are
     * usable within the layout-master-set subtree.
     */
    private static final BitSet layoutMasterSet;

    /**
     * set of all page flow subtree properties - properties which are
     * usable within the page flow subtree.
     */
    private static final BitSet pageFlowSet;

    static {

        // Iterator for the PropertySets defined in PropertySets
        Iterator propertySet;
        // fill the BitSet of all properties
        allProps = new BitSet(PropNames.LAST_PROPERTY_INDEX + 1);
        for (int i = 0; i <= PropNames.LAST_PROPERTY_INDEX; i++) {
            allProps.set(i);
        }
        allProps.clear(PropNames.NO_PROPERTY);

        //root only set of properties - properties for exclusive use on the
        // root element
        rootOnly = new BitSet(1);
        rootOnly.set(PropNames.MEDIA_USAGE);

        //declarations only set of properties - properties for exclusive use
        // in the declarations SUBTREE
        declarationsOnly = new BitSet(2);
        declarationsOnly.set(PropNames.COLOR_PROFILE_NAME);
        declarationsOnly.set(PropNames.RENDERING_INTENT);

        // set of all declarations properties - properties which may be
        // used in the declarations SUBTREE
        declarationsAll = new BitSet(3);
        declarationsAll.set(PropNames.SRC);
        declarationsAll.or(declarationsOnly);

        //layout-master-set only set of properties - properties for exclusive
        // use within the layout-master-set SUBTREE
        layoutMasterOnly = new BitSet();
        layoutMasterOnly.set(PropNames.MASTER_NAME);
        layoutMasterOnly.set(PropNames.MASTER_REFERENCE);
        layoutMasterOnly.set(PropNames.MAXIMUM_REPEATS);
        layoutMasterOnly.set(PropNames.PAGE_POSITION);
        layoutMasterOnly.set(PropNames.ODD_OR_EVEN);
        layoutMasterOnly.set(PropNames.BLANK_OR_NOT_BLANK);
        layoutMasterOnly.set(PropNames.PAGE_HEIGHT);
        layoutMasterOnly.set(PropNames.PAGE_WIDTH);
        layoutMasterOnly.set(PropNames.COLUMN_COUNT);
        layoutMasterOnly.set(PropNames.COLUMN_GAP);
        layoutMasterOnly.set(PropNames.REGION_NAME);
        layoutMasterOnly.set(PropNames.EXTENT);
        layoutMasterOnly.set(PropNames.PRECEDENCE);

        // set of all layout-master-set properties - properties which may be
        // used in the layout-master-set SUBTREE
        layoutMasterSet = new BitSet();

        // Add the laoyout-master-set exclusive properties
        layoutMasterSet.or(layoutMasterOnly);

        layoutMasterSet.set(PropNames.REFERENCE_ORIENTATION);
        layoutMasterSet.set(PropNames.WRITING_MODE);
        layoutMasterSet.set(PropNames.CLIP);
        layoutMasterSet.set(PropNames.DISPLAY_ALIGN);
        layoutMasterSet.set(PropNames.OVERFLOW);

        // Add the common margin properties - block
        propertySet = PropertySets.marginBlockSet.iterator();
        while (propertySet.hasNext()) {
            layoutMasterSet.set(((Integer)propertySet.next()).intValue());
        }
        // Add the common border properties
        propertySet = PropertySets.borderSet.iterator();
        while (propertySet.hasNext()) {
            layoutMasterSet.set(((Integer)propertySet.next()).intValue());
        }
        // Add the common padding properties
        propertySet = PropertySets.paddingSet.iterator();
        while (propertySet.hasNext()) {
            layoutMasterSet.set(((Integer)propertySet.next()).intValue());
        }
        // Add the common background properties
        propertySet = PropertySets.backgroundSet.iterator();
        while (propertySet.hasNext()) {
            layoutMasterSet.set(((Integer)propertySet.next()).intValue());
        }
        pageFlowSet = new BitSet(PropNames.LAST_PROPERTY_INDEX + 1);
        pageFlowSet.or(allProps);
        pageFlowSet.andNot(rootOnly);
        pageFlowSet.andNot(declarationsOnly);
        pageFlowSet.andNot(layoutMasterOnly);

    }

    static {
        String prefix = fobsPackageName + ".";
        String foPrefix = "Fo";

        foClassNames    = new String[FObjectNames.foLocalNames.length];
        foClasses       = new Class[FObjectNames.foLocalNames.length];
        foToIndex       = new HashMap(FObjectNames.foLocalNames.length);
        foClassToIndex  = new HashMap(FObjectNames.foLocalNames.length);

        for (int i = 1;i < FObjectNames.foLocalNames.length; i++) {
            String cname = foPrefix;
            StringTokenizer stoke =
                    new StringTokenizer(FObjectNames.foLocalNames[i], "-");
            while (stoke.hasMoreTokens()) {
                String token = stoke.nextToken();
                String pname = new Character(
                                    Character.toUpperCase(token.charAt(0))
                                ).toString() + token.substring(1);
                cname = cname + pname;
            }
            foClassNames[i] = cname;

            // Set up the array of Class objects, indexed by the fo
            // constants.
            String name = prefix + cname;
            try {
                foClasses[i] = Class.forName(name);
            } catch (ClassNotFoundException e) {}

            // Set up the foToIndex Hashmap with the name of the
            // flow object as a key, and the integer index as a value
            if (foToIndex.put((Object) FObjectNames.foLocalNames[i],
                                        Ints.consts.get(i)) != null) {
                throw new RuntimeException(
                    "Duplicate values in propertyToIndex for key " +
                    FObjectNames.foLocalNames[i]);
            }

            // Set up the foClassToIndex Hashmap with the name of the
            // fo class as a key, and the integer index as a value
            
            if (foClassToIndex.put((Object) foClassNames[i],
                                    Ints.consts.get(i)) != null) {
                throw new RuntimeException(
                    "Duplicate values in foClassToIndex for key " +
                    foClassNames[i]);
            }

        }
    }

}

