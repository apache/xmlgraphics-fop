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
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;

import org.apache.fop.datastructs.ROBitSet;
import org.apache.fop.datastructs.ROIntArray;
import org.apache.fop.datatypes.Ints;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.PropertyValueList;
import org.apache.fop.fo.expr.PropertyException;

/**
 * This class contains <tt>ROIntArray</tt>s which encode the various sets of
 * properties into which the shorthand and compound properties expand, and
 * utility methods for manipulating these expansions.
 */
public class ShorthandPropSets {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    private static final String packageName = "org.apache.fop.fo";


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
     * Array of <i>ROIntArray</i><b> in same order as <i>shorthands</i></b>
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
     * Map property index to shorthand array index
     */
    private static final HashMap shorthandMap;
    static {
        shorthandMap = new HashMap(shorthands.length);
        for (int i = 0; i < shorthands.length; i++) {
            shorthandMap.put
                    ((Ints.consts.get(shorthands[i])), 
                     (Ints.consts.get(i)));
        }
    }

    /**
     * RO Shorthand properties.
     */
    public static final ROIntArray roShorthands =
        new ROIntArray(shorthands);

    /**
     * A <tt>ROBitSet</tt> of the shorthand properties.
     */
    public static final ROBitSet shorthandPropSet;
    private static final BitSet shorthandpropset;
    static {
        shorthandpropset = new BitSet(PropNames.LAST_PROPERTY_INDEX + 1);
        for (int i = 0; i < shorthands.length; i++)
            shorthandpropset.set(shorthands[i]);
        shorthandPropSet = new ROBitSet(shorthandpropset);
    }

    /**
     * @param property <tt>int</tt> property index
     * @return <tt>ROIntArray</tt> containing the expansion list for
     * this shorthand.
     * @exception PropertyException if this is not a valid
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
        return shorthandExpansions[sHIndex.intValue()];
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
        return copyValueToSet(value, expansion, list);
    }

    /**
     * Generate a list of the intial values of each property in a
     * shorthand expansion.  Note that this will be a list of
     * <b>references</b> to the initial values.
     * @param foTree the <tt>FOTree</tt> for which properties are being
     * processed
     * @param property <tt>int</tt> property index
     * @return <tt>PropertyValueList</tt> containing the intial value
     * expansions for the (shorthand) property
     * @exception <tt>PropertyException</tt>
     */
     /*
      Don't do this.  Shorthands should not expand initial values, because
      a distinction is needed between those properties which are given
      a specified value and those which are set by normal inheritance or from
      their initial values.  This so that fromSpecifiedValue() will work.

    public static PropertyValueList initialValueSHandExpansion
        (FOTree foTree, int property)
        throws PropertyException
    {
        ROIntArray expansion = getSHandExpansionSet(property);
        PropertyValueList list = new PropertyValueList(property);
        for (int i = 0; i < expansion.length; i++) {
            int expandedProp = expansion.get(i);
            PropertyValue specified
                    = foTree.getInitialSpecifiedValue(expandedProp);
            list.add(specified);
        }
        return list;
    }
    */

