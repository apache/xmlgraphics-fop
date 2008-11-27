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

package org.apache.fop.afp;

import java.awt.Color;
import java.awt.Point;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.afp.fonts.AFPFont;
import org.apache.fop.afp.fonts.AFPFontAttributes;
import org.apache.fop.afp.modca.AbstractPageObject;
import org.apache.fop.afp.modca.Document;
import org.apache.fop.afp.modca.InterchangeSet;
import org.apache.fop.afp.modca.Overlay;
import org.apache.fop.afp.modca.PageGroup;
import org.apache.fop.afp.modca.PageObject;
import org.apache.fop.afp.modca.ResourceGroup;
import org.apache.fop.afp.modca.TagLogicalElementBean;
import org.apache.fop.afp.modca.triplets.FullyQualifiedNameTriplet;

/**
 * A data stream is a continuous ordered stream of data elements and objects
 * conforming to a given format. Application programs can generate data streams
 * destined for a presentation service, archive library, presentation device or
 * another application program. The strategic presentation data stream
 * architectures used is Mixed Object Document Content Architecture (MO:DCA).
 *
 * The MO:DCA architecture defines the data stream used by applications to
 * describe documents and object envelopes for interchange with other
 * applications and application services. Documents defined in the MO:DCA format
 * may be archived in a database, then later retrieved, viewed, annotated and
 * printed in local or distributed systems environments. Presentation fidelity
 * is accommodated by including resource objects in the documents that reference
 * them.
 */
public class DataStream {

    /** Static logging instance */
    protected static final Log log = LogFactory.getLog("org.apache.xmlgraphics.afp");

    /** Boolean completion indicator */
    private boolean complete = false;

    /** The AFP document object */
    private Document document = null;

    /** The current page group object */
    private PageGroup currentPageGroup = null;

    /** The current page object */
    private PageObject currentPageObject = null;

    /** The current overlay object */
    private Overlay currentOverlay = null;

    /** The current page */
    private AbstractPageObject currentPage = null;

    /** The MO:DCA interchange set in use (default to MO:DCA-P IS/2 set) */
    private InterchangeSet interchangeSet
        = InterchangeSet.valueOf(InterchangeSet.MODCA_PRESENTATION_INTERCHANGE_SET_2);

    private final Factory factory;

    private OutputStream outputStream;

    /** the afp painting state */
    private final AFPPaintingState paintingState;

    /**
     * Default constructor for the AFPDocumentStream.
     *
     * @param factory the resource factory
     * @param paintingState the AFP painting state
     * @param outputStream the outputstream to write to
     */
    public DataStream(Factory factory, AFPPaintingState paintingState, OutputStream outputStream) {
        this.paintingState = paintingState;
        this.factory = factory;
        this.outputStream = outputStream;
    }

    /**
     * Returns the outputstream
     *
     * @return the outputstream
     */
    public OutputStream getOutputStream() {
        return this.outputStream;
    }

    /**
     * Returns the document object
     *
     * @return the document object
     */
    private Document getDocument() {
        return this.document;
    }

    /**
     * Returns the current page
     *
     * @return the current page
     */
    public AbstractPageObject getCurrentPage() {
        return this.currentPage;
    }

    /**
     * The document is started by invoking this method which creates an instance
     * of the AFP Document object.
     *
     * @param name
     *            the name of this document.
     */
    public void setDocumentName(String name) {
        if (name != null) {
            getDocument().setFullyQualifiedName(
                    FullyQualifiedNameTriplet.TYPE_BEGIN_DOCUMENT_REF,
                    FullyQualifiedNameTriplet.FORMAT_CHARSTR, name);
        }
    }

