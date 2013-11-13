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

    private Shading shading;

    /**
     * Constructor
     * @param shading The shading object from which to write the output
     */
    public ShadingPattern(Shading shading) {
        this.shading = shading;
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
        case 1: p = shading.handleShadingType1(p); break;
        //Axial shading
        case 2:
        //Radial shading
        case 3: p = shading.handleShadingType2or3(p); break;
        //Free-form Gouraud-shaded triangle meshes
        case 4:
        //Coons patch meshes
        case 6:
        //Tensor product patch meshes
        case 7: p = shading.handleShadingType4or6or7(p); break;
        //Lattice Free form gouraud-shaded triangle mesh
        case 5: p = shading.handleShadingType5(p); break;
        default: //Shading pattern outside expecting values
            break;
        }

        p.append(">>");

        return (p.toString());
    }
}
