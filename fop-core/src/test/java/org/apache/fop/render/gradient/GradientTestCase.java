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

package org.apache.fop.render.gradient;

import java.awt.Color;
import java.awt.geom.AffineTransform;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.apache.batik.ext.awt.LinearGradientPaint;
import org.apache.batik.ext.awt.RadialGradientPaint;

public class GradientTestCase {

    private static class PatternChecker {

        private final Pattern pattern;

        PatternChecker(Pattern pattern) {
            this.pattern = pattern;
        }

        public PatternChecker type(int expectedType) {
            assertEquals(expectedType, pattern.getPatternType());
            return this;
        }

        public PatternChecker matrix(Double... expectedMatrix) {
            assertArrayEquals(expectedMatrix, pattern.getMatrix().toArray());
            return this;
        }

        public ShadingChecker shading() {
            return new ShadingChecker(pattern.getShading());
        }
    }

    private static class ShadingChecker {

        private final Shading shading;

        ShadingChecker(Shading shading) {
            this.shading = shading;
        }

        ShadingChecker shadingType(int expectedShadingType) {
            assertEquals(expectedShadingType, shading.getShadingType());
            return this;
        }

        ShadingChecker coords(double... expectedCoords) {
            double[] coords = new double[shading.getCoords().size()];
            int index = 0;
            for (Double d : shading.getCoords()) {
                coords[index++] = d;
            }
            assertArrayEquals(expectedCoords, coords, 0.0001);
            return this;
        }

        ShadingChecker extend(Boolean... expectedExtend) {
            assertArrayEquals(expectedExtend, shading.getExtend().toArray());
            return this;
        }

        FunctionChecker function() {
            return new FunctionChecker(shading.getFunction());
        }
    }

    private static class FunctionChecker {

        private final Function function;

        FunctionChecker(Function function) {
            this.function = function;
        }

        FunctionChecker functionType(int expectedFunctionType) {
            assertEquals(expectedFunctionType, function.getFunctionType());
            return this;
        }

        FunctionChecker domain(Double... expectedDomain) {
            assertArrayEquals(expectedDomain, function.getDomain().toArray());
            return this;
        }

        FunctionChecker bounds(Float... expectedBounds) {
            assertArrayEquals(expectedBounds, function.getBounds().toArray());
            return this;
        }

        FunctionChecker encode(Double... expectedEncode) {
            assertArrayEquals(expectedEncode, function.getEncode().toArray());
            return this;
        }

        FunctionChecker cZero(float... expectedCZero) {
            assertArrayEquals(expectedCZero, function.getCZero(), 0f);
            return this;
        }

        FunctionChecker cOne(float... expectedCOne) {
            assertArrayEquals(expectedCOne, function.getCOne(), 0f);
            return this;
        }

        FunctionChecker functions(int expectedFunctionCount) {
            assertEquals(expectedFunctionCount, function.getFunctions().size());
            return this;
        }

        FunctionChecker function(int index) {
            return new FunctionChecker(function.getFunctions().get(index));
        }
    }

    @Test
    public void simpleLinearGradient() {
        LinearGradientPaint gradient = new LinearGradientPaint(0f, 0f, 100f, 100f,
                fractions(0f, 1f), colors(Color.BLUE, Color.RED));
        Pattern pattern = GradientMaker.makeLinearGradient(gradient,
                AffineTransform.getTranslateInstance(10.0, 20.0),
                AffineTransform.getScaleInstance(100.0, 1000.0));
        PatternChecker patternChecker = new PatternChecker(pattern)
                .type(2)
                .matrix(100.0, 0.0, 0.0, 1000.0, 10.0, 20.0);
        ShadingChecker shadingChecker = patternChecker.shading()
                .shadingType(2)
                .coords(0.0, 0.0, 100.0, 100.0)
                .extend(true, true);
        FunctionChecker functionChecker = shadingChecker.function()
                .functionType(3)
                .domain(0.0, 1.0)
                .bounds()
                .encode(0.0, 1.0)
                .functions(1);
        functionChecker.function(0)
                .functionType(2)
                .domain(0.0, 1.0)
                .cZero(0f, 0f, 1f)
                .cOne(1f, 0f, 0f)
                .functions(0);
    }

    @Test
    public void simpleRadialGradient() {
        RadialGradientPaint gradient = new RadialGradientPaint(100, 200, 50,
                fractions(0f, 1f), colors(Color.BLUE, Color.RED));
        Pattern pattern = GradientMaker.makeRadialGradient(gradient, new AffineTransform(), new AffineTransform());
        PatternChecker patternChecker = new PatternChecker(pattern).type(2);
        ShadingChecker shadingChecker = patternChecker.shading()
                .shadingType(3)
                .coords(100.0, 200.0, 0.0, 100.0, 200.0, 50.0)
                .extend(true, true);
        FunctionChecker functionChecker = shadingChecker.function()
                .functionType(3)
                .domain(0.0, 1.0)
                .bounds()
                .encode(0.0, 1.0)
                .functions(1);
        functionChecker.function(0)
                .functionType(2)
                .domain(0.0, 1.0)
                .cZero(0f, 0f, 1f)
                .cOne(1f, 0f, 0f)
                .functions(0);
    }

