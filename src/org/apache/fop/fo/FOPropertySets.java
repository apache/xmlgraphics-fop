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

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FObjectNames;
import org.apache.fop.fo.PropertySets;
import org.apache.fop.fo.PropNames;
import org.apache.fop.datatypes.Ints;
import org.apache.fop.datastructs.ROIntArray;
import org.apache.fop.datastructs.ROBitSet;

/**
 * Data class relating sets of properties to Flow Objects.
 */

public class FOPropertySets {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    public static final String XSLNamespace =
                                        "http://www.w3.org/1999/XSL/Format";

    public static final String packageNamePrefix = "org.apache.fop";

    /**
     * Constants for the set of attributes of interest with FONodes
     */
    public static final int
              NO_SET = 0
           ,ROOT_SET = 1
   ,DECLARATIONS_SET = 2
         ,LAYOUT_SET = 3
     ,SEQ_MASTER_SET = 4
        ,PAGESEQ_SET = 5
           ,FLOW_SET = 6
         ,STATIC_SET = 7
         ,MARKER_SET = 8

           ,LAST_SET = MARKER_SET
                     ;

    public static String getAttrSetName(int attrSet) throws FOPException {
        switch (attrSet) {
        case ROOT_SET:
            return "ROOT";
        case DECLARATIONS_SET:
            return "DECLARATIONS";
        case LAYOUT_SET:
            return "LAYOUT";
        case SEQ_MASTER_SET:
            return "SEQ_MASTER";
        case PAGESEQ_SET:
            return "PAGESEQ";
        case FLOW_SET:
            return "FLOW";
        case STATIC_SET:
            return "STATIC";
        case MARKER_SET:
            return "MARKER";
        }
        throw new FOPException("Invalid attribute set: " + attrSet);
    }

    public static ROBitSet getAttrROBitSet(int attrSet)
            throws FOPException
    {
        switch (attrSet) {
        case ROOT_SET:
            return allProps;
        case DECLARATIONS_SET:
            return declarationsAll;
        case LAYOUT_SET:
            return layoutMasterSet;
        case SEQ_MASTER_SET:
            return seqMasterSet;
        case PAGESEQ_SET:
            return pageSeqSet;
        case FLOW_SET:
            return flowAllSet;
        case STATIC_SET:
            return staticAllSet;
        case MARKER_SET:
            return markerAllSet;
        }
        throw new FOPException("Invalid attribute set: " + attrSet);
    }

    /*
    public static ROBitSet getInheritedROBitSet(int attrSet)
            throws FOPException
    {
        switch (attrSet) {
        case ROOT_SET:
            return allInheritedProps;
        case DECLARATIONS_SET:
            return declarationsInherited;
        case LAYOUT_SET:
            return inheritedLayoutSet;
        case SEQ_MASTER_SET:
            return inheritedSeqMasterSet;
        case PAGESEQ_SET:
            return inheritedPageSeqSet;
        case FLOW_SET:
            return inheritedFlowSet;
        case STATIC_SET:
            return inheritedStaticSet;
        case MARKER_SET:
            return inheritedMarkerSet;
        }
        throw new FOPException("Invalid attribute set: " + attrSet);
    }

    public static ROBitSet getNonInheritedROBitSet(int attrSet)
            throws FOPException
    {
        switch (attrSet) {
        case ROOT_SET:
            return allNonInheritedProps;
        case DECLARATIONS_SET:
            return declarationsNonInherited;
        case LAYOUT_SET:
            return nonInheritedLayoutSet;
        case SEQ_MASTER_SET:
            return nonInheritedSeqMasterSet;
        case PAGESEQ_SET:
            return nonInheritedPageSeqSet;
        case FLOW_SET:
            return nonInheritedFlowSet;
        case STATIC_SET:
            return nonInheritedStaticSet;
        case MARKER_SET:
            return nonInheritedMarkerSet;
        }
        throw new FOPException("Invalid attribute set: " + attrSet);
    }
    */

    public static int getFoIndex(String name) {
        return ((Integer)(foToIndex.get(name))).intValue();
    }

    public static String getClassName(int foIndex) {
        return foClassNames[foIndex];
    }

    public static Class getClass(int foIndex) {
        return foClasses[foIndex];
    }

    /**
     * A String[] array of the fo class names.  This array is
     * effectively 1-based, with the first element being unused.
     * The array is initialized in a static initializer by converting the
     * fo names from the array FObjectNames.foLocalNames into class names by
     * converting the first character of every component word to upper case,
     * removing all punctuation characters and prepending the prefix 'Fo'.
     *  It can be indexed by the fo name constants defined in the
     * <tt>FObjectNames</tt> class.
     */
    private static final String[] foClassNames;

    /**
     * A String[] array of the fo class package names.  This array is
     * effectively 1-based, with the first element being unused.
     * The array is initialized in a static initializer by constructing
     * the package name from the common package prefix set in the field
     * <tt>packageNamePrefix</tt>, the package name suffix associated with
     * the fo local names in the <tt>FObjectNames.foLocalNames</tt> array,
     * the the class name which has been constructed in the
     * <tt>foClassNames</tt> array here.
     *  It can be indexed by the fo name constants defined in the
     * <tt>FObjectNames</tt> class.
     */
    private static final String[] foClassPackages;

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
    private static final ROBitSet[] foPropertyLists;

    /**
     * A Bitmap representing all of the Properties for use in building
     * the partition sets of the properties.
     */

