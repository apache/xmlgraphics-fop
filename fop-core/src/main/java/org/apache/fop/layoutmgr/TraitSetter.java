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

package org.apache.fop.layoutmgr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.accessibility.StructureTreeElement;
import org.apache.fop.area.Area;
import org.apache.fop.area.Trait;
import org.apache.fop.datatypes.LengthBase;
import org.apache.fop.datatypes.PercentBaseContext;
import org.apache.fop.datatypes.SimplePercentBaseContext;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground.BorderInfo;
import org.apache.fop.fo.properties.CommonMarginBlock;
import org.apache.fop.fo.properties.CommonTextDecoration;
import org.apache.fop.fonts.Font;
import org.apache.fop.traits.BorderProps;
import org.apache.fop.traits.MinOptMax;
import org.apache.fop.traits.Visibility;

/**
 * This is a helper class used for setting common traits on areas.
 */
public final class TraitSetter {

    private TraitSetter() {
    }

    /** logger */
    private static final Log LOG = LogFactory.getLog(TraitSetter.class);

    /**
     * Sets border and padding traits on areas.
     *
     * @param area      area to set the traits on
     * @param bpProps   border and padding properties
     * @param isNotFirst True if the area is not the first area
     * @param isNotLast  True if the area is not the last area
     * @param context   Property evaluation context
     */
    public static void setBorderPaddingTraits(Area area,
            CommonBorderPaddingBackground bpProps, boolean isNotFirst, boolean isNotLast,
            PercentBaseContext context) {
        int padding;
        padding = bpProps.getPadding(CommonBorderPaddingBackground.START, isNotFirst, context);
        if (padding > 0) {
            area.addTrait(Trait.PADDING_START, padding);
        }
        padding = bpProps.getPadding(CommonBorderPaddingBackground.END, isNotLast, context);
        if (padding > 0) {
            area.addTrait(Trait.PADDING_END, padding);
        }
        padding = bpProps.getPadding(CommonBorderPaddingBackground.BEFORE, false, context);
        if (padding > 0) {
            area.addTrait(Trait.PADDING_BEFORE, padding);
        }
        padding = bpProps.getPadding(CommonBorderPaddingBackground.AFTER, false, context);
        if (padding > 0) {
            area.addTrait(Trait.PADDING_AFTER, padding);
        }

        addBorderTrait(area, bpProps, isNotFirst,
                CommonBorderPaddingBackground.START,
                BorderProps.Mode.SEPARATE, Trait.BORDER_START, context);

        addBorderTrait(area, bpProps, isNotLast,
                CommonBorderPaddingBackground.END,
                BorderProps.Mode.SEPARATE, Trait.BORDER_END, context);

        addBorderTrait(area, bpProps, false,
                CommonBorderPaddingBackground.BEFORE,
                BorderProps.Mode.SEPARATE, Trait.BORDER_BEFORE, context);

        addBorderTrait(area, bpProps, false,
                CommonBorderPaddingBackground.AFTER,
                BorderProps.Mode.SEPARATE, Trait.BORDER_AFTER, context);
    }

    /*
     * Sets border traits on an area.
     *
     * @param area    area to set the traits on
     * @param bpProps border and padding properties
     * @param mode    the border paint mode (see BorderProps)
     */
    private static void addBorderTrait(Area area,
                                       CommonBorderPaddingBackground bpProps,

                                       boolean discard, int side, BorderProps.Mode mode,
                                       Integer traitCode, PercentBaseContext context) {
        int width = bpProps.getBorderWidth(side, discard);
        int radiusStart = bpProps.getBorderRadiusStart(side, discard, context);
        int radiusEnd = bpProps.getBorderRadiusEnd(side, discard, context);
        if (width > 0 || radiusStart > 0 || radiusEnd > 0) {
            area.addTrait(traitCode, new BorderProps(bpProps.getBorderStyle(side), width, radiusStart,
                    radiusEnd, bpProps.getBorderColor(side), mode));
        }
    }

