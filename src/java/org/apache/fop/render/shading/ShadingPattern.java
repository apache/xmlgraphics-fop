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
import org.apache.fop.pdf.PDFNumber;

/**
 * A class for writing shading objects for different output formats
 */
public class ShadingPattern {

    public interface ShadingRenderer {

        void outputFunction(StringBuffer out);
    }

    private Shading shading;

    private final ShadingRenderer shadingRenderer;

    /**
     * Constructor
     * @param shading The shading object from which to write the output
     */
    public ShadingPattern(Shading shading, ShadingRenderer shadingRenderer) {
        this.shading = shading;
        this.shadingRenderer = shadingRenderer;
    }

    /**
     * Outputs the given shading object to a String
     * @param colorSpace The Colospace (PDF and Postscript)
     * @param shadingType The shading type
     * @param background The background
     * @param bBox The bounding box
     * @param antiAlias Anti-aliasing
     * @return Returns the output string
     */
    public String toString(PDFDeviceColorSpace colorSpace, int shadingType, List background,
            List bBox, boolean antiAlias) {
        StringBuffer p = new StringBuffer(128);
        p.append("<<\n/ShadingType " + shadingType + " \n");
        if (colorSpace != null) {
            p.append("/ColorSpace /"
                     + colorSpace.getName() + " \n");
        }

        if (background != null) {
            p.append("/Background [ ");
            for (int bgIndex = 0; bgIndex < background.size(); bgIndex++) {
                p.append(PDFNumber.doubleOut((Double)background.get(bgIndex))
                         + " ");
            }
            p.append("] \n");
        }

        if (bBox
                != null) {    // I've never seen an example, so I guess this is right.
            p.append("/BBox [ ");
            for (int bboxIndex = 0; bboxIndex < bBox.size(); bboxIndex++) {
                p.append(PDFNumber.doubleOut((Double)bBox.get(bboxIndex))
                         + " ");
            }
            p.append("] \n");
        }

        if (antiAlias) {
            p.append("/AntiAlias " + antiAlias + " \n");
        }

        // Here's where we differentiate based on what type it is.
        switch (shadingType) {
        //Function based shading
        case 1: p = handleShadingType1(p); break;
        //Axial shading
        case 2:
        //Radial shading
        case 3: p = handleShadingType2or3(p); break;
        //Free-form Gouraud-shaded triangle meshes
        case 4:
        //Coons patch meshes
        case 6:
        //Tensor product patch meshes
        case 7: p = handleShadingType4or6or7(p); break;
        //Lattice Free form gouraud-shaded triangle mesh
        case 5: p = handleShadingType5(p); break;
        default: //Shading pattern outside expecting values
            break;
        }

        p.append(">>");

        return (p.toString());
    }


    /**
     * A method to write a type 1 shading object
     * @param p The StringBuffer to write the shading object
     * @return Returns the StringBuffer to which the shading object was written
     */
    public StringBuffer handleShadingType1(StringBuffer p) {
        if (shading.getDomain() != null) {
            p.append("/Domain [ ");
            for (int domainIndex = 0; domainIndex < shading.getDomain().size(); domainIndex++) {
                p.append(PDFNumber.doubleOut((Double)shading.getDomain().get(domainIndex))
                         + " ");
            }
            p.append("] \n");
        } else {
            p.append("/Domain [ 0 1 ] \n");
        }

        if (shading.getMatrix() != null) {
            p.append("/Matrix [ ");
            for (int matrixIndex = 0; matrixIndex < shading.getMatrix().size(); matrixIndex++) {
                p.append(PDFNumber.doubleOut((Double)shading.getMatrix().get(matrixIndex))
                         + " ");
            }
            p.append("] \n");
        }
        shadingRenderer.outputFunction(p);
        return p;
    }