    static {
        foPropertyLists = new ROBitSet[FObjectNames.LAST_FO + 1];
        
        BitSet no_fo = new BitSet();
        no_fo.set(PropNames.NO_PROPERTY);
        foPropertyLists[FObjectNames.NO_FO] = new ROBitSet(no_fo);

        //basic-link
        BitSet basic_link = new BitSet();
        basic_link.or(PropertySets.accessibilitySet);
        basic_link.or(PropertySets.auralSet);
        basic_link.or(PropertySets.backgroundSet);
        basic_link.or(PropertySets.borderSet);
        basic_link.or(PropertySets.paddingSet);
        basic_link.or(PropertySets.marginInlineSet);
        basic_link.or(PropertySets.relativePositionSet);
        basic_link.set(PropNames.ALIGNMENT_ADJUST);
        basic_link.set(PropNames.ALIGNMENT_BASELINE);
        basic_link.set(PropNames.BASELINE_SHIFT);
        basic_link.set(PropNames.DESTINATION_PLACEMENT_OFFSET);
        basic_link.set(PropNames.DOMINANT_BASELINE);
        basic_link.set(PropNames.EXTERNAL_DESTINATION);
        basic_link.set(PropNames.ID);
        basic_link.set(PropNames.INDICATE_DESTINATION);
        basic_link.set(PropNames.INTERNAL_DESTINATION);
        basic_link.set(PropNames.KEEP_TOGETHER);
        basic_link.set(PropNames.KEEP_WITH_NEXT);
        basic_link.set(PropNames.KEEP_WITH_PREVIOUS);
        basic_link.set(PropNames.LINE_HEIGHT);
        basic_link.set(PropNames.SHOW_DESTINATION);
        basic_link.set(PropNames.TARGET_PROCESSING_CONTEXT);
        basic_link.set(PropNames.TARGET_PRESENTATION_CONTEXT);
        basic_link.set(PropNames.TARGET_STYLESHEET);
        foPropertyLists[FObjectNames.BASIC_LINK] = new ROBitSet(basic_link);
        
        //bidi-override
        BitSet bidi_override = new BitSet();
        bidi_override.or(PropertySets.relativePositionSet);
        bidi_override.or(PropertySets.auralSet);
        bidi_override.or(PropertySets.fontSet);
        bidi_override.set(PropNames.COLOR);
        bidi_override.set(PropNames.DIRECTION);
        bidi_override.set(PropNames.ID);
        bidi_override.set(PropNames.LETTER_SPACING);
        bidi_override.set(PropNames.LINE_HEIGHT);
        bidi_override.set(PropNames.SCORE_SPACES);
        bidi_override.set(PropNames.UNICODE_BIDI);
        bidi_override.set(PropNames.WORD_SPACING);
        foPropertyLists[FObjectNames.BIDI_OVERRIDE] = new ROBitSet(bidi_override);

        //block
        BitSet block = new BitSet();
        block.or(PropertySets.accessibilitySet);
        block.or(PropertySets.auralSet);
        block.or(PropertySets.backgroundSet);
        block.or(PropertySets.borderSet);
        block.or(PropertySets.fontSet);
        block.or(PropertySets.hyphenationSet);
        block.or(PropertySets.marginBlockSet);
        block.or(PropertySets.paddingSet);
        block.or(PropertySets.relativePositionSet);
        block.set(PropNames.BREAK_AFTER);
        block.set(PropNames.BREAK_BEFORE);
        block.set(PropNames.COLOR);
        block.set(PropNames.TEXT_DEPTH);
        block.set(PropNames.TEXT_ALTITUDE);
        block.set(PropNames.HYPHENATION_KEEP);
        block.set(PropNames.HYPHENATION_LADDER_COUNT);
        block.set(PropNames.ID);
        block.set(PropNames.INTRUSION_DISPLACE);
        block.set(PropNames.KEEP_TOGETHER);
        block.set(PropNames.KEEP_WITH_NEXT);
        block.set(PropNames.KEEP_WITH_PREVIOUS);
        block.set(PropNames.LAST_LINE_END_INDENT);
        block.set(PropNames.LINEFEED_TREATMENT);
        block.set(PropNames.LINE_HEIGHT);
        block.set(PropNames.LINE_HEIGHT_SHIFT_ADJUSTMENT);
        block.set(PropNames.LINE_STACKING_STRATEGY);
        block.set(PropNames.ORPHANS);
        block.set(PropNames.WHITE_SPACE_TREATMENT);
        block.set(PropNames.SPAN);
        block.set(PropNames.TEXT_ALIGN);
        block.set(PropNames.TEXT_ALIGN_LAST);
        block.set(PropNames.TEXT_INDENT);
        block.set(PropNames.VISIBILITY);
        block.set(PropNames.WHITE_SPACE_COLLAPSE);
        block.set(PropNames.WIDOWS);
        block.set(PropNames.WRAP_OPTION);
        foPropertyLists[FObjectNames.BLOCK] = new ROBitSet(block);

        //block-container
        BitSet block_container = new BitSet();
        block_container.or(PropertySets.absolutePositionSet);
        block_container.or(PropertySets.backgroundSet);
        block_container.or(PropertySets.borderSet);
        block_container.or(PropertySets.marginBlockSet);
        block_container.or(PropertySets.paddingSet);
        block_container.set(PropNames.BLOCK_PROGRESSION_DIMENSION);
        block_container.set(PropNames.BREAK_AFTER);
        block_container.set(PropNames.BREAK_BEFORE);
        block_container.set(PropNames.CLIP);
        block_container.set(PropNames.DISPLAY_ALIGN);
        block_container.set(PropNames.HEIGHT);
        block_container.set(PropNames.ID);
        block_container.set(PropNames.INLINE_PROGRESSION_DIMENSION);
        block_container.set(PropNames.INTRUSION_DISPLACE);
        block_container.set(PropNames.KEEP_TOGETHER);
        block_container.set(PropNames.KEEP_WITH_NEXT);
        block_container.set(PropNames.KEEP_WITH_PREVIOUS);
        block_container.set(PropNames.OVERFLOW);
        block_container.set(PropNames.REFERENCE_ORIENTATION);
        block_container.set(PropNames.SPAN);
        block_container.set(PropNames.WIDTH);
        block_container.set(PropNames.WRITING_MODE);
        block_container.set(PropNames.Z_INDEX);
        foPropertyLists[FObjectNames.BLOCK_CONTAINER] = new ROBitSet(block_container);

        //character
        BitSet character = new BitSet();
        character.or(PropertySets.auralSet);
        character.or(PropertySets.backgroundSet);
        character.or(PropertySets.borderSet);
        character.or(PropertySets.fontSet);
        character.or(PropertySets.hyphenationSet);
        character.or(PropertySets.marginInlineSet);
        character.or(PropertySets.paddingSet);
        character.or(PropertySets.relativePositionSet);
        character.set(PropNames.ALIGNMENT_ADJUST);
        character.set(PropNames.TREAT_AS_WORD_SPACE);
        character.set(PropNames.ALIGNMENT_BASELINE);
        character.set(PropNames.BASELINE_SHIFT);
        character.set(PropNames.CHARACTER);
        character.set(PropNames.COLOR);
        character.set(PropNames.DOMINANT_BASELINE);
        character.set(PropNames.TEXT_DEPTH);
        character.set(PropNames.TEXT_ALTITUDE);
        character.set(PropNames.GLYPH_ORIENTATION_HORIZONTAL);
        character.set(PropNames.GLYPH_ORIENTATION_VERTICAL);
        character.set(PropNames.ID);
        character.set(PropNames.KEEP_WITH_NEXT);
        character.set(PropNames.KEEP_WITH_PREVIOUS);
        character.set(PropNames.LETTER_SPACING);
        character.set(PropNames.LINE_HEIGHT);
        character.set(PropNames.SCORE_SPACES);
        character.set(PropNames.SUPPRESS_AT_LINE_BREAK);
        character.set(PropNames.TEXT_DECORATION);
        character.set(PropNames.TEXT_SHADOW);
        character.set(PropNames.TEXT_TRANSFORM);
        character.set(PropNames.VISIBILITY);
        character.set(PropNames.WORD_SPACING);
        foPropertyLists[FObjectNames.CHARACTER] = new ROBitSet(character);

        //color-profile
        BitSet color_profile = new BitSet();
        color_profile.set(PropNames.COLOR_PROFILE_NAME);
        color_profile.set(PropNames.RENDERING_INTENT);
        color_profile.set(PropNames.SRC);
        foPropertyLists[FObjectNames.COLOR_PROFILE] = new ROBitSet(color_profile);

        //conditional-page-master-reference
        BitSet conditional_page_master_reference = new BitSet();
        conditional_page_master_reference.set(PropNames.MASTER_REFERENCE);
        conditional_page_master_reference.set(PropNames.PAGE_POSITION);
        conditional_page_master_reference.set(PropNames.ODD_OR_EVEN);
        conditional_page_master_reference.set(PropNames.BLANK_OR_NOT_BLANK);
        foPropertyLists[FObjectNames.CONDITIONAL_PAGE_MASTER_REFERENCE] = new ROBitSet(conditional_page_master_reference);

        //declarations
        foPropertyLists[FObjectNames.DECLARATIONS] = new ROBitSet(new BitSet());

        //external-graphic
        BitSet external_graphic = new BitSet();
        external_graphic.or(PropertySets.accessibilitySet);
        external_graphic.or(PropertySets.auralSet);
        external_graphic.or(PropertySets.backgroundSet);
        external_graphic.or(PropertySets.borderSet);
        external_graphic.or(PropertySets.marginInlineSet);
        external_graphic.or(PropertySets.paddingSet);
        external_graphic.or(PropertySets.relativePositionSet);
        external_graphic.set(PropNames.ALIGNMENT_ADJUST);
        external_graphic.set(PropNames.ALIGNMENT_BASELINE);
        external_graphic.set(PropNames.BASELINE_SHIFT);
        external_graphic.set(PropNames.BLOCK_PROGRESSION_DIMENSION);
        external_graphic.set(PropNames.CLIP);
        external_graphic.set(PropNames.CONTENT_HEIGHT);
        external_graphic.set(PropNames.CONTENT_TYPE);
        external_graphic.set(PropNames.CONTENT_WIDTH);
        external_graphic.set(PropNames.DISPLAY_ALIGN);
        external_graphic.set(PropNames.DOMINANT_BASELINE);
        external_graphic.set(PropNames.HEIGHT);
        external_graphic.set(PropNames.ID);
        external_graphic.set(PropNames.INLINE_PROGRESSION_DIMENSION);
        external_graphic.set(PropNames.KEEP_WITH_NEXT);
        external_graphic.set(PropNames.KEEP_WITH_PREVIOUS);
        external_graphic.set(PropNames.LINE_HEIGHT);
        external_graphic.set(PropNames.OVERFLOW);
        external_graphic.set(PropNames.SCALING);
        external_graphic.set(PropNames.SCALING_METHOD);
        external_graphic.set(PropNames.SRC);
        external_graphic.set(PropNames.TEXT_ALIGN);
        external_graphic.set(PropNames.WIDTH);
        foPropertyLists[FObjectNames.EXTERNAL_GRAPHIC] = new ROBitSet(external_graphic);

        //float
        BitSet floatset = new BitSet();
        floatset.set(PropNames.CLEAR);
        floatset.set(PropNames.FLOAT);
        foPropertyLists[FObjectNames.FLOAT] = new ROBitSet(floatset);

        //flow
        BitSet flow = new BitSet();
        flow.set(PropNames.FLOW_NAME);
        foPropertyLists[FObjectNames.FLOW] = new ROBitSet(flow);

        //footnote
        BitSet footnote = new BitSet();
        footnote.or(PropertySets.accessibilitySet);
        foPropertyLists[FObjectNames.FOOTNOTE] = new ROBitSet(footnote);

        //footnote-body
        BitSet footnote_body = new BitSet();
        footnote_body.or(PropertySets.accessibilitySet);
        foPropertyLists[FObjectNames.FOOTNOTE_BODY] = new ROBitSet(footnote_body);

        //initial-property-set
        BitSet initial_property_set = new BitSet();
        initial_property_set.or(PropertySets.accessibilitySet);
        initial_property_set.or(PropertySets.auralSet);
        initial_property_set.or(PropertySets.backgroundSet);
        initial_property_set.or(PropertySets.borderSet);
        initial_property_set.or(PropertySets.fontSet);
        initial_property_set.or(PropertySets.paddingSet);
        initial_property_set.or(PropertySets.relativePositionSet);
        initial_property_set.set(PropNames.COLOR);
        initial_property_set.set(PropNames.ID);
        initial_property_set.set(PropNames.LETTER_SPACING);
        initial_property_set.set(PropNames.LINE_HEIGHT);
        initial_property_set.set(PropNames.SCORE_SPACES);
        initial_property_set.set(PropNames.TEXT_DECORATION);
        initial_property_set.set(PropNames.TEXT_SHADOW);
        initial_property_set.set(PropNames.TEXT_TRANSFORM);
        initial_property_set.set(PropNames.WORD_SPACING);
        foPropertyLists[FObjectNames.INITIAL_PROPERTY_SET] = new ROBitSet(initial_property_set);

        //inline
        BitSet inline = new BitSet();
        inline.or(PropertySets.accessibilitySet);
        inline.or(PropertySets.auralSet);
        inline.or(PropertySets.backgroundSet);
        inline.or(PropertySets.borderSet);
        inline.or(PropertySets.fontSet);
        inline.or(PropertySets.marginInlineSet);
        inline.or(PropertySets.paddingSet);
        inline.or(PropertySets.relativePositionSet);
        inline.set(PropNames.ALIGNMENT_ADJUST);
        inline.set(PropNames.ALIGNMENT_BASELINE);
        inline.set(PropNames.BASELINE_SHIFT);
        inline.set(PropNames.BLOCK_PROGRESSION_DIMENSION);
        inline.set(PropNames.COLOR);
        inline.set(PropNames.DOMINANT_BASELINE);
        inline.set(PropNames.HEIGHT);
        inline.set(PropNames.ID);
        inline.set(PropNames.INLINE_PROGRESSION_DIMENSION);
        inline.set(PropNames.KEEP_TOGETHER);
        inline.set(PropNames.KEEP_WITH_NEXT);
        inline.set(PropNames.KEEP_WITH_PREVIOUS);
        inline.set(PropNames.LINE_HEIGHT);
        inline.set(PropNames.TEXT_DECORATION);
        inline.set(PropNames.VISIBILITY);
        inline.set(PropNames.WIDTH);
        inline.set(PropNames.WRAP_OPTION);
        foPropertyLists[FObjectNames.INLINE] = new ROBitSet(inline);

        //inline-container
        BitSet inline_container = new BitSet();
        inline_container.or(PropertySets.backgroundSet);
        inline_container.or(PropertySets.borderSet);
        inline_container.or(PropertySets.marginInlineSet);
        inline_container.or(PropertySets.paddingSet);
        inline_container.or(PropertySets.relativePositionSet);
        inline_container.set(PropNames.ALIGNMENT_ADJUST);
        inline_container.set(PropNames.ALIGNMENT_BASELINE);
        inline_container.set(PropNames.BASELINE_SHIFT);
        inline_container.set(PropNames.BLOCK_PROGRESSION_DIMENSION);
        inline_container.set(PropNames.CLIP);
        inline_container.set(PropNames.DISPLAY_ALIGN);
        inline_container.set(PropNames.DOMINANT_BASELINE);
        inline_container.set(PropNames.HEIGHT);
        inline_container.set(PropNames.ID);
        inline_container.set(PropNames.INLINE_PROGRESSION_DIMENSION);
        inline_container.set(PropNames.KEEP_TOGETHER);
        inline_container.set(PropNames.KEEP_WITH_NEXT);
        inline_container.set(PropNames.KEEP_WITH_PREVIOUS);
        inline_container.set(PropNames.LINE_HEIGHT);
        inline_container.set(PropNames.OVERFLOW);
        inline_container.set(PropNames.REFERENCE_ORIENTATION);
        inline_container.set(PropNames.WIDTH);
        inline_container.set(PropNames.WRITING_MODE);
        foPropertyLists[FObjectNames.INLINE_CONTAINER] = new ROBitSet(inline_container);


        //instream-foreign-object
        BitSet instream_foreign_object = new BitSet();
        instream_foreign_object.or(PropertySets.accessibilitySet);
        instream_foreign_object.or(PropertySets.auralSet);
        instream_foreign_object.or(PropertySets.backgroundSet);
        instream_foreign_object.or(PropertySets.borderSet);
        instream_foreign_object.or(PropertySets.marginInlineSet);
        instream_foreign_object.or(PropertySets.paddingSet);
        instream_foreign_object.or(PropertySets.relativePositionSet);
        instream_foreign_object.set(PropNames.ALIGNMENT_ADJUST);
        instream_foreign_object.set(PropNames.ALIGNMENT_BASELINE);
        instream_foreign_object.set(PropNames.BASELINE_SHIFT);
        instream_foreign_object.set(PropNames.BLOCK_PROGRESSION_DIMENSION);
        instream_foreign_object.set(PropNames.CLIP);
        instream_foreign_object.set(PropNames.CONTENT_HEIGHT);
        instream_foreign_object.set(PropNames.CONTENT_TYPE);
        instream_foreign_object.set(PropNames.CONTENT_WIDTH);
        instream_foreign_object.set(PropNames.DISPLAY_ALIGN);
        instream_foreign_object.set(PropNames.DOMINANT_BASELINE);
        instream_foreign_object.set(PropNames.HEIGHT);
        instream_foreign_object.set(PropNames.ID);
        instream_foreign_object.set(PropNames.INLINE_PROGRESSION_DIMENSION);
        instream_foreign_object.set(PropNames.KEEP_WITH_NEXT);
        instream_foreign_object.set(PropNames.KEEP_WITH_PREVIOUS);
        instream_foreign_object.set(PropNames.LINE_HEIGHT);
        instream_foreign_object.set(PropNames.OVERFLOW);
        instream_foreign_object.set(PropNames.SCALING);
        instream_foreign_object.set(PropNames.SCALING_METHOD);
        instream_foreign_object.set(PropNames.TEXT_ALIGN);
        instream_foreign_object.set(PropNames.WIDTH);
        foPropertyLists[FObjectNames.INSTREAM_FOREIGN_OBJECT] = new ROBitSet(instream_foreign_object);

        //layout-master-set
        foPropertyLists[FObjectNames.LAYOUT_MASTER_SET] = new ROBitSet(new BitSet());

        //leader
        BitSet leader = new BitSet();
        leader.or(PropertySets.accessibilitySet);
        leader.or(PropertySets.auralSet);
        leader.or(PropertySets.backgroundSet);
        leader.or(PropertySets.borderSet);
        leader.or(PropertySets.fontSet);
        leader.or(PropertySets.marginInlineSet);
        leader.or(PropertySets.paddingSet);
        leader.or(PropertySets.relativePositionSet);
        leader.set(PropNames.ALIGNMENT_ADJUST);
        leader.set(PropNames.ALIGNMENT_BASELINE);
        leader.set(PropNames.BASELINE_SHIFT);
        leader.set(PropNames.COLOR);
        leader.set(PropNames.DOMINANT_BASELINE);
        leader.set(PropNames.TEXT_DEPTH);
        leader.set(PropNames.TEXT_ALTITUDE);
        leader.set(PropNames.ID);
        leader.set(PropNames.KEEP_WITH_NEXT);
        leader.set(PropNames.KEEP_WITH_PREVIOUS);
        leader.set(PropNames.LEADER_ALIGNMENT);
        leader.set(PropNames.LEADER_LENGTH);
        leader.set(PropNames.LEADER_PATTERN);
        leader.set(PropNames.LEADER_PATTERN_WIDTH);
        leader.set(PropNames.RULE_STYLE);
        leader.set(PropNames.RULE_THICKNESS);
        leader.set(PropNames.LETTER_SPACING);
        leader.set(PropNames.LINE_HEIGHT);
        leader.set(PropNames.TEXT_SHADOW);
        leader.set(PropNames.VISIBILITY);
        leader.set(PropNames.WORD_SPACING);
        foPropertyLists[FObjectNames.LEADER] = new ROBitSet(leader);

        //list-block
        BitSet list_block = new BitSet();
        list_block.or(PropertySets.accessibilitySet);
        list_block.or(PropertySets.auralSet);
        list_block.or(PropertySets.backgroundSet);
        list_block.or(PropertySets.borderSet);
        list_block.or(PropertySets.marginBlockSet);
        list_block.or(PropertySets.paddingSet);
        list_block.or(PropertySets.relativePositionSet);
        list_block.set(PropNames.BREAK_AFTER);
        list_block.set(PropNames.BREAK_BEFORE);
        list_block.set(PropNames.ID);
        list_block.set(PropNames.INTRUSION_DISPLACE);
        list_block.set(PropNames.KEEP_TOGETHER);
        list_block.set(PropNames.KEEP_WITH_NEXT);
        list_block.set(PropNames.KEEP_WITH_PREVIOUS);
        list_block.set(PropNames.PROVISIONAL_DISTANCE_BETWEEN_STARTS);
        list_block.set(PropNames.PROVISIONAL_LABEL_SEPARATION);
        foPropertyLists[FObjectNames.LIST_BLOCK] = new ROBitSet(list_block);

        //list-item
        BitSet list_item = new BitSet();
        list_item.or(PropertySets.accessibilitySet);
        list_item.or(PropertySets.auralSet);
        list_item.or(PropertySets.backgroundSet);
        list_item.or(PropertySets.borderSet);
        list_item.or(PropertySets.marginBlockSet);
        list_item.or(PropertySets.paddingSet);
        list_item.or(PropertySets.relativePositionSet);
        list_item.set(PropNames.BREAK_AFTER);
        list_item.set(PropNames.BREAK_BEFORE);
        list_item.set(PropNames.ID);
        list_item.set(PropNames.INTRUSION_DISPLACE);
        list_item.set(PropNames.KEEP_TOGETHER);
        list_item.set(PropNames.KEEP_WITH_NEXT);
        list_item.set(PropNames.KEEP_WITH_PREVIOUS);
        list_item.set(PropNames.RELATIVE_ALIGN);
        foPropertyLists[FObjectNames.LIST_ITEM] = new ROBitSet(list_item);

        //list-item-body
        BitSet list_item_body = new BitSet();
        list_item_body.or(PropertySets.accessibilitySet);
        list_item_body.set(PropNames.ID);
        list_item_body.set(PropNames.KEEP_TOGETHER);
        foPropertyLists[FObjectNames.LIST_ITEM_BODY] = new ROBitSet(list_item_body);

        //list-item-label
        BitSet list_item_label = new BitSet();
        list_item_label.or(PropertySets.accessibilitySet);
        list_item_label.set(PropNames.ID);
        list_item_label.set(PropNames.KEEP_TOGETHER);
        foPropertyLists[FObjectNames.LIST_ITEM_LABEL] = new ROBitSet(list_item_label);

        //marker
        BitSet marker = new BitSet();
        marker.set(PropNames.MARKER_CLASS_NAME);
        foPropertyLists[FObjectNames.MARKER] = new ROBitSet(marker);

        //multi-case
        BitSet multi_case = new BitSet();
        multi_case.or(PropertySets.accessibilitySet);
        multi_case.set(PropNames.CASE_NAME);
        multi_case.set(PropNames.CASE_TITLE);
        multi_case.set(PropNames.ID);
        multi_case.set(PropNames.STARTING_STATE);
        foPropertyLists[FObjectNames.MULTI_CASE] = new ROBitSet(multi_case);

        //multi-properties
        BitSet multi_properties = new BitSet();
        multi_properties.or(PropertySets.accessibilitySet);
        multi_properties.set(PropNames.ID);
        foPropertyLists[FObjectNames.MULTI_PROPERTIES] = new ROBitSet(multi_properties);

        //multi-property-set
        BitSet multi_property_set = new BitSet();
        multi_properties.set(PropNames.ACTIVE_STATE);
        multi_properties.set(PropNames.ID);
        foPropertyLists[FObjectNames.MULTI_PROPERTY_SET] = new ROBitSet(multi_property_set);

        //multi-switch
        BitSet multi_switch = new BitSet();
        multi_switch.or(PropertySets.accessibilitySet);
        multi_switch.set(PropNames.AUTO_RESTORE);
        multi_switch.set(PropNames.ID);
        foPropertyLists[FObjectNames.MULTI_SWITCH] = new ROBitSet(multi_switch);

        //multi-toggle
        BitSet multi_toggle = new BitSet();
        multi_toggle.or(PropertySets.accessibilitySet);
        multi_toggle.set(PropNames.ID);
        multi_toggle.set(PropNames.SWITCH_TO);
        foPropertyLists[FObjectNames.MULTI_TOGGLE] = new ROBitSet(multi_toggle);

        //page-number
        BitSet page_number = new BitSet();
        page_number.or(PropertySets.accessibilitySet);
        page_number.or(PropertySets.auralSet);
        page_number.or(PropertySets.backgroundSet);
        page_number.or(PropertySets.borderSet);
        page_number.or(PropertySets.fontSet);
        page_number.or(PropertySets.marginInlineSet);
        page_number.or(PropertySets.paddingSet);
        page_number.or(PropertySets.relativePositionSet);
        page_number.set(PropNames.ALIGNMENT_ADJUST);
        page_number.set(PropNames.ALIGNMENT_BASELINE);
        page_number.set(PropNames.BASELINE_SHIFT);
        page_number.set(PropNames.DOMINANT_BASELINE);
        page_number.set(PropNames.ID);
        page_number.set(PropNames.KEEP_WITH_NEXT);
        page_number.set(PropNames.KEEP_WITH_PREVIOUS);
        page_number.set(PropNames.LETTER_SPACING);
        page_number.set(PropNames.LINE_HEIGHT);
        page_number.set(PropNames.SCORE_SPACES);
        page_number.set(PropNames.TEXT_ALTITUDE);
        page_number.set(PropNames.TEXT_DECORATION);
        page_number.set(PropNames.TEXT_DEPTH);
        page_number.set(PropNames.TEXT_SHADOW);
        page_number.set(PropNames.TEXT_TRANSFORM);
        page_number.set(PropNames.VISIBILITY);
        page_number.set(PropNames.WORD_SPACING);
        page_number.set(PropNames.WRAP_OPTION);
        foPropertyLists[FObjectNames.PAGE_NUMBER] = new ROBitSet(page_number);

        //page-number-citation
        BitSet page_number_citation = new BitSet();
        page_number_citation.or(PropertySets.accessibilitySet);
        page_number_citation.or(PropertySets.auralSet);
        page_number_citation.or(PropertySets.backgroundSet);
        page_number_citation.or(PropertySets.borderSet);
        page_number_citation.or(PropertySets.fontSet);
        page_number_citation.or(PropertySets.marginInlineSet);
        page_number_citation.or(PropertySets.paddingSet);
        page_number_citation.or(PropertySets.relativePositionSet);
        page_number_citation.set(PropNames.ALIGNMENT_ADJUST);
        page_number_citation.set(PropNames.ALIGNMENT_BASELINE);
        page_number_citation.set(PropNames.BASELINE_SHIFT);
        page_number_citation.set(PropNames.DOMINANT_BASELINE);
        page_number_citation.set(PropNames.ID);
        page_number_citation.set(PropNames.KEEP_WITH_NEXT);
        page_number_citation.set(PropNames.KEEP_WITH_PREVIOUS);
        page_number_citation.set(PropNames.LETTER_SPACING);
        page_number_citation.set(PropNames.LINE_HEIGHT);
        page_number_citation.set(PropNames.REF_ID);
        page_number_citation.set(PropNames.SCORE_SPACES);
        page_number_citation.set(PropNames.TEXT_ALTITUDE);
        page_number_citation.set(PropNames.TEXT_DECORATION);
        page_number_citation.set(PropNames.TEXT_DEPTH);
        page_number_citation.set(PropNames.TEXT_SHADOW);
        page_number_citation.set(PropNames.TEXT_TRANSFORM);
        page_number_citation.set(PropNames.VISIBILITY);
        page_number_citation.set(PropNames.WORD_SPACING);
        page_number_citation.set(PropNames.WRAP_OPTION);
        foPropertyLists[FObjectNames.PAGE_NUMBER_CITATION] = new ROBitSet(page_number_citation);

        //page-sequence
        BitSet page_sequence = new BitSet();
        page_sequence.set(PropNames.COUNTRY);
        page_sequence.set(PropNames.FORMAT);
        page_sequence.set(PropNames.LANGUAGE);
        page_sequence.set(PropNames.LETTER_VALUE);
        page_sequence.set(PropNames.GROUPING_SEPARATOR);
        page_sequence.set(PropNames.GROUPING_SIZE);
        page_sequence.set(PropNames.ID);
        page_sequence.set(PropNames.INITIAL_PAGE_NUMBER);
        page_sequence.set(PropNames.FORCE_PAGE_COUNT);
        page_sequence.set(PropNames.MASTER_REFERENCE);
        foPropertyLists[FObjectNames.PAGE_SEQUENCE] = new ROBitSet(page_sequence);

        //page-sequence-master
        BitSet page_sequence_master = new BitSet();
        page_sequence_master.set(PropNames.MASTER_NAME);
        foPropertyLists[FObjectNames.PAGE_SEQUENCE_MASTER] = new ROBitSet(page_sequence_master);

        //region-after
        BitSet region_after = new BitSet();
        region_after.or(PropertySets.backgroundSet);
        region_after.or(PropertySets.borderSet);
        region_after.or(PropertySets.paddingSet);
        region_after.set(PropNames.CLIP);
        region_after.set(PropNames.DISPLAY_ALIGN);
        region_after.set(PropNames.EXTENT);
        region_after.set(PropNames.OVERFLOW);
        region_after.set(PropNames.PRECEDENCE);
        region_after.set(PropNames.REGION_NAME);
        region_after.set(PropNames.REFERENCE_ORIENTATION);
        region_after.set(PropNames.WRITING_MODE);
        foPropertyLists[FObjectNames.REGION_AFTER] = new ROBitSet(region_after);

        //region-before
        BitSet region_before = new BitSet();
        region_before.or(PropertySets.backgroundSet);
        region_before.or(PropertySets.borderSet);
        region_before.or(PropertySets.paddingSet);
        region_before.set(PropNames.CLIP);
        region_before.set(PropNames.DISPLAY_ALIGN);
        region_before.set(PropNames.EXTENT);
        region_before.set(PropNames.OVERFLOW);
        region_before.set(PropNames.PRECEDENCE);
        region_before.set(PropNames.REGION_NAME);
        region_before.set(PropNames.REFERENCE_ORIENTATION);
        region_before.set(PropNames.WRITING_MODE);
        foPropertyLists[FObjectNames.REGION_BEFORE] = new ROBitSet(region_before);

        //region-body
        BitSet region_body = new BitSet();
        region_body.or(PropertySets.backgroundSet);
        region_body.or(PropertySets.borderSet);
        region_body.or(PropertySets.paddingSet);
        region_body.or(PropertySets.marginBlockSet);
        region_body.set(PropNames.CLIP);
        region_body.set(PropNames.COLUMN_COUNT);
        region_body.set(PropNames.COLUMN_GAP);
        region_body.set(PropNames.DISPLAY_ALIGN);
        region_body.set(PropNames.OVERFLOW);
        region_body.set(PropNames.REGION_NAME);
        region_body.set(PropNames.REFERENCE_ORIENTATION);
        region_body.set(PropNames.WRITING_MODE);
        foPropertyLists[FObjectNames.REGION_BODY] = new ROBitSet(region_body);

        //region-end
        BitSet region_end = new BitSet();
        region_end.or(PropertySets.backgroundSet);
        region_end.or(PropertySets.borderSet);
        region_end.or(PropertySets.paddingSet);
        region_end.set(PropNames.CLIP);
        region_end.set(PropNames.DISPLAY_ALIGN);
        region_end.set(PropNames.EXTENT);
        region_end.set(PropNames.OVERFLOW);
        region_end.set(PropNames.REGION_NAME);
        region_end.set(PropNames.REFERENCE_ORIENTATION);
        region_end.set(PropNames.WRITING_MODE);
        foPropertyLists[FObjectNames.REGION_END] = new ROBitSet(region_end);

        //region-start
        BitSet region_start = new BitSet();
        region_start.or(PropertySets.backgroundSet);
        region_start.or(PropertySets.borderSet);
        region_start.or(PropertySets.paddingSet);
        region_start.set(PropNames.CLIP);
        region_start.set(PropNames.DISPLAY_ALIGN);
        region_start.set(PropNames.EXTENT);
        region_start.set(PropNames.OVERFLOW);
        region_start.set(PropNames.REGION_NAME);
        region_start.set(PropNames.REFERENCE_ORIENTATION);
        region_start.set(PropNames.WRITING_MODE);
        foPropertyLists[FObjectNames.REGION_START] = new ROBitSet(region_start);

        //repeatable-page-master-alternatives
        BitSet repeatable_page_master_alternatives = new BitSet();
        repeatable_page_master_alternatives.set(PropNames.MAXIMUM_REPEATS);
        foPropertyLists[FObjectNames.REPEATABLE_PAGE_MASTER_ALTERNATIVES] = new ROBitSet(repeatable_page_master_alternatives);

        //repeatable-page-master-reference
        BitSet repeatable_page_master_reference = new BitSet();
        repeatable_page_master_reference.set(PropNames.MASTER_REFERENCE);
        repeatable_page_master_reference.set(PropNames.MAXIMUM_REPEATS);
        foPropertyLists[FObjectNames.REPEATABLE_PAGE_MASTER_REFERENCE] = new ROBitSet(repeatable_page_master_reference);

        //retrieve-marker
        BitSet retrieve_marker = new BitSet();
        retrieve_marker.set(PropNames.RETRIEVE_BOUNDARY);
        retrieve_marker.set(PropNames.RETRIEVE_CLASS_NAME);
        retrieve_marker.set(PropNames.RETRIEVE_POSITION);
        foPropertyLists[FObjectNames.RETRIEVE_MARKER] = new ROBitSet(retrieve_marker);

        //root
        BitSet root = new BitSet();
        root.set(PropNames.MEDIA_USAGE);
        foPropertyLists[FObjectNames.ROOT] = new ROBitSet(root);

        //simple-page-master
        BitSet simple_page_master = new BitSet();
        simple_page_master.or(PropertySets.marginBlockSet);
        simple_page_master.set(PropNames.MASTER_NAME);
        simple_page_master.set(PropNames.PAGE_HEIGHT);
        simple_page_master.set(PropNames.PAGE_WIDTH);
        simple_page_master.set(PropNames.REFERENCE_ORIENTATION);
        simple_page_master.set(PropNames.WRITING_MODE);
        foPropertyLists[FObjectNames.SIMPLE_PAGE_MASTER] = new ROBitSet(simple_page_master);

        //single-page-master-reference
        BitSet single_page_master_reference = new BitSet();
        single_page_master_reference.set(PropNames.MASTER_REFERENCE);
        foPropertyLists[FObjectNames.SINGLE_PAGE_MASTER_REFERENCE] = new ROBitSet(single_page_master_reference);

        //static-content
        BitSet static_content = new BitSet();
        static_content.set(PropNames.FLOW_NAME);
        foPropertyLists[FObjectNames.STATIC_CONTENT] = new ROBitSet(static_content);

        //table
        BitSet table = new BitSet();
        table.or(PropertySets.accessibilitySet);
        table.or(PropertySets.auralSet);
        table.or(PropertySets.backgroundSet);
        table.or(PropertySets.borderSet);
        table.or(PropertySets.marginBlockSet);
        table.or(PropertySets.paddingSet);
        table.or(PropertySets.relativePositionSet);
        table.set(PropNames.BLOCK_PROGRESSION_DIMENSION);
        table.set(PropNames.BORDER_AFTER_PRECEDENCE);
        table.set(PropNames.BORDER_BEFORE_PRECEDENCE);
        table.set(PropNames.BORDER_COLLAPSE);
        table.set(PropNames.BORDER_END_PRECEDENCE);
        table.set(PropNames.BORDER_SEPARATION);
        table.set(PropNames.BORDER_START_PRECEDENCE);
        table.set(PropNames.BREAK_AFTER);
        table.set(PropNames.BREAK_BEFORE);
        table.set(PropNames.ID);
        table.set(PropNames.INLINE_PROGRESSION_DIMENSION);
        table.set(PropNames.INTRUSION_DISPLACE);
        table.set(PropNames.HEIGHT);
        table.set(PropNames.KEEP_TOGETHER);
        table.set(PropNames.KEEP_WITH_NEXT);
        table.set(PropNames.KEEP_WITH_PREVIOUS);
        table.set(PropNames.TABLE_LAYOUT);
        table.set(PropNames.TABLE_OMIT_FOOTER_AT_BREAK);
        table.set(PropNames.TABLE_OMIT_HEADER_AT_BREAK);
        table.set(PropNames.WIDTH);
        table.set(PropNames.WRITING_MODE);
        foPropertyLists[FObjectNames.TABLE] = new ROBitSet(table);

        //table-and-caption
        BitSet table_and_caption = new BitSet();
        table_and_caption.or(PropertySets.accessibilitySet);
        table_and_caption.or(PropertySets.auralSet);
        table_and_caption.or(PropertySets.backgroundSet);
        table_and_caption.or(PropertySets.borderSet);
        table_and_caption.or(PropertySets.marginBlockSet);
        table_and_caption.or(PropertySets.paddingSet);
        table_and_caption.or(PropertySets.relativePositionSet);
        table_and_caption.set(PropNames.BREAK_AFTER);
        table_and_caption.set(PropNames.BREAK_BEFORE);
        table_and_caption.set(PropNames.CAPTION_SIDE);
        table_and_caption.set(PropNames.ID);
        table_and_caption.set(PropNames.INTRUSION_DISPLACE);
        table_and_caption.set(PropNames.KEEP_TOGETHER);
        table_and_caption.set(PropNames.KEEP_WITH_NEXT);
        table_and_caption.set(PropNames.KEEP_WITH_PREVIOUS);
        table_and_caption.set(PropNames.TEXT_ALIGN);
        foPropertyLists[FObjectNames.TABLE_AND_CAPTION] = new ROBitSet(table_and_caption);

        //table-body
        BitSet table_body = new BitSet();
        table_body.or(PropertySets.accessibilitySet);
        table_body.or(PropertySets.auralSet);
        table_body.or(PropertySets.backgroundSet);
        table_body.or(PropertySets.borderSet);
        table_body.or(PropertySets.relativePositionSet);
        table_body.set(PropNames.BORDER_AFTER_PRECEDENCE);
        table_body.set(PropNames.BORDER_BEFORE_PRECEDENCE);
        table_body.set(PropNames.BORDER_END_PRECEDENCE);
        table_body.set(PropNames.BORDER_START_PRECEDENCE);
        table_body.set(PropNames.ID);
        table_body.set(PropNames.VISIBILITY);
        foPropertyLists[FObjectNames.TABLE_BODY] = new ROBitSet(table_body);

        //table-caption
        BitSet table_caption = new BitSet();
        table_caption.or(PropertySets.accessibilitySet);
        table_caption.or(PropertySets.auralSet);
        table_caption.or(PropertySets.backgroundSet);
        table_caption.or(PropertySets.borderSet);
        table_caption.or(PropertySets.paddingSet);
        table_caption.or(PropertySets.relativePositionSet);
        table_caption.set(PropNames.BLOCK_PROGRESSION_DIMENSION);
        table_caption.set(PropNames.HEIGHT);
        table_caption.set(PropNames.ID);
        table_caption.set(PropNames.INLINE_PROGRESSION_DIMENSION);
        table_caption.set(PropNames.INTRUSION_DISPLACE);
        table_caption.set(PropNames.KEEP_TOGETHER);
        table_caption.set(PropNames.WIDTH);
        foPropertyLists[FObjectNames.TABLE_CAPTION] = new ROBitSet(table_caption);

        //table-cell
        BitSet table_cell = new BitSet();
        table_cell.or(PropertySets.accessibilitySet);
        table_cell.or(PropertySets.auralSet);
        table_cell.or(PropertySets.backgroundSet);
        table_cell.or(PropertySets.borderSet);
        table_cell.or(PropertySets.paddingSet);
        table_cell.or(PropertySets.relativePositionSet);
        table_cell.set(PropNames.BORDER_AFTER_PRECEDENCE);
        table_cell.set(PropNames.BORDER_BEFORE_PRECEDENCE);
        table_cell.set(PropNames.BORDER_END_PRECEDENCE);
        table_cell.set(PropNames.BORDER_START_PRECEDENCE);
        table_cell.set(PropNames.BLOCK_PROGRESSION_DIMENSION);
        table_cell.set(PropNames.COLUMN_NUMBER);
        table_cell.set(PropNames.DISPLAY_ALIGN);
        table_cell.set(PropNames.RELATIVE_ALIGN);
        table_cell.set(PropNames.EMPTY_CELLS);
        table_cell.set(PropNames.ENDS_ROW);
        table_cell.set(PropNames.HEIGHT);
        table_cell.set(PropNames.ID);
        table_cell.set(PropNames.INLINE_PROGRESSION_DIMENSION);
        table_cell.set(PropNames.NUMBER_COLUMNS_SPANNED);
        table_cell.set(PropNames.NUMBER_ROWS_SPANNED);
        table_cell.set(PropNames.STARTS_ROW);
        table_cell.set(PropNames.WIDTH);
        foPropertyLists[FObjectNames.TABLE_CELL] = new ROBitSet(table_cell);

        //table-column
        BitSet table_column = new BitSet();
        table_column.or(PropertySets.backgroundSet);
        table_column.or(PropertySets.borderSet);
        table_column.set(PropNames.BORDER_AFTER_PRECEDENCE);
        table_column.set(PropNames.BORDER_BEFORE_PRECEDENCE);
        table_column.set(PropNames.BORDER_END_PRECEDENCE);
        table_column.set(PropNames.BORDER_START_PRECEDENCE);
        table_column.set(PropNames.COLUMN_NUMBER);
        table_column.set(PropNames.COLUMN_WIDTH);
        table_column.set(PropNames.NUMBER_COLUMNS_REPEATED);
        table_column.set(PropNames.NUMBER_COLUMNS_SPANNED);
        table_column.set(PropNames.VISIBILITY);
        foPropertyLists[FObjectNames.TABLE_COLUMN] = new ROBitSet(table_column);

        //table-footer
        BitSet table_footer = new BitSet();
        table_footer.or(PropertySets.accessibilitySet);
        table_footer.or(PropertySets.auralSet);
        table_footer.or(PropertySets.backgroundSet);
        table_footer.or(PropertySets.borderSet);
        table_footer.or(PropertySets.relativePositionSet);
        table_footer.set(PropNames.BORDER_AFTER_PRECEDENCE);
        table_footer.set(PropNames.BORDER_BEFORE_PRECEDENCE);
        table_footer.set(PropNames.BORDER_END_PRECEDENCE);
        table_footer.set(PropNames.BORDER_START_PRECEDENCE);
        table_footer.set(PropNames.ID);
        table_footer.set(PropNames.VISIBILITY);
        foPropertyLists[FObjectNames.TABLE_FOOTER] = new ROBitSet(table_footer);

        //table-header
        BitSet table_header = new BitSet();
        table_header.or(PropertySets.accessibilitySet);
        table_header.or(PropertySets.auralSet);
        table_header.or(PropertySets.backgroundSet);
        table_header.or(PropertySets.borderSet);
        table_header.or(PropertySets.relativePositionSet);
        table_header.set(PropNames.BORDER_AFTER_PRECEDENCE);
        table_header.set(PropNames.BORDER_BEFORE_PRECEDENCE);
        table_header.set(PropNames.BORDER_END_PRECEDENCE);
        table_header.set(PropNames.BORDER_START_PRECEDENCE);
        table_header.set(PropNames.ID);
        table_header.set(PropNames.VISIBILITY);
        foPropertyLists[FObjectNames.TABLE_HEADER] = new ROBitSet(table_header);

        //table-row
        BitSet table_row = new BitSet();
        table_row.or(PropertySets.accessibilitySet);
        table_row.or(PropertySets.auralSet);
        table_row.or(PropertySets.backgroundSet);
        table_row.or(PropertySets.borderSet);
        table_row.or(PropertySets.relativePositionSet);
        table_row.set(PropNames.BLOCK_PROGRESSION_DIMENSION);
        table_row.set(PropNames.BORDER_AFTER_PRECEDENCE);
        table_row.set(PropNames.BORDER_BEFORE_PRECEDENCE);
        table_row.set(PropNames.BORDER_END_PRECEDENCE);
        table_row.set(PropNames.BORDER_START_PRECEDENCE);
        table_row.set(PropNames.BREAK_AFTER);
        table_row.set(PropNames.BREAK_BEFORE);
        table_row.set(PropNames.ID);
        table_row.set(PropNames.HEIGHT);
        table_row.set(PropNames.KEEP_TOGETHER);
        table_row.set(PropNames.KEEP_WITH_NEXT);
        table_row.set(PropNames.KEEP_WITH_PREVIOUS);
        table_row.set(PropNames.VISIBILITY);
        foPropertyLists[FObjectNames.TABLE_ROW] = new ROBitSet(table_row);

        //title
        BitSet title = new BitSet();
        title.or(PropertySets.accessibilitySet);
        title.or(PropertySets.auralSet);
        title.or(PropertySets.backgroundSet);
        title.or(PropertySets.borderSet);
        title.or(PropertySets.paddingSet);
        title.or(PropertySets.fontSet);
        title.or(PropertySets.marginInlineSet);
        title.set(PropNames.COLOR);
        title.set(PropNames.LINE_HEIGHT);
        title.set(PropNames.VISIBILITY);
        foPropertyLists[FObjectNames.TITLE] = new ROBitSet(title);

        //wrapper
        BitSet wrapper = new BitSet();
        wrapper.set(PropNames.ID);
        foPropertyLists[FObjectNames.WRAPPER] = new ROBitSet(wrapper);

    }

