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

package org.apache.fop.render.ps;

import java.util.List;

import org.apache.fop.render.gradient.Function;
import org.apache.fop.render.gradient.Function.SubFunctionRenderer;
import org.apache.fop.render.gradient.GradientMaker.DoubleFormatter;
import org.apache.fop.render.gradient.Pattern;
import org.apache.fop.render.gradient.Shading;

/**
 * Helper class to draw gradients in PostScript.
 */
public final class Gradient {

    private Gradient() { }

    public static String outputPattern(Pattern pattern, DoubleFormatter doubleFormatter) {
        StringBuilder p = new StringBuilder(64);
        p.append("/Pattern setcolorspace\n");
        p.append("<< \n/Type /Pattern \n");

        p.append("/PatternType " + pattern.getPatternType() + " \n");

        if (pattern.getShading() != null) {
            p.append("/Shading ");
            outputShading(p, pattern.getShading(), doubleFormatter);
            p.append(" \n");
        }
        p.append(">> \n");
        List<Double> matrix = pattern.getMatrix();
        if (matrix == null) {
            p.append("matrix ");
        } else {
            p.append("[ ");
            for (double m : pattern.getMatrix()) {
                p.append(doubleFormatter.formatDouble(m));
                p.append(" ");
            }
            p.append("] ");
        }
        p.append("makepattern setcolor\n");

        return p.toString();
    }

    private static void outputShading(StringBuilder out, Shading shading, final DoubleFormatter doubleFormatter) {
        final Function function = shading.getFunction();
        Shading.FunctionRenderer functionRenderer = new Shading.FunctionRenderer() {

            public void outputFunction(StringBuilder out) {
                SubFunctionRenderer subFunctionRenderer = new Function.SubFunctionRenderer() {

                    public void outputFunction(StringBuilder out, int functionIndex) {
                        Function subFunction = function.getFunctions().get(functionIndex);
                        assert subFunction.getFunctions().isEmpty();
                        subFunction.output(out, doubleFormatter, null);
                    }
                };
                function.output(out, doubleFormatter, subFunctionRenderer);
            }
        };
        shading.output(out, doubleFormatter, functionRenderer);
    }

}
