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
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.java2d.color.NamedColorSpace;
import org.apache.xmlgraphics.xmp.Metadata;

import org.apache.fop.fonts.CIDFont;
import org.apache.fop.fonts.CodePointMapping;
import org.apache.fop.fonts.CustomFont;
import org.apache.fop.fonts.EmbeddingMode;
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
import org.apache.fop.fonts.truetype.OFFontLoader;
import org.apache.fop.fonts.truetype.OTFSubSetFile;
import org.apache.fop.fonts.truetype.TTFSubSetFile;
import org.apache.fop.fonts.type1.PFBData;
import org.apache.fop.fonts.type1.PFBParser;
import org.apache.fop.fonts.type1.Type1SubsetFile;

/**
 * This class provides method to create and register PDF objects.
 */
public class PDFFactory {

    /** Resolution of the User Space coordinate system (72dpi). */
    public static final int DEFAULT_PDF_RESOLUTION = 72;

    private PDFDocument document;

    private Log log = LogFactory.getLog(PDFFactory.class);

    private int subsetFontCounter = -1;
    private Map<String, PDFDPart> dparts = new HashMap<String, PDFDPart>();

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
        PDFRoot pdfRoot = new PDFRoot(document, pages);
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
        PDFPages pdfPages = new PDFPages(getDocument());
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
        PDFResources pdfResources = new PDFResources(getDocument());
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
     * make a type Exponential interpolation function
     * (for shading usually)
     * @param domain List objects of Double objects.
     * This is the domain of the function.
     * See page 264 of the PDF 1.3 Spec.
     * @param range List of Doubles that is the Range of the function.
     * See page 264 of the PDF 1.3 Spec.
     * @param cZero This is a vector of Double objects which defines the function result
     * when x=0.
     *
     * This attribute is optional.
     * It's described on page 268 of the PDF 1.3 spec.
     * @param cOne This is a vector of Double objects which defines the function result
     * when x=1.
     *
     * This attribute is optional.
     * It's described on page 268 of the PDF 1.3 spec.
     * @param interpolationExponentN This is the inerpolation exponent.
     *
     * This attribute is required.
     * PDF Spec page 268
     *
     * @return the PDF function that was created
     */
    public PDFFunction makeFunction(List domain, List range, float[] cZero, float[] cOne,
                                    double interpolationExponentN) {
        PDFFunction function = new PDFFunction(domain, range, cZero, cOne, interpolationExponentN);
        function = registerFunction(function);
        return function;
    }

    /**
     * Registers a function against the document
     * @param function The function to register
     */
    public PDFFunction registerFunction(PDFFunction function) {
        PDFFunction oldfunc = getDocument().findFunction(function);
        if (oldfunc == null) {
            getDocument().registerObject(function);
        } else {
            function = oldfunc;
        }
        return function;
    }

    /* ========================= shadings ================================== */

    /**
     * Registers a shading object against the document
     * @param res The PDF resource context
     * @param shading The shading object to be registered
     */
    public PDFShading registerShading(PDFResourceContext res, PDFShading shading) {
        PDFShading oldshad = getDocument().findShading(shading);
        if (oldshad == null) {
            getDocument().registerObject(shading);
        } else {
            shading = oldshad;
        }

        // add this shading to resources
        if (res != null) {
            res.addShading(shading);
        }
        return shading;
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
    public PDFPattern makePattern(PDFResourceContext res, int thePatternType,
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
            res.addPattern(pattern);
        }

        return (pattern);
    }

