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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.render.afp.AFPFontAttributes;
import org.apache.fop.render.afp.DataObjectInfo;
import org.apache.fop.render.afp.ResourceInfo;
import org.apache.fop.render.afp.fonts.AFPFont;
import org.apache.fop.render.afp.modca.triplets.FullyQualifiedNameTriplet;
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
public class AFPDataStream extends AbstractResourceGroupContainer {

    /**
     * Static logging instance
     */
    protected static Log log = LogFactory.getLog("org.apache.fop.render.afp.modca");

    /**
     * Boolean completion indicator
     */
    private boolean complete = false;

    /**
     * The application producing the AFP document
     */
// not used
//    private String producer = null;

    /**
     * The AFP document object
     */
    private Document document = null;

    /**
     * The current page group object
     */
    private PageGroup currentPageGroup = null;

    /**
     * The current page object
     */
    private PageObject currentPageObject = null;

    /**
     * The current resource group
     */
//    private ResourceGroup currentResourceGroup = null;
    
    /**
     * The current overlay object
     */
    private Overlay currentOverlay = null;

    /**
     * The current page
     */
    private AbstractPageObject currentPage = null;

    /**
     * The page count
     */
    private int pageCount = 0;

    /**
     * The page group count
     */
    private int pageGroupCount = 0;

    /**
     * The overlay count
     */
    private int ovlCount = 0;

    /**
     * The portrait rotation
     */
    private int portraitRotation = 0;

    /**
     * The landscape rotation
     */
    private int landscapeRotation = 270;

    /**
     * The x offset
     */
    private int xOffset = 0;

    /**
     * The y offset
     */
    private int yOffset = 0;

    /**
     * The rotation
     */
    private int rotation;

    /**
     * The outputstream for the data stream
     */
    private OutputStream outputStream = null;

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

    private Document getDocument() {
        return this.document;
    }
    
    private AbstractPageObject getCurrentPage() {
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
//        if (document != null) {
//            String msg = "Invalid state - document already started.";
//            log.warn("startDocument():: " + msg);
//            throw new IllegalStateException(msg);
//        }
//        if (document != null) {
//            String msg = "Invalid state - print file level document already started"; 
//            log.warn(msg);
//            throw new IllegalStateException(msg);
//        }

        if (name != null) {
            document.setFullyQualifiedName(
                    FullyQualifiedNameTriplet.TYPE_BEGIN_DOCUMENT_REF,
                    FullyQualifiedNameTriplet.FORMAT_CHARSTR,
                    name);
        }
    }

