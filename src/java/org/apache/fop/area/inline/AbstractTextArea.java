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

package org.apache.fop.area.inline;

/**
 * Abstract base class for both TextArea and Character.
 */
public abstract class AbstractTextArea extends InlineParent {

    /**
     * this class stores information about spaces and potential adjustments
     * that can be used in order to re-compute adjustments when a
     * page-number or a page-number-citation is resolved
     */
    protected class TextAdjustingInfo extends InlineAdjustingInfo {

        /** difference between the optimal width of a space
         * and the default width of a space according to the font
         * (this is equivalent to the property word-spacing.optimum)
         */
        protected int spaceDifference = 0;

        /**
         * Constructor
         *
         * @param stretch the available space for stretching
         * @param shrink the available space for shrinking
         * @param adj space adjustment type
         */
        protected TextAdjustingInfo(int stretch, int shrink, int adj) {
            super(stretch, shrink, adj);
        }
    }

    private int textWordSpaceAdjust = 0;
    private int textLetterSpaceAdjust = 0;
    private TextAdjustingInfo textAdjustingInfo = null;
    private int baselineOffset = 0;

    /**
     * Default constructor
     */
    public AbstractTextArea() {
    }

    /**
     * Constructor with extra parameters:
     * create a TextAdjustingInfo object
     * @param stretch  the available stretch of the text
     * @param shrink   the available shrink of the text
     * @param adj      the current adjustment of the area
     */
    public AbstractTextArea(int stretch, int shrink, int adj) {
        textAdjustingInfo = new TextAdjustingInfo(stretch, shrink, adj);
    }

    /**
     * Get text word space adjust.
     *
     * @return the text word space adjustment
     */
    public int getTextWordSpaceAdjust() {
        return textWordSpaceAdjust;
    }

    /**
     * Set text word space adjust.
     *
     * @param textWordSpaceAdjust the text word space adjustment
     */
    public void setTextWordSpaceAdjust(int textWordSpaceAdjust) {
        this.textWordSpaceAdjust = textWordSpaceAdjust;
    }

    /**
     * Get text letter space adjust.
     *
     * @return the text letter space adjustment
     */
    public int getTextLetterSpaceAdjust() {
        return textLetterSpaceAdjust;
    }

    /**
     * Set text letter space adjust.
     *
     * @param textLetterSpaceAdjust the text letter space adjustment
     */
    public void setTextLetterSpaceAdjust(int textLetterSpaceAdjust) {
        this.textLetterSpaceAdjust = textLetterSpaceAdjust;
    }

    /**
     * Set the difference between optimal width of a space and
     * default width of a space according to the font; this part
     * of the space adjustment is fixed and must not be
     * multiplied by the variation factor.
     * @param spaceDiff the space difference
     */
    public void setSpaceDifference(int spaceDiff) {
        textAdjustingInfo.spaceDifference = spaceDiff;
    }

    /**
     * recursively apply the variation factor to all descendant areas
     * @param variationFactor the variation factor that must be applied to adjustments
     * @param lineStretch     the total stretch of the line
     * @param lineShrink      the total shrink of the line
     * @return true if there is an UnresolvedArea descendant
     */
    public boolean applyVariationFactor(double variationFactor,
                                        int lineStretch, int lineShrink) {
        if (textAdjustingInfo != null) {
            // compute the new adjustments:
            // if the variation factor is negative, it means that before
            // the ipd variation the line had to stretch and now it has
            // to shrink (or vice versa);
            // in this case, if the stretch and shrink are not equally
            // divided among the inline areas, we must compute a
            // balancing factor
            double balancingFactor = 1.0;
            if (variationFactor < 0) {
                if (textWordSpaceAdjust < 0) {
                    // from a negative adjustment to a positive one
                    balancingFactor
                        = ((double) textAdjustingInfo.availableStretch
                           / textAdjustingInfo.availableShrink)
                            * ((double) lineShrink / lineStretch);
                } else {
                    // from a positive adjustment to a negative one
                    balancingFactor
                        = ((double) textAdjustingInfo.availableShrink
                           / textAdjustingInfo.availableStretch)
                            * ((double) lineStretch / lineShrink);
                }
            }
            textWordSpaceAdjust = (int) ((textWordSpaceAdjust - textAdjustingInfo.spaceDifference)
                    * variationFactor * balancingFactor)
                    + textAdjustingInfo.spaceDifference;
            textLetterSpaceAdjust *= variationFactor;
            // update the ipd of the area
            int oldAdjustment = textAdjustingInfo.adjustment;
            textAdjustingInfo.adjustment *= balancingFactor * variationFactor;
            ipd += textAdjustingInfo.adjustment - oldAdjustment;
        }
        return false;
    }

    /**
     * Get baseline offset, i.e. the distance from the before edge
     * of this area to the nominal baseline.
     *
     * @return the baseline offset
     */
    public int getBaselineOffset() {
        return baselineOffset;
    }

    /**
     * Set the baseline offset.
     *
     * @param baselineOffset the baseline offset
     */
    public void setBaselineOffset(int baselineOffset) {
        this.baselineOffset = baselineOffset;
    }
}
