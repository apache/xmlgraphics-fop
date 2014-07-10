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

package org.apache.fop.render.ps.svg;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Paint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.batik.ext.awt.LinearGradientPaint;
import org.apache.batik.ext.awt.MultipleGradientPaint;
import org.apache.batik.ext.awt.RadialGradientPaint;

import org.apache.xmlgraphics.java2d.ps.PSGraphics2D;
import org.apache.xmlgraphics.ps.PSGenerator;

import org.apache.fop.pdf.PDFDeviceColorSpace;
import org.apache.fop.render.shading.Function;
import org.apache.fop.render.shading.GradientRegistrar;
import org.apache.fop.render.shading.PSGradientFactory;
import org.apache.fop.render.shading.Pattern;
import org.apache.fop.render.shading.Shading;


public class PSSVGGraphics2D extends PSGraphics2D implements GradientRegistrar {

    private static final Log LOG = LogFactory.getLog(PSSVGGraphics2D.class);

    /**
     * Create a new Graphics2D that generates PostScript code.
     * @param textAsShapes True if text should be rendered as graphics
     * @see org.apache.xmlgraphics.java2d.AbstractGraphics2D#AbstractGraphics2D(boolean)
     */
    public PSSVGGraphics2D(boolean textAsShapes) {
        super(textAsShapes);
    }

    /**
     * Create a new Graphics2D that generates PostScript code.
     * @param textAsShapes True if text should be rendered as graphics
     * @param gen PostScript generator to use for output
     * @see org.apache.xmlgraphics.java2d.AbstractGraphics2D#AbstractGraphics2D(boolean)
     */
    public PSSVGGraphics2D(boolean textAsShapes, PSGenerator gen) {
        super(textAsShapes, gen);
    }

    /**
     * Constructor for creating copies
     * @param g parent PostScript Graphics2D
     */
    public PSSVGGraphics2D(PSGraphics2D g) {
        super(g);
    }

    protected void applyPaint(Paint paint, boolean fill) {
        super.applyPaint(paint, fill);
        if (paint instanceof RadialGradientPaint) {
            RadialGradientPaint rgp = (RadialGradientPaint)paint;
            try {
                handleRadialGradient(rgp, gen);
            } catch (IOException ioe) {
                handleIOException(ioe);
            }
        } else if (paint instanceof LinearGradientPaint) {
            LinearGradientPaint lgp = (LinearGradientPaint)paint;
            try {
                handleLinearGradient(lgp, gen);
            } catch (IOException ioe) {
                handleIOException(ioe);
            }
        }
    }

    private void handleLinearGradient(LinearGradientPaint gp, PSGenerator gen) throws IOException {
        MultipleGradientPaint.CycleMethodEnum cycle = gp.getCycleMethod();
        if (cycle != MultipleGradientPaint.NO_CYCLE) {
            return;
        }

        List<Double> matrix = createGradientTransform(gp);

        List<Double> theCoords = new java.util.ArrayList<Double>();
        theCoords.add(gp.getStartPoint().getX());
        theCoords.add(gp.getStartPoint().getX());
        theCoords.add(gp.getEndPoint().getX());
        theCoords.add(gp.getEndPoint().getY());

        List<Color> colors = createGradientColors(gp);

        float[] fractions = gp.getFractions();
        List<Double> theBounds = new java.util.ArrayList<Double>();
        for (int count = 0; count < fractions.length; count++) {
            float offset = fractions[count];
            if (0f < offset && offset < 1f) {
                theBounds.add(new Double(offset));
            }
        }
        PDFDeviceColorSpace colSpace;
        colSpace = new PDFDeviceColorSpace(PDFDeviceColorSpace.DEVICE_RGB);

        PSGradientFactory gradientFactory = new PSGradientFactory();
        PSPattern myPattern = gradientFactory.createGradient(false, colSpace,
                colors, theBounds, theCoords, matrix);

        gen.write(myPattern.toString());

    }

