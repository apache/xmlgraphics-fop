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

import java.util.List;

import org.apache.fop.pdf.PDFDeviceColorSpace;
import org.apache.fop.render.ps.svg.PSFunction;
import org.apache.fop.render.ps.svg.PSPattern;
import org.apache.fop.render.ps.svg.PSShading;

public class PSGradientFactory extends GradientFactory<PSPattern> {

    public Function makeFunction(int functionType, List<Double> theDomain,
            List<Double> theRange, List<Function> theFunctions,
            List<Double> theBounds, List<Double> theEncode) {
        Function newFunction = new PSFunction(functionType, theDomain, theRange, theFunctions,
                    theBounds, theEncode);
        return newFunction;
    }

    @Override
    public Function makeFunction(int functionType, List<Double> theDomain,
            List<Double> theRange, List<Double> theCZero, List<Double> theCOne,
            double theInterpolationExponentN) {
        Function newFunction = new PSFunction(functionType, theDomain, theRange, theCZero,
                    theCOne, theInterpolationExponentN);
        return newFunction;
    }

    @Override
    public Shading makeShading(int theShadingType,
            PDFDeviceColorSpace theColorSpace, List<Double> theBackground, List<Double> theBBox,
            boolean theAntiAlias, List<Double> theCoords, List<Double> theDomain,
            Function theFunction, List<Integer> theExtend) {
        Shading newShading = new PSShading(theShadingType, theColorSpace, theBackground, theBBox,
                    theAntiAlias, theCoords, theDomain, theFunction, theExtend);
        return newShading;
    }

    @Override
    public PSPattern makePattern(int thePatternType, Shading theShading, List theXUID,
            StringBuffer theExtGState, List<Double> theMatrix) {
        return new PSPattern(thePatternType, theShading, theXUID, theExtGState, theMatrix);
    }
}
