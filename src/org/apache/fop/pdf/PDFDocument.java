/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

/* image support modified from work of BoBoGi */
/* font support based on work by Takayuki Takeuchi */

package org.apache.fop.pdf;

// images are the one place that FOP classes outside this package get
// referenced and I'd rather not do it
import org.apache.fop.image.FopImage;

import org.apache.fop.layout.LinkSet;
import org.apache.fop.datatypes.ColorSpace;

import org.apache.fop.render.pdf.CIDFont;
import org.apache.fop.render.pdf.fonts.LazyFont;

import org.apache.fop.datatypes.IDReferences;
import org.apache.fop.layout.FontMetric;
import org.apache.fop.layout.FontDescriptor;
// Java
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.awt.Rectangle;

/**
 * class representing a PDF document.
 *
 * The document is built up by calling various methods and then finally
 * output to given filehandle using output method.
 *
 * A PDF document consists of a series of numbered objects preceded by a
 * header and followed by an xref table and trailer. The xref table
 * allows for quick access to objects by listing their character
 * positions within the document. For this reason the PDF document must
 * keep track of the character position of each object.  The document
 * also keeps direct track of the /Root, /Info and /Resources objects.
 *
 * Modified by Mark Lillywhite, mark-fop@inomial.com. The changes
 * involve: ability to output pages one-at-a-time in a streaming
 * fashion (rather than storing them all for output at the end);
 * ability to write the /Pages object after writing the rest
 * of the document; ability to write to a stream and flush
 * the object list; enhanced trailer output; cleanups.
 *
 * Modified by lmckenzi@ca.ibm.com
 * Sometimes IDs are created, but not validated. This tracks
 * the difference.
 */
public class PDFDocument {
    private static final Integer locationPlaceholder = new Integer(0);
    /**
     * the version of PDF supported
     */
    protected static final String pdfVersion = "1.3";

    /**
     * the current character position
     */
    protected int position = 0;

    /**
     * the character position of each object
     */
    protected ArrayList location = new ArrayList();

    /** List of objects to write in the trailer */
    private ArrayList trailerObjects = new ArrayList();

    /**
     * the counter for object numbering
     */
    protected int objectcount = 0;

    /**
     * the objects themselves
     */
    protected ArrayList objects = new ArrayList();

    /**
     * character position of xref table
     */
    protected int xref;

    /**
     * the /Root object
     */
    protected PDFRoot root;

    /** The root outline object */
    private PDFOutline outlineRoot = null;

    /** The /Pages object (mark-fop@inomial.com) */
    private PDFPages pages;

    /**
     * the /Info object
     */
    protected PDFInfo info;

    /**
     * the /Resources object
     */
    protected PDFResources resources;

    /**
     * the documents idReferences
     */
    protected IDReferences idReferences;

    /**
     * the colorspace (0=RGB, 1=CMYK)
     */
    // protected int colorspace = 0;
    protected ColorSpace colorspace = new ColorSpace(ColorSpace.DEVICE_RGB);

    /**
     * the counter for Pattern name numbering (e.g. 'Pattern1')
     */
    protected int patternCount = 0;

    /**
     * the counter for Shading name numbering
     */
    protected int shadingCount = 0;

    /**
     * the counter for XObject numbering
     */
    protected int xObjectCount = 0;

    /**
     * the XObjects
     */
    protected ArrayList xObjects = new ArrayList();

    /**
     * the XObjects Map.
     * Should be modified (works only for image subtype)
     */
    protected HashMap xObjectsMap = new HashMap();

    /**
     * the objects themselves
     */
    protected ArrayList pendingLinks = null;

    /**
     * creates an empty PDF document <p>
     * 
     * The constructor creates a /Root and /Pages object to
     * track the document but does not write these objects until
     * the trailer is written. Note that the object ID of the
     * pages object is determined now, and the xref table is
     * updated later. This allows Pages to refer to their
     * Parent before we write it out. This took me a long
     * time to work out, and is so obvious now. Sigh.
     * mark-fop@inomial.com. Maybe I should do a PDF course.
     */
    public PDFDocument() {

        /* create the /Root, /Info and /Resources objects */
        this.pages = makePages();

        // Create the Root object
        this.root = makeRoot(pages);

        // Create the Resources object
        this.resources = makeResources();

        // Make the /Info record
        this.info = makeInfo();
    }

