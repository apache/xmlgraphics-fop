/*
 * Copyright 1999-2004 The Apache Software Foundation.
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

//FOP
import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.SimpleLog;
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.properties.ColorTypeProperty;
import org.apache.fop.fo.properties.EnumProperty;
import org.apache.fop.fo.properties.LengthProperty;
import org.apache.fop.fo.properties.SpaceProperty;
import org.apache.fop.datatypes.ColorType;

//RTF
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfAttributes;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfColorTable;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfFontManager;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfText;


/**  Converts FO properties to RtfAttributes
 *  @author Bertrand Delacretaz bdelacretaz@codeconsult.ch
 *  @author Andreas Putz a.putz@skynamics.com
 *  @author Boris Poud&#x00E9;rous boris.pouderous@eads-telecom.com
 *  @author Peter Herweg, pherweg@web.de
 *  @author Normand Mass&#x00E9;
 *  @author Chris Scott
 *  @author rmarra
 */

class TextAttributesConverter {
    private static Log log = new SimpleLog("FOP/RTF");

    /**
     * Converts all known text FO properties to RtfAttributes
     * @param props list of FO properites, which are to be converted
     */
    public static RtfAttributes convertAttributes(FObj fobj)
    throws FOPException {
        RtfAttributes attrib = null;

        attrib = new RtfAttributes();
        attrBlockFontFamily(fobj, attrib);
        attrBlockFontWeight(fobj, attrib);
        attrBlockFontSize(fobj, attrib);
        attrBlockFontColor(fobj, attrib);
        attrBlockFontItalic(fobj, attrib);
        attrBlockFontUnderline(fobj, attrib);
        attrBlockBackgroundColor(fobj, attrib);
        attrBlockSpaceBeforeAfter(fobj, attrib);
        attrBlockMargins(fobj, attrib);
        attrBlockTextAlign(fobj, attrib);

        return attrib;
    }

    /**
     * Converts all known text FO properties to RtfAttributes
     * @param props list of FO properites, which are to be converted
     */
    public static RtfAttributes convertBlockContainerAttributes(FObj fobj)
    throws FOPException {
        RtfAttributes attrib = new RtfAttributes();
        attrBlockFontFamily(fobj, attrib);
        attrBlockFontWeight(fobj, attrib);
        attrBlockFontSize(fobj, attrib);
        attrBlockFontColor(fobj, attrib);
        attrBlockFontItalic(fobj, attrib);
        attrBlockFontUnderline(fobj, attrib);
        attrBlockBackgroundColor(fobj, attrib);
        attrBlockSpaceBeforeAfter(fobj, attrib);
        attrBlockMargins(fobj, attrib);
        attrBlockTextAlign(fobj, attrib);
        //attrBlockDimension(fobj, attrib);

        return attrib;
    }

    /**
     * Converts all character related FO properties to RtfAttributes.
     * @param fobj FObj whose properties are to be converted
     */
    public static RtfAttributes convertCharacterAttributes(
            FObj fobj) throws FOPException {

        RtfAttributes attrib = new RtfAttributes();
        attrBlockFontFamily(fobj, attrib);
        attrBlockFontWeight(fobj, attrib);
        attrBlockFontSize(fobj, attrib);
        attrBlockFontColor(fobj, attrib);
        attrBlockFontItalic(fobj, attrib);
        attrBlockFontUnderline(fobj, attrib);
        attrBlockBackgroundColor(fobj, attrib);
        return attrib;
    }


    private static void attrBlockFontFamily(FObj fobj, RtfAttributes rtfAttr) {
        String fopValue = fobj.getProperty(Constants.PR_FONT_FAMILY).getString();

        if (fopValue != null) {
            rtfAttr.set(RtfText.ATTR_FONT_FAMILY,
                RtfFontManager.getInstance().getFontNumber(fopValue));
        }
    }

    private static void attrBlockFontSize(FObj fobj, RtfAttributes rtfAttr) {
        int fopValue = fobj.getPropLength(Constants.PR_FONT_SIZE) / 500;
        rtfAttr.set("fs", fopValue);
    }

    private static void attrBlockFontColor(FObj fobj, RtfAttributes rtfAttr) {
        // Cell background color
        ColorTypeProperty colorTypeProp = (ColorTypeProperty)fobj.getProperty(Constants.PR_COLOR);
        if (colorTypeProp != null) {
            ColorType colorType = colorTypeProp.getColorType();
            if (colorType != null) {
                if (colorType.getAlpha() != 0
                        || colorType.getRed() != 0
                        || colorType.getGreen() != 0
                        || colorType.getBlue() != 0) {
                    rtfAttr.set(
                        RtfText.ATTR_FONT_COLOR,
                        convertFOPColorToRTF(colorType));
                }
            } else {
                log.warn("Named color '" + colorTypeProp.toString() + "' not found. ");
            }
        }
    }



    private static void attrBlockFontWeight(FObj fobj, RtfAttributes rtfAttr) {
        String fopValue = fobj.getProperty(Constants.PR_FONT_WEIGHT).getString();
        if (fopValue == "bold" || fopValue == "700") {
            rtfAttr.set("b", 1);
        } else {
            rtfAttr.set("b", 0);
        }
    }

    private static void attrBlockFontItalic(FObj fobj, RtfAttributes rtfAttr) {
        String fopValue = fobj.getProperty(Constants.PR_FONT_STYLE).getString();
        if (fopValue.equals("italic")) {
            rtfAttr.set(RtfText.ATTR_ITALIC, 1);
        } else {
            rtfAttr.set(RtfText.ATTR_ITALIC, 0);
        }
    }

