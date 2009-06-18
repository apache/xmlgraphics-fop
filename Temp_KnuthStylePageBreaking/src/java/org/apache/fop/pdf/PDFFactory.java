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
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

// Apache libs
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// FOP
import org.apache.fop.fonts.CIDFont;
import org.apache.fop.fonts.CustomFont;
import org.apache.fop.fonts.Typeface;
import org.apache.fop.fonts.FontDescriptor;
import org.apache.fop.fonts.FontMetrics;
import org.apache.fop.fonts.FontType;
import org.apache.fop.fonts.LazyFont;
import org.apache.fop.fonts.MultiByteFont;
import org.apache.fop.fonts.truetype.FontFileReader;
import org.apache.fop.fonts.truetype.TTFSubSetFile;
import org.apache.fop.fonts.type1.PFBData;
import org.apache.fop.fonts.type1.PFBParser;

/**
 * This class provides method to create and register PDF objects.
 */
public class PDFFactory {

    private PDFDocument document;

    private Log log = LogFactory.getLog("org.apache.fop.pdf");

    /**
     * Creates a new PDFFactory.
     * @param document the parent PDFDocument needed to register the generated
     * objects
     */
    public PDFFactory(PDFDocument document) {
        this.document = document;
    }

    /**
     * Returns the parent PDFDocument associated with this factory.
     * @return PDFDocument the parent PDFDocument
     */
    public final PDFDocument getDocument() {
        return this.document;
    }

    /* ========================= structure objects ========================= */

    /**
     * Make a /Catalog (Root) object. This object is written in
     * the trailer.
     *
     * @param pages the pages pdf object that the root points to
     * @return the new pdf root object for this document
     */
    public PDFRoot makeRoot(PDFPages pages) {
        //Make a /Pages object. This object is written in the trailer.
        PDFRoot pdfRoot = new PDFRoot(++this.document.objectcount, pages);
        getDocument().addTrailerObject(pdfRoot);
        return pdfRoot;
    }

    /**
     * Make a /Pages object. This object is written in the trailer.
     *
     * @return a new PDF Pages object for adding pages to
     */
    public PDFPages makePages() {
        PDFPages pdfPages = new PDFPages(++(this.document.objectcount));
        getDocument().addTrailerObject(pdfPages);
        return pdfPages;
    }

    /**
     * Make a /Resources object. This object is written in the trailer.
     *
     * @return a new PDF resources object
     */
    public PDFResources makeResources() {
        PDFResources pdfResources = new PDFResources(++this.document.objectcount);
        getDocument().addTrailerObject(pdfResources);
        return pdfResources;
    }

    /**
     * make an /Info object
     *
     * @param prod string indicating application producing the PDF
     * @return the created /Info object
     */
    protected PDFInfo makeInfo(String prod) {

        /*
         * create a PDFInfo with the next object number and add to
         * list of objects
         */
        PDFInfo pdfInfo = new PDFInfo();
        // set the default producer
        pdfInfo.setProducer(prod);
        getDocument().registerObject(pdfInfo);
        return pdfInfo;
    }

    /**
     * Make a /Page object. The page is assigned an object number immediately
     * so references can already be made. The page must be added to the
     * PDFDocument later using addObject().
     *
     * @param resources resources object to use
     * @param pagewidth width of the page in points
     * @param pageheight height of the page in points
     *
     * @return the created /Page object
     */
    public PDFPage makePage(PDFResources resources,
                            int pagewidth, int pageheight) {

        /*
         * create a PDFPage with the next object number, the given
         * resources, contents and dimensions
         */
        PDFPage page = new PDFPage(resources,
                                   pagewidth, pageheight);

        getDocument().assignObjectNumber(page);
        getDocument().getPages().addPage(page);
        return page;
    }

    /* ========================= functions ================================= */