    /**
     * set the producer of the document
     *
     * @param producer string indicating application producing the PDF
     */
    public void setProducer(String producer) {
        this.info.setProducer(producer);
    }

    /**
     * Make a /Catalog (Root) object. This object is written in
     * the trailer.
     */
    public PDFRoot makeRoot(PDFPages pages) {

        /*
        * Make a /Pages object. This object is written in the trailer.
        */
        PDFRoot pdfRoot = new PDFRoot(++this.objectcount, pages);
        addTrailerObject(pdfRoot);
        return pdfRoot;
    }

    /**
     * Make a /Pages object. This object is written in the trailer.
     */

    public PDFPages makePages() {
        PDFPages pdfPages = new PDFPages(++this.objectcount);
        addTrailerObject(pdfPages);
        return pdfPages;
    }

    /**
     * Make a /Resources object. This object is written in the trailer.
     */
    public PDFResources makeResources() {
        PDFResources pdfResources = new PDFResources(++this.objectcount);
        addTrailerObject(pdfResources);
        return pdfResources;
    }

    /**
     * make an /Info object
     *
     * @param producer string indicating application producing the PDF
     * @return the created /Info object
     */
    protected PDFInfo makeInfo() {

        /*
         * create a PDFInfo with the next object number and add to
         * list of objects
         */
        PDFInfo pdfInfo = new PDFInfo(++this.objectcount);
        // set the default producer
        pdfInfo.setProducer(org.apache.fop.apps.Version.getVersion());
        this.objects.add(pdfInfo);
        return pdfInfo;
    }

    /**
     * Make a Type 0 sampled function
     *
     * @param theDomain ArrayList objects of Double objects.
     * This is the domain of the function.
     * See page 264 of the PDF 1.3 Spec.
     * @param theRange ArrayList objects of Double objects.
     * This is the Range of the function.
     * See page 264 of the PDF 1.3 Spec.
     * @param theSize A ArrayList object of Integer objects.
     * This is the number of samples in each input dimension.
     * I can't imagine there being more or less than two input dimensions,
     * so maybe this should be an array of length 2.
     *
     * See page 265 of the PDF 1.3 Spec.
     * @param theBitsPerSample An int specifying the number of bits user to represent each sample value.
     * Limited to 1,2,4,8,12,16,24 or 32.
     * See page 265 of the 1.3 PDF Spec.
     * @param theOrder The order of interpolation between samples. Default is 1 (one). Limited
     * to 1 (one) or 3, which means linear or cubic-spline interpolation.
     *
     * This attribute is optional.
     *
     * See page 265 in the PDF 1.3 spec.
     * @param theEncode ArrayList objects of Double objects.
     * This is the linear mapping of input values intop the domain
     * of the function's sample table. Default is hard to represent in
     * ascii, but basically [0 (Size0 1) 0 (Size1 1)...].
     * This attribute is optional.
     *
     * See page 265 in the PDF 1.3 spec.
     * @param theDecode ArrayList objects of Double objects.
     * This is a linear mapping of sample values into the range.
     * The default is just the range.
     *
     * This attribute is optional.
     * Read about it on page 265 of the PDF 1.3 spec.
     * @param theFunctionDataStream The sample values that specify the function are provided in a stream.
     *
     * This is optional, but is almost always used.
     *
     * Page 265 of the PDF 1.3 spec has more.
     * @param theFilter This is a vector of String objects which are the various filters that
     * have are to be applied to the stream to make sense of it. Order matters,
     * so watch out.
     *
     * This is not documented in the Function section of the PDF 1.3 spec,
     * it was deduced from samples that this is sometimes used, even if we may never
     * use it in FOP. It is added for completeness sake.
     * @param theNumber The object number of this PDF object.
     * @param theFunctionType This is the type of function (0,2,3, or 4).
     * It should be 0 as this is the constructor for sampled functions.
     */
    public PDFFunction makeFunction(int theFunctionType, ArrayList theDomain,
                                    ArrayList theRange, ArrayList theSize,
                                    int theBitsPerSample, int theOrder,
                                    ArrayList theEncode, ArrayList theDecode,
                                    StringBuffer theFunctionDataStream,
                                    ArrayList theFilter) {    // Type 0 function
        PDFFunction function = new PDFFunction(++this.objectcount,
                                               theFunctionType, theDomain,
                                               theRange, theSize,
                                               theBitsPerSample, theOrder,
                                               theEncode, theDecode,
                                               theFunctionDataStream,
                                               theFilter);

        this.objects.add(function);
        return (function);
    }

