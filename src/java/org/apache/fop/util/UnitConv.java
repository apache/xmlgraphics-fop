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

package org.apache.fop.util;

/**
 * Utility class for unit conversions.
 */
public final class UnitConv {

    /** conversion factory from millimeters to inches. */
    public static final float IN2MM = 25.4f;
    
    /** conversion factory from centimeters to inches. */
    public static final float IN2CM = 2.54f;
    
    /** conversion factory from inches to points. */
    public static final int IN2PT = 72;
    
    /**
     * Converts millimeters (mm) to points (pt)
     * @param mm the value in mm
     * @return the value in pt
     */
    public static double mm2pt(double mm) {
        return mm * IN2PT / IN2MM;
    }

    /**
     * Converts millimeters (mm) to millipoints (mpt)
     * @param mm the value in mm
     * @return the value in mpt
     */
    public static double mm2mpt(double mm) {
        return mm * 1000 * IN2PT / IN2MM;
    }

    /**
     * Converts points (pt) to millimeters (mm)
     * @param pt the value in pt
     * @return the value in mm
     */
    public static double pt2mm(double pt) {
        return pt * IN2MM / IN2PT;
    }
    
    /**
     * Converts millimeters (mm) to inches (in)
     * @param mm the value in mm
     * @return the value in inches
     */
    public static double mm2in(double mm) {
        return mm / IN2MM;
    }
    
    /**
     * Converts inches (in) to millimeters (mm)
     * @param in the value in inches
     * @return the value in mm
     */
    public static double in2mm(double in) {
        return in * IN2MM;
    }
    
    /**
     * Converts inches (in) to millipoints (mpt)
     * @param in the value in inches
     * @return the value in mpt
     */
    public static double in2mpt(double in) {
        return in * IN2PT * 1000;
    }
    
    /**
     * Converts inches (in) to points (pt)
     * @param in the value in inches
     * @return the value in pt
     */
    public static double in2pt(double in) {
        return in * IN2PT;
    }
    
    /**
     * Converts millipoints (mpt) to inches (in) 
     * @param mpt the value in mpt
     * @return the value in inches
     */
    public static double mpt2in(double mpt) {
        return mpt / IN2PT / 1000;
    }
    
    /**
     * Converts millimeters (mm) to pixels (px)
     * @param mm the value in mm
     * @param resolution the resolution in dpi (dots per inch)
     * @return the value in pixels
     */
    public static double mm2px(double mm, int resolution) {
        return mm2in(mm) * resolution;
    }

    /**
     * Converts millipoints (mpt) to pixels (px)
     * @param mpt the value in mpt
     * @param resolution the resolution in dpi (dots per inch)
     * @return the value in pixels
     */
    public static double mpt2px(double mpt, int resolution) {
        return mpt2in(mpt) * resolution;
    }

}
