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

import java.awt.geom.Rectangle2D;

import org.apache.xmlgraphics.image.codec.tiff.TIFFImage;
import org.apache.xmlgraphics.java2d.Graphics2DImagePainter;

import org.apache.fop.afp.ioca.IDEStructureParameter;
import org.apache.fop.afp.ioca.ImageContent;
import org.apache.fop.afp.modca.AbstractDataObject;
import org.apache.fop.afp.modca.AbstractNamedAFPObject;
import org.apache.fop.afp.modca.Document;
import org.apache.fop.afp.modca.GraphicsObject;
import org.apache.fop.afp.modca.ImageObject;
import org.apache.fop.afp.modca.IncludeObject;
import org.apache.fop.afp.modca.ObjectContainer;
import org.apache.fop.afp.modca.Overlay;
import org.apache.fop.afp.modca.PageSegment;
import org.apache.fop.afp.modca.Registry;
import org.apache.fop.afp.modca.ResourceObject;
import org.apache.fop.afp.modca.triplets.MappingOptionTriplet;
import org.apache.fop.afp.modca.triplets.ObjectClassificationTriplet;

/**
 * Factory for high level data objects (Image/Graphics etc)
 */
public class AFPDataObjectFactory {

    private final Factory factory;

    /**
     * Main constructor
     *
     * @param factory an object factory
     */
    public AFPDataObjectFactory(Factory factory) {
        this.factory = factory;
    }

    /**
     * Creates and configures an ObjectContainer.
     *
     * @param dataObjectInfo the object container info
     * @return a newly created Object Container
     */
    public ObjectContainer createObjectContainer(AFPDataObjectInfo dataObjectInfo) {
        ObjectContainer objectContainer = factory.createObjectContainer();

        // set data object viewport (i.e. position, rotation, dimension, resolution)
        objectContainer.setViewport(dataObjectInfo);

        // set object classification
        Registry.ObjectType objectType = dataObjectInfo.getObjectType();
        AFPResourceInfo resourceInfo = dataObjectInfo.getResourceInfo();
        AFPResourceLevel resourceLevel = resourceInfo.getLevel();
        final boolean dataInContainer = true;
        final boolean containerHasOEG = resourceLevel.isInline();
        final boolean dataInOCD = true;
        objectContainer.setObjectClassification(
                ObjectClassificationTriplet.CLASS_TIME_INVARIANT_PAGINATED_PRESENTATION_OBJECT,
                objectType, dataInContainer, containerHasOEG, dataInOCD);

        objectContainer.setData(dataObjectInfo.getData());
        return objectContainer;
    }

    /**
     * Creates and configures an IOCA Image Object.
     *
     * @param imageObjectInfo the image object info
     * @return a newly created IOCA Image Object
     */
    public ImageObject createImage(AFPImageObjectInfo imageObjectInfo) {
        // IOCA bitmap image
        ImageObject imageObj = factory.createImageObject();

        // set data object viewport (i.e. position, rotation, dimension, resolution)
        imageObj.setViewport(imageObjectInfo);

        if (imageObjectInfo.hasCompression()) {
            int compression = imageObjectInfo.getCompression();
            switch (compression) {
            case TIFFImage.COMP_FAX_G3_1D:
                imageObj.setEncoding(ImageContent.COMPID_G3_MH);
                break;
            case TIFFImage.COMP_FAX_G3_2D:
                imageObj.setEncoding(ImageContent.COMPID_G3_MR);
                break;
            case TIFFImage.COMP_FAX_G4_2D:
                imageObj.setEncoding(ImageContent.COMPID_G3_MMR);
                break;
            default:
                throw new IllegalStateException(
                        "Invalid compression scheme: " + compression);
            }
        }

        ImageContent content = imageObj.getImageSegment().getImageContent();
        int bitsPerPixel = imageObjectInfo.getBitsPerPixel();
        imageObj.setIDESize((byte) bitsPerPixel);
        IDEStructureParameter ideStruct;
        switch (bitsPerPixel) {
        case 1:
            //Skip IDE Structure Parameter
            break;
        case 4:
        case 8:
            ideStruct = content.needIDEStructureParameter();
            ideStruct.setBitsPerComponent(new int[] {bitsPerPixel});
            break;
        case 24:
            ideStruct = content.needIDEStructureParameter();
            ideStruct.setDefaultRGBColorModel();
            break;
        case 32:
            ideStruct = content.needIDEStructureParameter();
            ideStruct.setDefaultCMYKColorModel();
            break;
        default:
            throw new IllegalArgumentException("Unsupported number of bits per pixel: "
                    + bitsPerPixel);
        }
        if (imageObjectInfo.isSubtractive()) {
            ideStruct = content.needIDEStructureParameter();
            ideStruct.setSubtractive(imageObjectInfo.isSubtractive());
        }

        imageObj.setData(imageObjectInfo.getData());

        return imageObj;
    }