    /**
     * A method to write a type 2 or 3 shading object
     * @param p The StringBuffer to write the shading object
     * @return Returns the StringBuffer to which the shading object was written
     */
    public StringBuffer handleShadingType2or3(StringBuffer p) {
        // 3 is radial shading (circular gradient)
        if (shading.getCoords() != null) {
            p.append("/Coords [ ");
            for (int coordIndex = 0; coordIndex < shading.getCoords().size(); coordIndex++) {
                p.append(PDFNumber.doubleOut((Double)shading.getCoords().get(coordIndex))
                         + " ");
            }
            p.append("] \n");
        }

        // DOMAIN
        if (shading.getDomain() != null) {
            p.append("/Domain [ ");
            for (int domainIndex = 0; domainIndex < shading.getDomain().size(); domainIndex++) {
                p.append(PDFNumber.doubleOut((Double)shading.getDomain().get(domainIndex))
                         + " ");
            }
            p.append("] \n");
        } else {
            p.append("/Domain [ 0 1 ] \n");
        }

        if (shading.getExtend() != null) {
            p.append("/Extend [ ");
            for (int extendIndex = 0; extendIndex < shading.getExtend().size(); extendIndex++) {
                p.append((shading.getExtend().get(extendIndex)) + " ");
            }

            p.append("] \n");
        } else {
            p.append("/Extend [ true true ] \n");
        }

        shadingRenderer.outputFunction(p);

        return p;
    }

    /**
     * A method to write a type 4, 6 or 7 shading object
     * @param p The StringBuffer to write the shading object
     * @return Returns the StringBuffer to which the shading object was written
     */
    public StringBuffer handleShadingType4or6or7(StringBuffer p) {
        // 6:coons patch meshes
        // 7://tensor product patch meshes (which no one ever uses)
        if (shading.getBitsPerCoordinate() > 0) {
            p.append("/BitsPerCoordinate " + shading.getBitsPerCoordinate()
                     + " \n");
        } else {
            p.append("/BitsPerCoordinate 1 \n");
        }

        if (shading.getBitsPerComponent() > 0) {
            p.append("/BitsPerComponent " + shading.getBitsPerComponent()
                     + " \n");
        } else {
            p.append("/BitsPerComponent 1 \n");
        }

        if (shading.getBitsPerFlag() > 0) {
            p.append("/BitsPerFlag " + shading.getBitsPerFlag() + " \n");
        } else {
            p.append("/BitsPerFlag 2 \n");
        }

        if (shading.getDecode() != null) {
            p.append("/Decode [ ");
            for (int decodeIndex = 0; decodeIndex < shading.getDecode().size(); decodeIndex++) {
                p.append((shading.getDecode().get(decodeIndex)) + " ");
            }

            p.append("] \n");
        }

        shadingRenderer.outputFunction(p);

        return p;
    }

    /**
     * A method to write a type 5 shading object
     * @param p The StringBuffer to write the shading object
     * @return Returns the StringBuffer to which the shading object was written
     */
    public StringBuffer handleShadingType5(StringBuffer p) {
        if (shading.getBitsPerCoordinate() > 0) {
            p.append("/BitsPerCoordinate " + shading.getBitsPerCoordinate()
                     + " \n");
        } else {
            p.append("/BitsPerCoordinate 1 \n");
        }

        if (shading.getBitsPerComponent() > 0) {
            p.append("/BitsPerComponent " + shading.getBitsPerComponent()
                     + " \n");
        } else {
            p.append("/BitsPerComponent 1 \n");
        }

        if (shading.getDecode() != null) {
            p.append("/Decode [ ");
            for (int decodeIndex = 0; decodeIndex < shading.getDecode().size(); decodeIndex++) {
                p.append((shading.getDecode().get(decodeIndex)) + " ");
            }

            p.append("] \n");
        }

        shadingRenderer.outputFunction(p);

        if (shading.getVerticesPerRow() > 0) {
            p.append("/VerticesPerRow " + shading.getVerticesPerRow() + " \n");
        } else {
            p.append("/VerticesPerRow 2 \n");
        }

        return p;
    }

}
