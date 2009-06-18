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

//FOP
import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.datatypes.Length;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FOText;
import org.apache.fop.fo.flow.Block;
import org.apache.fop.fo.flow.BlockContainer;
import org.apache.fop.fo.flow.Inline;
import org.apache.fop.fo.flow.PageNumber;
import org.apache.fop.fo.properties.ColorTypeProperty;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.CommonFont;
import org.apache.fop.fo.properties.CommonMarginBlock;
import org.apache.fop.fo.properties.CommonTextDecoration;
import org.apache.fop.render.rtf.BorderAttributesConverter;
import org.apache.fop.render.rtf.rtflib.rtfdoc.IBorderAttributes;
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
final class TextAttributesConverter {
    
    /**
     * Constructor is private, because it's just a utility class.
     */
    private TextAttributesConverter() {
    }
    
    /**
     * Converts all known text FO properties to RtfAttributes
     * @param props list of FO properites, which are to be converted
     */
    public static RtfAttributes convertAttributes(Block fobj)
                throws FOPException {
        FOPRtfAttributes attrib = new FOPRtfAttributes();
        attrFont(fobj.getCommonFont(), attrib);
        attrFontColor(fobj.getColor(), attrib);
        //attrTextDecoration(fobj.getTextDecoration(), attrib);
        attrBlockBackgroundColor(fobj.getCommonBorderPaddingBackground(), attrib);
        attrBlockMargin(fobj.getCommonMarginBlock(), attrib);
        attrBlockTextAlign(fobj.getTextAlign(), attrib);
        attrBorder(fobj.getCommonBorderPaddingBackground(), attrib, fobj);

        return attrib;
    }

    /**
     * Converts all known text FO properties to RtfAttributes
     * @param props list of FO properites, which are to be converted
     */
    public static RtfAttributes convertBlockContainerAttributes(BlockContainer fobj)
                throws FOPException {
        FOPRtfAttributes attrib = new FOPRtfAttributes();
        attrBackgroundColor(fobj.getCommonBorderPaddingBackground(), attrib);
        attrBlockMargin(fobj.getCommonMarginBlock(), attrib);
        //attrBlockDimension(fobj, attrib);
        attrBorder(fobj.getCommonBorderPaddingBackground(), attrib, fobj);

        return attrib;
    }

    /**
     * Converts all character related FO properties to RtfAttributes.
     * @param fobj FObj whose properties are to be converted
     */
    public static RtfAttributes convertCharacterAttributes(
            FOText fobj) throws FOPException {

        FOPRtfAttributes attrib = new FOPRtfAttributes();
        attrFont(fobj.getCommonFont(), attrib);
        attrFontColor(fobj.getColor(), attrib);
        attrTextDecoration(fobj.getTextDecoration(), attrib);
        attrBaseLineShift(fobj.getBaseLineShift(), attrib);
        return attrib;
    }
    
    /**
     * Converts all character related FO properties to RtfAttributes.
     * @param fobj FObj whose properties are to be converted
     */
    public static RtfAttributes convertCharacterAttributes(
            PageNumber fobj) throws FOPException {

        FOPRtfAttributes attrib = new FOPRtfAttributes();
        attrFont(fobj.getCommonFont(), attrib);
        attrTextDecoration(fobj.getTextDecoration(), attrib);
        attrBackgroundColor(fobj.getCommonBorderPaddingBackground(), attrib);
        return attrib;
    }

    /**
     * Converts all character related FO properties to RtfAttributes.
     * @param fobj FObj whose properties are to be converted
     */
    public static RtfAttributes convertCharacterAttributes(
            Inline fobj) throws FOPException {

        FOPRtfAttributes attrib = new FOPRtfAttributes();
        attrFont(fobj.getCommonFont(), attrib);
        attrFontColor(fobj.getColor(), attrib);

        attrBackgroundColor(fobj.getCommonBorderPaddingBackground(), attrib);
        attrInlineBorder(fobj.getCommonBorderPaddingBackground(), attrib);
        return attrib;
    }