    /**
     * Creates and returns a new graphics object.
     *
     * @param graphicsObjectInfo the graphics object info
     * @return a new graphics object
     */
    public GraphicsObject createGraphic(AFPGraphicsObjectInfo graphicsObjectInfo) {
        // set newly created graphics object in g2d
        GraphicsObject graphicsObj = factory.createGraphicsObject();

        // set data object viewport (i.e. position, rotation, dimension, resolution)
        graphicsObj.setViewport(graphicsObjectInfo);

        AFPGraphics2D g2d = graphicsObjectInfo.getGraphics2D();
        g2d.setGraphicsObject(graphicsObj);

        // paint to graphics object
        Graphics2DImagePainter painter = graphicsObjectInfo.getPainter();
        Rectangle2D area = graphicsObjectInfo.getArea();
        g2d.scale(1, -1);
        g2d.translate(0, -area.getHeight());

        painter.paint(g2d, area);

        graphicsObj.setComplete(true);

        // return painted graphics object
        return graphicsObj;
    }

    /**
     * Creates and returns a new include object.
     *
     * @param includeName the include name
     * @param dataObjectInfo a data object info
     *
     * @return a new include object
     */
    public IncludeObject createInclude(String includeName, AFPDataObjectInfo dataObjectInfo) {
        IncludeObject includeObj = factory.createInclude(includeName);

        if (dataObjectInfo instanceof AFPImageObjectInfo) {
            // IOCA image object
            includeObj.setObjectType(IncludeObject.TYPE_IMAGE);
        } else if (dataObjectInfo instanceof AFPGraphicsObjectInfo) {
            // graphics object
            includeObj.setObjectType(IncludeObject.TYPE_GRAPHIC);
        } else {
            // object container
            includeObj.setObjectType(IncludeObject.TYPE_OTHER);

            // set mandatory object classification (type other)
            Registry.ObjectType objectType = dataObjectInfo.getObjectType();
            if (objectType != null) {
                // set object classification
                final boolean dataInContainer = true;
                final boolean containerHasOEG = false; // environment parameters set in include
                final boolean dataInOCD = true;
                includeObj.setObjectClassification(
                   // object scope not defined
                   ObjectClassificationTriplet.CLASS_TIME_VARIANT_PRESENTATION_OBJECT,
                   objectType, dataInContainer, containerHasOEG, dataInOCD);
            } else {
                throw new IllegalStateException(
                        "Failed to set Object Classification Triplet on Object Container.");
            }
        }

        AFPObjectAreaInfo objectAreaInfo = dataObjectInfo.getObjectAreaInfo();

        int xOffset = objectAreaInfo.getX();
        int yOffset = objectAreaInfo.getY();
        includeObj.setObjectAreaOffset(xOffset, yOffset);

        int width = objectAreaInfo.getWidth();
        int height = objectAreaInfo.getHeight();
        includeObj.setObjectAreaSize(width, height);

        int rotation = objectAreaInfo.getRotation();
        includeObj.setObjectAreaOrientation(rotation);

        int widthRes = objectAreaInfo.getWidthRes();
        int heightRes = objectAreaInfo.getHeightRes();
        includeObj.setMeasurementUnits(widthRes, heightRes);

        includeObj.setMappingOption(MappingOptionTriplet.SCALE_TO_FIT);

        return includeObj;
    }

    /**
     * Creates a resource object wrapper for named includable data objects
     *
     * @param namedObj an named object
     * @param resourceInfo resource information
     * @param objectType the object type
     * @return a new resource object wrapper
     */
    public ResourceObject createResource(AbstractNamedAFPObject namedObj,
            AFPResourceInfo resourceInfo, Registry.ObjectType objectType) {
        ResourceObject resourceObj = null;
        String resourceName = resourceInfo.getName();
        if (resourceName != null) {
            resourceObj = factory.createResource(resourceName);
        } else {
            resourceObj = factory.createResource();
        }

        if (namedObj instanceof Document) {
            resourceObj.setType(ResourceObject.TYPE_DOCUMENT);
        } else if (namedObj instanceof PageSegment) {
            resourceObj.setType(ResourceObject.TYPE_PAGE_SEGMENT);
        } else if (namedObj instanceof Overlay) {
            resourceObj.setType(ResourceObject.TYPE_OVERLAY_OBJECT);
        } else if (namedObj instanceof AbstractDataObject) {
            AbstractDataObject dataObj = (AbstractDataObject)namedObj;
            if (namedObj instanceof ObjectContainer) {
                resourceObj.setType(ResourceObject.TYPE_OBJECT_CONTAINER);

                // set object classification
                final boolean dataInContainer = true;
                final boolean containerHasOEG = false; // must be included
                final boolean dataInOCD = true;
                // mandatory triplet for object container
                resourceObj.setObjectClassification(
                    ObjectClassificationTriplet.CLASS_TIME_INVARIANT_PAGINATED_PRESENTATION_OBJECT,
                    objectType, dataInContainer, containerHasOEG, dataInOCD);
            } else if (namedObj instanceof ImageObject) {
                // ioca image type
                resourceObj.setType(ResourceObject.TYPE_IMAGE);
            } else if (namedObj instanceof GraphicsObject) {
                resourceObj.setType(ResourceObject.TYPE_GRAPHIC);
            } else {
                throw new UnsupportedOperationException(
                        "Unsupported resource object for data object type " + dataObj);
            }
        } else {
            throw new UnsupportedOperationException(
              "Unsupported resource object type " + namedObj);
        }

        // set the resource information/classification on the data object
        resourceObj.setDataObject(namedObj);
        return resourceObj;
    }

}
