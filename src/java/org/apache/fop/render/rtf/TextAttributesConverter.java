/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import java.awt.Color;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.PercentBaseContext;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FOText;
import org.apache.fop.fo.flow.Block;
import org.apache.fop.fo.flow.BlockContainer;
import org.apache.fop.fo.flow.Inline;
import org.apache.fop.fo.flow.Leader;
import org.apache.fop.fo.flow.PageNumber;
import org.apache.fop.fo.flow.table.TableCell;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.CommonFont;
import org.apache.fop.fo.properties.CommonMarginBlock;
import org.apache.fop.fo.properties.CommonTextDecoration;
import org.apache.fop.fo.properties.PercentLength;
import org.apache.fop.render.rtf.rtflib.rtfdoc.IBorderAttributes;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfAttributes;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfColorTable;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfFontManager;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfLeader;
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

    private static Log log = LogFactory.getLog(TextAttributesConverter.class);

    /**
     * Constructor is private, because it's just a utility class.
     */
    private TextAttributesConverter() {
    }

    /**
     * Converts all known text FO properties to RtfAttributes
     * @param fobj the FO for which the attributes are to be converted
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
        attrBreak(fobj, attrib);

        return attrib;
    }

    private static void attrBreak(Block fobj, FOPRtfAttributes attrib) {
        int breakValue = fobj.getBreakBefore();
        if (breakValue != Constants.EN_AUTO) {
            //"sect" Creates a new section and a page break,
            //a simple page break with control word "page" caused
            //some problems
            boolean bHasTableCellParent = false;
            FONode f = fobj;
            while (f.getParent() != null) {
                f = f.getParent();
                if (f instanceof TableCell) {
                    bHasTableCellParent = true;
                    break;
                }
            }
            if (!bHasTableCellParent) {
                attrib.set("sect");
                switch (breakValue) {
                case Constants.EN_EVEN_PAGE:
                    attrib.set("sbkeven");
                    break;
                case Constants.EN_ODD_PAGE:
                    attrib.set("sbkodd");
                    break;
                case Constants.EN_COLUMN:
                    attrib.set("sbkcol");
                    break;
                default:
                    attrib.set("sbkpage");
                }
            } else {
                log.warn("Cannot create break-before for a block inside a table.");
            }
        }
        //Break after is handled in RtfCloseGroupMark
    }

    /**
     * Converts all known text FO properties to RtfAttributes
     * @param fobj FObj whose properties are to be converted
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
        attrBaseLineShift(fobj.getBaselineShift(), attrib);
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


    /**
     * Converts FO properties used by RtfLeader to RtfAttributes.
     * @param fobj Leader
     * @param context PercentBaseContext
     * @return RtfAttributes
     * @throws FOPException
     */
    public static RtfAttributes convertLeaderAttributes(Leader fobj, PercentBaseContext context)
                throws FOPException {
        boolean tab = false;
        FOPRtfAttributes attrib = new FOPRtfAttributes();
        attrib.set(RtfText.ATTR_FONT_FAMILY,
        RtfFontManager.getInstance().getFontNumber(fobj.getCommonFont().getFirstFontFamily()));

        if (fobj.getLeaderLength() != null) {
            attrib.set(RtfLeader.LEADER_WIDTH, convertMptToTwips(fobj.getLeaderLength().getMaximum(
                    context).getLength().getValue(context)));

            if (fobj.getLeaderLength().getMaximum(context) instanceof PercentLength) {
                if (((PercentLength)fobj.getLeaderLength().getMaximum(context)).getString().equals(
                            "100.0%")) {
                    // Use Tab instead of white spaces
                    attrib.set(RtfLeader.LEADER_USETAB, 1);
                    tab = true;
                }
            }
        }

        attrFontColor(fobj.getColor(), attrib);

        if (fobj.getLeaderPatternWidth() != null) {
            //TODO calculate pattern width not possible for white spaces, because its using
            //underlines for tab it would work with LEADER_PATTERN_WIDTH (expndtw)
        }

        switch(fobj.getLeaderPattern()) {
        case Constants.EN_DOTS:
            if (tab) {
                attrib.set(RtfLeader.LEADER_TABLEAD, RtfLeader.LEADER_TAB_DOTTED);
            } else {
                attrib.set(RtfLeader.LEADER_TABLEAD, RtfLeader.LEADER_DOTTED);
            }
            break;
        case Constants.EN_SPACE:
            //nothing has to be set for spaces
            break;
        case Constants.EN_RULE:
            //Things like start-indent, space-after, ... not supported?
            //Leader class does not offer these properties
            //TODO aggregate them with the leader width or
            // create a second - blank leader - before

            if (fobj.getRuleThickness() != null) {
                //TODO See inside RtfLeader, better calculation for
                //white spaces would be necessary
                //attrib.set(RtfLeader.LEADER_RULE_THICKNESS,
                //    fobj.getRuleThickness().getValue(context));
                log.warn("RTF: fo:leader rule-thickness not supported");
            }

            switch (fobj.getRuleStyle()) {
            case Constants.EN_SOLID:
                if (tab) {
                    attrib.set(RtfLeader.LEADER_TABLEAD, RtfLeader.LEADER_TAB_THICK);
                } else {
                    attrib.set(RtfLeader.LEADER_TABLEAD, RtfLeader.LEADER_THICK);
                }
                break;
            case Constants.EN_DASHED:
                if (tab) {
                    attrib.set(RtfLeader.LEADER_TABLEAD, RtfLeader.LEADER_TAB_MIDDLEDOTTED);
                } else {
                    attrib.set(RtfLeader.LEADER_TABLEAD, RtfLeader.LEADER_MIDDLEDOTTED);
                }
                break;
            case Constants.EN_DOTTED:
                if (tab) {
                    attrib.set(RtfLeader.LEADER_TABLEAD, RtfLeader.LEADER_TAB_DOTTED);
                } else {
                    attrib.set(RtfLeader.LEADER_TABLEAD, RtfLeader.LEADER_DOTTED);
                }
                break;
            case Constants.EN_DOUBLE:
                if (tab) {
                    attrib.set(RtfLeader.LEADER_TABLEAD, RtfLeader.LEADER_TAB_EQUAL);
                } else {
                    attrib.set(RtfLeader.LEADER_TABLEAD, RtfLeader.LEADER_EQUAL);
                }
                break;
            case Constants.EN_GROOVE:
                if (tab) {
                    attrib.set(RtfLeader.LEADER_TABLEAD, RtfLeader.LEADER_TAB_HYPHENS);
                } else {
                    attrib.set(RtfLeader.LEADER_TABLEAD, RtfLeader.LEADER_HYPHENS);
                }
                break;
            case Constants.EN_RIDGE:
                if (tab) {
                    attrib.set(RtfLeader.LEADER_TABLEAD, RtfLeader.LEADER_TAB_UNDERLINE);
                } else {
                    attrib.set(RtfLeader.LEADER_TABLEAD, RtfLeader.LEADER_UNDERLINE);
                }
                break;
            default:
                break;
            }
            break;
        case Constants.EN_USECONTENT:
            log.warn("RTF: fo:leader use-content not supported");
            break;
        default:
            break;
        }

        if (fobj.getLeaderAlignment() == Constants.EN_REFERENCE_AREA) {
            log.warn("RTF: fo:leader reference-area not supported");
        }
        return attrib;
    }

    private static int convertMptToTwips(int mpt) {
        return Math.round(FoUnitsConverter.getInstance().convertMptToTwips(mpt));
    }

    private static void attrFont(CommonFont font, FOPRtfAttributes rtfAttr) {
        rtfAttr.set(RtfText.ATTR_FONT_FAMILY,
                RtfFontManager.getInstance().getFontNumber(font.getFirstFontFamily()));
        rtfAttr.setHalfPoints(RtfText.ATTR_FONT_SIZE, font.fontSize);

        if (font.getFontWeight() == Constants.EN_700
                || font.getFontWeight() == Constants.EN_800
                || font.getFontWeight() == Constants.EN_900) {
            //Everything from 700 and above is declared as bold
            rtfAttr.set("b", 1);
        } else {
            rtfAttr.set("b", 0);
        }

        if (font.getFontStyle() == Constants.EN_ITALIC) {
            rtfAttr.set(RtfText.ATTR_ITALIC, 1);
        } else {
            rtfAttr.set(RtfText.ATTR_ITALIC, 0);
        }


    }


    private static void attrFontColor(Color colorType, RtfAttributes rtfAttr) {
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
            rtfAttr.set(RtfText.ATTR_UNDERLINE, 0);
            rtfAttr.set(RtfText.ATTR_STRIKETHROUGH, 0);
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
     * @param bpb the CommonBorderPaddingBackground from which the properties are read
     * @param rtfAttr the RtfAttributes object the attributes are written to
     */
    private static void attrBackgroundColor(CommonBorderPaddingBackground bpb,
                RtfAttributes rtfAttr) {
        Color fopValue = bpb.backgroundColor;
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
   public static int convertFOPColorToRTF(Color fopColor) {
       // TODO: This code is duplicated in FOPRtfAttributesConverter
       int redComponent = fopColor.getRed();
       int greenComponent = fopColor.getGreen();
       int blueComponent = fopColor.getBlue();
       return RtfColorTable.getInstance().getColorNumber(redComponent,
               greenComponent, blueComponent).intValue();
   }
}
