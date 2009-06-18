/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */
package org.apache.fop.area;

import java.awt.geom.Rectangle2D;
import java.awt.Rectangle;
import java.io.Serializable;

import org.apache.fop.fo.properties.WritingMode;

/**
 * Describe a PDF or PostScript style coordinate transformation matrix (CTM).
 * The matrix encodes translations, scaling and rotations of the coordinate
 * system used to render pages.
 */

public class CTM implements Serializable {
    private double a,b,c,d,e,f;

    private static CTM s_CTM_lrtb = new CTM(1,0,0,1,0,0);
    private static CTM s_CTM_rltb = new CTM(-1,0,0,1,0,0);
    private static CTM s_CTM_tbrl = new CTM(0,1,-1,0,0,0);
/**
 * Create the identity matrix
 */
    public CTM() {
        a=1;
        b=0;
        c=0;
        d=1;
        e=0;
        f=0;
    }

    /**
     * Initialize a CTM from the passed arguments.
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
     * specified by x and y.
     */
    public CTM(double x, double y) {
        this.a = 1;
        this.b = 0;
        this.c = 0;
        this.d = 1;
        this.e = x;
        this.f = y;
    }

    protected CTM(CTM ctm) {
	this.a = ctm.a;
	this.b = ctm.b;
	this.c = ctm.c;
	this.d = ctm.d;
	this.e = ctm.e;
	this.f = ctm.f;
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
     */
    static public CTM getWMctm(int wm, int ipd, int bpd) {
	CTM wmctm;
        switch (wm) {
            case WritingMode.LR_TB:
                return new CTM(s_CTM_lrtb);
            case WritingMode.RL_TB:
		{
		wmctm = new CTM(s_CTM_rltb);
		wmctm.e = ipd;
		return wmctm;
		}
                //return  s_CTM_rltb.translate(ipd, 0);
            case WritingMode.TB_RL: // CJK
		{
		wmctm = new CTM(s_CTM_tbrl);
		wmctm.e = bpd;
		return wmctm;
		}
                //return s_CTM_tbrl.translate(0, ipd);
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
        CTM rslt= new CTM (
            (premult.a * a) + (premult.b * c),
            (premult.a * b) + (premult.b * d),
            (premult.c * a) + (premult.d * c),
            (premult.c * b) + (premult.d * d),
            (premult.e * a) + (premult.f * c) + e,
            (premult.e * b) + (premult.f * d) + f
        );
        return rslt;
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
	if (angle == 90.0) {
	    cos = 0.0;
	    sin = 1.0;
	}
	else if (angle == 270.0) {
	    cos = 0.0;
	    sin = -1.0;
	}
	else if (angle == 180.0) {
	    cos = -1.0;
	    sin = 0.0;
	}
	else {
	    double rad = Math.toRadians(angle);
	    cos = Math.cos(rad);
	    sin = Math.sin(rad);
	}
        CTM rotate= new CTM(cos,-sin, sin, cos, 0, 0);
        return multiply(rotate);
    }

    /**
     * Translate this CTM by the passed x and y values and return a new result CTM.
     * @param x The amount to translate along the x axis.
     * @param y The amount to translate along the y axis.
     * @return CTM The result of translating this CTM.
     */
    public CTM translate(double x, double y) {
        CTM translate= new CTM(1,0,0,1,x,y);
        return multiply(translate);
    }

    /**
     * Scale this CTM by the passed x and y values and return a new result CTM.
     * @param x The amount to scale along the x axis.
     * @param y The amount to scale along the y axis.
     * @return CTM The result of scaling this CTM.
     */
    public CTM scale(double x, double y) {
        CTM scale= new CTM(x,0,0,y,0,0);
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
        int x1t = (int)(inRect.getX()*a + inRect.getY()*c + e);
        int y1t = (int)(inRect.getX()*b + inRect.getY()*d + f);
        int x2t = (int)((inRect.getX()+inRect.getWidth())*a +
            (inRect.getY()+inRect.getHeight())*c + e);
        int y2t = (int)((inRect.getX()+inRect.getWidth())*b +
            (inRect.getY()+inRect.getHeight())*d + f);
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
        return new Rectangle(x1t, y1t, x2t-x1t, y2t-y1t);
    }

    public String toString() {
	return "[" + a + " " + b + " " + c + " " + d + " " + e + " " + f + "]";
    }

    public String toPDFctm() {
	return a + " " + b + " " + c + " " + d + " " + e/1000f + " " + f/1000f;
    }
}
