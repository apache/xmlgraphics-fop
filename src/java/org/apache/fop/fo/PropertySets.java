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
 * @version $Revision$ $Name$
 */

package org.apache.fop.fo;

import java.util.BitSet;

import org.apache.fop.datastructs.ROBitSet;

/**
 * This class contains <tt>ROBitSet</tt>s which encode the various sets of
 * properties which are defined to apply to each of the Flow Objects.  These 
 * <tt>ROBitSet</tt>s provide a convenient means of specifying the
 * relationship between FOs and properties.
 */
public class PropertySets {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    private static final String packageName = "org.apache.fop.fo";

    /**
     * <tt>BitSet</tt> of the <tt>Integer</tt> objects corresponding to the
     * constant index of each property in the set of
     * <b>Common Accessibility Properties</b>.
     */
    private static final BitSet accessibilityProps = new BitSet();
    public static final ROBitSet accessibilitySet;
    public static BitSet accessibilitySetClone() {
        return (BitSet)(accessibilityProps.clone());
    }

    static {
         accessibilityProps.set(PropNames.ROLE);       
         accessibilityProps.set(PropNames.SOURCE_DOCUMENT);
         accessibilitySet = new ROBitSet(accessibilityProps);
    }
    /**
     * <tt>BitSet</tt> of the <tt>Integer</tt> objects corresponding to the
     * constant index of each property in the set of
     * <b>Common Absolute Position Properties</b>.
     */
    private static final BitSet absolutePositionProps = new BitSet();
    public static final ROBitSet absolutePositionSet;
    public static BitSet absolutePositionSetClone() {
        return (BitSet)(absolutePositionProps.clone());
    }

    static {
        absolutePositionProps.set(PropNames.ABSOLUTE_POSITION);
        absolutePositionProps.set(PropNames.BOTTOM);
        absolutePositionProps.set(PropNames.LEFT);
        absolutePositionProps.set(PropNames.RIGHT);
        absolutePositionProps.set(PropNames.TOP);
        absolutePositionSet = new ROBitSet(absolutePositionProps);
    }
    /**
     * <tt>BitSet</tt> of the <tt>Integer</tt> objects corresponding to the
     * constant index of each property in the set of
     * <b>Common Aural Properties</b>.
     */
    private static final BitSet auralProps = new BitSet();
    public static final ROBitSet auralSet;
    public static BitSet auralSetClone() {
        return (BitSet)(auralProps.clone());
    }

    static {
        auralProps.set(PropNames.AZIMUTH);
        auralProps.set(PropNames.CUE_AFTER);
        auralProps.set(PropNames.CUE_BEFORE);
        auralProps.set(PropNames.ELEVATION);
        auralProps.set(PropNames.PAUSE_AFTER);
        auralProps.set(PropNames.PAUSE_BEFORE);
        auralProps.set(PropNames.PITCH);
        auralProps.set(PropNames.PITCH_RANGE);
        auralProps.set(PropNames.PLAY_DURING);
        auralProps.set(PropNames.RICHNESS);
        auralProps.set(PropNames.SPEAK);
        auralProps.set(PropNames.SPEAK_HEADER);
        auralProps.set(PropNames.SPEAK_NUMERAL);
        auralProps.set(PropNames.SPEAK_PUNCTUATION);
        auralProps.set(PropNames.SPEECH_RATE);
        auralProps.set(PropNames.STRESS);
        auralProps.set(PropNames.VOICE_FAMILY);
        auralProps.set(PropNames.VOLUME);
        auralSet = new ROBitSet(auralProps);
    }
    /**
     * <tt>BitSet</tt> of the <tt>Integer</tt> objects corresponding to the
     * constant index of each property in the set of
     * <b>Common Background Properties</b>.
     */
    private static final BitSet backgroundProps = new BitSet();
    public static final ROBitSet backgroundSet;
    public static BitSet backgroundSetClone() {
        return (BitSet)(backgroundProps.clone());
    }

