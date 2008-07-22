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
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.render.afp.AFPFontAttributes;
import org.apache.fop.render.afp.LineDataInfo;
import org.apache.fop.render.afp.TextDataInfo;
import org.apache.fop.render.afp.DataObjectInfo;
import org.apache.fop.render.afp.ExternalResourceGroupManager;
import org.apache.fop.render.afp.ObjectAreaInfo;
import org.apache.fop.render.afp.ResourceInfo;
import org.apache.fop.render.afp.ResourceLevel;
import org.apache.fop.render.afp.DataObjectCache;
import org.apache.fop.render.afp.fonts.AFPFont;
import org.apache.fop.render.afp.modca.triplets.FullyQualifiedNameTriplet;
import org.apache.fop.render.afp.tools.StringUtils;

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
public class AFPDataStream extends AbstractResourceGroupContainer {

    /** Static logging instance */
    protected static Log log = LogFactory.getLog("org.apache.fop.render.afp.modca");

    /** Boolean completion indicator */
    private boolean complete = false;

    /** The application producing the AFP document */
    // not used
    // private String producer = null;
    
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

    /** The page count */
    private int pageCount = 0;

    /** The page group count */
    private int pageGroupCount = 0;

    /** The overlay count */
    private int overlayCount = 0;

    /** The portrait rotation */
    private int portraitRotation = 0;

    /** The landscape rotation */
    private int landscapeRotation = 270;

    /** The rotation */
    private int orientation;

    /** The outputstream for the data stream */
    private OutputStream outputStream = null;

    /** Maintain a reference count of instream objects for referencing purposes */
    private int instreamObjectCount = 0;

    /** The MO:DCA interchange set in use (default to MO:DCA-P IS/2 set) */
    private InterchangeSet interchangeSet
        = InterchangeSet.valueOf(InterchangeSet.MODCA_PRESENTATION_INTERCHANGE_SET_2);

    private DataObjectCache cache = DataObjectCache.getInstance();

    /**
     * The external resource group manager
     */
    private ExternalResourceGroupManager externalResourceGroupManager = null;

