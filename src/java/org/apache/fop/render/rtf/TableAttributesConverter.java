/*
 * $Id$
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 *
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 *
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */
package org.apache.fop.render.rtf;

//RTF
import org.apache.fop.render.rtf.rtflib.rtfdoc.BorderAttributesConverter;

//FOP
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.EnumProperty;
import org.apache.fop.fo.expr.NCnameProperty;
import org.apache.fop.fo.properties.Constants;
import org.apache.fop.fo.LengthProperty;
import org.apache.fop.fo.ListProperty;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.Property;
import org.apache.fop.fo.ColorTypeProperty;
import org.apache.fop.fo.NumberProperty;
import org.apache.fop.datatypes.ColorType;

import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfAttributes;
import org.apache.fop.render.rtf.rtflib.rtfdoc.ITableAttributes;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfColorTable;

/**
 * Contributor(s):
 *  @author Roberto Marra <roberto@link-u.com>
 *  @author Boris Poudérous <boris.pouderous@eads-telecom.com>
 *  @author Normand Massé
 *  @author Peter Herweg <pherweg@web.de>
 *
 * This class was originally developed for the JFOR project and
 * is now integrated into FOP.
-----------------------------------------------------------------------------*/

/**
 * Provides methods to convert the attributes to RtfAttributes.
 */

public class TableAttributesConverter {

    private static Logger log = new ConsoleLogger();

    //////////////////////////////////////////////////
    // @@ Construction
    //////////////////////////////////////////////////

    /**
     * Constructor.
     */
    private TableAttributesConverter() {
    }

    //////////////////////////////////////////////////
    // @@ Static converter methods
    //////////////////////////////////////////////////
    /**
     * Converts table-only attributes to rtf attributes.
     * 
     * @param attrs Given attributes
     * @param defaultAttributes Default rtf attributes
     *
     * @return All valid rtf attributes together
     *
     * @throws ConverterException On convertion error
     */
    static RtfAttributes convertTableAttributes(PropertyList propertyList)
            throws FOPException {
        RtfAttributes attrib = new RtfAttributes();

        LengthProperty lengthProp = null;
        // margin-left
        lengthProp = (LengthProperty)propertyList.get("margin-left");
        if (lengthProp != null) {
            Float f = new Float(lengthProp.getLength().getValue() / 1000f);
            final String sValue = f.toString() + "pt";

            attrib.set(
                    ITableAttributes.ATTR_ROW_LEFT_INDENT,
                    (int)FoUnitsConverter.getInstance().convertToTwips(sValue));
        }

        return attrib;
    }

