/**
 * $Id$
 * <br/>Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * <br/>For details on use and redistribution please refer to the
 * <br/>LICENSE file included with these sources.
 *
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */

package org.apache.fop.fo;

import java.lang.CloneNotSupportedException;

import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Arrays;
import java.util.Collections;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.expr.PropertyValue;
import org.apache.fop.fo.expr.PropertyValueList;
import org.apache.fop.fo.PropNames;
import org.apache.fop.datastructs.ROIntArray;
import org.apache.fop.datatypes.Ints;

/**
 * This class contains <tt>HashMap</tt>s which encode the various sets of
 * properties which are defined to apply to each of the Flow Objects.  These 
 * <tt>HashMap</tt>s provide a convenient means of specifying the
 * relationship between FOs and properties.
 */
public class PropertySets {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    private static final String packageName = "org.apache.fop.fo";

    /**
     * The number of <b>Common Accessibility Properties</b>
     */
    public static final int accessibilityPropsSize = 2;
    /**
     * <tt>HashSet</tt> of the <tt>Integer</tt> objects corresponding to the
     * constant index of each property in the set of
     * <b>Common Accessibility Properties</b>.
     */
    private static final HashSet accessibilityProps =
                                        new HashSet(accessibilityPropsSize);
    public static final Set accessibilitySet;

    static {
         accessibilityProps.add(Ints.consts.get(PropNames.ROLE));       
         accessibilityProps.add(Ints.consts.get(PropNames.SOURCE_DOCUMENT));       
         accessibilitySet =
                         Collections.unmodifiableSet((Set)accessibilityProps);
    }
    /**
     * The number of <b>Common Absolute Position Properties</b>.
     */
    public static final int absolutePositionPropsSize = 5;
    /**
     * <tt>HashSet</tt> of the <tt>Integer</tt> objects corresponding to the
     * constant index of each property in the set of
     * <b>Common Absolute Position Properties</b>.
     */
    private static final HashSet absolutePositionProps =
                                    new HashSet(absolutePositionPropsSize);
    public static final Set absolutePositionSet;

    static {
        absolutePositionProps.add
                            (Ints.consts.get(PropNames.ABSOLUTE_POSITION));
        absolutePositionProps.add(Ints.consts.get(PropNames.BOTTOM));
        absolutePositionProps.add(Ints.consts.get(PropNames.LEFT));
        absolutePositionProps.add(Ints.consts.get(PropNames.RIGHT));
        absolutePositionProps.add(Ints.consts.get(PropNames.TOP));
        absolutePositionSet =
                    Collections.unmodifiableSet((Set)absolutePositionProps);
    }
    /**
     * The number of <b>Common Aural Properties</b>.
     */
    public static final int auralPropsSize = 18;
    /**
     * <tt>HashSet</tt> of the <tt>Integer</tt> objects corresponding to the
     * constant index of each property in the set of
     * <b>Common Aural Properties</b>.
     */
    private static final HashSet auralProps = new HashSet(auralPropsSize);
    public static final Set auralSet;

    static {
        auralProps.add(Ints.consts.get(PropNames.AZIMUTH));
        auralProps.add(Ints.consts.get(PropNames.CUE_AFTER));
        auralProps.add(Ints.consts.get(PropNames.CUE_BEFORE));
        auralProps.add(Ints.consts.get(PropNames.ELEVATION));
        auralProps.add(Ints.consts.get(PropNames.PAUSE_AFTER));
        auralProps.add(Ints.consts.get(PropNames.PAUSE_BEFORE));
        auralProps.add(Ints.consts.get(PropNames.PITCH));
        auralProps.add(Ints.consts.get(PropNames.PITCH_RANGE));
        auralProps.add(Ints.consts.get(PropNames.PLAY_DURING));
        auralProps.add(Ints.consts.get(PropNames.RICHNESS));
        auralProps.add(Ints.consts.get(PropNames.SPEAK));
        auralProps.add(Ints.consts.get(PropNames.SPEAK_HEADER));
        auralProps.add(Ints.consts.get(PropNames.SPEAK_NUMERAL));
        auralProps.add(Ints.consts.get(PropNames.SPEAK_PUNCTUATION));
        auralProps.add(Ints.consts.get(PropNames.SPEECH_RATE));
        auralProps.add(Ints.consts.get(PropNames.STRESS));
        auralProps.add(Ints.consts.get(PropNames.VOICE_FAMILY));
        auralProps.add(Ints.consts.get(PropNames.VOLUME));
        auralSet = Collections.unmodifiableSet((Set)auralProps);
    }
    /**
     * The number of <b>Common Background Properties</b>.
     */
    public static final int backgroundPropsSize = 8;
    /**
     * <tt>HashSet</tt> of the <tt>Integer</tt> objects corresponding to the
     * constant index of each property in the set of
     * <b>Common Background Properties</b>.
     */
    private static final HashSet backgroundProps =
                                            new HashSet(backgroundPropsSize);
    public static final Set backgroundSet;