    /**
     * make a type Exponential interpolation function
     * (for shading usually)
     *
     * @param theDomain ArrayList objects of Double objects.
     * This is the domain of the function.
     * See page 264 of the PDF 1.3 Spec.
     * @param theRange ArrayList of Doubles that is the Range of the function.
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
     */
    public PDFFunction makeFunction(int theFunctionType, ArrayList theDomain,
                                    ArrayList theRange, ArrayList theCZero,
                                    ArrayList theCOne,
                                    double theInterpolationExponentN) {    // type 2
        PDFFunction function = new PDFFunction(++this.objectcount,
                                               theFunctionType, theDomain,
                                               theRange, theCZero, theCOne,
                                               theInterpolationExponentN);

        this.objects.add(function);
        return (function);
    }

    /**
     * Make a Type 3 Stitching function
     *
     * @param theDomain ArrayList objects of Double objects.
     * This is the domain of the function.
     * See page 264 of the PDF 1.3 Spec.
     * @param theRange ArrayList objects of Double objects.
     * This is the Range of the function.
     * See page 264 of the PDF 1.3 Spec.
     * @param theFunctions A ArrayList of the PDFFunction objects that the stitching function stitches.
     *
     * This attributed is required.
     * It is described on page 269 of the PDF spec.
     * @param theBounds This is a vector of Doubles representing the numbers that,
     * in conjunction with Domain define the intervals to which each function from
     * the 'functions' object applies. It must be in order of increasing magnitude,
     * and each must be within Domain.
     *
     * It basically sets how much of the gradient each function handles.
     *
     * This attributed is required.
     * It's described on page 269 of the PDF 1.3 spec.
     * @param theEncode ArrayList objects of Double objects.
     * This is the linear mapping of input values intop the domain
     * of the function's sample table. Default is hard to represent in
     * ascii, but basically [0 (Size0 1) 0 (Size1 1)...].
     * This attribute is required.
     *
     * See page 270 in the PDF 1.3 spec.
     * @param theFunctionType This is the function type. It should be 3,
     * for a stitching function.
     */
    public PDFFunction makeFunction(int theFunctionType, ArrayList theDomain,
                                    ArrayList theRange, ArrayList theFunctions,
                                    ArrayList theBounds,
                                    ArrayList theEncode) {    // Type 3

        PDFFunction function = new PDFFunction(++this.objectcount,
                                               theFunctionType, theDomain,
                                               theRange, theFunctions,
                                               theBounds, theEncode);

        this.objects.add(function);
        return (function);
    }

    /**
     * make a postscript calculator function
     *
     * @param theNumber
     * @param theFunctionType
     * @param theDomain
     * @param theRange
     * @param theFunctionDataStream
     */
    public PDFFunction makeFunction(int theNumber, int theFunctionType,
                                    ArrayList theDomain, ArrayList theRange,
                                    StringBuffer theFunctionDataStream) {    // Type 4
        PDFFunction function = new PDFFunction(++this.objectcount,
                                               theFunctionType, theDomain,
                                               theRange,
                                               theFunctionDataStream);

        this.objects.add(function);
        return (function);

    }