    // Following are the sets of properties which apply to particular
    // subtrees of the FO Tree.  This whole section is probably redundant.
    // If it is restored to full functioning, the public BitSet objects
    // must be replaced with unmodifiableSets.

    /**
     * Set of all properties available at fo:root.
     */
    public static final ROBitSet allProps;
    /**
     * Set of all inherited properties available at fo:root.
     */
    //public static final ROBitSet allInheritedProps;
    /**
     * Set of all non-inherited properties available at fo:root.
     */
    //public static final ROBitSet allNonInheritedProps;

    /**
     * set of all properties which are
     * usable within the declarations subtree.
     */
    public static final ROBitSet declarationsAll;
    /**
     * set of all inherted properties which are
     * usable within the declarations subtree.
     */
    //public static final ROBitSet declarationsInherited;
    /**
     * set of all non-inherited properties which are
     * usable within the declarations subtree.
     */
    //public static final ROBitSet declarationsNonInherited;

    /**
     * Set of properties for exclusive
     * use within the layout-master-set subtree.  These properties make no
     * sense in or under declarations or page-sequences.
     */
    //public static final ROBitSet layoutMasterOnly;

    /**
     * set of all properties which are
     * usable within the page-sequence-master-set subtree.
     */
    public static final ROBitSet seqMasterSet;
    /**
     * set of all inherited properties which are
     * usable within the page-sequence-master-set subtree.
     */
    //public static final ROBitSet inheritedSeqMasterSet;
    /**
     * set of all non-inherited properties which are
     * usable within the page-sequence-master-set subtree.
     */
    //public static final ROBitSet nonInheritedSeqMasterSet;

