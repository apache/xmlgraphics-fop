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

import org.apache.fop.fo.Constants;
import org.apache.fop.traits.WritingMode;


/**
 * The FOP specific incarnation of the XSL-FO scaled baseline table.
 * All baseline tables are scaled to the font size of the font they
 * apply to. This class uses a coordinate system with its origin
 * where the dominant baseline intersects the start edge of the box.
 * All measurements are in mpt.
 */
final class ScaledBaselineTable {

    private static final float HANGING_BASELINE_FACTOR = 0.8f;

    private static final float MATHEMATICAL_BASELINE_FACTOR = 0.5f;

    private final int altitude;

    private final int depth;

    private final int xHeight;

    private final int dominantBaselineIdentifier;

    private final WritingMode writingMode;

    private final int dominantBaselineOffset;

    private int beforeEdgeOffset;

    private int afterEdgeOffset;


    /**
     *
     * Creates a new instance of BasicScaledBaselineTable for the given
     * altitude, depth, xHeight, baseline and writing mode.
     * @param altitude the height of the box or the font ascender
     * @param depth the font descender or 0
     * @param xHeight the font xHeight
     * @param dominantBaselineIdentifier the dominant baseline given as an integer constant
     * @param writingMode the writing mode given as an integer constant
     */
    ScaledBaselineTable(int altitude,
            int depth,
            int xHeight,
            int dominantBaselineIdentifier,
            WritingMode writingMode) {
        this.altitude = altitude;
        this.depth = depth;
        this.xHeight = xHeight;
        this.dominantBaselineIdentifier = dominantBaselineIdentifier;
        this.writingMode = writingMode;
        this.dominantBaselineOffset = getBaselineDefaultOffset(this.dominantBaselineIdentifier);
        this.beforeEdgeOffset = altitude - dominantBaselineOffset;
        this.afterEdgeOffset = depth - dominantBaselineOffset;
    }

    /**
     * Return the dominant baseline for this baseline table.
     * @return the dominant baseline
     */
    int getDominantBaselineIdentifier() {
        return this.dominantBaselineIdentifier;
    }

    /**
     * Return the writing mode for this baseline table.
     * @return the writing mode
     */
    WritingMode getWritingMode() {
        return this.writingMode;
    }

    /**
     * Return the offset of the given baseline from the dominant baseline.
     *
     * @param baselineIdentifier a baseline identifier
     * @return the offset from the dominant baseline
     */
    int getBaseline(int baselineIdentifier) {
        int offset = 0;
        if (!isHorizontalWritingMode()) {
            switch (baselineIdentifier) {
                case Constants.EN_TOP:
                case Constants.EN_TEXT_TOP:
                case Constants.EN_TEXT_BOTTOM:
                case Constants.EN_BOTTOM:
                throw new IllegalArgumentException("Baseline " + baselineIdentifier
                        + " only supported for horizontal writing modes");
                default: // TODO
            }
        }
        switch (baselineIdentifier) {
            case Constants.EN_TOP: // fall through
            case Constants.EN_BEFORE_EDGE:
                offset = beforeEdgeOffset;
                break;
            case Constants.EN_TEXT_TOP:
            case Constants.EN_TEXT_BEFORE_EDGE:
            case Constants.EN_HANGING:
            case Constants.EN_CENTRAL:
            case Constants.EN_MIDDLE:
            case Constants.EN_MATHEMATICAL:
            case Constants.EN_ALPHABETIC:
            case Constants.EN_IDEOGRAPHIC:
            case Constants.EN_TEXT_BOTTOM:
            case Constants.EN_TEXT_AFTER_EDGE:
                offset = getBaselineDefaultOffset(baselineIdentifier) - dominantBaselineOffset;
                break;
            case Constants.EN_BOTTOM: // fall through
            case Constants.EN_AFTER_EDGE:
                offset = afterEdgeOffset;
                break;
            default: throw new IllegalArgumentException(String.valueOf(baselineIdentifier));
        }
        return offset;
    }

    private boolean isHorizontalWritingMode() {
        return writingMode.isHorizontal();
    }

    /**
     * Return the baseline offset measured from the font's default
     * baseline for the given baseline.
     * @param baselineIdentifier the baseline identifier
     * @return the baseline offset
     */
    private int getBaselineDefaultOffset(int baselineIdentifier) {
        int offset = 0;
        switch (baselineIdentifier) {
            case Constants.EN_TEXT_BEFORE_EDGE:
                offset = altitude;
                break;
            case Constants.EN_HANGING:
                offset = Math.round(altitude * HANGING_BASELINE_FACTOR);
                break;
            case Constants.EN_CENTRAL:
                offset = (altitude - depth) / 2 + depth;
                break;
            case Constants.EN_MIDDLE:
                offset = xHeight / 2;
                break;
            case Constants.EN_MATHEMATICAL:
                offset = Math.round(altitude * MATHEMATICAL_BASELINE_FACTOR);
                break;
            case Constants.EN_ALPHABETIC:
                offset = 0;
                break;
            case Constants.EN_IDEOGRAPHIC: // Fall through
            case Constants.EN_TEXT_AFTER_EDGE:
                offset = depth;
                break;
            default: throw new IllegalArgumentException(String.valueOf(baselineIdentifier));
        }
        return offset;
    }

    /**
     * Sets the position of the before and after baselines.
     * This is usually only done for line areas. For other
     * areas the position of the before and after baselines
     * are fixed when the table is constructed.
     * @param beforeBaseline the offset of the before-edge baseline from the dominant baseline
     * @param afterBaseline the offset of the after-edge baseline from the dominant baseline
     */
    void setBeforeAndAfterBaselines(int beforeBaseline, int afterBaseline) {
        beforeEdgeOffset = beforeBaseline;
        afterEdgeOffset = afterBaseline;
    }

    /**
     * Return a new baseline table for the given baseline based
     * on the current baseline table.
     * @param baselineIdentifier the baseline identifer
     * @return a new baseline with the new baseline
     */
    ScaledBaselineTable deriveScaledBaselineTable(int baselineIdentifier) {
        ScaledBaselineTable bac = new ScaledBaselineTable(altitude, depth, xHeight,
                baselineIdentifier, this.writingMode);
        return bac;
    }

}