    /**
     * make a function based shading object
     *
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
    public PDFShading makeShading(int theShadingType,
                                  ColorSpace theColorSpace,
                                  ArrayList theBackground, ArrayList theBBox,
                                  boolean theAntiAlias, ArrayList theDomain,
                                  ArrayList theMatrix,
                                  PDFFunction theFunction) {    // make Shading of Type 1
        String theShadingName = new String("Sh" + (++this.shadingCount));

        PDFShading shading = new PDFShading(++this.objectcount,
                                            theShadingName, theShadingType,
                                            theColorSpace, theBackground,
                                            theBBox, theAntiAlias, theDomain,
                                            theMatrix, theFunction);
        this.objects.add(shading);

        // add this shading to resources
        this.resources.addShading(shading);

        return (shading);
    }

    /**
     * Make an axial or radial shading object.
     *
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
    public PDFShading makeShading(int theShadingType,
                                  ColorSpace theColorSpace,
                                  ArrayList theBackground, ArrayList theBBox,
                                  boolean theAntiAlias, ArrayList theCoords,
                                  ArrayList theDomain, PDFFunction theFunction,
                                  ArrayList theExtend) {    // make Shading of Type 2 or 3
        String theShadingName = new String("Sh" + (++this.shadingCount));

        PDFShading shading = new PDFShading(++this.objectcount,
                                            theShadingName, theShadingType,
                                            theColorSpace, theBackground,
                                            theBBox, theAntiAlias, theCoords,
                                            theDomain, theFunction,
                                            theExtend);

        this.resources.addShading(shading);

        this.objects.add(shading);
        return (shading);
    }

    /**
     * Make a free-form gouraud shaded triangle mesh, coons patch mesh, or tensor patch mesh
     * shading object
     *
     * @param theShadingType 4, 6, or 7 depending on whether it's
     * Free-form gouraud-shaded triangle meshes, coons patch meshes,
     * or tensor product patch meshes, respectively.
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
    public PDFShading makeShading(int theShadingType,
                                  ColorSpace theColorSpace,
                                  ArrayList theBackground, ArrayList theBBox,
                                  boolean theAntiAlias,
                                  int theBitsPerCoordinate,
                                  int theBitsPerComponent,
                                  int theBitsPerFlag, ArrayList theDecode,
                                  PDFFunction theFunction) {    // make Shading of type 4,6 or 7
        String theShadingName = new String("Sh" + (++this.shadingCount));

        PDFShading shading = new PDFShading(++this.objectcount,
                                            theShadingName, theShadingType,
                                            theColorSpace, theBackground,
                                            theBBox, theAntiAlias,
                                            theBitsPerCoordinate,
                                            theBitsPerComponent,
                                            theBitsPerFlag, theDecode,
                                            theFunction);

        this.resources.addShading(shading);

        this.objects.add(shading);
        return (shading);
    }

    /**
     * make a Lattice-Form Gouraud mesh shading object
     *
     * @param theShadingType 5 for lattice-Form Gouraud shaded-triangle mesh
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
     */
    public PDFShading makeShading(int theShadingType,
                                  ColorSpace theColorSpace,
                                  ArrayList theBackground, ArrayList theBBox,
                                  boolean theAntiAlias,
                                  int theBitsPerCoordinate,
                                  int theBitsPerComponent, ArrayList theDecode,
                                  int theVerticesPerRow,
                                  PDFFunction theFunction) {    // make shading of Type 5
        String theShadingName = new String("Sh" + (++this.shadingCount));

        PDFShading shading = new PDFShading(++this.objectcount,
                                            theShadingName, theShadingType,
                                            theColorSpace, theBackground,
                                            theBBox, theAntiAlias,
                                            theBitsPerCoordinate,
                                            theBitsPerComponent, theDecode,
                                            theVerticesPerRow, theFunction);

        this.resources.addShading(shading);

        this.objects.add(shading);

        return (shading);
    }

    /**
     * Make a tiling pattern
     *
     * @param thePatternType the type of pattern, which is 1 for tiling.
     * @param theResources the resources associated with this pattern
     * @param thePaintType 1 or 2, colored or uncolored.
     * @param theTilingType 1, 2, or 3, constant spacing, no distortion, or faster tiling
     * @param theBBox ArrayList of Doubles: The pattern cell bounding box
     * @param theXStep horizontal spacing
     * @param theYStep vertical spacing
     * @param theMatrix Optional ArrayList of Doubles transformation matrix
     * @param theXUID Optional vector of Integers that uniquely identify the pattern
     * @param thePatternDataStream The stream of pattern data to be tiled.
     */
    public PDFPattern makePattern(int thePatternType,    // 1
                                  PDFResources theResources, int thePaintType, int theTilingType,
                                  ArrayList theBBox, double theXStep, double theYStep, ArrayList theMatrix,
                                  ArrayList theXUID, StringBuffer thePatternDataStream) {
        String thePatternName = new String("Pa" + (++this.patternCount));
        // int theNumber, String thePatternName,
        // PDFResources theResources
        PDFPattern pattern = new PDFPattern(++this.objectcount,
                                            thePatternName, theResources, 1,
                                            thePaintType, theTilingType,
                                            theBBox, theXStep, theYStep,
                                            theMatrix, theXUID,
                                            thePatternDataStream);

        this.resources.addPattern(pattern);
        this.objects.add(pattern);

        return (pattern);
    }

