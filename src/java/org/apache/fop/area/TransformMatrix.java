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
public class TransformMatrix implements Serializable {

    private double a, b, c, d, e, f;

    private static final TransformMatrix TM_LRTB =
        new TransformMatrix(1, 0, 0, 1, 0, 0);
    private static final TransformMatrix TM_RLTB =
        new TransformMatrix(-1, 0, 0, 1, 0, 0);
    private static final TransformMatrix TM_TBRL =
        new TransformMatrix(0, 1, -1, 0, 0, 0);

    /**
     * Create the identity matrix
     */
    public TransformMatrix() {
        a = 1;
        b = 0;
        c = 0;
        d = 1;
        e = 0;
        f = 0;
    }

    /**
     * Initialize a TransformMatrix from the passed arguments.
     *
     * @param a the x scale
     * @param b the x shear
     * @param c the y shear
     * @param d the y scale
     * @param e the x shift
     * @param f the y shift
     */
    public TransformMatrix(
            double a, double b, double c, double d, double e, double f) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.e = e;
        this.f = f;
    }

    /**
     * Initialize a TransformMatrix to the identity matrix with a translation
     * specified by x and y
     *
     * @param x the x shift
     * @param y the y shift.
     */
    public TransformMatrix(double x, double y) {
        this.a = 1;
        this.b = 0;
        this.c = 0;
        this.d = 1;
        this.e = x;
        this.f = y;
    }

    /**
     * Initialize a TransformMatrix with the values of another TransformMatrix.
     *
     * @param tm another TransformMatrix
     */
    protected TransformMatrix(TransformMatrix tm) {
        this.a = tm.a;
        this.b = tm.b;
        this.c = tm.c;
        this.d = tm.d;
        this.e = tm.e;
        this.f = tm.f;
    }

    /**
     * Return a TransformMatrix which will transform coordinates for a
     * particular writing-mode into normalized first quandrant coordinates.
     * @param wm A writing mode constant from fo.properties.WritingMode, ie.
     * one of LR_TB, RL_TB, TB_RL.
     * @param ipd The inline-progression dimension of the reference area whose
     * TransformMatrix is being set..
     * @param bpd The block-progression dimension of the reference area whose
     * TransformMatrix is being set.
     * @return a new TransformMatrix with the required transform
     */
    public static TransformMatrix getWMtm(int wm, int ipd, int bpd) {
        TransformMatrix wmtm;
        switch (wm) {
            case WritingMode.LR_TB:
                return new TransformMatrix(TM_LRTB);
            case WritingMode.RL_TB: {
                    wmtm = new TransformMatrix(TM_RLTB);
                    wmtm.e = ipd;
                    return wmtm;
                }
                //return  TM_RLTB.translate(ipd, 0);
            case WritingMode.TB_RL: { // CJK
                    wmtm = new TransformMatrix(TM_TBRL);
                    wmtm.e = bpd;
                    return wmtm;
                }
                //return TM_TBRL.translate(0, ipd);
            default:
                return null;
        }
    }

    /**
     * Multiply new passed TransformMatrix with this one and generate a new
     * result TransformMatrix.
     * @param premult The TransformMatrix to multiply with this one.
     *  The new one will be the first multiplicand.
     * @return TransformMatrix The result of multiplying premult * this.
     */
    public TransformMatrix multiply(TransformMatrix premult) {
        TransformMatrix rslt = 
            new TransformMatrix ((premult.a * a) + (premult.b * c),
                            (premult.a * b) + (premult.b * d),
                            (premult.c * a) + (premult.d * c),
                            (premult.c * b) + (premult.d * d),
                            (premult.e * a) + (premult.f * c) + e,
                            (premult.e * b) + (premult.f * d) + f);
        return rslt;
    }

    /**
     * Rotate this TransformMatrix by "angle" radians and return a new result
     * TransformMatrix.  This is used to account for reference-orientation.
     * @param angle The angle in radians.
     * Positive angles are measured counter-clockwise.
     * @return TransformMatrix The result of rotating this TransformMatrix.
     */
    public TransformMatrix rotate(double angle) {
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
        TransformMatrix rotate = new TransformMatrix(cos, -sin, sin, cos, 0, 0);
        return multiply(rotate);
    }

    /**
     * Translate this TransformMatrix by the passed x and y values and return
     * a new result TransformMatrix.
     * @param x The amount to translate along the x axis.
     * @param y The amount to translate along the y axis.
     * @return TransformMatrix The result of translating this TransformMatrix.
     */
    public TransformMatrix translate(double x, double y) {
        TransformMatrix translate = new TransformMatrix(1, 0, 0, 1, x, y);
        return multiply(translate);
    }

    /**
     * Scale this TransformMatrix by the passed x and y values and return
     * a new result TransformMatrix.
     * @param x The amount to scale along the x axis.
     * @param y The amount to scale along the y axis.
     * @return TransformMatrix The result of scaling this TransformMatrix.
     */
    public TransformMatrix scale(double x, double y) {
        TransformMatrix scale = new TransformMatrix(x, 0, 0, y, 0, 0);
        return multiply(scale);
    }

    /**
     * Transform a rectangle by the TransformMatrix to produce a rectangle in
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
     * @param absRefOrient
     * @param writingMode
     * @param absVPrect
     * @return
     */
    /**
     * Construct a coordinate transformation matrix.
     * @param absVPrect absolute viewpoint rectangle
     * @param relBPDim the relative block progression dimension
     * @param relIPDim the relative inline progression dimension
     * @return TransformMatrix the coordinate transformation matrix
     */
    public static TransformMatrix getMatrixandRelDims(int absRefOrient,
                                       int writingMode,
                                       Rectangle2D absVPrect,
                                       int relBPDim, int relIPDim) {
        int width, height;
        // We will use the absolute reference-orientation to set up the
        // TransformMatrix.
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
        /* Set up the TransformMatrix for the content of this reference area.
         * This will transform region content coordinates in
         * writing-mode relative into absolute page-relative
         * which will then be translated based on the position of
         * the region viewport.
         * (Note: scrolling between region vp and ref area when
         * doing online content!)
         */
        TransformMatrix tm = 
            new TransformMatrix(absVPrect.getX(), absVPrect.getY());

        // First transform for rotation
        if (absRefOrient != 0) {
            // Rotation implies translation to keep the drawing area in the
            // first quadrant. Note: rotation is counter-clockwise
            switch (absRefOrient) {
                case 90:
                    tm = tm.translate(0, width); // width = absVPrect.height
                    break;
                case 180:
                    tm = tm.translate(width, height);
                    break;
                case 270:
                    tm = tm.translate(height, 0); // height = absVPrect.width
                    break;
            }
            tm = tm.rotate(absRefOrient);
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
        return tm.multiply(
                TransformMatrix.getWMtm(writingMode, relIPDim, relBPDim));
    }

}