    /**
     * Helper method to mark the end of the current document.
     *
     * @throws IOException thrown if an I/O exception of some sort has occurred
     */
    public void endDocument() throws IOException {
        if (complete) {
            String msg = "Invalid state - document already ended.";
            log.warn("endDocument():: " + msg);
            throw new IllegalStateException(msg);
        }

        if (currentPageObject != null) {
            // End the current page if necessary
            endPage();
        }

        if (currentPageGroup != null) {
            // End the current page group if necessary
            endPageGroup();
        }

        // Write out document
        if (document != null) {
            document.endDocument();
            document.writeToStream(this.outputStream);
        }

        this.outputStream.flush();

        this.complete = true;

        this.document = null;

        this.outputStream = null;
    }

    /**
     * Start a new page. When processing has finished on the current page, the
     * {@link #endPage()}method must be invoked to mark the page ending.
     *
     * @param pageWidth
     *            the width of the page
     * @param pageHeight
     *            the height of the page
     * @param pageRotation
     *            the rotation of the page
     * @param pageWidthRes
     *            the width resolution of the page
     * @param pageHeightRes
     *            the height resolution of the page
     */
    public void startPage(int pageWidth, int pageHeight, int pageRotation,
            int pageWidthRes, int pageHeightRes) {
        currentPageObject = factory.createPage(pageWidth, pageHeight,
                pageRotation, pageWidthRes, pageHeightRes);
        currentPage = currentPageObject;
        currentOverlay = null;
    }

    /**
     * Start a new overlay. When processing has finished on the current overlay,
     * the {@link #endOverlay()}method must be invoked to mark the overlay
     * ending.
     *
     * @param x
     *            the x position of the overlay on the page
     * @param y
     *            the y position of the overlay on the page
     * @param width
     *            the width of the overlay
     * @param height
     *            the height of the overlay
     * @param widthRes
     *            the width resolution of the overlay
     * @param heightRes
     *            the height resolution of the overlay
     * @param overlayRotation
     *            the rotation of the overlay
     */
    public void startOverlay(int x, int y, int width, int height, int widthRes,
            int heightRes, int overlayRotation) {
        this.currentOverlay = factory.createOverlay(
                width, height, widthRes, heightRes, overlayRotation);

        String overlayName = currentOverlay.getName();
        currentPageObject.createIncludePageOverlay(overlayName, x, y, 0);
        currentPage = currentOverlay;
    }

    /**
     * Helper method to mark the end of the current overlay.
     *
     * @throws IOException thrown if an I/O exception of some sort has occurred
     */
    public void endOverlay() throws IOException {
        if (currentOverlay != null) {
            currentOverlay.endPage();
            currentOverlay = null;
            currentPage = currentPageObject;
        }
    }

    /**
     * Helper method to save the current page.
     *
     * @return current page object that was saved
     */
    public PageObject savePage() {
        PageObject pageObject = currentPageObject;
        if (currentPageGroup != null) {
            currentPageGroup.addPage(currentPageObject);
        } else {
            document.addPage(currentPageObject);
        }
        currentPageObject = null;
        currentPage = null;
        return pageObject;
    }

    /**
     * Helper method to restore the current page.
     *
     * @param pageObject
     *            page object
     */
    public void restorePage(PageObject pageObject) {
        currentPageObject = pageObject;
        currentPage = pageObject;
    }

    /**
     * Helper method to mark the end of the current page.
     *
     * @throws IOException thrown if an I/O exception of some sort has occurred
     */
    public void endPage() throws IOException {
        if (currentPageObject != null) {
            currentPageObject.endPage();
            if (currentPageGroup != null) {
                currentPageGroup.addPage(currentPageObject);
                currentPageGroup.writeToStream(this.outputStream);
            } else {
                document.addPage(currentPageObject);
                document.writeToStream(this.outputStream);
            }
            currentPageObject = null;
            currentPage = null;
        }
    }

    /**
     * Creates the given page fonts in the current page
     *
     * @param pageFonts
     *            a collection of AFP font attributes
     */
    public void addFontsToCurrentPage(Map pageFonts) {
        Iterator iter = pageFonts.values().iterator();
        while (iter.hasNext()) {
            AFPFontAttributes afpFontAttributes = (AFPFontAttributes) iter
                    .next();
            createFont(afpFontAttributes.getFontReference(), afpFontAttributes
                    .getFont(), afpFontAttributes.getPointSize());
        }
    }

