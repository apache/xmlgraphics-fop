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

package org.apache.fop.render.gradient;

import java.util.List;

import org.apache.fop.pdf.PDFDeviceColorSpace;


public class Shading {

    public interface FunctionRenderer {

        void outputFunction(StringBuilder out);
    }

    /**
     * Required: The Type of shading (1,2,3,4,5,6,7)
     */
    private final int shadingType;

    /**
     * A ColorSpace representing the colorspace. "DeviceRGB" is an example.
     */
    private final PDFDeviceColorSpace colorSpace;

    /**
     * Required for Type 2: An Array of four numbers specifying
     *                      the starting and ending coordinate pairs
     * Required for Type 3: An Array of six numbers [x0,y0,r0,x1,y1,r1]
     *                      specifying the centers and radii of
     *                      the starting and ending circles.
     */
    private final List<Double> coords;

    /**
     * Required for Type 1, 2, and 3:
     * The object of the color mapping function (usually type 2 or 3).
     * Optional for Type 4,5,6, and 7: When it's nearly the same thing.
     */
    private final Function function;

    /**
     * Optional: A List specifying the clipping rectangle
     */
    private final List<Double> bbox;

    /**
     * Optional for Type 1: A transformation matrix
     */
    private final List<Double> matrix;

    /**
     * The background color. Since shading is opaque,
     * this is very rarely used.
     */
    private final List<Double> background;

    /**
     * Optional for Type 1: Array of four numbers, xmin, xmax, ymin, ymax.
     *                      Default is [0 1 0 1]
     * Optional for Type 2: An array of two numbers between which the blend
     *                      varies between start and end points. Default is 0, 1.
     * Optional for Type 3: An array of two numbers between which the blend
     *                      varies between start and end points. Default is 0, 1.
     */
    private final List<Double> domain;

    /**
     * Required for Type 4,5,6, and 7: Array of Doubles which specifies
     * how to decode coordinate and color component values.
     * Each type has a differing number of decode array members, so check
     * the spec.
     * Page 303 in PDF Spec 1.3
     */
    private final List<Double> decode;

    /**
     * Required for Type 2+3: An Array of two boolean values specifying
     * whether to extend the start and end colors past the start
     * and end points, respectively.
     * Default is false, false.
     */
    private final List<Boolean> extend;

    /**
     * Required for Type 4,5,6, and 7: Specifies the number of bits used
     * to represent each vertex coordinate.
     * Allowed to be 1,2,4,8,12,16,24, or 32.
     */
    private final int bitsPerCoordinate;

    /**
     * Required for Type 4,5,6, and 7: Specifies the number of bits used
     * to represent the edge flag for each vertex.
     * Allowed to be 2,4,or 8, while the Edge flag itself is allowed to
     * be 0,1 or 2.
     */
    private final int bitsPerFlag;

    /**
     * Optional: A flag whether or not to filter the shading function
     * to prevent aliasing artifacts. Default is false.
     */
    private final boolean antiAlias;

    /**
     * Required for Type 4,5,6, and 7: Specifies the number of bits used
     * to represent each color coordinate.
     * Allowed to be 1,2,4,8,12, or 16
     */
    private final int bitsPerComponent;

    /**
     * Required for Type 5:The number of vertices in each "row" of
     * the lattice; it must be greater than or equal to 2.
     */
    private final int verticesPerRow;

    public Shading(int shadingType, PDFDeviceColorSpace colorSpace,
            List<Double> coords, Function function) {
        this.shadingType = shadingType;
        this.colorSpace = colorSpace;
        this.background = null;
        this.bbox = null;
        this.antiAlias = false;
        this.coords = coords;
        this.domain = null;
        this.function = function;
        this.extend = null;
        this.matrix = null;
        this.decode = null;
        this.bitsPerCoordinate = 0;
        this.bitsPerFlag = 0;
        this.bitsPerComponent = 0;
        this.verticesPerRow = 0;
    }

    public int getShadingType() {
        return shadingType;
    }

    public PDFDeviceColorSpace getColorSpace() {
        return colorSpace;
    }

    public List<Double> getCoords() {
        return coords;
    }

    public Function getFunction() {
        return function;
    }

    public List<Double> getBBox() {
        return bbox;
    }

    public List<Double> getMatrix() {
        return matrix;
    }

    public List<Double> getBackground() {
        return background;
    }

    public List<Double> getDomain() {
        return domain;
    }

    public List<Double> getDecode() {
        return decode;
    }

    public List<Boolean> getExtend() {
        return extend;
    }

    public int getBitsPerCoordinate() {
        return bitsPerCoordinate;
    }

    public int getBitsPerFlag() {
        return bitsPerFlag;
    }

    public boolean isAntiAlias() {
        return antiAlias;
    }