    /**
     * Make a Type 0 sampled function
     *
     * @param theDomain List objects of Double objects.
     * This is the domain of the function.
     * See page 264 of the PDF 1.3 Spec.
     * @param theRange List objects of Double objects.
     * This is the Range of the function.
     * See page 264 of the PDF 1.3 Spec.
     * @param theSize A List object of Integer objects.
     * This is the number of samples in each input dimension.
     * I can't imagine there being more or less than two input dimensions,
     * so maybe this should be an array of length 2.
     *
     * See page 265 of the PDF 1.3 Spec.
     * @param theBitsPerSample An int specifying the number of bits user
     *                    to represent each sample value.
     * Limited to 1,2,4,8,12,16,24 or 32.
     * See page 265 of the 1.3 PDF Spec.
     * @param theOrder The order of interpolation between samples.
     *                 Default is 1 (one). Limited
     * to 1 (one) or 3, which means linear or cubic-spline interpolation.
     *
     * This attribute is optional.
     *
     * See page 265 in the PDF 1.3 spec.
     * @param theEncode List objects of Double objects.
     * This is the linear mapping of input values intop the domain
     * of the function's sample table. Default is hard to represent in
     * ascii, but basically [0 (Size0 1) 0 (Size1 1)...].
     * This attribute is optional.
     *
     * See page 265 in the PDF 1.3 spec.
     * @param theDecode List objects of Double objects.
     * This is a linear mapping of sample values into the range.
     * The default is just the range.
     *
     * This attribute is optional.
     * Read about it on page 265 of the PDF 1.3 spec.
     * @param theFunctionDataStream The sample values that specify
     *                        the function are provided in a stream.
     *
     * This is optional, but is almost always used.
     *
     * Page 265 of the PDF 1.3 spec has more.
     * @param theFilter This is a vector of String objects which
     *                  are the various filters that have are to be
     *                  applied to the stream to make sense of it.
     *                  Order matters, so watch out.
     *
     * This is not documented in the Function section of the PDF 1.3 spec,
     * it was deduced from samples that this is sometimes used, even if we may never
     * use it in FOP. It is added for completeness sake.
     * @param theFunctionType This is the type of function (0,2,3, or 4).
     * It should be 0 as this is the constructor for sampled functions.
     * @return the PDF function that was created
     */
    public PDFFunction makeFunction(int theFunctionType, List theDomain,
                                    List theRange, List theSize,
                                    int theBitsPerSample, int theOrder,
                                    List theEncode, List theDecode,
                                    StringBuffer theFunctionDataStream,
                                    List theFilter) {
        // Type 0 function
        PDFFunction function = new PDFFunction(theFunctionType, theDomain,
                                               theRange, theSize,
                                               theBitsPerSample, theOrder,
                                               theEncode, theDecode,
                                               theFunctionDataStream,
                                               theFilter);

        PDFFunction oldfunc = getDocument().findFunction(function);
        if (oldfunc == null) {
            getDocument().registerObject(function);
        } else {
            function = oldfunc;
        }
        return (function);
    }

    /**
     * make a type Exponential interpolation function
     * (for shading usually)
     *
     * @param theDomain List objects of Double objects.
     * This is the domain of the function.
     * See page 264 of the PDF 1.3 Spec.
     * @param theRange List of Doubles that is the Range of the function.
     * See page 264 of the PDF 1.3 Spec.
     * @param theCZero This is a vector of Double objects which defines the function result
     * when x=0.
     *
     * This attribute is optional.
     * It's described on page 268 of the PDF 1.3 spec.
     * @param theCOne This is a vector of Double objects which defines the function result
     * when x=1.
     *
     * This attribute is optional.
     * It's described on page 268 of the PDF 1.3 spec.
     * @param theInterpolationExponentN This is the inerpolation exponent.
     *
     * This attribute is required.
     * PDF Spec page 268
     * @param theFunctionType The type of the function, which should be 2.
     * @return the PDF function that was created
     */
    public PDFFunction makeFunction(int theFunctionType, List theDomain,
                                    List theRange, List theCZero,
                                    List theCOne,
                                    double theInterpolationExponentN) {    // type 2
        PDFFunction function = new PDFFunction(theFunctionType, theDomain,
                                               theRange, theCZero, theCOne,
                                               theInterpolationExponentN);
        PDFFunction oldfunc = getDocument().findFunction(function);
        if (oldfunc == null) {
            getDocument().registerObject(function);
        } else {
            function = oldfunc;
        }
        return (function);
    }

    /**
     * Make a Type 3 Stitching function
     *
     * @param theDomain List objects of Double objects.
     * This is the domain of the function.
     * See page 264 of the PDF 1.3 Spec.
     * @param theRange List objects of Double objects.
     * This is the Range of the function.
     * See page 264 of the PDF 1.3 Spec.
     * @param theFunctions An List of the PDFFunction objects
     *                     that the stitching function stitches.
     *
     * This attributed is required.
     * It is described on page 269 of the PDF spec.
     * @param theBounds This is a vector of Doubles representing
     *                  the numbers that, in conjunction with Domain
     *                  define the intervals to which each function from
     *                  the 'functions' object applies. It must be in
     *                  order of increasing magnitude, and each must be
     *                  within Domain.
     *
     * It basically sets how much of the gradient each function handles.
     *
     * This attributed is required.
     * It's described on page 269 of the PDF 1.3 spec.
     * @param theEncode List objects of Double objects.
     * This is the linear mapping of input values intop the domain
     * of the function's sample table. Default is hard to represent in
     * ascii, but basically [0 (Size0 1) 0 (Size1 1)...].
     * This attribute is required.
     *
     * See page 270 in the PDF 1.3 spec.
     * @param theFunctionType This is the function type. It should be 3,
     * for a stitching function.
     * @return the PDF function that was created
     */
    public PDFFunction makeFunction(int theFunctionType, List theDomain,
                                    List theRange, List theFunctions,
                                    List theBounds,
                                    List theEncode) {
        // Type 3

        PDFFunction function = new PDFFunction(theFunctionType, theDomain,
                                               theRange, theFunctions,
                                               theBounds, theEncode);

        PDFFunction oldfunc = getDocument().findFunction(function);
        if (oldfunc == null) {
            getDocument().registerObject(function);
        } else {
            function = oldfunc;
        }
        return (function);
    }

