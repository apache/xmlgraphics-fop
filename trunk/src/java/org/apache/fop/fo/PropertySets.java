/*
 * Copyright 1999-2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */


package org.apache.fop.fo;

import org.apache.fop.fo.Constants;
import java.util.BitSet;
import java.util.ArrayList;

public class PropertySets {
    private static short[][] mapping = null;
    private static BitSet can_have_markers = null;
    private static BitSet no_inline_areas = null;

    private Element[] elements = new Element[Constants.FRM_OBJ_COUNT+1];
    private BitSet block_elems = new BitSet();
    private BitSet inline_elems = new BitSet();

    BitSet CommonAccessibilityProperties = new BitSet();
    BitSet CommonAbsolutePositionProperties = new BitSet();
    BitSet CommonAuralProperties = new BitSet();
    BitSet CommonBorderPaddingBackgroundProperties = new BitSet();
    BitSet CommonFontProperties = new BitSet();
    BitSet CommonHyphenationProperties = new BitSet();
    BitSet CommonMarginPropertiesBlock = new BitSet();
    BitSet CommonMarginPropertiesInline = new BitSet();
    BitSet CommonRelativePositionProperties = new BitSet();

    public void initializeElements() {
        block_elems.set(Constants.FO_BLOCK);
        block_elems.set(Constants.FO_BLOCK_CONTAINER);
        block_elems.set(Constants.FO_TABLE_AND_CAPTION);
        block_elems.set(Constants.FO_TABLE);
        block_elems.set(Constants.FO_LIST_BLOCK);

        inline_elems.set(Constants.FO_BIDI_OVERRIDE);
        inline_elems.set(Constants.FO_CHARACTER);
        inline_elems.set(Constants.FO_EXTERNAL_GRAPHIC);
        inline_elems.set(Constants.FO_INSTREAM_FOREIGN_OBJECT);
        inline_elems.set(Constants.FO_INLINE);
        inline_elems.set(Constants.FO_INLINE_CONTAINER);
        inline_elems.set(Constants.FO_LEADER);
        inline_elems.set(Constants.FO_PAGE_NUMBER);
        inline_elems.set(Constants.FO_PAGE_NUMBER_CITATION);
        inline_elems.set(Constants.FO_BASIC_LINK);
        inline_elems.set(Constants.FO_MULTI_TOGGLE);
    }

    public void initializeCommon() {
        CommonAccessibilityProperties.set(Constants.PR_SOURCE_DOCUMENT);
        CommonAccessibilityProperties.set(Constants.PR_ROLE);

        CommonAbsolutePositionProperties.set(Constants.PR_ABSOLUTE_POSITION);
        CommonAbsolutePositionProperties.set(Constants.PR_POSITION);
        CommonAbsolutePositionProperties.set(Constants.PR_TOP);
        CommonAbsolutePositionProperties.set(Constants.PR_RIGHT);
        CommonAbsolutePositionProperties.set(Constants.PR_BOTTOM);
        CommonAbsolutePositionProperties.set(Constants.PR_LEFT);

        CommonAuralProperties.set(Constants.PR_AZIMUTH);
        CommonAuralProperties.set(Constants.PR_CUE_AFTER);
        CommonAuralProperties.set(Constants.PR_CUE_BEFORE);
        CommonAuralProperties.set(Constants.PR_CUE);
        CommonAuralProperties.set(Constants.PR_ELEVATION);
        CommonAuralProperties.set(Constants.PR_PAUSE_AFTER);
        CommonAuralProperties.set(Constants.PR_PAUSE_BEFORE);
        CommonAuralProperties.set(Constants.PR_PAUSE);
        CommonAuralProperties.set(Constants.PR_PITCH);
        CommonAuralProperties.set(Constants.PR_PITCH_RANGE);
        CommonAuralProperties.set(Constants.PR_PLAY_DURING);
        CommonAuralProperties.set(Constants.PR_RICHNESS);
        CommonAuralProperties.set(Constants.PR_SPEAK);
        CommonAuralProperties.set(Constants.PR_SPEAK_HEADER);
        CommonAuralProperties.set(Constants.PR_SPEAK_NUMERAL);
        CommonAuralProperties.set(Constants.PR_SPEAK_PUNCTUATION);
        CommonAuralProperties.set(Constants.PR_SPEECH_RATE);
        CommonAuralProperties.set(Constants.PR_STRESS);
        CommonAuralProperties.set(Constants.PR_VOICE_FAMILY);
        CommonAuralProperties.set(Constants.PR_VOLUME);

        CommonBorderPaddingBackgroundProperties.set(Constants.PR_BACKGROUND_ATTACHMENT);
        CommonBorderPaddingBackgroundProperties.set(Constants.PR_BACKGROUND_COLOR);
        CommonBorderPaddingBackgroundProperties.set(Constants.PR_BACKGROUND_IMAGE);
        CommonBorderPaddingBackgroundProperties.set(Constants.PR_BACKGROUND_REPEAT);
        CommonBorderPaddingBackgroundProperties.set(Constants.PR_BACKGROUND_POSITION_HORIZONTAL);
        CommonBorderPaddingBackgroundProperties.set(Constants.PR_BACKGROUND_POSITION_VERTICAL);
        CommonBorderPaddingBackgroundProperties.set(Constants.PR_BORDER_BEFORE_COLOR);
        CommonBorderPaddingBackgroundProperties.set(Constants.PR_BORDER_BEFORE_STYLE);
        CommonBorderPaddingBackgroundProperties.set(Constants.PR_BORDER_BEFORE_WIDTH);
        CommonBorderPaddingBackgroundProperties.set(Constants.PR_BORDER_AFTER_COLOR);
        CommonBorderPaddingBackgroundProperties.set(Constants.PR_BORDER_AFTER_STYLE);
        CommonBorderPaddingBackgroundProperties.set(Constants.PR_BORDER_AFTER_WIDTH);
        CommonBorderPaddingBackgroundProperties.set(Constants.PR_BORDER_START_COLOR);
        CommonBorderPaddingBackgroundProperties.set(Constants.PR_BORDER_START_STYLE);
        CommonBorderPaddingBackgroundProperties.set(Constants.PR_BORDER_START_WIDTH);
        CommonBorderPaddingBackgroundProperties.set(Constants.PR_BORDER_END_COLOR);
        CommonBorderPaddingBackgroundProperties.set(Constants.PR_BORDER_END_STYLE);
        CommonBorderPaddingBackgroundProperties.set(Constants.PR_BORDER_END_WIDTH);
        CommonBorderPaddingBackgroundProperties.set(Constants.PR_BORDER_TOP_COLOR);
        CommonBorderPaddingBackgroundProperties.set(Constants.PR_BORDER_TOP_STYLE);
        CommonBorderPaddingBackgroundProperties.set(Constants.PR_BORDER_TOP_WIDTH);
        CommonBorderPaddingBackgroundProperties.set(Constants.PR_BORDER_BOTTOM_COLOR);
        CommonBorderPaddingBackgroundProperties.set(Constants.PR_BORDER_BOTTOM_STYLE);
        CommonBorderPaddingBackgroundProperties.set(Constants.PR_BORDER_BOTTOM_WIDTH);
        CommonBorderPaddingBackgroundProperties.set(Constants.PR_BORDER_LEFT_COLOR);
        CommonBorderPaddingBackgroundProperties.set(Constants.PR_BORDER_LEFT_STYLE);
        CommonBorderPaddingBackgroundProperties.set(Constants.PR_BORDER_LEFT_WIDTH);
        CommonBorderPaddingBackgroundProperties.set(Constants.PR_BORDER_RIGHT_COLOR);
        CommonBorderPaddingBackgroundProperties.set(Constants.PR_BORDER_RIGHT_STYLE);
        CommonBorderPaddingBackgroundProperties.set(Constants.PR_BORDER_RIGHT_WIDTH);
        CommonBorderPaddingBackgroundProperties.set(Constants.PR_PADDING_BEFORE);
        CommonBorderPaddingBackgroundProperties.set(Constants.PR_PADDING_AFTER);
        CommonBorderPaddingBackgroundProperties.set(Constants.PR_PADDING_START);
        CommonBorderPaddingBackgroundProperties.set(Constants.PR_PADDING_END);
        CommonBorderPaddingBackgroundProperties.set(Constants.PR_PADDING_TOP);
        CommonBorderPaddingBackgroundProperties.set(Constants.PR_PADDING_BOTTOM);
        CommonBorderPaddingBackgroundProperties.set(Constants.PR_PADDING_LEFT);
        CommonBorderPaddingBackgroundProperties.set(Constants.PR_PADDING_RIGHT);
        CommonBorderPaddingBackgroundProperties.set(Constants.PR_BORDER);
        CommonBorderPaddingBackgroundProperties.set(Constants.PR_BORDER_STYLE);
        CommonBorderPaddingBackgroundProperties.set(Constants.PR_BORDER_WIDTH);
        CommonBorderPaddingBackgroundProperties.set(Constants.PR_BORDER_COLOR);
        CommonBorderPaddingBackgroundProperties.set(Constants.PR_BORDER_TOP);
        CommonBorderPaddingBackgroundProperties.set(Constants.PR_BORDER_BOTTOM);
        CommonBorderPaddingBackgroundProperties.set(Constants.PR_BORDER_LEFT);
        CommonBorderPaddingBackgroundProperties.set(Constants.PR_BORDER_RIGHT);
        CommonBorderPaddingBackgroundProperties.set(Constants.PR_PADDING);

        CommonFontProperties.set(Constants.PR_FONT);
        CommonFontProperties.set(Constants.PR_FONT_FAMILY);
        CommonFontProperties.set(Constants.PR_FONT_SELECTION_STRATEGY);
        CommonFontProperties.set(Constants.PR_FONT_SIZE);
        CommonFontProperties.set(Constants.PR_FONT_STRETCH);
        CommonFontProperties.set(Constants.PR_FONT_SIZE_ADJUST);
        CommonFontProperties.set(Constants.PR_FONT_STYLE);
        CommonFontProperties.set(Constants.PR_FONT_VARIANT);
        CommonFontProperties.set(Constants.PR_FONT_WEIGHT);

        CommonHyphenationProperties.set(Constants.PR_COUNTRY);
        CommonHyphenationProperties.set(Constants.PR_LANGUAGE);
        CommonHyphenationProperties.set(Constants.PR_SCRIPT);
        CommonHyphenationProperties.set(Constants.PR_HYPHENATE);
        CommonHyphenationProperties.set(Constants.PR_HYPHENATION_CHARACTER);
        CommonHyphenationProperties.set(Constants.PR_HYPHENATION_PUSH_CHARACTER_COUNT);
        CommonHyphenationProperties.set(Constants.PR_HYPHENATION_REMAIN_CHARACTER_COUNT);

        CommonMarginPropertiesBlock.set(Constants.PR_MARGIN);
        CommonMarginPropertiesBlock.set(Constants.PR_MARGIN_TOP);
        CommonMarginPropertiesBlock.set(Constants.PR_MARGIN_BOTTOM);
        CommonMarginPropertiesBlock.set(Constants.PR_MARGIN_LEFT);
        CommonMarginPropertiesBlock.set(Constants.PR_MARGIN_RIGHT);
        CommonMarginPropertiesBlock.set(Constants.PR_SPACE_BEFORE);
        CommonMarginPropertiesBlock.set(Constants.PR_SPACE_AFTER);
        CommonMarginPropertiesBlock.set(Constants.PR_START_INDENT);
        CommonMarginPropertiesBlock.set(Constants.PR_END_INDENT);

        CommonMarginPropertiesInline.set(Constants.PR_SPACE_END);
        CommonMarginPropertiesInline.set(Constants.PR_SPACE_START);

        CommonRelativePositionProperties.set(Constants.PR_RELATIVE_POSITION);


    }