    private static void attrFont(CommonFont font, FOPRtfAttributes rtfAttr) {
        rtfAttr.set(RtfText.ATTR_FONT_FAMILY,
                RtfFontManager.getInstance().getFontNumber(font.getFirstFontFamily()));
        rtfAttr.setHalfPoints(RtfText.ATTR_FONT_SIZE, font.fontSize);

        if (font.fontWeight == Constants.EN_700
                || font.fontWeight == Constants.EN_800
                || font.fontWeight == Constants.EN_900) {
            //Everything from 700 and above is declared as bold
            rtfAttr.set("b", 1);
        } else {
            rtfAttr.set("b", 0);
        }
        
        if (font.fontStyle == Constants.EN_ITALIC) {
            rtfAttr.set(RtfText.ATTR_ITALIC, 1);
        } else {
            rtfAttr.set(RtfText.ATTR_ITALIC, 0);
        }


    }


    private static void attrFontColor(ColorType colorType, RtfAttributes rtfAttr) {
        // Cell background color
        if (colorType != null) {
           if (colorType.getAlpha() != 0
                    || colorType.getRed() != 0
                    || colorType.getGreen() != 0
                    || colorType.getBlue() != 0) {
                rtfAttr.set(RtfText.ATTR_FONT_COLOR,
                        convertFOPColorToRTF(colorType));
            }
        }
    }



    private static void attrTextDecoration(CommonTextDecoration textDecoration, 
                RtfAttributes rtfAttr) {
        if (textDecoration == null) {
            return;
        }
                
        if (textDecoration.hasUnderline()) {
            rtfAttr.set(RtfText.ATTR_UNDERLINE, 1);
        } else {
            rtfAttr.set(RtfText.ATTR_UNDERLINE, 0);
        }
        
        if (textDecoration.hasLineThrough()) {
            rtfAttr.set(RtfText.ATTR_STRIKETHROUGH, 1);
        } else {
            rtfAttr.set(RtfText.ATTR_STRIKETHROUGH, 0);
        }
    }

    private static void attrBlockMargin(CommonMarginBlock cmb, FOPRtfAttributes rtfAttr) {
        rtfAttr.setTwips(RtfText.SPACE_BEFORE, 
                cmb.spaceBefore.getOptimum(null).getLength());
        rtfAttr.setTwips(RtfText.SPACE_AFTER, 
                cmb.spaceAfter.getOptimum(null).getLength());
        rtfAttr.setTwips(RtfText.LEFT_INDENT_BODY, cmb.startIndent);
        rtfAttr.setTwips(RtfText.RIGHT_INDENT_BODY, cmb.endIndent);
    }


    /*
    private static void attrBlockDimension(FObj fobj, FOPRtfAttributes rtfAttr) {
        Length ipd = fobj.getProperty(Constants.PR_INLINE_PROGRESSION_DIMENSION)
                .getLengthRange().getOptimum().getLength();
        if (ipd.getEnum() != Constants.EN_AUTO) {
            rtfAttr.set(RtfText.FRAME_WIDTH, ipd);
        }
        Length bpd = fobj.getProperty(Constants.PR_BLOCK_PROGRESSION_DIMENSION)
                .getLengthRange().getOptimum().getLength();
        if (bpd.getEnum() != Constants.EN_AUTO) {
            rtfAttr.set(RtfText.FRAME_HEIGHT, bpd);
        }
    }
    */

    private static void attrBlockTextAlign(int alignment, RtfAttributes rtfAttr) {
        String rtfValue = null;
        switch (alignment) {
            case Constants.EN_CENTER:
                rtfValue = RtfText.ALIGN_CENTER;
                break;
            case Constants.EN_END:
                rtfValue = RtfText.ALIGN_RIGHT;
                break;
            case Constants.EN_JUSTIFY:
                rtfValue = RtfText.ALIGN_JUSTIFIED;
                break;
            default:
                rtfValue = RtfText.ALIGN_LEFT;
                break;
        }

        rtfAttr.set(rtfValue);
    }

