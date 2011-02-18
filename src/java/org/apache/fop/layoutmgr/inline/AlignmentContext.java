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

package org.apache.fop.layoutmgr.inline;

import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.LengthBase;
import org.apache.fop.datatypes.SimplePercentBaseContext;
import org.apache.fop.fo.Constants;
import org.apache.fop.fonts.Font;

/**
 * The alignment context is carried within a LayoutContext and as
 * part of the Knuth Inline elements to facilitate proper line building.
 * All measurements are in mpt.
 */
public class AlignmentContext implements Constants {

    /** The height or BPD of this context. */
    private int areaHeight;

    /** The computed line-height property value applicable. */
    private int lineHeight;

    /** The distance in BPD from the top of the box to the alignmentPoint. */
    private int alignmentPoint;

    /** The baseline shift value in effect. */
    private int baselineShiftValue;

    /** The computed alignment baseline identifier. */
    private int alignmentBaselineIdentifier;

    /** The x height. */
    private int xHeight;

    private ScaledBaselineTable scaledBaselineTable;
    private ScaledBaselineTable actualBaselineTable;
    private AlignmentContext parentAlignmentContext;

    /**
     * Creates a new instance of AlignmentContext
     * for graphics areas.
     * @param height the total height of the area
     * @param alignmentAdjust the alignment-adjust property
     * @param alignmentBaseline the alignment-baseline property
     * @param baselineShift the baseline-shift property
     * @param dominantBaseline the dominant-baseline property
     * @param parentAlignmentContext the parent alignment context
     */
    AlignmentContext(int height,
            Length alignmentAdjust,
            int alignmentBaseline,
            Length baselineShift,
            int dominantBaseline,
            AlignmentContext parentAlignmentContext) {

        this(height, 0, height, height, alignmentAdjust, alignmentBaseline, baselineShift,
                dominantBaseline, parentAlignmentContext);
    }

    /**
     * Creates a new instance.
     *
     * @param font the font
     * @param lineHeight the computed value of the lineHeight property
     * @param alignmentAdjust the alignment-adjust property
     * @param alignmentBaseline the alignment-baseline property
     * @param baselineShift the baseline-shift property
     * @param dominantBaseline the dominant-baseline property
     * @param parentAlignmentContext the parent alignment context
     */
    AlignmentContext(Font font,
            int lineHeight,
            Length alignmentAdjust,
            int alignmentBaseline,
            Length baselineShift,
            int dominantBaseline,
            AlignmentContext parentAlignmentContext) {
        this(font.getAscender(), font.getDescender(), lineHeight, font.getXHeight(),
                alignmentAdjust, alignmentBaseline, baselineShift, dominantBaseline,
                parentAlignmentContext);
    }

    /**
     * Creates a new instance of AlignmentContext.
     * @param altitude the altitude of the area
     * @param depth the depth of the area
     * @param lineHeight the line height
     * @param xHeight the xHeight
     * @param alignmentAdjust the alignment-adjust property
     * @param alignmentBaseline the alignment-baseline property
     * @param baselineShift the baseline-shift property
     * @param dominantBaseline the dominant-baseline property
     * @param parentAlignmentContext the parent alignment context
     */
    private AlignmentContext(int altitude,                              // CSOK: ParameterNumber
            int depth,
            int lineHeight,
            int xHeight,
            Length alignmentAdjust,
            int alignmentBaseline,
            Length baselineShift,
            int dominantBaseline,
            AlignmentContext parentAlignmentContext) {

        this.areaHeight = altitude - depth;
        this.lineHeight = lineHeight;
        this.xHeight = xHeight;
        this.parentAlignmentContext = parentAlignmentContext;
        this.scaledBaselineTable = parentAlignmentContext.getScaledBaselineTable();
        setAlignmentBaselineIdentifier(alignmentBaseline
                                       , parentAlignmentContext.getDominantBaselineIdentifier());
        setBaselineShift(baselineShift);
        int dominantBaselineIdentifier = parentAlignmentContext.getDominantBaselineIdentifier();
        boolean newScaledBaselineTableRequired = false;
        if (baselineShiftValue != 0) {
            newScaledBaselineTableRequired = true;
        }
        switch (dominantBaseline) {
            case EN_AUTO:
                newScaledBaselineTableRequired = baselineShiftValue != 0;
                break;
            case EN_USE_SCRIPT: // TODO
                break;
            case EN_NO_CHANGE:
                break;
            case EN_RESET_SIZE:
                newScaledBaselineTableRequired = true;
                break;
            default:
                newScaledBaselineTableRequired = true;
                dominantBaselineIdentifier = dominantBaseline;
                break;
        }
        actualBaselineTable = new ScaledBaselineTable(
                altitude,
                depth,
                xHeight,
                dominantBaselineIdentifier,
                scaledBaselineTable.getWritingMode());
        if (newScaledBaselineTableRequired) {
            scaledBaselineTable = new ScaledBaselineTable(
                    altitude,
                    depth,
                    xHeight,
                    dominantBaselineIdentifier,
                    scaledBaselineTable.getWritingMode());
        }
        setAlignmentAdjust(alignmentAdjust);
    }