    /**
     * Make a smooth shading pattern
     *
     * @param thePatternType the type of the pattern, which is 2, smooth shading
     * @param theShading the PDF Shading object that comprises this pattern
     * @param theXUID optional:the extended unique Identifier if used.
     * @param theExtGState optional: the extended graphics state, if used.
     * @param theMatrix Optional:ArrayList of Doubles that specify the matrix.
     */
    public PDFPattern makePattern(int thePatternType, PDFShading theShading,
                                  ArrayList theXUID, StringBuffer theExtGState,
                                  ArrayList theMatrix) {
        String thePatternName = new String("Pa" + (++this.patternCount));

        PDFPattern pattern = new PDFPattern(++this.objectcount,
                                            thePatternName, 2, theShading,
                                            theXUID, theExtGState, theMatrix);

        this.resources.addPattern(pattern);
        this.objects.add(pattern);

        return (pattern);
    }

    public int getColorSpace() {
        return (this.colorspace.getColorSpace());
    }

    public void setColorSpace(int theColorspace) {
        this.colorspace.setColorSpace(theColorspace);
        return;
    }

    public PDFPattern createGradient(boolean radial,
                                     ColorSpace theColorspace,
                                     ArrayList theColors, ArrayList theBounds,
                                     ArrayList theCoords) {
        PDFShading myShad;
        PDFFunction myfunky;
        PDFFunction myfunc;
        ArrayList theCzero;
        ArrayList theCone;
        PDFPattern myPattern;
        ColorSpace theColorSpace;
        double interpolation = (double)1.000;
        ArrayList theFunctions = new ArrayList();

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
            if (this.colorspace.getColorSpace()
                    != currentColor.getColorSpace())
                currentColor.setColorSpace(this.colorspace.getColorSpace());

            if (this.colorspace.getColorSpace() != nextColor.getColorSpace())
                nextColor.setColorSpace(this.colorspace.getColorSpace());

            theCzero = currentColor.getVector();
            theCone = nextColor.getVector();

            myfunc = this.makeFunction(2, null, null, theCzero, theCone,
                                       interpolation);

            theFunctions.add(myfunc);

        }                               // end of for every consecutive color pair

        myfunky = this.makeFunction(3, null, null, theFunctions, theBounds,
                                    null);

        if (radial) {
            if (theCoords.size() == 6) {
                myShad = this.makeShading(3, this.colorspace, null, null,
                                          false, theCoords, null, myfunky,
                                          null);
            } else {    // if the center x, center y, and radius specifiy
                // the gradient, then assume the same center x, center y,
                // and radius of zero for the other necessary component
                ArrayList newCoords = new ArrayList();
                newCoords.add(theCoords.get(0));
                newCoords.add(theCoords.get(1));
                newCoords.add(theCoords.get(2));
                newCoords.add(theCoords.get(0));
                newCoords.add(theCoords.get(1));
                newCoords.add(new Double(0.0));

                myShad = this.makeShading(3, this.colorspace, null, null,
                                          false, newCoords, null, myfunky,
                                          null);

            }
        } else {
            myShad = this.makeShading(2, this.colorspace, null, null, false,
                                      theCoords, null, myfunky, null);

        }

        myPattern = this.makePattern(2, myShad, null, null, null);