    /**
     * set of all properties which are
     * usable within the layout-master-set subtree.
     */
    public static final ROBitSet layoutMasterSet;
    /**
     * set of all inherited properties which are
     * usable within the layout-master-set subtree.
     */
    //public static final ROBitSet inheritedLayoutSet;
    /**
     * set of all non-inherited properties which are
     * usable within the layout-master-set subtree.
     */
    //public static final ROBitSet nonInheritedLayoutSet;

    /**
     * set of all properties which are
     * usable within the page sequence subtree.
     */
    public static final ROBitSet pageSeqSet;
    /**
     * set of all inherited properties which are
     * usable within the page sequence subtree.
     */
    //public static final ROBitSet inheritedPageSeqSet;
    /**
     * set of all non-inherited properties which are
     * usable within the page sequence subtree.
     */
    //public static final ROBitSet nonInheritedPageSeqSet;

    /**
     * set of all properties which are
     * usable within the fo:flow subtree.
     */
    public static final ROBitSet flowAllSet;
    /**
     * set of all inherited properties which are
     * usable within the fo:flow subtree.
     */
    //public static final ROBitSet inheritedFlowSet;
    /**
     * set of all non-inherite properties which are
     * usable within the fo:flow subtree.
     */
    //public static final ROBitSet nonInheritedFlowSet;