    /**
     * Given a shorthand expansion list and a <tt>PropertyValue</tt>,
     * override the list element corresponding to the <tt>PropertyValue</tt>.
     * Correspondence is based on the <em>property</em> field of the
     * <tt>PropertyValue</tt>.
     * @param expansionList the expansion <tt>PropertyValueList</tt>
     * @param element the overriding <tt>PropertyValue</tt>
     * @return <tt>PropertyValueList</tt> the expansion list with the
     *  appropriate element reset
     * @exception PropertyException
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

    /**
     * Given a shorthand expansion list and a <tt>PropertyValueList</tt>,
     * override the expansion list elements corresponding to the elements
     * of the <tt>PropertyValueList</tt>.
     * Correspondence is based on the <em>property</em> field of the
     * <tt>PropertyValue</tt>.
     * @param expansionList the expansion <tt>PropertyValueList</tt>
     * @param list the overriding <tt>PropertyValueList</tt>
     * @return <tt>PropertyValueList</tt> the new expansion list with
     *  appropriate elements reset
     * @exception PropertyException
     */
    public static PropertyValueList overrideSHandElements
        (PropertyValueList expansionList, PropertyValueList list)
        throws PropertyException
    {
        // From the overriding list, form an array of PropertyValue references
        // an array of property indices and an array of booleans,
        int listsize = list.size();
        Object[] listrefs = new Object[listsize];
        int[] listprops = new int[listsize];
        boolean[] propseen = new boolean[listsize];
        Iterator listels = list.iterator();
        int i = 0;
        while (listels.hasNext()) {
            listrefs[i] = listels.next();
            listprops[i] = ((PropertyValue)listrefs[i]).getProperty();
            i++;
        }

        ListIterator elements = expansionList.listIterator();
        while (elements.hasNext()) {
            PropertyValue next = (PropertyValue)(elements.next());
            int expprop = next.getProperty();
            for (i = 0; i < listsize; i++) {
                if (expprop != listprops[i]) continue;
                elements.set(listrefs[i]);
                propseen[i] = true;
            }
        }
        // Check for unmatched override elements
        String unmatched = "";
        boolean someunmatched = false;
        for (i = 0; i < listsize; i++) {
            if ( ! propseen[i]) {
                someunmatched = true;
                unmatched = unmatched + " " +
                        PropNames.getPropertyName(listprops[i]);
            }
        }
        if (someunmatched)
            throw new PropertyException
                ("Unmatched properties:" + unmatched +
                 " : in expansion list for " + expansionList.getProperty());
        return expansionList;
    }

    public static final ROIntArray blockProgressionDimensionCopyExpansion =
        new ROIntArray(new int[] {
            PropNames.BLOCK_PROGRESSION_DIMENSION_MINIMUM
            ,PropNames.BLOCK_PROGRESSION_DIMENSION_OPTIMUM
            ,PropNames.BLOCK_PROGRESSION_DIMENSION_MAXIMUM
        });

    public static final ROIntArray blockProgressionDimensionNonCopyExpansion =
        new ROIntArray(new int[] {});

    public static final ROIntArray borderAfterWidthCopyExpansion =
        new ROIntArray(new int[] {
            PropNames.BORDER_AFTER_WIDTH_LENGTH
        });

    public static final ROIntArray borderAfterWidthNonCopyExpansion =
        new ROIntArray(new int[] {
            PropNames.BORDER_AFTER_WIDTH_CONDITIONALITY
        });

    public static final ROIntArray borderBeforeWidthCopyExpansion =
        new ROIntArray(new int[] {
            PropNames.BORDER_BEFORE_WIDTH_LENGTH
        });

    public static final ROIntArray borderBeforeWidthNonCopyExpansion =
        new ROIntArray(new int[] {
            PropNames.BORDER_BEFORE_WIDTH_CONDITIONALITY
        });

    public static final ROIntArray borderEndWidthCopyExpansion =
        new ROIntArray(new int[] {
            PropNames.BORDER_END_WIDTH_LENGTH
        });

    public static final ROIntArray borderEndWidthNonCopyExpansion =
        new ROIntArray(new int[] {
            PropNames.BORDER_END_WIDTH_CONDITIONALITY
        });

    public static final ROIntArray borderStartWidthCopyExpansion =
        new ROIntArray(new int[] {
            PropNames.BORDER_START_WIDTH_LENGTH
        });

    public static final ROIntArray borderStartWidthNonCopyExpansion =
        new ROIntArray(new int[] {
            PropNames.BORDER_START_WIDTH_CONDITIONALITY
        });

    public static final ROIntArray borderSeparationCopyExpansion =
        new ROIntArray(new int[] {
            PropNames.BORDER_SEPARATION_BLOCK_PROGRESSION_DIRECTION
            ,PropNames.BORDER_SEPARATION_INLINE_PROGRESSION_DIRECTION
        });

    public static final ROIntArray borderSeparationNonCopyExpansion =
        new ROIntArray(new int[] {});

