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
 */
package org.apache.fop.fo.properties;

import java.util.HashMap;

import org.apache.fop.datatypes.EnumType;
import org.apache.fop.datatypes.FontFamilySet;
import org.apache.fop.datatypes.Ints;
import org.apache.fop.datatypes.MappedNumeric;
import org.apache.fop.datatypes.NCName;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.PropertyValueList;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.PropertyConsts;
import org.apache.fop.fo.ShorthandPropSets;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.expr.SystemFontFunction;

public class Font extends Property  {
    public static final int dataTypes = SHORTHAND;

    public int getDataTypes() {
        return dataTypes;
    }

    public static final int traitMapping = SHORTHAND_MAP;

    public int getTraitMapping() {
        return traitMapping;
    }

    public static final int initialValueType = NOTYPE_IT;

    public int getInitialValueType() {
        return initialValueType;
    }

    public static final int inherited = NO;

    public int getInherited() {
        return inherited;
    }


    public static final int
               CAPTION = 1
                 ,ICON = 2
                 ,MENU = 3
          ,MESSAGE_BOX = 4
        ,SMALL_CAPTION = 5
           ,STATUS_BAR = 6
                       ;

    private static final String[] rwEnums = {
        null
        ,"caption"
        ,"icon"
        ,"menu"
        ,"message-box"
        ,"small-caption"
        ,"status-bar"
    };

    private static final HashMap rwEnumHash;
    static {
        rwEnumHash = new HashMap((int)(rwEnums.length / 0.75) + 1);
        for (int i = 1; i < rwEnums.length; i++ ) {
            rwEnumHash.put(rwEnums[i],
                                Ints.consts.get(i));
        }
    }

