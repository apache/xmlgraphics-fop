/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

// Java...
import java.util.ArrayList;

// FOP
import org.apache.fop.datatypes.ColorSpace;

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

    /**
     * Required: The Type of shading (1,2,3,4,5,6,7)
     */
    protected int shadingType = 3;    // Default

    /**
     * A ColorSpace representing the colorspace. "DeviceRGB" is an example.
     */
    // protected StringBuffer colorSpace = null;
    protected ColorSpace colorSpace = null;

    /**
     * The background color. Since shading is opaque,
     * this is very rarely used.
     */
    protected ArrayList background = null;

    /**
     * Optional: A ArrayList specifying the clipping rectangle
     */
    protected ArrayList bBox = null;

    /**
     * Optional: A flag whether or not to filter the shading function
     * to prevent aliasing artifacts. Default is false.
     */
    protected boolean antiAlias = false;

    /**
     * Optional for Type 1: Array of four numbers, xmin, xmax, ymin, ymax. Default is [0 1 0 1]
     * Optional for Type 2: An array of two numbers between which the blend varies between start and end points. Default is 0, 1.
     * Optional for Type 3: An array of two numbers between which the blend varies between start and end points. Default is 0, 1.
     */

    protected ArrayList domain = null;

    /**
     * Optional for Type 1: A transformation matrix
     */
    protected ArrayList matrix = null;

    /**
     * Required for Type 1, 2, and 3:
     * The object of the color mapping function (usually type 2 or 3).
     * Optional for Type 4,5,6, and 7: When it's nearly the same thing.
     */
    protected PDFFunction function = null;

    /**
     * Required for Type 2: An Array of four numbers specifying the starting and ending coordinate pairs
     * Required for Type 3: An Array of six numbers [x0,y0,r0,x1,y1,r1] specifying the centers and radii of
     * the starting and ending circles.
     */
    protected ArrayList coords = null;

    /**
     * Required for Type 2+3: An Array of two boolean values specifying whether to extend the
     * start and end colors past the start and end points,
     * respectively. Default is false, false.
     */
    protected ArrayList extend = null;

    /**
     * Required for Type 4,5,6, and 7: Specifies the number of bits used to represent each vertex coordinate.
     * Allowed to be 1,2,4,8,12,16,24, or 32.
     */
    protected int bitsPerCoordinate = 0;

    /**
     * Required for Type 4,5,6, and 7: Specifies the number of bits used to represent the edge flag for each vertex.
     * Allowed to be 2,4,or 8, while the Edge flag itself is allowed to be 0,1 or 2.
     */
    protected int bitsPerFlag = 0;

    /**
     * Required for Type 4,5,6, and 7: Array of Doubles which specifies how to decode coordinate and color component values.
     * Each type has a differing number of decode array members, so check the spec.
     * Page 303 in PDF Spec 1.3
     */
    protected ArrayList decode = null;

    /**
     * Required for Type 4,5,6, and 7: Specifies the number of bits used to represent each color coordinate.
     * Allowed to be 1,2,4,8,12, or 16
     */
    protected int bitsPerComponent = 0;

    /**
     * Required for Type 5:The number of vertices in each "row" of the lattice; it must be greater than or equal to 2.
     */
    protected int verticesPerRow = 0;

    /**
     * Constructor for type function based shading
     *
     * @param theNumber The object number of this PDF object
     * @param theShadingName The name of the shading pattern. Can be anything
     * without spaces. "Shading1" or "Sh1" are good examples.
     * @param theShadingType The type of shading object, which should be 1 for function
     * based shading.
     * @param theColorSpace The colorspace is 'DeviceRGB' or something similar.
     * @param theBackground An array of color components appropriate to the
     * colorspace key specifying a single color value.
     * This key is used by the f operator buy ignored by the sh operator.
     * @param theBBox ArrayList of double's representing a rectangle
     * in the coordinate space that is current at the
     * time of shading is imaged. Temporary clipping
     * boundary.
     * @param theAntiAlias Whether or not to anti-alias.
     * @param theDomain Optional vector of Doubles specifying the domain.
     * @param theMatrix ArrayList of Doubles specifying the matrix.
     * If it's a pattern, then the matrix maps it to pattern space.
     * If it's a shading, then it maps it to current user space.
     * It's optional, the default is the identity matrix
     * @param theFunction The PDF Function that maps an (x,y) location to a color
     */
    public PDFShading(int theNumber, String theShadingName,
                      int theShadingType, ColorSpace theColorSpace,
                      ArrayList theBackground, ArrayList theBBox,
                      boolean theAntiAlias, ArrayList theDomain,
                      ArrayList theMatrix, PDFFunction theFunction) {
        super(theNumber);
        this.shadingName = theShadingName;
        this.shadingType = theShadingType;    // 1
        this.colorSpace = theColorSpace;
        this.background = theBackground;
        this.bBox = theBBox;
        this.antiAlias = theAntiAlias;

        this.domain = theDomain;
        this.matrix = theMatrix;
        this.function = theFunction;

    }

    /**
     * Constructor for Type 2 and 3
     *
     * @param theNumber The object number of this PDF object.
     * @param theShadingName The name of the shading pattern. Can be anything
     * without spaces. "Shading1" or "Sh1" are good examples.
     * @param theShadingType 2 or 3 for axial or radial shading
     * @param theColorSpace "DeviceRGB" or similar.
     * @param theBackground theBackground An array of color components appropriate to the
     * colorspace key specifying a single color value.
     * This key is used by the f operator buy ignored by the sh operator.
     * @param theBBox ArrayList of double's representing a rectangle
     * in the coordinate space that is current at the
     * time of shading is imaged. Temporary clipping
     * boundary.
     * @param theAntiAlias Default is false
     * @param theCoords ArrayList of four (type 2) or 6 (type 3) Double
     * @param theDomain ArrayList of Doubles specifying the domain
     * @param theFunction the Stitching (PDFfunction type 3) function, even if it's stitching a single function
     * @param theExtend ArrayList of Booleans of whether to extend teh start and end colors past the start and end points
     * The default is [false, false]
     */
    public PDFShading(int theNumber, String theShadingName,
                      int theShadingType, ColorSpace theColorSpace,
                      ArrayList theBackground, ArrayList theBBox,
                      boolean theAntiAlias, ArrayList theCoords,
                      ArrayList theDomain, PDFFunction theFunction,
                      ArrayList theExtend) {
        super(theNumber);
        this.shadingName = theShadingName;
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
     * Constructor for Type 4,6, or 7
     *
     * @param theNumber The object number of this PDF object.
     * @param theShadingType 4, 6, or 7 depending on whether it's
     * Free-form gouraud-shaded triangle meshes, coons patch meshes,
     * or tensor product patch meshes, respectively.
     * @param theShadingName The name of the shading pattern. Can be anything
     * without spaces. "Shading1" or "Sh1" are good examples.
     * @param theColorSpace "DeviceRGB" or similar.
     * @param theBackground theBackground An array of color components appropriate to the
     * colorspace key specifying a single color value.
     * This key is used by the f operator buy ignored by the sh operator.
     * @param theBBox ArrayList of double's representing a rectangle
     * in the coordinate space that is current at the
     * time of shading is imaged. Temporary clipping
     * boundary.
     * @param theAntiAlias Default is false
     * @param theBitsPerCoordinate 1,2,4,8,12,16,24 or 32.
     * @param theBitsPerComponent 1,2,4,8,12, and 16
     * @param theBitsPerFlag 2,4,8.
     * @param theDecode ArrayList of Doubles see PDF 1.3 spec pages 303 to 312.
     * @param theFunction the PDFFunction
     */
    public PDFShading(int theNumber, String theShadingName,
                      int theShadingType, ColorSpace theColorSpace,
                      ArrayList theBackground, ArrayList theBBox,
                      boolean theAntiAlias, int theBitsPerCoordinate,
                      int theBitsPerComponent, int theBitsPerFlag,
                      ArrayList theDecode, PDFFunction theFunction) {
        super(theNumber);

        this.shadingType = theShadingType;    // 4,6 or 7
        this.colorSpace = theColorSpace;
        this.background = theBackground;
        this.bBox = theBBox;
        this.antiAlias = theAntiAlias;

        this.bitsPerCoordinate = theBitsPerCoordinate;
        this.bitsPerComponent = theBitsPerComponent;
        this.bitsPerFlag = theBitsPerFlag;
        this.decode = theDecode;
        this.function = theFunction;
    }

    /**
     * Constructor for type 5
     *
     * @param theShadingType 5 for lattice-Form Gouraud shaded-triangle mesh
     * @param theShadingName The name of the shading pattern. Can be anything
     * without spaces. "Shading1" or "Sh1" are good examples.
     * @param theColorSpace "DeviceRGB" or similar.
     * @param theBackground theBackground An array of color components appropriate to the
     * colorspace key specifying a single color value.
     * This key is used by the f operator buy ignored by the sh operator.
     * @param theBBox ArrayList of double's representing a rectangle
     * in the coordinate space that is current at the
     * time of shading is imaged. Temporary clipping
     * boundary.
     * @param theAntiAlias Default is false
     * @param theBitsPerCoordinate 1,2,4,8,12,16, 24, or 32
     * @param theBitsPerComponent 1,2,4,8,12,24,32
     * @param theDecode ArrayList of Doubles. See page 305 in PDF 1.3 spec.
     * @param theVerticesPerRow number of vertices in each "row" of the lattice.
     * @param theFunction The PDFFunction that's mapped on to this shape
     * @param theNumber the object number of this PDF object.
     */
    public PDFShading(int theNumber, String theShadingName,
                      int theShadingType, ColorSpace theColorSpace,
                      ArrayList theBackground, ArrayList theBBox,
                      boolean theAntiAlias, int theBitsPerCoordinate,
                      int theBitsPerComponent, ArrayList theDecode,
                      int theVerticesPerRow, PDFFunction theFunction) {
        super(theNumber);
        this.shadingName = theShadingName;
        this.shadingType = theShadingType;    // 5
        this.colorSpace = theColorSpace;
        this.background = theBackground;
        this.bBox = theBBox;
        this.antiAlias = theAntiAlias;

        this.bitsPerCoordinate = theBitsPerCoordinate;
        this.bitsPerComponent = theBitsPerComponent;
        this.decode = theDecode;
        this.verticesPerRow = theVerticesPerRow;
        this.function = theFunction;

    }

    public String getName() {
        return (this.shadingName);
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
    public byte[] toPDF() {
        int vectorSize;
        int tempInt;
        StringBuffer p = new StringBuffer();
        p.append(this.number + " " + this.generation
                 + " obj\n<< \n/ShadingType " + this.shadingType + " \n");
        if (this.colorSpace != null) {
            p.append("/ColorSpace /"
                     + this.colorSpace.getColorSpacePDFString() + " \n");
        }

        if (this.background != null) {
            p.append("/Background [ ");
            vectorSize = this.background.size();
            for (tempInt = 0; tempInt < vectorSize; tempInt++) {
                p.append(PDFNumber.doubleOut((Double)this.background.get(tempInt))
                         + " ");
            }
            p.append("] \n");
        }

        if (this.bBox
                != null) {    // I've never seen an example, so I guess this is right.
            p.append("/BBox [ ");
            vectorSize = this.bBox.size();
            for (tempInt = 0; tempInt < vectorSize; tempInt++) {
                p.append(PDFNumber.doubleOut((Double)this.bBox.get(tempInt))
                         + " ");
            }
            p.append("] \n");
        }

        if (this.antiAlias) {
            p.append("/AntiAlias " + this.antiAlias + " \n");
        }

        // Here's where we differentiate based on what type it is.
        if (this.shadingType == 1) {    // function based shading
            if (this.domain != null) {
                p.append("/Domain [ ");
                vectorSize = this.domain.size();
                for (tempInt = 0; tempInt < vectorSize; tempInt++) {
                    p.append(PDFNumber.doubleOut((Double)this.domain.get(tempInt))
                             + " ");
                }
                p.append("] \n");
            } else {
                p.append("/Domain [ 0 1 ] \n");
            }

            if (this.matrix != null) {
                p.append("/Matrix [ ");
                vectorSize = this.matrix.size();
                for (tempInt = 0; tempInt < vectorSize; tempInt++) {
                    p.append(PDFNumber.doubleOut((Double)this.matrix.get(tempInt))
                             + " ");
                }
                p.append("] \n");
            }

            if (this.function != null) {
                p.append("/Function ");
                p.append(this.function.referencePDF() + " \n");
            }
        } else if ((this.shadingType == 2)
                   || (this.shadingType
                       == 3)) {         // 2 is axial shading (linear gradient)
            // 3 is radial shading (circular gradient)
            if (this.coords != null) {
                p.append("/Coords [ ");
                vectorSize = this.coords.size();
                for (tempInt = 0; tempInt < vectorSize; tempInt++) {
                    p.append(PDFNumber.doubleOut((Double)this.coords.get(tempInt))
                             + " ");
                }
                p.append("] \n");
            }

            // DOMAIN
            if (this.domain != null) {
                p.append("/Domain [ ");
                vectorSize = this.domain.size();
                for (tempInt = 0; tempInt < vectorSize; tempInt++) {
                    p.append(PDFNumber.doubleOut((Double)this.domain.get(tempInt))
                             + " ");
                }
                p.append("] \n");
            } else {
                p.append("/Domain [ 0 1 ] \n");
            }

            if (this.extend != null) {
                p.append("/Extend [ ");
                vectorSize = this.extend.size();
                for (tempInt = 0; tempInt < vectorSize; tempInt++) {
                    p.append(((Boolean)this.extend.get(tempInt)) + " ");
                }

                p.append("] \n");
            } else {
                p.append("/Extend [ true true ] \n");
            }


            if (this.function != null) {
                p.append("/Function ");
                p.append(this.function.referencePDF() + " \n");
            }


        } else if ((this.shadingType == 4) || (this.shadingType == 6)
                   || (this.shadingType
                       == 7)) {    // 4:Free-form Gouraud-shaded triangle meshes
            // 6:coons patch meshes
            // 7://tensor product patch meshes (which no one ever uses)
            if (this.bitsPerCoordinate > 0) {
                p.append("/BitsPerCoordinate " + this.bitsPerCoordinate
                         + " \n");
            } else {
                p.append("/BitsPerCoordinate 1 \n");
            }

            if (this.bitsPerComponent > 0) {
                p.append("/BitsPerComponent " + this.bitsPerComponent
                         + " \n");
            } else {
                p.append("/BitsPerComponent 1 \n");
            }

            if (this.bitsPerFlag > 0) {
                p.append("/BitsPerFlag " + this.bitsPerFlag + " \n");
            } else {
                p.append("/BitsPerFlag 2 \n");
            }

            if (this.decode != null) {
                p.append("/Decode [ ");
                vectorSize = this.decode.size();
                for (tempInt = 0; tempInt < vectorSize; tempInt++) {
                    p.append(((Boolean)this.decode.get(tempInt)) + " ");
                }

                p.append("] \n");
            }

            if (this.function != null) {
                p.append("/Function ");
                p.append(this.function.referencePDF() + " \n");
            }

        } else if (this.shadingType
                   == 5) {    // Lattice Free form gouraud-shaded triangle mesh

            if (this.bitsPerCoordinate > 0) {
                p.append("/BitsPerCoordinate " + this.bitsPerCoordinate
                         + " \n");
            } else {
                p.append("/BitsPerCoordinate 1 \n");
            }

            if (this.bitsPerComponent > 0) {
                p.append("/BitsPerComponent " + this.bitsPerComponent
                         + " \n");
            } else {
                p.append("/BitsPerComponent 1 \n");
            }

            if (this.decode != null) {
                p.append("/Decode [ ");
                vectorSize = this.decode.size();
                for (tempInt = 0; tempInt < vectorSize; tempInt++) {
                    p.append(((Boolean)this.decode.get(tempInt)) + " ");
                }

                p.append("] \n");
            }

            if (this.function != null) {
                p.append("/Function ");
                p.append(this.function.referencePDF() + " \n");
            }

            if (this.verticesPerRow > 0) {
                p.append("/VerticesPerRow " + this.verticesPerRow + " \n");
            } else {
                p.append("/VerticesPerRow 2 \n");
            }

        }

        p.append(">> \nendobj\n");

        return (p.toString().getBytes());
    }

}