    /**
     * Converts cell attributes to rtf attributes.
     * @param attrs Given attributes
     * @param defaultAttributes Default rtf attributes
     *
     * @return All valid rtf attributes together
     *
     * @throws ConverterException On convertion error
     */
    static RtfAttributes convertCellAttributes(PropertyList props)
    throws FOPException {

        Property p;
        EnumProperty ep;
        RtfColorTable colorTable = RtfColorTable.getInstance();

        RtfAttributes attrib = null;

            attrib = new RtfAttributes();

        boolean isBorderPresent = false;

        // Cell background color
        if ((p = props.getNearestSpecified("background-color")) != null) {
            ColorType color = p.getColorType();
            if (color != null) {
                if (color.getAlpha() != 0
                        || color.getRed() != 0
                        || color.getGreen() != 0
                        || color.getBlue() != 0) {
                    attrib.set(
                        ITableAttributes.CELL_COLOR_BACKGROUND,
                        TextAttributesConverter.convertFOPColorToRTF(color));
                }
            } else {
                log.warn("Named color '" + p.toString() + "' not found. ");
            }

        }

        // Cell borders :
        if ((p = props.getExplicit("border-color")) != null) {
            ListProperty listprop = (ListProperty)p;
            ColorType color = null;
            if (listprop.getList().get(0) instanceof NCnameProperty) {
                color = new ColorType(((NCnameProperty)listprop.getList().get(0)).getNCname());
            } else if (listprop.getList().get(0) instanceof ColorTypeProperty) {
                color = ((ColorTypeProperty)listprop.getList().get(0)).getColorType();
            }

            attrib.set(
                BorderAttributesConverter.BORDER_COLOR,
                colorTable.getColorNumber((int)color.getRed(), (int)color.getGreen(),
                        (int)color.getBlue()).intValue());
        }
        if ((p = props.getExplicit("border-top-color")) != null) {
            ColorType color = p.getColorType();
            attrib.set(
                BorderAttributesConverter.BORDER_COLOR,
                colorTable.getColorNumber((int)color.getRed(), (int)color.getGreen(),
                        (int)color.getBlue()).intValue());
        }
        if ((p = props.getExplicit("border-bottom-color")) != null) {
            ColorType color = p.getColorType();
            attrib.set(
                BorderAttributesConverter.BORDER_COLOR,
                colorTable.getColorNumber((int)color.getRed(), (int)color.getGreen(),
                        (int)color.getBlue()).intValue());
        }
        if ((p = props.getExplicit("border-left-color")) != null) {
            ColorType color = p.getColorType();
            attrib.set(
                BorderAttributesConverter.BORDER_COLOR,
                colorTable.getColorNumber((int)color.getRed(), (int)color.getGreen(),
                        (int)color.getBlue()).intValue());
        }
        if ((p = props.getExplicit("border-right-color")) != null) {
            ColorType color = p.getColorType();
            attrib.set(
                BorderAttributesConverter.BORDER_COLOR,
                colorTable.getColorNumber((int)color.getRed(), (int)color.getGreen(),
                        (int)color.getBlue()).intValue());
        }

        // Border styles do not inherit from parent
        if ((p = props.get("border-style")) != null) {
            log.warn("border-style not implemented. Please use border-style-left, "
                     + "...-right, ...-top or ...-bottom");
            /*
            attrib.set(ITableAttributes.CELL_BORDER_LEFT,  "\\"+convertAttributetoRtf(e.getEnum()));
            attrib.set(ITableAttributes.CELL_BORDER_RIGHT, "\\"+convertAttributetoRtf(e.getEnum()));
            attrib.set(ITableAttributes.CELL_BORDER_BOTTOM,"\\"+convertAttributetoRtf(e.getEnum()));
            attrib.set(ITableAttributes.CELL_BORDER_TOP,   "\\"+convertAttributetoRtf(e.getEnum()));
            isBorderPresent=true;
            */
        }
        ep = (EnumProperty)props.get("border-top-style");
        if (ep != null && ep.getEnum() != Constants.NONE) {
            attrib.set(ITableAttributes.CELL_BORDER_TOP,   "\\"
                       + convertAttributetoRtf(ep.getEnum()));
            isBorderPresent = true;
        }
        ep = (EnumProperty)props.get("border-bottom-style");
        if (ep != null && ep.getEnum() != Constants.NONE) {
            attrib.set(ITableAttributes.CELL_BORDER_BOTTOM, "\\"
                       + convertAttributetoRtf(ep.getEnum()));
            isBorderPresent = true;
        }
        ep = (EnumProperty)props.get("border-left-style");
        if (ep != null && ep.getEnum() != Constants.NONE) {
            attrib.set(ITableAttributes.CELL_BORDER_LEFT,  "\\"
                       + convertAttributetoRtf(ep.getEnum()));
            isBorderPresent = true;
        }
        ep = (EnumProperty)props.get("border-right-style");
        if (ep != null && ep.getEnum() != Constants.NONE) {
            attrib.set(ITableAttributes.CELL_BORDER_RIGHT, "\\"
                       + convertAttributetoRtf(ep.getEnum()));
            isBorderPresent = true;
        }

        if ((p = props.get("border-width")) != null) {
            ListProperty listprop = (ListProperty)p;
            LengthProperty lengthprop = (LengthProperty)listprop.getList().get(0);

            Float f = new Float(lengthprop.getLength().getValue() / 1000f);
            String sValue = f.toString() + "pt";

            attrib.set(BorderAttributesConverter.BORDER_WIDTH,
                       (int)FoUnitsConverter.getInstance().convertToTwips(sValue));
        } else if (isBorderPresent) {
            //if not defined, set default border width
            //note 20 twips = 1 point
            attrib.set(BorderAttributesConverter.BORDER_WIDTH,
                       (int)FoUnitsConverter.getInstance().convertToTwips("1pt"));
        }


        // Column spanning :
        NumberProperty n = (NumberProperty)props.get("number-columns-spanned");
        if (n != null && n.getNumber().intValue() > 1) {
            attrib.set(ITableAttributes.COLUMN_SPAN, n.getNumber().intValue());
        }

        return attrib;
    }


