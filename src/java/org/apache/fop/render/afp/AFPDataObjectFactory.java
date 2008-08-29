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

/* $Id: $ */

package org.apache.fop.render.afp;

import org.apache.fop.render.afp.ioca.ImageContent;
import org.apache.fop.render.afp.modca.AbstractDataObject;
import org.apache.fop.render.afp.modca.AbstractNamedAFPObject;
import org.apache.fop.render.afp.modca.Document;
import org.apache.fop.render.afp.modca.Factory;
import org.apache.fop.render.afp.modca.GraphicsObject;
import org.apache.fop.render.afp.modca.ImageObject;
import org.apache.fop.render.afp.modca.IncludeObject;
import org.apache.fop.render.afp.modca.MapDataResource;
import org.apache.fop.render.afp.modca.ObjectContainer;
import org.apache.fop.render.afp.modca.Overlay;
import org.apache.fop.render.afp.modca.PageSegment;
import org.apache.fop.render.afp.modca.Registry;
import org.apache.fop.render.afp.modca.ResourceObject;
import org.apache.fop.render.afp.modca.triplets.FullyQualifiedNameTriplet;
import org.apache.fop.render.afp.modca.triplets.MappingOptionTriplet;
import org.apache.fop.render.afp.modca.triplets.ObjectClassificationTriplet;
import org.apache.xmlgraphics.image.codec.tiff.TIFFImage;

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
     * Creates an IOCA ImageObject or an ObjectContainer as appropriate.
     *
     * @param imageObjectInfo the image object info
     * @return a newly created image object
     */
    public AbstractDataObject createImage(AFPImageObjectInfo imageObjectInfo) {
        AbstractDataObject dataObj = null;
        // A known object type so place in an object container
        if (imageObjectInfo.isBuffered()) {
            // IOCA bitmap image
            ImageObject imageObj = factory.createImageObject();
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

            if (imageObjectInfo.isColor()) {
                imageObj.setIDESize((byte) 24);
            } else {
                imageObj.setIDESize((byte) imageObjectInfo.getBitsPerPixel());
            }
            imageObj.setData(imageObjectInfo.getData());

            dataObj = imageObj;

        } else {
            ObjectContainer objectContainer = factory.createObjectContainer();

            Registry.ObjectType objectType = imageObjectInfo.getObjectType();

            objectContainer.setObjectClassification(
                    ObjectClassificationTriplet.CLASS_TIME_INVARIANT_PAGINATED_PRESENTATION_OBJECT,
                    objectType);

            objectContainer.setInputStream(imageObjectInfo.getInputStream());

            dataObj = objectContainer;
        }
        return dataObj;
    }

    /**
     * Creates and returns a new graphics object.
     *
     * @param graphicsObjectInfo the graphics object info
     * @return a new graphics object
     */
    public GraphicsObject createGraphic(AFPGraphicsObjectInfo graphicsObjectInfo) {
        GraphicsObject graphicsObj = factory.createGraphic();
        // paint the graphic using batik
        graphicsObjectInfo.getPainter().paint(graphicsObj);
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
    public IncludeObject createInclude(String name, AFPDataObjectInfo dataObjectInfo) {
        IncludeObject includeObj = factory.createInclude(name);

        if (dataObjectInfo instanceof AFPImageObjectInfo) {
            includeObj.setObjectType(IncludeObject.TYPE_IMAGE);
        } else if (dataObjectInfo instanceof AFPGraphicsObjectInfo) {
            includeObj.setObjectType(IncludeObject.TYPE_GRAPHIC);
        } else {
            includeObj.setObjectType(IncludeObject.TYPE_OTHER);
        }

        Registry.ObjectType objectType = dataObjectInfo.getObjectType();
        if (objectType != null) {
            includeObj.setObjectClassification(
               // object scope not defined
               ObjectClassificationTriplet.CLASS_TIME_VARIANT_PRESENTATION_OBJECT,
               objectType);
        }

        AFPObjectAreaInfo objectAreaInfo = dataObjectInfo.getObjectAreaInfo();

        includeObj.setObjectArea(objectAreaInfo.getX(), objectAreaInfo.getY());

        includeObj.setObjectAreaSize(
                objectAreaInfo.getWidth(), objectAreaInfo.getHeight());

        includeObj.setOrientation(objectAreaInfo.getRotation());

        includeObj.setMeasurementUnits(
                objectAreaInfo.getWidthRes(), objectAreaInfo.getHeightRes());

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

            // other type by default
            byte fqnType = FullyQualifiedNameTriplet.TYPE_OTHER_OBJECT_DATA_REF;
            if (namedObj instanceof ObjectContainer) {
                resourceObj.setType(ResourceObject.TYPE_OBJECT_CONTAINER);

                // mandatory triplet for object container
                resourceObj.setObjectClassification(
                    ObjectClassificationTriplet.CLASS_TIME_INVARIANT_PAGINATED_PRESENTATION_OBJECT,
                    objectType);
            } else if (namedObj instanceof ImageObject) {
                resourceObj.setType(ResourceObject.TYPE_IMAGE);
                // ioca image type
                fqnType = FullyQualifiedNameTriplet.TYPE_BEGIN_RESOURCE_OBJECT_REF;
            } else if (namedObj instanceof GraphicsObject) {
                resourceObj.setType(ResourceObject.TYPE_GRAPHIC);
            } else {
                throw new UnsupportedOperationException(
                        "Unsupported resource object for data object type " + dataObj);
            }

            // set the map data resource
            MapDataResource mapDataResource = factory.createMapDataResource();
            mapDataResource.setFullyQualifiedName(
                 fqnType,
                 FullyQualifiedNameTriplet.FORMAT_CHARSTR,
                 resourceObj.getName());
            dataObj.getObjectEnvironmentGroup().setMapDataResource(mapDataResource);

        } else {
            throw new UnsupportedOperationException(
              "Unsupported resource object type " + namedObj);
        }

        // set the resource information/classification on the data object
        resourceObj.setDataObject(namedObj);
        return resourceObj;
    }

}