    /**
     * set of all properties which are
     * usable <i>within</i> the fo:marker subtree.
     */
    public static final ROBitSet markerAllSet;
    /**
     * set of all inherited properties which are
     * usable <i>within</i> the fo:marker subtree.
     */
    //public static final ROBitSet inheritedMarkerSet;
    /**
     * set of all non-inherited properties which are
     * usable <i>within</i> the fo:marker subtree.
     */
    //public static final ROBitSet nonInheritedMarkerSet;

    /**
     * set of all properties which are
     * usable within the fo:static-content subtree.
     */
    public static final ROBitSet staticAllSet;
    /**
     * set of all inherited properties which are
     * usable within the fo:static-content subtree.
     */
    //public static final ROBitSet inheritedStaticSet;
    /**
     * set of all non-inherited properties which are
     * usable within the fo:static-content subtree.
     */
    //public static final ROBitSet nonInheritedStaticSet;

    /*
    private static BitSet makeInheritedSet(BitSet set) {
        BitSet newset = new BitSet(set.size());
        newset.or(set);
        // This excludes the shorthand and compound properties, as they are
        // all non-inherited.
        newset.andNot(PropertyConsts.nonInheritedProps);
        return newset;
    }

    private static BitSet makeNonInheritedSet(BitSet set) {
        BitSet newset = new BitSet(set.size());
        newset.or(set);
        // This includes the shorthand and compound properties, as they are
        // all non-inherited.
        newset.and(PropertyConsts.nonInheritedProps);
        newset.andNot(ShorthandPropSets.shorthandCompoundProps);
        return newset;
    }
    */