    /**
     * Creates a new instance of AlignmentContext based simply
     * on the font and the writing mode.
     * @param font the font
     * @param lineHeight the computed value of the lineHeight property
     * @param writingMode the current writing mode
     */
    AlignmentContext(Font font, int lineHeight, int writingMode) {
        this.areaHeight = font.getAscender() - font.getDescender();
        this.lineHeight = lineHeight;
        this.xHeight = font.getXHeight();
        this.scaledBaselineTable = new ScaledBaselineTable(font.getAscender(), font.getDescender(),
                font.getXHeight(), Constants.EN_ALPHABETIC, writingMode);
        this.actualBaselineTable = scaledBaselineTable;
        this.alignmentBaselineIdentifier = getDominantBaselineIdentifier();
        this.alignmentPoint = font.getAscender();
        this.baselineShiftValue = 0;
    }

    /**
     * Returns the alignment point for this context.
     * This is the point on the start edge of the area this context
     * applies to measured from the before edge of the area.
     * @return the default alignment point
     */
    public int getAlignmentPoint() {
        return alignmentPoint;
    }

    /**
     * Returns the current value of baseline shift in effect.
     * @return the baseline shift
     */
    public int getBaselineShiftValue() {
        return baselineShiftValue;
    }

    /**
     * Returns the current alignment baseline identifier.
     *
     * @return the alignment baseline identifier
     */
    public int getAlignmentBaselineIdentifier() {
        return alignmentBaselineIdentifier;
    }

    /**
     * Sets the current alignment baseline identifier. For
     * alignment-baseline values of "auto" and "baseline" this
     * method does the conversion into the appropriate computed
     * value assuming script is "auto" and the fo is not fo:character.
     * @param alignmentBaseline the alignment-baseline property
     * @param parentDominantBaselineIdentifier the dominant baseline of the parent fo
     */
    private void setAlignmentBaselineIdentifier(int alignmentBaseline
                                               , int parentDominantBaselineIdentifier) {
        switch (alignmentBaseline) {
            case EN_AUTO: // fall through
            case EN_BASELINE:
                this.alignmentBaselineIdentifier = parentDominantBaselineIdentifier;
                break;
            case EN_BEFORE_EDGE:
            case EN_TEXT_BEFORE_EDGE:
            case EN_CENTRAL:
            case EN_MIDDLE:
            case EN_AFTER_EDGE:
            case EN_TEXT_AFTER_EDGE:
            case EN_IDEOGRAPHIC:
            case EN_ALPHABETIC:
            case EN_HANGING:
            case EN_MATHEMATICAL:
                this.alignmentBaselineIdentifier = alignmentBaseline;
                break;
            default: throw new IllegalArgumentException(String.valueOf(alignmentBaseline));
        }
    }