    /**
     * Converts table and row attributes to rtf attributes.
     *
     * @param attrs Given attributes
     * @param defaultAttributes Default rtf attributes
     *
     * @return All valid rtf attributes together
     *
     * @throws ConverterException On convertion error
     */
    static RtfAttributes convertRowAttributes(PropertyList props,
            RtfAttributes rtfatts)
    throws FOPException {

        Property p;
        EnumProperty ep;
        RtfColorTable colorTable = RtfColorTable.getInstance();

        RtfAttributes attrib = null;

            if (rtfatts == null) {
                attrib = new RtfAttributes();
            } else {
                attrib = rtfatts;
        }

        String attrValue;
        boolean isBorderPresent = false;
        //need to set a default width

        //check for keep-together row attribute
        if ((p = props.get("keep-together.within-page")) != null) {
            attrib.set(ITableAttributes.ROW_KEEP_TOGETHER);
        }

        if ((p = props.get("keep-together")) != null) {
            attrib.set(ITableAttributes.ROW_KEEP_TOGETHER);
        }

        //Check for keep-with-next row attribute.
        if ((p = props.get("keep-together")) != null) {
            attrib.set(ITableAttributes.ROW_KEEP_WITH_NEXT);
        }

        //Check for keep-with-previous row attribute.
        if ((p = props.get("keep-with-previous")) != null) {
            attrib.set(ITableAttributes.ROW_KEEP_WITH_PREVIOUS);
        }

        //Check for height row attribute.
        if ((p = props.get("height")) != null) {
            Float f = new Float(p.getLength().getValue() / 1000);
            attrValue = f.toString() + "pt";
            attrib.set(ITableAttributes.ROW_HEIGHT,
                       (int)FoUnitsConverter.getInstance().convertToTwips(attrValue));
        }

        /* to write a border to a side of a cell one must write the directional
         * side (ie. left, right) and the inside value if one needs to be taken
         * out ie if the cell lies on the edge of a table or not, the offending
         * value will be taken out by RtfTableRow.  This is because you can't
         * say BORDER_TOP and BORDER_HORIZONTAL if the cell lies at the top of
         * the table.  Similarly using BORDER_BOTTOM and BORDER_HORIZONTAL will
         * not work if the cell lies at th bottom of the table.  The same rules
         * apply for left right and vertical.

         * Also, the border type must be written after every control word.  Thus
         * it is implemented that the border type is the value of the border
         * place.
         */
        if ((p = props.get("border-style")) != null) {
            log.warn("border-style not implemented. Please use border-style-left, "
                     + "...-right, ...-top or ...-bottom");
/*
            attrValue = new String(AbstractBuilder.getValue( attrs, "border-style", defAttrs ));
            attrib.set(ITableAttributes.ROW_BORDER_LEFT,"\\"
                       + BorderAttributesConverter.convertAttributetoRtf(attrValue));
            attrib.set(ITableAttributes.ROW_BORDER_RIGHT,"\\"
                       + BorderAttributesConverter.convertAttributetoRtf(attrValue));
            attrib.set(ITableAttributes.ROW_BORDER_HORIZONTAL,"\\"
                       + BorderAttributesConverter.convertAttributetoRtf(attrValue));
            attrib.set(ITableAttributes.ROW_BORDER_VERTICAL,"\\"
                       + BorderAttributesConverter.convertAttributetoRtf(attrValue));
            attrib.set(ITableAttributes.ROW_BORDER_BOTTOM,"\\"
                       + BorderAttributesConverter.convertAttributetoRtf(attrValue));
            attrib.set(ITableAttributes.ROW_BORDER_TOP,"\\"
                       + BorderAttributesConverter.convertAttributetoRtf(attrValue));
            isBorderPresent=true;
*/
        }
        ep = (EnumProperty)props.get("border-top-style");
        if (ep != null && ep.getEnum() != Constants.NONE) {
            attrib.set(ITableAttributes.ROW_BORDER_TOP,       "\\"
                       + convertAttributetoRtf(ep.getEnum()));
            attrib.set(ITableAttributes.ROW_BORDER_HORIZONTAL, "\\"
                       + convertAttributetoRtf(ep.getEnum()));
            isBorderPresent = true;
        }
        ep = (EnumProperty)props.get("border-bottom-style");
        if (ep != null && ep.getEnum() != Constants.NONE) {
            attrib.set(ITableAttributes.ROW_BORDER_BOTTOM,    "\\"
                       + convertAttributetoRtf(ep.getEnum()));
            attrib.set(ITableAttributes.ROW_BORDER_HORIZONTAL, "\\"
                       + convertAttributetoRtf(ep.getEnum()));
            isBorderPresent = true;
        }
        ep = (EnumProperty)props.get("border-left-style");
        if (ep != null && ep.getEnum() != Constants.NONE) {
            attrib.set(ITableAttributes.ROW_BORDER_LEFT,     "\\"
                       + convertAttributetoRtf(ep.getEnum()));
            attrib.set(ITableAttributes.ROW_BORDER_VERTICAL, "\\"
                       + convertAttributetoRtf(ep.getEnum()));
            isBorderPresent = true;
        }
        ep = (EnumProperty)props.get("border-right-style");
        if (ep != null && ep.getEnum() != Constants.NONE) {
            attrib.set(ITableAttributes.ROW_BORDER_RIGHT,    "\\"
                       + convertAttributetoRtf(ep.getEnum()));
            attrib.set(ITableAttributes.ROW_BORDER_VERTICAL, "\\"
                       + convertAttributetoRtf(ep.getEnum()));
            isBorderPresent = true;
        }
        ep = (EnumProperty)props.get("border-horizontal-style");
        if (ep != null && ep.getEnum() != Constants.NONE) {
            attrib.set(ITableAttributes.ROW_BORDER_HORIZONTAL, "\\"
                       + convertAttributetoRtf(ep.getEnum()));
            attrib.set(ITableAttributes.ROW_BORDER_TOP,        "\\"
                       + convertAttributetoRtf(ep.getEnum()));
            attrib.set(ITableAttributes.ROW_BORDER_BOTTOM,     "\\"
                       + convertAttributetoRtf(ep.getEnum()));
            isBorderPresent = true;
        }
        ep = (EnumProperty)props.get("border-vertical-style");
        if (ep != null && ep.getEnum() != Constants.NONE) {
            attrib.set(ITableAttributes.ROW_BORDER_VERTICAL,  "\\"
                       + convertAttributetoRtf(ep.getEnum()));
            attrib.set(ITableAttributes.ROW_BORDER_LEFT,      "\\"
                       + convertAttributetoRtf(ep.getEnum()));
            attrib.set(ITableAttributes.ROW_BORDER_RIGHT,     "\\"
                       + convertAttributetoRtf(ep.getEnum()));
            isBorderPresent = true;
        }

        if ((p = props.get("border-width")) != null) {
            ListProperty listprop = (ListProperty)p;
            LengthProperty lengthprop = (LengthProperty)listprop.getList().get(0);

            Float f = new Float(lengthprop.getLength().getValue() / 1000f);
            String sValue = f.toString() + "pt";

            attrib.set(BorderAttributesConverter.BORDER_WIDTH,
                       (int)FoUnitsConverter.getInstance().convertToTwips(sValue));
        } else if (isBorderPresent) {
            //if not defined, set default border width
            //note 20 twips = 1 point
            attrib.set(BorderAttributesConverter.BORDER_WIDTH,
                       (int)FoUnitsConverter.getInstance().convertToTwips("1pt"));
        }

        return attrib;
    }