    /**
     * Add borders to an area. Note: this method also adds unconditional padding. Don't use!
     * Layout managers that create areas with borders can use this to
     * add the borders to the area.
     * @param area the area to set the traits on.
     * @param borderProps border properties
     * @param context Property evaluation context
     * @deprecated Call the other addBorders() method and addPadding separately.
     */
    @Deprecated
    public static void addBorders(Area area, CommonBorderPaddingBackground borderProps,
                                  PercentBaseContext context) {
        BorderProps bps = getBorderProps(borderProps, CommonBorderPaddingBackground.BEFORE, context);
        if (bps != null) {
            area.addTrait(Trait.BORDER_BEFORE, bps);
        }
        bps = getBorderProps(borderProps, CommonBorderPaddingBackground.AFTER, context);
        if (bps != null) {
            area.addTrait(Trait.BORDER_AFTER, bps);
        }
        bps = getBorderProps(borderProps, CommonBorderPaddingBackground.START, context);
        if (bps != null) {
            area.addTrait(Trait.BORDER_START, bps);
        }
        bps = getBorderProps(borderProps, CommonBorderPaddingBackground.END, context);
        if (bps != null) {
            area.addTrait(Trait.BORDER_END, bps);
        }

        addPadding(area, borderProps, context);
    }

    /**
     * Add borders to an area.
     * Layout managers that create areas with borders can use this to
     * add the borders to the area.
     * @param area the area to set the traits on.
     * @param borderProps border properties
     * @param discardBefore true if the before border should be discarded
     * @param discardAfter true if the after border should be discarded
     * @param discardStart true if the start border should be discarded
     * @param discardEnd true if the end border should be discarded
     * @param context Property evaluation context
     */
    //TODO: remove evaluation context; unused, since border-widths are always absolute lengths
    public static void addBorders(Area area, CommonBorderPaddingBackground borderProps,
                boolean discardBefore, boolean discardAfter,
                boolean discardStart, boolean discardEnd,
                PercentBaseContext context) {
        BorderProps bps = getBorderProps(borderProps, CommonBorderPaddingBackground.BEFORE, context);
        if (bps != null && !discardBefore) {
            area.addTrait(Trait.BORDER_BEFORE, bps);
        }
        bps = getBorderProps(borderProps, CommonBorderPaddingBackground.AFTER, context);
        if (bps != null && !discardAfter) {
            area.addTrait(Trait.BORDER_AFTER, bps);
        }
        bps = getBorderProps(borderProps, CommonBorderPaddingBackground.START, context);
        if (bps != null && !discardStart) {
            area.addTrait(Trait.BORDER_START, bps);
        }
        bps = getBorderProps(borderProps, CommonBorderPaddingBackground.END, context);
        if (bps != null && !discardEnd) {
            area.addTrait(Trait.BORDER_END, bps);
        }

    }

    /**
     * Add borders to an area for the collapsing border model in tables.
     * Layout managers that create areas with borders can use this to
     * add the borders to the area.
     * @param area the area to set the traits on.
     * @param borderBefore the resolved before border
     * @param borderAfter the resolved after border
     * @param borderStart the resolved start border
     * @param borderEnd the resolved end border
     * @param outer 4 boolean values indicating if the side represents the
     *     table's outer border. Order: before, after, start, end
     */
    public static void addCollapsingBorders(Area area,
            BorderInfo borderBefore, BorderInfo borderAfter,
            BorderInfo borderStart, BorderInfo borderEnd,
            boolean[] outer) {
        BorderProps bps = getCollapsingBorderProps(borderBefore, outer[0]);
        if (bps != null) {
            area.addTrait(Trait.BORDER_BEFORE, bps);
        }
        bps = getCollapsingBorderProps(borderAfter, outer[1]);
        if (bps != null) {
            area.addTrait(Trait.BORDER_AFTER, bps);
        }
        bps = getCollapsingBorderProps(borderStart, outer[2]);
        if (bps != null) {
            area.addTrait(Trait.BORDER_START, bps);
        }
        bps = getCollapsingBorderProps(borderEnd, outer[3]);
        if (bps != null) {
            area.addTrait(Trait.BORDER_END, bps);
        }
    }

    private static void addPadding(Area area, CommonBorderPaddingBackground bordProps,
                                PercentBaseContext context) {
        addPadding(area, bordProps, false, false, false, false, context);
    }