    /**
     * make a postscript calculator function
     *
     * @param theNumber the PDF object number
     * @param theFunctionType the type of function to make
     * @param theDomain the domain values
     * @param theRange the range values of the function
     * @param theFunctionDataStream a string containing the pdf drawing
     * @return the PDF function that was created
     */
    public PDFFunction makeFunction(int theNumber, int theFunctionType,
                                    List theDomain, List theRange,
                                    StringBuffer theFunctionDataStream) {
        // Type 4
        PDFFunction function = new PDFFunction(theFunctionType, theDomain,
                                               theRange,
                                               theFunctionDataStream);

        PDFFunction oldfunc = getDocument().findFunction(function);
        if (oldfunc == null) {
            getDocument().registerObject(function);
        } else {
            function = oldfunc;
        }
        return (function);

    }

    /* ========================= shadings ================================== */

    /**
     * make a function based shading object
     *
     * @param res the PDF resource context to add the shading, may be null
     * @param theShadingType The type of shading object, which should be 1 for function
     * based shading.
     * @param theColorSpace The colorspace is 'DeviceRGB' or something similar.
     * @param theBackground An array of color components appropriate to the
     * colorspace key specifying a single color value.
     * This key is used by the f operator buy ignored by the sh operator.
     * @param theBBox List of double's representing a rectangle
     * in the coordinate space that is current at the
     * time of shading is imaged. Temporary clipping
     * boundary.
     * @param theAntiAlias Whether or not to anti-alias.
     * @param theDomain Optional vector of Doubles specifying the domain.
     * @param theMatrix List of Doubles specifying the matrix.
     * If it's a pattern, then the matrix maps it to pattern space.
     * If it's a shading, then it maps it to current user space.
     * It's optional, the default is the identity matrix
     * @param theFunction The PDF Function that maps an (x,y) location to a color
     * @return the PDF shading that was created
     */
    public PDFShading makeShading(PDFResourceContext res, int theShadingType,
                                  PDFColorSpace theColorSpace,
                                  List theBackground, List theBBox,
                                  boolean theAntiAlias, List theDomain,
                                  List theMatrix,
                                  PDFFunction theFunction) {
        // make Shading of Type 1
        PDFShading shading = new PDFShading(theShadingType,
                                            theColorSpace, theBackground,
                                            theBBox, theAntiAlias, theDomain,
                                            theMatrix, theFunction);

        PDFShading oldshad = getDocument().findShading(shading);
        if (oldshad == null) {
            getDocument().registerObject(shading);
        } else {
            shading = oldshad;
        }

        // add this shading to resources
        if (res != null) {
            res.getPDFResources().addShading(shading);
        } else {
            getDocument().getResources().addShading(shading);
        }

        return (shading);
    }

    /**
     * Make an axial or radial shading object.
     *
     * @param res the PDF resource context to add the shading, may be null
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
     * @param theExtend List of Booleans of whether to extend the
     *                  start and end colors past the start and end points
     * The default is [false, false]
     * @return the PDF shading that was created
     */
    public PDFShading makeShading(PDFResourceContext res, int theShadingType,
                                  PDFColorSpace theColorSpace,
                                  List theBackground, List theBBox,
                                  boolean theAntiAlias, List theCoords,
                                  List theDomain, PDFFunction theFunction,
                                  List theExtend) {
        // make Shading of Type 2 or 3
        PDFShading shading = new PDFShading(theShadingType,
                                            theColorSpace, theBackground,
                                            theBBox, theAntiAlias, theCoords,
                                            theDomain, theFunction,
                                            theExtend);

        PDFShading oldshad = getDocument().findShading(shading);
        if (oldshad == null) {
            getDocument().registerObject(shading);
        } else {
            shading = oldshad;
        }

        if (res != null) {
            res.getPDFResources().addShading(shading);
        } else {
            getDocument().getResources().addShading(shading);
        }

        return (shading);
    }

    /**
     * Make a free-form gouraud shaded triangle mesh, coons patch mesh, or tensor patch mesh
     * shading object
     *
     * @param res the PDF resource context to add the shading, may be null
     * @param theShadingType 4, 6, or 7 depending on whether it's
     * Free-form gouraud-shaded triangle meshes, coons patch meshes,
     * or tensor product patch meshes, respectively.
     * @param theColorSpace "DeviceRGB" or similar.
     * @param theBackground theBackground An array of color components appropriate to the
     * colorspace key specifying a single color value.
     * This key is used by the f operator buy ignored by the sh operator.
     * @param theBBox List of double's representing a rectangle
     * in the coordinate space that is current at the
     * time of shading is imaged. Temporary clipping
     * boundary.
     * @param theAntiAlias Default is false
     * @param theBitsPerCoordinate 1,2,4,8,12,16,24 or 32.
     * @param theBitsPerComponent 1,2,4,8,12, and 16
     * @param theBitsPerFlag 2,4,8.
     * @param theDecode List of Doubles see PDF 1.3 spec pages 303 to 312.
     * @param theFunction the PDFFunction
     * @return the PDF shading that was created
     */
    public PDFShading makeShading(PDFResourceContext res, int theShadingType,
                                  PDFColorSpace theColorSpace,
                                  List theBackground, List theBBox,
                                  boolean theAntiAlias,
                                  int theBitsPerCoordinate,
                                  int theBitsPerComponent,
                                  int theBitsPerFlag, List theDecode,
                                  PDFFunction theFunction) {
        // make Shading of type 4,6 or 7
        PDFShading shading = new PDFShading(theShadingType,
                                            theColorSpace, theBackground,
                                            theBBox, theAntiAlias,
                                            theBitsPerCoordinate,
                                            theBitsPerComponent,
                                            theBitsPerFlag, theDecode,
                                            theFunction);

        PDFShading oldshad = getDocument().findShading(shading);
        if (oldshad == null) {
            getDocument().registerObject(shading);
        } else {
            shading = oldshad;
        }

        if (res != null) {
            res.getPDFResources().addShading(shading);
        } else {
            getDocument().getResources().addShading(shading);
        }

        return (shading);
    }