    static {
        backgroundProps.add(Ints.consts.get(PropNames.BACKGROUND));
        backgroundProps.add(Ints.consts.get(PropNames.BACKGROUND_ATTACHMENT));
        backgroundProps.add(Ints.consts.get(PropNames.BACKGROUND_COLOR));
        backgroundProps.add(Ints.consts.get(PropNames.BACKGROUND_IMAGE));
        backgroundProps.add(Ints.consts.get(PropNames.BACKGROUND_POSITION));
        backgroundProps.add
                (Ints.consts.get(PropNames.BACKGROUND_POSITION_HORIZONTAL));
        backgroundProps.add
                    (Ints.consts.get(PropNames.BACKGROUND_POSITION_VERTICAL));
        backgroundProps.add(Ints.consts.get(PropNames.BACKGROUND_REPEAT));
        backgroundSet = Collections.unmodifiableSet((Set)backgroundProps);
    }
    /**
     * The number of <b>Common Border Properties</b>.
     */
    public static final int borderPropsSize = 41;
    /**
     * <tt>HashSet</tt> of the <tt>Integer</tt> objects corresponding to the
     * constant index of each property in the set of
     * <b>Common Border Properties</b>.
     */
    private static final HashSet borderProps = new HashSet(borderPropsSize);
    public static final Set borderSet;

    static {
        borderProps.add(Ints.consts.get(PropNames.BORDER));
        borderProps.add(Ints.consts.get(PropNames.BORDER_AFTER_COLOR));
        borderProps.add(Ints.consts.get(PropNames.BORDER_AFTER_STYLE));
        borderProps.add(Ints.consts.get(PropNames.BORDER_AFTER_WIDTH));
        borderProps.add(Ints.consts.get(PropNames.BORDER_AFTER_WIDTH_LENGTH));
        borderProps.add
            (Ints.consts.get(PropNames.BORDER_AFTER_WIDTH_CONDITIONALITY));
        borderProps.add(Ints.consts.get(PropNames.BORDER_BEFORE_COLOR));
        borderProps.add(Ints.consts.get(PropNames.BORDER_BEFORE_STYLE));
        borderProps.add(Ints.consts.get(PropNames.BORDER_BEFORE_WIDTH));
        borderProps.add(Ints.consts.get(PropNames.BORDER_BEFORE_WIDTH_LENGTH));
        borderProps.add
            (Ints.consts.get(PropNames.BORDER_BEFORE_WIDTH_CONDITIONALITY));
        borderProps.add(Ints.consts.get(PropNames.BORDER_BOTTOM));
        borderProps.add(Ints.consts.get(PropNames.BORDER_BOTTOM_COLOR));
        borderProps.add(Ints.consts.get(PropNames.BORDER_BOTTOM_STYLE));
        borderProps.add(Ints.consts.get(PropNames.BORDER_BOTTOM_WIDTH));
        borderProps.add(Ints.consts.get(PropNames.BORDER_COLOR));
        borderProps.add(Ints.consts.get(PropNames.BORDER_END_COLOR));
        borderProps.add(Ints.consts.get(PropNames.BORDER_END_STYLE));
        borderProps.add(Ints.consts.get(PropNames.BORDER_END_WIDTH));
        borderProps.add(Ints.consts.get(PropNames.BORDER_END_WIDTH_LENGTH));
        borderProps.add
            (Ints.consts.get(PropNames.BORDER_END_WIDTH_CONDITIONALITY));
        borderProps.add(Ints.consts.get(PropNames.BORDER_LEFT));
        borderProps.add(Ints.consts.get(PropNames.BORDER_LEFT_COLOR));
        borderProps.add(Ints.consts.get(PropNames.BORDER_LEFT_STYLE));
        borderProps.add(Ints.consts.get(PropNames.BORDER_LEFT_WIDTH));
        borderProps.add(Ints.consts.get(PropNames.BORDER_RIGHT));
        borderProps.add(Ints.consts.get(PropNames.BORDER_RIGHT_COLOR));
        borderProps.add(Ints.consts.get(PropNames.BORDER_RIGHT_STYLE));
        borderProps.add(Ints.consts.get(PropNames.BORDER_RIGHT_WIDTH));
        borderProps.add(Ints.consts.get(PropNames.BORDER_START_COLOR));
        borderProps.add(Ints.consts.get(PropNames.BORDER_START_STYLE));
        borderProps.add(Ints.consts.get(PropNames.BORDER_START_WIDTH));
        borderProps.add(Ints.consts.get(PropNames.BORDER_START_WIDTH_LENGTH));
        borderProps.add
            (Ints.consts.get(PropNames.BORDER_START_WIDTH_CONDITIONALITY));
        borderProps.add(Ints.consts.get(PropNames.BORDER_STYLE));
        borderProps.add(Ints.consts.get(PropNames.BORDER_TOP));
        borderProps.add(Ints.consts.get(PropNames.BORDER_TOP_COLOR));
        borderProps.add(Ints.consts.get(PropNames.BORDER_TOP_STYLE));
        borderProps.add(Ints.consts.get(PropNames.BORDER_TOP_WIDTH));
        borderProps.add(Ints.consts.get(PropNames.BORDER_WIDTH));
        borderSet = Collections.unmodifiableSet((Set)borderProps);
    }
    /**
     * The number of <b>Common Font Properties</b>.
     */
    public static final int fontPropsSize = 9;
    /**
     * <tt>HashSet</tt> of the <tt>Integer</tt> objects corresponding to the
     * constant index of each property in the set of
     * <b>Common Font Properties</b>.
     */
    private static final HashSet fontProps = new HashSet(fontPropsSize);
    public static final Set fontSet;

