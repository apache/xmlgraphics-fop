/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

// Java
import java.util.List;
import java.io.OutputStream;
import java.io.IOException;

/**
 * class representing a PDF Function.
 *
 * PDF Functions represent parameterized mathematical formulas and sampled representations with
 * arbitrary resolution. Functions are used in two areas: device-dependent
 * rasterization information for halftoning and transfer
 * functions, and color specification for smooth shading (a PDF 1.3 feature).
 *
 * All PDF Functions have a FunctionType (0,2,3, or 4), a Domain, and a Range.
 */
public class PDFPattern extends PDFPathPaint {

    /**
     * The resources associated with this pattern
     */
    protected PDFResources resources = null;

    /**
     * Either one (1) for tiling, or two (2) for shading.
     */
    protected int patternType = 2;      // Default

    /**
     * The name of the pattern such as "Pa1" or "Pattern1"
     */
    protected String patternName = null;

    /**
     * 1 for colored pattern, 2 for uncolored
     */
    protected int paintType = 2;

    /**
     * 1 for constant spacing, 2 for no distortion, and 3 for fast rendering
     */
    protected int tilingType = 1;

    /**
     * List of Doubles representing the Bounding box rectangle
     */
    protected List bBox = null;

    /**
     * Horizontal spacing
     */
    protected double xStep = -1;

    /**
     * Vertical spacing
     */
    protected double yStep = -1;

    /**
     * The Shading object comprising the Type 2 pattern
     */
    protected PDFShading shading = null;

    /**
     * List of Integers represetning the Extended unique Identifier
     */
    protected List xUID = null;

    /**
     * TODO use PDFGState
     * String representing the extended Graphics state.
     * Probably will never be used like this.
     */
    protected StringBuffer extGState = null;

    /**
     * List of Doubles representing the Transformation matrix.
     */
    protected List matrix = null;

    /**
     * The stream of a pattern
     */
    protected StringBuffer patternDataStream = null;

    /**
     * Create a tiling pattern (type 1).
     *
     * @param theResources the resources associated with this pattern
     * @param thePatternType the type of pattern, which is 1 for tiling.
     * @param thePaintType 1 or 2, colored or uncolored.
     * @param theTilingType 1, 2, or 3, constant spacing, no distortion, or faster tiling
     * @param theBBox List of Doubles: The pattern cell bounding box
     * @param theXStep horizontal spacing
     * @param theYStep vertical spacing
     * @param theMatrix Optional List of Doubles transformation matrix
     * @param theXUID Optional vector of Integers that uniquely identify the pattern
     * @param thePatternDataStream The stream of pattern data to be tiled.
     */
    public PDFPattern(PDFResources theResources, int thePatternType,    // 1
                      int thePaintType, int theTilingType, List theBBox, 
                      double theXStep, double theYStep, 
                      List theMatrix, List theXUID,
                      StringBuffer thePatternDataStream) {
        super();
        this.resources = theResources;
        // This next parameter is implicit to all constructors, and is
        // not directly passed.

        this.patternType = 1;    // thePatternType;
        this.paintType = thePaintType;
        this.tilingType = theTilingType;
        this.bBox = theBBox;
        this.xStep = theXStep;
        this.yStep = theYStep;
        this.matrix = theMatrix;
        this.xUID = theXUID;
        this.patternDataStream = thePatternDataStream;
    }

    /**
     * Create a type 2 pattern (smooth shading)
     *
     * @param thePatternType the type of the pattern, which is 2, smooth shading
     * @param theShading the PDF Shading object that comprises this pattern
     * @param theXUID optional:the extended unique Identifier if used.
     * @param theExtGState optional: the extended graphics state, if used.
     * @param theMatrix Optional:List of Doubles that specify the matrix.
     */
    public PDFPattern(int thePatternType, PDFShading theShading,
                      List theXUID, StringBuffer theExtGState,
                      List theMatrix) {
        super();

        this.patternType = 2;             // thePatternType;
        this.shading = theShading;
        this.xUID = theXUID;
        // this isn't really implemented, so it should always be null.
        // I just don't want to have to add a new parameter once it is implemented.
        this.extGState = theExtGState;    // always null
        this.matrix = theMatrix;
    }

    /**
     * Get the name of the pattern
     *
     * @return String representing the name of the pattern.
     */
    public String getName() {
        return (this.patternName);
    }

    /**
     * Sets the name of the pattern.
     * @param name the name of the pattern. Can be anything
     * without spaces. "Pattern1" or "Pa1" are good examples.
     */
    public void setName(String name) {
        if (name.indexOf(" ") >= 0) {
            throw new IllegalArgumentException(
                    "Pattern name must not contain any spaces");
        }
        this.patternName = name;
    }

    /**
     * Get the PDF command for setting to this pattern.
     *
     * @param fillNotStroke if true fill otherwise stroke
     * @return the PDF string for setting the pattern
     */
    public String getColorSpaceOut(boolean fillNotStroke) {
        if (fillNotStroke) {    // fill but no stroke
            return ("/Pattern cs /" + this.getName() + " scn \n");
        } else {                // stroke (or border)
            return ("/Pattern CS /" + this.getName() + " SCN \n");
        }
    }

