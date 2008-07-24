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

package org.apache.fop.render.afp.modca.resource;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.render.afp.DataObjectInfo;
import org.apache.fop.render.afp.GraphicsObjectInfo;
import org.apache.fop.render.afp.GraphicsObjectPainter;
import org.apache.fop.render.afp.ImageObjectInfo;
import org.apache.fop.render.afp.ObjectAreaInfo;
import org.apache.fop.render.afp.ResourceInfo;
import org.apache.fop.render.afp.ResourceLevel;
import org.apache.fop.render.afp.modca.AbstractDataObject;
import org.apache.fop.render.afp.modca.AbstractNamedAFPObject;
import org.apache.fop.render.afp.modca.Document;
import org.apache.fop.render.afp.modca.GraphicsObject;
import org.apache.fop.render.afp.modca.ImageContent;
import org.apache.fop.render.afp.modca.ImageObject;
import org.apache.fop.render.afp.modca.IncludeObject;
import org.apache.fop.render.afp.modca.ObjectContainer;
import org.apache.fop.render.afp.modca.Overlay;
import org.apache.fop.render.afp.modca.PageGroup;
import org.apache.fop.render.afp.modca.PageObject;
import org.apache.fop.render.afp.modca.PageSegment;
import org.apache.fop.render.afp.modca.Registry;
import org.apache.fop.render.afp.modca.ResourceGroup;
import org.apache.fop.render.afp.modca.ResourceObject;
import org.apache.fop.render.afp.modca.Registry.ObjectType;
import org.apache.fop.render.afp.modca.triplets.FullyQualifiedNameTriplet;
import org.apache.fop.render.afp.modca.triplets.MappingOptionTriplet;
import org.apache.fop.render.afp.modca.triplets.ObjectClassificationTriplet;
import org.apache.fop.render.afp.tools.StringUtils;

import org.apache.xmlgraphics.image.codec.tiff.TIFFImage;

/**
 * Creator of MO:DCA data objects
 */
public class ResourceFactory {

    /** Static logging instance */
    private static final Log log = LogFactory.getLog(ResourceFactory.class);

    private static final String IMAGE_NAME_PREFIX = "IMG";

    private static final String GRAPHIC_NAME_PREFIX = "GRA";

    private static final String BARCODE_NAME_PREFIX = "BAR";
    
//    private static final String OTHER_NAME_PREFIX = "OTH";

    private static final String OBJECT_CONTAINER_NAME_PREFIX = "OC";

    private static final String RESOURCE_NAME_PREFIX = "RES";

    /** Default name for the resource group */
    private static final String RESOURCE_GROUP_NAME_PREFIX = "RG";
    
    private static final String PAGE_GROUP_NAME_PREFIX = "PGP";

    private static final String PAGE_NAME_PREFIX = "PGN";

    private static final String OVERLAY_NAME_PREFIX = "OVL";

    
    private Map/*<ResourceInfo,IncludeObject>*/ includeMap
        = new java.util.HashMap/*<ResourceInfo,IncludeObject>*/();
    
    private ResourceManager manager;

    
    /** The page group count */
    private int pageGroupCount = 0;
    
    /** The page count */
    private int pageCount = 0;

    /** The image count */
    private int imageCount = 0;

    /** The graphic count */
    private int graphicCount = 0;

    /** The object container count */
    private int objectContainerCount = 0;

    /** The resource count */
    private int resourceCount = 0;

    /** The resource group count */
    private int resourceGroupCount = 0;

    /** The overlay count */
    private int overlayCount = 0;
        
    /**
     * Main constructor
     * 
     * @param resourceManager the resource manager
     */
    public ResourceFactory(ResourceManager resourceManager) {
        this.manager = resourceManager;
    }

