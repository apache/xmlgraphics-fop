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

package org.apache.fop.render.afp.modca;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.render.afp.fonts.AFPFont;
import org.apache.fop.render.afp.tools.StringUtils;

/**
 * A data stream is a continuous ordered stream of data elements and objects
 * conforming to a given format. Application programs can generate data streams
 * destined for a presentation service, archive library, presentation device or
 * another application program. The strategic presentation data stream
 * architectures used is Mixed Object Document Content Architecture (MO:DCA�).
 *
 * The MO:DCA architecture defines the data stream used by applications to
 * describe documents and object envelopes for interchange with other
 * applications and application services. Documents defined in the MO:DCA format
 * may be archived in a database, then later retrieved, viewed, annotated and
 * printed in local or distributed systems environments. Presentation fidelity
 * is accommodated by including resource objects in the documents that reference
 * them.
 *
 */
public class AFPDataStream {

    /**
     * Static logging instance
     */
    protected static final Log log = LogFactory.getLog("org.apache.fop.render.afp.modca");

    /**
     * Boolean completion indicator
     */
    private boolean _complete = false;

    /**
     * The application producing the AFP document
     */
    private String _producer = null;

    /**
     * The AFP document object
     */
    private Document _document = null;

    /**
     * The current page group object
     */
    private PageGroup _currentPageGroup = null;

    /**
     * The current page object
     */
    private PageObject _currentPageObject = null;

    /**
     * The current overlay object
     */
    private Overlay _currentOverlay = null;

    /**
     * The current page
     */
    private AbstractPageObject _currentPage = null;

    /**
     * The page count
     */
    private int _pageCount = 0;

    /**
     * The page group count
     */
    private int _pageGroupCount = 0;

    /**
     * The overlay count
     */
    private int _ovlCount = 0;

    /**
     * The portrait rotation
     */
    private int _portraitRotation = 0;

    /**
     * The landscape rotation
     */
    private int _landscapeRotation = 270;

    /**
     * The x offset
     */
    private int _xOffset = 0;

    /**
     * The y offset
     */
    private int _yOffset = 0;

    /**
     * The rotation
     */
    private int _rotation;

    /**
     * The outputstream for the data stream
     */
    private OutputStream _outputStream = null;

    /**
     * Default constructor for the AFPDataStream.
     */
    public AFPDataStream() {
    }

    /**
     * The document is started by invoking this method which creates an instance
     * of the AFP Document object.
     */
    public void startDocument(OutputStream outputStream) {

        if (_document != null) {
            String msg = "Invalid state - document already started.";
            log.warn("startDocument():: " + msg);
            throw new IllegalStateException(msg);
        }

        _document = new Document();
        _outputStream = outputStream;

    }