        return (myPattern);
    }


    /**
     * make a /Encoding object
     *
     * @param encodingName character encoding scheme name
     * @return the created /Encoding object
     */
    public PDFEncoding makeEncoding(String encodingName) {

        /*
         * create a PDFEncoding with the next object number and add to the
         * list of objects
         */
        PDFEncoding encoding = new PDFEncoding(++this.objectcount,
                                               encodingName);
        this.objects.add(encoding);
        return encoding;
    }

        /**
         * Create a PDFICCStream
         @see PDFXObject
         @see org.apache.fop.image.JpegImage
         @see org.apache.fop.datatypes.ColorSpace
        */
    public PDFICCStream makePDFICCStream() {
        PDFICCStream iccStream = new PDFICCStream(++this.objectcount);
        this.objects.add(iccStream);
        return iccStream;
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
                            String encoding, FontMetric metrics,
                            FontDescriptor descriptor) {

        /*
         * create a PDFFont with the next object number and add to the
         * list of objects
         */
        if (descriptor == null) {
            PDFFont font = new PDFFont(++this.objectcount, fontname,
                                       PDFFont.TYPE1, basefont, encoding);
            this.objects.add(font);
            return font;
        } else {
            byte subtype = PDFFont.TYPE1;
            if (metrics instanceof org.apache.fop.render.pdf.Font)
                subtype =
                    ((org.apache.fop.render.pdf.Font)metrics).getSubType();

            PDFFontDescriptor pdfdesc = makeFontDescriptor(descriptor,
                                        subtype);

            PDFFontNonBase14 font = null;
            if (subtype == PDFFont.TYPE0) {
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
                    (PDFFontNonBase14)PDFFont.createFont(++this.objectcount,
                                                         fontname, subtype,
                                                         basefont,
                                                         "Identity-H");
            } else {

                font =
                    (PDFFontNonBase14)PDFFont.createFont(++this.objectcount,
                                                         fontname, subtype,
                                                         basefont, encoding);
            }
            this.objects.add(font);

            font.setDescriptor(pdfdesc);

            if (subtype == PDFFont.TYPE0) {
                CIDFont cidMetrics;
                if(metrics instanceof LazyFont){
                    cidMetrics = (CIDFont) ((LazyFont) metrics).getRealFont();
                }else{
                    cidMetrics = (CIDFont)metrics;
                }
                PDFCIDSystemInfo sysInfo =
                    new PDFCIDSystemInfo(cidMetrics.getRegistry(),
                                         cidMetrics.getOrdering(),
                                         cidMetrics.getSupplement());
                PDFCIDFont cidFont =
                    new PDFCIDFont(++this.objectcount, basefont,
                                   cidMetrics.getCidType(),
                                   cidMetrics.getDefaultWidth(),
                                   cidMetrics.getWidths(), sysInfo,
                                   (PDFCIDFontDescriptor)pdfdesc);
                this.objects.add(cidFont);

                // ((PDFFontType0)font).setCMAP(cmap);

                ((PDFFontType0)font).setDescendantFonts(cidFont);
            } else {
                font.setWidthMetrics(metrics.getFirstChar(),
                                     metrics.getLastChar(),
                                     makeArray(metrics.getWidths(1)));
            }

            return font;
        }
    }


    /**
     * make a /FontDescriptor object
     */
    public PDFFontDescriptor makeFontDescriptor(FontDescriptor desc,
            byte subtype) {
        PDFFontDescriptor font = null;

        if (subtype == PDFFont.TYPE0) {
            // CID Font
            font = new PDFCIDFontDescriptor(++this.objectcount,
                                            desc.fontName(),
                                            desc.getFontBBox(),
                                            // desc.getAscender(),
                                            // desc.getDescender(),
                                            desc.getCapHeight(), desc.getFlags(),
                                            // new PDFRectangle(desc.getFontBBox()),
                                            desc.getItalicAngle(), desc.getStemV(), null);    // desc.getLang(),
            // null);//desc.getPanose());
        }
        else {
            // Create normal FontDescriptor
            font = new PDFFontDescriptor(++this.objectcount, desc.fontName(),
                                         desc.getAscender(),
                                         desc.getDescender(),
                                         desc.getCapHeight(),
                                         desc.getFlags(),
                                         new PDFRectangle(desc.getFontBBox()),
                                         desc.getStemV(),
                                         desc.getItalicAngle());
        }
        this.objects.add(font);

        // Check if the font is embeddable
        if (desc.isEmbeddable()) {
            PDFStream stream = desc.getFontFile(this.objectcount + 1);
            if (stream != null) {
                this.objectcount++;
                font.setFontFile(desc.getSubType(), stream);
                this.objects.add(stream);
            }
        }
        return font;
    }


    /**
     * make an Array object (ex. Widths array for a font)
     */
    public PDFArray makeArray(int[] values) {

        PDFArray array = new PDFArray(++this.objectcount, values);
        this.objects.add(array);
        return array;
    }


    public int addImage(FopImage img) {
        // check if already created
        String url = img.getURL();
        PDFXObject xObject = (PDFXObject)this.xObjectsMap.get(url);
        if (xObject != null)
            return xObject.getXNumber();
        // else, create a new one
        xObject = new PDFXObject(++this.objectcount, ++this.xObjectCount,
                                 img, this);
        this.objects.add(xObject);
        this.xObjects.add(xObject);
        this.xObjectsMap.put(url, xObject);
        return xObjectCount;
    }

    /**
     * make a /Page object
     *
     * @param resources resources object to use
     * @param contents stream object with content
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
        PDFPage page = new PDFPage(++this.objectcount, resources,
                                   pagewidth, pageheight);

        /* add it to the list of objects */
        this.objects.add(page);
        pages.addPage(page);
        return page;
    }

    public void addPage(PDFPage page) {
        /* add it to the list of objects */
        this.objects.add(page);
    }

    /**
     * make a link object
     *
     * @param rect   the clickable rectangle
     * @param destination  the destination file
     * @param linkType the link type
     * @return the PDFLink object created
     */
    public PDFLink makeLink(Rectangle rect, String destination,
                            int linkType) {

        PDFLink linkObject;
        PDFAction action;

        PDFLink link = new PDFLink(++this.objectcount, rect);
        this.objects.add(link);

        if (linkType == LinkSet.EXTERNAL) {
            // check destination
            if (destination.endsWith(".pdf")) {    // FileSpec
                PDFFileSpec fileSpec = new PDFFileSpec(++this.objectcount,
                                                       destination);
                this.objects.add(fileSpec);
                action = new PDFGoToRemote(++this.objectcount, fileSpec);
                this.objects.add(action);
                link.setAction(action);
            } else {                               // URI
                PDFUri uri = new PDFUri(destination);
                link.setAction(uri);
            }
        } else {                                   // linkType is internal
            String goToReference = getGoToReference(destination);
            PDFInternalLink internalLink = new PDFInternalLink(goToReference);
            link.setAction(internalLink);
        }
        return link;
    }

    private String getGoToReference(String destination) {
        String goToReference;
        if (idReferences.doesIDExist(destination)) {
            if (idReferences.doesGoToReferenceExist(destination)) {
                goToReference =
                    idReferences.getInternalLinkGoToReference(destination);
            } else {    // assign Internal Link GoTo object
                goToReference =
                    idReferences.createInternalLinkGoTo(destination,
                                                        ++this.objectcount);
                addTrailerObject(idReferences.getPDFGoTo(destination));
            }
        } else {        // id was not found, so create it

            //next line by lmckenzi@ca.ibm.com
            //solves when IDNode made before IDReferences.createID called
            //idReferences.createNewId(destination);
 
            idReferences.createUnvalidatedID(destination); 
            idReferences.addToIdValidationList(destination);
            goToReference = idReferences.createInternalLinkGoTo(destination,
                            ++this.objectcount);
            addTrailerObject(idReferences.getPDFGoTo(destination));
        }
        return goToReference;
    }

    public void addTrailerObject(PDFObject object) {
        this.trailerObjects.add(object);
    }

    class PendingLink {
        PDFLink link;
        String dest;
    }

    public PDFLink makeLinkCurrentPage(Rectangle rect, String dest) {
        PDFLink link = new PDFLink(++this.objectcount, rect);
        this.objects.add(link);
        PendingLink pl = new PendingLink();
        pl.link = link;
        pl.dest = dest;
        if(pendingLinks == null) {
            pendingLinks = new ArrayList();
        }
        pendingLinks.add(pl);

        return link;
    }

    public PDFLink makeLink(Rectangle rect, String page, String dest) {
        PDFLink link = new PDFLink(++this.objectcount, rect);
        this.objects.add(link);

        PDFGoTo gt = new PDFGoTo(++this.objectcount, page);
        gt.setDestination(dest);
        addTrailerObject(gt);
        PDFInternalLink internalLink = new PDFInternalLink(gt.referencePDF());
        link.setAction(internalLink);

        return link;
    }

    /**
      Ensure there is room in the locations xref for the number of
      objects that have been created.
     */
    private void prepareLocations() {
        while(location.size() < objectcount)
            location.add(locationPlaceholder);
    }

    /**
     * make a stream object
     *
     * @return the stream object created
     */
    public PDFStream makeStream() {

        /*
         * create a PDFStream with the next object number and add it
         *
         * to the list of objects
         */
        PDFStream obj = new PDFStream(++this.objectcount);
        obj.addDefaultFilters();

        this.objects.add(obj);
        return obj;
    }


    /**
     * make an annotation list object
     *
     * @return the annotation list object created
     */
    public PDFAnnotList makeAnnotList() {

        /*
         * create a PDFAnnotList with the next object number and add it
         * to the list of objects
         */
        PDFAnnotList obj = new PDFAnnotList(++this.objectcount);
        this.objects.add(obj);
        return obj;
    }

    /**
     * Get the root Outlines object. This method does not write
     * the outline to the PDF document, it simply creates a
     * reference for later.
     */
    public PDFOutline getOutlineRoot() {
        if(outlineRoot != null)
            return outlineRoot;

        outlineRoot = new PDFOutline(++this.objectcount, null, null);
        addTrailerObject(outlineRoot);
        root.setRootOutline(outlineRoot);
        return outlineRoot;
    }

    /**
     * Make an outline object and add it to the given outline
     * @param parent parent PDFOutline object
     * @param label the title for the new outline object
     * @param destination the reference string for the action to go to
     */
    public PDFOutline makeOutline(PDFOutline parent, String label,
                                  String destination) {
        String goToRef = getGoToReference(destination);

        PDFOutline obj = new PDFOutline(++this.objectcount, label, goToRef);
        //log.debug("created new outline object");

        if (parent != null) {
            parent.addOutline(obj);
        }
        this.objects.add(obj);
        return obj;

    }

    /**
     * get the /Resources object for the document
     *
     * @return the /Resources object
     */
    public PDFResources getResources() {
        return this.resources;
    }

    /**
     * write the entire document out
     *
     * @param writer the OutputStream to output the document to
     */
    public void output(OutputStream stream) throws IOException {

        prepareLocations();

        for (int count = 0; count < this.objects.size(); count++) {
            /* retrieve the object with the current number */
            PDFObject object = (PDFObject)this.objects.get(count);

            /*
             * add the position of this object to the list of object
             * locations
             */
            location.set(object.getNumber() - 1, 
                         new Integer(this.position));

            /*
             * output the object and increment the character position
             * by the object's length
             */
            this.position += object.output(stream);
        }

        this.objects.clear();
    }

    /**
     * write the PDF header <P>
     *
     * This method must be called prior to formatting
     * and outputting AreaTrees.
     *
     * @param stream the OutputStream to write the header to
     * @return the number of bytes written
     */
    public void outputHeader(OutputStream stream)
    throws IOException {
        this.position=0;

        byte[] pdf = ("%PDF-" + this.pdfVersion + "\n").getBytes();
        stream.write(pdf);
        this.position += pdf.length;

        // output a binary comment as recommended by the PDF spec (3.4.1)
        byte[] bin = {
            (byte)'%', (byte)0xAA, (byte)0xAB, (byte)0xAC, (byte)0xAD,
            (byte)'\n'
        };
        stream.write(bin);
        this.position += bin.length;

        this.resources.setXObjects(xObjects);
    }

    /**
     * write the trailer
     *
     * @param stream the OutputStream to write the trailer to
     */
    public void outputTrailer(OutputStream stream)
    throws IOException {
        output(stream);
        for(int count = 0; count < trailerObjects.size(); count++) {
            PDFObject o = (PDFObject) trailerObjects.get(count);
            this.location.set(o.getNumber() - 1, 
                              new Integer(this.position));
            this.position += o.output(stream);
        }
        /* output the xref table and increment the character position
          by the table's length */
        this.position += outputXref(stream);

        /* construct the trailer */
        String pdf =
            "trailer\n" +
            "<<\n" +
            "/Size " + (this.objectcount + 1) + "\n" +
            "/Root " + this.root.number + " " + this.root.generation + " R\n" +
            "/Info " + this.info.number + " " + this.info.generation + " R\n" +
            ">>\n" +
            "startxref\n" +
            this.xref + "\n" +
            "%%EOF\n";

        /* write the trailer */
        stream.write(pdf.getBytes());
    }

    /**
     * write the xref table
     *
     * @param stream the OutputStream to write the xref table to
     * @return the number of characters written
     */
    private int outputXref(OutputStream stream) throws IOException {

        /* remember position of xref table */
        this.xref = this.position;

        /* construct initial part of xref */
        StringBuffer pdf = new StringBuffer("xref\n0 "
                                            + (this.objectcount + 1)
                                            + "\n0000000000 65535 f \n");

        for (int count = 0; count < this.location.size(); count++) {
            String x = this.location.get(count).toString();

            /* contruct xref entry for object */
            String padding = "0000000000";
            String loc = padding.substring(x.length()) + x;

            /* append to xref table */
            pdf = pdf.append(loc + " 00000 n \n");
        }

        /* write the xref table and return the character length */
        byte[] pdfBytes = pdf.toString().getBytes();
        stream.write(pdfBytes);
        return pdfBytes.length;
    }

    public void setIDReferences(IDReferences idReferences) {
        this.idReferences = idReferences;
    }

}