    static {
        fontProps.add(Ints.consts.get(PropNames.FONT));
        fontProps.add(Ints.consts.get(PropNames.FONT_FAMILY));
        fontProps.add(Ints.consts.get(PropNames.FONT_SELECTION_STRATEGY));
        fontProps.add(Ints.consts.get(PropNames.FONT_SIZE));
        fontProps.add(Ints.consts.get(PropNames.FONT_SIZE_ADJUST));
        fontProps.add(Ints.consts.get(PropNames.FONT_STRETCH));
        fontProps.add(Ints.consts.get(PropNames.FONT_STYLE));
        fontProps.add(Ints.consts.get(PropNames.FONT_VARIANT));
        fontProps.add(Ints.consts.get(PropNames.FONT_WEIGHT));
        fontSet = Collections.unmodifiableSet((Set)fontProps);
    }
    /**
     * The number of <b>Common Hyphenation Properties</b>.
     */
    public static final int hyphenationPropsSize = 7;
    /**
     * <tt>HashSet</tt> of the <tt>Integer</tt> objects corresponding to the
     * constant index of each property in the set of
     * <b>Common Hyphenation Properties</b>.
     */
    private static final HashSet hyphenationProps =
                                            new HashSet(hyphenationPropsSize);
    public static final Set hyphenationSet;

    static {
        hyphenationProps.add(Ints.consts.get(PropNames.COUNTRY));
        hyphenationProps.add(Ints.consts.get(PropNames.LANGUAGE));
        hyphenationProps.add(Ints.consts.get(PropNames.SCRIPT));
        hyphenationProps.add(Ints.consts.get(PropNames.HYPHENATE));
        hyphenationProps.add(Ints.consts.get(PropNames.HYPHENATION_CHARACTER));
        hyphenationProps.add
                (Ints.consts.get(PropNames.HYPHENATION_PUSH_CHARACTER_COUNT));
        hyphenationProps.add
            (Ints.consts.get(PropNames.HYPHENATION_REMAIN_CHARACTER_COUNT));
        hyphenationSet = Collections.unmodifiableSet((Set)hyphenationProps);
    }
    /**
     * The number of <b>Common Margin-Block Properties</b>.
     */
    public static final int marginBlockPropsSize = 5;
    /**
     * <tt>HashSet</tt> of the <tt>Integer</tt> objects corresponding to the
     * constant index of each property in the set of
     * <b>Common Margin-Block Properties</b>.
     */
    private static final HashSet marginBlockProps =
                                            new HashSet(marginBlockPropsSize);
    public static final Set marginBlockSet;