    public PDFPattern registerPattern(PDFResourceContext res, PDFPattern pattern) {
        PDFPattern oldpatt = getDocument().findPattern(pattern);
        if (oldpatt == null) {
            getDocument().registerObject(pattern);
        } else {
            pattern = oldpatt;
        }

        if (res != null) {
            res.addPattern(pattern);
        }
        return pattern;
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
        } else if ((index = targetLo.indexOf(".pdf#page=")) > 0) {
            // PDF file + page?
            String filename = target.substring(0, index + 4);
            int page = Integer.parseInt(target.substring(index + 10));
            return getGoToPDFAction(filename, null, page, newWindow);
        } else if ((index = targetLo.indexOf(".pdf#dest=")) > 0) {
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
                                  PDFReference actionRef, boolean showSubItems) {
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
                 : makeOutline(parent, label, new PDFReference(pdfAction.getAction()), showSubItems);
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
        return makeOutline(parent, label, new PDFReference(goToRef), showSubItems);
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
            if (descriptor instanceof RefPDFFont) {
                font.setObjectNumber(((RefPDFFont)descriptor).getRef().getObjectNumber());
                font.setDocument(getDocument());
                getDocument().addObject(font);
            } else {
                getDocument().registerObject(font);
            }

            if ((fonttype == FontType.TYPE0 || fonttype == FontType.CIDTYPE0)) {
                font.setEncoding(encoding);
                CIDFont cidMetrics;
                if (metrics instanceof LazyFont) {
                    cidMetrics = (CIDFont)((LazyFont) metrics).getRealFont();
                } else {
                    cidMetrics = (CIDFont)metrics;
                }
                PDFCIDSystemInfo sysInfo = new PDFCIDSystemInfo(cidMetrics.getRegistry(),
                        cidMetrics.getOrdering(), cidMetrics.getSupplement());
                sysInfo.setDocument(document);
                assert pdfdesc instanceof PDFCIDFontDescriptor;
                PDFCIDFont cidFont = new PDFCIDFont(subsetFontName, cidMetrics.getCIDType(),
                        cidMetrics.getDefaultWidth(), getFontWidths(cidMetrics), sysInfo,
                        (PDFCIDFontDescriptor) pdfdesc);
                getDocument().registerObject(cidFont);

                PDFCMap cmap;
                if (cidMetrics instanceof MultiByteFont && ((MultiByteFont) cidMetrics).getCmapStream() != null) {
                    cmap = new PDFCMap("fop-ucs-H", null);
                    try {
                        cmap.setData(IOUtils.toByteArray(((MultiByteFont) cidMetrics).getCmapStream()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    cmap = new PDFToUnicodeCMap(cidMetrics.getCIDSet().getChars(), "fop-ucs-H",
                        new PDFCIDSystemInfo("Adobe", "Identity", 0), false);
                }
                getDocument().registerObject(cmap);
                assert font instanceof PDFFontType0;
                ((PDFFontType0)font).setCMAP(cmap);
                ((PDFFontType0)font).setDescendantFonts(cidFont);
            } else {
                assert font instanceof PDFFontNonBase14;
                PDFFontNonBase14 nonBase14 = (PDFFontNonBase14)font;
                nonBase14.setDescriptor(pdfdesc);

                SingleByteFont singleByteFont;
                if (metrics instanceof LazyFont) {
                    singleByteFont = (SingleByteFont)((LazyFont)metrics).getRealFont();
                } else {
                    singleByteFont = (SingleByteFont)metrics;
                }

                int firstChar = 0;
                int lastChar = 0;
                boolean defaultChars = false;
                if (singleByteFont.getEmbeddingMode() == EmbeddingMode.SUBSET) {
                    Map<Integer, Integer> usedGlyphs = singleByteFont.getUsedGlyphs();
                    if (fonttype == FontType.TYPE1 && usedGlyphs.size() > 0) {
                        SortedSet<Integer> keys = new TreeSet<Integer>(usedGlyphs.keySet());
                        keys.remove(0);
                        if (keys.size() > 0) {
                            firstChar = keys.first();
                            lastChar = keys.last();
                            int[] newWidths = new int[(lastChar - firstChar) + 1];
                            for (int i = firstChar; i < lastChar + 1; i++) {
                                if (usedGlyphs.get(i) != null) {
                                    if (i - singleByteFont.getFirstChar() < metrics.getWidths().length) {
                                        newWidths[i - firstChar] = metrics.getWidths()[i
                                        - singleByteFont.getFirstChar()];
                                    } else {
                                        defaultChars = true;
                                        break;
                                    }
                                } else {
                                    newWidths[i - firstChar] = 0;
                                }
                            }
                            nonBase14.setWidthMetrics(firstChar,
                                    lastChar,
                                    new PDFArray(null, newWidths));
                        }
                    } else {
                        defaultChars = true;
                    }
                } else {
                    defaultChars = true;
                }

                if (defaultChars) {
                    firstChar = singleByteFont.getFirstChar();
                    lastChar = singleByteFont.getLastChar();
                    nonBase14.setWidthMetrics(firstChar,
                            lastChar,
                            new PDFArray(null, metrics.getWidths()));
                }

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
                } else if (mapping.getName().equals("FOPPDFEncoding")) {
                    String[] charNameMap = mapping.getCharNameMap();
                    char[] intmap = mapping.getUnicodeCharMap();
                    PDFArray differences = new PDFArray();
                    int len = intmap.length;
                    if (charNameMap.length < len) {
                        len = charNameMap.length;
                    }
                    int last = 0;
                    for (int i = 0; i < len; i++) {
                        if (intmap[i] - 1 != last) {
                            differences.add(intmap[i]);
                        }
                        last = intmap[i];
                        differences.add(new PDFName(charNameMap[i]));
                    }
                    PDFEncoding pdfEncoding = new PDFEncoding(singleByteFont.getEncodingName());
                    getDocument().registerObject(pdfEncoding);
                    pdfEncoding.setDifferences(differences);
                    font.setEncoding(pdfEncoding);
                    if (mapping.getUnicodeCharMap() != null) {
                        generateToUnicodeCmap(nonBase14, mapping);
                    }
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
     * @param fontName ...
     * @return the PDF Encoding dictionary (or a String with the predefined encoding)
     */
    public Object createPDFEncoding(SingleByteEncoding encoding, String fontName) {
        return PDFEncoding.createPDFEncoding(encoding, fontName);
    }

    private PDFWArray getFontWidths(CIDFont cidFont) {
        // Create widths for reencoded chars
        PDFWArray warray = new PDFWArray();
        if (cidFont instanceof MultiByteFont && ((MultiByteFont)cidFont).getWidthsMap() != null) {
            Map<Integer, Integer> map = ((MultiByteFont)cidFont).getWidthsMap();
            for (Map.Entry<Integer, Integer> cid : map.entrySet()) {
                warray.addEntry(cid.getKey(), new int[] {cid.getValue()});
            }
//            List<Integer> l = new ArrayList<Integer>(map.keySet());
//            for (int i=0; i<map.size(); i++) {
//                int cid = l.get(i);
//                List<Integer> cids = new ArrayList<Integer>();
//                cids.add(map.get(cid));
//                while (i<map.size()-1 && l.get(i) + 1 == l.get(i + 1)) {
//                    cids.add(map.get(l.get(i + 1)));
//                    i++;
//                }
//                int[] cidsints = new int[cids.size()];
//                for (int j=0; j<cids.size(); j++) {
//                    cidsints[j] = cids.get(j);
//                }
//                warray.addEntry(cid, cidsints);
//            }
        } else {
            int[] widths = cidFont.getCIDSet().getWidths();
            warray.addEntry(0, widths);
        }
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

        if (desc.getFontType() == FontType.TYPE0 || desc.getFontType() == FontType.CIDTYPE0) {
            // CID Font
            descriptor = new PDFCIDFontDescriptor(fontPrefix + desc.getEmbedFontName(),
                                            desc.getFontBBox(),
                                            desc.getCapHeight(),
                                            desc.getFlags(),
                                            desc.getItalicAngle(),
                                            desc.getStemV(), null);
        } else {
            // Create normal FontDescriptor
            descriptor = new PDFFontDescriptor(fontPrefix + desc.getEmbedFontName(),
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
            AbstractPDFStream stream = makeFontFile(desc, fontPrefix);
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
        BitSet cidSet = cidFont.getCIDSet().getGlyphIndices();
        PDFStream pdfStream = makeStream(null, true);
        ByteArrayOutputStream baout = new ByteArrayOutputStream(cidSet.length() / 8 + 1);
        int value = 0;
        for (int i = 0, c = cidSet.length(); i < c; i++) {
            int shift = i % 8;
            boolean b = cidSet.get(i);
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
            pdfStream.setData(baout.toByteArray());
            descriptor.setCIDSet(pdfStream);
        } catch (IOException ioe) {
            log.error(
                    "Failed to write CIDSet [" + cidFont + "] "
                    + cidFont.getEmbedFontName(), ioe);
        } finally {
            IOUtils.closeQuietly(baout);
        }
    }

    /**
     * Embeds a font.
     * @param desc FontDescriptor of the font.
     * @return PDFStream The embedded font file
     */
    public AbstractPDFStream makeFontFile(FontDescriptor desc, String fontPrefix) {
        if (desc.getFontType() == FontType.OTHER) {
            throw new IllegalArgumentException("Trying to embed unsupported font type: "
                                                + desc.getFontType());
        }

        CustomFont font = getCustomFont(desc);

        InputStream in = null;
        try {
            in = font.getInputStream();
            if (in == null) {
                return null;
            }
            AbstractPDFStream embeddedFont = null;
            if (desc.getFontType() == FontType.TYPE0) {
                MultiByteFont mbfont = (MultiByteFont) font;
                FontFileReader reader = new FontFileReader(in);
                byte[] fontBytes;
                String header = OFFontLoader.readHeader(reader);
                boolean isCFF = mbfont.isOTFFile();
                if (font.getEmbeddingMode() == EmbeddingMode.FULL) {
                    fontBytes = reader.getAllBytes();
                    if (isCFF) {
                        //Ensure version 1.6 for full OTF CFF embedding
                        document.setPDFVersion(Version.V1_6);
                    }
                } else {
                    fontBytes = getFontSubsetBytes(reader, mbfont, header, fontPrefix, desc,
                            isCFF);
                }
                embeddedFont = getFontStream(font, fontBytes, isCFF);
            } else if (desc.getFontType() == FontType.TYPE1) {
                if (font.getEmbeddingMode() != EmbeddingMode.SUBSET) {
                    embeddedFont = fullyEmbedType1Font(in);
                } else {
                    assert font instanceof SingleByteFont;
                    SingleByteFont sbfont = (SingleByteFont)font;
                    Type1SubsetFile pfbFile = new Type1SubsetFile();
                    byte[] subsetData = pfbFile.createSubset(in, sbfont);
                    InputStream subsetStream = new ByteArrayInputStream(subsetData);
                    PFBParser parser = new PFBParser();
                    PFBData pfb = parser.parsePFB(subsetStream);
                    embeddedFont = new PDFT1Stream();
                    ((PDFT1Stream) embeddedFont).setData(pfb);
                }
            } else if (desc.getFontType() == FontType.TYPE1C) {
                byte[] file = IOUtils.toByteArray(in);
                PDFCFFStream embeddedFont2 = new PDFCFFStream("Type1C");
                embeddedFont2.setData(file);
                return embeddedFont2;
            } else if (desc.getFontType() == FontType.CIDTYPE0) {
                byte[] file = IOUtils.toByteArray(in);
                PDFCFFStream embeddedFont2 = new PDFCFFStream("CIDFontType0C");
                embeddedFont2.setData(file);
                return embeddedFont2;
            } else {
                byte[] file = IOUtils.toByteArray(in);
                embeddedFont = new PDFTTFStream(file.length);
                ((PDFTTFStream) embeddedFont).setData(file, file.length);
            }

            /*
            embeddedFont.getFilterList().addFilter("flate");
            if (getDocument().isEncryptionActive()) {
                getDocument().applyEncryption(embeddedFont);
            } else {
                embeddedFont.getFilterList().addFilter("ascii-85");
            }*/

            return embeddedFont;
        } catch (IOException ioe) {
            log.error("Failed to embed font [" + desc + "] " + desc.getEmbedFontName(), ioe);
            return null;
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    private AbstractPDFStream fullyEmbedType1Font(InputStream in) throws IOException {
        PFBParser parser = new PFBParser();
        PFBData pfb = parser.parsePFB(in);
        AbstractPDFStream embeddedFont = new PDFT1Stream();
        ((PDFT1Stream) embeddedFont).setData(pfb);
        return embeddedFont;
    }

    private byte[] getFontSubsetBytes(FontFileReader reader, MultiByteFont mbfont, String header,
            String fontPrefix, FontDescriptor desc, boolean isCFF) throws IOException {
        if (isCFF) {
            OTFSubSetFile otfFile = new OTFSubSetFile();
            otfFile.readFont(reader, fontPrefix + desc.getEmbedFontName(), header, mbfont);
            return otfFile.getFontSubset();
        } else {
            TTFSubSetFile otfFile = new TTFSubSetFile();
            otfFile.readFont(reader, mbfont.getTTCName(), header, mbfont.getUsedGlyphs());
            return otfFile.getFontSubset();
        }
    }

    private AbstractPDFStream getFontStream(CustomFont font, byte[] fontBytes, boolean isCFF)
            throws IOException {
        AbstractPDFStream embeddedFont;
        if (isCFF) {
            embeddedFont = new PDFCFFStreamType0C(font.getEmbeddingMode() == EmbeddingMode.FULL);
            ((PDFCFFStreamType0C) embeddedFont).setData(fontBytes, fontBytes.length);
        } else {
            embeddedFont = new PDFTTFStream(fontBytes.length);
            ((PDFTTFStream) embeddedFont).setData(fontBytes, fontBytes.length);
        }
        return embeddedFont;
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
        List domain = Arrays.asList(new Double[] {zero, one});
        List range = Arrays.asList(new Double[] {zero, one, zero, one, zero, one});
        float[] cZero = new float[] {1f, 1f, 1f};
        float[] cOne = ncs.getRGBColor().getColorComponents(null);
        PDFFunction tintFunction = makeFunction(domain, range, cZero, cOne, 1.0d);
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

    public PDFLayer makeLayer(String id) {
        PDFLayer layer = new PDFLayer(id);
        getDocument().registerObject(layer);
        return layer;
    }

    public PDFSetOCGStateAction makeSetOCGStateAction(String id) {
        PDFSetOCGStateAction action = new PDFSetOCGStateAction(id);
        getDocument().registerObject(action);
        return action;
    }

    public PDFTransitionAction makeTransitionAction(String id) {
        PDFTransitionAction action = new PDFTransitionAction(id);
        getDocument().registerObject(action);
        return action;
    }

    public PDFNavigator makeNavigator(String id) {
        PDFNavigator navigator = new PDFNavigator(id);
        getDocument().registerObject(navigator);
        return navigator;
    }

    public void makeDPart(PDFPage page, String pageMasterName) {
        PDFDPartRoot root = getDocument().getRoot().getDPartRoot();
        PDFDPart dPart;
        if (dparts.containsKey(pageMasterName)) {
            dPart = dparts.get(pageMasterName);
        } else {
            dPart = new PDFDPart(root.dpart);
            root.add(dPart);
            getDocument().registerTrailerObject(dPart);
            dparts.put(pageMasterName, dPart);
        }
        dPart.addPage(page);
        page.put("DPart", dPart);
    }

    public PDFDPartRoot makeDPartRoot() {
        PDFDPartRoot pdfdPartRoot = new PDFDPartRoot(getDocument());
        getDocument().registerTrailerObject(pdfdPartRoot);
        return pdfdPartRoot;
    }
}