    public int getBitsPerComponent() {
        return bitsPerComponent;
    }

    public int getVerticesPerRow() {
        return verticesPerRow;
    }

    public void output(StringBuilder out, FunctionRenderer functionRenderer) {
        out.append("<<\n/ShadingType " + shadingType + "\n");
        if (colorSpace != null) {
            out.append("/ColorSpace /" + colorSpace.getName() + "\n");
        }

        if (background != null) {
            out.append("/Background ");
            GradientMaker.outputDoubles(out, background);
            out.append("\n");
        }

        if (bbox != null) {
            out.append("/BBox");
            GradientMaker.outputDoubles(out, bbox);
            out.append("\n");
        }

        if (antiAlias) {
            out.append("/AntiAlias " + antiAlias + "\n");
        }

        switch (shadingType) {
        // Function based shading
        case 1: outputShadingType1(out, functionRenderer); break;
        // Axial shading
        case 2:
        // Radial shading
        case 3: outputShadingType2or3(out, functionRenderer); break;
        // Free-form Gouraud-shaded triangle meshes
        case 4:
        // Coons patch meshes
        case 6:
        // Tensor product patch meshes
        case 7: outputShadingType4or6or7(out, functionRenderer); break;
        // Lattice Free form gouraud-shaded triangle mesh
        case 5: outputShadingType5(out, functionRenderer); break;
        default: throw new UnsupportedOperationException("Shading type " + shadingType);
        }

        out.append(">>");
    }

    private void outputShadingType1(StringBuilder out, Shading.FunctionRenderer functionRenderer) {
        if (domain != null) {
            out.append("/Domain ");
            GradientMaker.outputDoubles(out, domain);
            out.append("\n");
        } else {
            out.append("/Domain [ 0 1 ] \n");
        }

        if (matrix != null) {
            out.append("/Matrix ");
            GradientMaker.outputDoubles(out, matrix);
            out.append("\n");
        }
        outputFunction(out, functionRenderer);
    }

    private void outputShadingType2or3(StringBuilder out, Shading.FunctionRenderer functionRenderer) {
        if (coords != null) {
            out.append("/Coords ");
            GradientMaker.outputDoubles(out, coords);
            out.append("\n");
        }

        if (domain != null) {
            out.append("/Domain ");
            GradientMaker.outputDoubles(out, domain);
            out.append("\n");
        } else {
            out.append("/Domain [ 0 1 ] \n");
        }

        if (extend != null) {
            out.append("/Extend [");
            for (Boolean b : extend) {
                out.append(b);
                out.append(" ");
            }
            out.append("\n");
        } else {
            out.append("/Extend [ true true ] \n");
        }

        outputFunction(out, functionRenderer);
    }

    private void outputShadingType4or6or7(StringBuilder out, Shading.FunctionRenderer functionRenderer) {
        if (bitsPerCoordinate > 0) {
            out.append("/BitsPerCoordinate " + bitsPerCoordinate + "\n");
        } else {
            out.append("/BitsPerCoordinate 1 \n");
        }

        if (bitsPerComponent > 0) {
            out.append("/BitsPerComponent " + bitsPerComponent + "\n");
        } else {
            out.append("/BitsPerComponent 1 \n");
        }

        if (bitsPerFlag > 0) {
            out.append("/BitsPerFlag " + bitsPerFlag + "\n");
        } else {
            out.append("/BitsPerFlag 2 \n");
        }

        if (decode != null) {
            out.append("/Decode ");
            GradientMaker.outputDoubles(out, decode);
            out.append("\n");
        }

        outputFunction(out, functionRenderer);
    }

    private void outputShadingType5(StringBuilder out, Shading.FunctionRenderer functionRenderer) {
        if (bitsPerCoordinate > 0) {
            out.append("/BitsPerCoordinate " + bitsPerCoordinate + "\n");
        } else {
            out.append("/BitsPerCoordinate 1 \n");
        }

        if (bitsPerComponent > 0) {
            out.append("/BitsPerComponent " + bitsPerComponent + "\n");
        } else {
            out.append("/BitsPerComponent 1 \n");
        }

        if (decode != null) {
            out.append("/Decode ");
            GradientMaker.outputDoubles(out, decode);
            out.append("\n");
        }

        outputFunction(out, functionRenderer);

        if (verticesPerRow > 0) {
            out.append("/VerticesPerRow " + verticesPerRow + "\n");
        } else {
            out.append("/VerticesPerRow 2 \n");
        }
    }

    private void outputFunction(StringBuilder out, FunctionRenderer functionRenderer) {
        if (function != null) {
            out.append("/Function ");
            functionRenderer.outputFunction(out);
            out.append("\n");
        }
    }
}
