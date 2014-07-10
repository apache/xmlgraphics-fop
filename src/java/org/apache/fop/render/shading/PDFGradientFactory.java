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

package org.apache.fop.render.shading;

import java.awt.Color;
import java.util.List;

import org.apache.fop.pdf.PDFDeviceColorSpace;
import org.apache.fop.pdf.PDFFunction;
import org.apache.fop.pdf.PDFPattern;
import org.apache.fop.pdf.PDFShading;
import org.apache.fop.svg.PDFGraphics2D;

public class PDFGradientFactory extends GradientFactory {

    private final GradientRegistrar registrar;

    public PDFGradientFactory(PDFGraphics2D pdfGraphics2D) {
        this.registrar = pdfGraphics2D;
    }

    @Override
    public PDFPattern createGradient(boolean radial, PDFDeviceColorSpace theColorspace, List<Color> theColors,
            List<Double> theBounds, List<Double> theCoords, List<Double> theMatrix) {
        return (PDFPattern)makeGradient(radial, theColorspace, theColors, theBounds,
                theCoords, theMatrix);
    }

    @Override
    public Function makeFunction(int functionType, List<Double> theDomain,
            List<Double> theRange, List<Function> theFunctions,
            List<Double> theBounds, List<Double> theEncode) {
        Function newFunction = new PDFFunction(functionType, theDomain, theRange, theFunctions,
                    theBounds, theEncode);
        newFunction = registrar.registerFunction(newFunction);
        return newFunction;
    }

    public Function makeFunction(int functionType, List<Double> theDomain,
            List<Double> theRange, List<Double> theCZero, List<Double> theCOne,
            double theInterpolationExponentN) {
        Function newFunction = new PDFFunction(functionType, theDomain, theRange, theCZero,
                    theCOne, theInterpolationExponentN);
        newFunction = registrar.registerFunction(newFunction);
        return newFunction;
    }

    @Override
    public Shading makeShading(int theShadingType,
            PDFDeviceColorSpace theColorSpace, List<Double> theBackground, List<Double> theBBox,
            boolean theAntiAlias, List<Double> theCoords, List<Double> theDomain,
            Function theFunction, List<Integer> theExtend) {
        Shading newShading = new PDFShading(theShadingType, theColorSpace, theBackground,
                    theBBox, theAntiAlias, theCoords, theDomain, theFunction, theExtend);
        newShading = registrar.registerShading(newShading);
        return newShading;
    }

    @Override
    public Pattern makePattern(int thePatternType, Shading theShading, List theXUID,
            StringBuffer theExtGState, List<Double> theMatrix) {
        Pattern newPattern = new PDFPattern(thePatternType, theShading, theXUID, theExtGState,
                    theMatrix);
        newPattern = registrar.registerPattern(newPattern);
        return newPattern;
    }

}