    static {
        backgroundProps.set(PropNames.BACKGROUND);
        backgroundProps.set(PropNames.BACKGROUND_ATTACHMENT);
        backgroundProps.set(PropNames.BACKGROUND_COLOR);
        backgroundProps.set(PropNames.BACKGROUND_IMAGE);
        backgroundProps.set(PropNames.BACKGROUND_POSITION);
        backgroundProps.set(PropNames.BACKGROUND_POSITION_HORIZONTAL);
        backgroundProps.set(PropNames.BACKGROUND_POSITION_VERTICAL);
        backgroundProps.set(PropNames.BACKGROUND_REPEAT);
        backgroundSet = new ROBitSet(backgroundProps);
    }
    /**
     * <tt>BitSet</tt> of the <tt>Integer</tt> objects corresponding to the
     * constant index of each property in the set of
     * <b>Common Border Properties</b>.
     */
    private static final BitSet borderProps = new BitSet();
    public static final ROBitSet borderSet;
    public static BitSet borderSetClone() {
        return (BitSet)(borderProps.clone());
    }

    static {
        borderProps.set(PropNames.BORDER);
        borderProps.set(PropNames.BORDER_AFTER_COLOR);
        borderProps.set(PropNames.BORDER_AFTER_STYLE);
        borderProps.set(PropNames.BORDER_AFTER_WIDTH);
        borderProps.set(PropNames.BORDER_AFTER_WIDTH_LENGTH);
        borderProps.set(PropNames.BORDER_AFTER_WIDTH_CONDITIONALITY);
        borderProps.set(PropNames.BORDER_BEFORE_COLOR);
        borderProps.set(PropNames.BORDER_BEFORE_STYLE);
        borderProps.set(PropNames.BORDER_BEFORE_WIDTH);
        borderProps.set(PropNames.BORDER_BEFORE_WIDTH_LENGTH);
        borderProps.set(PropNames.BORDER_BEFORE_WIDTH_CONDITIONALITY);
        borderProps.set(PropNames.BORDER_BOTTOM);
        borderProps.set(PropNames.BORDER_BOTTOM_COLOR);
        borderProps.set(PropNames.BORDER_BOTTOM_STYLE);
        borderProps.set(PropNames.BORDER_BOTTOM_WIDTH);
        borderProps.set(PropNames.BORDER_COLOR);
        borderProps.set(PropNames.BORDER_END_COLOR);
        borderProps.set(PropNames.BORDER_END_STYLE);
        borderProps.set(PropNames.BORDER_END_WIDTH);
        borderProps.set(PropNames.BORDER_END_WIDTH_LENGTH);
        borderProps.set(PropNames.BORDER_END_WIDTH_CONDITIONALITY);
        borderProps.set(PropNames.BORDER_LEFT);
        borderProps.set(PropNames.BORDER_LEFT_COLOR);
        borderProps.set(PropNames.BORDER_LEFT_STYLE);
        borderProps.set(PropNames.BORDER_LEFT_WIDTH);
        borderProps.set(PropNames.BORDER_RIGHT);
        borderProps.set(PropNames.BORDER_RIGHT_COLOR);
        borderProps.set(PropNames.BORDER_RIGHT_STYLE);
        borderProps.set(PropNames.BORDER_RIGHT_WIDTH);
        borderProps.set(PropNames.BORDER_START_COLOR);
        borderProps.set(PropNames.BORDER_START_STYLE);
        borderProps.set(PropNames.BORDER_START_WIDTH);
        borderProps.set(PropNames.BORDER_START_WIDTH_LENGTH);
        borderProps.set(PropNames.BORDER_START_WIDTH_CONDITIONALITY);
        borderProps.set(PropNames.BORDER_STYLE);
        borderProps.set(PropNames.BORDER_TOP);
        borderProps.set(PropNames.BORDER_TOP_COLOR);
        borderProps.set(PropNames.BORDER_TOP_STYLE);
        borderProps.set(PropNames.BORDER_TOP_WIDTH);
        borderProps.set(PropNames.BORDER_WIDTH);
        borderSet = new ROBitSet(borderProps);
    }
    /**
     * <tt>BitSet</tt> of the <tt>Integer</tt> objects corresponding to the
     * constant index of each property in the set of
     * <b>Common Font Properties</b>.
     */
    private static final BitSet fontProps = new BitSet();
    public static final ROBitSet fontSet;
    public static BitSet fontSetClone() {
        return (BitSet)(fontProps.clone());
    }