    public static final ROIntArray inlineProgressionDimensionCopyExpansion =
        new ROIntArray(new int[] {
            PropNames.INLINE_PROGRESSION_DIMENSION_MINIMUM
            ,PropNames.INLINE_PROGRESSION_DIMENSION_OPTIMUM
            ,PropNames.INLINE_PROGRESSION_DIMENSION_MAXIMUM
        });

    public static final ROIntArray inlineProgressionDimensionNonCopyExpansion =
        new ROIntArray(new int[] {});

    public static final ROIntArray keepTogetherCopyExpansion =
        new ROIntArray(new int[] {
            PropNames.KEEP_TOGETHER_WITHIN_LINE
            ,PropNames.KEEP_TOGETHER_WITHIN_COLUMN
            ,PropNames.KEEP_TOGETHER_WITHIN_PAGE
        });

    public static final ROIntArray keepTogetherNonCopyExpansion =
        new ROIntArray(new int[] {});

    public static final ROIntArray keepWithNextCopyExpansion =
        new ROIntArray(new int[] {
            PropNames.KEEP_WITH_NEXT_WITHIN_LINE
            ,PropNames.KEEP_WITH_NEXT_WITHIN_COLUMN
            ,PropNames.KEEP_WITH_NEXT_WITHIN_PAGE
        });

    public static final ROIntArray keepWithNextNonCopyExpansion =
        new ROIntArray(new int[] {});

    public static final ROIntArray keepWithPreviousCopyExpansion =
        new ROIntArray(new int[] {
            PropNames.KEEP_WITH_PREVIOUS_WITHIN_LINE
            ,PropNames.KEEP_WITH_PREVIOUS_WITHIN_COLUMN
            ,PropNames.KEEP_WITH_PREVIOUS_WITHIN_PAGE
        });

    public static final ROIntArray keepWithPreviousNonCopyExpansion =
        new ROIntArray(new int[] {});

    public static final ROIntArray leaderLengthCopyExpansion =
        new ROIntArray(new int[] {
            PropNames.LEADER_LENGTH_MINIMUM
            ,PropNames.LEADER_LENGTH_OPTIMUM
            ,PropNames.LEADER_LENGTH_MAXIMUM
        });

    public static final ROIntArray leaderLengthNonCopyExpansion =
        new ROIntArray(new int[] {});

    public static final ROIntArray letterSpacingCopyExpansion =
        new ROIntArray(new int[] {
            PropNames.LETTER_SPACING_MINIMUM
            ,PropNames.LETTER_SPACING_OPTIMUM
            ,PropNames.LETTER_SPACING_MAXIMUM
        });

    public static final ROIntArray letterSpacingNonCopyExpansion =
        new ROIntArray(new int[] {
            PropNames.LETTER_SPACING_CONDITIONALITY
            ,PropNames.LETTER_SPACING_PRECEDENCE
        });

    public static final ROIntArray lineHeightCopyExpansion =
        new ROIntArray(new int[] {
            PropNames.LINE_HEIGHT_MINIMUM
            ,PropNames.LINE_HEIGHT_OPTIMUM
            ,PropNames.LINE_HEIGHT_MAXIMUM
        });

    public static final ROIntArray lineHeightNonCopyExpansion =
        new ROIntArray(new int[] {
            PropNames.LINE_HEIGHT_CONDITIONALITY
            ,PropNames.LINE_HEIGHT_PRECEDENCE
        });

    public static final ROIntArray paddingAfterCopyExpansion =
        new ROIntArray(new int[] {
            PropNames.PADDING_AFTER_LENGTH
        });

    public static final ROIntArray paddingAfterNonCopyExpansion =
        new ROIntArray(new int[] {
            PropNames.PADDING_AFTER_CONDITIONALITY
        });

    public static final ROIntArray paddingBeforeCopyExpansion =
        new ROIntArray(new int[] {
            PropNames.PADDING_BEFORE_LENGTH
        });

    public static final ROIntArray paddingBeforeNonCopyExpansion =
        new ROIntArray(new int[] {
            PropNames.PADDING_BEFORE_CONDITIONALITY
        });

