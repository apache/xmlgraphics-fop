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

// Java
import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.java2d.color.ColorUtil;
import org.apache.xmlgraphics.java2d.color.NamedColorSpace;
import org.apache.xmlgraphics.xmp.Metadata;

import org.apache.fop.fonts.CIDFont;
import org.apache.fop.fonts.CIDSubset;
import org.apache.fop.fonts.CodePointMapping;
import org.apache.fop.fonts.CustomFont;
import org.apache.fop.fonts.FontDescriptor;
import org.apache.fop.fonts.FontMetrics;
import org.apache.fop.fonts.FontType;
import org.apache.fop.fonts.LazyFont;
import org.apache.fop.fonts.MultiByteFont;
import org.apache.fop.fonts.SimpleSingleByteEncoding;
import org.apache.fop.fonts.SingleByteEncoding;
import org.apache.fop.fonts.SingleByteFont;
import org.apache.fop.fonts.Typeface;
import org.apache.fop.fonts.truetype.FontFileReader;
import org.apache.fop.fonts.truetype.TTFSubSetFile;
import org.apache.fop.fonts.type1.PFBData;
import org.apache.fop.fonts.type1.PFBParser;
import org.apache.xmlgraphics.xmp.Metadata;

/**
 * This class provides method to create and register PDF objects.
 */
public class PDFFactory {

    /** Resolution of the User Space coordinate system (72dpi). */
    public static final int DEFAULT_PDF_RESOLUTION = 72;

    private PDFDocument document;

    private Log log = LogFactory.getLog(PDFFactory.class);

    private int subsetFontCounter = -1;

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
        pdfRoot.setDocument(getDocument());
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
        pdfPages.setDocument(getDocument());
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
        pdfResources.setDocument(getDocument());
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
     * Make a Metadata object.
     * @param meta the DOM Document containing the XMP metadata.
     * @param readOnly true if the metadata packet should be marked read-only
     * @return the newly created Metadata object
     */
    public PDFMetadata makeMetadata(Metadata meta, boolean readOnly) {
        PDFMetadata pdfMetadata = new PDFMetadata(meta, readOnly);
        getDocument().registerObject(pdfMetadata);
        return pdfMetadata;
    }

    /**
     * Make a OutputIntent dictionary.
     * @return the newly created OutputIntent dictionary
     */
    public PDFOutputIntent makeOutputIntent() {
        PDFOutputIntent outputIntent = new PDFOutputIntent();
        getDocument().registerObject(outputIntent);
        return outputIntent;
    }

    /**
     * Make a /Page object. The page is assigned an object number immediately
     * so references can already be made. The page must be added to the
     * PDFDocument later using addObject().
     *
     * @param resources resources object to use
     * @param pageIndex index of the page (zero-based)
     * @param mediaBox the MediaBox area
     * @param cropBox the CropBox area
     * @param bleedBox the BleedBox area
     * @param trimBox the TrimBox area
     *
     * @return the created /Page object
     */
    public PDFPage makePage(PDFResources resources, int pageIndex,
                            Rectangle2D mediaBox, Rectangle2D cropBox,
                            Rectangle2D bleedBox, Rectangle2D trimBox) {
        /*
         * create a PDFPage with the next object number, the given
         * resources, contents and dimensions
         */
        PDFPage page = new PDFPage(resources, pageIndex, mediaBox, cropBox, bleedBox, trimBox);

        getDocument().assignObjectNumber(page);
        getDocument().getPages().addPage(page);
        return page;
    }

    /**
     * Make a /Page object. The page is assigned an object number immediately
     * so references can already be made. The page must be added to the
     * PDFDocument later using addObject().
     *
     * @param resources resources object to use
     * @param pageWidth width of the page in points
     * @param pageHeight height of the page in points
     * @param pageIndex index of the page (zero-based)
     *
     * @return the created /Page object
     */
    public PDFPage makePage(PDFResources resources,
                            int pageWidth, int pageHeight, int pageIndex) {
        Rectangle2D mediaBox = new Rectangle2D.Double(0, 0, pageWidth, pageHeight);
        return makePage(resources, pageIndex, mediaBox, mediaBox, mediaBox, mediaBox);
    }