    /**
     * Add padding to an area.
     * Layout managers that create areas with padding can use this to
     * add the borders to the area.
     * @param area the area to set the traits on.
     * @param bordProps border and padding properties
     * @param discardBefore true if the before padding should be discarded
     * @param discardAfter true if the after padding should be discarded
     * @param discardStart true if the start padding should be discarded
     * @param discardEnd true if the end padding should be discarded
     * @param context Property evaluation context
     */
    public static void addPadding(Area area, CommonBorderPaddingBackground bordProps,
                boolean discardBefore, boolean discardAfter,
                boolean discardStart, boolean discardEnd,
                PercentBaseContext context) {
        int padding = bordProps.getPadding(CommonBorderPaddingBackground.BEFORE,
                discardBefore, context);
        if (padding != 0) {
            area.addTrait(Trait.PADDING_BEFORE, padding);
        }

        padding = bordProps.getPadding(CommonBorderPaddingBackground.AFTER,
                discardAfter, context);
        if (padding != 0) {
            area.addTrait(Trait.PADDING_AFTER, padding);
        }

        padding = bordProps.getPadding(CommonBorderPaddingBackground.START,
                discardStart, context);
        if (padding != 0) {
            area.addTrait(Trait.PADDING_START, padding);
        }

        padding = bordProps.getPadding(CommonBorderPaddingBackground.END,
                discardEnd, context);
        if (padding != 0) {
            area.addTrait(Trait.PADDING_END, padding);
        }

    }

    private static BorderProps getBorderProps(CommonBorderPaddingBackground bordProps,
            int side, PercentBaseContext context) {
        int width = bordProps.getBorderWidth(side, false);
        int radiusStart = bordProps.getBorderRadiusStart(side, false, context);
        int radiusEnd = bordProps.getBorderRadiusEnd(side, false, context);
        if (width != 0 || radiusStart != 0 || radiusEnd != 0) {
            return new BorderProps(bordProps.getBorderStyle(side), width, radiusStart, radiusEnd,
                                  bordProps.getBorderColor(side), BorderProps.Mode.SEPARATE);
        } else {
            return null;
        }
    }

    private static BorderProps getCollapsingBorderProps(BorderInfo borderInfo, boolean outer) {
        assert borderInfo != null;
        int width = borderInfo.getRetainedWidth();
        if (width != 0) {
            return  BorderProps.makeRectangular(borderInfo.getStyle(), width, borderInfo.getColor(),
                    (outer ? BorderProps.Mode.COLLAPSE_OUTER : BorderProps.Mode.COLLAPSE_INNER));
        } else {
            return null;
        }
    }


