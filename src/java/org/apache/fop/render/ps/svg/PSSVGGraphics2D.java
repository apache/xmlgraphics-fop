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
import org.apache.fop.render.shading.GradientFactory;
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

    private void handleLinearGradient(LinearGradientPaint lgp, PSGenerator gen) throws IOException {
        MultipleGradientPaint.CycleMethodEnum cycle = lgp.getCycleMethod();
        if (cycle != MultipleGradientPaint.NO_CYCLE) {
            return;
        }
        float[] fractions = lgp.getFractions();
        Color[] cols = lgp.getColors();

        AffineTransform transform = new AffineTransform(getBaseTransform());
        transform.concatenate(getTransform());
        transform.concatenate(lgp.getTransform());

        List theMatrix = new ArrayList();
        double [] mat = new double[6];
        transform.getMatrix(mat);
        for (int idx = 0; idx < mat.length; idx++) {
            theMatrix.add(Double.valueOf(mat[idx]));
        }


        List<Double> theCoords = new java.util.ArrayList<Double>();


        AffineTransform start = applyTransform(lgp.getTransform(),
                lgp.getStartPoint().getX(), lgp.getStartPoint().getY());
        AffineTransform end = applyTransform(lgp.getTransform(), lgp.getEndPoint().getX(), lgp.getEndPoint().getY());
        double startX = start.getTranslateX();
        double startY = start.getTranslateY();
        double endX = end.getTranslateX();
        double endY = end.getTranslateY();

        double width = endX - startX;
        double height = endY - startY;

        startX = startX + width * fractions[0];
        endX = endX - width * (1 - fractions[fractions.length - 1]);
        startY = startY + (height * fractions[0]);
        endY =  endY - height * (1 - fractions[fractions.length - 1]);

        theCoords.add(startX);
        theCoords.add(startY);
        theCoords.add(endX);
        theCoords.add(endY);


        List<Color> someColors = new java.util.ArrayList<Color>();
        for (int count = 0; count < cols.length; count++) {
            Color c1 = cols[count];
            if (c1.getAlpha() != 255) {
                LOG.warn("Opacity is not currently supported for Postscript output");
            }
            someColors.add(c1);
        }
        List<Double> theBounds = new java.util.ArrayList<Double>();
        for (int count = 1; count < fractions.length - 1; count++) {
            float offset = fractions[count];
            theBounds.add(Double.valueOf(offset));
        }
        PDFDeviceColorSpace colSpace;
        colSpace = new PDFDeviceColorSpace(PDFDeviceColorSpace.DEVICE_RGB);

        PSGradientFactory gradientFactory = (PSGradientFactory)GradientFactory.newInstance(this);
        PSPattern myPattern = gradientFactory.createGradient(false, colSpace,
                someColors, theBounds, theCoords, theMatrix);

        gen.write(myPattern.toString());

    }



    private void handleRadialGradient(RadialGradientPaint rgp, PSGenerator gen) throws IOException {
        MultipleGradientPaint.CycleMethodEnum cycle = rgp.getCycleMethod();
        if (cycle != MultipleGradientPaint.NO_CYCLE) {
            return;
        }

        AffineTransform transform;
        transform = new AffineTransform(getBaseTransform());
        transform.concatenate(getTransform());
        transform.concatenate(rgp.getTransform());

        AffineTransform resultCentre = applyTransform(rgp.getTransform(),
                rgp.getCenterPoint().getX(), rgp.getCenterPoint().getY());
        AffineTransform resultFocus = applyTransform(rgp.getTransform(),
                rgp.getFocusPoint().getX(), rgp.getFocusPoint().getY());
        double scale = Math.sqrt(rgp.getTransform().getDeterminant());
        double radius = rgp.getRadius() * scale;
        double centreX = resultCentre.getTranslateX();
        double centreY = resultCentre.getTranslateY();
        double focusX = resultFocus.getTranslateX();
        double focusY = resultFocus.getTranslateY();

        List<Double> theMatrix = new java.util.ArrayList<Double>();
        double [] mat = new double[6];
        transform.getMatrix(mat);
        for (int idx = 0; idx < mat.length; idx++) {
            theMatrix.add(Double.valueOf(mat[idx]));
        }

        List<Double> theCoords = new java.util.ArrayList<Double>();
        float[] fractions = rgp.getFractions();

        theCoords.add(centreX);
        theCoords.add(centreY);
        theCoords.add(radius * rgp.getFractions()[0]);
        theCoords.add(focusX);
        theCoords.add(focusY);
        theCoords.add(radius * fractions[fractions.length - 1]);

        Color[] cols = rgp.getColors();
        List<Color> someColors = new java.util.ArrayList<Color>();
        for (int count = 0; count < cols.length; count++) {
            Color cc = cols[count];
            if (cc.getAlpha() != 255) {
                /* This should never happen because radial gradients with opacity should now
                 * be rasterized in the PSImageHandlerSVG class. Please see the shouldRaster()
                 * method for more information. */
                LOG.warn("Opacity is not currently supported for Postscript output");
            }

            someColors.add(cc);
        }

        List<Double> theBounds = new java.util.ArrayList<Double>();
        for (int count = 1; count < fractions.length - 1; count++) {
            float offset = fractions[count];
            theBounds.add(Double.valueOf(offset));
        }
        PDFDeviceColorSpace colSpace;
        colSpace = new PDFDeviceColorSpace(PDFDeviceColorSpace.DEVICE_RGB);

        PSGradientFactory gradientFactory = (PSGradientFactory)GradientFactory.newInstance(this);
        PSPattern myPattern = gradientFactory.createGradient(true, colSpace,
                someColors, theBounds, theCoords, theMatrix);

        gen.write(myPattern.toString());
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