    private static void attrBlockFontUnderline(FObj fobj, RtfAttributes rtfAttr) {
        EnumProperty enumProp = (EnumProperty) fobj.getProperty(Constants.PR_TEXT_DECORATION);
        if (enumProp.getEnum() == Constants.UNDERLINE) {
            rtfAttr.set(RtfText.ATTR_UNDERLINE, 1);
        } else {
            rtfAttr.set(RtfText.ATTR_UNDERLINE, 0);
        }
    }

    private static void attrBlockSpaceBeforeAfter(FObj fobj, RtfAttributes rtfAttr) {
        SpaceProperty spaceProp = null;

        //space-before
        spaceProp = (SpaceProperty)fobj.getProperty(Constants.PR_SPACE_BEFORE);
        if (spaceProp != null) {
            Float f = new Float(
                spaceProp.getLengthRange().getOptimum().getLength().getValue() / 1000f);
            String sValue = f.toString() + "pt";

            try {
                rtfAttr.set(
                        RtfText.SPACE_BEFORE,
                        (int)FoUnitsConverter.getInstance().convertToTwips(sValue));
            } catch (FOPException fe) {
                log.warn("attrBlockSpaceBeforeAfter: " + fe.getMessage());
            }
        }

        //space-after
        spaceProp = (SpaceProperty)fobj.getProperty(Constants.PR_SPACE_AFTER);
        if (spaceProp != null) {
            Float f = new Float(
                spaceProp.getLengthRange().getOptimum().getLength().getValue() / 1000f);
            String sValue = f.toString() + "pt";

            try {
                rtfAttr.set(
                        RtfText.SPACE_AFTER,
                        (int)FoUnitsConverter.getInstance().convertToTwips(sValue));
            } catch (FOPException fe) {
                log.warn("attrBlockSpaceBeforeAfter: " + fe.getMessage());
            }
        }
    }

    private static void attrBlockMargins(FObj fobj, RtfAttributes rtfAttr) {
        try {
            LengthProperty lengthProp = null;

            // margin-left
            lengthProp = (LengthProperty)fobj.getProperty(Constants.PR_MARGIN_LEFT);
            if (lengthProp != null) {
                Float f = new Float(lengthProp.getLength().getValue() / 1000f);
                String sValue = f.toString() + "pt";

                rtfAttr.set(
                        RtfText.LEFT_INDENT_BODY,
                        (int)FoUnitsConverter.getInstance().convertToTwips(sValue));
            } else {
                rtfAttr.set(RtfText.LEFT_INDENT_BODY, 0);
            }

            // margin-right
            lengthProp = (LengthProperty)fobj.getProperty(Constants.PR_MARGIN_RIGHT);
            if (lengthProp != null) {
                Float f = new Float(lengthProp.getLength().getValue() / 1000f);
                String sValue = f.toString() + "pt";

                rtfAttr.set(
                        RtfText.RIGHT_INDENT_BODY,
                        (int)FoUnitsConverter.getInstance().convertToTwips(sValue));
            } else {
                rtfAttr.set(RtfText.RIGHT_INDENT_BODY, 0);
            }
        } catch (FOPException fe) {
            log.warn("attrBlockSpaceBeforeAfter: " + fe.getMessage());
        }
    }



    private static void attrBlockTextAlign(FObj fobj, RtfAttributes rtfAttr) {
        int fopValue = fobj.getPropEnum(Constants.PR_TEXT_ALIGN);
        String rtfValue = null;
        switch (fopValue) {
            case Constants.CENTER:
                rtfValue = RtfText.ALIGN_CENTER;
                break;
            case Constants.END:
                rtfValue = RtfText.ALIGN_RIGHT;
                break;
            case Constants.JUSTIFY:
                rtfValue = RtfText.ALIGN_JUSTIFIED;
                break;
            default:
                rtfValue = RtfText.ALIGN_LEFT;
                break;
        }

        rtfAttr.set(rtfValue);
    }

    /**
     * Reads background-color from bl and writes it to rtfAttr.
     *
     * @param bl the Block object the properties are read from
     * @param rtfAttr the RtfAttributes object the attributes are written to
     */
    private static void attrBlockBackgroundColor(FObj fobj, RtfAttributes rtfAttr) {
        ColorType fopValue = fobj.getProperty(Constants.PR_BACKGROUND_COLOR).getColorType();
        int rtfColor = 0;
        /* FOP uses a default background color of "transparent", which is
           actually a transparent black, which is generally not suitable as a
           default here. Changing FOP's default to "white" causes problems in
           PDF output, so we will look for the default here & change it to
           "auto". */
        if ((fopValue.getRed() == 0)
                && (fopValue.getGreen() == 0)
                && (fopValue.getBlue() == 0)
                && (fopValue.getAlpha() == 0)) {
            rtfColor = 0; //=auto
        } else {
            rtfColor = convertFOPColorToRTF(fopValue);
        }

        rtfAttr.set(RtfText.ATTR_BACKGROUND_COLOR, rtfColor);
   }

   /**
    * Converts a FOP ColorType to the integer pointing into the RTF color table
    * @param fopColor the ColorType object to be converted
    * @return integer pointing into the RTF color table
    */
   public static int convertFOPColorToRTF(ColorType fopColor) {
       int redComponent = ColorTypeProperty.convertChannelToInteger (fopColor.getRed());
       int greenComponent = ColorTypeProperty.convertChannelToInteger (fopColor.getGreen());
       int blueComponent = ColorTypeProperty.convertChannelToInteger (fopColor.getBlue());
       return RtfColorTable.getInstance().getColorNumber(redComponent,
               greenComponent, blueComponent).intValue();
   }

}