    public static final ROIntArray paddingEndCopyExpansion =
        new ROIntArray(new int[] {
            PropNames.PADDING_END_LENGTH
        });

    public static final ROIntArray paddingEndNonCopyExpansion =
        new ROIntArray(new int[] {
            PropNames.PADDING_END_CONDITIONALITY
        });

    public static final ROIntArray paddingStartCopyExpansion =
        new ROIntArray(new int[] {
            PropNames.PADDING_START_LENGTH
        });

    public static final ROIntArray paddingStartNonCopyExpansion =
        new ROIntArray(new int[] {
            PropNames.PADDING_START_CONDITIONALITY
        });

    public static final ROIntArray spaceAfterCopyExpansion =
        new ROIntArray(new int[] {
            PropNames.SPACE_AFTER_MINIMUM
            ,PropNames.SPACE_AFTER_OPTIMUM
            ,PropNames.SPACE_AFTER_MAXIMUM
        });

    public static final ROIntArray spaceAfterNonCopyExpansion =
        new ROIntArray(new int[] {
            PropNames.SPACE_AFTER_CONDITIONALITY
            ,PropNames.SPACE_AFTER_PRECEDENCE
        });

    public static final ROIntArray spaceBeforeCopyExpansion =
        new ROIntArray(new int[] {
            PropNames.SPACE_BEFORE_MINIMUM
            ,PropNames.SPACE_BEFORE_OPTIMUM
            ,PropNames.SPACE_BEFORE_MAXIMUM
        });

    public static final ROIntArray spaceBeforeNonCopyExpansion =
        new ROIntArray(new int[] {
            PropNames.SPACE_BEFORE_CONDITIONALITY
            ,PropNames.SPACE_BEFORE_PRECEDENCE
        });

    public static final ROIntArray spaceEndCopyExpansion =
        new ROIntArray(new int[] {
            PropNames.SPACE_END_MINIMUM
            ,PropNames.SPACE_END_OPTIMUM
            ,PropNames.SPACE_END_MAXIMUM
        });

    public static final ROIntArray spaceEndNonCopyExpansion =
        new ROIntArray(new int[] {
            PropNames.SPACE_END_CONDITIONALITY
            ,PropNames.SPACE_END_PRECEDENCE
        });

    public static final ROIntArray spaceStartCopyExpansion =
        new ROIntArray(new int[] {
            PropNames.SPACE_START_MINIMUM
            ,PropNames.SPACE_START_OPTIMUM
            ,PropNames.SPACE_START_MAXIMUM
        });

    public static final ROIntArray spaceStartNonCopyExpansion =
        new ROIntArray(new int[] {
            PropNames.SPACE_START_CONDITIONALITY
            ,PropNames.SPACE_START_PRECEDENCE
        });

    public static final ROIntArray wordSpacingCopyExpansion =
        new ROIntArray(new int[] {
            PropNames.WORD_SPACING_MINIMUM
            ,PropNames.WORD_SPACING_OPTIMUM
            ,PropNames.WORD_SPACING_MAXIMUM
        });

    public static final ROIntArray wordSpacingNonCopyExpansion =
        new ROIntArray(new int[] {
            PropNames.WORD_SPACING_CONDITIONALITY
            ,PropNames.WORD_SPACING_PRECEDENCE
        });

    private static final int[] compounds = {
        PropNames.BLOCK_PROGRESSION_DIMENSION
        ,PropNames.BORDER_AFTER_WIDTH
        ,PropNames.BORDER_BEFORE_WIDTH
        ,PropNames.BORDER_END_WIDTH
        ,PropNames.BORDER_START_WIDTH
        ,PropNames.BORDER_SEPARATION
        ,PropNames.INLINE_PROGRESSION_DIMENSION
        ,PropNames.KEEP_TOGETHER
        ,PropNames.KEEP_WITH_NEXT
        ,PropNames.KEEP_WITH_PREVIOUS
        ,PropNames.LEADER_LENGTH
        ,PropNames.LETTER_SPACING
        ,PropNames.LINE_HEIGHT
        ,PropNames.PADDING_AFTER
        ,PropNames.PADDING_BEFORE
        ,PropNames.PADDING_END
        ,PropNames.PADDING_START
        ,PropNames.SPACE_AFTER
        ,PropNames.SPACE_BEFORE
        ,PropNames.SPACE_END
        ,PropNames.SPACE_START
        ,PropNames.WORD_SPACING
    };