    public void initialize() {
        // define the fo: elements
        for (int i = 1; i < elements.length; i++) {
            elements[i] = new Element(i);
        }

        // populate the elements with properties and content elements.
        Element elem;
        elem = elements[Constants.FO_ROOT];
        elem.addProperty(Constants.PR_MEDIA_USAGE);
        elem.addContent(Constants.FO_LAYOUT_MASTER_SET);
        elem.addContent(Constants.FO_DECLARATIONS);
        elem.addContent(Constants.FO_PAGE_SEQUENCE);

        elem = elements[Constants.FO_DECLARATIONS];
        elem.addContent(Constants.FO_COLOR_PROFILE);

        elem = elements[Constants.FO_COLOR_PROFILE];
        elem.addProperty(Constants.PR_SRC);
        elem.addProperty(Constants.PR_COLOR_PROFILE_NAME);
        elem.addProperty(Constants.PR_RENDERING_INTENT);

        elem = elements[Constants.FO_BOOKMARK_TREE];
        elem.addContent(Constants.FO_BOOKMARK);

        elem = elements[Constants.FO_BOOKMARK];
        elem.addContent(Constants.FO_BOOKMARK_TITLE);
        elem.addContent(Constants.FO_BOOKMARK);
        elem.addProperties(CommonAccessibilityProperties);
        elem.addProperty(Constants.PR_EXTERNAL_DESTINATION);
        elem.addProperty(Constants.PR_INTERNAL_DESTINATION);
        elem.addProperty(Constants.PR_STARTING_STATE);

        elem = elements[Constants.FO_BOOKMARK_TITLE];
        elem.addProperties(CommonAccessibilityProperties);
        elem.addProperty(Constants.PR_FONT_STYLE);
        elem.addProperty(Constants.PR_FONT_WEIGHT);

        elem = elements[Constants.FO_PAGE_SEQUENCE_WRAPPER];
        elem.addProperty(Constants.PR_ID);
        elem.addProperty(Constants.PR_INDEX_CLASS);
        elem.addProperty(Constants.PR_INDEX_KEY);

        elem = elements[Constants.FO_PAGE_SEQUENCE];
        elem.addProperty(Constants.PR_COUNTRY);
        elem.addProperty(Constants.PR_FORMAT);
        elem.addProperty(Constants.PR_LANGUAGE);
        elem.addProperty(Constants.PR_LETTER_VALUE);
        elem.addProperty(Constants.PR_GROUPING_SEPARATOR);
        elem.addProperty(Constants.PR_GROUPING_SIZE);
        elem.addProperty(Constants.PR_ID);
        elem.addProperty(Constants.PR_INITIAL_PAGE_NUMBER);
        elem.addProperty(Constants.PR_FORCE_PAGE_COUNT);
        elem.addProperty(Constants.PR_MASTER_REFERENCE);
        elem.addContent(Constants.FO_TITLE);
        elem.addContent(Constants.FO_STATIC_CONTENT);
        elem.addContent(Constants.FO_FLOW);

        elem = elements[Constants.FO_LAYOUT_MASTER_SET];
        elem.addProperty(Constants.PR_MASTER_NAME);
        elem.addContent(Constants.FO_SIMPLE_PAGE_MASTER);
        elem.addContent(Constants.FO_PAGE_SEQUENCE_MASTER);

        elem = elements[Constants.FO_PAGE_SEQUENCE_MASTER];
        elem.addProperty(Constants.PR_MASTER_NAME);
        elem.addContent(Constants.FO_SINGLE_PAGE_MASTER_REFERENCE);
        elem.addContent(Constants.FO_REPEATABLE_PAGE_MASTER_REFERENCE);
        elem.addContent(Constants.FO_REPEATABLE_PAGE_MASTER_ALTERNATIVES);

        elem = elements[Constants.FO_SINGLE_PAGE_MASTER_REFERENCE];
        elem.addProperty(Constants.PR_MASTER_REFERENCE);

        elem = elements[Constants.FO_REPEATABLE_PAGE_MASTER_REFERENCE];
        elem.addProperty(Constants.PR_MASTER_REFERENCE);
        elem.addProperty(Constants.PR_MAXIMUM_REPEATS);

        elem = elements[Constants.FO_REPEATABLE_PAGE_MASTER_ALTERNATIVES];
        elem.addProperty(Constants.PR_MAXIMUM_REPEATS);
        elem.addContent(Constants.FO_CONDITIONAL_PAGE_MASTER_REFERENCE);

        elem = elements[Constants.FO_CONDITIONAL_PAGE_MASTER_REFERENCE];
        elem.addProperty(Constants.PR_MASTER_REFERENCE);
        elem.addProperty(Constants.PR_PAGE_POSITION);
        elem.addProperty(Constants.PR_ODD_OR_EVEN);
        elem.addProperty(Constants.PR_BLANK_OR_NOT_BLANK);

        elem = elements[Constants.FO_SIMPLE_PAGE_MASTER];
        elem.addProperties(CommonMarginPropertiesBlock);
        elem.addProperty(Constants.PR_MASTER_NAME);
        elem.addProperty(Constants.PR_PAGE_HEIGHT);
        elem.addProperty(Constants.PR_PAGE_WIDTH);
        elem.addProperty(Constants.PR_REFERENCE_ORIENTATION);
        elem.addProperty(Constants.PR_WRITING_MODE);
        elem.addContent(Constants.FO_REGION_BODY);
        elem.addContent(Constants.FO_REGION_BEFORE);
        elem.addContent(Constants.FO_REGION_AFTER);
        elem.addContent(Constants.FO_REGION_START);
        elem.addContent(Constants.FO_REGION_END);

        elem = elements[Constants.FO_REGION_BODY];
        elem.addProperties(CommonBorderPaddingBackgroundProperties);
        elem.addProperties(CommonMarginPropertiesBlock);
        elem.addProperty(Constants.PR_CLIP);
        elem.addProperty(Constants.PR_COLUMN_COUNT);
        elem.addProperty(Constants.PR_COLUMN_GAP);
        elem.addProperty(Constants.PR_DISPLAY_ALIGN);
        elem.addProperty(Constants.PR_OVERFLOW);
        elem.addProperty(Constants.PR_REGION_NAME);
        elem.addProperty(Constants.PR_REFERENCE_ORIENTATION);
        elem.addProperty(Constants.PR_WRITING_MODE);

        elem = elements[Constants.FO_REGION_BEFORE];
        elem.addProperties(CommonBorderPaddingBackgroundProperties);
        elem.addProperty(Constants.PR_CLIP);
        elem.addProperty(Constants.PR_DISPLAY_ALIGN);
        elem.addProperty(Constants.PR_EXTENT);
        elem.addProperty(Constants.PR_OVERFLOW);
        elem.addProperty(Constants.PR_PRECEDENCE);
        elem.addProperty(Constants.PR_REGION_NAME);
        elem.addProperty(Constants.PR_REFERENCE_ORIENTATION);
        elem.addProperty(Constants.PR_WRITING_MODE);

        elem = elements[Constants.FO_REGION_AFTER];
        elem.addProperties(CommonBorderPaddingBackgroundProperties);
        elem.addProperty(Constants.PR_CLIP);
        elem.addProperty(Constants.PR_DISPLAY_ALIGN);
        elem.addProperty(Constants.PR_EXTENT);
        elem.addProperty(Constants.PR_OVERFLOW);
        elem.addProperty(Constants.PR_PRECEDENCE);
        elem.addProperty(Constants.PR_REGION_NAME);
        elem.addProperty(Constants.PR_REFERENCE_ORIENTATION);
        elem.addProperty(Constants.PR_WRITING_MODE);

        elem = elements[Constants.FO_REGION_START];
        elem.addProperties(CommonBorderPaddingBackgroundProperties);
        elem.addProperty(Constants.PR_CLIP);
        elem.addProperty(Constants.PR_DISPLAY_ALIGN);
        elem.addProperty(Constants.PR_EXTENT);
        elem.addProperty(Constants.PR_OVERFLOW);
        elem.addProperty(Constants.PR_REGION_NAME);
        elem.addProperty(Constants.PR_REFERENCE_ORIENTATION);
        elem.addProperty(Constants.PR_WRITING_MODE);

        elem = elements[Constants.FO_REGION_END];
        elem.addProperties(CommonBorderPaddingBackgroundProperties);
        elem.addProperty(Constants.PR_CLIP);
        elem.addProperty(Constants.PR_DISPLAY_ALIGN);
        elem.addProperty(Constants.PR_EXTENT);
        elem.addProperty(Constants.PR_OVERFLOW);
        elem.addProperty(Constants.PR_REGION_NAME);
        elem.addProperty(Constants.PR_REFERENCE_ORIENTATION);
        elem.addProperty(Constants.PR_WRITING_MODE);

        elem = elements[Constants.FO_FLOW];
        elem.addProperty(Constants.PR_FLOW_NAME);
        elem.addContent(block_elems);

        elem = elements[Constants.FO_STATIC_CONTENT];
        elem.addProperty(Constants.PR_FLOW_NAME);
        elem.addContent(block_elems);

        elem = elements[Constants.FO_TITLE];
        elem.addProperties(CommonAccessibilityProperties);
        elem.addProperties(CommonAuralProperties);
        elem.addProperties(CommonBorderPaddingBackgroundProperties);
        elem.addProperties(CommonFontProperties);
        elem.addProperties(CommonMarginPropertiesInline);
        elem.addProperty(Constants.PR_COLOR);
        elem.addProperty(Constants.PR_LINE_HEIGHT);
        elem.addProperty(Constants.PR_VISIBILITY);
        elem.addContent(inline_elems);

        elem = elements[Constants.FO_BLOCK];
        elem.addProperties(CommonAccessibilityProperties);
        elem.addProperties(CommonAuralProperties);
        elem.addProperties(CommonBorderPaddingBackgroundProperties);
        elem.addProperties(CommonFontProperties);
        elem.addProperties(CommonHyphenationProperties);
        elem.addProperties(CommonMarginPropertiesBlock);
        elem.addProperties(CommonRelativePositionProperties);
        elem.addProperty(Constants.PR_PAGE_BREAK_AFTER);
        elem.addProperty(Constants.PR_PAGE_BREAK_BEFORE);
        elem.addProperty(Constants.PR_BREAK_AFTER);
        elem.addProperty(Constants.PR_BREAK_BEFORE);
        elem.addProperty(Constants.PR_COLOR);
        elem.addProperty(Constants.PR_TEXT_DEPTH);
        elem.addProperty(Constants.PR_TEXT_ALTITUDE);
        elem.addProperty(Constants.PR_HYPHENATION_KEEP);
        elem.addProperty(Constants.PR_HYPHENATION_LADDER_COUNT);
        elem.addProperty(Constants.PR_ID);
        elem.addProperty(Constants.PR_PAGE_BREAK_INSIDE);
        elem.addProperty(Constants.PR_KEEP_TOGETHER);
        elem.addProperty(Constants.PR_KEEP_WITH_NEXT);
        elem.addProperty(Constants.PR_KEEP_WITH_PREVIOUS);
        elem.addProperty(Constants.PR_LAST_LINE_END_INDENT);
        elem.addProperty(Constants.PR_LINEFEED_TREATMENT);
        elem.addProperty(Constants.PR_LINE_HEIGHT);
        elem.addProperty(Constants.PR_LINE_HEIGHT_SHIFT_ADJUSTMENT);
        elem.addProperty(Constants.PR_LINE_STACKING_STRATEGY);
        elem.addProperty(Constants.PR_ORPHANS);
        elem.addProperty(Constants.PR_WHITE_SPACE_TREATMENT);
        elem.addProperty(Constants.PR_SPAN);
        elem.addProperty(Constants.PR_TEXT_ALIGN);
        elem.addProperty(Constants.PR_TEXT_ALIGN_LAST);
        elem.addProperty(Constants.PR_TEXT_INDENT);
        elem.addProperty(Constants.PR_VISIBILITY);
        elem.addProperty(Constants.PR_WHITE_SPACE_COLLAPSE);
        elem.addProperty(Constants.PR_WIDOWS);
        elem.addProperty(Constants.PR_WRAP_OPTION);
        elem.addContent(inline_elems);
        elem.addContent(block_elems);

        elem = elements[Constants.FO_BLOCK_CONTAINER];
        elem.addProperties(CommonAbsolutePositionProperties);
        elem.addProperties(CommonBorderPaddingBackgroundProperties);
        elem.addProperties(CommonMarginPropertiesBlock);
        elem.addProperty(Constants.PR_BLOCK_PROGRESSION_DIMENSION);
        elem.addProperty(Constants.PR_X_BLOCK_PROGRESSION_UNIT);
        elem.addProperty(Constants.PR_PAGE_BREAK_AFTER);
        elem.addProperty(Constants.PR_PAGE_BREAK_BEFORE);
        elem.addProperty(Constants.PR_BREAK_AFTER);
        elem.addProperty(Constants.PR_BREAK_BEFORE);
        elem.addProperty(Constants.PR_CLIP);
        elem.addProperty(Constants.PR_DISPLAY_ALIGN);
        elem.addProperty(Constants.PR_HEIGHT);
        elem.addProperty(Constants.PR_ID);
        elem.addProperty(Constants.PR_INLINE_PROGRESSION_DIMENSION);
        elem.addProperty(Constants.PR_PAGE_BREAK_INSIDE);
        elem.addProperty(Constants.PR_KEEP_TOGETHER);
        elem.addProperty(Constants.PR_KEEP_WITH_NEXT);
        elem.addProperty(Constants.PR_KEEP_WITH_PREVIOUS);
        elem.addProperty(Constants.PR_OVERFLOW);
        elem.addProperty(Constants.PR_REFERENCE_ORIENTATION);
        elem.addProperty(Constants.PR_SPAN);
        elem.addProperty(Constants.PR_WIDTH);
        elem.addProperty(Constants.PR_WRITING_MODE);
        elem.addProperty(Constants.PR_Z_INDEX);
        elem.addContent(block_elems);

        elem = elements[Constants.FO_BIDI_OVERRIDE];
        elem.addProperties(CommonAuralProperties);
        elem.addProperties(CommonFontProperties);
        elem.addProperties(CommonRelativePositionProperties);
        elem.addProperty(Constants.PR_COLOR);
        elem.addProperty(Constants.PR_DIRECTION);
        elem.addProperty(Constants.PR_ID);
        elem.addProperty(Constants.PR_LETTER_SPACING);
        elem.addProperty(Constants.PR_LINE_HEIGHT);
        elem.addProperty(Constants.PR_SCORE_SPACES);
        elem.addProperty(Constants.PR_UNICODE_BIDI);
        elem.addProperty(Constants.PR_WORD_SPACING);
        elem.addContent(inline_elems);
        elem.addContent(block_elems);

        elem = elements[Constants.FO_CHARACTER];
        elem.addProperties(CommonAuralProperties);
        elem.addProperties(CommonBorderPaddingBackgroundProperties);
        elem.addProperties(CommonFontProperties);
        elem.addProperties(CommonHyphenationProperties);
        elem.addProperties(CommonMarginPropertiesInline);
        elem.addProperties(CommonRelativePositionProperties);
        elem.addProperty(Constants.PR_VERTICAL_ALIGN);
        elem.addProperty(Constants.PR_ALIGNMENT_ADJUST);
        elem.addProperty(Constants.PR_TREAT_AS_WORD_SPACE);
        elem.addProperty(Constants.PR_ALIGNMENT_BASELINE);
        elem.addProperty(Constants.PR_BASELINE_SHIFT);
        elem.addProperty(Constants.PR_CHARACTER);
        elem.addProperty(Constants.PR_COLOR);
        elem.addProperty(Constants.PR_DOMINANT_BASELINE);
        elem.addProperty(Constants.PR_TEXT_DEPTH);
        elem.addProperty(Constants.PR_TEXT_ALTITUDE);
        elem.addProperty(Constants.PR_GLYPH_ORIENTATION_HORIZONTAL);
        elem.addProperty(Constants.PR_GLYPH_ORIENTATION_VERTICAL);
        elem.addProperty(Constants.PR_ID);
        elem.addProperty(Constants.PR_KEEP_WITH_NEXT);
        elem.addProperty(Constants.PR_KEEP_WITH_PREVIOUS);
        elem.addProperty(Constants.PR_LETTER_SPACING);
        elem.addProperty(Constants.PR_LINE_HEIGHT);
        elem.addProperty(Constants.PR_SCORE_SPACES);
        elem.addProperty(Constants.PR_SUPPRESS_AT_LINE_BREAK);
        elem.addProperty(Constants.PR_TEXT_DECORATION);
        elem.addProperty(Constants.PR_TEXT_SHADOW);
        elem.addProperty(Constants.PR_TEXT_TRANSFORM);
        elem.addProperty(Constants.PR_VISIBILITY);
        elem.addProperty(Constants.PR_WORD_SPACING);

        elem = elements[Constants.FO_INITIAL_PROPERTY_SET];
        elem.addProperties(CommonAccessibilityProperties);
        elem.addProperties(CommonAuralProperties);
        elem.addProperties(CommonBorderPaddingBackgroundProperties);
        elem.addProperties(CommonFontProperties);
        elem.addProperties(CommonRelativePositionProperties);
        elem.addProperty(Constants.PR_COLOR);
        elem.addProperty(Constants.PR_ID);
        elem.addProperty(Constants.PR_LETTER_SPACING);
        elem.addProperty(Constants.PR_LINE_HEIGHT);
        elem.addProperty(Constants.PR_SCORE_SPACES);
        elem.addProperty(Constants.PR_TEXT_DECORATION);
        elem.addProperty(Constants.PR_TEXT_SHADOW);
        elem.addProperty(Constants.PR_TEXT_TRANSFORM);
        elem.addProperty(Constants.PR_WORD_SPACING);

        elem = elements[Constants.FO_EXTERNAL_GRAPHIC];
        elem.addProperties(CommonAccessibilityProperties);
        elem.addProperties(CommonAuralProperties);
        elem.addProperties(CommonBorderPaddingBackgroundProperties);
        elem.addProperties(CommonMarginPropertiesInline);
        elem.addProperties(CommonRelativePositionProperties);
        elem.addProperty(Constants.PR_VERTICAL_ALIGN);
        elem.addProperty(Constants.PR_ALIGNMENT_ADJUST);
        elem.addProperty(Constants.PR_ALIGNMENT_BASELINE);
        elem.addProperty(Constants.PR_BASELINE_SHIFT);
        elem.addProperty(Constants.PR_BLOCK_PROGRESSION_DIMENSION);
        elem.addProperty(Constants.PR_CLIP);
        elem.addProperty(Constants.PR_CONTENT_HEIGHT);
        elem.addProperty(Constants.PR_CONTENT_TYPE);
        elem.addProperty(Constants.PR_CONTENT_WIDTH);
        elem.addProperty(Constants.PR_DISPLAY_ALIGN);
        elem.addProperty(Constants.PR_DOMINANT_BASELINE);
        elem.addProperty(Constants.PR_HEIGHT);
        elem.addProperty(Constants.PR_ID);
        elem.addProperty(Constants.PR_INLINE_PROGRESSION_DIMENSION);
        elem.addProperty(Constants.PR_KEEP_WITH_NEXT);
        elem.addProperty(Constants.PR_KEEP_WITH_PREVIOUS);
        elem.addProperty(Constants.PR_LINE_HEIGHT);
        elem.addProperty(Constants.PR_OVERFLOW);
        elem.addProperty(Constants.PR_SCALING);
        elem.addProperty(Constants.PR_SCALING_METHOD);
        elem.addProperty(Constants.PR_SRC);
        elem.addProperty(Constants.PR_TEXT_ALIGN);
        elem.addProperty(Constants.PR_WIDTH);

        elem = elements[Constants.FO_INSTREAM_FOREIGN_OBJECT];
        elem.addProperties(CommonAccessibilityProperties);
        elem.addProperties(CommonAuralProperties);
        elem.addProperties(CommonBorderPaddingBackgroundProperties);
        elem.addProperties(CommonMarginPropertiesInline);
        elem.addProperties(CommonRelativePositionProperties);
        elem.addProperty(Constants.PR_VERTICAL_ALIGN);
        elem.addProperty(Constants.PR_ALIGNMENT_ADJUST);
        elem.addProperty(Constants.PR_ALIGNMENT_BASELINE);
        elem.addProperty(Constants.PR_BASELINE_SHIFT);
        elem.addProperty(Constants.PR_BLOCK_PROGRESSION_DIMENSION);
        elem.addProperty(Constants.PR_CLIP);
        elem.addProperty(Constants.PR_CONTENT_HEIGHT);
        elem.addProperty(Constants.PR_CONTENT_TYPE);
        elem.addProperty(Constants.PR_CONTENT_WIDTH);
        elem.addProperty(Constants.PR_DISPLAY_ALIGN);
        elem.addProperty(Constants.PR_DOMINANT_BASELINE);
        elem.addProperty(Constants.PR_HEIGHT);
        elem.addProperty(Constants.PR_ID);
        elem.addProperty(Constants.PR_INLINE_PROGRESSION_DIMENSION);
        elem.addProperty(Constants.PR_KEEP_WITH_NEXT);
        elem.addProperty(Constants.PR_KEEP_WITH_PREVIOUS);
        elem.addProperty(Constants.PR_LINE_HEIGHT);
        elem.addProperty(Constants.PR_OVERFLOW);
        elem.addProperty(Constants.PR_SCALING);
        elem.addProperty(Constants.PR_SCALING_METHOD);
        elem.addProperty(Constants.PR_TEXT_ALIGN);
        elem.addProperty(Constants.PR_WIDTH);

        elem = elements[Constants.FO_INLINE];
        elem.addProperties(CommonAccessibilityProperties);
        elem.addProperties(CommonAuralProperties);
        elem.addProperties(CommonBorderPaddingBackgroundProperties);
        elem.addProperties(CommonFontProperties);
        elem.addProperties(CommonMarginPropertiesInline);
        elem.addProperties(CommonRelativePositionProperties);
        elem.addProperty(Constants.PR_VERTICAL_ALIGN);
        elem.addProperty(Constants.PR_ALIGNMENT_ADJUST);
        elem.addProperty(Constants.PR_ALIGNMENT_BASELINE);
        elem.addProperty(Constants.PR_BASELINE_SHIFT);
        elem.addProperty(Constants.PR_BLOCK_PROGRESSION_DIMENSION);
        elem.addProperty(Constants.PR_COLOR);
        elem.addProperty(Constants.PR_DOMINANT_BASELINE);
        elem.addProperty(Constants.PR_HEIGHT);
        elem.addProperty(Constants.PR_ID);
        elem.addProperty(Constants.PR_INLINE_PROGRESSION_DIMENSION);
        elem.addProperty(Constants.PR_PAGE_BREAK_INSIDE);
        elem.addProperty(Constants.PR_KEEP_TOGETHER);
        elem.addProperty(Constants.PR_KEEP_WITH_NEXT);
        elem.addProperty(Constants.PR_KEEP_WITH_PREVIOUS);
        elem.addProperty(Constants.PR_LINE_HEIGHT);
        elem.addProperty(Constants.PR_TEXT_DECORATION);
        elem.addProperty(Constants.PR_VISIBILITY);
        elem.addProperty(Constants.PR_WIDTH);
        elem.addProperty(Constants.PR_WRAP_OPTION);
        elem.addContent(inline_elems);
        elem.addContent(block_elems);

        elem = elements[Constants.FO_INLINE_CONTAINER];
        elem.addProperties(CommonBorderPaddingBackgroundProperties);
        elem.addProperties(CommonMarginPropertiesInline);
        elem.addProperties(CommonRelativePositionProperties);
        elem.addProperty(Constants.PR_VERTICAL_ALIGN);
        elem.addProperty(Constants.PR_ALIGNMENT_ADJUST);
        elem.addProperty(Constants.PR_ALIGNMENT_BASELINE);
        elem.addProperty(Constants.PR_BASELINE_SHIFT);
        elem.addProperty(Constants.PR_BLOCK_PROGRESSION_DIMENSION);
        elem.addProperty(Constants.PR_CLIP);
        elem.addProperty(Constants.PR_DISPLAY_ALIGN);
        elem.addProperty(Constants.PR_DOMINANT_BASELINE);
        elem.addProperty(Constants.PR_HEIGHT);
        elem.addProperty(Constants.PR_ID);
        elem.addProperty(Constants.PR_INLINE_PROGRESSION_DIMENSION);
        elem.addProperty(Constants.PR_PAGE_BREAK_INSIDE);
        elem.addProperty(Constants.PR_KEEP_TOGETHER);
        elem.addProperty(Constants.PR_KEEP_WITH_NEXT);
        elem.addProperty(Constants.PR_KEEP_WITH_PREVIOUS);
        elem.addProperty(Constants.PR_LINE_HEIGHT);
        elem.addProperty(Constants.PR_OVERFLOW);
        elem.addProperty(Constants.PR_REFERENCE_ORIENTATION);
        elem.addProperty(Constants.PR_WIDTH);
        elem.addProperty(Constants.PR_WRITING_MODE);
        elem.addContent(block_elems);

        elem = elements[Constants.FO_LEADER];
        elem.addProperties(CommonAccessibilityProperties);
        elem.addProperties(CommonAuralProperties);
        elem.addProperties(CommonBorderPaddingBackgroundProperties);
        elem.addProperties(CommonFontProperties);
        elem.addProperties(CommonMarginPropertiesInline);
        elem.addProperties(CommonRelativePositionProperties);
        elem.addProperty(Constants.PR_VERTICAL_ALIGN);
        elem.addProperty(Constants.PR_ALIGNMENT_ADJUST);
        elem.addProperty(Constants.PR_ALIGNMENT_BASELINE);
        elem.addProperty(Constants.PR_BASELINE_SHIFT);
        elem.addProperty(Constants.PR_COLOR);
        elem.addProperty(Constants.PR_DOMINANT_BASELINE);
        elem.addProperty(Constants.PR_TEXT_DEPTH);
        elem.addProperty(Constants.PR_TEXT_ALTITUDE);
        elem.addProperty(Constants.PR_ID);
        elem.addProperty(Constants.PR_KEEP_WITH_NEXT);
        elem.addProperty(Constants.PR_KEEP_WITH_PREVIOUS);
        elem.addProperty(Constants.PR_LEADER_ALIGNMENT);
        elem.addProperty(Constants.PR_LEADER_LENGTH);
        elem.addProperty(Constants.PR_LEADER_PATTERN);
        elem.addProperty(Constants.PR_LEADER_PATTERN_WIDTH);
        elem.addProperty(Constants.PR_RULE_STYLE);
        elem.addProperty(Constants.PR_RULE_THICKNESS);
        elem.addProperty(Constants.PR_LETTER_SPACING);
        elem.addProperty(Constants.PR_LINE_HEIGHT);
        elem.addProperty(Constants.PR_TEXT_SHADOW);
        elem.addProperty(Constants.PR_VISIBILITY);
        elem.addProperty(Constants.PR_WORD_SPACING);
        elem.addContent(inline_elems);

        elem = elements[Constants.FO_PAGE_NUMBER];
        elem.addProperties(CommonAccessibilityProperties);
        elem.addProperties(CommonAuralProperties);
        elem.addProperties(CommonBorderPaddingBackgroundProperties);
        elem.addProperties(CommonFontProperties);
        elem.addProperties(CommonMarginPropertiesInline);
        elem.addProperties(CommonRelativePositionProperties);
        elem.addProperty(Constants.PR_VERTICAL_ALIGN);
        elem.addProperty(Constants.PR_ALIGNMENT_ADJUST);
        elem.addProperty(Constants.PR_ALIGNMENT_BASELINE);
        elem.addProperty(Constants.PR_BASELINE_SHIFT);
        elem.addProperty(Constants.PR_DOMINANT_BASELINE);
        elem.addProperty(Constants.PR_ID);
        elem.addProperty(Constants.PR_KEEP_WITH_NEXT);
        elem.addProperty(Constants.PR_KEEP_WITH_PREVIOUS);
        elem.addProperty(Constants.PR_LETTER_SPACING);
        elem.addProperty(Constants.PR_LINE_HEIGHT);
        elem.addProperty(Constants.PR_SCORE_SPACES);
        elem.addProperty(Constants.PR_TEXT_ALTITUDE);
        elem.addProperty(Constants.PR_TEXT_DECORATION);
        elem.addProperty(Constants.PR_TEXT_DEPTH);
        elem.addProperty(Constants.PR_TEXT_SHADOW);
        elem.addProperty(Constants.PR_TEXT_TRANSFORM);
        elem.addProperty(Constants.PR_VISIBILITY);
        elem.addProperty(Constants.PR_WORD_SPACING);
        elem.addProperty(Constants.PR_WRAP_OPTION);

        elem = elements[Constants.FO_PAGE_NUMBER_CITATION];
        elem.addProperties(CommonAccessibilityProperties);
        elem.addProperties(CommonAuralProperties);
        elem.addProperties(CommonBorderPaddingBackgroundProperties);
        elem.addProperties(CommonFontProperties);
        elem.addProperties(CommonMarginPropertiesInline);
        elem.addProperties(CommonRelativePositionProperties);
        elem.addProperty(Constants.PR_VERTICAL_ALIGN);
        elem.addProperty(Constants.PR_ALIGNMENT_ADJUST);
        elem.addProperty(Constants.PR_ALIGNMENT_BASELINE);
        elem.addProperty(Constants.PR_BASELINE_SHIFT);
        elem.addProperty(Constants.PR_DOMINANT_BASELINE);
        elem.addProperty(Constants.PR_ID);
        elem.addProperty(Constants.PR_KEEP_WITH_NEXT);
        elem.addProperty(Constants.PR_KEEP_WITH_PREVIOUS);
        elem.addProperty(Constants.PR_LETTER_SPACING);
        elem.addProperty(Constants.PR_LINE_HEIGHT);
        elem.addProperty(Constants.PR_REF_ID);
        elem.addProperty(Constants.PR_SCORE_SPACES);
        elem.addProperty(Constants.PR_TEXT_ALTITUDE);
        elem.addProperty(Constants.PR_TEXT_DECORATION);
        elem.addProperty(Constants.PR_TEXT_DEPTH);
        elem.addProperty(Constants.PR_TEXT_SHADOW);
        elem.addProperty(Constants.PR_TEXT_TRANSFORM);
        elem.addProperty(Constants.PR_VISIBILITY);
        elem.addProperty(Constants.PR_WORD_SPACING);
        elem.addProperty(Constants.PR_WRAP_OPTION);

        elem = elements[Constants.FO_TABLE_AND_CAPTION];
        elem.addProperties(CommonAccessibilityProperties);
        elem.addProperties(CommonAuralProperties);
        elem.addProperties(CommonBorderPaddingBackgroundProperties);
        elem.addProperties(CommonMarginPropertiesBlock);
        elem.addProperties(CommonRelativePositionProperties);
        elem.addProperty(Constants.PR_PAGE_BREAK_AFTER);
        elem.addProperty(Constants.PR_PAGE_BREAK_BEFORE);
        elem.addProperty(Constants.PR_BREAK_AFTER);
        elem.addProperty(Constants.PR_BREAK_BEFORE);
        elem.addProperty(Constants.PR_CAPTION_SIDE);
        elem.addProperty(Constants.PR_ID);
        elem.addProperty(Constants.PR_PAGE_BREAK_INSIDE);
        elem.addProperty(Constants.PR_KEEP_TOGETHER);
        elem.addProperty(Constants.PR_KEEP_WITH_NEXT);
        elem.addProperty(Constants.PR_KEEP_WITH_PREVIOUS);
        elem.addProperty(Constants.PR_TEXT_ALIGN);
        elem.addContent(Constants.FO_TABLE_CAPTION);
        elem.addContent(Constants.FO_TABLE);

        elem = elements[Constants.FO_TABLE];
        elem.addProperties(CommonAccessibilityProperties);
        elem.addProperties(CommonAuralProperties);
        elem.addProperties(CommonBorderPaddingBackgroundProperties);
        elem.addProperties(CommonMarginPropertiesBlock);
        elem.addProperties(CommonRelativePositionProperties);
        elem.addProperty(Constants.PR_BLOCK_PROGRESSION_DIMENSION);
        elem.addProperty(Constants.PR_BORDER_AFTER_PRECEDENCE);
        elem.addProperty(Constants.PR_BORDER_BEFORE_PRECEDENCE);
        elem.addProperty(Constants.PR_BORDER_SPACING);
        elem.addProperty(Constants.PR_BORDER_COLLAPSE);
        elem.addProperty(Constants.PR_BORDER_END_PRECEDENCE);
        elem.addProperty(Constants.PR_BORDER_SEPARATION);
        elem.addProperty(Constants.PR_BORDER_START_PRECEDENCE);
        elem.addProperty(Constants.PR_PAGE_BREAK_AFTER);
        elem.addProperty(Constants.PR_PAGE_BREAK_BEFORE);
        elem.addProperty(Constants.PR_BREAK_AFTER);
        elem.addProperty(Constants.PR_BREAK_BEFORE);
        elem.addProperty(Constants.PR_ID);
        elem.addProperty(Constants.PR_INLINE_PROGRESSION_DIMENSION);
        elem.addProperty(Constants.PR_HEIGHT);
        elem.addProperty(Constants.PR_PAGE_BREAK_INSIDE);
        elem.addProperty(Constants.PR_KEEP_TOGETHER);
        elem.addProperty(Constants.PR_KEEP_WITH_NEXT);
        elem.addProperty(Constants.PR_KEEP_WITH_PREVIOUS);
        elem.addProperty(Constants.PR_TABLE_LAYOUT);
        elem.addProperty(Constants.PR_TABLE_OMIT_FOOTER_AT_BREAK);
        elem.addProperty(Constants.PR_TABLE_OMIT_HEADER_AT_BREAK);
        elem.addProperty(Constants.PR_WIDTH);
        elem.addProperty(Constants.PR_WRITING_MODE);
        elem.addContent(Constants.FO_TABLE_COLUMN);
        elem.addContent(Constants.FO_TABLE_HEADER);
        elem.addContent(Constants.FO_TABLE_FOOTER);
        elem.addContent(Constants.FO_TABLE_BODY);

        elem = elements[Constants.FO_TABLE_COLUMN];
        elem.addProperties(CommonBorderPaddingBackgroundProperties);
        elem.addProperty(Constants.PR_BORDER_AFTER_PRECEDENCE);
        elem.addProperty(Constants.PR_BORDER_BEFORE_PRECEDENCE);
        elem.addProperty(Constants.PR_BORDER_END_PRECEDENCE);
        elem.addProperty(Constants.PR_BORDER_START_PRECEDENCE);
        elem.addProperty(Constants.PR_COLUMN_NUMBER);
        elem.addProperty(Constants.PR_COLUMN_WIDTH);
        elem.addProperty(Constants.PR_NUMBER_COLUMNS_REPEATED);
        elem.addProperty(Constants.PR_NUMBER_COLUMNS_SPANNED);
        elem.addProperty(Constants.PR_VISIBILITY);

        elem = elements[Constants.FO_TABLE_CAPTION];
        elem.addProperties(CommonAccessibilityProperties);
        elem.addProperties(CommonAuralProperties);
        elem.addProperties(CommonBorderPaddingBackgroundProperties);
        elem.addProperties(CommonRelativePositionProperties);
        elem.addProperty(Constants.PR_BLOCK_PROGRESSION_DIMENSION);
        elem.addProperty(Constants.PR_HEIGHT);
        elem.addProperty(Constants.PR_ID);
        elem.addProperty(Constants.PR_INLINE_PROGRESSION_DIMENSION);
        elem.addProperty(Constants.PR_PAGE_BREAK_INSIDE);
        elem.addProperty(Constants.PR_KEEP_TOGETHER);
        elem.addProperty(Constants.PR_WIDTH);
        elem.addContent(block_elems);

        elem = elements[Constants.FO_TABLE_HEADER];
        elem.addProperties(CommonAccessibilityProperties);
        elem.addProperties(CommonAuralProperties);
        elem.addProperties(CommonBorderPaddingBackgroundProperties);
        elem.addProperties(CommonRelativePositionProperties);
        elem.addProperty(Constants.PR_BORDER_AFTER_PRECEDENCE);
        elem.addProperty(Constants.PR_BORDER_BEFORE_PRECEDENCE);
        elem.addProperty(Constants.PR_BORDER_END_PRECEDENCE);
        elem.addProperty(Constants.PR_BORDER_START_PRECEDENCE);
        elem.addProperty(Constants.PR_ID);
        elem.addProperty(Constants.PR_VISIBILITY);
        elem.addContent(Constants.FO_TABLE_ROW);
        elem.addContent(Constants.FO_TABLE_CELL);

        elem = elements[Constants.FO_TABLE_FOOTER];
        elem.addProperties(CommonAccessibilityProperties);
        elem.addProperties(CommonAuralProperties);
        elem.addProperties(CommonBorderPaddingBackgroundProperties);
        elem.addProperties(CommonRelativePositionProperties);
        elem.addProperty(Constants.PR_BORDER_AFTER_PRECEDENCE);
        elem.addProperty(Constants.PR_BORDER_BEFORE_PRECEDENCE);
        elem.addProperty(Constants.PR_BORDER_END_PRECEDENCE);
        elem.addProperty(Constants.PR_BORDER_START_PRECEDENCE);
        elem.addProperty(Constants.PR_ID);
        elem.addProperty(Constants.PR_VISIBILITY);
        elem.addContent(Constants.FO_TABLE_ROW);
        elem.addContent(Constants.FO_TABLE_CELL);

        elem = elements[Constants.FO_TABLE_BODY];
        elem.addProperties(CommonAccessibilityProperties);
        elem.addProperties(CommonAuralProperties);
        elem.addProperties(CommonBorderPaddingBackgroundProperties);
        elem.addProperties(CommonRelativePositionProperties);
        elem.addProperty(Constants.PR_BORDER_AFTER_PRECEDENCE);
        elem.addProperty(Constants.PR_BORDER_BEFORE_PRECEDENCE);
        elem.addProperty(Constants.PR_BORDER_END_PRECEDENCE);
        elem.addProperty(Constants.PR_BORDER_START_PRECEDENCE);
        elem.addProperty(Constants.PR_ID);
        elem.addProperty(Constants.PR_VISIBILITY);
        elem.addContent(Constants.FO_TABLE_ROW);
        elem.addContent(Constants.FO_TABLE_CELL);

        elem = elements[Constants.FO_TABLE_ROW];
        elem.addProperties(CommonAccessibilityProperties);
        elem.addProperty(Constants.PR_BLOCK_PROGRESSION_DIMENSION);
        elem.addProperties(CommonAuralProperties);
        elem.addProperties(CommonBorderPaddingBackgroundProperties);
        elem.addProperties(CommonRelativePositionProperties);
        elem.addProperty(Constants.PR_BORDER_AFTER_PRECEDENCE);
        elem.addProperty(Constants.PR_BORDER_BEFORE_PRECEDENCE);
        elem.addProperty(Constants.PR_BORDER_END_PRECEDENCE);
        elem.addProperty(Constants.PR_BORDER_START_PRECEDENCE);
        elem.addProperty(Constants.PR_PAGE_BREAK_AFTER);
        elem.addProperty(Constants.PR_PAGE_BREAK_BEFORE);
        elem.addProperty(Constants.PR_BREAK_AFTER);
        elem.addProperty(Constants.PR_BREAK_BEFORE);
        elem.addProperty(Constants.PR_ID);
        elem.addProperty(Constants.PR_HEIGHT);
        elem.addProperty(Constants.PR_PAGE_BREAK_INSIDE);
        elem.addProperty(Constants.PR_KEEP_TOGETHER);
        elem.addProperty(Constants.PR_KEEP_WITH_NEXT);
        elem.addProperty(Constants.PR_KEEP_WITH_PREVIOUS);
        elem.addProperty(Constants.PR_VISIBILITY);
        elem.addContent(Constants.FO_TABLE_CELL);

        elem = elements[Constants.FO_TABLE_CELL];
        elem.addProperties(CommonAccessibilityProperties);
        elem.addProperties(CommonAuralProperties);
        elem.addProperties(CommonBorderPaddingBackgroundProperties);
        elem.addProperties(CommonRelativePositionProperties);
        elem.addProperty(Constants.PR_BORDER_AFTER_PRECEDENCE);
        elem.addProperty(Constants.PR_BORDER_BEFORE_PRECEDENCE);
        elem.addProperty(Constants.PR_BORDER_END_PRECEDENCE);
        elem.addProperty(Constants.PR_BORDER_START_PRECEDENCE);
        elem.addProperty(Constants.PR_BLOCK_PROGRESSION_DIMENSION);
        elem.addProperty(Constants.PR_COLUMN_NUMBER);
        elem.addProperty(Constants.PR_DISPLAY_ALIGN);
        elem.addProperty(Constants.PR_RELATIVE_ALIGN);
        elem.addProperty(Constants.PR_EMPTY_CELLS);
        elem.addProperty(Constants.PR_ENDS_ROW);
        elem.addProperty(Constants.PR_HEIGHT);
        elem.addProperty(Constants.PR_ID);
        elem.addProperty(Constants.PR_INLINE_PROGRESSION_DIMENSION);
        elem.addProperty(Constants.PR_NUMBER_COLUMNS_SPANNED);
        elem.addProperty(Constants.PR_NUMBER_ROWS_SPANNED);
        elem.addProperty(Constants.PR_STARTS_ROW);
        elem.addProperty(Constants.PR_WIDTH);
        elem.addContent(block_elems);

        elem = elements[Constants.FO_LIST_BLOCK];
        elem.addProperties(CommonAccessibilityProperties);
        elem.addProperties(CommonAuralProperties);
        elem.addProperties(CommonBorderPaddingBackgroundProperties);
        elem.addProperties(CommonMarginPropertiesBlock);
        elem.addProperties(CommonRelativePositionProperties);
        elem.addProperty(Constants.PR_PAGE_BREAK_AFTER);
        elem.addProperty(Constants.PR_PAGE_BREAK_BEFORE);
        elem.addProperty(Constants.PR_BREAK_AFTER);
        elem.addProperty(Constants.PR_BREAK_BEFORE);
        elem.addProperty(Constants.PR_ID);
        elem.addProperty(Constants.PR_PAGE_BREAK_INSIDE);
        elem.addProperty(Constants.PR_KEEP_TOGETHER);
        elem.addProperty(Constants.PR_KEEP_WITH_NEXT);
        elem.addProperty(Constants.PR_KEEP_WITH_PREVIOUS);
        elem.addProperty(Constants.PR_PROVISIONAL_DISTANCE_BETWEEN_STARTS);
        elem.addProperty(Constants.PR_PROVISIONAL_LABEL_SEPARATION);
        elem.addContent(Constants.FO_LIST_ITEM);

        elem = elements[Constants.FO_LIST_ITEM];
        elem.addProperties(CommonAccessibilityProperties);
        elem.addProperties(CommonAuralProperties);
        elem.addProperties(CommonBorderPaddingBackgroundProperties);
        elem.addProperties(CommonMarginPropertiesBlock);
        elem.addProperties(CommonRelativePositionProperties);
        elem.addProperty(Constants.PR_PAGE_BREAK_AFTER);
        elem.addProperty(Constants.PR_PAGE_BREAK_BEFORE);
        elem.addProperty(Constants.PR_BREAK_AFTER);
        elem.addProperty(Constants.PR_BREAK_BEFORE);
        elem.addProperty(Constants.PR_ID);
        elem.addProperty(Constants.PR_PAGE_BREAK_INSIDE);
        elem.addProperty(Constants.PR_KEEP_TOGETHER);
        elem.addProperty(Constants.PR_KEEP_WITH_NEXT);
        elem.addProperty(Constants.PR_KEEP_WITH_PREVIOUS);
        elem.addProperty(Constants.PR_RELATIVE_ALIGN);
        elem.addContent(Constants.FO_LIST_ITEM_LABEL);
        elem.addContent(Constants.FO_LIST_ITEM_BODY);

        elem = elements[Constants.FO_LIST_ITEM_BODY];
        elem.addProperties(CommonAccessibilityProperties);
        elem.addProperty(Constants.PR_ID);
        elem.addProperty(Constants.PR_PAGE_BREAK_INSIDE);
        elem.addProperty(Constants.PR_KEEP_TOGETHER);
        elem.addContent(block_elems);

        elem = elements[Constants.FO_LIST_ITEM_LABEL];
        elem.addProperties(CommonAccessibilityProperties);
        elem.addProperty(Constants.PR_ID);
        elem.addProperty(Constants.PR_PAGE_BREAK_INSIDE);
        elem.addProperty(Constants.PR_KEEP_TOGETHER);
        elem.addContent(block_elems);

        elem = elements[Constants.FO_BASIC_LINK];
        elem.addProperties(CommonAccessibilityProperties);
        elem.addProperties(CommonAuralProperties);
        elem.addProperties(CommonBorderPaddingBackgroundProperties);
        elem.addProperties(CommonMarginPropertiesInline);
        elem.addProperties(CommonRelativePositionProperties);
        elem.addProperty(Constants.PR_VERTICAL_ALIGN);
        elem.addProperty(Constants.PR_ALIGNMENT_ADJUST);
        elem.addProperty(Constants.PR_ALIGNMENT_BASELINE);
        elem.addProperty(Constants.PR_BASELINE_SHIFT);
        elem.addProperty(Constants.PR_DESTINATION_PLACEMENT_OFFSET);
        elem.addProperty(Constants.PR_DOMINANT_BASELINE);
        elem.addProperty(Constants.PR_EXTERNAL_DESTINATION);
        elem.addProperty(Constants.PR_ID);
        elem.addProperty(Constants.PR_INDICATE_DESTINATION);
        elem.addProperty(Constants.PR_INTERNAL_DESTINATION);
        elem.addProperty(Constants.PR_PAGE_BREAK_INSIDE);
        elem.addProperty(Constants.PR_KEEP_TOGETHER);
        elem.addProperty(Constants.PR_KEEP_WITH_NEXT);
        elem.addProperty(Constants.PR_KEEP_WITH_PREVIOUS);
        elem.addProperty(Constants.PR_LINE_HEIGHT);
        elem.addProperty(Constants.PR_SHOW_DESTINATION);
        elem.addProperty(Constants.PR_TARGET_PROCESSING_CONTEXT);
        elem.addProperty(Constants.PR_TARGET_PRESENTATION_CONTEXT);
        elem.addProperty(Constants.PR_TARGET_STYLESHEET);
        elem.addContent(inline_elems);
        elem.addContent(block_elems);

        elem = elements[Constants.FO_MULTI_SWITCH];
        elem.addProperties(CommonAccessibilityProperties);
        elem.addProperty(Constants.PR_AUTO_RESTORE);
        elem.addProperty(Constants.PR_ID);
        elem.addContent(Constants.FO_MULTI_CASE);

        elem = elements[Constants.FO_MULTI_CASE];
        elem.addProperties(CommonAccessibilityProperties);
        elem.addProperty(Constants.PR_ID);
        elem.addProperty(Constants.PR_STARTING_STATE);
        elem.addProperty(Constants.PR_CASE_NAME);
        elem.addProperty(Constants.PR_CASE_TITLE);
        elem.addContent(inline_elems);
        elem.addContent(block_elems);

        elem = elements[Constants.FO_MULTI_TOGGLE];
        elem.addProperties(CommonAccessibilityProperties);
        elem.addProperty(Constants.PR_ID);
        elem.addProperty(Constants.PR_SWITCH_TO);
        elem.addContent(inline_elems);
        elem.addContent(block_elems);

        elem = elements[Constants.FO_MULTI_PROPERTIES];
        elem.addProperties(CommonAccessibilityProperties);
        elem.addProperty(Constants.PR_ID);
        elem.addContent(Constants.FO_MULTI_PROPERTY_SET);
        elem.addContent(Constants.FO_WRAPPER);

        elem = elements[Constants.FO_MULTI_PROPERTY_SET];
        elem.addProperty(Constants.PR_ID);
        elem.addProperty(Constants.PR_ACTIVE_STATE);

        elem = elements[Constants.FO_FLOAT];
        elem.addProperty(Constants.PR_FLOAT);
        elem.addProperty(Constants.PR_CLEAR);
        elem.addContent(block_elems);

        elem = elements[Constants.FO_FOOTNOTE];
        elem.addProperties(CommonAccessibilityProperties);
        elem.addContent(Constants.FO_INLINE);
        elem.addContent(Constants.FO_FOOTNOTE_BODY);

        elem = elements[Constants.FO_FOOTNOTE_BODY];
        elem.addProperties(CommonAccessibilityProperties);
        elem.addContent(block_elems);

        elem = elements[Constants.FO_WRAPPER];
        elem.addProperty(Constants.PR_ID);
        elem.addContent(inline_elems);
        elem.addContent(block_elems);

        elem = elements[Constants.FO_MARKER];
        elem.addProperty(Constants.PR_MARKER_CLASS_NAME);
        elem.addContent(inline_elems);
        elem.addContent(block_elems);

        elem = elements[Constants.FO_RETRIEVE_MARKER];
        elem.addProperty(Constants.PR_RETRIEVE_CLASS_NAME);
        elem.addProperty(Constants.PR_RETRIEVE_POSITION);
        elem.addProperty(Constants.PR_RETRIEVE_BOUNDARY);

        // Merge the attributes from the children into the parent.
        for (boolean dirty = true; dirty; ) {
            dirty = false;
            for (int i = 1; i < elements.length; i++) {
                dirty = dirty || elements[i].merge();
            }
        }
        // Calculate the sparse indices for each element.
        for (int i = 1; i < elements.length; i++) {
            mapping[i] = makeSparseIndices(elements[i].valid);
        }
    }