    /**
     * Sets the current alignment baseline identifer. For
     * alignment-baseline values of "auto" and "baseline" this
     * method does the conversion into the appropriate computed
     * value assuming script is "auto" and the fo is not fo:character.
     * @param alignmentAdjust the alignment-adjust property
     */
    private void setAlignmentAdjust(Length alignmentAdjust) {
        int beforeEdge = actualBaselineTable.getBaseline(EN_BEFORE_EDGE);
        switch (alignmentAdjust.getEnum()) {
            case EN_AUTO:
                alignmentPoint = beforeEdge
                                    - actualBaselineTable.getBaseline(alignmentBaselineIdentifier);
                break;
            case EN_BASELINE:
                alignmentPoint = beforeEdge;
                break;
            case EN_BEFORE_EDGE:
            case EN_TEXT_BEFORE_EDGE:
            case EN_CENTRAL:
            case EN_MIDDLE:
            case EN_AFTER_EDGE:
            case EN_TEXT_AFTER_EDGE:
            case EN_IDEOGRAPHIC:
            case EN_ALPHABETIC:
            case EN_HANGING:
            case EN_MATHEMATICAL:
                alignmentPoint = beforeEdge
                                    - actualBaselineTable.getBaseline(alignmentAdjust.getEnum());
                break;
            default:
                alignmentPoint = beforeEdge
                    + alignmentAdjust.getValue(new SimplePercentBaseContext(null
                                                        , LengthBase.ALIGNMENT_ADJUST
                                                        , lineHeight));
                break;
        }
    }

    /**
     * Return the scaled baseline table for this context.
     * @return the scaled baseline table
     */
    private ScaledBaselineTable getScaledBaselineTable() {
        return this.scaledBaselineTable;
    }

    /**
     * Return the dominant baseline identifier.
     * @return the dominant baseline identifier
     */
    private int getDominantBaselineIdentifier() {
        return actualBaselineTable.getDominantBaselineIdentifier();
    }

    /**
     * Calculates the baseline shift value based on the baseline-shift
     * property value.
     * @param baselineShift the baseline shift property value
     */
    private void setBaselineShift(Length baselineShift) {
        baselineShiftValue = 0;
        switch (baselineShift.getEnum()) {
            case EN_BASELINE: //Nothing to do
                break;
            case EN_SUB:
                baselineShiftValue = Math.round(-(xHeight / 2)
                                + parentAlignmentContext.getActualBaselineOffset(EN_ALPHABETIC)
                                );
                break;
            case EN_SUPER:
                baselineShiftValue = Math.round(parentAlignmentContext.getXHeight()
                                + parentAlignmentContext.getActualBaselineOffset(EN_ALPHABETIC)
                                );
                break;
            case 0: // A <length> or <percentage> value
                baselineShiftValue = baselineShift.getValue(
                    new SimplePercentBaseContext(null
                                                , LengthBase.CUSTOM_BASE
                                                , parentAlignmentContext.getLineHeight()));
                break;
            default: throw new IllegalArgumentException(String.valueOf(baselineShift.getEnum()));
        }
    }

    /**
     * Return the parent alignment context.
     * @return the parent alignment context
     */
    public AlignmentContext getParentAlignmentContext() {
        return parentAlignmentContext;
    }

    /**
     * Return the offset between the current dominant baseline and
     * the parent dominant baseline.
     * @return the offset in shift direction
     */
    private int getBaselineOffset() {
        if (parentAlignmentContext == null) {
            return 0;
        }
        return parentAlignmentContext.getScaledBaselineTable()
                                    .getBaseline(alignmentBaselineIdentifier)
                - scaledBaselineTable
                    .deriveScaledBaselineTable(parentAlignmentContext
                                               .getDominantBaselineIdentifier())
                    .getBaseline(alignmentBaselineIdentifier)
                - scaledBaselineTable
                    .getBaseline(parentAlignmentContext
                                 .getDominantBaselineIdentifier())
                + baselineShiftValue;
    }

    /**
     * Return the offset between the current dominant baseline and
     * the outermost parent dominant baseline.
     * @return the offset in shift direction
     */
    private int getTotalBaselineOffset() {
        int offset = 0;
        if (parentAlignmentContext != null) {
            offset = getBaselineOffset() + parentAlignmentContext.getTotalBaselineOffset();
        }
        return offset;
    }

    /**
     * Return the offset between the alignment baseline and
     * the outermost parent dominant baseline.
     * @return the offset in shift direction
     */
    public int getTotalAlignmentBaselineOffset() {
        return getTotalAlignmentBaselineOffset(alignmentBaselineIdentifier);
    }