    /**
     * Reads background-color for block from <code>bpb</code> and writes it to
     * <code>rtfAttr</code>.
     */
    private static void attrBlockBackgroundColor(
                CommonBorderPaddingBackground bpb, RtfAttributes rtfAttr) {
        if (bpb.hasBackground()) {
            rtfAttr.set(RtfText.SHADING, RtfText.FULL_SHADING);
            rtfAttr.set(RtfText.SHADING_FRONT_COLOR,
                    convertFOPColorToRTF(bpb.backgroundColor));
        }
    }

    /** Adds border information from <code>bpb</code> to <code>rtrAttr</code>. */
    private static void attrBorder(CommonBorderPaddingBackground bpb,
           RtfAttributes rtfAttr, FONode fobj) {
       if (hasBorder(fobj.getParent())) {
           attrInlineBorder(bpb, rtfAttr);
           return;
       }

       BorderAttributesConverter.makeBorder(bpb,
               CommonBorderPaddingBackground.BEFORE, rtfAttr,
               IBorderAttributes.BORDER_TOP);
       BorderAttributesConverter.makeBorder(bpb,
               CommonBorderPaddingBackground.AFTER, rtfAttr,
               IBorderAttributes.BORDER_BOTTOM);
       BorderAttributesConverter.makeBorder(bpb,
               CommonBorderPaddingBackground.START, rtfAttr,
               IBorderAttributes.BORDER_LEFT);
       BorderAttributesConverter.makeBorder(bpb,
               CommonBorderPaddingBackground.END, rtfAttr,
               IBorderAttributes.BORDER_RIGHT);
    }

    /** @return true, if element <code>node</code> has border. */
    private static boolean hasBorder(FONode node) {
        while (node != null) {
            CommonBorderPaddingBackground commonBorderPaddingBackground = null;
            if (node instanceof Block) {
                Block block = (Block) node;
                commonBorderPaddingBackground = block.getCommonBorderPaddingBackground(); 
            } else if (node instanceof BlockContainer) { 
                BlockContainer container = (BlockContainer) node;
                commonBorderPaddingBackground = container.getCommonBorderPaddingBackground();
            } 

            if (commonBorderPaddingBackground != null 
                    && commonBorderPaddingBackground.hasBorder()) {
                return true;
            }

            node = node.getParent();
        }
        return false; 
    }

    /** Adds inline border information from <code>bpb</code> to <code>rtrAttr</code>. */
    private static void attrInlineBorder(CommonBorderPaddingBackground bpb,
            RtfAttributes rtfAttr) {
        BorderAttributesConverter.makeBorder(bpb,
                CommonBorderPaddingBackground.BEFORE, rtfAttr,
                IBorderAttributes.BORDER_CHARACTER);
    }

    /**
     * Reads background-color from bl and writes it to rtfAttr.
     *
     * @param bl the Block object the properties are read from
     * @param rtfAttr the RtfAttributes object the attributes are written to
     */
    private static void attrBackgroundColor(CommonBorderPaddingBackground bpb, 
                RtfAttributes rtfAttr) {
        ColorType fopValue = bpb.backgroundColor;
        int rtfColor = 0;
        /* FOP uses a default background color of "transparent", which is
           actually a transparent black, which is generally not suitable as a
           default here. Changing FOP's default to "white" causes problems in
           PDF output, so we will look for the default here & change it to
           "auto". */
        if ((fopValue == null)
                || ((fopValue.getRed() == 0)
                && (fopValue.getGreen() == 0)
                && (fopValue.getBlue() == 0)
                && (fopValue.getAlpha() == 0))) {
            return;
        } else {
            rtfColor = convertFOPColorToRTF(fopValue);
        }

        rtfAttr.set(RtfText.ATTR_BACKGROUND_COLOR, rtfColor);
   }
    
   private static void attrBaseLineShift(Length baselineShift, RtfAttributes rtfAttr) {
       
       int s = baselineShift.getEnum();
       
       if (s == Constants.EN_SUPER) {
           rtfAttr.set(RtfText.ATTR_SUPERSCRIPT);
       } else if (s == Constants.EN_SUB) {
           rtfAttr.set(RtfText.ATTR_SUBSCRIPT);
       }
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