    /**
     * Converts a byte array containing 24 bit RGB image data to a grayscale
     * image.
     * 
     * @param io
     *            the target image object
     * @param info
     *            the image object info
     *
     * @return the converted image data
     */
    private static byte[] convertToGrayScaleImage(ImageObject io, ImageObjectInfo info) {
        byte[] raw = info.getData();
        int width = info.getDataWidth();
        int height = info.getDataHeight();
        int bitsPerPixel = info.getBitsPerPixel();
        
        int pixelsPerByte = 8 / bitsPerPixel;
        int bytewidth = (width / pixelsPerByte);
        if ((width % pixelsPerByte) != 0) {
            bytewidth++;
        }
        byte[] data = new byte[height * bytewidth];
        byte ib;
        for (int y = 0; y < height; y++) {
            ib = 0;
            int i = 3 * y * width;
            for (int x = 0; x < width; x++, i += 3) {

                // see http://www.jguru.com/faq/view.jsp?EID=221919
                double greyVal = 0.212671d * ((int) raw[i] & 0xff) + 0.715160d
                        * ((int) raw[i + 1] & 0xff) + 0.072169d
                        * ((int) raw[i + 2] & 0xff);
                switch (bitsPerPixel) {
                case 1:
                    if (greyVal < 128) {
                        ib |= (byte) (1 << (7 - (x % 8)));
                    }
                    break;
                case 4:
                    greyVal /= 16;
                    ib |= (byte) ((byte) greyVal << ((1 - (x % 2)) * 4));
                    break;
                case 8:
                    ib = (byte) greyVal;
                    break;
                default:
                    throw new UnsupportedOperationException(
                            "Unsupported bits per pixel: " + bitsPerPixel);
                }

                if ((x % pixelsPerByte) == (pixelsPerByte - 1)
                        || ((x + 1) == width)) {
                    data[(y * bytewidth) + (x / pixelsPerByte)] = ib;
                    ib = 0;
                }
            }
        }
        return data;
    }

    /**
     * Helper method to create an image on the current container and to return
     * the object.
     * 
     * @param imageObjectInfo the image object info
     * @return a newly created image object
     */
    private ImageObject createImage(ImageObjectInfo imageObjectInfo) {
        String name = IMAGE_NAME_PREFIX
                + StringUtils.lpad(String.valueOf(++imageCount), '0', 5);
        ImageObject imageObj = new ImageObject(name);
        if (imageObjectInfo.hasCompression()) {
            int compression = imageObjectInfo.getCompression();
            switch (compression) {
            case TIFFImage.COMP_FAX_G3_1D:
                imageObj.setImageEncoding(ImageContent.COMPID_G3_MH);
                    break;
                case TIFFImage.COMP_FAX_G3_2D:
                    imageObj.setImageEncoding(ImageContent.COMPID_G3_MR);
                    break;
                case TIFFImage.COMP_FAX_G4_2D:
                    imageObj.setImageEncoding(ImageContent.COMPID_G3_MMR);
                    break;
                default:
                    throw new IllegalStateException(
                            "Invalid compression scheme: " + compression);
            }
        }
        ObjectAreaInfo objectAreaInfo = imageObjectInfo.getObjectAreaInfo();
        imageObj.setImageParameters(objectAreaInfo.getWidthRes(), objectAreaInfo.getHeightRes(), 
                imageObjectInfo.getDataWidth(), imageObjectInfo.getDataHeight());
        if (imageObjectInfo.isBuffered()) {
            if (imageObjectInfo.isColor()) {
                imageObj.setImageIDESize((byte)24);
                imageObj.setImageData(imageObjectInfo.getData());
            } else {
                int bitsPerPixel = imageObjectInfo.getBitsPerPixel();
                imageObj.setImageIDESize((byte)bitsPerPixel);
                byte[] data = convertToGrayScaleImage(imageObj, imageObjectInfo);
                imageObj.setImageData(data);
            }    
        }
        return imageObj;
    }
    
    /**
     * Creates and returns a new graphics object.
     * 
     * @param graphicsObjectInfo the graphics object info
     * @return a new graphics object
     */
    private GraphicsObject createGraphic(GraphicsObjectInfo graphicsObjectInfo) {
        String name = GRAPHIC_NAME_PREFIX
            + StringUtils.lpad(String.valueOf(++graphicCount), '0', 5);
        GraphicsObject graphicsObj = new GraphicsObject(name);
        
        // paint the graphic using batik
        GraphicsObjectPainter painter = graphicsObjectInfo.getPainter();
        painter.paint(graphicsObj);
        
        return graphicsObj;
    }