    /**
     * Make a /Page object. The page is assigned an object number immediately
     * so references can already be made. The page must be added to the
     * PDFDocument later using addObject().
     *
     * @param resources resources object to use
     * @param pageWidth width of the page in points
     * @param pageHeight height of the page in points
     *
     * @return the created /Page object
     */
    public PDFPage makePage(PDFResources resources,
                            int pageWidth, int pageHeight) {
        return makePage(resources, pageWidth, pageHeight, -1);
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
    public PDFFunction makeFunction(                             // CSOK: ParameterNumber
            int theFunctionType, List theDomain,
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
    public PDFShading makeShading(                               // CSOK: ParameterNumber
            PDFResourceContext res, int theShadingType,
            PDFDeviceColorSpace theColorSpace,
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
    public PDFShading makeShading(                               // CSOK: ParameterNumber
            PDFResourceContext res, int theShadingType,
            PDFDeviceColorSpace theColorSpace,
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
    public PDFShading makeShading(                               // CSOK: ParameterNumber
            PDFResourceContext res, int theShadingType,
            PDFDeviceColorSpace theColorSpace,
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
    public PDFShading makeShading(                               // CSOK: ParameterNumber
            PDFResourceContext res, int theShadingType,
            PDFDeviceColorSpace theColorSpace,
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
    public PDFPattern makePattern(                               // CSOK: ParameterNumber
            PDFResourceContext res, int thePatternType,    // 1
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
     * @param theMatrix the coordinate-transformation matrix
     * @return the PDF pattern that was created
     */
    public PDFPattern makeGradient(PDFResourceContext res, boolean radial,
                                   PDFDeviceColorSpace theColorspace,
                                   List theColors, List theBounds,
                                   List theCoords, List theMatrix) {
        PDFShading myShad;
        PDFFunction myfunky;
        PDFFunction myfunc;
        List theCzero;
        List theCone;
        PDFPattern myPattern;
        //PDFColorSpace theColorSpace;
        double interpolation = 1.000;
        List theFunctions = new ArrayList();

        int currentPosition;
        int lastPosition = theColors.size() - 1;


        // if 5 elements, the penultimate element is 3.
        // do not go beyond that, because you always need
        // to have a next color when creating the function.

        for (currentPosition = 0; currentPosition < lastPosition;
                currentPosition++) {    // for every consecutive color pair
            Color currentColor = (Color)theColors.get(currentPosition);
            Color nextColor = (Color)theColors.get(currentPosition + 1);

            // colorspace must be consistent, so we simply convert to sRGB where necessary
            if (!currentColor.getColorSpace().isCS_sRGB()) {
                //Convert to sRGB
                currentColor = ColorUtil.toSRGBColor(currentColor);
                theColors.set(currentPosition, currentColor);
            }
            if (!nextColor.getColorSpace().isCS_sRGB()) {
                //Convert to sRGB
                nextColor = ColorUtil.toSRGBColor(nextColor);
                theColors.set(currentPosition + 1, nextColor);
            }

            theCzero = toColorVector(currentColor);
            theCone = toColorVector(nextColor);

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
                List newCoords = new ArrayList();
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

        myPattern = makePattern(res, 2, myShad, null, null, theMatrix);

        return (myPattern);
    }

    private List toColorVector(Color nextColor) {
        List vector = new java.util.ArrayList();
        float[] comps = nextColor.getColorComponents(null);
        for (int i = 0, c = comps.length; i < c; i++) {
            vector.add(new Double(comps[i]));
        }
        return vector;
    }

    /* ============= named destinations and the name dictionary ============ */

    /**
     * Registers and returns newdest if it is unique. Otherwise, returns
     * the equal destination already present in the document.
     *
     * @param newdest a new, as yet unregistered destination
     * @return newdest if unique, else the already registered instance
     */
    protected PDFDestination getUniqueDestination(PDFDestination newdest) {
        PDFDestination existing = getDocument().findDestination(newdest);
        if (existing != null) {
            return existing;
        } else {
            getDocument().addDestination(newdest);
            return newdest;
        }
    }

    /**
     * Make a named destination.
     *
     * @param idRef ID Reference for this destination (the name of the destination)
     * @param goToRef Object reference to the GoTo Action
     * @return the newly created destrination
     */
    public PDFDestination makeDestination(String idRef, Object goToRef) {
        PDFDestination destination = new PDFDestination(idRef, goToRef);
        return getUniqueDestination(destination);
    }

    /**
     * Make a names dictionary (the /Names object).
     * @return the new PDFNames object
     */
    public PDFNames makeNames() {
        PDFNames names = new PDFNames();
        getDocument().assignObjectNumber(names);
        getDocument().addTrailerObject(names);
        return names;
    }

    /**
     * Make a names dictionary (the /PageLabels object).
     * @return the new PDFPageLabels object
     */
    public PDFPageLabels makePageLabels() {
        PDFPageLabels pageLabels = new PDFPageLabels();
        getDocument().assignObjectNumber(pageLabels);
        getDocument().addTrailerObject(pageLabels);
        return pageLabels;
    }

    /**
     * Make a the head object of the name dictionary (the /Dests object).
     *
     * @param destinationList a list of PDFDestination instances
     * @return the new PDFDests object
     */
    public PDFDests makeDests(List destinationList) {
        PDFDests dests;

        //TODO: Check why the below conditional branch is needed. Condition is always true...
        final boolean deep = true;
        //true for a "deep" structure (one node per entry), true for a "flat" structure
        if (deep) {
            dests = new PDFDests();
            PDFArray kids = new PDFArray(dests);
            Iterator iter = destinationList.iterator();
            while (iter.hasNext()) {
                PDFDestination dest = (PDFDestination)iter.next();
                PDFNameTreeNode node = new PDFNameTreeNode();
                getDocument().registerObject(node);
                node.setLowerLimit(dest.getIDRef());
                node.setUpperLimit(dest.getIDRef());
                node.setNames(new PDFArray(node));
                PDFArray names = node.getNames();
                names.add(dest);
                kids.add(node);
            }
            dests.setLowerLimit(((PDFNameTreeNode)kids.get(0)).getLowerLimit());
            dests.setUpperLimit(((PDFNameTreeNode)kids.get(kids.length() - 1)).getUpperLimit());
            dests.setKids(kids);
        } else {
            dests = new PDFDests(destinationList);
        }
        getDocument().registerObject(dests);
        return dests;
    }

    /**
     * Make a name tree node.
     *
     * @return the new name tree node
     */
    public PDFNameTreeNode makeNameTreeNode() {
        PDFNameTreeNode node = new PDFNameTreeNode();
        getDocument().registerObject(node);
        return node;
    }

    /* ========================= links ===================================== */
    // Some of the "yoffset-only" functions in this part are obsolete and can
    // possibly be removed or deprecated. Some are still called by PDFGraphics2D
    // (although that could be changed, they don't need the yOffset param anyway).

    /**
     * Create a PDF link to an existing PDFAction object
     *
     * @param rect the hotspot position in absolute coordinates
     * @param pdfAction the PDFAction that this link refers to
     * @return the new PDFLink object, or null if either rect or pdfAction is null
     */
    public PDFLink makeLink(Rectangle2D rect, PDFAction pdfAction) {
        if (rect == null || pdfAction == null) {
            return null;
        } else {
            PDFLink link = new PDFLink(rect);
            link.setAction(pdfAction);
            getDocument().registerObject(link);
            return link;
            // does findLink make sense? I mean, how often will it happen that several
            // links have the same target *and* the same hot rect? And findLink has to
            // walk and compare the entire link list everytime you call it...
        }
    }

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
        getDocument().registerObject(gt);
        PDFInternalLink internalLink = new PDFInternalLink(gt.referencePDF());
        link.setAction(internalLink);

        return link;
    }

    /**
     * Make a {@link PDFLink} object
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
        PDFLink link = new PDFLink(rect);

        if (linkType == PDFLink.EXTERNAL) {
            link.setAction(getExternalAction(destination, false));
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

    private static final String EMBEDDED_FILE = "embedded-file:";

    /**
     * Create/find and return the appropriate external PDFAction according to the target
     *
     * @param target The external target. This may be a PDF file name
     * (optionally with internal page number or destination) or any type of URI.
     * @param newWindow boolean indicating whether the target should be
     *                  displayed in a new window
     * @return the PDFAction thus created or found
     */
    public PDFAction getExternalAction(String target, boolean newWindow) {
        int index;
        String targetLo = target.toLowerCase();
        if (target.startsWith(EMBEDDED_FILE)) {
            // File Attachments (Embedded Files)
            String filename = target.substring(EMBEDDED_FILE.length());
            return getActionForEmbeddedFile(filename, newWindow);
        } else if (targetLo.startsWith("http://")) {
            // HTTP URL?
            return new PDFUri(target);
        } else if (targetLo.startsWith("https://")) {
            // HTTPS URL?
            return new PDFUri(target);
        } else if (targetLo.startsWith("file://")) {
            // Non PDF files. Try to /Launch them.
            target = target.substring("file://".length());
            return getLaunchAction(target);
        } else if (targetLo.endsWith(".pdf")) {
            // Bare PDF file name?
            return getGoToPDFAction(target, null, -1, newWindow);
        } else if ((index = targetLo.indexOf(".pdf#page=")) > 0) { // CSOK: InnerAssignment
            // PDF file + page?
            String filename = target.substring(0, index + 4);
            int page = Integer.parseInt(target.substring(index + 10));
            return getGoToPDFAction(filename, null, page, newWindow);
        } else if ((index = targetLo.indexOf(".pdf#dest=")) > 0) { // CSOK: InnerAssignment
            // PDF file + destination?
            String filename = target.substring(0, index + 4);
            String dest = target.substring(index + 10);
            return getGoToPDFAction(filename, dest, -1, newWindow);
        } else {
            // None of the above? Default to URI:
            return new PDFUri(target);
        }
    }

    private PDFAction getActionForEmbeddedFile(String filename, boolean newWindow) {
        PDFNames names = getDocument().getRoot().getNames();
        if (names == null) {
            throw new IllegalStateException(
                    "No Names dictionary present."
                    + " Cannot create Launch Action for embedded file: " + filename);
        }
        PDFNameTreeNode embeddedFiles = names.getEmbeddedFiles();
        if (embeddedFiles == null) {
            throw new IllegalStateException(
                    "No /EmbeddedFiles name tree present."
                    + " Cannot create Launch Action for embedded file: " + filename);
        }

        //Find filespec reference for the embedded file
        filename = PDFText.toPDFString(filename, '_');
        PDFArray files = embeddedFiles.getNames();
        PDFReference embeddedFileRef = null;
        int i = 0;
        while (i < files.length()) {
            String name = (String)files.get(i);
            i++;
            PDFReference ref = (PDFReference)files.get(i);
            if (name.equals(filename)) {
                embeddedFileRef = ref;
                break;
            }
            i++;
        }
        if (embeddedFileRef == null) {
            throw new IllegalStateException(
                    "No embedded file with name " + filename + " present.");
        }

        //Finally create the action
        //PDFLaunch action = new PDFLaunch(embeddedFileRef);
        //This works with Acrobat 8 but not with Acrobat 9

        //The following two options didn't seem to have any effect.
        //PDFGoToEmbedded action = new PDFGoToEmbedded(embeddedFileRef, 0, newWindow);
        //PDFGoToRemote action = new PDFGoToRemote(embeddedFileRef, 0, newWindow);

        //This finally seems to work:
        StringBuffer scriptBuffer = new StringBuffer();
        scriptBuffer.append("this.exportDataObject({cName:\"");
        scriptBuffer.append(filename);
        scriptBuffer.append("\", nLaunch:2});");

        PDFJavaScriptLaunchAction action = new PDFJavaScriptLaunchAction(scriptBuffer.toString());
        return action;
    }

    /**
     * Create or find a PDF GoTo with the given page reference string and Y offset,
     * and return its PDF object reference
     *
     * @param pdfPageRef the PDF page reference, e.g. "23 0 R"
     * @param yoffset the distance from the bottom of the page in points
     * @return the GoTo's object reference
     */
    public String getGoToReference(String pdfPageRef, float yoffset) {
        return getPDFGoTo(pdfPageRef, new Point2D.Float(0.0f, yoffset)).referencePDF();
    }

    /**
     * Finds and returns a PDFGoTo to the given page and position.
     * Creates the PDFGoTo if not found.
     *
     * @param pdfPageRef the PDF page reference
     * @param position the (X,Y) position in points
     *
     * @return the new or existing PDFGoTo object
     */
    public PDFGoTo getPDFGoTo(String pdfPageRef, Point2D position) {
        getDocument().getProfile().verifyActionAllowed();
        PDFGoTo gt = new PDFGoTo(pdfPageRef, position);
        PDFGoTo oldgt = getDocument().findGoTo(gt);
        if (oldgt == null) {
            getDocument().assignObjectNumber(gt);
            getDocument().addTrailerObject(gt);
        } else {
            gt = oldgt;
        }
        return gt;
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
     * @param newWindow boolean indicating whether the target should be
     *                  displayed in a new window
     * @return the pdf goto remote object
     */
    private PDFGoToRemote getGoToPDFAction(String file, String dest, int page, boolean newWindow) {
        getDocument().getProfile().verifyActionAllowed();
        PDFFileSpec fileSpec = new PDFFileSpec(file);
        PDFFileSpec oldspec = getDocument().findFileSpec(fileSpec);
        if (oldspec == null) {
            getDocument().registerObject(fileSpec);
        } else {
            fileSpec = oldspec;
        }
        PDFGoToRemote remote;

        if (dest == null && page == -1) {
            remote = new PDFGoToRemote(fileSpec, newWindow);
        } else if (dest != null) {
            remote = new PDFGoToRemote(fileSpec, dest, newWindow);
        } else {
            remote = new PDFGoToRemote(fileSpec, page, newWindow);
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
     * Creates and returns a launch pdf document action using
     * <code>file</code> to create a file spcifiaciton for
     * the document/file to be opened with an external application.
     *
     * @param file the pdf file name
     * @return the pdf launch object
     */
    private PDFLaunch getLaunchAction(String file) {
        getDocument().getProfile().verifyActionAllowed();

        PDFFileSpec fileSpec = new PDFFileSpec(file);
        PDFFileSpec oldSpec = getDocument().findFileSpec(fileSpec);

        if (oldSpec == null) {
            getDocument().registerObject(fileSpec);
        } else {
            fileSpec = oldSpec;
        }
        PDFLaunch launch = new PDFLaunch(fileSpec);
        PDFLaunch oldLaunch = getDocument().findLaunch(launch);

        if (oldLaunch == null) {
            getDocument().registerObject(launch);
        } else {
            launch = oldLaunch;
        }

        return launch;
    }

    /**
     * Make an outline object and add it to the given parent
     *
     * @param parent the parent PDFOutline object (may be null)
     * @param label the title for the new outline object
     * @param actionRef the action reference string to be placed after the /A
     * @param showSubItems whether to initially display child outline items
     * @return the new PDF outline object
     */
    public PDFOutline makeOutline(PDFOutline parent, String label,
                                  String actionRef, boolean showSubItems) {
        PDFOutline pdfOutline = new PDFOutline(label, actionRef, showSubItems);
        if (parent != null) {
            parent.addOutline(pdfOutline);
        }
        getDocument().registerObject(pdfOutline);
        return pdfOutline;
    }

    /**
     * Make an outline object and add it to the given parent
     *
     * @param parent the parent PDFOutline object (may be null)
     * @param label the title for the new outline object
     * @param pdfAction the action that this outline item points to - must not be null!
     * @param showSubItems whether to initially display child outline items
     * @return the new PDFOutline object, or null if pdfAction is null
     */
    public PDFOutline makeOutline(PDFOutline parent, String label,
                                  PDFAction pdfAction, boolean showSubItems) {
        return pdfAction == null
                 ? null
                 : makeOutline(parent, label, pdfAction.getAction(), showSubItems);
    }

    // This one is obsolete now, at least it isn't called from anywhere inside FOP
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
        return makeOutline(parent, label, goToRef, showSubItems);
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
     * Make a Type1 /Font object.
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

        boolean forceToUnicode = true;

        if (descriptor == null) {
            //Usually Base 14 fonts
            PDFFont font = new PDFFont(fontname, FontType.TYPE1, basefont, encoding);
            getDocument().registerObject(font);
            if (forceToUnicode && !PDFEncoding.isPredefinedEncoding(encoding)) {
                SingleByteEncoding mapping;
                if (encoding != null) {
                    mapping = CodePointMapping.getMapping(encoding);
                } else {
                    //for Symbol and ZapfDingbats where encoding must be null in PDF
                    Typeface tf = (Typeface)metrics;
                    mapping = CodePointMapping.getMapping(tf.getEncodingName());
                }
                generateToUnicodeCmap(font, mapping);
            }
            return font;
        } else {
            FontType fonttype = metrics.getFontType();

            String fontPrefix = descriptor.isSubsetEmbedded() ? createSubsetFontPrefix() : "";

            String subsetFontName = fontPrefix + basefont;

            PDFFontDescriptor pdfdesc = makeFontDescriptor(descriptor, fontPrefix);

            PDFFont font = null;

            font = PDFFont.createFont(fontname, fonttype, subsetFontName, null);
            getDocument().registerObject(font);

            if (fonttype == FontType.TYPE0) {
                font.setEncoding(encoding);
                CIDFont cidMetrics;
                if (metrics instanceof LazyFont) {
                    cidMetrics = (CIDFont)((LazyFont) metrics).getRealFont();
                } else {
                    cidMetrics = (CIDFont)metrics;
                }
                PDFCIDSystemInfo sysInfo
                    = new PDFCIDSystemInfo(cidMetrics.getRegistry(),
                                         cidMetrics.getOrdering(),
                                         cidMetrics.getSupplement());
                PDFCIDFont cidFont = new PDFCIDFont(subsetFontName,
                                   cidMetrics.getCIDType(),
                                   cidMetrics.getDefaultWidth(),
                                   getSubsetWidths(cidMetrics), sysInfo,
                                   (PDFCIDFontDescriptor)pdfdesc);
                getDocument().registerObject(cidFont);

                PDFCMap cmap = new PDFToUnicodeCMap(
                        cidMetrics.getCIDSubset().getSubsetChars(),
                        "fop-ucs-H",
                        new PDFCIDSystemInfo("Adobe",
                            "Identity",
                            0), false);
                getDocument().registerObject(cmap);
                ((PDFFontType0)font).setCMAP(cmap);
                ((PDFFontType0)font).setDescendantFonts(cidFont);
            } else {
                PDFFontNonBase14 nonBase14 = (PDFFontNonBase14)font;
                nonBase14.setDescriptor(pdfdesc);

                SingleByteFont singleByteFont;
                if (metrics instanceof LazyFont) {
                    singleByteFont = (SingleByteFont)((LazyFont)metrics).getRealFont();
                } else {
                    singleByteFont = (SingleByteFont)metrics;
                }
                int firstChar = singleByteFont.getFirstChar();
                int lastChar = singleByteFont.getLastChar();
                nonBase14.setWidthMetrics(firstChar,
                                     lastChar,
                                     new PDFArray(null, metrics.getWidths()));

                //Handle encoding
                SingleByteEncoding mapping = singleByteFont.getEncoding();
                if (singleByteFont.isSymbolicFont()) {
                    //no encoding, use the font's encoding
                    if (forceToUnicode) {
                        generateToUnicodeCmap(nonBase14, mapping);
                    }
                } else if (PDFEncoding.isPredefinedEncoding(mapping.getName())) {
                    font.setEncoding(mapping.getName());
                    //No ToUnicode CMap necessary if PDF 1.4, chapter 5.9 (page 368) is to be
                    //believed.
                } else {
                    Object pdfEncoding = createPDFEncoding(mapping,
                            singleByteFont.getFontName());
                    if (pdfEncoding instanceof PDFEncoding) {
                        font.setEncoding((PDFEncoding)pdfEncoding);
                    } else {
                        font.setEncoding((String)pdfEncoding);
                    }
                    if (forceToUnicode) {
                        generateToUnicodeCmap(nonBase14, mapping);
                    }
                }

                //Handle additional encodings (characters outside the primary encoding)
                if (singleByteFont.hasAdditionalEncodings()) {
                    for (int i = 0, c = singleByteFont.getAdditionalEncodingCount(); i < c; i++) {
                        SimpleSingleByteEncoding addEncoding
                            = singleByteFont.getAdditionalEncoding(i);
                        String name = fontname + "_" + (i + 1);
                        Object pdfenc = createPDFEncoding(addEncoding,
                                singleByteFont.getFontName());
                        PDFFontNonBase14 addFont = (PDFFontNonBase14)PDFFont.createFont(
                                name, fonttype,
                                basefont, pdfenc);
                        addFont.setDescriptor(pdfdesc);
                        addFont.setWidthMetrics(
                                addEncoding.getFirstChar(),
                                addEncoding.getLastChar(),
                                new PDFArray(null, singleByteFont.getAdditionalWidths(i)));
                        getDocument().registerObject(addFont);
                        getDocument().getResources().addFont(addFont);
                        if (forceToUnicode) {
                            generateToUnicodeCmap(addFont, addEncoding);
                        }
                    }
                }
            }

            return font;
        }
    }

    private void generateToUnicodeCmap(PDFFont font, SingleByteEncoding encoding) {
        PDFCMap cmap = new PDFToUnicodeCMap(encoding.getUnicodeCharMap(),
                "fop-ucs-H",
                new PDFCIDSystemInfo("Adobe", "Identity", 0), true);
        getDocument().registerObject(cmap);
        font.setToUnicode(cmap);
    }

    /**
     * Creates a PDFEncoding instance from a CodePointMapping instance.
     * @param encoding the code point mapping (encoding)
     * @param fontNameHint ...
     * @return the PDF Encoding dictionary (or a String with the predefined encoding)
     */
    public Object createPDFEncoding(SingleByteEncoding encoding, String fontNameHint) {
        SingleByteEncoding baseEncoding;
        if (fontNameHint.indexOf("Symbol") >= 0) {
            baseEncoding = CodePointMapping.getMapping(
                    CodePointMapping.SYMBOL_ENCODING);
        } else {
            baseEncoding = CodePointMapping.getMapping(
                    CodePointMapping.STANDARD_ENCODING);
        }
        PDFEncoding pdfEncoding = new PDFEncoding(baseEncoding.getName());
        PDFEncoding.DifferencesBuilder builder
                = pdfEncoding.createDifferencesBuilder();
        int start = -1;
        String[] baseNames = baseEncoding.getCharNameMap();
        String[] charNameMap = encoding.getCharNameMap();
        for (int i = 0, ci = charNameMap.length; i < ci; i++) {
            String basec = baseNames[i];
            String c = charNameMap[i];
            if (!basec.equals(c)) {
                if (start != i) {
                    builder.addDifference(i);
                    start = i;
                }
                builder.addName(c);
                start++;
            }
        }
        if (builder.hasDifferences()) {
            pdfEncoding.setDifferences(builder.toPDFArray());
            return pdfEncoding;
        } else {
            return baseEncoding.getName();
        }
    }

    /**
     * Creates and returns a width array with the widths of all the characters in the subset.
     * @param cidFont the font
     * @return the width array
     */
    public PDFWArray getSubsetWidths(CIDFont cidFont) {
        // Create widths for reencoded chars
        PDFWArray warray = new PDFWArray();
        int[] widths = cidFont.getWidths();
        CIDSubset subset = cidFont.getCIDSubset();
        int[] tmpWidth = new int[subset.getSubsetSize()];

        for (int i = 0, c = subset.getSubsetSize(); i < c; i++) {
            int nwx = Math.max(0, subset.getGlyphIndexForSubsetIndex(i));
            tmpWidth[i] = widths[nwx];
        }
        warray.addEntry(0, tmpWidth);
        return warray;
    }

    private String createSubsetFontPrefix() {
        subsetFontCounter++;
        DecimalFormat counterFormat = new DecimalFormat("00000");
        String counterString = counterFormat.format(subsetFontCounter);

        // Subset prefix as described in chapter 5.5.3 of PDF 1.4
        StringBuffer sb = new StringBuffer("E");

        for (char c : counterString.toCharArray()) {
            // translate numbers to uppercase characters
            sb.append((char) (c + ('A' - '0')));
        }
        sb.append("+");
        return sb.toString();
    }

    /**
     * make a /FontDescriptor object
     *
     * @param desc the font descriptor
     * @param fontPrefix the String with which to prefix the font name
     * @return the new PDF font descriptor
     */
    private PDFFontDescriptor makeFontDescriptor(FontDescriptor desc, String fontPrefix) {
        PDFFontDescriptor descriptor = null;

        if (desc.getFontType() == FontType.TYPE0) {
            // CID Font
            descriptor = new PDFCIDFontDescriptor(fontPrefix + desc.getEmbedFontName(),
                                            desc.getFontBBox(),
                                            desc.getCapHeight(),
                                            desc.getFlags(),
                                            desc.getItalicAngle(),
                                            desc.getStemV(), null);
        } else {
            // Create normal FontDescriptor
            descriptor = new PDFFontDescriptor(desc.getEmbedFontName(),
                                         desc.getAscender(),
                                         desc.getDescender(),
                                         desc.getCapHeight(),
                                         desc.getFlags(),
                                         new PDFRectangle(desc.getFontBBox()),
                                         desc.getItalicAngle(),
                                         desc.getStemV());
        }
        getDocument().registerObject(descriptor);

        // Check if the font is embeddable
        if (desc.isEmbeddable()) {
            AbstractPDFStream stream = makeFontFile(desc);
            if (stream != null) {
                descriptor.setFontFile(desc.getFontType(), stream);
                getDocument().registerObject(stream);
            }
            CustomFont font = getCustomFont(desc);
            if (font instanceof CIDFont) {
                CIDFont cidFont = (CIDFont)font;
                buildCIDSet(descriptor, cidFont);
            }
        }
        return descriptor;
    }

    private void buildCIDSet(PDFFontDescriptor descriptor, CIDFont cidFont) {
        BitSet cidSubset = cidFont.getCIDSubset().getGlyphIndexBitSet();
        PDFStream cidSet = makeStream(null, true);
        ByteArrayOutputStream baout = new ByteArrayOutputStream(cidSubset.length() / 8 + 1);
        int value = 0;
        for (int i = 0, c = cidSubset.length(); i < c; i++) {
            int shift = i % 8;
            boolean b = cidSubset.get(i);
            if (b) {
                value |= 1 << 7 - shift;
            }
            if (shift == 7) {
                baout.write(value);
                value = 0;
            }
        }
        baout.write(value);
        try {
            cidSet.setData(baout.toByteArray());
            descriptor.setCIDSet(cidSet);
        } catch (IOException ioe) {
            log.error(
                    "Failed to write CIDSet [" + cidFont + "] "
                    + cidFont.getEmbedFontName(), ioe);
        }
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

        CustomFont font = getCustomFont(desc);

        InputStream in = null;
        try {
            Source source = font.getEmbedFileSource();
            if (source == null && font.getEmbedResourceName() != null) {
                source = new StreamSource(this.getClass()
                        .getResourceAsStream(font.getEmbedResourceName()));
            }
            if (source == null) {
                return null;
            }
            if (source instanceof StreamSource) {
                in = ((StreamSource) source).getInputStream();
            }
            if (in == null && source.getSystemId() != null) {
                try {
                    in = new java.net.URL(source.getSystemId()).openStream();
                } catch (MalformedURLException e) {
                    //TODO: Why construct a new exception here, when it is not thrown?
                    new FileNotFoundException(
                            "File not found. URL could not be resolved: "
                                    + e.getMessage());
                }
            }
            if (in == null) {
                return null;
            }
            //Make sure the InputStream is decorated with a BufferedInputStream
            if (!(in instanceof java.io.BufferedInputStream)) {
                in = new java.io.BufferedInputStream(in);
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
                        subset.readFont(reader, mbfont.getTTCName(), mbfont.getUsedGlyphs());
                        byte[] subsetFont = subset.getFontSubset();
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
                    + desc.getEmbedFontName(), ioe);
            return null;
        }
    }

    private CustomFont getCustomFont(FontDescriptor desc) {
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
        return (CustomFont)tempFont;
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
     * @see PDFImageXObject
     * @see org.apache.fop.pdf.PDFDeviceColorSpace
     * @return the new PDF ICC stream object
     */
    public PDFICCStream makePDFICCStream() {
        PDFICCStream iccStream = new PDFICCStream();

        getDocument().registerObject(iccStream);
        //getDocument().applyEncryption(iccStream);
        return iccStream;
    }

    /* ========================= misc. objects ============================= */

    /**
     * Makes a new ICCBased color space and registers it in the resource context.
     * @param res the PDF resource context to add the shading, may be null
     * @param explicitName the explicit name for the color space, may be null
     * @param iccStream the ICC stream to associate with this color space
     * @return the newly instantiated color space
     */
    public PDFICCBasedColorSpace makeICCBasedColorSpace(PDFResourceContext res,
            String explicitName, PDFICCStream iccStream) {
        PDFICCBasedColorSpace cs = new PDFICCBasedColorSpace(explicitName, iccStream);

        getDocument().registerObject(cs);

        if (res != null) {
            res.getPDFResources().addColorSpace(cs);
        } else {
            getDocument().getResources().addColorSpace(cs);
        }

        return cs;
    }

    /**
     * Create a new Separation color space.
     * @param res the resource context (may be null)
     * @param ncs the named color space to map to a separation color space
     * @return the newly created Separation color space
     */
    public PDFSeparationColorSpace makeSeparationColorSpace(PDFResourceContext res,
            NamedColorSpace ncs) {
        String colorName = ncs.getColorName();
        final Double zero = new Double(0d);
        final Double one = new Double(1d);
        List theDomain = Arrays.asList(new Double[] {zero, one});
        List theRange = Arrays.asList(new Double[] {zero, one, zero, one, zero, one});
        List theCZero = Arrays.asList(new Double[] {one, one, one});
        List theCOne = new ArrayList();
        float[] comps = ncs.getRGBColor().getColorComponents(null);
        for (int i = 0, c = comps.length; i < c; i++) {
            theCOne.add(new Double(comps[i]));
        }
        PDFFunction tintFunction = makeFunction(2, theDomain, theRange,
                theCZero, theCOne, 1.0d);
        PDFSeparationColorSpace cs = new PDFSeparationColorSpace(colorName, tintFunction);
        getDocument().registerObject(cs);
        if (res != null) {
            res.getPDFResources().addColorSpace(cs);
        } else {
            getDocument().getResources().addColorSpace(cs);
        }

        return cs;
    }

    /**
     * Make an Array object (ex. Widths array for a font).
     *
     * @param values the int array values
     * @return the PDF Array with the int values
     */
    public PDFArray makeArray(int[] values) {
        PDFArray array = new PDFArray(null, values);
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