    /**
     * Default constructor for the AFPDataStream.
     */
    public AFPDataStream() {
        this.document = new Document();
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
    protected AbstractPageObject getCurrentPage() {
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
     * Sets the OutputStream
     *
     * @param outputStream
     *            the AFP OutputStream
     */
    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    /** {@inheritDoc} */
    public void write() throws IOException {
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

        if (interchangeSet.supportsLevel2()) {
            // Write out any external resource groups
            getExternalResourceGroupManager().write();

            // Write out any print-file level resources
            if (hasResources()) {
                getResourceGroup().write(this.outputStream);
            }
        }

        // Write out document
        if (document != null) {
            document.endDocument();
            document.write(this.outputStream);
        }

        this.outputStream.flush();

        this.complete = true;

        this.document = null;

        this.outputStream = null;

        cache.clear();
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
        String pageName = "PGN"
                + StringUtils.lpad(String.valueOf(++pageCount), '0', 5);

        currentPageObject = new PageObject(pageName, pageWidth, pageHeight,
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
        String overlayName = "OVL"
                + StringUtils.lpad(String.valueOf(++overlayCount), '0', 5);

        DataObjectFactory factory = cache.getFactory();
        this.currentOverlay = factory.createOverlay(
                overlayName, width, height, widthRes, heightRes, overlayRotation);

        currentPageObject.createIncludePageOverlay(overlayName, x, y, 0);
        currentPage = currentOverlay;
    }

    /**
     * Helper method to mark the end of the current overlay.
     */
    public void endOverlay() {
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
     */
    public void endPage() {
        if (currentPageObject != null) {
            currentPageObject.endPage();
            if (currentPageGroup != null) {
                currentPageGroup.addPage(currentPageObject);
            } else {
                document.addPage(currentPageObject);
            }
        }
        currentPageObject = null;
        currentPage = null;
    }

    /**
     * Sets the offsets to be used for element positioning
     *
     * @param xOff
     *            the offset in the x direction
     * @param yOff
     *            the offset in the y direction
     * @param orientation
     *            the rotation
     * @deprecated offsets are no longer used, use setOrientation() for setting the orientation
     */
    public void setOffsets(int xOff, int yOff, int orientation) {
        setOrientation(orientation);
    }

    /**
     * Sets the orientation to be used for element positioning
     * 
     * @param orientation
     *            the orientation used for element positioning
     */
    public void setOrientation(int orientation) {
        this.orientation = orientation;
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
        getCurrentPage().createFont(fontReference, font, size);
    }

    /**
     * Helper method to create text on the current page, this method delegates
     * to the current presentation text object in order to construct the text.
     *
     * @param textDataInfo
     *            the afp text data
     */
    public void createText(TextDataInfo textDataInfo) {
        textDataInfo.setOrientation(orientation);
        getCurrentPage().createText(textDataInfo);
    }

    /**
     * Creates a data object in the datastream. The data object resides
     * according to its type, info and MO:DCA-L (resource) support.
     *
     * @param dataObjectInfo
     *            the data object info
     */
    public void createObject(DataObjectInfo dataObjectInfo) {
        ResourceInfo resourceInfo = dataObjectInfo.getResourceInfo();
        String uri = resourceInfo.getUri();

        if (uri == null) {
            uri = "/";
        }
        // if this is an instream data object adjust uri to ensure that it is
        // unique
        if (uri.endsWith("/")) {
            uri += "#" + (++instreamObjectCount);
            resourceInfo.setUri(uri);
        }

        // Update placement with current state
        ObjectAreaInfo areaInfo = dataObjectInfo.getObjectAreaInfo();
        areaInfo.setX(areaInfo.getX());
        areaInfo.setY(areaInfo.getY());
        areaInfo.setRotation(this.rotation);

        // Do we have a recognised/registered object type?
        Registry registry = Registry.getInstance();
        Registry.ObjectType objectType = registry.getObjectType(dataObjectInfo);
        if (objectType != null) {
            dataObjectInfo.setObjectType(objectType);
        } else {
            log.info("Unknown object type for '" + dataObjectInfo + "'");
        }
        
        DataObjectCache.Record record = cache.store(dataObjectInfo);

        if (objectType != null) {
            
            ResourceLevel resourceLevel = resourceInfo.getLevel();

            // is MO:DCA-L available?
            if (interchangeSet.supportsLevel2()) {
            
                // Can this data object use the include object (IOB) referencing
                // mechanism?
                if (objectType.isIncludable()) {
                    
                    // Create and return include
                    DataObjectFactory factory = cache.getFactory();
                    String objectName = record.getObjectName();
                    IncludeObject includeObj = factory.createInclude(objectName, dataObjectInfo);
                    getCurrentPage().addObject(includeObj);
                    
                    // Record the resource cache key (uri) in the ResourceGroup
                    ResourceGroup resourceGroup = getResourceGroup(resourceLevel);
                    resourceGroup.addObject(record);
                    return;   
                } else {
                    log.warn("Data object located at '" + uri + "'"
                            + " of type '" + objectType.getMimeType() + "'"
                            + " cannot be referenced with an include"
                            + " so it will be embedded directly in the page");
                }                
            } else {
                if (resourceLevel.isExternal()) {
                    log.warn(interchangeSet
                            + ": not available, object " + getName() + " will reside inline");
                }
            }
        }
        // Unrecognised/unsupported object type so add object reference directly in current page
        currentPageObject.addObject(record);
    }

//    /**
//     * Sets the object view port taking into account rotation.
//     *
//     * @param x
//     *            the x position of the object
//     * @param y
//     *            the y position of the object
//     * @param w
//     *            the width of the object
//     * @param h
//     *            the height of the object
//     * @param wr
//     *            the resolution width of the object
//     * @param hr
//     *            the resolution height of the object
//     * @return a new graphics object
//     */
//    private void setObjectViewPort(AbstractDataObject dataObj, int x, int y,
//            int w, int h, int wr, int hr) {
//        int xOrigin;
//        int yOrigin;
//        int width;
//        int height;
//        int widthRes;
//        int heightRes;
//        switch (this.rotation) {
//        case 90:
//            xOrigin = getCurrentPage().getWidth() - y - yOffset;
//            yOrigin = x + xOffset;
//            width = h;
//            height = w;
//            widthRes = hr;
//            heightRes = wr;
//            break;
//        case 180:
//            xOrigin = getCurrentPage().getWidth() - x - xOffset;
//            yOrigin = getCurrentPage().getHeight() - y - yOffset;
//            width = w;
//            height = h;
//            widthRes = wr;
//            heightRes = hr;
//            break;
//        case 270:
//            xOrigin = y + yOffset;
//            yOrigin = getCurrentPage().getHeight() - x - xOffset;
//            width = h;
//            height = w;
//            widthRes = hr;
//            heightRes = wr;
//            break;
//        default:
//            xOrigin = x + xOffset;
//            yOrigin = y + yOffset;
//            width = w;
//            height = h;
//            widthRes = wr;
//            heightRes = hr;
//            break;
//        }
//        dataObj.setViewport(xOrigin, yOrigin, width, height, widthRes,
//                heightRes, rotation);
//    }

    /**
     * Method to create a line on the current page.
     *
     * @param lineDataInfo the line data information.
     */
    public void createLine(LineDataInfo lineDataInfo) {
        lineDataInfo.setOrientation(orientation);
        getCurrentPage().createLine(lineDataInfo);
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
        currentPageObject.createIncludePageOverlay(name, 0, 0, orientation);
        currentPageObject.getActiveEnvironmentGroup().createOverlay(name);
    }

    /**
     * Helper method which allows creation of the IMM object.
     *
     * @param name
     *            the name of the medium map
     */
    public void createInvokeMediumMap(String name) {
        getCurrentPageGroup().createInvokeMediumMap(name);
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
        switch (orientation) {
        case 90:
            xOrigin = getCurrentPage().getWidth() - y;
            yOrigin = x;
            break;
        case 180:
            xOrigin = getCurrentPage().getWidth() - x;
            yOrigin = getCurrentPage().getHeight() - y;
            break;
        case 270:
            xOrigin = y;
            yOrigin = getCurrentPage().getHeight() - x;
            break;
        default:
            xOrigin = x;
            yOrigin = y;
            break;
        }
        getCurrentPage().createIncludePageSegment(name, xOrigin, yOrigin);
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
            getCurrentPage().createTagLogicalElement(name, value);
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
            String name = (String) attributes[i].getKey();
            String value = (String) attributes[i].getValue();
            getCurrentPageGroup().createTagLogicalElement(name, value);
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
            getCurrentPage().createTagLogicalElement(name, value);
        }
    }

    /**
     * Creates a NoOperation item
     *
     * @param content
     *            byte data
     */
    public void createNoOperation(String content) {
        getCurrentPage().createNoOperation(content);
    }

    /**
     * Returns the current page group
     * 
     * @return the current page group
     */
    private PageGroup getCurrentPageGroup() {
        if (currentPageGroup == null) {
            String pageGroupName = "PGP"
                    + StringUtils
                            .lpad(String.valueOf(++pageGroupCount), '0', 5);
            this.currentPageGroup = new PageGroup(pageGroupName);
        }
        return currentPageGroup;
    }

    /**
     * Start a new page group. When processing has finished on the current page
     * group the {@link #endPageGroup()}method must be invoked to mark the page
     * group ending.
     */
    public void startPageGroup() {
        getCurrentPageGroup();
    }

    /**
     * Helper method to mark the end of the page group.
     */
    public void endPageGroup() {
        if (currentPageGroup != null) {
            currentPageGroup.endPageGroup();
            getDocument().addPageGroup(currentPageGroup);
            currentPageGroup = null;
        }
    }

    /**
     * Sets the rotation to be used for portrait pages, valid values are 0
     * (default), 90, 180, 270.
     *
     * @param pageRotation the rotation in degrees.
     */
    public void setPortraitRotation(int pageRotation) {
        if (pageRotation == 0 || pageRotation == 90 || pageRotation == 180
                || pageRotation == 270) {
            this.portraitRotation = pageRotation;
        } else {
            throw new IllegalArgumentException(
                    "The portrait rotation must be one of the values 0, 90, 180, 270");
        }
    }

    /**
     * Sets the rotation to be used for landscape pages, valid values are 0, 90,
     * 180, 270 (default).
     *
     * @param pageRotation the rotation in degrees.
     */
    public void setLandscapeRotation(int pageRotation) {
        if (pageRotation == 0 || pageRotation == 90 || pageRotation == 180
                || pageRotation == 270) {
            this.landscapeRotation = pageRotation;
        } else {
            throw new IllegalArgumentException(
                    "The landscape rotation must be one of the values 0, 90, 180, 270");
        }
    }

    /**
     * Sets the default resource group file
     *
     * @param filePath the default resource group file path
     */
    public void setDefaultResourceGroupFilePath(String filePath) {
        getExternalResourceGroupManager().setDefaultResourceGroupFilePath(filePath);
    }

    /**
     * Returns the external resource group manager
     * 
     * @return the resource group manager
     */
    protected ExternalResourceGroupManager getExternalResourceGroupManager() {
        if (externalResourceGroupManager == null) {
            this.externalResourceGroupManager = new ExternalResourceGroupManager();
        }
        return this.externalResourceGroupManager;
    }

    /**
     * Returns the resource group for a given resource info
     *
     * @param level a resource level
     * @return a resource group for the given resource info
     */
    private ResourceGroup getResourceGroup(ResourceLevel level) {
        ResourceGroup resourceGroup = null;
        if (level.isExternal()) {
            resourceGroup = getExternalResourceGroupManager().getResourceGroup(level);
            // use print-file level resource group in the absence
            // of an external resource group file definition
            if (resourceGroup == null) {
                level.setLevel(ResourceLevel.PRINT_FILE);
            }
        }
        if (level.isPrintFile()) {
            resourceGroup = getResourceGroup();
        } else if (level.isDocument()) {
            resourceGroup = getDocument().getResourceGroup();
        } else if (level.isPageGroup()) {
            resourceGroup = getCurrentPageGroup().getResourceGroup();
        } else if (level.isPage()) {
            resourceGroup = currentPageObject.getResourceGroup();
        } 
        return resourceGroup;
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
}