    /**
     * Helper method to create a map coded font object on the current page, this
     * method delegates the construction of the map coded font object to the
     * active environment group on the current page.
     *
     * @param fontReference
     *            the font number used as the resource identifier
     * @param font
     *            the font
     * @param size
     *            the point size of the font
     */
    public void createFont(int fontReference, AFPFont font, int size) {
        currentPage.createFont(fontReference, font, size);
    }

    /**
     * Returns a point on the current page
     *
     * @param x the X-coordinate
     * @param y the Y-coordinate
     * @return a point on the current page
     */
    private Point getPoint(int x, int y) {
        Point p = new Point();
        int rotation = paintingState.getRotation();
        switch (rotation) {
        case 90:
            p.x = y;
            p.y = currentPage.getWidth() - x;
            break;
        case 180:
            p.x = currentPage.getWidth() - x;
            p.y = currentPage.getHeight() - y;
            break;
        case 270:
            p.x = currentPage.getHeight() - y;
            p.y = x;
            break;
        default:
            p.x = x;
            p.y = y;
            break;
        }
        return p;
    }

    /**
     * Helper method to create text on the current page, this method delegates
     * to the current presentation text object in order to construct the text.
     *
     * @param textDataInfo
     *            the afp text data
     * @throws UnsupportedEncodingException thrown if character encoding is not supported
     */
    public void createText(AFPTextDataInfo textDataInfo) throws UnsupportedEncodingException {
        int rotation = paintingState.getRotation();
        if (rotation != 0) {
            textDataInfo.setRotation(rotation);
            Point p = getPoint(textDataInfo.getX(), textDataInfo.getY());
            textDataInfo.setX(p.x);
            textDataInfo.setY(p.y);
        }
        currentPage.createText(textDataInfo);
    }

    /**
     * Method to create a line on the current page.
     *
     * @param lineDataInfo the line data information.
     */
    public void createLine(AFPLineDataInfo lineDataInfo) {
        currentPage.createLine(lineDataInfo);
    }

    /**
     * This method will create shading on the page using the specified
     * coordinates (the shading contrast is controlled via the red, green, blue
     * parameters, by converting this to grey scale).
     *
     * @param x
     *            the x coordinate of the shading
     * @param y
     *            the y coordinate of the shading
     * @param w
     *            the width of the shaded area
     * @param h
     *            the height of the shaded area
     * @param col
     *            the shading color
     */
    public void createShading(int x, int y, int w, int h, Color col) {
        currentPageObject.createShading(x, y, w, h, col.getRed(), col.getGreen(), col.getBlue());
    }

    /**
     * Helper method which allows creation of the MPO object, via the AEG. And
     * the IPO via the Page. (See actual object for descriptions.)
     *
     * @param name
     *            the name of the static overlay
     */
    public void createIncludePageOverlay(String name) {
        currentPageObject.createIncludePageOverlay(name, 0, 0, paintingState.getRotation());
        currentPageObject.getActiveEnvironmentGroup().createOverlay(name);
    }

    /**
     * Helper method which allows creation of the IMM object.
     *
     * @param name
     *            the name of the medium map
     */
    public void createInvokeMediumMap(String name) {
        currentPageGroup.createInvokeMediumMap(name);
    }

    /**
     * Creates an IncludePageSegment on the current page.
     *
     * @param name
     *            the name of the include page segment
     * @param x
     *            the x coordinate for the overlay
     * @param y
     *            the y coordinate for the overlay
     */
    public void createIncludePageSegment(String name, int x, int y) {
        int xOrigin;
        int yOrigin;
        int orientation = paintingState.getRotation();
        switch (orientation) {
        case 90:
            xOrigin = currentPage.getWidth() - y;
            yOrigin = x;
            break;
        case 180:
            xOrigin = currentPage.getWidth() - x;
            yOrigin = currentPage.getHeight() - y;
            break;
        case 270:
            xOrigin = y;
            yOrigin = currentPage.getHeight() - x;
            break;
        default:
            xOrigin = x;
            yOrigin = y;
            break;
        }
        currentPage.createIncludePageSegment(name, xOrigin, yOrigin);
    }