    /**
     * Add background to an area. This method is mainly used by table-related layout
     * managers to add background for column, body or row. Since the area corresponding to
     * border-separation must be filled with the table's background, for every cell an
     * additional area with the same dimensions is created to hold the background for the
     * corresponding column/body/row. An additional shift must then be added to
     * background-position-horizontal/vertical to ensure the background images are
     * correctly placed. Indeed the placement of images must be made WRT the
     * column/body/row and not the cell.
     *
     * <p>Note: The area's IPD and BPD must be set before calling this method.</p>
     *
     * <p>TODO the regular
     * {@link #addBackground(Area, CommonBorderPaddingBackground, PercentBaseContext)}
     * method should be used instead, and a means to retrieve the original area's
     * dimensions must be found.</p>
     *
     * <p>TODO the placement of images in the x- or y-direction will be incorrect if
     * background-repeat is set for that direction.</p>
     *
     * @param area the area to set the traits on
     * @param backProps the background properties
     * @param context Property evaluation context
     * @param ipdShift horizontal shift to affect to the background, in addition to the
     * value of the background-position-horizontal property
     * @param bpdShift vertical shift to affect to the background, in addition to the
     * value of the background-position-vertical property
     * @param referenceIPD value to use as a reference for percentage calculation
     * @param referenceBPD value to use as a reference for percentage calculation
     */
    public static void addBackground(Area area,
            CommonBorderPaddingBackground backProps,
            PercentBaseContext context,
            int ipdShift, int bpdShift, int referenceIPD, int referenceBPD) {
        if (!backProps.hasBackground()) {
            return;
        }
        Trait.Background back = new Trait.Background();
        back.setColor(backProps.backgroundColor);

        if (backProps.getImageInfo() != null) {
            back.setURL(backProps.backgroundImage);
            back.setImageInfo(backProps.getImageInfo());
            back.setRepeat(backProps.backgroundRepeat);
            if (backProps.backgroundPositionHorizontal != null) {
                if (back.getRepeat() == Constants.EN_NOREPEAT
                        || back.getRepeat() == Constants.EN_REPEATY) {
                    if (area.getIPD() > 0) {
                        PercentBaseContext refContext = new SimplePercentBaseContext(context,
                                LengthBase.IMAGE_BACKGROUND_POSITION_HORIZONTAL,
                                (referenceIPD - back.getImageInfo().getSize().getWidthMpt()));

                        back.setHoriz(ipdShift
                                + backProps.backgroundPositionHorizontal.getValue(refContext));
                    } else {
                        // TODO Area IPD has to be set for this to work
                        LOG.warn("Horizontal background image positioning ignored"
                                + " because the IPD was not set on the area."
                                + " (Yes, it's a bug in FOP)");
                    }
                }
            }
            if (backProps.backgroundPositionVertical != null) {
                if (back.getRepeat() == Constants.EN_NOREPEAT
                        || back.getRepeat() == Constants.EN_REPEATX) {
                    if (area.getBPD() > 0) {
                        PercentBaseContext refContext = new SimplePercentBaseContext(context,
                                LengthBase.IMAGE_BACKGROUND_POSITION_VERTICAL,
                                (referenceBPD - back.getImageInfo().getSize().getHeightMpt()));
                        back.setVertical(bpdShift
                                + backProps.backgroundPositionVertical.getValue(refContext));
                    } else {
                        // TODO Area BPD has to be set for this to work
                        LOG.warn("Vertical background image positioning ignored"
                                + " because the BPD was not set on the area."
                                + " (Yes, it's a bug in FOP)");
                    }
                }
            }
        }

        area.addTrait(Trait.BACKGROUND, back);
    }

    /**
     * Add background to an area.
     * Layout managers that create areas with a background can use this to
     * add the background to the area.
     * Note: The area's IPD and BPD must be set before calling this method.
     * @param area the area to set the traits on
     * @param backProps the background properties
     * @param context Property evaluation context
     */
    public static void addBackground(Area area,
                                     CommonBorderPaddingBackground backProps,
                                     PercentBaseContext context) {
        if (!backProps.hasBackground()) {
            return;
        }
        Trait.Background back = new Trait.Background();
        back.setColor(backProps.backgroundColor);

        if (backProps.getImageInfo() != null) {
            back.setURL(backProps.backgroundImage);
            back.setImageInfo(backProps.getImageInfo());
            back.setRepeat(backProps.backgroundRepeat);
            if (backProps.backgroundPositionHorizontal != null) {
                if (back.getRepeat() == Constants.EN_NOREPEAT
                        || back.getRepeat() == Constants.EN_REPEATY) {
                    if (area.getIPD() > 0) {
                        int width = area.getIPD();
                        width += backProps.getPaddingStart(false, context);
                        width += backProps.getPaddingEnd(false, context);
                        int imageWidthMpt = back.getImageInfo().getSize().getWidthMpt();
                        int lengthBaseValue = width - imageWidthMpt;
                        SimplePercentBaseContext simplePercentBaseContext
                                = new SimplePercentBaseContext(context,
                                LengthBase.IMAGE_BACKGROUND_POSITION_HORIZONTAL,
                                lengthBaseValue);
                        int horizontal = backProps.backgroundPositionHorizontal.getValue(
                                simplePercentBaseContext);
                        back.setHoriz(horizontal);
                    } else {
                        //TODO Area IPD has to be set for this to work
                        LOG.warn("Horizontal background image positioning ignored"
                                + " because the IPD was not set on the area."
                                + " (Yes, it's a bug in FOP)");
                    }
                }
            }
            if (backProps.backgroundPositionVertical != null) {
                if (back.getRepeat() == Constants.EN_NOREPEAT
                        || back.getRepeat() == Constants.EN_REPEATX) {
                    if (area.getBPD() > 0) {
                        int height = area.getBPD();
                        height += backProps.getPaddingBefore(false, context);
                        height += backProps.getPaddingAfter(false, context);
                        int imageHeightMpt = back.getImageInfo().getSize().getHeightMpt();
                        int lengthBaseValue = height - imageHeightMpt;
                        SimplePercentBaseContext simplePercentBaseContext
                                = new SimplePercentBaseContext(context,
                                LengthBase.IMAGE_BACKGROUND_POSITION_VERTICAL,
                                lengthBaseValue);
                        int vertical = backProps.backgroundPositionVertical.getValue(
                                simplePercentBaseContext);
                        back.setVertical(vertical);
                    } else {
                        //TODO Area BPD has to be set for this to work
                        LOG.warn("Vertical background image positioning ignored"
                                + " because the BPD was not set on the area."
                                + " (Yes, it's a bug in FOP)");
                    }
                }
            }
            if (backProps.backgroungImageTargetWidth.getValue() != 0) {
                back.setImageTargetWidth(backProps.backgroungImageTargetWidth.getValue());
            }
            if (backProps.backgroungImageTargetHeight.getValue() != 0) {
                back.setImageTargetHeight(backProps.backgroungImageTargetHeight.getValue());
            }
        }

        area.addTrait(Trait.BACKGROUND, back);
    }