    /**
     * make a Lattice-Form Gouraud mesh shading object
     *
     * @param res the PDF resource context to add the shading, may be null
     * @param theShadingType 5 for lattice-Form Gouraud shaded-triangle mesh
     * without spaces. "Shading1" or "Sh1" are good examples.
     * @param theColorSpace "DeviceRGB" or similar.
     * @param theBackground theBackground An array of color components appropriate to the
     * colorspace key specifying a single color value.
     * This key is used by the f operator buy ignored by the sh operator.
     * @param theBBox List of double's representing a rectangle
     * in the coordinate space that is current at the
     * time of shading is imaged. Temporary clipping
     * boundary.
     * @param theAntiAlias Default is false
     * @param theBitsPerCoordinate 1,2,4,8,12,16, 24, or 32
     * @param theBitsPerComponent 1,2,4,8,12,24,32
     * @param theDecode List of Doubles. See page 305 in PDF 1.3 spec.
     * @param theVerticesPerRow number of vertices in each "row" of the lattice.
     * @param theFunction The PDFFunction that's mapped on to this shape
     * @return the PDF shading that was created
     */
    public PDFShading makeShading(PDFResourceContext res, int theShadingType,
                                  PDFColorSpace theColorSpace,
                                  List theBackground, List theBBox,
                                  boolean theAntiAlias,
                                  int theBitsPerCoordinate,
                                  int theBitsPerComponent, List theDecode,
                                  int theVerticesPerRow,
                                  PDFFunction theFunction) {
        // make shading of Type 5
        PDFShading shading = new PDFShading(theShadingType,
                                            theColorSpace, theBackground,
                                            theBBox, theAntiAlias,
                                            theBitsPerCoordinate,
                                            theBitsPerComponent, theDecode,
                                            theVerticesPerRow, theFunction);

        PDFShading oldshad = getDocument().findShading(shading);
        if (oldshad == null) {
            getDocument().registerObject(shading);
        } else {
            shading = oldshad;
        }

        if (res != null) {
            res.getPDFResources().addShading(shading);
        } else {
            getDocument().getResources().addShading(shading);
        }

        return (shading);
    }

    /* ========================= patterns ================================== */

    /**
     * Make a tiling pattern
     *
     * @param res the PDF resource context to add the shading, may be null
     * @param thePatternType the type of pattern, which is 1 for tiling.
     * @param theResources the resources associated with this pattern
     * @param thePaintType 1 or 2, colored or uncolored.
     * @param theTilingType 1, 2, or 3, constant spacing, no distortion, or faster tiling
     * @param theBBox List of Doubles: The pattern cell bounding box
     * @param theXStep horizontal spacing
     * @param theYStep vertical spacing
     * @param theMatrix Optional List of Doubles transformation matrix
     * @param theXUID Optional vector of Integers that uniquely identify the pattern
     * @param thePatternDataStream The stream of pattern data to be tiled.
     * @return the PDF pattern that was created
     */
    public PDFPattern makePattern(PDFResourceContext res, int thePatternType,    // 1
                                  PDFResources theResources, int thePaintType, int theTilingType,
                                  List theBBox, double theXStep,
                                  double theYStep, List theMatrix,
                                  List theXUID, StringBuffer thePatternDataStream) {
        // PDFResources theResources
        PDFPattern pattern = new PDFPattern(theResources, 1,
                                            thePaintType, theTilingType,
                                            theBBox, theXStep, theYStep,
                                            theMatrix, theXUID,
                                            thePatternDataStream);

        PDFPattern oldpatt = getDocument().findPattern(pattern);
        if (oldpatt == null) {
            getDocument().registerObject(pattern);
        } else {
            pattern = oldpatt;
        }

        if (res != null) {
            res.getPDFResources().addPattern(pattern);
        } else {
            getDocument().getResources().addPattern(pattern);
        }

        return (pattern);
    }