    /**
     * represent as PDF. Whatever the FunctionType is, the correct
     * representation spits out. The sets of required and optional
     * attributes are different for each type, but if a required
     * attribute's object was constructed as null, then no error
     * is raised. Instead, the malformed PDF that was requested
     * by the construction is dutifully output.
     * This policy should be reviewed.
     *
     * @param stream the stream to write to
     * @throws IOException if there is an error writing to the stream
     * @return the PDF string.
     */
    protected int output(OutputStream stream) throws IOException {

        int vectorSize = 0;
        int tempInt = 0;
        byte[] buffer;
        StringBuffer p = new StringBuffer(64);
        p.append(getObjectID());
        p.append("<< \n/Type /Pattern \n");

        if (this.resources != null) {
            p.append("/Resources " + this.resources.referencePDF() + " \n");
        }

        p.append("/PatternType " + this.patternType + " \n");

        PDFStream pdfStream = null;
        StreamCache encodedStream = null;

        if (this.patternType == 1) {
            p.append("/PaintType " + this.paintType + " \n");
            p.append("/TilingType " + this.tilingType + " \n");

            if (this.bBox != null) {
                vectorSize = this.bBox.size();
                p.append("/BBox [ ");
                for (tempInt = 0; tempInt < vectorSize; tempInt++) {
                    p.append(PDFNumber.doubleOut((Double)this.bBox.get(tempInt)));
                    p.append(" ");
                }
                p.append("] \n");
            }
            p.append("/XStep " + PDFNumber.doubleOut(new Double(this.xStep))
                     + " \n");
            p.append("/YStep " + PDFNumber.doubleOut(new Double(this.yStep))
                     + " \n");

            if (this.matrix != null) {
                vectorSize = this.matrix.size();
                p.append("/Matrix [ ");
                for (tempInt = 0; tempInt < vectorSize; tempInt++) {
                    p.append(PDFNumber.doubleOut((Double)this.matrix.get(tempInt)));
                    p.append(" ");
                }
                p.append("] \n");
            }

            if (this.xUID != null) {
                vectorSize = this.xUID.size();
                p.append("/XUID [ ");
                for (tempInt = 0; tempInt < vectorSize; tempInt++) {
                    p.append(((Integer)this.xUID.get(tempInt)) + " ");
                }
                p.append("] \n");
            }

            // don't forget the length of the stream.
            if (this.patternDataStream != null) {
                pdfStream = new PDFStream();
                pdfStream.setDocument(getDocumentSafely());
                pdfStream.add(this.patternDataStream.toString());
                pdfStream.getFilterList().addDefaultFilters(
                        getDocument().getFilterMap(), 
                        PDFFilterList.CONTENT_FILTER);
                encodedStream = pdfStream.encodeStream();        
                p.append(pdfStream.getFilterList().buildFilterDictEntries());
                p.append("/Length " + (encodedStream.getSize() + 1)
                         + " \n");
            }

        } else {
            // if (this.patternType ==2)
            // Smooth Shading...
            if (this.shading != null) {
                p.append("/Shading " + this.shading.referencePDF() + " \n");
            }

            if (this.xUID != null) {
                vectorSize = this.xUID.size();
                p.append("/XUID [ ");
                for (tempInt = 0; tempInt < vectorSize; tempInt++) {
                    p.append(((Integer)this.xUID.get(tempInt)) + " ");
                }
                p.append("] \n");
            }

            if (this.extGState != null) {
                p.append("/ExtGState " + this.extGState + " \n");
            }

            if (this.matrix != null) {
                vectorSize = this.matrix.size();
                p.append("/Matrix [ ");
                for (tempInt = 0; tempInt < vectorSize; tempInt++) {
                    p.append(PDFNumber.doubleOut((Double)this.matrix.get(tempInt)));
                    p.append(" ");
                }
                p.append("] \n");
            }
        }         // end of if patterntype =1...else 2.

        p.append(">> \n");

        buffer = encode(p.toString());
        int length = buffer.length;
        stream.write(buffer);

        // stream representing the function
        if (pdfStream != null) {
            length += pdfStream.outputStreamData(encodedStream, stream);
        }

        buffer = encode("\nendobj\n");
        stream.write(buffer);
        length += buffer.length;

        return length;
    }

    /**
     * Output PDF bytes, not used.
     * @return returns null
     */
    public byte[] toPDF() { return null; }

    /**
     * Check if this pattern is equal to another.
     *
     * @param obj the object to compare against
     * @return true if the patterns are equal
     */
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof PDFPattern)) {
            return false;
        }
        PDFPattern patt = (PDFPattern)obj;
        if (patternType != patt.patternType) {
            return false;
        }
        if (paintType != patt.paintType) {
            return false;
        }
        if (tilingType != patt.tilingType) {
            return false;
        }
        if (xStep != patt.xStep) {
            return false;
        }
        if (yStep != patt.yStep) {
            return false;
        }
        if (bBox != null) {
            if (!bBox.equals(patt.bBox)) {
                return false;
            }
        } else if (patt.bBox != null) {
            return false;
        }
        if (bBox != null) {
            if (!bBox.equals(patt.bBox)) {
                return false;
            }
        } else if (patt.bBox != null) {
            return false;
        }
        if (xUID != null) {
            if (!xUID.equals(patt.xUID)) {
                return false;
            }
        } else if (patt.xUID != null) {
            return false;
        }
        if (extGState != null) {
            if (!extGState.equals(patt.extGState)) {
                return false;
            }
        } else if (patt.extGState != null) {
            return false;
        }
        if (matrix != null) {
            if (!matrix.equals(patt.matrix)) {
                return false;
            }
        } else if (patt.matrix != null) {
            return false;
        }
        if (resources != null) {
            if (!resources.equals(patt.resources)) {
                return false;
            }
        } else if (patt.resources != null) {
            return false;
        }
        if (shading != null) {
            if (!shading.equals(patt.shading)) {
                return false;
            }
        } else if (patt.shading != null) {
            return false;
        }
        if (patternDataStream != null) {
            if (!patternDataStream.equals(patt.patternDataStream)) {
                return false;
            }
        } else if (patt.patternDataStream != null) {
            return false;
        }

        return true;
    }

}
