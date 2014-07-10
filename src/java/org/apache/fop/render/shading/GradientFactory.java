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

package org.apache.fop.render.shading;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.apache.batik.ext.awt.LinearGradientPaint;
import org.apache.batik.ext.awt.MultipleGradientPaint;
import org.apache.batik.ext.awt.RadialGradientPaint;

import org.apache.xmlgraphics.java2d.color.ColorUtil;

import org.apache.fop.pdf.PDFDeviceColorSpace;

public abstract class GradientFactory<P extends Pattern> {

    public P createLinearGradient(LinearGradientPaint gp,
            AffineTransform baseTransform, AffineTransform transform) {
        Point2D startPoint = gp.getStartPoint();
        Point2D endPoint = gp.getEndPoint();
        List<Double> coords = new java.util.ArrayList<Double>(4);
        coords.add(new Double(startPoint.getX()));
        coords.add(new Double(startPoint.getY()));
        coords.add(new Double(endPoint.getX()));
        coords.add(new Double(endPoint.getY()));
        return createGradient(gp, coords, baseTransform, transform);
    }

    public P createRadialGradient(RadialGradientPaint gradient,
            AffineTransform baseTransform, AffineTransform transform) {
        double radius = gradient.getRadius();
        Point2D center = gradient.getCenterPoint();
        Point2D focus = gradient.getFocusPoint();
        double dx = focus.getX() - center.getX();
        double dy = focus.getY() - center.getY();
        double d = Math.sqrt(dx * dx + dy * dy);
        if (d > radius) {
            // The center point must be within the circle with
            // radius radius centered at center so limit it to that.
            double scale = (radius * .9999) / d;
            dx *= scale;
            dy *= scale;
        }
        List<Double> coords = new java.util.ArrayList<Double>();
        coords.add(Double.valueOf(center.getX() + dx));
        coords.add(Double.valueOf(center.getY() + dy));
        coords.add(Double.valueOf(0));
        coords.add(Double.valueOf(center.getX()));
        coords.add(Double.valueOf(center.getY()));
        coords.add(Double.valueOf(radius));
        return createGradient(gradient, coords, baseTransform, transform);
    }

    private P createGradient(MultipleGradientPaint gradient, List<Double> coords,
            AffineTransform baseTransform, AffineTransform transform) {
        List<Double> matrix = createTransform(gradient, baseTransform, transform);
        List<Color> colors = createColors(gradient);
        List<Double> bounds = createBounds(gradient);
        //Gradients are currently restricted to sRGB
        PDFDeviceColorSpace colSpace = new PDFDeviceColorSpace(PDFDeviceColorSpace.DEVICE_RGB);
        return makeGradient(gradient instanceof RadialGradientPaint, colSpace, colors, bounds, coords, matrix);
    }

    private List<Double> createTransform(MultipleGradientPaint gradient,
            AffineTransform baseTransform, AffineTransform transform) {
        AffineTransform gradientTransform = new AffineTransform(baseTransform);
        gradientTransform.concatenate(transform);
        gradientTransform.concatenate(gradient.getTransform());
        List<Double> matrix = new ArrayList<Double>(6);
        double[] m = new double[6];
        gradientTransform.getMatrix(m);
        for (double d : m) {
            matrix.add(Double.valueOf(d));
        }
        return matrix;
    }

    private List<Color> createColors(MultipleGradientPaint gradient) {
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

    private List<Double> createBounds(MultipleGradientPaint gradient) {
        // TODO is the conversion to double necessary?
        float[] fractions = gradient.getFractions();
        List<Double> bounds = new java.util.ArrayList<Double>(fractions.length);
        for (float offset : fractions) {
            if (0f < offset && offset < 1f) {
                bounds.add(Double.valueOf(offset));
            }
        }
        return bounds;
    }

    /**
     * Creates a new gradient.
     *
     * @param radial whether the gradient is linear or radial
     * @param colorspace the colorspace used in PDF and Postscript
     * @param colors the colors to be used in the gradient
     * @param bounds the bounds of each color
     * @param coords the coordinates of the gradient
     * @param matrix the transformation matrix
     * @return the Pattern object of the gradient
     */
    protected P makeGradient(boolean radial, PDFDeviceColorSpace colorspace,
            List<Color> colors, List<Double> bounds, List<Double> coords, List<Double> matrix) {
        List<Function> functions = new ArrayList<Function>();
        // if 5 elements, the penultimate element is 3.
        // do not go beyond that, because you always need
        // to have a next color when creating the function.
        for (int currentPosition = 0, lastPosition = colors.size() - 1; currentPosition < lastPosition;
                currentPosition++) {    // for every consecutive color pair
            Color currentColor = colors.get(currentPosition);
            Color nextColor = colors.get(currentPosition + 1);

            // colorspace must be consistent, so we simply convert to sRGB where necessary
            if (!currentColor.getColorSpace().isCS_sRGB()) {
                //Convert to sRGB
                currentColor = ColorUtil.toSRGBColor(currentColor);
                colors.set(currentPosition, currentColor);
            }
            if (!nextColor.getColorSpace().isCS_sRGB()) {
                //Convert to sRGB
                nextColor = ColorUtil.toSRGBColor(nextColor);
                colors.set(currentPosition + 1, nextColor);
            }
            List<Double> c0 = toColorVector(currentColor);
            List<Double> c1 = toColorVector(nextColor);
            Function function = makeFunction(2, null, null, c0, c1, 1.0);
            functions.add(function);
        }

        Function function = makeFunction(3, null, null, functions, bounds, null);
        Shading shading;
        if (radial) {
            if (coords.size() == 6) {
                shading = makeShading(3, colorspace, null, null, false, coords, null, function, null);
            } else {    // if the center x, center y, and radius specifiy
                // the gradient, then assume the same center x, center y,
                // and radius of zero for the other necessary component
                List<Double> newCoords = new ArrayList<Double>();
                newCoords.add(coords.get(0));
                newCoords.add(coords.get(1));
                newCoords.add(coords.get(2));
                newCoords.add(coords.get(0));
                newCoords.add(coords.get(1));
                newCoords.add(Double.valueOf(0.0));

                shading = makeShading(3, colorspace, null, null, false, newCoords,
                        null, function, null);
            }
        } else {
            shading = makeShading(2, colorspace, null, null, false, coords,
                    null, function, null);
        }
        return makePattern(2, shading, null, null, matrix);
    }

    public abstract Function makeFunction(int functionType, List<Double> theDomain,
            List<Double> theRange, List<Function> theFunctions,
            List<Double> theBounds, List<Double> theEncode);

    public abstract Function makeFunction(int functionType, List<Double> theDomain,
            List<Double> theRange, List<Double> theCZero, List<Double> theCOne,
            double theInterpolationExponentN);

    public abstract Shading makeShading(int theShadingType,
            PDFDeviceColorSpace theColorSpace, List<Double> theBackground, List<Double> theBBox,
            boolean theAntiAlias, List<Double> theCoords, List<Double> theDomain,
            Function theFunction, List<Integer> theExtend);

    public abstract P makePattern(int thePatternType, Shading theShading, List theXUID,
            StringBuffer theExtGState, List<Double> theMatrix);

    private List<Double> toColorVector(Color nextColor) {
        List<Double> vector = new java.util.ArrayList<Double>();
        float[] comps = nextColor.getColorComponents(null);
        for (int i = 0, c = comps.length; i < c; i++) {
            vector.add(Double.valueOf(comps[i]));
        }
        return vector;
    }
}