    /**
     * Add space to a block area.
     * Layout managers that create block areas can use this to add space
     * outside of the border rectangle to the area.
     * @param area the area to set the traits on.
     * @param bpProps the border, padding and background properties
     * @param startIndent the effective start-indent value
     * @param endIndent the effective end-indent value
     * @param context the context for evaluation of percentages
     */
    public static void addMargins(Area area,
                                  CommonBorderPaddingBackground bpProps,
                                  int startIndent, int endIndent,
                                  PercentBaseContext context) {
        if (startIndent != 0) {
            area.addTrait(Trait.START_INDENT, startIndent);
        }

        int spaceStart = startIndent
                - bpProps.getBorderStartWidth(false)
                - bpProps.getPaddingStart(false, context);
        if (spaceStart != 0) {
            area.addTrait(Trait.SPACE_START, spaceStart);
        }

        if (endIndent != 0) {
            area.addTrait(Trait.END_INDENT, endIndent);
        }
        int spaceEnd = endIndent
                - bpProps.getBorderEndWidth(false)
                - bpProps.getPaddingEnd(false, context);
        if (spaceEnd != 0) {
            area.addTrait(Trait.SPACE_END, spaceEnd);
        }
    }

    /**
     * Add space to a block area.
     * Layout managers that create block areas can use this to add space
     * outside of the border rectangle to the area.
     * @param area the area to set the traits on.
     * @param bpProps the border, padding and background properties
     * @param marginProps the margin properties.
     * @param context the context for evaluation of percentages
     */
    public static void addMargins(Area area,
                                  CommonBorderPaddingBackground bpProps,
                                  CommonMarginBlock marginProps,
                                  PercentBaseContext context) {
        int startIndent = marginProps.startIndent.getValue(context);
        int endIndent = marginProps.endIndent.getValue(context);
        addMargins(area, bpProps, startIndent, endIndent, context);
    }

    /**
     * Returns the effective space length of a resolved space specifier based on the adjustment
     * value.
     * @param adjust the adjustment value
     * @param space the space specifier
     * @return the effective space length
     */
    public static int getEffectiveSpace(double adjust, MinOptMax space) {
        if (space == null) {
            return 0;
        } else {
            int spaceOpt = space.getOpt();
            if (adjust > 0) {
                spaceOpt += (int) (adjust * space.getStretch());
            } else {
                spaceOpt += (int) (adjust * space.getShrink());
            }
            return spaceOpt;
        }
    }

