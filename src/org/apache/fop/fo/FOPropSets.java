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
        block.set(PropNames.USAGE_CONTEXT_OF_SUPPRESS_AT_LINE_BREAK);
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

        //conditional-page-master-reference

        //declarations

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
        inline.set(PropNames.USAGE_CONTEXT_OF_SUPPRESS_AT_LINE_BREAK);
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
        page_number.set(PropNames.USAGE_CONTEXT_OF_SUPPRESS_AT_LINE_BREAK);
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
        page_number_citation.set(PropNames.USAGE_CONTEXT_OF_SUPPRESS_AT_LINE_BREAK);
        page_number_citation.set(PropNames.VISIBILITY);
        page_number_citation.set(PropNames.WORD_SPACING);
        page_number_citation.set(PropNames.WRAP_OPTION);
        foPropertyLists[FObjectNames.PAGE_NUMBER_CITATION] = new ROBitSet(page_number_citation);

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
        BitSet retrieve_marker = new BitSet();
        retrieve_marker.set(PropNames.RETRIEVE_BOUNDARY);
        retrieve_marker.set(PropNames.RETRIEVE_CLASS_NAME);
        retrieve_marker.set(PropNames.RETRIEVE_POSITION);
        foPropertyLists[FObjectNames.RETRIEVE_MARKER] = new ROBitSet(retrieve_marker);

        //root

        //simple-page-master

        //single-page-master-reference

        //static-content

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

        //wrapper
        BitSet wrapper = new BitSet();
        wrapper.set(PropNames.ID);
        foPropertyLists[FObjectNames.WRAPPER] = new ROBitSet(wrapper);

    }

}