    /**
     * Creates a TagLogicalElement on the current page.
     *
     * @param attributes
     *            the array of key value pairs.
     */
    public void createPageTagLogicalElement(TagLogicalElementBean[] attributes) {
        for (int i = 0; i < attributes.length; i++) {
            String name = attributes[i].getKey();
            String value = attributes[i].getValue();
            currentPage.createTagLogicalElement(name, value);
        }
    }

    /**
     * Creates a TagLogicalElement on the current page group.
     *
     * @param attributes
     *            the array of key value pairs.
     */
    public void createPageGroupTagLogicalElement(TagLogicalElementBean[] attributes) {
        for (int i = 0; i < attributes.length; i++) {
            String name = attributes[i].getKey();
            String value = attributes[i].getValue();
            currentPageGroup.createTagLogicalElement(name, value);
        }
    }

    /**
     * Creates a TagLogicalElement on the current page or page group
     *
     * @param name
     *            The tag name
     * @param value
     *            The tag value
     */
    public void createTagLogicalElement(String name, String value) {
        if (currentPageGroup != null) {
            currentPageGroup.createTagLogicalElement(name, value);
        } else {
            currentPage.createTagLogicalElement(name, value);
        }
    }

    /**
     * Creates a NoOperation item
     *
     * @param content
     *            byte data
     */
    public void createNoOperation(String content) {
        currentPage.createNoOperation(content);
    }

    /**
     * Returns the current page group
     *
     * @return the current page group
     */
    public PageGroup getCurrentPageGroup() {
        return this.currentPageGroup;
    }

    /**
     * Start a new document.
     *
     * @throws IOException thrown if an I/O exception of some sort has occurred
     */
    public void startDocument() throws IOException {
        this.document = factory.createDocument();
        document.writeToStream(this.outputStream);
    }

    /**
     * Start a new page group. When processing has finished on the current page
     * group the {@link #endPageGroup()}method must be invoked to mark the page
     * group ending.
     *
     * @throws IOException thrown if an I/O exception of some sort has occurred
     */
    public void startPageGroup() throws IOException {
        endPageGroup();
        this.currentPageGroup = factory.createPageGroup();
    }

    /**
     * Helper method to mark the end of the page group.
     *
     * @throws IOException thrown if an I/O exception of some sort has occurred
     */
    public void endPageGroup() throws IOException {
        if (currentPageGroup != null) {
            currentPageGroup.endPageGroup();
            document.addPageGroup(currentPageGroup);
            document.writeToStream(outputStream);
            currentPageGroup = null;
        }
    }

    /**
     * Sets the MO:DCA interchange set to use
     *
     * @param interchangeSet the MO:DCA interchange set
     */
    public void setInterchangeSet(InterchangeSet interchangeSet) {
        this.interchangeSet = interchangeSet;
    }

    /**
     * Returns the MO:DCA interchange set in use
     *
     * @return the MO:DCA interchange set in use
     */
    public InterchangeSet getInterchangeSet() {
        return this.interchangeSet;
    }

    /**
     * Returns the resource group for a given resource info
     *
     * @param level a resource level
     * @return a resource group for the given resource info
     */
    public ResourceGroup getResourceGroup(AFPResourceLevel level) {
        ResourceGroup resourceGroup = null;
        if (level.isDocument()) {
            resourceGroup = document.getResourceGroup();
        } else if (level.isPageGroup()) {
            resourceGroup = currentPageGroup.getResourceGroup();
        } else if (level.isPage()) {
            resourceGroup = currentPageObject.getResourceGroup();
        }
        return resourceGroup;
    }

}