    /**
     *
     * @param iBorderStyle the border style to be converted
     * @return String with the converted border style
     */
    public static String convertAttributetoRtf(int iBorderStyle) {
        // Added by Normand Masse
        // "solid" is interpreted like "thin"
        if (iBorderStyle == Constants.SOLID) {
            return BorderAttributesConverter.BORDER_SINGLE_THICKNESS;
/*        } else if (iBorderStyle==Constants.THIN) {
                        return BorderAttributesConverter.BORDER_SINGLE_THICKNESS;
        } else if (iBorderStyle==Constants.THICK) {
            return BorderAttributesConverter.BORDER_DOUBLE_THICKNESS;
        } else if (iBorderStyle==Constants. value.equals("shadowed")) {
            return BorderAttributesConverter.BORDER_SHADOWED;*/
        } else if (iBorderStyle == Constants.DOUBLE) {
            return BorderAttributesConverter.BORDER_DOUBLE;
        } else if (iBorderStyle == Constants.DOTTED) {
            return BorderAttributesConverter.BORDER_DOTTED;
        } else if (iBorderStyle == Constants.DASHED) {
            return BorderAttributesConverter.BORDER_DASH;
/*        } else if (iBorderStyle==Constants value.equals("hairline")) {
            return BorderAttributesConverter.BORDER_HAIRLINE;*/
/*        } else if (iBorderStyle==Constant value.equals("dot-dash")) {
            return BorderAttributesConverter.BORDER_DOT_DASH;
        } else if (iBorderStyle==Constant value.equals("dot-dot-dash")) {
            return BorderAttributesConverter.BORDER_DOT_DOT_DASH;
        } else if (iBorderStyle==Constant value.equals("triple")) {
            return BorderAttributesConverter.BORDER_TRIPLE;
        } else if (iBorderStyle==Constant value.equals("wavy")) {
            return BorderAttributesConverter.BORDER_WAVY;
        } else if (iBorderStyle==Constant value.equals("wavy-double")) {
            return BorderAttributesConverter.BORDER_WAVY_DOUBLE;
        } else if (iBorderStyle==Constant value.equals("striped")) {
            return BorderAttributesConverter.BORDER_STRIPED;
        } else if (iBorderStyle==Constant value.equals("emboss")) {
            return BorderAttributesConverter.BORDER_EMBOSS;
        } else if (iBorderStyle==Constant value.equals("engrave")) {
            return BorderAttributesConverter.BORDER_ENGRAVE;*/
        } else {
            return null;
        }
    }

}