    /**
     * Map property index to compound array index
     */
    private static final HashMap compoundMap;
    static {
        compoundMap = new HashMap(compounds.length);
        for (int i = 0; i < compounds.length; i++) {
            compoundMap.put
                    ((Ints.consts.get(compounds[i])), 
                     (Ints.consts.get(i)));
        }
    }

    /**
     * RO compound properties.
     */
    public static final ROIntArray roCompounds;
    static {
        roCompounds = new ROIntArray(compounds);
    }

    /**
     * A <tt>ROBitSet</tt> of the compound properties.
     */
    public static final ROBitSet compoundPropSet;
    private static final BitSet compoundpropset;

    /**
     * a <tt>ROBitSet of shorthand and compound properties.
     */
    public static final ROBitSet shorthandCompoundProps;
    private static final BitSet shorthandcompoundprops;

    static {
        compoundpropset = new BitSet(PropNames.LAST_PROPERTY_INDEX + 1);
        for (int i = 0; i < compounds.length; i++)
            compoundpropset.set(compounds[i]);
        compoundPropSet = new ROBitSet(compoundpropset);
        shorthandcompoundprops = new BitSet();
        shorthandcompoundprops.or(compoundpropset);
        shorthandcompoundprops.or(shorthandpropset);
        shorthandCompoundProps = new ROBitSet(shorthandcompoundprops);
    }

    /**
     * Array of <i>ROIntArray</i><b> of the copy expansion properties of
     * compounds in same order as <i>compounds</i></b>
     * <i>ROIntArray</i>.
     * If a public view of this is required, use
     * Collections.unmodifiableList(Arrays.asList(compoundCopyExpansions))
     */
    private static final ROIntArray[] compoundCopyExpansions = {
        blockProgressionDimensionCopyExpansion
        ,borderAfterWidthCopyExpansion
        ,borderBeforeWidthCopyExpansion
        ,borderEndWidthCopyExpansion
        ,borderStartWidthCopyExpansion
        ,borderSeparationCopyExpansion
        ,inlineProgressionDimensionCopyExpansion
        ,keepTogetherCopyExpansion
        ,keepWithNextCopyExpansion
        ,keepWithPreviousCopyExpansion
        ,leaderLengthCopyExpansion
        ,letterSpacingCopyExpansion
        ,lineHeightCopyExpansion
        ,paddingAfterCopyExpansion
        ,paddingBeforeCopyExpansion
        ,paddingEndCopyExpansion
        ,paddingStartCopyExpansion
        ,spaceAfterCopyExpansion
        ,spaceBeforeCopyExpansion
        ,spaceEndCopyExpansion
        ,spaceStartCopyExpansion
        ,wordSpacingCopyExpansion
    };

    /**
     * Array of <i>ROIntArray</i><b> of the non-copy expansion properties of
     * compounds in same order as <i>compounds</i></b>
     * <i>ROIntArray</i>.
     * If a public view of this is required, use
     * Collections.unmodifiableList(Arrays.asList(compoundNonCopyExpansions))
     */
    private static final ROIntArray[] compoundNonCopyExpansions = {
        blockProgressionDimensionNonCopyExpansion
        ,borderAfterWidthNonCopyExpansion
        ,borderBeforeWidthNonCopyExpansion
        ,borderEndWidthNonCopyExpansion
        ,borderStartWidthNonCopyExpansion
        ,borderSeparationNonCopyExpansion
        ,inlineProgressionDimensionNonCopyExpansion
        ,keepTogetherNonCopyExpansion
        ,keepWithNextNonCopyExpansion
        ,keepWithPreviousNonCopyExpansion
        ,leaderLengthNonCopyExpansion
        ,letterSpacingNonCopyExpansion
        ,lineHeightNonCopyExpansion
        ,paddingAfterNonCopyExpansion
        ,paddingBeforeNonCopyExpansion
        ,paddingEndNonCopyExpansion
        ,paddingStartNonCopyExpansion
        ,spaceAfterNonCopyExpansion
        ,spaceBeforeNonCopyExpansion
        ,spaceEndNonCopyExpansion
        ,spaceStartNonCopyExpansion
        ,wordSpacingNonCopyExpansion
    };

