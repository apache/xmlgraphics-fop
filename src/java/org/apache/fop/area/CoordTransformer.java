/*
 * $Id$
 *
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.apache.fop.area;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

import org.apache.fop.fo.properties.WritingMode;

/**
 * Describe a PDF or PostScript style coordinate transformation matrix.
 * The matrix encodes translations, scaling and rotations of the coordinate
 * system used to render pages.
 */
public class CoordTransformer implements Serializable {

    private double a, b, c, d, e, f;

    private static final CoordTransformer CT_LRTB =
        new CoordTransformer(1, 0, 0, 1, 0, 0);
    private static final CoordTransformer CT_RLTB =
        new CoordTransformer(-1, 0, 0, 1, 0, 0);
    private static final CoordTransformer CT_TBRL =
        new CoordTransformer(0, 1, -1, 0, 0, 0);

    /**
     * Create the identity matrix
     */
    public CoordTransformer() {
        a = 1;
        b = 0;
        c = 0;
        d = 1;
        e = 0;
        f = 0;
    }

    /**
     * Initialize a CoordTransformer from the passed arguments.
     *
     * @param a the x scale
     * @param b the x shear
     * @param c the y shear
     * @param d the y scale
     * @param e the x shift
     * @param f the y shift
     */
    public CoordTransformer(
            double a, double b, double c, double d, double e, double f) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.e = e;
        this.f = f;
    }

    /**
     * Initialize a CoordTransformer to the identity matrix with a translation
     * specified by x and y
     *
     * @param x the x shift
     * @param y the y shift.
     */
    public CoordTransformer(double x, double y) {
        this.a = 1;
        this.b = 0;
        this.c = 0;
        this.d = 1;
        this.e = x;
        this.f = y;
    }

    /**
     * Initialize a CoordTransformer with the values of another CoordTransformer.
     *
     * @param ct another CoordTransformer
     */
    protected CoordTransformer(CoordTransformer ct) {
        this.a = ct.a;
        this.b = ct.b;
        this.c = ct.c;
        this.d = ct.d;
        this.e = ct.e;
        this.f = ct.f;
    }

    /**
     * Return a CoordTransformer which will transform coordinates for a
     * particular writing-mode into normalized first quandrant coordinates.
     * @param wm A writing mode constant from fo.properties.WritingMode, ie.
     * one of LR_TB, RL_TB, TB_RL.
     * @param ipd The inline-progression dimension of the reference area whose
     * CoordTransformer is being set..
     * @param bpd The block-progression dimension of the reference area whose
     * CoordTransformer is being set.
     * @return a new CoordTransformer with the required transform
     */
    public static CoordTransformer getWMct(int wm, int ipd, int bpd) {
        CoordTransformer wmct;
        switch (wm) {
            case WritingMode.LR_TB:
                return new CoordTransformer(CT_LRTB);
            case WritingMode.RL_TB: {
                    wmct = new CoordTransformer(CT_RLTB);
                    wmct.e = ipd;
                    return wmct;
                }
                //return  CT_RLTB.translate(ipd, 0);
            case WritingMode.TB_RL: { // CJK
                    wmct = new CoordTransformer(CT_TBRL);
                    wmct.e = bpd;
                    return wmct;
                }
                //return CT_TBRL.translate(0, ipd);
            default:
                return null;
        }
    }

    /**
     * Multiply new passed CoordTransformer with this one and generate a new
     * result CoordTransformer.
     * @param premult The CoordTransformer to multiply with this one.
     *  The new one will be the first multiplicand.
     * @return CoordTransformer The result of multiplying premult * this.
     */
    public CoordTransformer multiply(CoordTransformer premult) {
        CoordTransformer rslt = 
            new CoordTransformer ((premult.a * a) + (premult.b * c),
                            (premult.a * b) + (premult.b * d),
                            (premult.c * a) + (premult.d * c),
                            (premult.c * b) + (premult.d * d),
                            (premult.e * a) + (premult.f * c) + e,
                            (premult.e * b) + (premult.f * d) + f);
        return rslt;
    }

    /**
     * Rotate this CoordTransformer by "angle" radians and return a new result
     * CoordTransformer.  This is used to account for reference-orientation.
     * @param angle The angle in radians.
     * Positive angles are measured counter-clockwise.
     * @return CoordTransformer The result of rotating this CoordTransformer.
     */
    public CoordTransformer rotate(double angle) {
        double cos, sin;
        if (angle == 90.0) {
            cos = 0.0;
            sin = 1.0;
        } else if (angle == 270.0) {
            cos = 0.0;
            sin = -1.0;
        } else if (angle == 180.0) {
            cos = -1.0;
            sin = 0.0;
        } else {
            double rad = Math.toRadians(angle);
            cos = Math.cos(rad);
            sin = Math.sin(rad);
        }
        CoordTransformer rotate = new CoordTransformer(cos, -sin, sin, cos, 0, 0);
        return multiply(rotate);
    }

    /**
     * Translate this CoordTransformer by the passed x and y values and return
     * a new result CoordTransformer.
     * @param x The amount to translate along the x axis.
     * @param y The amount to translate along the y axis.
     * @return CoordTransformer The result of translating this CoordTransformer.
     */
    public CoordTransformer translate(double x, double y) {
        CoordTransformer translate = new CoordTransformer(1, 0, 0, 1, x, y);
        return multiply(translate);
    }

    /**
     * Scale this CoordTransformer by the passed x and y values and return
     * a new result CoordTransformer.
     * @param x The amount to scale along the x axis.
     * @param y The amount to scale along the y axis.
     * @return CoordTransformer The result of scaling this CoordTransformer.
     */
    public CoordTransformer scale(double x, double y) {
        CoordTransformer scale = new CoordTransformer(x, 0, 0, y, 0, 0);
        return multiply(scale);
    }

    /**
     * Transform a rectangle by the CoordTransformer to produce a rectangle in
     * the transformed coordinate system.
     * @param inRect The rectangle in the original coordinate system
     * @return Rectangle2D The rectangle in the transformed coordinate system.
     */
    public Rectangle2D transform(Rectangle2D inRect) {
        // Store as 2 sets of 2 points and transform those, then
        // recalculate the width and height
        int x1t = (int)(inRect.getX() * a + inRect.getY() * c + e);
        int y1t = (int)(inRect.getX() * b + inRect.getY() * d + f);
        int x2t = (int)((inRect.getX() + inRect.getWidth()) * a
                        + (inRect.getY() + inRect.getHeight()) * c + e);
        int y2t = (int)((inRect.getX() + inRect.getWidth()) * b
                        + (inRect.getY() + inRect.getHeight()) * d + f);
        // Normalize with x1 < x2
        if (x1t > x2t) {
            int tmp = x2t;
            x2t = x1t;
            x1t = tmp;
        }
        if (y1t > y2t) {
            int tmp = y2t;
            y2t = y1t;
            y1t = tmp;
        }
        return new Rectangle(x1t, y1t, x2t - x1t, y2t - y1t);
    }

    /**
     * Get string for this transform.
     *
     * @return a string with the transform values
     */
    public String toString() {
        return "[" + a + " " + b + " " + c + " " + d + " " + e + " "
               + f + "]";
    }

    /**
     * Get an array containing the values of this transform.
     * This creates and returns a new transform with the values in it.
     *
     * @return an array containing the transform values
     */
    public double[] toArray() {
        return new double[]{a, b, c, d, e, f};
    }

    /**
     * Construct a coordinate transformation matrix.
     * @param absRefOrient
     * @param writingMode
     * @param absVPrect absolute viewpoint rectangle
     * @param relBPDim the relative block progression dimension
     * @param relIPDim the relative inline progression dimension
     * @return CoordTransformer the coordinate transformation matrix
     */
    public static CoordTransformer getMatrixandRelDims(int absRefOrient,
                                       int writingMode,
                                       Rectangle2D absVPrect,
                                       int relBPDim, int relIPDim) {
        int width, height;
        // We will use the absolute reference-orientation to set up the
        // CoordTransformer.
        // The value here is relative to its ancestor reference area.
        if (absRefOrient % 180 == 0) {
            width = (int) absVPrect.getWidth();
            height = (int) absVPrect.getHeight();
        } else {
            // invert width and height since top left are rotated by 90
            // (cl or ccl)
            height = (int) absVPrect.getWidth();
            width = (int) absVPrect.getHeight();
        }
        /* Set up the CoordTransformer for the content of this reference area.
         * This will transform region content coordinates in
         * writing-mode relative into absolute page-relative
         * which will then be translated based on the position of
         * the region viewport.
         * (Note: scrolling between region vp and ref area when
         * doing online content!)
         */
        CoordTransformer ct = 
            new CoordTransformer(absVPrect.getX(), absVPrect.getY());

        // First transform for rotation
        if (absRefOrient != 0) {
            // Rotation implies translation to keep the drawing area in the
            // first quadrant. Note: rotation is counter-clockwise
            switch (absRefOrient) {
                case 90:
                    ct = ct.translate(0, width); // width = absVPrect.height
                    break;
                case 180:
                    ct = ct.translate(width, height);
                    break;
                case 270:
                    ct = ct.translate(height, 0); // height = absVPrect.width
                    break;
            }
            ct = ct.rotate(absRefOrient);
        }
        /* Since we've already put adjusted width and height values for the
         * top and left positions implied by the reference-orientation, we
         * can set ipd and bpd appropriately based on the writing mode.
         */

        if (writingMode == WritingMode.LR_TB
                || writingMode == WritingMode.RL_TB) {
            relIPDim = width;
            relBPDim = height;
        } else {
            relIPDim = height;
            relBPDim = width;
        }
        // Set a rectangle to be the writing-mode relative version???
        // Now transform for writing mode
        return ct.multiply(
                CoordTransformer.getWMct(writingMode, relIPDim, relBPDim));
    }

}