    /**
     * Adds traits for space-before and space-after to an area.
     * @param area the target area
     * @param adjust the adjustment value
     * @param spaceBefore the space-before space specifier
     * @param spaceAfter the space-after space specifier
     */
    public static void addSpaceBeforeAfter(Area area, double adjust, MinOptMax spaceBefore,
                                           MinOptMax spaceAfter) {
        addSpaceTrait(area, Trait.SPACE_BEFORE, spaceBefore, adjust);
        addSpaceTrait(area, Trait.SPACE_AFTER, spaceAfter, adjust);
    }

    private static void addSpaceTrait(Area area, Integer spaceTrait,
            MinOptMax space, double adjust) {
        int effectiveSpace = getEffectiveSpace(adjust, space);
        if (effectiveSpace != 0) {
            area.addTrait(spaceTrait, effectiveSpace);
        }
    }

    /**
     * Sets the traits for breaks on an area.
     * @param area the area to set the traits on.
     * @param breakBefore the value for break-before
     * @param breakAfter the value for break-after
     */
    public static void addBreaks(Area area, int breakBefore, int breakAfter) {
        /* Currently disabled as these traits are never used by the renderers
        area.addTrait(Trait.BREAK_AFTER, Integer.valueOf(breakAfter));
        area.addTrait(Trait.BREAK_BEFORE, Integer.valueOf(breakBefore));
        */
    }

    /**
     * Adds font traits to an area
     * @param area the target are
     * @param font the font to use
     */
    public static void addFontTraits(Area area, Font font) {
        area.addTrait(Trait.FONT, font.getFontTriplet());
        area.addTrait(Trait.FONT_SIZE, font.getFontSize());
    }

    /**
     * Adds the text-decoration traits to the area.
     * @param area the area to set the traits on
     * @param deco the text decorations
     */
    public static void addTextDecoration(Area area, CommonTextDecoration deco) {
        //TODO Finish text-decoration
        if (deco != null) {
            if (deco.hasUnderline()) {
                area.addTrait(Trait.UNDERLINE, Boolean.TRUE);
                area.addTrait(Trait.UNDERLINE_COLOR, deco.getUnderlineColor());
            }
            if (deco.hasOverline()) {
                area.addTrait(Trait.OVERLINE, Boolean.TRUE);
                area.addTrait(Trait.OVERLINE_COLOR, deco.getOverlineColor());
            }
            if (deco.hasLineThrough()) {
                area.addTrait(Trait.LINETHROUGH, Boolean.TRUE);
                area.addTrait(Trait.LINETHROUGH_COLOR, deco.getLineThroughColor());
            }
            if (deco.isBlinking()) {
                area.addTrait(Trait.BLINK, Boolean.TRUE);
            }
        }
    }

    public static void setVisibility(Area area, int visibility) {
        Visibility v;
        switch (visibility) {
            case Constants.EN_COLLAPSE:
                v = Visibility.COLLAPSE;
                break;
            case Constants.EN_HIDDEN:
                v = Visibility.HIDDEN;
                break;
            default:
                v = Visibility.VISIBLE;
        }
        area.addTrait(Trait.VISIBILITY, v);
    }

    /**
     * Sets the structure tree element associated to the given area.
     *
     * @param area the area to set the traits on
     * @param structureTreeElement the element the area is associated to in the document structure
     */
    public static void addStructureTreeElement(Area area,
            StructureTreeElement structureTreeElement) {
        if (structureTreeElement != null) {
            area.addTrait(Trait.STRUCTURE_TREE_ELEMENT, structureTreeElement);
        }
    }

    /**
     * Sets the producer's ID as a trait on the area. This can be used to track back the
     * generating FO node.
     * @param area the area to set the traits on
     * @param id the ID to set
     */
    public static void setProducerID(Area area, String id) {
        if (id != null && id.length() > 0) {
            area.addTrait(Trait.PROD_ID, id);
        }
    }

    /**
     * Sets the optional content group layer as a trait on the area.
     * @param area the area to set the traits on
     * @param layer the layer ID to set
     */
    public static void setLayer(Area area, String layer) {
        if (layer != null && layer.length() > 0) {
            area.addTrait(Trait.LAYER, layer);
        }
    }
}