    static {
        marginBlockProps.add(Ints.consts.get(PropNames.MARGIN));
        marginBlockProps.add(Ints.consts.get(PropNames.MARGIN_BOTTOM));
        marginBlockProps.add(Ints.consts.get(PropNames.MARGIN_LEFT));
        marginBlockProps.add(Ints.consts.get(PropNames.MARGIN_RIGHT));
        marginBlockProps.add(Ints.consts.get(PropNames.MARGIN_TOP));
        marginBlockSet = Collections.unmodifiableSet((Set)marginBlockProps);
    }
    /**
     * The number of <b>Common Margin-Inline Properties</b>.
     */
    public static final int marginInlinePropsSize = 2;
    /**
     * <tt>HashSet</tt> of the <tt>Integer</tt> objects corresponding to the
     * constant index of each property in the set of
     * <b>Common Margin-Inline Properties</b>.
     */
    private static final HashSet marginInlineProps =
                                        new HashSet(marginInlinePropsSize);
    public static final Set marginInlineSet;

    static {
        marginInlineProps.add(Ints.consts.get(PropNames.SPACE_END));
        marginInlineProps.add(Ints.consts.get(PropNames.SPACE_START));
        marginInlineSet = Collections.unmodifiableSet((Set)marginInlineProps);
    }
    /**
     * The number of <b>Common Padding Properties</b>.
     */
    public static final int paddingPropsSize = 17;
    /**
     * <tt>HashSet</tt> of the <tt>Integer</tt> objects corresponding to the
     * constant index of each property in the set of
     * <b>Common Padding Properties</b>.
     */
    private static final HashSet paddingProps = new HashSet(paddingPropsSize);
    public static final Set paddingSet;

    static {
        paddingProps.add(Ints.consts.get(PropNames.PADDING));
        paddingProps.add(Ints.consts.get(PropNames.PADDING_AFTER));
        paddingProps.add(Ints.consts.get(PropNames.PADDING_AFTER_LENGTH));
        paddingProps.add
                    (Ints.consts.get(PropNames.PADDING_AFTER_CONDITIONALITY));
        paddingProps.add(Ints.consts.get(PropNames.PADDING_BEFORE));
        paddingProps.add(Ints.consts.get(PropNames.PADDING_BEFORE_LENGTH));
        paddingProps.add
                    (Ints.consts.get(PropNames.PADDING_BEFORE_CONDITIONALITY));
        paddingProps.add(Ints.consts.get(PropNames.PADDING_BOTTOM));
        paddingProps.add(Ints.consts.get(PropNames.PADDING_END));
        paddingProps.add(Ints.consts.get(PropNames.PADDING_END_LENGTH));
        paddingProps.add
                    (Ints.consts.get(PropNames.PADDING_END_CONDITIONALITY));
        paddingProps.add(Ints.consts.get(PropNames.PADDING_LEFT));
        paddingProps.add(Ints.consts.get(PropNames.PADDING_RIGHT));
        paddingProps.add(Ints.consts.get(PropNames.PADDING_START));
        paddingProps.add(Ints.consts.get(PropNames.PADDING_START_LENGTH));
        paddingProps.add
                    (Ints.consts.get(PropNames.PADDING_START_CONDITIONALITY));
        paddingProps.add(Ints.consts.get(PropNames.PADDING_TOP));
        paddingSet = Collections.unmodifiableSet((Set)paddingProps);
    }
    /**
     * The number of <b>Common Relative Position Properties</b>.
     */
    public static final int relativePositionPropsSize = 5;
    /**
     * <tt>HashSet</tt> of the <tt>Integer</tt> objects corresponding to the
     * constant index of each property in the set of
     * <b>Common Relative Position Properties</b>.
     */
    private static final HashSet relativePositionProps =
                                    new HashSet(relativePositionPropsSize);
    public static final Set relativePositionSet;

