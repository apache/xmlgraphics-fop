/*
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

//FOP
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.EnumProperty;
import org.apache.fop.fo.properties.Constants;
import org.apache.fop.fo.LengthProperty;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.SpaceProperty;
import org.apache.fop.fo.ColorTypeProperty;
import org.apache.fop.datatypes.ColorType;

//RTF
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfAttributes;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfColorTable;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfFontManager;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfText;


/**  Converts FO properties to RtfAttributes
 *  @author Bertrand Delacretaz bdelacretaz@codeconsult.ch
 *  @author Andreas Putz a.putz@skynamics.com
 *  @author Boris Poudérous boris.pouderous@eads-telecom.com
 *  @author Peter Herweg, pherweg@web.de
 *  @author Normand Massé
 *  @author Chris Scott
 *  @author rmarra
 */

class TextAttributesConverter {
    private static Logger log = new ConsoleLogger();
    
    /**
     * Converts all known text FO properties to RtfAttributes
     * @param props list of FO properites, which are to be converted
     * @param props list of default FO properites (usally null)
     */
    public static RtfAttributes convertAttributes(PropertyList props, PropertyList defProps)
    throws FOPException {
        RtfAttributes attrib = null;

        if (defProps != null) {
            attrib = convertAttributes(defProps, null);
        } else {
            attrib = new RtfAttributes();
        }
        
        attrBlockFontFamily(props, attrib);
        attrBlockFontWeight(props, attrib);
        attrBlockFontSize(props, attrib);
        attrBlockFontColor(props, attrib);
        attrBlockFontItalic(props, attrib);
        attrBlockFontUnderline(props, attrib);
        attrBlockBackgroundColor(props, attrib);
        attrBlockSpaceBeforeAfter(props, attrib);
        attrBlockMargins(props, attrib);
        attrBlockTextAlign(props, attrib);
        
        return attrib;      
    }
  
    /**
     * Converts all character related FO properties to RtfAttributes.
     * @param props list of FO properites, which are to be converted
     * @param props list of default FO properites (usally null)
     */
    public static RtfAttributes convertCharacterAttributes(
            PropertyList props, PropertyList defProps) throws FOPException {

        RtfAttributes attrib = null;

        if (defProps != null) {
            attrib = convertCharacterAttributes(defProps, null);
        } else {
            attrib = new RtfAttributes();
        }

        attrBlockFontFamily(props, attrib);
        attrBlockFontWeight(props, attrib);
        attrBlockFontSize(props, attrib);
        attrBlockFontColor(props, attrib);
        attrBlockFontItalic(props, attrib);
        attrBlockFontUnderline(props, attrib);
        attrBlockBackgroundColor(props, attrib);

        return attrib;
    }


    private static void attrBlockFontFamily(PropertyList properties, RtfAttributes rtfAttr) {
        String fopValue = properties.get("font-family").getString();
        
        if (fopValue != null) {
            rtfAttr.set(RtfText.ATTR_FONT_FAMILY, 
                RtfFontManager.getInstance().getFontNumber(fopValue));
        }
    }

    private static void attrBlockFontSize(PropertyList properties, RtfAttributes rtfAttr) {
        int fopValue = properties.get("font-size").getLength().getValue() / 500;
        rtfAttr.set("fs", fopValue);
    }
    
    private static void attrBlockFontColor(PropertyList properties, RtfAttributes rtfAttr) {
        // Cell background color
        ColorTypeProperty colorTypeProp = (ColorTypeProperty)properties.get("color");
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
    


    private static void attrBlockFontWeight(PropertyList properties, RtfAttributes rtfAttr) {
        String fopValue = properties.get("font-weight").getString();
        if (fopValue == "bold" || fopValue == "700") {
            rtfAttr.set("b", 1);
        } else {
            rtfAttr.set("b", 0);
        }
    }

    private static void attrBlockFontItalic(PropertyList properties, RtfAttributes rtfAttr) {
        String fopValue = properties.get("font-style").getString();
        if (fopValue.equals("italic")) {
            rtfAttr.set(RtfText.ATTR_ITALIC, 1);
        } else {
            rtfAttr.set(RtfText.ATTR_ITALIC, 0);
        }
    }
    
    private static void attrBlockFontUnderline(PropertyList properties, RtfAttributes rtfAttr) {
        EnumProperty enumProp = (EnumProperty)properties.get("text-decoration");
        if (enumProp.getEnum() == Constants.UNDERLINE) {
            rtfAttr.set(RtfText.ATTR_UNDERLINE, 1);
        } else {
            rtfAttr.set(RtfText.ATTR_UNDERLINE, 0);
        }
    }
     
    private static void attrBlockSpaceBeforeAfter(PropertyList properties, RtfAttributes rtfAttr) {
        SpaceProperty spaceProp = null;
        
        //space-before
        spaceProp = (SpaceProperty)properties.get("space-before");
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
        spaceProp = (SpaceProperty)properties.get("space-after");
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

    private static void attrBlockMargins(PropertyList properties, RtfAttributes rtfAttr) {
        try {
            LengthProperty lengthProp = null;
            
            // margin-left
            lengthProp = (LengthProperty)properties.get("margin-left");
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
            lengthProp = (LengthProperty)properties.get("margin-right");
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



    private static void attrBlockTextAlign(PropertyList properties, RtfAttributes rtfAttr) {
        int fopValue = properties.get("text-align").getEnum();
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
    private static void attrBlockBackgroundColor(PropertyList properties, RtfAttributes rtfAttr) {
        ColorType fopValue = properties.get("background-color").getColorType();
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
       int redComponent = ColorType.convertChannelToInteger (fopColor.getRed());
       int greenComponent = ColorType.convertChannelToInteger (fopColor.getGreen());
       int blueComponent = ColorType.convertChannelToInteger (fopColor.getBlue());
       return RtfColorTable.getInstance().getColorNumber(redComponent,
               greenComponent, blueComponent).intValue();
   }

}