    static {

        // fill the BitSet of all properties
        BitSet allprops = new BitSet(PropNames.LAST_PROPERTY_INDEX + 1);
        allprops.set(1, PropNames.LAST_PROPERTY_INDEX);

        allProps = new ROBitSet(allprops);
        //allInheritedProps =
                //new ROBitSet(makeInheritedSet(allprops));
        //allNonInheritedProps =
                //new ROBitSet(makeNonInheritedSet(allprops));

        //root only set of properties - properties for exclusive use on the
        // root element
        BitSet rootonly = new BitSet(PropNames.MEDIA_USAGE + 1);
        rootonly.set(PropNames.MEDIA_USAGE);

        //rootOnly = new ROBitSet(rootonly);

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
        // None of the declarations properties are inherited
        //declarationsInherited = new ROBitSet(new BitSet(1));
        //declarationsNonInherited = new ROBitSet(declarationsall);

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
        //inheritedSeqMasterSet = new ROBitSet(makeInheritedSet(seqmasterset));
        //nonInheritedSeqMasterSet
                            //= new ROBitSet(makeNonInheritedSet(seqmasterset));

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
        //inheritedLayoutSet =
            //new ROBitSet(makeInheritedSet(layoutmasterset));
        //nonInheritedLayoutSet =
            //new ROBitSet(makeNonInheritedSet(layoutmasterset));

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
        //inheritedPageSeqSet =
                    //new ROBitSet(makeInheritedSet(pageseqset));
        //nonInheritedPageSeqSet =
                //new ROBitSet(makeNonInheritedSet(pageseqset));

        BitSet flowallset = (BitSet)pageseqset.clone();
        flowallset.andNot(pageseqonly);
        flowallset.andNot(staticonlyset);

        flowAllSet = new ROBitSet(flowallset);
        //inheritedFlowSet =
                //new ROBitSet(makeInheritedSet(flowallset));
        //nonInheritedFlowSet =
                //new ROBitSet(makeNonInheritedSet(flowallset));

        BitSet staticallset = (BitSet)pageseqset.clone();
        staticallset.andNot(pageseqonly);
        staticallset.andNot(flowonlyset);

        staticAllSet = new ROBitSet(staticallset);
        //inheritedStaticSet =
                //new ROBitSet(makeInheritedSet(staticallset));
        //nonInheritedStaticSet =
            //new ROBitSet(makeNonInheritedSet(staticallset));

        BitSet markerallset = (BitSet)flowallset.clone();
        markerallset.clear(PropNames.MARKER_CLASS_NAME);

        markerAllSet = new ROBitSet(markerallset);
        //inheritedMarkerSet =
                //new ROBitSet(makeInheritedSet(markerallset));
        //nonInheritedMarkerSet =
            //new ROBitSet(makeNonInheritedSet(markerallset));
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

    static {
        String prefix = packageNamePrefix + ".";
        String foPrefix = "Fo";
        int namei = 0;	// Index of localName in FObjectNames.foLocalNames
        int pkgi = 1;	// Index of package suffix in foLocalNames

        foClassNames    = new String[FObjectNames.foLocalNames.length];
        foClassPackages = new String[FObjectNames.foLocalNames.length];
        foClasses       = new Class[FObjectNames.foLocalNames.length];
        foToIndex       = new HashMap(FObjectNames.foLocalNames.length);
        foClassToIndex  = new HashMap(FObjectNames.foLocalNames.length);

        for (int i = 1;i < FObjectNames.foLocalNames.length; i++) {
            String cname = foPrefix;
            StringTokenizer stoke =
                    new StringTokenizer(FObjectNames.foLocalNames[i][namei],
                                        "-");
            while (stoke.hasMoreTokens()) {
                String token = stoke.nextToken();
                String pname = new Character(
                                    Character.toUpperCase(token.charAt(0))
                                ).toString() + token.substring(1);
                cname = cname + pname;
            }
            foClassNames[i] = cname;

            // Set up the array of class package names
            String pkgname = prefix + FObjectNames.foLocalNames[i][pkgi];

            // Set up the array of Class objects, indexed by the fo
            // constants.
            String name = prefix + cname;
            try {
                foClasses[i] = Class.forName(name);
            } catch (ClassNotFoundException e) {}

            // Set up the foToIndex Hashmap with the name of the
            // flow object as a key, and the integer index as a value
            if (foToIndex.put((Object) FObjectNames.foLocalNames[i][namei],
                                        Ints.consts.get(i)) != null) {
                throw new RuntimeException(
                    "Duplicate values in propertyToIndex for key " +
                    FObjectNames.foLocalNames[i][namei]);
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