    static {
        relativePositionProps.add
                            (Ints.consts.get(PropNames.RELATIVE_POSITION));
        relativePositionProps.add(Ints.consts.get(PropNames.BOTTOM));
        relativePositionProps.add(Ints.consts.get(PropNames.LEFT));
        relativePositionProps.add(Ints.consts.get(PropNames.RIGHT));
        relativePositionProps.add(Ints.consts.get(PropNames.TOP));
        relativePositionSet =
                    Collections.unmodifiableSet((Set)relativePositionProps);
    }
    /**
     * The number of <b>Common Table Properties</b>.
     */
    public static final int tablePropsSize = 21;
    /**
     * <tt>HashSet</tt> of the <tt>Integer</tt> objects corresponding to the
     * constant index of each property in the set of
     * <b>Common Table Properties</b>.
     */
    private static final HashSet tableProps = new HashSet(tablePropsSize);
    public static final Set tableSet;

    static {
        tableProps.add(Ints.consts.get(PropNames.BORDER_AFTER_PRECEDENCE));
        tableProps.add(Ints.consts.get(PropNames.BORDER_BEFORE_PRECEDENCE));
        tableProps.add(Ints.consts.get(PropNames.BORDER_COLLAPSE));
        tableProps.add(Ints.consts.get(PropNames.BORDER_END_PRECEDENCE));
        tableProps.add(Ints.consts.get(PropNames.BORDER_SEPARATION));
        tableProps.add(Ints.consts.get
                (PropNames.BORDER_SEPARATION_BLOCK_PROGRESSION_DIRECTION));
        tableProps.add(Ints.consts.get
                (PropNames.BORDER_SEPARATION_INLINE_PROGRESSION_DIRECTION));
        tableProps.add(Ints.consts.get(PropNames.BORDER_SPACING));
        tableProps.add(Ints.consts.get(PropNames.BORDER_START_PRECEDENCE));
        tableProps.add(Ints.consts.get(PropNames.CAPTION_SIDE));
        tableProps.add(Ints.consts.get(PropNames.COLUMN_NUMBER));
        tableProps.add(Ints.consts.get(PropNames.COLUMN_WIDTH));
        tableProps.add(Ints.consts.get(PropNames.EMPTY_CELLS));
        tableProps.add(Ints.consts.get(PropNames.ENDS_ROW));
        tableProps.add(Ints.consts.get(PropNames.NUMBER_COLUMNS_REPEATED));
        tableProps.add(Ints.consts.get(PropNames.NUMBER_COLUMNS_SPANNED));
        tableProps.add(Ints.consts.get(PropNames.NUMBER_ROWS_SPANNED));
        tableProps.add(Ints.consts.get(PropNames.STARTS_ROW));
        tableProps.add(Ints.consts.get(PropNames.TABLE_LAYOUT));
        tableProps.add(Ints.consts.get(PropNames.TABLE_OMIT_FOOTER_AT_BREAK));
        tableProps.add(Ints.consts.get(PropNames.TABLE_OMIT_HEADER_AT_BREAK));
        tableSet = Collections.unmodifiableSet((Set)tableProps);
    }

    // My preference here for shorthands which expand to compound properties
    // would be to expand only to the compound, and allow compounds to be
    // "normally" expanded in a second step.  Unfortunately, the shorthand
    // border-spacing expands (potentially) into both of the elements of
    // the border-separation compound.  However, other compound expansions
    // are defined with only a single value for the compound, so I will
    // treat border-separation as a special case in the event that two
    // values are provided.
    // I''m not sure whether a shorthand specification which sets a compound
    // property with a single top-level value should be regarded as a
    // specification for the purposes of inheritance, but I will assume so
    // until further notice.
    // pbw

    private static final int[] backgroundPosition = {
        PropNames.BACKGROUND_POSITION_HORIZONTAL
        ,PropNames.BACKGROUND_POSITION_VERTICAL
    };

    private static final int[] borderColor = {
        PropNames.BORDER_TOP_COLOR
        ,PropNames.BORDER_RIGHT_COLOR
        ,PropNames.BORDER_BOTTOM_COLOR
        ,PropNames.BORDER_LEFT_COLOR
    };

    private static final int[] borderStyle = {
        PropNames.BORDER_TOP_STYLE
        ,PropNames.BORDER_RIGHT_STYLE
        ,PropNames.BORDER_BOTTOM_STYLE
        ,PropNames.BORDER_LEFT_STYLE
    };

