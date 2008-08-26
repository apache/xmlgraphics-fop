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

/* $Id: $ */

package org.apache.fop.util;

import java.awt.geom.AffineTransform;

/**
 * Utility class for unit conversions.
 * @deprecated use org.apache.xmlgraphics.util.UnitConv instead.
 */
public final class UnitConv {

    /**
     * Converts millimeters (mm) to points (pt)
     * @param mm the value in mm
     * @return the value in pt
     * @deprecated use org.apache.xmlgraphics.util.UnitConv.mm2pt(mm) instead.
     */
    public static double mm2pt(double mm) {
        return org.apache.xmlgraphics.util.UnitConv.mm2pt(mm);
    }

    /**
     * Converts millimeters (mm) to millipoints (mpt)
     * @param mm the value in mm
     * @return the value in mpt
     * @deprecated use org.apache.xmlgraphics.util.UnitConv.mm2mpt(mm) instead.
     */
    public static double mm2mpt(double mm) {
        return org.apache.xmlgraphics.util.UnitConv.mm2mpt(mm);
    }

    /**
     * Converts points (pt) to millimeters (mm)
     * @param pt the value in pt
     * @return the value in mm
     * @deprecated use org.apache.xmlgraphics.util.UnitConv.pt2mm(pt) instead.
     */
    public static double pt2mm(double pt) {
        return org.apache.xmlgraphics.util.UnitConv.pt2mm(pt);
    }

    /**
     * Converts millimeters (mm) to inches (in)
     * @param mm the value in mm
     * @return the value in inches
     * @deprecated use org.apache.xmlgraphics.util.UnitConv.pt2mm(pt) instead.
     */
    public static double mm2in(double mm) {
        return org.apache.xmlgraphics.util.UnitConv.mm2in(mm);
    }

    /**
     * Converts inches (in) to millimeters (mm)
     * @param in the value in inches
     * @return the value in mm
     * @deprecated use org.apache.xmlgraphics.util.UnitConv.in2mm(in) instead.
     */
    public static double in2mm(double in) {
        return org.apache.xmlgraphics.util.UnitConv.in2mm(in);
    }

    /**
     * Converts inches (in) to millipoints (mpt)
     * @param in the value in inches
     * @return the value in mpt
     * @deprecated use org.apache.xmlgraphics.util.UnitConv.in2mpt(in) instead.
     */
    public static double in2mpt(double in) {
        return org.apache.xmlgraphics.util.UnitConv.in2mpt(in);
    }

    /**
     * Converts inches (in) to points (pt)
     * @param in the value in inches
     * @return the value in pt
     * @deprecated use org.apache.xmlgraphics.util.UnitConv.in2pt(in) instead.
     */
    public static double in2pt(double in) {
        return org.apache.xmlgraphics.util.UnitConv.in2pt(in);
    }

    /**
     * Converts millipoints (mpt) to inches (in)
     * @param mpt the value in mpt
     * @return the value in inches
     * @deprecated use org.apache.xmlgraphics.util.UnitConv.mpt2in(mpt) instead.
     */
    public static double mpt2in(double mpt) {
        return org.apache.xmlgraphics.util.UnitConv.mpt2in(mpt);
    }

    /**
     * Converts millimeters (mm) to pixels (px)
     * @param mm the value in mm
     * @param resolution the resolution in dpi (dots per inch)
     * @return the value in pixels
     * @deprecated use org.apache.xmlgraphics.util.UnitConv.mm2px(mm, resolution) instead.
     */
    public static double mm2px(double mm, int resolution) {
        return org.apache.xmlgraphics.util.UnitConv.mm2px(mm, resolution);
    }

    /**
     * Converts millipoints (mpt) to pixels (px)
     * @param mpt the value in mpt
     * @param resolution the resolution in dpi (dots per inch)
     * @return the value in pixels
     * @deprecated use org.apache.xmlgraphics.util.UnitConv.mpt2px(mpt, resolution) instead.
     */
    public static double mpt2px(double mpt, int resolution) {
        return org.apache.xmlgraphics.util.UnitConv.mpt2px(mpt, resolution);
    }

    /**
     * Converts a millipoint-based transformation matrix to points.
     * @param at a millipoint-based transformation matrix
     * @return a point-based transformation matrix
     * @deprecated use org.apache.xmlgraphics.util.UnitConv.mptToPt(at) instead.
     */
    public static AffineTransform mptToPt(AffineTransform at) {
        return org.apache.xmlgraphics.util.UnitConv.mptToPt(at);
    }

    /**
     * Converts a point-based transformation matrix to millipoints.
     * @param at a point-based transformation matrix
     * @return a millipoint-based transformation matrix
     * @deprecated use org.apache.xmlgraphics.util.UnitConv.ptToMpt(at) instead.
     */
    public static AffineTransform ptToMpt(AffineTransform at) {
        return org.apache.xmlgraphics.util.UnitConv.ptToMpt(at);
    }

}
