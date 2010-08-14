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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.fo.Constants;


/**
 * An implementation of the ScaledBaselineTable interface which calculates
 * all baselines given the height above and below the dominant baseline.
 */
public class BasicScaledBaselineTable implements ScaledBaselineTable, Constants {

    /** A logger for this class */
    protected Log log = LogFactory.getLog(BasicScaledBaselineTable.class);

    private int altitude;
    private int depth;
    private int xHeight;
    private int dominantBaselineIdentifier;
    private int writingMode;
    private int dominantBaselineOffset;
    private int beforeEdgeOffset;
    private int afterEdgeOffset;

    private static final float HANGING_BASELINE_FACTOR = 0.8f;
    private static final float MATHEMATICAL_BASELINE_FACTOR = 0.5f;

    /**
     *
     * Creates a new instance of BasicScaledBaselineTable for the given
     * altitude, depth, xHeight, baseline and writingmode.
     * @param altitude the height of the box or the font ascender
     * @param depth the font descender or 0
     * @param xHeight the font xHeight
     * @param dominantBaselineIdentifier the dominant baseline given as an integer constant
     * @param writingMode the writing mode given as an integer constant
     */
    public BasicScaledBaselineTable(int altitude
                                    , int depth
                                    , int xHeight
                                    , int dominantBaselineIdentifier
                                    , int writingMode) {
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
    public int getDominantBaselineIdentifier() {
        return this.dominantBaselineIdentifier;
    }

    /**
     * Return the writing mode for this baseline table.
     * @return the writing mode
     */
    public int getWritingMode() {
        return this.writingMode;
    }

    /**
     * Return the baseline offset measured from the dominant
     * baseline for the given baseline.
     * @param baselineIdentifier the baseline identifier
     * @return the baseline offset
     */
    public int getBaseline(int baselineIdentifier) {
        int offset;
        if (!isHorizontalWritingMode()) {
            switch (baselineIdentifier) {
                default:
                case EN_TOP:
                case EN_TEXT_TOP:
                case EN_TEXT_BOTTOM:
                case EN_BOTTOM:
                    log.warn("The given baseline is only supported for horizontal"
                        + " writing modes");
                    offset = 0;
            }
        } else {
            switch (baselineIdentifier) {
                case EN_TOP: // fall through
                case EN_BEFORE_EDGE:
                    offset = beforeEdgeOffset;
                    break;
                case EN_TEXT_TOP:
                case EN_TEXT_BEFORE_EDGE:
                case EN_HANGING:
                case EN_CENTRAL:
                case EN_MIDDLE:
                case EN_MATHEMATICAL:
                case EN_ALPHABETIC:
                case EN_IDEOGRAPHIC:
                case EN_TEXT_BOTTOM:
                case EN_TEXT_AFTER_EDGE:
                    offset = getBaselineDefaultOffset(baselineIdentifier) - dominantBaselineOffset;
                    break;
                case EN_BOTTOM: // fall through
                case EN_AFTER_EDGE:
                    offset = afterEdgeOffset;
                    break;
                default:
                    offset = 0;
                    break;
            }
        }
        return offset;
    }

    private boolean isHorizontalWritingMode() {
        return writingMode == EN_LR_TB || writingMode == EN_RL_TB;
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
            case EN_TEXT_BEFORE_EDGE:
                offset = altitude;
                break;
            case EN_HANGING:
                offset = (int)Math.round(altitude * HANGING_BASELINE_FACTOR);
                break;
            case EN_CENTRAL:
                offset = (altitude - depth) / 2 + depth;
                break;
            case EN_MIDDLE:
                offset = xHeight / 2;
                break;
            case EN_MATHEMATICAL:
                offset = (int)Math.round(altitude * MATHEMATICAL_BASELINE_FACTOR);
                break;
            case EN_ALPHABETIC:
                offset = 0;
                break;
            case EN_IDEOGRAPHIC: // Fall through
            case EN_TEXT_AFTER_EDGE:
                offset = depth;
                break;
            default:
                break;
        }
        return offset;
    }

    /**
     * {@inheritDoc}
     */
    public void setBeforeAndAfterBaselines(int beforeBaseline, int afterBaseline) {
        beforeEdgeOffset = beforeBaseline;
        afterEdgeOffset = afterBaseline;
    }

    /**
     * {@inheritDoc}
     */
    public ScaledBaselineTable deriveScaledBaselineTable(int baselineIdentifier) {
        BasicScaledBaselineTable bac
            = new BasicScaledBaselineTable(altitude, depth, xHeight
                                            , baselineIdentifier, this.writingMode);
        return bac;
    }

}