    /**
     * The document is ended by invoking this method which creates an instance
     * of the AFP Document object and registers the start with a validation map
     * which ensures that methods are not invoked out of the correct sequence.
     */
    public void endDocument()
        throws IOException {

        if (_complete) {
            String msg = "Invalid state - document already ended.";
            log.warn("endDocument():: " + msg);
            throw new IllegalStateException(msg);
        }

        if (_currentPageObject != null) {
            // End the current page if necessary
            endPage();
        }

        if (_currentPageGroup != null) {
            // End the current page group if necessary
            endPageGroup();
        }

        _document.endDocument();
        _document.writeDataStream(_outputStream);
        _outputStream.flush();

        _complete = true;

        _document = null;

        _outputStream = null;
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
     */
    public void startPage(int pageWidth, int pageHeight, int pageRotation) {

        String pageName = "PGN"
            + StringUtils.lpad(String.valueOf(_pageCount++), '0', 5);

        _currentPageObject = new PageObject(pageName, pageWidth, pageHeight, pageRotation);
        _currentPage = _currentPageObject;
        _currentOverlay = null;
        setOffsets(0, 0, 0);
    }

    /**
     * Start a new overlay. When processing has finished on the current overlay, the
     * {@link #endOverlay()}method must be invoked to mark the overlay ending.
     *
     * @param overlayX
     *            the x position of the overlay on the page
     * @param overlayY
     *            the y position of the overlay on the page
     * @param overlayWidth
     *            the width of the overlay
     * @param overlayHeight
     *            the height of the overlay
     * @param overlayRotation
     *            the rotation of the overlay
     */
    public void startOverlay(int overlayX, int overlayY, int overlayWidth, int overlayHeight, int overlayRotation) {

        String overlayName = "OVL"
            + StringUtils.lpad(String.valueOf(_ovlCount++), '0', 5);

        _currentOverlay = new Overlay(overlayName, overlayWidth, overlayHeight, overlayRotation);
        _currentPageObject.addOverlay(_currentOverlay);
        _currentPageObject.createIncludePageOverlay(overlayName, overlayX, overlayY, 0);
        _currentPage = _currentOverlay;
        setOffsets(0, 0, 0);
    }

    /**
     * Helper method to mark the end of the current overlay.
     */
    public void endOverlay() {

        _currentOverlay.endPage();
        _currentOverlay = null;
        _currentPage = _currentPageObject;

    }

    /**
     * Helper method to save the current page.
     */
    public PageObject savePage() {

        PageObject pageObject = _currentPageObject;
        if (_currentPageGroup != null) {
            _currentPageGroup.addPage(_currentPageObject);
        } else {
            _document.addPage(_currentPageObject);
        }
        _currentPageObject = null;
        _currentPage = null;
        return pageObject;

    }

    /**
     * Helper method to restore the current page.
     */
    public void restorePage(PageObject pageObject) {

        _currentPageObject = pageObject;
        _currentPage = pageObject;

    }

    /**
     * Helper method to mark the end of the current page.
     */
    public void endPage()
        throws IOException {

        _currentPageObject.endPage();
        if (_currentPageGroup != null) {
            _currentPageGroup.addPage(_currentPageObject);
        } else {
            _document.addPage(_currentPageObject);
            _document.writeDataStream(_outputStream);
        }

        _currentPageObject = null;
        _currentPage = null;

    }

    /**
     * Sets the offsets to be used for element positioning
     *
     * @param xOffset
     *            the offset in the x direction
     * @param yOffset
     *            the offset in the y direction
     * @param rotation
     *            the rotation
     */
    public void setOffsets(int xOffset, int yOffset, int rotation) {
        _xOffset = xOffset;
        _yOffset = yOffset;
        _rotation = rotation;
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
    public void createFont(byte fontReference, AFPFont font, int size) {

        _currentPage.createFont(fontReference, font, size);

    }

    /**
     * Helper method to create text on the current page, this method delegates
     * to the current presentation text object in order to construct the text.
     *
     * @param fontNumber
     *            the font number used as the resource identifier
     * @param x
     *            the x coordinate of the text
     * @param y
     *            the y coordinate of the text
     * @param col
     *            the text color
     * @param vsci
     *            The variable space character increment.
     * @param ica
     *            The inter character adjustment.
     * @param data
     *            the text data to create
     */
    public void createText(int fontNumber, int x, int y, Color col, int vsci, int ica, byte[] data) {

        _currentPage.createText(fontNumber, x + _xOffset, y + _yOffset, _rotation, col, vsci, ica, data);

    }

    /**
     * Returns an ImageObject used to create an image in the datastream.
     *
     * @param x
     *            the x position of the image
     * @param y
     *            the y position of the image
     * @param w
     *            the width of the image
     * @param h
     *            the height of the image
     */
    public ImageObject getImageObject(int x, int y, int w, int h) {

        int xOrigin;
        int yOrigin;
        int width;
        int height;
        switch (_rotation) {
            case 90:
                xOrigin = _currentPage.getWidth() - y - _yOffset;
                yOrigin = x + _xOffset;
                width = h;
                height = w;
                break;
            case 180:
                xOrigin = _currentPage.getWidth() - x - _xOffset;
                yOrigin = _currentPage.getHeight() - y - _yOffset;
                width = w;
                height = h;
                break;
            case 270:
                xOrigin = y + _yOffset;
                yOrigin = _currentPage.getHeight() - x - _xOffset;
                width = h;
                height = w;
                break;
            default:
                xOrigin = x + _xOffset;
                yOrigin = y + _yOffset;
                width = w;
                height = h;
                break;
        }
        ImageObject io = _currentPage.getImageObject();
        io.setImageViewport(xOrigin, yOrigin, width, height, _rotation);
        return io;

    }

    /**
     * Method to create a line on the current page.
     *
     * @param x1
     *            the first x coordinate of the line
     * @param y1
     *            the first y coordinate of the line
     * @param x2
     *            the second x coordinate of the line
     * @param y2
     *            the second y coordinate of the line
     * @param thickness
     *            the thickness of the line
     * @param col
     *            The text color.
     */
    public void createLine(int x1, int y1, int x2, int y2, int thickness, Color col) {

        _currentPage.createLine(x1 + _xOffset, y1 + _yOffset, x2 + _xOffset, y2 + _yOffset, thickness, _rotation, col);

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
     * @param red
     *            the red value
     * @param green
     *            the green value
     * @param blue
     *            the blue value
     */
    public void createShading(int x, int y, int w, int h, int red, int green,
        int blue) {

        _currentPage.createShading(x + _xOffset, y + _xOffset, w, h, red, green, blue);

    }

    /**
     * Helper method which allows creation of the MPO object, via the AEG. And
     * the IPO via the Page. (See actual object for descriptions.)
     *
     * @param name
     *            the name of the static overlay
     */
    public void createIncludePageOverlay(String name) {

        _currentPageObject.createIncludePageOverlay(name, 0, 0, _rotation);
        ActiveEnvironmentGroup aeg = _currentPageObject.getActiveEnvironmentGroup();
        aeg.createOverlay(name);

    }

    /**
     * Helper method which allows creation of the IMM object.
     *
     * @param name
     *            the name of the medium map
     */
    public void createInvokeMediumMap(String name) {

        if (_currentPageGroup == null) {
            startPageGroup();
        }
        _currentPageGroup.createInvokeMediumMap(name);

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
        switch (_rotation) {
            case 90:
                xOrigin = _currentPage.getWidth() - y - _yOffset;
                yOrigin = x + _xOffset;
                break;
            case 180:
                xOrigin = _currentPage.getWidth() - x - _xOffset;
                yOrigin = _currentPage.getHeight() - y - _yOffset;
                break;
            case 270:
                xOrigin = y + _yOffset;
                yOrigin = _currentPage.getHeight() - x - _xOffset;
                break;
            default:
                xOrigin = x + _xOffset;
                yOrigin = y + _yOffset;
                break;
        }
        _currentPage.createIncludePageSegment(name, xOrigin, yOrigin);

    }

    /**
     * Creates a TagLogicalElement on the current page.
     *
     * @param attributes
     *            the array of key value pairs.
     */
    public void createPageTagLogicalElement(TagLogicalElementBean[] attributes) {

        for (int i = 0; i < attributes.length; i++) {
            String name = (String) attributes[i].getKey();
            String value = (String) attributes[i].getValue();
            _currentPage.createTagLogicalElement(name, value);
        }

    }

    /**
     * Creates a TagLogicalElement on the current page group.
     *
     * @param attributes
     *            the array of key value pairs.
     */
    public void createPageGroupTagLogicalElement(
        TagLogicalElementBean[] attributes) {

        for (int i = 0; i < attributes.length; i++) {
            String name = (String) attributes[i].getKey();
            String value = (String) attributes[i].getValue();
            _currentPageGroup.createTagLogicalElement(name, value);
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

        if (_currentPageGroup != null) {
            _currentPageGroup.createTagLogicalElement(name, value);
        } else {
            _currentPage.createTagLogicalElement(name, value);
        }

    }

    /**
     * Creates a NoOperation item
     * 
     * @param content byte data
     */
    public void createNoOperation(String content) {
        _currentPage.createNoOperation(content);
    }

    /**
     * Start a new page group. When processing has finished on the current page
     * group the {@link #endPageGroup()}method must be invoked to mark the page
     * group ending.
     *
     * @param name
     *            the name of the page group
     */
    public void startPageGroup() {

        String pageGroupName = "PGP"
            + StringUtils.lpad(String.valueOf(_pageCount++), '0', 5);

        _currentPageGroup = new PageGroup(pageGroupName);

    }

    /**
     * Helper method to mark the end of the page group.
     */
    public void endPageGroup()
        throws IOException {

        _currentPageGroup.endPageGroup();
        _document.addPageGroup(_currentPageGroup);
        _document.writeDataStream(_outputStream);
        _currentPageGroup = null;

    }

    /**
     * Sets the rotation to be used for portrait pages, valid values are 0
     * (default), 90, 180, 270.
     *
     * @param rotation
     *            The rotation in degrees.
     */
    public void setPortraitRotation(int rotation) {

        if (rotation == 0 || rotation == 90 || rotation == 180
            || rotation == 270) {
            _portraitRotation = rotation;
        } else {
            throw new IllegalArgumentException(
                "The portrait rotation must be one of the values 0, 90, 180, 270");
        }

    }

    /**
     * Sets the rotation to be used for landscape pages, valid values are 0, 90,
     * 180, 270 (default).
     *
     * @param rotation
     *            The rotation in degrees.
     */
    public void setLandscapeRotation(int rotation) {

        if (rotation == 0 || rotation == 90 || rotation == 180
            || rotation == 270) {
            _landscapeRotation = rotation;
        } else {
            throw new IllegalArgumentException(
                "The landscape rotation must be one of the values 0, 90, 180, 270");
        }

    }

}