    /**
     * 'value' is a PropertyValueList or an individual PropertyValue.
     *
     * <p>If 'value' is a PropertyValueList it may contain, in turn, a
     * PropertyValueList with the space-separated elements of the
     * original expression, or a list of comma-separated elements if
     * these were the only elements of the expression.
     * <p>
     * If a top-level list occurs, i.e. if 'value' is a PropertyValueList
     * which contains multiple elements, it represents a comma-separated
     * list.  Commas can only legitimately occur between elements of
     * a font-family specifier, but more than one may occur.
     * The font-family specifier MUST be preceded by at least one other
     * element, a font-size, and MAY be preceded by more than one.
     * I.e., if a COMMA is present in the list,
     * <pre>
     * value (PropertyValueList)
     *   |
     *   +  PropertyValueList COMMA element [...]
     *      (space separated) 
     *              |
     *              +- PropertyValue PropertyValue [...]
     * </pre>
     * <p>The complication here is that, while the final element of this
     * list must belong with the comma-separated font-family specifier,
     * preceding elements MAY also belong to the font-family.
     *
     * <p>If 'value' is <i>not</i> a PropertyValueList,
     * 'value' can contain a parsed Inherit value or
     *  a parsed NCName value containing one of the system font
     *  enumeration tokens listed in rwEnums, above.
     *  Note that it cannot contain a font-family, because that MUST
     *  be preceded by at least one other element.
     *
     * <p>If 'value' is a space-separated list, it may contain:
     *   A trailing <em>font-family</em> specification, preceded by
     *   a dimension specification, possibly
     *   preceded by an initial detailed font specification.
     *   The font-family specification may be comprised of a single
     *   element or a comma-separated list of specific or generic font
     *   names.  A single font name may be a space-separated name.
     *
     *   <p>The optional initial detailed font specification may be
     *    comprised of a trailing dimension specifier, possibly preceded
     *    by an optional set of font descriptor elements.
     *
     *   <p>The dimension specifier is comprised of a <em>font-size</em>
     *    specifier, possibly followed by a height
     *    specifier.  The height specifier is made up of a slash
     *    character followed by a <em>line-height</em> specifier proper.
     *
     *   <p>The optional set of font descriptor elements may include,
     *    in any order, from one to three of
     *    <br>a font-style element
     *    <br>a font-variant element
     *    <br>a font-weight element
     * <p>
     * [ [&lt;font-style&gt;||&lt;font-variant&gt;||&lt;font-weight&gt;]?
     *    &lt;font-size&gt; [/ &lt;line-height&gt;]? &lt;font-family&gt; ]
     *    |caption|icon|menu|message-box|small-caption|status-bar
     *    |inherit
     *
     * <p><b>Handling the font-family list.</b>
     * <p>For any list, the last element or elements must belong to a
     * font-family specifier.  If only a space-separated list is present
     * only one font is specified in the font-family, but it may have
     * space-separated components, e.g. 'Times New' in 'Times New Roman'.
     * If a comma-separated list is present, there are COMMAs + 1 fonts
     * given in the font-family, but for the first of these, some parts
     * of the name may be present in the preceding space-separated list,
     * e.g. (again) 'Times New'.
     *
     * <p>This is handled by splitting the passed PropertyValueList into
     * two sublists:- initial-list and font-family-list.  The latter
     * contains those components which MUST bw part of a font-family
     * specifier.  The trailing elements of the initila-list are scanned
     * to determine which are components of the font-family-list, and
     * these are trimmed from initial-list and prepended to
     * font-family-list.
     *
     * <p>The value(s) provided, if valid, are converted into a list
     * containing the expansion of the shorthand.  A minimum of one
     * value will be present.
     *
     * <p>N.B. from the specification:<br>
     * <p>QUOTE<br>
     * The "font" property is, except as described below, a shorthand
     * property for setting "font-style", "font-variant", "font-weight",
     * "font-size", "line-height", and "font-family", at the same place
     * in the stylesheet. The syntax of this property is based on a
     * traditional typographical shorthand notation to set multiple
     * properties related to fonts.
     * 
     * <p>All font-related properties are first reset to their initial
     * values, including those listed in the preceding paragraph plus
     * "font-stretch" and "font-size-adjust". Then, those properties
     * that are given explicit values in the "font" shorthand are set
     * to those values. For a definition of allowed and initial values,
     * see the previously defined properties. For reasons of backward
     * compatibility, it is not possible to set "font-stretch" and
     * "font-size-adjust" to other than their initial values using the
     * "font" shorthand property; instead, set the individual properties.
     * <br>ENDQUOTE
     *
     * <p>The setup of the shorthand expansion list is determined by the
     * above considerations.
     *
     * @param propindex - the <tt>int</tt> property index.
     * @param foNode - the <tt>FONode</tt> being built
     * @param value <tt>PropertyValue</tt> returned by the parser
     * @return <tt>PropertyValue</tt> the verified value
     */
    public PropertyValue refineParsing
                        (int propindex, FONode foNode, PropertyValue value)
                    throws PropertyException
    {
        PropertyValueList startList = null;
        PropertyValueList fontList = null;
        if (value.getType() != PropertyValue.LIST) {
            return processValue(foNode, value);
        } else {
            fontList = (PropertyValueList)value;
            try {
                startList = spaceSeparatedList(fontList);
                // A space-separated list.  Only the last item(s) can
                // be part of the font-family-specifier
                // Remove the space-separated list element (the only
                // element) from fontList, so that the then-empty
                // fontList can serve as the
                // cachement list for font-family elements discovered
                // at the end of startList
                fontList.removeFirst();
            } catch (PropertyException e) {
                // Must be a comma-separated list
                // Pop the first element off the list; this will then
                // be equivalent to the space-separated list above.
                // Keep the remainder as the seed for a list argument
                // to the FontFamily verifier
                // Note the assumption that the list is not empty.
                // This is consistent with the values returned from the
                // parser.
                Object tmpo = fontList.removeFirst();
                // This MUST be a list, because it must contain both the
                // font-family element preceding the implict COMMA which
                // separated the original elements, and at least a
                // font-size element
                if ( ! (tmpo instanceof PropertyValueList))
                    throw new PropertyException
                        ("No space-separated list preceding COMMA in "
                        + "'font' expression");
                startList = (PropertyValueList)tmpo;
            }
            return processSpaceSepList(foNode, startList, fontList);
        }
    }

    private PropertyValueList processValue
        (FONode foNode, PropertyValue value)
                    throws PropertyException
    {
        // Can be Inherit, FromParent, FromNearestSpecified, a
        // system font NCName or a single element font-family specifier
        int type = value.getType();
        if (type == PropertyValue.INHERIT ||
                type == PropertyValue.FROM_PARENT ||
                    type == PropertyValue.FROM_NEAREST_SPECIFIED)
        {
            return refineExpansionList(PropNames.FONT, foNode,
                                ShorthandPropSets.expandAndCopySHand(value));
        }
        // else not Inherit/From../From..
        FontFamilySet family = null;
        if (type == PropertyValue.NCNAME) {
            // Is it a system font enumeration?
            EnumType enumval = null;
            String ncname = ((NCName)value).getNCName();
            try {
                enumval = new EnumType(value.getProperty(), ncname);
            } catch (PropertyException e) {
                throw new PropertyException
                    ("Unrecognized NCName in font expression: " + ncname);
            }
            // A system font enumval
            // System font characteristics should require no further
            // refinement
            return SystemFontFunction.expandFontSHand
                                            (PropNames.FONT, ncname);
        }
        throw new PropertyException("Unrecognized element in font " +
                                    "expression: " + value);
    }

