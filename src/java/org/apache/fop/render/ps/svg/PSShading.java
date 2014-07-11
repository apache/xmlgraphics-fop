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

import java.util.ArrayList;
import java.util.List;

import org.apache.fop.pdf.PDFDeviceColorSpace;
import org.apache.fop.pdf.PDFNumber;
import org.apache.fop.render.shading.Function;
import org.apache.fop.render.shading.FunctionPattern;
import org.apache.fop.render.shading.Shading;
import org.apache.fop.render.shading.ShadingPattern;

public class PSShading implements Shading {

    /**
     * Required: The Type of shading (1,2,3,4,5,6,7)
     */
    protected int shadingType = 3;    // Default

    /**
     * A ColorSpace representing the colorspace. "DeviceRGB" is an example.
     */
    protected PDFDeviceColorSpace colorSpace = null;

    /**
     * The background color. Since shading is opaque,
     * this is very rarely used.
     */
    protected List background = null;

    /**
     * Optional: A List specifying the clipping rectangle
     */
    protected List bBox = null;

    /**
     * Optional: A flag whether or not to filter the shading function
     * to prevent aliasing artifacts. Default is false.
     */
    protected boolean antiAlias = false;

    /**
     * Optional for Type 1: Array of four numbers, xmin, xmax, ymin, ymax.
     *                      Default is [0 1 0 1]
     * Optional for Type 2: An array of two numbers between which the blend
     *                      varies between start and end points. Default is 0, 1.
     * Optional for Type 3: An array of two numbers between which the blend
     *                      varies between start and end points. Default is 0, 1.
     */
    protected List domain = null;

    /**
     * Required for Type 1, 2, and 3:
     * The object of the color mapping function (usually type 2 or 3).
     * Optional for Type 4,5,6, and 7: When it's nearly the same thing.
     */
    protected Function function;

    /**
     * Required for Type 2: An Array of four numbers specifying
     *                      the starting and ending coordinate pairs
     * Required for Type 3: An Array of six numbers [x0,y0,r0,x1,y1,r1]
     *                      specifying the centers and radii of
     *                      the starting and ending circles.
     */
    protected List coords = null;

    /**
     * Required for Type 2+3: An Array of two boolean values specifying
     * whether to extend the start and end colors past the start
     * and end points, respectively.
     * Default is false, false.
     */
    protected List extend = null;

    /**
     * Constructor for Type 2 and 3
     *
     * @param theShadingType 2 or 3 for axial or radial shading
     * @param theColorSpace "DeviceRGB" or similar.
     * @param theBackground theBackground An array of color components appropriate to the
     * colorspace key specifying a single color value.
     * This key is used by the f operator buy ignored by the sh operator.
     * @param theBBox List of double's representing a rectangle
     * in the coordinate space that is current at the
     * time of shading is imaged. Temporary clipping
     * boundary.
     * @param theAntiAlias Default is false
     * @param theCoords List of four (type 2) or 6 (type 3) Double
     * @param theDomain List of Doubles specifying the domain
     * @param theFunction the Stitching (PDFfunction type 3) function,
     *                    even if it's stitching a single function
     * @param theExtend List of Booleans of whether to extend the start
     *                  and end colors past the start and end points
     * The default is [false, false]
     */
    public PSShading(int theShadingType, PDFDeviceColorSpace theColorSpace,
                      List<Double> theBackground, List<Double> theBBox,
                      boolean theAntiAlias, List<Double> theCoords,
                      List<Double> theDomain, Function theFunction,
                      List<Integer> theExtend) {
        this.shadingType = theShadingType;    // 2 or 3
        this.colorSpace = theColorSpace;
        this.background = theBackground;
        this.bBox = theBBox;
        this.antiAlias = theAntiAlias;

        this.coords = theCoords;
        this.domain = theDomain;
        this.function = theFunction;
        this.extend = theExtend;
    }

    /**
     * represent as PS. Whatever the shadingType is, the correct
     * representation spits out. The sets of required and optional
     * attributes are different for each type, but if a required
     * attribute's object was constructed as null, then no error
     * is raised. Instead, the malformed PS that was requested
     * by the construction is dutifully output.
     * This policy should be reviewed.
     *
     * @return the PDF string.
     */
    public String toString() {
        ShadingPattern pattern = new ShadingPattern(this);
        return pattern.toString(colorSpace, shadingType, background, bBox, antiAlias);
    }

    /**
     * A method to write a type 2 or 3 shading object
     * @param p The StringBuffer to write the shading object
     * @return Returns the StringBuffer to which the shading object was written
     */
    public StringBuffer handleShadingType2or3(StringBuffer p) {
        if (this.coords != null) {
            p.append("\t/Coords [ ");
            for (int coordIndex = 0; coordIndex < coords.size(); coordIndex++) {
                p.append(PDFNumber.doubleOut((Double)this.coords.get(coordIndex))
                         + " ");
            }
            p.append("] \n");
        }

        // DOMAIN
        if (this.domain != null) {
            p.append("\t/Domain [ ");
            for (int domainIndex = 0; domainIndex < domain.size(); domainIndex++) {
                p.append(PDFNumber.doubleOut((Double)this.domain.get(domainIndex))
                         + " ");
            }
            p.append("] \n");
        } else {
            p.append("\t/Domain [ 0 1 ] \n");
        }

        if (this.extend != null) {
            p.append("\t/Extend [ ");
            for (int extendIndex = 0; extendIndex < extend.size(); extendIndex++) {
                p.append((this.extend.get(extendIndex)) + " ");
            }

            p.append("] \n");
        } else {
            p.append("\t/Extend [ true true ] \n");
        }


        if (this.function != null) {
            p.append("\t/Function ");
            p.append(functionToString(function) + " \n");
        }
        return p;
    }

    private String functionToString(Function function) {
        FunctionPattern pattern = new FunctionPattern(function);
        List<String> functionsStrings = new ArrayList<String>(function.getFunctions().size());
        for (Function f : function.getFunctions()) {
            functionsStrings.add(functionToString(f));
        }
        return pattern.toWriteableString(functionsStrings);
    }

    /**
     * A method to write a type 1 shading object
     * @param p The StringBuffer to write the shading object
     * @return Returns the StringBuffer to which the shading object was written
     */
    public StringBuffer handleShadingType1(StringBuffer p) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * A method to write a type 4, 6 or 7 shading object
     * @param p The StringBuffer to write the shading object
     * @return Returns the StringBuffer to which the shading object was written
     */
    public StringBuffer handleShadingType4or6or7(StringBuffer p) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * A method to write a type 5 shading object
     * @param p The StringBuffer to write the shading object
     * @return Returns the StringBuffer to which the shading object was written
     */
    public StringBuffer handleShadingType5(StringBuffer p) {
        // TODO Auto-generated method stub
        return null;
    }
}