    static {
        fontProps.set(PropNames.FONT);
        fontProps.set(PropNames.FONT_FAMILY);
        fontProps.set(PropNames.FONT_SELECTION_STRATEGY);
        fontProps.set(PropNames.FONT_SIZE);
        fontProps.set(PropNames.FONT_SIZE_ADJUST);
        fontProps.set(PropNames.FONT_STRETCH);
        fontProps.set(PropNames.FONT_STYLE);
        fontProps.set(PropNames.FONT_VARIANT);
        fontProps.set(PropNames.FONT_WEIGHT);
        fontSet = new ROBitSet(fontProps);
    }
    /**
     * <tt>BitSet</tt> of the <tt>Integer</tt> objects corresponding to the
     * constant index of each property in the set of
     * <b>Common Hyphenation Properties</b>.
     */
    private static final BitSet hyphenationProps = new BitSet();
    public static final ROBitSet hyphenationSet;
    public static BitSet hyphenationSetClone() {
        return (BitSet)(hyphenationProps.clone());
    }

    static {
        hyphenationProps.set(PropNames.COUNTRY);
        hyphenationProps.set(PropNames.LANGUAGE);
        hyphenationProps.set(PropNames.SCRIPT);
        hyphenationProps.set(PropNames.HYPHENATE);
        hyphenationProps.set(PropNames.HYPHENATION_CHARACTER);
        hyphenationProps.set(PropNames.HYPHENATION_PUSH_CHARACTER_COUNT);
        hyphenationProps.set(PropNames.HYPHENATION_REMAIN_CHARACTER_COUNT);
        hyphenationSet = new ROBitSet(hyphenationProps);
    }
    /**
     * <tt>BitSet</tt> of the <tt>Integer</tt> objects corresponding to the
     * constant index of each property in the set of
     * <b>Common Margin-Block Properties</b>.
     */
    private static final BitSet marginBlockProps = new BitSet();
    public static final ROBitSet marginBlockSet;
    public static BitSet marginBlockSetClone() {
        return (BitSet)(marginBlockProps.clone());
    }

    static {
        marginBlockProps.set(PropNames.MARGIN);
        marginBlockProps.set(PropNames.MARGIN_BOTTOM);
        marginBlockProps.set(PropNames.MARGIN_LEFT);
        marginBlockProps.set(PropNames.MARGIN_RIGHT);
        marginBlockProps.set(PropNames.MARGIN_TOP);
        marginBlockSet = new ROBitSet(marginBlockProps);
    }
    /**
     * <tt>BitSet</tt> of the <tt>Integer</tt> objects corresponding to the
     * constant index of each property in the set of
     * <b>Common Margin-Inline Properties</b>.
     */
    private static final BitSet marginInlineProps = new BitSet();
    public static final ROBitSet marginInlineSet;
    public static BitSet marginInlineSetClone() {
        return (BitSet)(marginInlineProps.clone());
    }

    static {
        marginInlineProps.set(PropNames.SPACE_END);
        marginInlineProps.set(PropNames.SPACE_START);
        marginInlineSet = new ROBitSet(marginInlineProps);
    }
    /**
     * <tt>BitSet</tt> of the <tt>Integer</tt> objects corresponding to the
     * constant index of each property in the set of
     * <b>Common Padding Properties</b>.
     */
    private static final BitSet paddingProps = new BitSet();
    public static final ROBitSet paddingSet;
    public static BitSet paddingSetClone() {
        return (BitSet)(paddingProps.clone());
    }

