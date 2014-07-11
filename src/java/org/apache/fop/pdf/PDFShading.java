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

package org.apache.fop.pdf;

import java.util.List;

import org.apache.fop.render.shading.Shading;


/**
 * class representing a PDF Smooth Shading object.
 *
 * PDF Functions represent parameterized mathematical formulas and sampled representations with
 * arbitrary resolution. Functions are used in two areas: device-dependent
 * rasterization information for halftoning and transfer
 * functions, and color specification for smooth shading (a PDF 1.3 feature).
 *
 * All PDF Functions have a shadingType (0,2,3, or 4), a Domain, and a Range.
 */
public class PDFShading extends PDFObject {
    // Guts common to all function types

    /**
     * The name of the Shading e.g. "Shading1"
     */
    protected String shadingName = null;

    private final Shading shading;

    private final PDFFunction pdfFunction;

    /**
     * Constructor for Type 2 and 3
     *
     * @param shadingType 2 or 3 for axial or radial shading
     * @param colorSpace "DeviceRGB" or similar.
     * @param coords List of four (type 2) or 6 (type 3) Double
     * @param pdfFunction the Stitching (PDFfunction type 3) function,
     *                    even if it's stitching a single function
     */
    public PDFShading(int shadingType, PDFDeviceColorSpace colorSpace,
            List coords, PDFFunction pdfFunction) {
        shading = new Shading(shadingType, colorSpace, coords, pdfFunction.getFunction());
        this.pdfFunction = pdfFunction;
    }

    /**
     * Get the name of this shading.
     *
     * @return the name of the shading
     */
    public String getName() {
        return (this.shadingName);
    }

    /**
     * Sets the name of the shading
     * @param name the name of the shading pattern. Can be anything
     * without spaces. "Shading1" or "Sh1" are good examples.
     */
    public void setName(String name) {
        if (name.indexOf(" ") >= 0) {
            throw new IllegalArgumentException(
                    "Shading name must not contain any spaces");
        }
        this.shadingName = name;
    }

    /**
     * represent as PDF. Whatever the shadingType is, the correct
     * representation spits out. The sets of required and optional
     * attributes are different for each type, but if a required
     * attribute's object was constructed as null, then no error
     * is raised. Instead, the malformed PDF that was requested
     * by the construction is dutifully output.
     * This policy should be reviewed.
     *
     * @return the PDF string.
     */
    public String toPDFString() {
        Shading.FunctionRenderer functionRenderer = new Shading.FunctionRenderer() {

            public void outputFunction(StringBuilder out) {
                out.append(pdfFunction.referencePDF());
            }
        };
        StringBuilder out = new StringBuilder();
        shading.output(out, functionRenderer);
        return out.toString();
    }

    /** {@inheritDoc} */
    protected boolean contentEquals(PDFObject obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof PDFShading)) {
            return false;
        }
        Shading other = ((PDFShading) obj).shading;
        if (shading.getShadingType() != other.getShadingType()) {
            return false;
        }
        if (shading.isAntiAlias() != other.isAntiAlias()) {
            return false;
        }
        if (shading.getBitsPerCoordinate() != other.getBitsPerCoordinate()) {
            return false;
        }
        if (shading.getBitsPerFlag() != other.getBitsPerFlag()) {
            return false;
        }
        if (shading.getBitsPerComponent() != other.getBitsPerComponent()) {
            return false;
        }
        if (shading.getVerticesPerRow() != other.getVerticesPerRow()) {
            return false;
        }
        if (shading.getColorSpace() != null) {
            if (!shading.getColorSpace().equals(other.getColorSpace())) {
                return false;
            }
        } else if (other.getColorSpace() != null) {
            return false;
        }
        if (shading.getBackground() != null) {
            if (!shading.getBackground().equals(other.getBackground())) {
                return false;
            }
        } else if (other.getBackground() != null) {
            return false;
        }
        if (shading.getBBox() != null) {
            if (!shading.getBBox().equals(other.getBBox())) {
                return false;
            }
        } else if (other.getBBox() != null) {
            return false;
        }
        if (shading.getDomain() != null) {
            if (!shading.getDomain().equals(other.getDomain())) {
                return false;
            }
        } else if (other.getDomain() != null) {
            return false;
        }
        if (shading.getMatrix() != null) {
            if (!shading.getMatrix().equals(other.getMatrix())) {
                return false;
            }
        } else if (other.getMatrix() != null) {
            return false;
        }
        if (shading.getCoords() != null) {
            if (!shading.getCoords().equals(other.getCoords())) {
                return false;
            }
        } else if (other.getCoords() != null) {
            return false;
        }
        if (shading.getExtend() != null) {
            if (!shading.getExtend().equals(other.getExtend())) {
                return false;
            }
        } else if (other.getExtend() != null) {
            return false;
        }
        if (shading.getDecode() != null) {
            if (!shading.getDecode().equals(other.getDecode())) {
                return false;
            }
        } else if (other.getDecode() != null) {
            return false;
        }
        if (shading.getFunction() != null) {
            if (!shading.getFunction().equals(other.getFunction())) {
                return false;
            }
        } else if (other.getFunction() != null) {
            return false;
        }
        return true;
    }

}