    /**
     * Turn a BitSet into an array of shorts with the first element
     * on the array the number of set bits in the BitSet.
     */
    private static short[] makeSparseIndices(BitSet set) {
        short[] indices = new short[Constants.PROPERTY_COUNT+1];
        int j = 1;
        for (int i = 0; i < Constants.PROPERTY_COUNT+1; i++) {
            if (set.get(i)) {
                indices[i] = (short) j++;
            }
        }
        indices[0] = (short)j;
        return indices;
    }

    public static short[] getPropertySet(int elementId) {
        if (mapping == null) {
            mapping = new short[Constants.FRM_OBJ_COUNT+1][];
            PropertySets ps = new PropertySets();
            ps.initializeElements();
            ps.initializeCommon();
            ps.initialize();
        }
        return mapping[elementId];
    }

    /**
     * Determines if fo:markers are allowed as children for the given FO
     * @param elementId Constants enumeration ID of the FO (e.g., FO_ROOT)
     * @return true if fo:markers allowed, false otherwise
     * @todo check if still needed after validateChildNode() fully implemented
     */
    public static boolean canHaveMarkers(int elementId) {
        if (can_have_markers == null) {
            can_have_markers = new BitSet();
            can_have_markers.set(Constants.FO_BASIC_LINK);
            can_have_markers.set(Constants.FO_BIDI_OVERRIDE);
            can_have_markers.set(Constants.FO_BLOCK);
            can_have_markers.set(Constants.FO_BLOCK_CONTAINER);
            can_have_markers.set(Constants.FO_FLOW);
            can_have_markers.set(Constants.FO_INLINE);
            can_have_markers.set(Constants.FO_INLINE_CONTAINER);
            can_have_markers.set(Constants.FO_LIST_BLOCK);
            can_have_markers.set(Constants.FO_LIST_ITEM);
            can_have_markers.set(Constants.FO_LIST_ITEM_BODY);
            can_have_markers.set(Constants.FO_LIST_ITEM_LABEL);
            can_have_markers.set(Constants.FO_TABLE);
            can_have_markers.set(Constants.FO_TABLE_BODY);
            can_have_markers.set(Constants.FO_TABLE_HEADER);
            can_have_markers.set(Constants.FO_TABLE_FOOTER);
            can_have_markers.set(Constants.FO_TABLE_CELL);
            can_have_markers.set(Constants.FO_TABLE_AND_CAPTION);
            can_have_markers.set(Constants.FO_TABLE_CAPTION);
            can_have_markers.set(Constants.FO_WRAPPER);
        }
        return can_have_markers.get(elementId);
    }