    private static final int[] borderWidth = {
        PropNames.BORDER_TOP_WIDTH
        ,PropNames.BORDER_RIGHT_WIDTH
        ,PropNames.BORDER_BOTTOM_WIDTH
        ,PropNames.BORDER_LEFT_WIDTH
    };

    public static final ROIntArray backgroundExpansion =
        new ROIntArray(new int[][] {
            new int[] {
                PropNames.BACKGROUND_COLOR
                ,PropNames.BACKGROUND_IMAGE
                ,PropNames.BACKGROUND_REPEAT
                ,PropNames.BACKGROUND_ATTACHMENT
                ,PropNames.BACKGROUND_POSITION_HORIZONTAL
                ,PropNames.BACKGROUND_POSITION_VERTICAL
            }, backgroundPosition});

    public static final ROIntArray backgroundPositionExpansion =
        new ROIntArray(backgroundPosition);

    public static final ROIntArray borderExpansion =
        new ROIntArray(new int[][] { borderStyle, borderColor, borderWidth });

    public static final ROIntArray borderStyleExpansion =
        new ROIntArray(borderStyle);

    public static final ROIntArray borderColorExpansion =
        new ROIntArray(borderColor);

    public static final ROIntArray borderWidthExpansion =
        new ROIntArray(borderWidth);

    public static final ROIntArray borderTopExpansion =
        new ROIntArray(new int[] {
            PropNames.BORDER_TOP_STYLE
            ,PropNames.BORDER_TOP_COLOR
            ,PropNames.BORDER_TOP_WIDTH
        });

    public static final ROIntArray borderRightExpansion =
        new ROIntArray(new int[] {
            PropNames.BORDER_RIGHT_STYLE
            ,PropNames.BORDER_RIGHT_COLOR
            ,PropNames.BORDER_RIGHT_WIDTH
        });

    public static final ROIntArray borderBottomExpansion =
        new ROIntArray(new int[] {
            PropNames.BORDER_BOTTOM_STYLE
            ,PropNames.BORDER_BOTTOM_COLOR
            ,PropNames.BORDER_BOTTOM_WIDTH
        });

    public static final ROIntArray borderLeftExpansion =
        new ROIntArray(new int[] {
            PropNames.BORDER_LEFT_STYLE
            ,PropNames.BORDER_LEFT_COLOR
            ,PropNames.BORDER_LEFT_WIDTH
        });

    /**
     * Watch this one.  <i>border-spacing</i> is a shorthand which expands
     * into the components of the <tt>&lt;border-separation&gt;</tt> compound
     * property.
     */
    public static final ROIntArray borderSpacingExpansion =
        new ROIntArray(new int[] {
            PropNames.BORDER_SEPARATION
        });

    public static final ROIntArray cueExpansion =
        new ROIntArray(new int[] {
            PropNames.CUE_BEFORE
            ,PropNames.CUE_AFTER
        });

    /**
     * Another nasty one.  <i>font</i> expands, in part, into
     * <i>line-height</i>, which is itself a compound property with a
     * <tt>&lt;space&gt;</tt> value.
     */
    public static final ROIntArray fontExpansion =
        new ROIntArray(new int[] {
            PropNames.FONT_FAMILY
            ,PropNames.FONT_STYLE
            ,PropNames.FONT_VARIANT
            ,PropNames.FONT_WEIGHT
            ,PropNames.FONT_SIZE
            ,PropNames.LINE_HEIGHT
            ,PropNames.FONT_SIZE_ADJUST
            ,PropNames.FONT_STRETCH
            ,PropNames.FONT_SELECTION_STRATEGY
        });

    public static final ROIntArray marginExpansion =
        new ROIntArray(new int[] {
            PropNames.MARGIN_TOP
            ,PropNames.MARGIN_RIGHT
            ,PropNames.MARGIN_BOTTOM
            ,PropNames.MARGIN_LEFT
        });

    public static final ROIntArray paddingExpansion =
        new ROIntArray(new int[] {
            PropNames.PADDING_TOP
            ,PropNames.PADDING_RIGHT
            ,PropNames.PADDING_BOTTOM
            ,PropNames.PADDING_LEFT
        });

    public static final ROIntArray pageBreakAfterExpansion =
        new ROIntArray(new int[] {
            PropNames.BREAK_AFTER
            ,PropNames.KEEP_WITH_NEXT
        });

