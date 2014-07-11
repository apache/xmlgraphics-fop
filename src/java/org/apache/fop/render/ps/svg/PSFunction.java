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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.fop.render.shading.Function;
import org.apache.fop.render.shading.FunctionPattern;

public class PSFunction extends Function {

    /**
     * Creates a Postscript function dictionary
     * @param theFunctionType The function type (0 = Sampled, 2 = Exponential
     * Interpolation, 3 = Stitching)
     * @param theDomain The function domain
     * @param theRange Range used for clipping
     * @param theFunctions An array of sub-functions such as determining the
     * colour values used in a gradient.
     * @param theBounds Bounds determines where each boundary exists for whatever
     * the function is mean't. In a gradient case, it would be the point between
     * colours.
     * @param theEncode The function encoding
     */
    public PSFunction(int theFunctionType, List<Double> theDomain,
            List<Double> theRange, List<Function> theFunctions,
            List<Double> theBounds, List<Double> theEncode) {
        super(theFunctionType, theDomain, theRange, theFunctions, theBounds, theEncode);
    }

    /**
     * Creates a Postscript function dictionary
     * @param theFunctionType The function type (0 = Sampled, 2 = Exponential
     * Interpolation, 3 = Stitching)
     * @param theDomain The function domain
     * @param theRange Range used for clipping
     * @param theCZero In a gradient, this would be the first colour
     * @param theCOne In a gradient, this would be the second colour
     * @param theInterpolationExponentN Determines the number of values
     * the function returns.
     */
    public PSFunction(int theFunctionType, List<Double> theDomain,
            List<Double> theRange, List<Double> theCZero, List<Double> theCOne,
            double theInterpolationExponentN) {
        super(theFunctionType, theDomain, theRange, theCZero, theCOne, theInterpolationExponentN);
    }

    /**
     * Outputs the function to a byte array
     */
    public byte[] toByteString() {
        FunctionPattern pattern = new FunctionPattern(this);
        try {
            List<String> functionsStrings = new ArrayList<String>(getFunctions().size());
            for (Function f : getFunctions()) {
                functionsStrings.add(new String(((PSFunction) f).toByteString(), "UTF-8"));
            }
            return pattern.toWriteableString(functionsStrings).getBytes("UTF-8");
        } catch (UnsupportedEncodingException ex) {
            //This should have been made an enum type to avoid throwing exceptions.
            return new byte[0];
        }
    }

}