    /**
     * Sets the OutputStream
     * @param outputStream the AFP OutputStream
     */
    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }
    
    /**
     * The document is written/ended by invoking this method which creates an instance
     * of the AFP Document object and registers the start with a validation map
     * which ensures that methods are not invoked out of the correct sequence.
     * 
     * @throws java.io.IOException
     *             throws an I/O exception of some sort has occurred
     */
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

        // Write out any external resource groups
        getExternalResourceGroupManager().writeExternalResources();

        // Write out any print-file level resources
        if (hasResources()) {
            getResourceGroup().writeDataStream(this.outputStream);
        }

        // Write out document
        if (document != null) {
            document.endDocument();
            document.writeDataStream(this.outputStream);
        }

        this.outputStream.flush();

        complete = true;

        document = null;

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
        String pageName = "PGN"
                + StringUtils.lpad(String.valueOf(pageCount++), '0', 5);

        currentPageObject = new PageObject(pageName, pageWidth, pageHeight,
                pageRotation, pageWidthRes, pageHeightRes);
        currentPage = currentPageObject;
        currentOverlay = null;
        setOffsets(0, 0, 0);
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
     * @param rotation
     *            the rotation of the overlay
     */
    public void startOverlay(int x, int y, int width, int height, int widthRes,
            int heightRes, int rotation) {
        String overlayName = "OVL"
                + StringUtils.lpad(String.valueOf(ovlCount++), '0', 5);

        currentOverlay = new Overlay(overlayName, width, height,
                widthRes, heightRes, rotation);
        
        currentPageObject.createIncludePageOverlay(overlayName, x, y, 0);
        currentPage = currentOverlay;
        setOffsets(0, 0, 0);
    }

    /**
     * Helper method to mark the end of the current overlay.
     */
    public void endOverlay() {
        currentOverlay.endPage();
        currentOverlay = null;
        currentPage = currentPageObject;
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
        currentPageObject.endPage();
        if (currentPageGroup != null) {
            currentPageGroup.addPage(currentPageObject);
        } else {
            document.addPage(currentPageObject);
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
     * @param rot
     *            the rotation
     */
    public void setOffsets(int xOff, int yOff, int rot) {
        this.xOffset = xOff;
        this.yOffset = yOff;
        this.rotation = rot;
    }

    /**
     * Creates the given page fonts in the current page
     * @param pageFonts a collection of AFP font attributes
     */
    public void addFontsToCurrentPage(Map pageFonts) {
        Iterator iter = pageFonts.values().iterator();
        while (iter.hasNext()) {
            AFPFontAttributes afpFontAttributes = (AFPFontAttributes)iter.next();
            createFont(
                afpFontAttributes.getFontReference(),
                afpFontAttributes.getFont(),
                afpFontAttributes.getPointSize());
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
     * @param fontReference
     *            the font reference used as the resource identifier
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
    public void createText(int fontReference, int x, int y, Color col, int vsci,
            int ica, byte[] data) {
        getCurrentPage().createText(fontReference, x + xOffset, y + yOffset, rotation,
                col, vsci, ica, data);
    }

    /**
     * Creates a data object in the datastream.  The data object resides according
     * to its type, info and MO:DCA-L (resource) support.
     * 
     * @param dataObjectInfo
     *            the data object info
     * @return
     *            a data object
     */
    public AbstractNamedAFPObject createObject(DataObjectInfo dataObjectInfo) {
        ObjectTypeRegistry registry = ObjectTypeRegistry.getInstance();
        ObjectTypeRegistry.ObjectType objectType = registry.getObjectType(dataObjectInfo);
        if (objectType != null) {
            dataObjectInfo.setObjectType(objectType);
        }        
        AbstractNamedAFPObject dataObj;
        // can this data object use the include object (IOB) mechanism?
        if (objectType.canBeIncluded()) {
            ResourceInfo resourceInfo = dataObjectInfo.getResourceInfo();
            ResourceGroup resourceGroup = getResourceGroup(resourceInfo);
            dataObj = resourceGroup.createObject(dataObjectInfo);
        } else {
            dataObj = getCurrentPage().createObject(dataObjectInfo);
        }
        return dataObj;
    }
    
    /**
     * Sets the object view port taking into account rotation.
     *
     * @param x
     *            the x position of the object
     * @param y
     *            the y position of the object
     * @param w
     *            the width of the object
     * @param h
     *            the height of the object
     * @param wr
     *            the resolution width of the object
     * @param hr
     *            the resolution height of the object
     * @return
     *            a new graphics object
     */
    private void setObjectViewPort(AbstractDataObject dataObj,
            int x, int y, int w, int h, int wr, int hr) {
        int xOrigin;
        int yOrigin;
        int width;
        int height;
        int widthRes;
        int heightRes;
        switch (this.rotation) {
        case 90:
            xOrigin = getCurrentPage().getWidth() - y - yOffset;
            yOrigin = x + xOffset;
            width = h;
            height = w;
            widthRes = hr;
            heightRes = wr;
            break;
        case 180:
            xOrigin = getCurrentPage().getWidth() - x - xOffset;
            yOrigin = getCurrentPage().getHeight() - y - yOffset;
            width = w;
            height = h;
            widthRes = wr;
            heightRes = hr;
            break;
        case 270:
            xOrigin = y + yOffset;
            yOrigin = getCurrentPage().getHeight() - x - xOffset;
            width = h;
            height = w;
            widthRes = hr;
            heightRes = wr;
            break;
        default:
            xOrigin = x + xOffset;
            yOrigin = y + yOffset;
            width = w;
            height = h;
            widthRes = wr;
            heightRes = hr;
            break;
        }
        dataObj.setViewport(xOrigin, yOrigin, width, height, widthRes, heightRes, rotation);
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
    public void createLine(int x1, int y1, int x2, int y2, int thickness,
            Color col) {
        getCurrentPage().createLine(x1 + xOffset, y1 + yOffset, x2 + xOffset, y2
                + yOffset, thickness, rotation, col);
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
        getCurrentPage().createShading(x + xOffset, y + xOffset, w, h, red, green,
                blue);
    }

    /**
     * Helper method which allows creation of the MPO object, via the AEG. And
     * the IPO via the Page. (See actual object for descriptions.)
     * 
     * @param name
     *            the name of the static overlay
     */
    public void createIncludePageOverlay(String name) {
        currentPageObject.createIncludePageOverlay(name, 0, 0, rotation);
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
        switch (rotation) {
        case 90:
            xOrigin = getCurrentPage().getWidth() - y - yOffset;
            yOrigin = x + xOffset;
            break;
        case 180:
            xOrigin = getCurrentPage().getWidth() - x - xOffset;
            yOrigin = getCurrentPage().getHeight() - y - yOffset;
            break;
        case 270:
            xOrigin = y + yOffset;
            yOrigin = getCurrentPage().getHeight() - x - xOffset;
            break;
        default:
            xOrigin = x + xOffset;
            yOrigin = y + yOffset;
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

    private PageGroup getCurrentPageGroup() {
        if (currentPageGroup == null) {
            String pageGroupName = "PGP"
                + StringUtils.lpad(String.valueOf(pageGroupCount++), '0', 5);
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
     * @param pageRotation
     *            The rotation in degrees.
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
     * @param pageRotation
     *            The rotation in degrees.
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
     * Returns the resource group for a given resource into
     * @param resourceInfo resource info
     * @return a resource group container for the given resource info
     */
    private ResourceGroup getResourceGroup(ResourceInfo resourceInfo) {
        ResourceGroup resourceGroup = null;
        if (resourceInfo.isPrintFile()) {
            resourceGroup = getResourceGroup();
        } else if (resourceInfo.isDocument()) {
            resourceGroup = getDocument().getResourceGroup();
        } else if (resourceInfo.isPageGroup()) {
            resourceGroup = getCurrentPageGroup().getResourceGroup();
        } else if (resourceInfo.isPage()) {
            resourceGroup = getCurrentPage().getResourceGroup(); 
        } else if (resourceInfo.isExternal()) {
            resourceGroup = getExternalResourceGroupManager().getExternalResourceGroup(
                    resourceInfo);
        }
        return resourceGroup;
    }

    /**
     * Sets the default resource group file
     * @param resourceGroupFile the default resource group file
     */
    public void setDefaultResourceGroupFile(File resourceGroupFile) {
        getExternalResourceGroupManager().setDefaultResourceGroupFile(resourceGroupFile);
    }

    /**
     * @return the resource group manager
     */
    protected ExternalResourceGroupManager getExternalResourceGroupManager() {
        if (externalResourceGroupManager == null) {
            this.externalResourceGroupManager = new ExternalResourceGroupManager(this);
        }
        return this.externalResourceGroupManager;
    }

    
    /**
     * Manages the use of resource groups (external and internal)
     */
    private final class ExternalResourceGroupManager {
        /**
         * A mapping of external resource destinations to resource groups
         */
        private Map/*<File, ResourceGroup>*/ externalResourceGroups = null;

        /** sets the default resource group file */
        private File defaultResourceGroupFile;

        /** the container of this manager */
        private AbstractResourceGroupContainer container;

        /**
         * Main constructor
         * @param container the container of this manager
         */
        private ExternalResourceGroupManager(AbstractResourceGroupContainer container) {
            this.container = container;
        }
        
        /**
         * Sets the default resource group file
         * @param resourceGroupFile the default resource group file
         */
        private void setDefaultResourceGroupFile(File resourceGroupFile) {
            this.defaultResourceGroupFile = resourceGroupFile;
        }

        /**
         * Writes out external AFP resources
         */
        private void writeExternalResources() {
            // write any external resources
            Iterator it = getExternalResourceGroups().keySet().iterator();
            while (it.hasNext()) {
                String externalDest = (String)it.next();
                ResourceGroup resourceGroup
                    = (ResourceGroup)getExternalResourceGroups().get(externalDest);
                OutputStream os = null;
                try {
                    log.debug("Writing external AFP resource file " + externalDest);
                    os = new java.io.FileOutputStream(externalDest);
                    resourceGroup.writeDataStream(os);
                } catch (FileNotFoundException e) {
                    log.error("Failed to open external AFP resource file "
                            + externalDest);
                } catch (IOException e) {
                    log.error(
                            "An error occurred when attempting to write external AFP resource file "
                            + externalDest);
                } finally {
                    if (os != null) {
                        try {
                            os.close();
                        } catch (IOException e) {
                            log.error("Failed to close outputstream for external AFP resource file "
                                    + externalDest);
                        }
                    }
                }
            }
        }
        
        private ResourceGroup getExternalResourceGroup(ResourceInfo resourceInfo) {
            ResourceGroup resourceGroup;
            // this resource info does not have a an external resource group file definition
            if (!resourceInfo.hasExternalResourceGroupFile()) {
                if (defaultResourceGroupFile != null) {
                    // fallback to default resource group file
                    resourceInfo.setExternalResourceGroupFile(defaultResourceGroupFile);
                    resourceGroup = getExternalResourceGroup(resourceInfo);
                } else {
                    // use print-file level resource group in the absence
                    // of an external resource group file definition
                    resourceGroup = container.getResourceGroup();
                }
            } else {
                File resourceGroupFile = resourceInfo.getExternalResourceGroupFile();
                resourceGroup = (ResourceGroup)getExternalResourceGroups().get(resourceGroupFile);
                if (resourceGroup == null) {        
                    resourceGroup = new ResourceGroup(container);
                    externalResourceGroups.put(resourceGroupFile, resourceGroup);
                }
            }
            return resourceGroup;
        }
        
        private Map/*<File, ResourceGroup>*/ getExternalResourceGroups() {
            if (externalResourceGroups == null) {
                externalResourceGroups = new java.util.HashMap/*<File, ResourceGroup>*/();
            }
            return externalResourceGroups;
        }      
    }
}
