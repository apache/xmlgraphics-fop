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

        ShadingChecker coords(Double... expectedCoords) {
            assertArrayEquals(expectedCoords, shading.getCoords().toArray());
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
    public void testGradient() {
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
                .functions(1);
        functionChecker.function(0)
                .functionType(2)
                .domain(0.0, 1.0)
                .cZero(0f, 0f, 1f)
                .cOne(1f, 0f, 0f)
                .functions(0);
    }

    private float[] fractions(float... fractions) {
        return fractions;
    }

    private Color[] colors(Color... colors) {
        return colors;
    }

}