    /**
     * Determines if the FO generates inline areas.  Used only within flow.Block
     * for whitespace handling
     * @param elementId Constants enumeration ID of the FO (e.g., FO_ROOT)
     * @return true if id property is applicable, false otherwise
     * @todo see if more values need to be entered here (copied values over
     *      from legacy code, list of FO's below probably incomplete)
     * @todo see if still needed (LM has a similar generatesInlineAreas()
     *      method)
     */
    public static boolean generatesInlineAreas(int elementId) {
        if (no_inline_areas == null) {
            no_inline_areas = new BitSet();
            no_inline_areas.set(Constants.FO_UNKNOWN_NODE);
            no_inline_areas.set(Constants.FO_BLOCK);
            no_inline_areas.set(Constants.FO_BLOCK_CONTAINER);
            no_inline_areas.set(Constants.FO_LIST_BLOCK);
            no_inline_areas.set(Constants.FO_LIST_ITEM);
            no_inline_areas.set(Constants.FO_TABLE);
            no_inline_areas.set(Constants.FO_TABLE_AND_CAPTION);
        }
        return !(no_inline_areas.get(elementId));
    }

    /**
     * An object that represent the properties and contents of a fo element
     */
    class Element {
        BitSet relevant = new BitSet();
        BitSet valid = new BitSet();
        int elementId;
        ArrayList childFOs;

