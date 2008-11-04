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

package org.apache.fop.afp;

import java.awt.geom.AffineTransform;



/**
 * AFP Unit converter
 */
public class AFPUnitConverter {

    /** the AFP state */
    private final AFPPaintingState state;

    /**
     * Unit converter
     *
     * @param state the AFP painting state
     */
    public AFPUnitConverter(AFPPaintingState state) {
        this.state = state;
    }

    /**
     * Converts millipoints to units
     *
     * @param srcPts source points
     * @param dstPts destination points
     * @return transformed points
     */
    public int[] mpts2units(float[] srcPts, float[] dstPts) {
        return transformPoints(srcPts, dstPts, true);
    }

    /**
     * Converts points to units
     *
     * @param srcPts source points
     * @param dstPts destination points
     * @return transformed points
     */
    public int[] pts2units(float[] srcPts, float[] dstPts) {
        return transformPoints(srcPts, dstPts, false);
    }

    /**
     * Converts millipoints to units
     *
     * @param srcPts source points
     * @return transformed points
     */
    public int[] mpts2units(float[] srcPts) {
        return transformPoints(srcPts, null, true);
    }

    /**
     * Converts points to units
     *
     * @param srcPts source points
     * @return transformed points
     */
    public int[] pts2units(float[] srcPts) {
        return transformPoints(srcPts, null, false);
    }

    /**
     * Converts point to unit
     *
     * @param pt point
     * @return transformed point
     */
    public float pt2units(float pt) {
        return pt / ((float)AFPConstants.DPI_72 / state.getResolution());
    }

    /**
     * Converts millipoint to unit
     *
     * @param mpt millipoint
     * @return transformed point
     */
    public float mpt2units(float mpt) {
        return mpt / ((float)AFPConstants.DPI_72_MPTS / state.getResolution());
    }

    private int[] transformPoints(float[] srcPts, float[] dstPts, boolean milli) {
        if (dstPts == null) {
            dstPts = new float[srcPts.length];
        }
        AffineTransform at = state.getData().getTransform();
        at.transform(srcPts, 0, dstPts, 0, srcPts.length / 2);
        int[] coords = new int[srcPts.length];
        for (int i = 0; i < srcPts.length; i++) {
            if (!milli) {
                dstPts[i] *= 1000;
            }
            coords[i] = Math.round(dstPts[i]);
        }
        return coords;
    }

}