    /**
     * Expand the <tt>PropertyValue</tt> assigned to a compound property
     * into <tt>propertyValues</tt> for the individual property components.
     * N.B. This method assumes that the set of expansion properties is
     * comprised of a copy and a non-copy set.  For example, &lt;space&gt;
     * compounds have a copy set of .minimum, .optimum and .maximum, and a
     * non-copy set of .precedence and .conditionality. For each element of
     * the copy set, the given value is cloned.  For each member of the
     * non-copy set, a reference to the initial value is taken.
     * @param foTree - the <tt>FOTree</tt> for which properties are being
     * developed.
     * @param value - the <tt>PropertyValue</tt> to be cloned for the copy
     * set members.
     * @return a <tt>PropertyValueList</tt> containing the copy set
     * expansions followed by the non-copy set expansions, in the order
     * they are defined in appropriate <tt>ROIntArray</tt>s in this class.
     */
    public static PropertyValueList expandCompoundProperty
                                        (FOTree foTree, PropertyValue value)
        throws PropertyException
    {
        int property = value.getProperty();
        Integer compoundX =
                    (Integer)(compoundMap.get(Ints.consts.get(property)));
        if (compoundX == null)
            throw new PropertyException
                (PropNames.getPropertyName(property) + " (" + property + ") "
                + " is not a compound property.");
        int compoundIdx = compoundX.intValue();
        PropertyValueList list = new PropertyValueList(property);
        ROIntArray expansion;
        // Expand the copy components
        list = copyValueToSet
                        (value, compoundCopyExpansions[compoundIdx], list);
        // Expand the non-copy components
        return initialValueCompoundExpansion
                    (foTree, compoundNonCopyExpansions[compoundIdx], list);
    }

    /**
     * Clone the given property value for each property in the given
     * expansion set.  Append the new property values to the given list.
     * @param value - the property value to clone.
     * @param expansionSet - the set of indices of the expansion properties.
     * @param list - the list to which to append the expansion elements.
     * @return the original <tt>PropertyValueList</tt> containing the
     * appended expansions.
     * @exception PropertyException
     */
    private static PropertyValueList copyValueToSet(PropertyValue value,
                            ROIntArray expansionSet, PropertyValueList list)
        throws PropertyException
    {
        for (int i = 0; i < expansionSet.length; i++) {
            int expandedProp = expansionSet.get(i);
            PropertyValue expandedPropValue;
            //   The PropertyValue must be cloneable
            // The property associated with each PV in the expansion will
            // necessarily be different.
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
     * Append the initial values of each non-copy property in a
     * compound expansion to a list.  Note that these elements will be
     * <b>references</b> to the initial values. Note also that, in the
     * expansion of a compound value, the initial value comonents are
     * regarded as having been specified.
     * @param foTree tree of the node whose properties are
     * being processed
     * @param expansion the set of indices of the expansion properties
     * @param list the list to which to append the expansion elements
     * @return the original <tt>PropertyValueList</tt> containing the
     * appended initial value expansions for the (compound) property.
     * @exception PropertyException
     */
    public static PropertyValueList initialValueCompoundExpansion
                (FOTree foTree, ROIntArray expansion, PropertyValueList list)
        throws PropertyException
    {
        for (int i = 0; i < expansion.length; i++) {
            int expandedProp = expansion.get(i);
            PropertyValue specified
                    = PropertyConsts.pconsts.getInitialValue(expandedProp);
            list.add(specified);
        }
        return list;
    }

    private ShorthandPropSets (){}

}