    public static final ROIntArray pageBreakBeforeExpansion =
        new ROIntArray(new int[] {
            PropNames.BREAK_BEFORE
            ,PropNames.KEEP_WITH_PREVIOUS
        });

    public static final ROIntArray pageBreakInsideExpansion =
        new ROIntArray(new int[] {
            PropNames.KEEP_TOGETHER
        });

    public static final ROIntArray pauseExpansion =
        new ROIntArray(new int[] {
            PropNames.PAUSE_BEFORE
            ,PropNames.PAUSE_AFTER
        });

    public static final ROIntArray positionExpansion =
        new ROIntArray(new int[] {
            PropNames.RELATIVE_POSITION
            ,PropNames.ABSOLUTE_POSITION
        });

    public static final ROIntArray sizeExpansion =
        new ROIntArray(new int[] {
            PropNames.PAGE_HEIGHT
            ,PropNames.PAGE_WIDTH
        });

    public static final ROIntArray verticalAlignExpansion =
        new ROIntArray(new int[] {
            PropNames.ALIGNMENT_BASELINE
            ,PropNames.ALIGNMENT_ADJUST
            ,PropNames.BASELINE_SHIFT
            ,PropNames.DOMINANT_BASELINE
        });

    public static final ROIntArray whiteSpaceExpansion =
        new ROIntArray(new int[] {
            PropNames.LINEFEED_TREATMENT
            ,PropNames.WHITE_SPACE_COLLAPSE
            ,PropNames.WHITE_SPACE_TREATMENT
            ,PropNames.WRAP_OPTION
        });

    public static final ROIntArray xmlLangExpansion =
        new ROIntArray(new int[] {
            PropNames.COUNTRY
            ,PropNames.LANGUAGE
        });

    /**
     * Shorthand properties.  Where properties interact, they are listed
     * in increasing precision.
     */
    private static final int[] shorthands = {
            PropNames.BACKGROUND
            ,PropNames.BACKGROUND_POSITION
            ,PropNames.BORDER
            ,PropNames.BORDER_STYLE
            ,PropNames.BORDER_COLOR
            ,PropNames.BORDER_WIDTH
            ,PropNames.BORDER_TOP
            ,PropNames.BORDER_RIGHT
            ,PropNames.BORDER_BOTTOM
            ,PropNames.BORDER_LEFT
            ,PropNames.BORDER_SPACING
            ,PropNames.CUE
            ,PropNames.FONT
            ,PropNames.MARGIN
            ,PropNames.PADDING
            ,PropNames.PAGE_BREAK_AFTER
            ,PropNames.PAGE_BREAK_BEFORE
            ,PropNames.PAGE_BREAK_INSIDE
            ,PropNames.PAUSE
            ,PropNames.POSITION
            ,PropNames.SIZE
            ,PropNames.VERTICAL_ALIGN
            ,PropNames.WHITE_SPACE
            ,PropNames.XML_LANG
        };

    /**
     * Map property index to shorthand array index
     */
    private static final HashMap shorthandMap;
    static {
        shorthandMap = new HashMap(shorthands.length);
        for (int i = 0; i < shorthands.length; i++) {
            shorthandMap.put
                    ((Object)(Ints.consts.get(shorthands[i])), 
                     (Object)(Ints.consts.get(i)));
        }
    }

    /**
     * RO Shorthand properties.
     */
    public static final ROIntArray roShorthands =
        new ROIntArray(shorthands);

    /**
     * Array of <i>ROIntArray</i> <b>in same order as <i>shorthands</></b>
     * <i>ROIntArray</i>.
     * If a public view of this is required, use
     * Collections.unmodifiableList(Arrays.asList(shorthandExpansions))
     */
    private static final ROIntArray[] shorthandExpansions = {
        backgroundExpansion
        ,backgroundPositionExpansion
        ,borderExpansion
        ,borderStyleExpansion
        ,borderColorExpansion
        ,borderWidthExpansion
        ,borderTopExpansion
        ,borderRightExpansion
        ,borderBottomExpansion
        ,borderLeftExpansion
        ,borderSpacingExpansion
        ,cueExpansion
        ,fontExpansion
        ,marginExpansion
        ,paddingExpansion
        ,pageBreakAfterExpansion
        ,pageBreakBeforeExpansion
        ,pageBreakInsideExpansion
        ,pauseExpansion
        ,positionExpansion
        ,sizeExpansion
        ,verticalAlignExpansion
        ,whiteSpaceExpansion
        ,xmlLangExpansion
    };