    /**
     * Creates and returns a new object container
     * 
     * @return a new object container
     */
    private ObjectContainer createObjectContainer() {
        String name = OBJECT_CONTAINER_NAME_PREFIX
        + StringUtils.lpad(String.valueOf(++objectContainerCount), '0', 6);
        return new ObjectContainer(name);
    }

    /**
     * Creates and returns a new resource object
     * 
     * @param resourceName the resource name
     * @return a new resource object
     */
    private ResourceObject createResource(String resourceName) {
        return new ResourceObject(resourceName);
    }

    /**
     * Creates and returns a new resource object
     * 
     * @return a new resource object
     */
    private ResourceObject createResource() {
        String name = RESOURCE_NAME_PREFIX
        + StringUtils.lpad(String.valueOf(++resourceCount), '0', 5);
        return createResource(name);
    }

    /**
     * Creates and returns a new include object.
     * 
     * @param name the name of this include object
     * @param dataObjectInfo a data object info
     * 
     * @return a new include object
     */
    public IncludeObject createInclude(String name, DataObjectInfo dataObjectInfo) {
        ResourceInfo resourceInfo = dataObjectInfo.getResourceInfo();
        IncludeObject includeObj = (IncludeObject)includeMap.get(resourceInfo);
        if (includeObj == null) {
            includeObj = new IncludeObject(name);
        
            if (dataObjectInfo instanceof ImageObjectInfo) {
                includeObj.setDataObjectType(IncludeObject.TYPE_IMAGE);
            } else if (dataObjectInfo instanceof GraphicsObjectInfo) {
                includeObj.setDataObjectType(IncludeObject.TYPE_GRAPHIC);
            } else {
                includeObj.setDataObjectType(IncludeObject.TYPE_OTHER);
            }
            
            Registry.ObjectType objectType = dataObjectInfo.getObjectType();
            if (objectType != null) {
                includeObj.setObjectClassification(
                   ObjectClassificationTriplet.CLASS_TIME_INVARIANT_PAGINATED_PRESENTATION_OBJECT,
                   objectType);
            }
            
            ObjectAreaInfo objectAreaInfo = dataObjectInfo.getObjectAreaInfo();
            
            includeObj.setObjectArea(objectAreaInfo.getX(), objectAreaInfo.getY());
    
            includeObj.setObjectAreaSize(
                    objectAreaInfo.getWidth(), objectAreaInfo.getHeight());        
    
            includeObj.setMeasurementUnits(
                    objectAreaInfo.getWidthRes(), objectAreaInfo.getHeightRes());
            
            includeObj.setMappingOption(MappingOptionTriplet.SCALE_TO_FIT);
            
            includeMap.put(resourceInfo, includeObj);
        }
        
        return includeObj;
    }

    /**
     * Creates and returns a new page group
     * 
     * @return a new page group object
     */
    public PageGroup createPageGroup() {
        String name = PAGE_GROUP_NAME_PREFIX
        + StringUtils.lpad(String.valueOf(++pageGroupCount), '0', 5);
        return new PageGroup(manager, name);
    }    

    /**
     * Creates and returns a new resource object
     * 
     * @return a new resource object
     */
    public ResourceGroup createResourceGroup() {
        String name = RESOURCE_GROUP_NAME_PREFIX
        + StringUtils.lpad(String.valueOf(++resourceGroupCount), '0', 6);
        return new ResourceGroup(manager, name);
    }
    
    /**
     * Creates and returns a new page object
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
     * 
     * @return a new page object
     */
    public PageObject createPage(int pageWidth, int pageHeight, int pageRotation,
            int pageWidthRes, int pageHeightRes) {
        String pageName = PAGE_NAME_PREFIX
        + StringUtils.lpad(String.valueOf(++pageCount), '0', 5);
        return new PageObject(manager, pageName, pageWidth, pageHeight,
            pageRotation, pageWidthRes, pageHeightRes);
    }


