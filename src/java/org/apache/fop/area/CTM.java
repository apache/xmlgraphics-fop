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

package org.apache.fop.area;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

import org.apache.fop.datatypes.FODimension;
import org.apache.fop.fo.Constants;

/**
 * Describe a PDF or PostScript style coordinate transformation matrix (CTM).
 * The matrix encodes translations, scaling and rotations of the coordinate
 * system used to render pages.
 */
public class CTM implements Serializable {

    private static final long serialVersionUID = -8743287485623778341L;

    private double a, b, c, d, e, f;

    private static final CTM CTM_LRTB = new CTM(1, 0, 0, 1, 0, 0);
    private static final CTM CTM_RLTB = new CTM(-1, 0, 0, 1, 0, 0);
    private static final CTM CTM_TBRL = new CTM(0, 1, -1, 0, 0, 0);

    /**
     * Create the identity matrix
     */
    public CTM() {
        a = 1;
        b = 0;
        c = 0;
        d = 1;
        e = 0;
        f = 0;
    }

    /**
     * Initialize a CTM from the passed arguments.
     *
     * @param a the x scale
     * @param b the x shear
     * @param c the y shear
     * @param d the y scale
     * @param e the x shift
     * @param f the y shift
     */
    public CTM(double a, double b, double c, double d, double e, double f) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.e = e;
        this.f = f;
    }

    /**
     * Initialize a CTM to the identity matrix with a translation
     * specified by x and y
     *
     * @param x the x shift
     * @param y the y shift.
     */
    public CTM(double x, double y) {
        this.a = 1;
        this.b = 0;
        this.c = 0;
        this.d = 1;
        this.e = x;
        this.f = y;
    }

    /**
     * Initialize a CTM with the values of another CTM.
     *
     * @param ctm another CTM
     */
    protected CTM(CTM ctm) {
        this.a = ctm.a;
        this.b = ctm.b;
        this.c = ctm.c;
        this.d = ctm.d;
        this.e = ctm.e;
        this.f = ctm.f;
    }

    /**
     * Initialize a CTM with the values of an AffineTransform.
     *
     * @param at the transformation matrix
     */
    public CTM(AffineTransform at) {
        double[] matrix = new double[6];
        at.getMatrix(matrix);
        this.a = matrix[0];
        this.b = matrix[1];
        this.c = matrix[2];
        this.d = matrix[3];
        this.e = matrix[4];
        this.f = matrix[5];
    }

    /**
     * Return a CTM which will transform coordinates for a particular writing-mode
     * into normalized first quandrant coordinates.
     * @param wm A writing mode constant from fo.properties.WritingMode, ie.
     * one of LR_TB, RL_TB, TB_RL.
     * @param ipd The inline-progression dimension of the reference area whose
     * CTM is being set..
     * @param bpd The block-progression dimension of the reference area whose
     * CTM is being set.
     * @return a new CTM with the required transform
     */
    public static CTM getWMctm(int wm, int ipd, int bpd) {
        CTM wmctm;
        switch (wm) {
            case Constants.EN_LR_TB:
                return new CTM(CTM_LRTB);
            case Constants.EN_RL_TB:
                wmctm = new CTM(CTM_RLTB);
                wmctm.e = ipd;
                return wmctm;
                //return  CTM_RLTB.translate(ipd, 0);
            case Constants.EN_TB_RL:  // CJK
                wmctm = new CTM(CTM_TBRL);
                wmctm.e = bpd;
                return wmctm;
                //return CTM_TBRL.translate(0, ipd);
            default:
                return null;
        }
    }

    /**
     * Multiply new passed CTM with this one and generate a new result CTM.
     * @param premult The CTM to multiply with this one. The new one will be
     * the first multiplicand.
     * @return CTM The result of multiplying premult * this.
     */
    public CTM multiply(CTM premult) {
        CTM result = new CTM ((premult.a * a) + (premult.b * c),
                              (premult.a * b) + (premult.b * d),
                              (premult.c * a) + (premult.d * c),
                              (premult.c * b) + (premult.d * d),
                              (premult.e * a) + (premult.f * c) + e,
                              (premult.e * b) + (premult.f * d) + f);
        return result;
    }

    /**
     * Rotate this CTM by "angle" radians and return a new result CTM.
     * This is used to account for reference-orientation.
     * @param angle The angle in radians. Positive angles are measured counter-
     * clockwise.
     * @return CTM The result of rotating this CTM.
     */
    public CTM rotate(double angle) {
        double cos, sin;
        if (angle == 90.0 || angle == -270.0) {
            cos = 0.0;
            sin = 1.0;
        } else if (angle == 270.0 || angle == -90.0) {
            cos = 0.0;
            sin = -1.0;
        } else if (angle == 180.0 || angle == -180.0) {
            cos = -1.0;
            sin = 0.0;
        } else {
            double rad = Math.toRadians(angle);
            cos = Math.cos(rad);
            sin = Math.sin(rad);
        }
        CTM rotate = new CTM(cos, -sin, sin, cos, 0, 0);
        return multiply(rotate);
    }

    /**
     * Translate this CTM by the passed x and y values and return a new result CTM.
     * @param x The amount to translate along the x axis.
     * @param y The amount to translate along the y axis.
     * @return CTM The result of translating this CTM.
     */
    public CTM translate(double x, double y) {
        CTM translate = new CTM(1, 0, 0, 1, x, y);
        return multiply(translate);
    }

    /**
     * Scale this CTM by the passed x and y values and return a new result CTM.
     * @param x The amount to scale along the x axis.
     * @param y The amount to scale along the y axis.
     * @return CTM The result of scaling this CTM.
     */
    public CTM scale(double x, double y) {
        CTM scale = new CTM(x, 0, 0, y, 0, 0);
        return multiply(scale);
    }

    /**
     * Transform a rectangle by the CTM to produce a rectangle in the transformed
     * coordinate system.
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
     * Returns this CTM as an AffineTransform object.
     * @return the AffineTransform representation
     */
    public AffineTransform toAffineTransform() {
        return new AffineTransform(toArray());
    }

    /**
     * Construct a coordinate transformation matrix (CTM).
     * @param absRefOrient absolute reference orientation
     * @param writingMode the writing mode
     * @param absVPrect absolute viewpoint rectangle
     * @param reldims relative dimensions
     * @return CTM the coordinate transformation matrix (CTM)
     */
    public static CTM getCTMandRelDims(int absRefOrient,
                                       int writingMode,
                                       Rectangle2D absVPrect,
                                       FODimension reldims) {
        int width, height;
        // We will use the absolute reference-orientation to set up the CTM.
        // The value here is relative to its ancestor reference area.
        if (absRefOrient % 180 == 0) {
            width = (int) absVPrect.getWidth();
            height = (int) absVPrect.getHeight();
        } else {
            // invert width and height since top left are rotated by 90 (cl or ccl)
            height = (int) absVPrect.getWidth();
            width = (int) absVPrect.getHeight();
        }
        /* Set up the CTM for the content of this reference area.
         * This will transform region content coordinates in
         * writing-mode relative into absolute page-relative
         * which will then be translated based on the position of
         * the region viewport.
         * (Note: scrolling between region vp and ref area when
         * doing online content!)
         */
        CTM ctm = new CTM(absVPrect.getX(), absVPrect.getY());

        // First transform for rotation
        if (absRefOrient != 0) {
            // Rotation implies translation to keep the drawing area in the
            // first quadrant. Note: rotation is counter-clockwise
            switch (absRefOrient) {
                case 90:
                case -270:
                    ctm = ctm.translate(0, width); // width = absVPrect.height
                    break;
                case 180:
                case -180:
                    ctm = ctm.translate(width, height);
                    break;
                case 270:
                case -90:
                    ctm = ctm.translate(height, 0); // height = absVPrect.width
                    break;
                default:
                    throw new RuntimeException();
            }
            ctm = ctm.rotate(absRefOrient);
        }
        /* Since we've already put adjusted width and height values for the
         * top and left positions implied by the reference-orientation, we
         * can set ipd and bpd appropriately based on the writing mode.
         */

        if (writingMode == Constants.EN_LR_TB || writingMode == Constants.EN_RL_TB) {
            reldims.ipd = width;
            reldims.bpd = height;
        } else {
            reldims.ipd = height;
            reldims.bpd = width;
        }
        // Set a rectangle to be the writing-mode relative version???
        // Now transform for writing mode
        return ctm.multiply(CTM.getWMctm(writingMode, reldims.ipd, reldims.bpd));
    }

}