    /**
     * The space separated <i>list</i> must end with the first or only
     * font named in the font-family specifier of the font expression.<br>
     * The comma-separated <i>fontList</i> contains the second and
     * subsequent font names from the font-family specifier in the font
     * expression, or is empty.<br>
     * <p>Because font names may contain spaces and may be
     * specified "raw", without enclosing quotes, the font name which
     * ends the <i>list</i> may occupy more than one element at the end
     * of the <i>list</i>.
     * <p>The font-family is preceded by a compulsory size/height element;
     * font-size [ / line-height ]?, so searching backwards for a slash
     * character will locate the penultimate and utlimate elements
     * preceding the first component of the font-family.
     * <p>If no slash is found, the preceding element must be a font-size,
     * which may be specfied as a Length, a Percentage, or with an
     * enumeration token.
     * <p>Any elements preceding a font-size[/line-height]
     * must be from the specification
     * [font-style||font-variant||font-weight]?
     *
     * @param foNode the <tt>FONode</tt> with which this property is
     * associated.
     * @param list a <tt>PropertyValueList</tt> containing the actual
     * space-separated list; i.e. the single inner list from the
     * outer list returned by the parser. or removed from the front of
     * a comma-separated list.
     * @param fontList a <tt>PropertyValueList</tt> containing any
     * font-family elements which followed the first comma of the font
     * expression.  This list may be empty (but is not null) in the case
     * where there was only one font-family element specified.
     * @return <tt>PropertyValueList</tt> containing a
     * <tt>PropertyValue</tt> for each property in the expansion of the
     * shorthand.
     * @exception PropertyException
     */
    private static PropertyValueList
            processSpaceSepList(FONode foNode,
                                PropertyValueList list,
                                PropertyValueList fontList)
                    throws PropertyException
    {
        PropertyValueList newlist = null;

        // copy the list into an array for random access
        Object[] props = list.toArray();
        PropertyValue[] propvals = new PropertyValue[props.length];
        int slash = -1;
        int firstcomma = -1;
        int familyStart = -1;
        int fontsize = -1;

        PropertyValue style = null;
        PropertyValue variant = null;
        PropertyValue weight = null;
        PropertyValue size = null;
        PropertyValue height = null;
        PropertyValue fontset = null;

        for (int i = 0; i < props.length; i++) {
            propvals[i] = (PropertyValue)(props[i]);
            int type = propvals[i].getType();
            if (type == PropertyValue.SLASH)
                slash = i;
            else if (type == PropertyValue.LIST
                        && firstcomma == -1)
                firstcomma = i;
        }
        if (slash != -1 && slash >= (propvals.length -2))
            throw new PropertyException
                                ("Invalid slash position in font list");
        if (slash != -1) {
            // know where slash and line-height are
            // font-family begins at slash + 2
            familyStart = slash + 2;
            fontsize = slash - 1;
            size = PropertyConsts.pconsts.refineParsing
                (PropNames.FONT_SIZE, foNode, propvals[fontsize], IS_NESTED);
            // derive the line-height
            // line-height is at slash + 1
            height = PropertyConsts.pconsts.refineParsing
                        (PropNames.LINE_HEIGHT,
                                    foNode, propvals[slash + 1], IS_NESTED);
        } else {
            // Don''t know where slash is.  If anything precedes the
            // font-family, it must be a font-size.  Look for that.
            if (firstcomma == -1) firstcomma = propvals.length - 1;
            for (fontsize = firstcomma - 1; fontsize >= 0; fontsize--) {
                if (propvals[fontsize].getType() == PropertyValue.NCNAME) {
                    // try for a font-size enumeration
                    String name = ((NCName)propvals[fontsize]).getNCName();
                    try {
                        size = new MappedNumeric
                                        (foNode, PropNames.FONT_SIZE, name);
                    } catch (PropertyException e) {
                        // Attempt to derive mapped numeric failed
                        continue;
                    }
                    // Presumably we have a mapped numeric
                    break;
                }
                if (propvals[fontsize].getType() == PropertyValue.NUMERIC)
                {
                    // Length (incl. Ems) or Percentage allowed
                    if (((Numeric)(propvals[fontsize])).isDistance()) {
                        size = propvals[fontsize];
                        break;
                    }
                    // else don't know what this Numeric is doing here -
                    // spit the dummy
                    throw new PropertyException("Non-distance Numeric"
                        + " found in 'font' expression while looking "
                        + "for font-size");
                }
                // Not an NCName, not a Numeric.  What the ... ?
                throw new PropertyException
                    (propvals[fontsize].getClass().getName() +
                    " found in 'font' "
                    + "expression while looking for font-size");
            }
            // Indicate start of font-family.
            if (size != null) familyStart = fontsize + 1;
            else  // no font-size found
                // A font-size[/line-height] element is compulsory
                throw new PropertyException
                    ("Required 'font-size' element not found in 'font'"
                        + " shorthand");
        }
        // Attempt to derive the FontFamilySet
        // The discovered font or font-family name must be prepended to
        // the fontList.  If the font name is a single element, and the
        // fontList is empty, only that single element is passed to the
        // refineParsing() method of FontFamily.  Otherwise the font
        // name element or elements is formed into a PropertyValueList,
        // and that list is prepended to fontList.
        if (fontList.size() == 0
                            && familyStart == (propvals.length - 1)) {
            fontset = PropertyConsts.pconsts.refineParsing
                            (PropNames.FONT_FAMILY, foNode,
                                            propvals[familyStart], IS_NESTED);
        } else {
            // Must develop a list to prepend to fontList
            PropertyValueList tmpList =
                            new PropertyValueList(PropNames.FONT_FAMILY);
            for (int i = familyStart; i < propvals.length; i++)
                tmpList.add(propvals[i]);
            fontList.addFirst(tmpList);
            // Get a FontFamilySet
            fontset = PropertyConsts.pconsts.refineParsing
                                (PropNames.FONT_FAMILY, foNode,
                                                        fontList, IS_NESTED);
        }
        // Only font-style font-variant and font-weight, in any order
        // remain as possibilities at the front of the expression
        for (int i = 0; i < fontsize; i++) {
            PropertyValue pv = null;
            try {
                pv = PropertyConsts.pconsts.refineParsing
                                (PropNames.FONT_STYLE, foNode,
                                                    propvals[i], IS_NESTED);
                if (style != null)
                    logger.info("font: duplicate" +
                    "style overrides previous style");
                style = pv;
                continue;
            } catch(PropertyException e) {}

            try {
                pv = PropertyConsts.pconsts.refineParsing
                                (PropNames.FONT_VARIANT, foNode,
                                                    propvals[i], IS_NESTED);
                if (variant != null)
                    logger.info("font: duplicate" +
                    "variant overrides previous variant");
                variant = pv;
                continue;
            } catch(PropertyException e) {}

            try {
                pv = PropertyConsts.pconsts.refineParsing
                                (PropNames.FONT_WEIGHT, foNode,
                                                    propvals[i], IS_NESTED);
                if (weight != null)
                    logger.info("font: duplicate" +
                    "weight overrides previous weight");
                weight = pv;
                continue;
            } catch(PropertyException e) {}
            throw new PropertyException
                ("Unrecognized value; looking for style, "
                + "variant or color in font: "
                + propvals[i].getClass().getName());
        }
        // Construct the shorthand expansion list from the discovered
        // values of individual components

        newlist = new PropertyValueList(PropNames.FONT);
        // Add each discovered property to the list.
        // N.B. These properties should be added in the order given
        // in the ShorthandPropSets.fontExpansion ROIntArray.
        for (int i = 0; i < ShorthandPropSets.fontExpansion.length; i++) {
            switch (ShorthandPropSets.fontExpansion.get(i)) {
            case PropNames.FONT_STYLE:
                if (style != null) newlist.add(style);
                break;
            case PropNames.FONT_VARIANT:
                if (variant != null) newlist.add(variant);
                break;
            case PropNames.FONT_WEIGHT:
                if (weight != null) newlist.add(weight);
                break;
            case PropNames.FONT_SIZE:
                if (size != null) newlist.add(size);
                break;
            case PropNames.FONT_FAMILY:
                if (fontset != null) newlist.add(fontset);
                break;
            case PropNames.LINE_HEIGHT:
                if (height != null)
                    newlist.addAll
                        (ShorthandPropSets.expandCompoundProperty
                                                (foNode.getFOTree(), height));
                break;
            }
        }

        return newlist;
    }
}