    /**
     * Make a smooth shading pattern
     *
     * @param res the PDF resource context to add the shading, may be null
     * @param thePatternType the type of the pattern, which is 2, smooth shading
     * @param theShading the PDF Shading object that comprises this pattern
     * @param theXUID optional:the extended unique Identifier if used.
     * @param theExtGState optional: the extended graphics state, if used.
     * @param theMatrix Optional:List of Doubles that specify the matrix.
     * @return the PDF pattern that was created
     */
    public PDFPattern makePattern(PDFResourceContext res,
                                  int thePatternType, PDFShading theShading,
                                  List theXUID, StringBuffer theExtGState,
                                  List theMatrix) {
        PDFPattern pattern = new PDFPattern(2, theShading,
                                            theXUID, theExtGState, theMatrix);

        PDFPattern oldpatt = getDocument().findPattern(pattern);
        if (oldpatt == null) {
            getDocument().registerObject(pattern);
        } else {
            pattern = oldpatt;
        }

        if (res != null) {
            res.getPDFResources().addPattern(pattern);
        } else {
            getDocument().getResources().addPattern(pattern);
        }

        return (pattern);
    }

    /**
     * Make a gradient
     *
     * @param res the PDF resource context to add the shading, may be null
     * @param radial if true a radial gradient will be created
     * @param theColorspace the colorspace of the gradient
     * @param theColors the list of colors for the gradient
     * @param theBounds the list of bounds associated with the colors
     * @param theCoords the coordinates for the gradient
     * @return the PDF pattern that was created
     */
    public PDFPattern makeGradient(PDFResourceContext res, boolean radial,
                                   PDFColorSpace theColorspace,
                                   List theColors, List theBounds,
                                   List theCoords) {
        PDFShading myShad;
        PDFFunction myfunky;
        PDFFunction myfunc;
        List theCzero;
        List theCone;
        PDFPattern myPattern;
        //PDFColorSpace theColorSpace;
        double interpolation = (double)1.000;
        List theFunctions = new java.util.ArrayList();

        int currentPosition;
        int lastPosition = theColors.size() - 1;


        // if 5 elements, the penultimate element is 3.
        // do not go beyond that, because you always need
        // to have a next color when creating the function.

        for (currentPosition = 0; currentPosition < lastPosition;
                currentPosition++) {    // for every consecutive color pair
            PDFColor currentColor =
                (PDFColor)theColors.get(currentPosition);
            PDFColor nextColor = (PDFColor)theColors.get(currentPosition
                                 + 1);
            // colorspace must be consistant
            if (getDocument().getColorSpace()
                    != currentColor.getColorSpace()) {
                currentColor.setColorSpace(
                    getDocument().getColorSpace());
            }

            if (getDocument().getColorSpace()
                    != nextColor.getColorSpace()) {
                nextColor.setColorSpace(
                    getDocument().getColorSpace());
            }

            theCzero = currentColor.getVector();
            theCone = nextColor.getVector();

            myfunc = makeFunction(2, null, null, theCzero, theCone,
                                       interpolation);

            theFunctions.add(myfunc);

        }                               // end of for every consecutive color pair

        myfunky = makeFunction(3, null, null, theFunctions, theBounds,
                                    null);

        if (radial) {
            if (theCoords.size() == 6) {
                myShad = makeShading(res, 3, getDocument().getPDFColorSpace(),
                                     null, null,
                                     false, theCoords, null, myfunky,
                                     null);
            } else {    // if the center x, center y, and radius specifiy
                // the gradient, then assume the same center x, center y,
                // and radius of zero for the other necessary component
                List newCoords = new java.util.ArrayList();
                newCoords.add(theCoords.get(0));
                newCoords.add(theCoords.get(1));
                newCoords.add(theCoords.get(2));
                newCoords.add(theCoords.get(0));
                newCoords.add(theCoords.get(1));
                newCoords.add(new Double(0.0));

                myShad = makeShading(res, 3, getDocument().getPDFColorSpace(),
                                     null, null,
                                     false, newCoords, null, myfunky,
                                     null);

            }
        } else {
            myShad = makeShading(res, 2, getDocument().getPDFColorSpace(),
                                 null, null,
                                 false, theCoords, null, myfunky,
                                 null);

        }

        myPattern = makePattern(res, 2, myShad, null, null, null);

        return (myPattern);
    }

    /* ========================= links ===================================== */

    /**
     * Make an internal link.
     *
     * @param rect the hotspot position in absolute coordinates
     * @param page the target page reference value
     * @param dest the position destination
     * @return the new PDF link object
     */
    public PDFLink makeLink(Rectangle2D rect, String page, String dest) {
        PDFLink link = new PDFLink(rect);
        getDocument().registerObject(link);

        PDFGoTo gt = new PDFGoTo(page);
        gt.setDestination(dest);
        getDocument().addTrailerObject(gt);
        PDFInternalLink internalLink = new PDFInternalLink(gt.referencePDF());
        link.setAction(internalLink);

        return link;
    }

