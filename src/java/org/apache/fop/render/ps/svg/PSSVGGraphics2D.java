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

import java.awt.Graphics;
import java.awt.Paint;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.batik.ext.awt.LinearGradientPaint;
import org.apache.batik.ext.awt.RadialGradientPaint;

import org.apache.xmlgraphics.java2d.ps.PSGraphics2D;
import org.apache.xmlgraphics.ps.PSGenerator;

import org.apache.fop.render.shading.Function;
import org.apache.fop.render.shading.GradientMaker;
import org.apache.fop.render.shading.Pattern;
import org.apache.fop.render.shading.Shading;


public class PSSVGGraphics2D extends PSGraphics2D {

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
        if (paint instanceof LinearGradientPaint) {
            Pattern pattern = GradientMaker.makeLinearGradient((LinearGradientPaint) paint,
                    getBaseTransform(), getTransform());
            try {
                gen.write(toString(pattern));
            } catch (IOException ioe) {
                handleIOException(ioe);
            }
        } else if (paint instanceof RadialGradientPaint) {
            Pattern pattern = GradientMaker.makeRadialGradient((RadialGradientPaint) paint,
                    getBaseTransform(), getTransform());
            try {
                gen.write(toString(pattern));
            } catch (IOException ioe) {
                handleIOException(ioe);
            }
        }
    }

    /**
     * Outputs the radial or axial pattern as a string dictionary to insert
     * into a postscript document.
     * @param pattern
     */
    private String toString(Pattern pattern) {
        StringBuilder p = new StringBuilder(64);
        p.append("/Pattern setcolorspace\n");
        p.append("<< \n/Type /Pattern \n");

        p.append("/PatternType " + pattern.getPatternType() + " \n");

        if (pattern.getShading() != null) {
            p.append("/Shading ");
            outputShading(p, pattern.getShading());
            p.append(" \n");
        }
        p.append(">> \n");
        p.append("[ ");
        for (double m : pattern.getMatrix()) {
            p.append(Double.toString(m)); // TODO refactor so that PSGenerator.formatDouble can be used
            p.append(" ");
        }
        p.append("] ");
        p.append("makepattern setcolor\n");

        return p.toString();
    }

    private void outputShading(StringBuilder p, Shading shading) {
        final Function function = shading.getFunction();
        Shading.FunctionRenderer functionRenderer = new Shading.FunctionRenderer() {

            public void outputFunction(StringBuilder out) {
                List<String> functionsStrings = new ArrayList<String>(function.getFunctions().size());
                for (Function f : function.getFunctions()) {
                    functionsStrings.add(functionToString(f));
                }
                out.append(function.toWriteableString(functionsStrings));
            }
        };
        shading.output(p, functionRenderer);
    }

    private String functionToString(Function function) {
        List<String> functionsStrings = new ArrayList<String>(function.getFunctions().size());
        for (Function f : function.getFunctions()) {
            functionsStrings.add(functionToString(f));
        }
        return function.toWriteableString(functionsStrings);
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

}