    /**
     * Return the offset between the given alignment baseline and
     * the outermost parent dominant baseline.
     * @param alignmentBaselineId the alignment baseline
     * @return the offset
     */
    private int getTotalAlignmentBaselineOffset(int alignmentBaselineId) {
        int offset = baselineShiftValue;
        if (parentAlignmentContext != null) {
            offset = parentAlignmentContext.getTotalBaselineOffset()
                    + parentAlignmentContext.getScaledBaselineTable()
                        .getBaseline(alignmentBaselineId)
                    + baselineShiftValue;
        }
        return offset;
    }

    /**
     * Return the offset between the dominant baseline and
     * the given actual baseline.
     *
     * @param baselineIdentifier the baseline
     * @return the offset
     */
    private int getActualBaselineOffset(int baselineIdentifier) {
        // This is the offset from the dominant baseline to the alignment baseline
        int offset = getTotalAlignmentBaselineOffset() - getTotalBaselineOffset();
        // Add the offset to the actual baseline we want
        offset += actualBaselineTable.deriveScaledBaselineTable(alignmentBaselineIdentifier)
                    .getBaseline(baselineIdentifier);
        return offset;
    }

    /**
     * Return the offset the outermost parent dominant baseline
     * and the top of this box.
     * @return the offset
     */
    private int getTotalTopOffset() {
        int offset = getTotalAlignmentBaselineOffset() + getAltitude();
        return offset;
    }

    /**
     * Return the total height of the context.
     * @return the height
     */
    public int getHeight() {
        return areaHeight;
    }

    /**
     * Return the line height of the context.
     * @return the height
     */
    private int getLineHeight() {
        return lineHeight;
    }

    /**
     * The altitude of the context that is the height above the
     * alignment point.
     * @return the altitude
     */
    public int getAltitude() {
        return alignmentPoint;
    }

    /**
     * The depth of the context that is the height below
     * alignment point.
     * @return the altitude
     */
    public int getDepth() {
        return getHeight() - alignmentPoint;
    }

    /**
     * The x height of the context.
     * @return the x height
     */
    private int getXHeight() {
        return this.xHeight;
    }

    /**
     * Resizes the line as specified. Assumes that the new alignment point
     * is on the dominant baseline, that is this function should be called for
     * line areas only.
     * @param newLineHeight the new height of the line
     * @param newAlignmentPoint the new alignment point
     */
    public void resizeLine(int newLineHeight, int newAlignmentPoint) {
        areaHeight = newLineHeight;
        alignmentPoint = newAlignmentPoint;
        scaledBaselineTable.setBeforeAndAfterBaselines(alignmentPoint
                                                        , alignmentPoint - areaHeight);
    }

    /**
     * Returns the offset from the before-edge of the parent to
     * this context.
     * @return the offset for rendering
     */
    public int getOffset() {
        int offset = 0;
        if (parentAlignmentContext != null) {
            offset = parentAlignmentContext.getTotalTopOffset() - getTotalTopOffset();
        } else {
            offset = getAltitude() - scaledBaselineTable.getBaseline(EN_TEXT_BEFORE_EDGE);
        }
        return offset;
    }

    /**
     * Returns an indication if we still use the initial baseline table.
     * The initial baseline table is the table generated by the Line LM.
     * @return true if this is still the initial baseline table
     */
    public boolean usesInitialBaselineTable() {
        return parentAlignmentContext == null
               || (scaledBaselineTable == parentAlignmentContext.getScaledBaselineTable()
                    && parentAlignmentContext.usesInitialBaselineTable());
    }

    /** {@inheritDoc} */
    public String toString() {
        StringBuffer sb = new StringBuffer(64);
        sb.append("areaHeight=").append(areaHeight);
        sb.append(" lineHeight=").append(lineHeight);
        sb.append(" alignmentPoint=").append(alignmentPoint);
        sb.append(" alignmentBaselineID=").append(alignmentBaselineIdentifier);
        sb.append(" baselineShift=").append(baselineShiftValue);
        return sb.toString();
    }

}