    @Test
    public void threeColorLinearGradient() {
        LinearGradientPaint gradient = new LinearGradientPaint(0f, 10f, 20f, 30f,
                fractions(0f, 0.5f, 1f), colors(Color.BLUE, Color.RED, Color.GREEN));
        Pattern pattern = GradientMaker.makeLinearGradient(gradient, new AffineTransform(), new AffineTransform());
        PatternChecker patternChecker = new PatternChecker(pattern)
                .type(2)
                .matrix(1.0, 0.0, 0.0, 1.0, 0.0, 0.0);
        ShadingChecker shadingChecker = patternChecker.shading()
                .shadingType(2)
                .coords(0.0, 10.0, 20.0, 30.0)
                .extend(true, true);
        FunctionChecker functionChecker = shadingChecker.function()
                .functionType(3)
                .domain(0.0, 1.0)
                .bounds(0.5f)
                .encode(0.0, 1.0, 0.0, 1.0)
                .functions(2);
        functionChecker.function(0)
                .functionType(2)
                .domain(0.0, 1.0)
                .cZero(0f, 0f, 1f)
                .cOne(1f, 0f, 0f)
                .functions(0);
        functionChecker.function(1)
                .functionType(2)
                .domain(0.0, 1.0)
                .cZero(1f, 0f, 0f)
                .cOne(0f, 1f, 0f)
                .functions(0);
    }

    @Test
    public void fourColorRadialGradientNonZeroFirstStop() {
        RadialGradientPaint gradient = new RadialGradientPaint(100, 200, 50, 110, 220,
                fractions(0.2f, 0.5f, 0.7f, 1f), colors(Color.BLUE, Color.RED, Color.GREEN, Color.WHITE));
        Pattern pattern = GradientMaker.makeRadialGradient(gradient, new AffineTransform(), new AffineTransform());
        ShadingChecker shadingChecker = new PatternChecker(pattern).shading()
                .coords(110.0, 220.0, 0.0, 100.0, 200.0, 50.0);
        FunctionChecker functionChecker = shadingChecker.function()
                .functionType(3)
                .bounds(0.2f, 0.5f, 0.7f)
                .encode(0.0, 1.0, 0.0, 1.0, 0.0, 1.0, 0.0, 1.0)
                .functions(4);
        functionChecker.function(0)
                .functionType(2)
                .cZero(0f, 0f, 1f)
                .cOne(0f, 0f, 1f);
        functionChecker.function(1)
                .functionType(2)
                .cZero(0f, 0f, 1f)
                .cOne(1f, 0f, 0f);
        functionChecker.function(2)
                .functionType(2)
                .cZero(1f, 0f, 0f)
                .cOne(0f, 1f, 0f);
        functionChecker.function(3)
                .functionType(2)
                .cZero(0f, 1f, 0f)
                .cOne(1f, 1f, 1f);
    }

    @Test
    public void fourColorRadialGradientNonZeroLastStopFocalOut() {
        RadialGradientPaint gradient = new RadialGradientPaint(0, 0, 100, 100, 100,
                fractions(0f, 0.3f, 0.6f, 0.9f), colors(Color.WHITE, Color.RED, Color.GREEN, Color.BLUE));
        Pattern pattern = GradientMaker.makeRadialGradient(gradient, new AffineTransform(), new AffineTransform());
        ShadingChecker shadingChecker = new PatternChecker(pattern).shading()
                .coords(70.7036, 70.7036, 0.0, 0.0, 0.0, 100.0);
        FunctionChecker functionChecker = shadingChecker.function()
                .functionType(3)
                .bounds(0.3f, 0.6f, 0.9f)
                .encode(0.0, 1.0, 0.0, 1.0, 0.0, 1.0, 0.0, 1.0)
                .functions(4);
        functionChecker.function(0)
                .functionType(2)
                .cZero(1f, 1f, 1f)
                .cOne(1f, 0f, 0f);
        functionChecker.function(1)
                .functionType(2)
                .cZero(1f, 0f, 0f)
                .cOne(0f, 1f, 0f);
        functionChecker.function(2)
                .functionType(2)
                .cZero(0f, 1f, 0f)
                .cOne(0f, 0f, 1f);
        functionChecker.function(3)
                .functionType(2)
                .cZero(0f, 0f, 1f)
                .cOne(0f, 0f, 1f);
    }

    private float[] fractions(float... fractions) {
        return fractions;
    }

    private Color[] colors(Color... colors) {
        return colors;
    }

}