    /**
     * make a link object
     *
     * @param rect   the clickable rectangle
     * @param destination  the destination file
     * @param linkType the link type
     * @param yoffset the yoffset on the page for an internal link
     * @return the PDFLink object created
     */
    public PDFLink makeLink(Rectangle2D rect, String destination,
                            int linkType, float yoffset) {

        //PDFLink linkObject;
        int index;

        PDFLink link = new PDFLink(rect);

        if (linkType == PDFLink.EXTERNAL) {
            // check destination
            if (destination.startsWith("http://")) {
                PDFUri uri = new PDFUri(destination);
                link.setAction(uri);
            } else if (destination.endsWith(".pdf")) {    // FileSpec
                PDFGoToRemote remote = getGoToPDFAction(destination, null, -1);
                link.setAction(remote);
            } else if ((index = destination.indexOf(".pdf#page=")) > 0) {
                //String file = destination.substring(0, index + 4);
                int page = Integer.parseInt(destination.substring(index + 10));
                PDFGoToRemote remote = getGoToPDFAction(destination, null, page);
                link.setAction(remote);
            } else if ((index = destination.indexOf(".pdf#dest=")) > 0) {
                //String file = destination.substring(0, index + 4);
                String dest = destination.substring(index + 10);
                PDFGoToRemote remote = getGoToPDFAction(destination, dest, -1);
                link.setAction(remote);
            } else {                               // URI
                PDFUri uri = new PDFUri(destination);
                link.setAction(uri);
            }
        } else {
            // linkType is internal
            String goToReference = getGoToReference(destination, yoffset);
            PDFInternalLink internalLink = new PDFInternalLink(goToReference);
            link.setAction(internalLink);
        }

        PDFLink oldlink = getDocument().findLink(link);
        if (oldlink == null) {
            getDocument().registerObject(link);
        } else {
            link = oldlink;
        }

        return link;
    }

    private String getGoToReference(String destination, float yoffset) {
        String goToReference = null;
        PDFGoTo gt = new PDFGoTo(destination);
        gt.setYPosition(yoffset);
        PDFGoTo oldgt = getDocument().findGoTo(gt);
        if (oldgt == null) {
            getDocument().assignObjectNumber(gt);
            getDocument().addTrailerObject(gt);
        } else {
            gt = oldgt;
        }

        goToReference = gt.referencePDF();
        return goToReference;
    }

    /**
     * Create and return a goto pdf document action.
     * This creates a pdf files spec and pdf goto remote action.
     * It also checks available pdf objects so it will not create an
     * object if it already exists.
     *
     * @param file the pdf file name
     * @param dest the remote name destination, may be null
     * @param page the remote page number, -1 means not specified
     * @return the pdf goto remote object
     */
    private PDFGoToRemote getGoToPDFAction(String file, String dest, int page) {
        PDFFileSpec fileSpec = new PDFFileSpec(file);
        PDFFileSpec oldspec = getDocument().findFileSpec(fileSpec);
        if (oldspec == null) {
            getDocument().registerObject(fileSpec);
        } else {
            fileSpec = oldspec;
        }
        PDFGoToRemote remote;

        if (dest == null && page == -1) {
            remote = new PDFGoToRemote(fileSpec);
        } else if (dest != null) {
            remote = new PDFGoToRemote(fileSpec, dest);
        } else {
            remote = new PDFGoToRemote(fileSpec, page);
        }
        PDFGoToRemote oldremote = getDocument().findGoToRemote(remote);
        if (oldremote == null) {
            getDocument().registerObject(remote);
        } else {
            remote = oldremote;
        }
        return remote;
    }

    /**
     * Make an outline object and add it to the given outline
     *
     * @param parent parent PDFOutline object which may be null
     * @param label the title for the new outline object
     * @param destination the reference string for the action to go to
     * @param yoffset the yoffset on the destination page
     * @param showSubItems whether to initially display child outline items
     * @return the new PDF outline object
     */
    public PDFOutline makeOutline(PDFOutline parent, String label,
                                  String destination, float yoffset, 
                                  boolean showSubItems) {

        String goToRef = getGoToReference(destination, yoffset);
        PDFOutline obj = new PDFOutline(label, goToRef, showSubItems);

        if (parent != null) {
            parent.addOutline(obj);
        }
        getDocument().registerObject(obj);
        return obj;
    }



    /* ========================= fonts ===================================== */

    /**
     * make a /Encoding object
     *
     * @param encodingName character encoding scheme name
     * @return the created /Encoding object
     */
    public PDFEncoding makeEncoding(String encodingName) {
        PDFEncoding encoding = new PDFEncoding(encodingName);

        getDocument().registerObject(encoding);
        return encoding;
    }