    /**
     * @param property <tt>int</tt> property index
     * @return <tt>ROIntArray</tt> containing the expansion list for
     * this shorthand.
     * @exception <tt>PropertyException</tt> if this is not a valid
     * shorthand property
     */
    public static ROIntArray getSHandExpansionSet(int property)
        throws PropertyException
    {
        // Is the property of the argument a shorthand?
        Integer sHIndex =
                (Integer)(shorthandMap.get(Ints.consts.get(property)));
        if (sHIndex == null) {
            String propname = PropNames.getPropertyName(property);
            throw new PropertyException
                    (propname + " not a shorthand property");
        }
        // Get the array of indices of the properties in the
        // expansion of this shorthand
        return shorthandExpansions[property];
    }

    /**
     * Expand the shorthand property associated with the
     * <tt>PropertyValue</tt> argument by copying the given value for each
     * property in the expansion.  The <em>property</em> field of each
     * <tt>PropertyValue</tt> will be set to one of the proeprties in the
     * shorthand expansion.
     * @param value a <tt>propertyValue</tt> whose <em>property</em> field
     *  is assumed to be set to a shorthand property.
     * @return <tt>PropertyValueList</tt> containing a list of
     *  <tt>PropertyValue</tt>s, one for each property in the expansion of
     *  the shorthand property.
     * @exception PropertyException
     */
    public static PropertyValueList expandAndCopySHand(PropertyValue value)
        throws PropertyException
    {
        // The property associated with this PropertyValue
        int property = value.getProperty();
        ROIntArray expansion = getSHandExpansionSet(property);
        PropertyValueList list = new PropertyValueList(property);
        for (int i = 0; i < expansion.length; i++) {
            int expandedProp = expansion.get(i);
            PropertyValue expandedPropValue;
            //   The PropertyValue must be cloneable
            try {
                expandedPropValue = (PropertyValue)(value.clone());
            } catch (CloneNotSupportedException e) {
                throw new PropertyException(e.getMessage());
            }

            expandedPropValue.setProperty(expandedProp);
            list.add(expandedPropValue);
        }
        return list;
    }

    /**
     * Generate a list of the intial values of each property in a
     * shorthand expansion
     * @param foTree the <tt>FOTree</tt> for which properties are being
     * processed
     * @param property <tt>int</tt> property index
     * @return <tt>PropertyValueList</tt> containing the intial value
     * expansions for the (shorthand) property
     * @exception <tt>PropertyException</tt>
     */
    public static PropertyValueList initialValueExpansion
        (FOTree foTree, int property)
        throws PropertyException
    {
        ROIntArray expansion = getSHandExpansionSet(property);
        PropertyValueList list = new PropertyValueList(property);
        for (int i = 0; i < expansion.length; i++) {
            int expandedProp = expansion.get(i);
            PropertyValue expandedPropValue;
            PropertyValue specified =
                    foTree.getInitialValue(expandedProp).getSpecified();
            //   The PropertyValue must be cloneable
            try {
                expandedPropValue = (PropertyValue)(specified.clone());
            } catch (CloneNotSupportedException e) {
                throw new PropertyException(e.getMessage());
            }

            list.add(expandedPropValue);
        }
        return list;
    }

    /**
     * Given a shorthand expansion list and a <tt>PropertyValue</tt>,
     * override the list element corresponding to the <tt>PropertyValue</tt>.
     * Correspondence is based on the <em>property</em> field of the
     * <tt>PropertyValue</tt>.
     * @param list the expansion <tt>PropertyValueList</tt>
     * @param value the overriding <tt>PropertyValue</tt>
     * @return <tt>PropertyValueList</tt> the new list
     */
    public static PropertyValueList overrideSHandElement
        (PropertyValueList expansionList, PropertyValue element)
        throws PropertyException
    {
        int elementProp = element.getProperty();
        ListIterator elements = expansionList.listIterator();
        while (elements.hasNext()) {
            PropertyValue next = (PropertyValue)(elements.next());
            if (next.getProperty() == elementProp) {
                elements.set(element);
                return expansionList;
            }
        }
        throw new PropertyException
                ("Unmatched property " + elementProp +
                 " in expansion list for " + expansionList.getProperty());
    }

    private PropertySets (){}

}