        Element(int elementId) {
            this.elementId = elementId;
        }

        /**
         * Add a single property to the element.
         */
        public void addProperty(int propId) {
            relevant.set(propId);
            valid.set(propId);
        }

        /**
         * Add a set of properties to the element.
         */
        public void addProperties(BitSet properties) {
            relevant.or(properties);
            valid.or(properties);
        }

        /**
         * Add a single fo element as a content child.
         */
        public void addContent(int elementId) {
            if (childFOs == null) {
                childFOs = new ArrayList();
            }
            childFOs.add(elements[elementId]);
        }

        /**
         * Add a set of fo elements as content childFOs.
         */
        public void addContent(BitSet elements) {
            for (int i = 0; i < elements.size(); i++) {
                if (elements.get(i)) {
                    addContent(i);
                }
            }
        }

        /**
         * Merge the properties from the child FO's into the set of valid
         * properties. Return true if at least one property could be added.
         */
        public boolean merge() {
            if (childFOs == null) {
                return false;
            }
            boolean dirty = false;
            for (int i = 0; i < childFOs.size(); i++) {
                Element child = (Element) childFOs.get(i);
                BitSet childValid = child.valid;
                int n = childValid.length();
                for (int j = 0; j < n; j++) {
                    if (childValid.get(j) && !valid.get(j)) {
                        dirty = true;
                        valid.set(j);
                    }
                }
            }
            return dirty;
        }
    }
}