    static {
        paddingProps.set(PropNames.PADDING);
        paddingProps.set(PropNames.PADDING_AFTER);
        paddingProps.set(PropNames.PADDING_AFTER_LENGTH);
        paddingProps.set(PropNames.PADDING_AFTER_CONDITIONALITY);
        paddingProps.set(PropNames.PADDING_BEFORE);
        paddingProps.set(PropNames.PADDING_BEFORE_LENGTH);
        paddingProps.set(PropNames.PADDING_BEFORE_CONDITIONALITY);
        paddingProps.set(PropNames.PADDING_BOTTOM);
        paddingProps.set(PropNames.PADDING_END);
        paddingProps.set(PropNames.PADDING_END_LENGTH);
        paddingProps.set(PropNames.PADDING_END_CONDITIONALITY);
        paddingProps.set(PropNames.PADDING_LEFT);
        paddingProps.set(PropNames.PADDING_RIGHT);
        paddingProps.set(PropNames.PADDING_START);
        paddingProps.set(PropNames.PADDING_START_LENGTH);
        paddingProps.set(PropNames.PADDING_START_CONDITIONALITY);
        paddingProps.set(PropNames.PADDING_TOP);
        paddingSet = new ROBitSet(paddingProps);
    }
    /**
     * <tt>BitSet</tt> of the <tt>Integer</tt> objects corresponding to the
     * constant index of each property in the set of
     * <b>Common Relative Position Properties</b>.
     */
    private static final BitSet relativePositionProps = new BitSet();
    public static final ROBitSet relativePositionSet;
    public static BitSet relativePositionSetClone() {
        return (BitSet)(relativePositionProps.clone());
    }

    static {
        relativePositionProps.set(PropNames.RELATIVE_POSITION);
        relativePositionProps.set(PropNames.BOTTOM);
        relativePositionProps.set(PropNames.LEFT);
        relativePositionProps.set(PropNames.RIGHT);
        relativePositionProps.set(PropNames.TOP);
        relativePositionSet =
                    new ROBitSet(relativePositionProps);
    }
    /**
     * <tt>BitSet</tt> of the <tt>Integer</tt> objects corresponding to the
     * constant index of each property in the set of
     * <b>Common Table Properties</b>.
     */
    private static final BitSet tableProps = new BitSet();
    public static final ROBitSet tableSet;
    public static BitSet tableSetClone() {
        return (BitSet)(tableProps.clone());
    }

    static {
        tableProps.set(PropNames.BORDER_AFTER_PRECEDENCE);
        tableProps.set(PropNames.BORDER_BEFORE_PRECEDENCE);
        tableProps.set(PropNames.BORDER_COLLAPSE);
        tableProps.set(PropNames.BORDER_END_PRECEDENCE);
        tableProps.set(PropNames.BORDER_SEPARATION);
        tableProps.set(PropNames.BORDER_SEPARATION_BLOCK_PROGRESSION_DIRECTION);
        tableProps.set(PropNames.BORDER_SEPARATION_INLINE_PROGRESSION_DIRECTION);
        tableProps.set(PropNames.BORDER_SPACING);
        tableProps.set(PropNames.BORDER_START_PRECEDENCE);
        tableProps.set(PropNames.CAPTION_SIDE);
        tableProps.set(PropNames.COLUMN_NUMBER);
        tableProps.set(PropNames.COLUMN_WIDTH);
        tableProps.set(PropNames.EMPTY_CELLS);
        tableProps.set(PropNames.ENDS_ROW);
        tableProps.set(PropNames.NUMBER_COLUMNS_REPEATED);
        tableProps.set(PropNames.NUMBER_COLUMNS_SPANNED);
        tableProps.set(PropNames.NUMBER_ROWS_SPANNED);
        tableProps.set(PropNames.STARTS_ROW);
        tableProps.set(PropNames.TABLE_LAYOUT);
        tableProps.set(PropNames.TABLE_OMIT_FOOTER_AT_BREAK);
        tableProps.set(PropNames.TABLE_OMIT_HEADER_AT_BREAK);
        tableSet = new ROBitSet(tableProps);
    }

    private PropertySets (){}

}