    /**
     * make a Type1 /Font object
     *
     * @param fontname internal name to use for this font (eg "F1")
     * @param basefont name of the base font (eg "Helvetica")
     * @param encoding character encoding scheme used by the font
     * @param metrics additional information about the font
     * @param descriptor additional information about the font
     * @return the created /Font object
     */
    public PDFFont makeFont(String fontname, String basefont,
                            String encoding, FontMetrics metrics,
                            FontDescriptor descriptor) {
        PDFFont preRegisteredfont = getDocument().findFont(fontname);
        if (preRegisteredfont != null) {
            return preRegisteredfont;
        }

        if (descriptor == null) {
            PDFFont font = new PDFFont(fontname, FontType.TYPE1, basefont, encoding);
            getDocument().registerObject(font);
            return font;
        } else {
            FontType fonttype = metrics.getFontType();

            PDFFontDescriptor pdfdesc = makeFontDescriptor(descriptor);

            PDFFontNonBase14 font = null;
            if (fonttype == FontType.TYPE0) {
                /*
                 * Temporary commented out - customized CMaps
                 * isn't needed until /ToUnicode support is added
                 * PDFCMap cmap = new PDFCMap(++this.objectcount,
                 * "fop-ucs-H",
                 * new PDFCIDSystemInfo("Adobe",
                 * "Identity",
                 * 0));
                 * cmap.addContents();
                 * this.objects.add(cmap);
                 */
                font =
                    (PDFFontNonBase14)PDFFont.createFont(fontname, fonttype,
                                                         basefont,
                                                         "Identity-H");
            } else {

                font =
                    (PDFFontNonBase14)PDFFont.createFont(fontname, fonttype,
                                                         basefont, encoding);
            }
            getDocument().registerObject(font);

            font.setDescriptor(pdfdesc);

            if (fonttype == FontType.TYPE0) {
                CIDFont cidMetrics;
                if (metrics instanceof LazyFont) {
                    cidMetrics = (CIDFont)((LazyFont) metrics).getRealFont();
                } else {
                    cidMetrics = (CIDFont)metrics;
                }
                PDFCIDSystemInfo sysInfo =
                    new PDFCIDSystemInfo(cidMetrics.getRegistry(),
                                         cidMetrics.getOrdering(),
                                         cidMetrics.getSupplement());
                PDFCIDFont cidFont =
                    new PDFCIDFont(basefont,
                                   cidMetrics.getCIDType(),
                                   cidMetrics.getDefaultWidth(),
                                   getSubsetWidths(cidMetrics), sysInfo,
                                   (PDFCIDFontDescriptor)pdfdesc);
                getDocument().registerObject(cidFont);

                ((PDFFontType0)font).setDescendantFonts(cidFont);
            } else {
                int firstChar = 0;
                int lastChar = 255;
                if (metrics instanceof CustomFont) {
                    CustomFont cf = (CustomFont)metrics;
                    firstChar = cf.getFirstChar();
                    lastChar = cf.getLastChar();
                }
                font.setWidthMetrics(firstChar,
                                     lastChar,
                                     makeArray(metrics.getWidths()));
            }

            return font;
        }
    }

    public PDFWArray getSubsetWidths(CIDFont cidFont) {
        // Create widths for reencoded chars
        PDFWArray warray = new PDFWArray();
        int[] tmpWidth = new int[cidFont.usedGlyphsCount];

        for (int i = 0; i < cidFont.usedGlyphsCount; i++) {
            Integer nw = (Integer)cidFont.usedGlyphsIndex.get(new Integer(i));
            int nwx = (nw == null) ? 0 : nw.intValue();
            tmpWidth[i] = cidFont.width[nwx];
        }
        warray.addEntry(0, tmpWidth);
        return warray;
    }

    /**
     * make a /FontDescriptor object
     *
     * @param desc the font descriptor
     * @return the new PDF font descriptor
     */
    public PDFFontDescriptor makeFontDescriptor(FontDescriptor desc) {
        PDFFontDescriptor descriptor = null;

        if (desc.getFontType() == FontType.TYPE0) {
            // CID Font
            descriptor = new PDFCIDFontDescriptor(desc.getFontName(),
                                            desc.getFontBBox(),
                                            desc.getCapHeight(),
                                            desc.getFlags(),
                                            desc.getItalicAngle(),
                                            desc.getStemV(), null);
        } else {
            // Create normal FontDescriptor
            descriptor = new PDFFontDescriptor(desc.getFontName(),
                                         desc.getAscender(),
                                         desc.getDescender(),
                                         desc.getCapHeight(),
                                         desc.getFlags(),
                                         new PDFRectangle(desc.getFontBBox()),
                                         desc.getStemV(),
                                         desc.getItalicAngle());
        }
        getDocument().registerObject(descriptor);

        // Check if the font is embeddable
        if (desc.isEmbeddable()) {
            AbstractPDFStream stream = makeFontFile(desc);
            if (stream != null) {
                descriptor.setFontFile(desc.getFontType(), stream);
                getDocument().registerObject(stream);
            }
        }
        return descriptor;
    }