    private void handleRadialGradient(RadialGradientPaint gp, PSGenerator gen) throws IOException {
        MultipleGradientPaint.CycleMethodEnum cycle = gp.getCycleMethod();
        if (cycle != MultipleGradientPaint.NO_CYCLE) {
            return;
        }

        List<Double> matrix = createGradientTransform(gp);

        double ar = gp.getRadius();
        Point2D ac = gp.getCenterPoint();
        Point2D af = gp.getFocusPoint();
        List<Double> theCoords = new java.util.ArrayList<Double>();
        double dx = af.getX() - ac.getX();
        double dy = af.getY() - ac.getY();
        double d = Math.sqrt(dx * dx + dy * dy);
        if (d > ar) {
            // the center point af must be within the circle with
            // radius ar centered at ac so limit it to that.
            double scale = (ar * .9999) / d;
            dx = dx * scale;
            dy = dy * scale;
        }

        theCoords.add(new Double(ac.getX() + dx)); // Fx
        theCoords.add(new Double(ac.getY() + dy)); // Fy
        theCoords.add(new Double(0));
        theCoords.add(new Double(ac.getX()));
        theCoords.add(new Double(ac.getY()));
        theCoords.add(new Double(ar));

        List<Color> colors = createGradientColors(gp);

        float[] fractions = gp.getFractions();
        List<Double> theBounds = new java.util.ArrayList<Double>();
        for (int count = 0; count < fractions.length; count++) {
            float offset = fractions[count];
            if (0f < offset && offset < 1f) {
                theBounds.add(new Double(offset));
            }
        }
        PDFDeviceColorSpace colSpace;
        colSpace = new PDFDeviceColorSpace(PDFDeviceColorSpace.DEVICE_RGB);

        PSGradientFactory gradientFactory = new PSGradientFactory();
        PSPattern myPattern = gradientFactory.createGradient(true, colSpace,
                colors, theBounds, theCoords, matrix);

        gen.write(myPattern.toString());
    }

    private List<Double> createGradientTransform(MultipleGradientPaint gradient) {
        AffineTransform transform = new AffineTransform(getBaseTransform());
        transform.concatenate(getTransform());
        transform.concatenate(gradient.getTransform());
        List<Double> matrix = new ArrayList<Double>(6);
        double[] m = new double[6];
        transform.getMatrix(m);
        for (double d : m) {
            matrix.add(Double.valueOf(d));
        }
        return matrix;
    }

    private List<Color> createGradientColors(MultipleGradientPaint gradient) {
        Color[] svgColors = gradient.getColors();
        List<Color> gradientColors = new ArrayList<Color>(svgColors.length + 2);
        float[] fractions = gradient.getFractions();
        if (fractions[0] > 0f) {
            gradientColors.add(svgColors[0]);
        }
        for (Color c : svgColors) {
            gradientColors.add(c);
        }
        if (fractions[fractions.length - 1] < 1f) {
            gradientColors.add(svgColors[svgColors.length - 1]);
        }
        return gradientColors;
    }

    private AffineTransform applyTransform(AffineTransform base, double posX, double posY) {
        AffineTransform result = AffineTransform.getTranslateInstance(posX, posY);
        AffineTransform orig = base;
        orig.concatenate(result);
        return orig;
    }

    protected AffineTransform getBaseTransform() {
        AffineTransform at = new AffineTransform(this.getTransform());
        return at;
    }

    /**
     * Creates a new <code>Graphics</code> object that is
     * a copy of this <code>Graphics</code> object.
     * @return     a new graphics context that is a copy of
     * this graphics context.
     */
    @Override
    public Graphics create() {
        preparePainting();
        return new PSSVGGraphics2D(this);
    }

    /**
     * Registers a function object against the output format document
     * @param function The function object to register
     * @return Returns either the function which has already been registered
     * or the current new registered object.
     */
    public Function registerFunction(Function function) {
        //Objects aren't needed to be registered in Postscript
        return function;
    }

    /**
     * Registers a shading object against the otuput format document
     * @param shading The shading object to register
     * @return Returs either the shading which has already been registered
     * or the current new registered object
     */
    public Shading registerShading(Shading shading) {
        //Objects aren't needed to be registered in Postscript
        return shading;
    }

    /**
     * Registers a pattern object against the output format document
     * @param pattern The pattern object to register
     * @return Returns either the pattern which has already been registered
     * or the current new registered object
     */
    public Pattern registerPattern(Pattern pattern) {
        // TODO Auto-generated method stub
        return pattern;
    }
}
