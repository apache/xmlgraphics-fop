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

package org.apache.fop.render.svg;

import java.awt.geom.AffineTransform;

import org.apache.fop.render.intermediate.IFUtil;

/**
 * This class provides utility methods for generating SVG.
 */
public class SVGUtil {

    /**
     * Formats a length in millipoints as a point value.
     * @param mpt the length in millipoints
     * @return the formatted value in points
     */
    public static String formatMptToPt(int mpt) {
        return Float.toString(mpt / 1000f);
    }

    /**
     * Formats an array of lengths in millipoints as point values.
     * @param lengths the lengths in millipoints
     * @return the formatted array in points
     */
    public static String formatMptArrayToPt(int[] lengths) {
        return IFUtil.toString(lengths);
    }

    /**
     * Formats a transformation matrix in millipoints with values as points.
     * @param transform the transformation matrix in millipoints
     * @return the formatted matrix in points
     */
    public static String formatAffineTransformMptToPt(AffineTransform transform) {
        AffineTransform scaled = new AffineTransform(transform);
        scaled.setToTranslation(
                transform.getTranslateX() / 1000,
                transform.getTranslateY() / 1000);
        return IFUtil.toString(scaled);
    }

    /**
     * Formats an array of transformation matrices in millipoints with values as points.
     * @param transforms the transformation matrices in millipoints
     * @return the formatted matrices in points
     */
    public static String formatAffineTransformsMptToPt(AffineTransform[] transforms) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0, c = transforms.length; i < c; i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(formatAffineTransformMptToPt(transforms[i]));
        }
        return sb.toString();
    }

}
