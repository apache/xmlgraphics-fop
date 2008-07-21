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
import org.apache.fop.render.afp.modca.Registry.ObjectType;
import org.apache.fop.render.afp.modca.triplets.FullyQualifiedNameTriplet;
import org.apache.fop.render.afp.modca.triplets.MappingOptionTriplet;
import org.apache.fop.render.afp.modca.triplets.ObjectClassificationTriplet;
import org.apache.fop.render.afp.tools.StringUtils;
import org.apache.xmlgraphics.image.codec.tiff.TIFFImage;

/**
 * Creator of MO:DCA data objects
 */
public class DataObjectFactory {
    private static final String IMAGE_NAME_PREFIX = "IMG";
    private static final String GRAPHIC_NAME_PREFIX = "GRA";
//    private static final String BARCODE_NAME_PREFIX = "BAR";
//    private static final String OTHER_NAME_PREFIX = "OTH";
    private static final String OBJECT_CONTAINER_NAME_PREFIX = "OC";
    private static final String RESOURCE_NAME_PREFIX = "RES";

    private int imageCount = 0;
    private int graphicCount = 0;
    private int objectContainerCount = 0;
    private int resourceCount = 0;
    
    private Map/*<ResourceInfo,IncludeObject>*/ includeMap
        = new java.util.HashMap/*<ResourceInfo,IncludeObject>*/();
    
    /** Static logging instance */
    private static final Log log = LogFactory.getLog(DataObjectFactory.class);
    
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
        + StringUtils.lpad(String.valueOf(++resourceCount ), '0', 5);
        return createResource(name);
    }

    /**
     * Creates and returns a new Overlay.
     *
     * @param overlayName
     *            the name of the overlay
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
    public Overlay createOverlay(String overlayName, int width, int height,
            int widthRes, int heightRes, int overlayRotation) {
        Overlay overlay = new Overlay(overlayName, width, height,
                overlayRotation, widthRes, heightRes);
        return overlay;        
    }
    
    /**
     * Creates and returns a new data object
     * 
     * @param dataObjectInfo the data object info
     * 
     * @return a newly created data object
     */
    public AbstractNamedAFPObject createObject(DataObjectInfo dataObjectInfo) {
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
            
            if (objectType != null && objectType.canBeIncluded()) {
                
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
                        resourceObj.setType(ResourceObject.OBJECT_CONTAINER);
                    } else if (dataObj instanceof ImageObject) {
                        resourceObj.setType(ResourceObject.IMAGE_OBJECT);
                    } else if (dataObj instanceof GraphicsObject) {
                        resourceObj.setType(ResourceObject.GRAPHICS_OBJECT);
                    } else if (dataObj instanceof Document) {
                        resourceObj.setType(ResourceObject.DOCUMENT_OBJECT);
                    } else if (dataObj instanceof PageSegment) {
                        resourceObj.setType(ResourceObject.PAGE_SEGMENT_OBJECT);
                    } else if (dataObj instanceof Overlay) {
                        resourceObj.setType(ResourceObject.OVERLAY_OBJECT);
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