    /**
     * Creates and returns a new Overlay.
     *
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
     * 
     * @return a new overlay object
     */
    public Overlay createOverlay(int width, int height,
            int widthRes, int heightRes, int overlayRotation) {
        String overlayName = OVERLAY_NAME_PREFIX
        + StringUtils.lpad(String.valueOf(++overlayCount), '0', 5);
        Overlay overlay = new Overlay(manager, overlayName, width, height,
                overlayRotation, widthRes, heightRes);
        return overlay;        
    }
    
    /**
     * Creates a new MO:DCA document object
     * 
     * @return a new MO:DCA document object
     */
    public Document createDocument() {
        return new Document(manager);
    }

    /**
     * Creates and returns a new data object
     * 
     * @param dataObjectInfo the data object info
     * 
     * @return a newly created data object
     */
    public AbstractNamedAFPObject create(DataObjectInfo dataObjectInfo) {
        AbstractNamedAFPObject dataObj;
        
        if (dataObjectInfo instanceof ImageObjectInfo) {
            dataObj = createImage((ImageObjectInfo)dataObjectInfo);
        } else if (dataObjectInfo instanceof GraphicsObjectInfo) {
            dataObj = createGraphic((GraphicsObjectInfo)dataObjectInfo);
        } else {
            throw new IllegalArgumentException("Unknown data object type: " + dataObjectInfo);
        }
        
        if (dataObj instanceof AbstractDataObject) {
            ((AbstractDataObject)dataObj).setViewport(dataObjectInfo.getObjectAreaInfo());
        }
        
        dataObj.setFullyQualifiedName(
                FullyQualifiedNameTriplet.TYPE_DATA_OBJECT_INTERNAL_RESOURCE_REF,
                FullyQualifiedNameTriplet.FORMAT_CHARSTR, dataObj.getName());

        ResourceInfo resourceInfo = dataObjectInfo.getResourceInfo();
        ResourceLevel resourceLevel = resourceInfo.getLevel();

        if (resourceLevel.isPrintFile() || resourceLevel.isExternal()) {
            
            ObjectType objectType = dataObjectInfo.getObjectType();
            
            if (objectType != null && objectType.isIncludable()) {
                
                // Wrap newly created data object in a resource object
                // if it is to reside within a resource group at print-file or external level
                if (resourceLevel.isPrintFile() || resourceLevel.isExternal()) {
                    ResourceObject resourceObj = null;
                    String resourceName = resourceInfo.getName();
                    if (resourceName != null) {
                        resourceObj = createResource(resourceName);
                    } else {
                        resourceObj = createResource();
                    }
                    
                    if (dataObj instanceof ObjectContainer) {
                        resourceObj.setType(ResourceObject.TYPE_OBJECT_CONTAINER);
                    } else if (dataObj instanceof ImageObject) {
                        resourceObj.setType(ResourceObject.TYPE_IMAGE);
                    } else if (dataObj instanceof GraphicsObject) {
                        resourceObj.setType(ResourceObject.TYPE_GRAPHIC);
                    } else if (dataObj instanceof Document) {
                        resourceObj.setType(ResourceObject.TYPE_DOCUMENT);
                    } else if (dataObj instanceof PageSegment) {
                        resourceObj.setType(ResourceObject.TYPE_PAGE_SEGMENT);
                    } else if (dataObj instanceof Overlay) {
                        resourceObj.setType(ResourceObject.TYPE_OVERLAY_OBJECT);
                    } else {
                        throw new UnsupportedOperationException(
                          "Unsupported resource object type " + dataObj);
                    }
                                
                    resourceObj.setObjectClassification(
                    ObjectClassificationTriplet.CLASS_TIME_INVARIANT_PAGINATED_PRESENTATION_OBJECT,
                    objectType);
                    
                    resourceObj.setDataObject(dataObj);
                    dataObj = resourceObj;
                }
            }
        }
        
        return dataObj;
    }
}
