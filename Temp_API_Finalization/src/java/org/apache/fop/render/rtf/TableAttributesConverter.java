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

package org.apache.fop.render.rtf;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.SimpleLog;
import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.flow.Table;
import org.apache.fop.fo.flow.TableBody;
import org.apache.fop.fo.flow.TableCell;
import org.apache.fop.fo.flow.TableHeader;
import org.apache.fop.fo.flow.TableRow;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.render.rtf.BorderAttributesConverter;
import org.apache.fop.render.rtf.rtflib.rtfdoc.ITableAttributes;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfAttributes;

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

    private static Log log = new SimpleLog("FOP/RTF");

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
    static RtfAttributes convertTableAttributes(Table fobj)
            throws FOPException {
        FOPRtfAttributes attrib = new FOPRtfAttributes();
        attrib.setTwips(ITableAttributes.ATTR_ROW_LEFT_INDENT, 
                fobj.getCommonMarginBlock().marginLeft);
        return attrib;
    }

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
    static RtfAttributes convertTableBodyAttributes(TableBody fobj)
            throws FOPException {
        FOPRtfAttributes attrib = new FOPRtfAttributes();
        return attrib;
    }

    /**
     * Converts cell attributes to rtf attributes.
     * @param fobj FObj whose properties are to be converted
     *
     * @return All valid rtf attributes together
     *
     * @throws ConverterException On conversion error
     */
    static RtfAttributes convertCellAttributes(TableCell fobj)
    throws FOPException {

        //Property p;
        //RtfColorTable colorTable = RtfColorTable.getInstance();
        
        FOPRtfAttributes attrib = new FOPRtfAttributes();

        //boolean isBorderPresent = false;
        CommonBorderPaddingBackground border = fobj.getCommonBorderPaddingBackground();

        // Cell background color
        ColorType color = border.backgroundColor;
        if (color == null) {
            //If there is no background-color specified for the cell,
            //then try to read it from table-row or table-header.
            CommonBorderPaddingBackground brd = null;
            
            if (fobj.getParent() instanceof TableRow) {
                TableRow parentRow = (TableRow)fobj.getParent();
                brd = parentRow.getCommonBorderPaddingBackground();
                color = brd.backgroundColor;
            } else if (fobj.getParent() instanceof TableHeader) {
                TableHeader parentHeader = (TableHeader)fobj.getParent();
                brd = parentHeader.getCommonBorderPaddingBackground();
                color = brd.backgroundColor;
            }
            
            if (color == null
                    && fobj.getParent() != null 
                    && fobj.getParent().getParent() != null 
                    && fobj.getParent().getParent().getParent() instanceof Table) {

                Table table = (Table)fobj.getParent().getParent().getParent();
                brd = table.getCommonBorderPaddingBackground();
                color = brd.backgroundColor;
            }
            
            
        }
        if ((color != null) 
                && (color.getAlpha() != 0
                        || color.getRed() != 0
                        || color.getGreen() != 0
                        || color.getBlue() != 0)) {
            attrib.set(ITableAttributes.CELL_COLOR_BACKGROUND, color);
        }

        BorderAttributesConverter.makeBorder(border, CommonBorderPaddingBackground.BEFORE,
                attrib, ITableAttributes.CELL_BORDER_TOP);
        BorderAttributesConverter.makeBorder(border, CommonBorderPaddingBackground.AFTER,
                attrib, ITableAttributes.CELL_BORDER_BOTTOM);
        BorderAttributesConverter.makeBorder(border, CommonBorderPaddingBackground.START,
                attrib, ITableAttributes.CELL_BORDER_LEFT);
        BorderAttributesConverter.makeBorder(border, CommonBorderPaddingBackground.END,
                attrib,  ITableAttributes.CELL_BORDER_RIGHT);

        int padding;
        boolean reproduceMSWordBug = true;
        //TODO Make this configurable
        if (reproduceMSWordBug) {
            //MS Word has a bug where padding left and top are exchanged
            padding = border.getPaddingStart(false, null); // TODO do we need a real context here?
            if (padding != 0) {
                attrib.setTwips(ITableAttributes.ATTR_CELL_PADDING_TOP, padding);
                attrib.set(ITableAttributes.ATTR_CELL_U_PADDING_TOP, 3 /*=twips*/);
            }
            padding = border.getPaddingBefore(false, null); // TODO do we need a real context here?
            if (padding != 0) {
                attrib.setTwips(ITableAttributes.ATTR_CELL_PADDING_LEFT, padding);
                attrib.set(ITableAttributes.ATTR_CELL_U_PADDING_LEFT, 3 /*=twips*/);
            }
        } else {
            padding = border.getPaddingStart(false, null); // TODO do we need a real context here?
            if (padding != 0) {
                attrib.setTwips(ITableAttributes.ATTR_CELL_PADDING_LEFT, padding);
                attrib.set(ITableAttributes.ATTR_CELL_U_PADDING_LEFT, 3 /*=twips*/);
            }
            padding = border.getPaddingBefore(false, null); // TODO do we need a real context here?
            if (padding != 0) {
                attrib.setTwips(ITableAttributes.ATTR_CELL_PADDING_TOP, padding);
                attrib.set(ITableAttributes.ATTR_CELL_U_PADDING_TOP, 3 /*=twips*/);
            }
        }
        padding = border.getPaddingEnd(false, null);  // TODO do we need a real context here?
        if (padding != 0) {
            attrib.setTwips(ITableAttributes.ATTR_CELL_PADDING_RIGHT, padding);
            attrib.set(ITableAttributes.ATTR_CELL_U_PADDING_RIGHT, 3 /*=twips*/);
        }
        padding = border.getPaddingAfter(false, null); // TODO do we need a real context here?
        if (padding != 0) {
            attrib.setTwips(ITableAttributes.ATTR_CELL_PADDING_BOTTOM, padding);
            attrib.set(ITableAttributes.ATTR_CELL_U_PADDING_BOTTOM, 3 /*=twips*/);
        }
        
        int n = fobj.getNumberColumnsSpanned();
        // Column spanning :
        if (n > 1) {
            attrib.set(ITableAttributes.COLUMN_SPAN, n);
        }

        return attrib;
    }


    /**
     * Converts table and row attributes to rtf attributes.
     *
     * @param fobj FObj to be converted
     * @param defaultAttributes Default rtf attributes
     *
     * @return All valid rtf attributes together
     * @throws ConverterException On converion error
     */
    static RtfAttributes convertRowAttributes(TableRow fobj,
            RtfAttributes rtfatts)
    throws FOPException {

        //Property p;
        //RtfColorTable colorTable = RtfColorTable.getInstance();

        RtfAttributes attrib = null;

        if (rtfatts == null) {
            attrib = new RtfAttributes();
        } else {
            attrib = rtfatts;
        }

        //String attrValue;
        //boolean isBorderPresent = false;
        //need to set a default width

        //check for keep-together row attribute

        if (fobj.getKeepTogether().getWithinPage().getEnum() == Constants.EN_ALWAYS) {
            attrib.set(ITableAttributes.ROW_KEEP_TOGETHER);
        }

        //Check for keep-with-next row attribute.
        if (fobj.getKeepWithNext().getWithinPage().getEnum() == Constants.EN_ALWAYS) {
            attrib.set(ITableAttributes.ROW_KEEP_WITH_NEXT);
        }

        //Check for keep-with-previous row attribute.
        if (fobj.getKeepWithPrevious().getWithinPage().getEnum() == Constants.EN_ALWAYS) {
            attrib.set(ITableAttributes.ROW_KEEP_WITH_PREVIOUS);
        }

        //Check for height row attribute.
        if (fobj.getHeight().getEnum() != Constants.EN_AUTO) {
            attrib.set(ITableAttributes.ROW_HEIGHT, fobj.getHeight().getValue() / (1000 / 20));
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
        CommonBorderPaddingBackground border = fobj.getCommonBorderPaddingBackground();
        BorderAttributesConverter.makeBorder(border, CommonBorderPaddingBackground.BEFORE,
                attrib, ITableAttributes.CELL_BORDER_TOP);
        BorderAttributesConverter.makeBorder(border, CommonBorderPaddingBackground.AFTER,
                attrib, ITableAttributes.CELL_BORDER_BOTTOM);
        BorderAttributesConverter.makeBorder(border, CommonBorderPaddingBackground.START,
                attrib, ITableAttributes.CELL_BORDER_LEFT);
        BorderAttributesConverter.makeBorder(border, CommonBorderPaddingBackground.END,
                attrib, ITableAttributes.CELL_BORDER_RIGHT);

/*
        ep = (EnumProperty)fobj.getProperty(Constants.PR_BORDER_TOP_STYLE);
        if (ep != null && ep.getEnum() != Constants.EN_NONE) {
            attrib.set(ITableAttributes.ROW_BORDER_TOP,       "\\"
                       + convertAttributetoRtf(ep.getEnum()));
            attrib.set(ITableAttributes.ROW_BORDER_HORIZONTAL, "\\"
                       + convertAttributetoRtf(ep.getEnum()));
            isBorderPresent = true;
        }
        ep = (EnumProperty)fobj.getProperty(Constants.PR_BORDER_BOTTOM_STYLE);
        if (ep != null && ep.getEnum() != Constants.EN_NONE) {
            attrib.set(ITableAttributes.ROW_BORDER_BOTTOM,    "\\"
                       + convertAttributetoRtf(ep.getEnum()));
            attrib.set(ITableAttributes.ROW_BORDER_HORIZONTAL, "\\"
                       + convertAttributetoRtf(ep.getEnum()));
            isBorderPresent = true;
        }
        ep = (EnumProperty)fobj.getProperty(Constants.PR_BORDER_LEFT_STYLE);
        if (ep != null && ep.getEnum() != Constants.EN_NONE) {
            attrib.set(ITableAttributes.ROW_BORDER_LEFT,     "\\"
                       + convertAttributetoRtf(ep.getEnum()));
            attrib.set(ITableAttributes.ROW_BORDER_VERTICAL, "\\"
                       + convertAttributetoRtf(ep.getEnum()));
            isBorderPresent = true;
        }
        ep = (EnumProperty)fobj.getProperty(Constants.PR_BORDER_RIGHT_STYLE);
        if (ep != null && ep.getEnum() != Constants.EN_NONE) {
            attrib.set(ITableAttributes.ROW_BORDER_RIGHT,    "\\"
                       + convertAttributetoRtf(ep.getEnum()));
            attrib.set(ITableAttributes.ROW_BORDER_VERTICAL, "\\"
                       + convertAttributetoRtf(ep.getEnum()));
            isBorderPresent = true;
        }

        //Currently there is only one border width supported in each cell.  
        p = fobj.getProperty(Constants.PR_BORDER_LEFT_WIDTH);
        if(p == null) {
            p = fobj.getProperty(Constants.PR_BORDER_RIGHT_WIDTH);
        }
        if(p == null) {
            p = fobj.getProperty(Constants.PR_BORDER_TOP_WIDTH);
        }
        if(p == null) {
            p = fobj.getProperty(Constants.PR_BORDER_BOTTOM_WIDTH);
        }
        if (p != null) {
            LengthProperty lengthprop = (LengthProperty)p;

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
*/
        return attrib;
    }

}