    /**
     * Embeds a font.
     * @param desc FontDescriptor of the font.
     * @return PDFStream The embedded font file
     */
    public AbstractPDFStream makeFontFile(FontDescriptor desc) {
        if (desc.getFontType() == FontType.OTHER) {
            throw new IllegalArgumentException("Trying to embed unsupported font type: "
                                                + desc.getFontType());
        }

        Typeface tempFont;
        if (desc instanceof LazyFont) {
            tempFont = ((LazyFont)desc).getRealFont();
        } else {
            tempFont = (Typeface)desc;
        }
        if (!(tempFont instanceof CustomFont)) {
            throw new IllegalArgumentException(
                      "FontDescriptor must be instance of CustomFont, but is a "
                       + desc.getClass().getName());
        }
        CustomFont font = (CustomFont)tempFont;

        InputStream in = null;
        try {
            // Get file first
            if (font.getEmbedFileName() != null) {
                try {
                    in = getDocument().resolveURI(font.getEmbedFileName());
                } catch (Exception e) {
                    log.error("Failed to embed fontfile: "
                                       + font.getEmbedFileName()
                                       + "(" + e.getMessage() + ")");
                }
            }

            // Get resource
            if (in == null && font.getEmbedResourceName() != null) {
                try {
                    in = new java.io.BufferedInputStream(
                            this.getClass().getResourceAsStream(
                                font.getEmbedResourceName()));
                } catch (Exception e) {
                    log.error(
                                         "Failed to embed fontresource: "
                                       + font.getEmbedResourceName()
                                       + "(" + e.getMessage() + ")");
                }
            }

            if (in == null) {
                return null;
            } else {
                try {
                    AbstractPDFStream embeddedFont;
                    if (desc.getFontType() == FontType.TYPE0) {
                        MultiByteFont mbfont = (MultiByteFont)font;
                        FontFileReader reader = new FontFileReader(in);

                        TTFSubSetFile subset = new TTFSubSetFile();
                        byte[] subsetFont = subset.readFont(reader,
                                             mbfont.getTTCName(), mbfont.getUsedGlyphs());
                        // Only TrueType CID fonts are supported now

                        embeddedFont = new PDFTTFStream(subsetFont.length);
                        ((PDFTTFStream)embeddedFont).setData(subsetFont, subsetFont.length);
                    } else if (desc.getFontType() == FontType.TYPE1) {
                        PFBParser parser = new PFBParser();
                        PFBData pfb = parser.parsePFB(in);
                        embeddedFont = new PDFT1Stream();
                        ((PDFT1Stream)embeddedFont).setData(pfb);
                    } else {
                        byte[] file = IOUtils.toByteArray(in);
                        embeddedFont = new PDFTTFStream(file.length);
                        ((PDFTTFStream)embeddedFont).setData(file, file.length);
                    }

                    /*
                    embeddedFont.getFilterList().addFilter("flate");
                    if (getDocument().isEncryptionActive()) {
                        getDocument().applyEncryption(embeddedFont);
                    } else {
                        embeddedFont.getFilterList().addFilter("ascii-85");
                    }*/

                    return embeddedFont;
                } finally {
                    in.close();
                }
            }
        } catch (IOException ioe) {
            log.error(
                    "Failed to embed font [" + desc + "] "
                    + desc.getFontName(), ioe);
            return (PDFStream) null;
        }
    }


    /* ========================= streams =================================== */

    /**
     * Make a stream object
     *
     * @param type the type of stream to be created
     * @param add if true then the stream will be added immediately
     * @return the stream object created
     */
    public PDFStream makeStream(String type, boolean add) {

        // create a PDFStream with the next object number
        // and add it to the list of objects
        PDFStream obj = new PDFStream();
        obj.setDocument(getDocument());
        obj.getFilterList().addDefaultFilters(
                getDocument().getFilterMap(),
                type);

        if (add) {
            getDocument().registerObject(obj);
        }
        //getDocument().applyEncryption(obj);
        return obj;
    }

    /**
     * Create a PDFICCStream
     * @see PDFXObject
     * @see org.apache.fop.image.JpegImage
     * @see org.apache.fop.pdf.PDFColorSpace
     * @return the new PDF ICC stream object
     */
    public PDFICCStream makePDFICCStream() {
        PDFICCStream iccStream = new PDFICCStream();
        iccStream.getFilterList().addDefaultFilters(
                getDocument().getFilterMap(),
                PDFFilterList.CONTENT_FILTER);

        getDocument().registerObject(iccStream);
        //getDocument().applyEncryption(iccStream);
        return iccStream;
    }

    /* ========================= misc. objects ============================= */

    /**
     * make an Array object (ex. Widths array for a font)
     *
     * @param values the int array values
     * @return the PDF Array with the int values
     */
    public PDFArray makeArray(int[] values) {
        PDFArray array = new PDFArray(values);

        getDocument().registerObject(array);
        return array;
    }

    /**
     * make an ExtGState for extra graphics options
     * This tries to find a GState that will setup the correct values
     * for the current context. If there is no suitable GState it will
     * create a new one.
     *
     * @param settings the settings required by the caller
     * @param current the current GState of the current PDF context
     * @return a PDF GState, either an existing GState or a new one
     */
    public PDFGState makeGState(Map settings, PDFGState current) {

        // try to locate a gstate that has all the settings
        // or will inherit from the current gstate
        // compare "DEFAULT + settings" with "current + each gstate"

        PDFGState wanted = new PDFGState();
        wanted.addValues(PDFGState.DEFAULT);
        wanted.addValues(settings);


        PDFGState existing = getDocument().findGState(wanted, current);
        if (existing != null) {
            return existing;
        }

        PDFGState gstate = new PDFGState();
        gstate.addValues(settings);
        getDocument().registerObject(gstate);
        return gstate;
    }

    /**
     * Make an annotation list object
     *
     * @return the annotation list object created
     */
    public PDFAnnotList makeAnnotList() {
        PDFAnnotList obj = new PDFAnnotList();
        getDocument().assignObjectNumber(obj);
        return obj;
    }



}
