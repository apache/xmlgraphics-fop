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

package org.apache.fop.render.afp;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.Map;

import org.apache.fop.afp.AFPDataObjectInfo;
import org.apache.fop.afp.AFPObjectAreaInfo;
import org.apache.fop.afp.AFPPaintingState;
import org.apache.fop.afp.AFPResourceInfo;
import org.apache.fop.afp.AFPUnitConverter;
import org.apache.fop.render.ImageHandlerBase;
import org.apache.fop.render.RendererContext;

/**
 * A base abstract AFP image handler
 */
public abstract class AFPImageHandler implements ImageHandlerBase {
    private static final int X = 0;
    private static final int Y = 1;

    /** foreign attribute reader */
    private static final AFPForeignAttributeReader FOREIGN_ATTRIBUTE_READER
        = new AFPForeignAttributeReader();

    /**
     * Generates an intermediate AFPDataObjectInfo that is later used to construct
     * the appropriate data object in the AFP DataStream.
     *
     * @param rendererImageInfo the renderer image info
     * @return a data object info object
     * @throws IOException thrown if an I/O exception of some sort has occurred.
     */
    public AFPDataObjectInfo generateDataObjectInfo(
            AFPRendererImageInfo rendererImageInfo) throws IOException {
        AFPDataObjectInfo dataObjectInfo = createDataObjectInfo();

        // set resource information
        dataObjectInfo.setResourceInfo(createResourceInformation(
                rendererImageInfo.getURI(),
                rendererImageInfo.getForeignAttributes()));


        Point origin = rendererImageInfo.getOrigin();
        Rectangle2D position = rendererImageInfo.getPosition();
        int srcX = Math.round(origin.x + (float)position.getX());
        int srcY = Math.round(origin.y + (float)position.getY());
        Rectangle targetRect = new Rectangle(
                srcX,
                srcY,
                (int)Math.round(position.getWidth()),
                (int)Math.round(position.getHeight()));

        RendererContext context = rendererImageInfo.getRendererContext();
        assert (context instanceof AFPRendererContext);
        AFPRendererContext rendererContext = (AFPRendererContext) context;
        AFPInfo afpInfo = rendererContext.getInfo();
        AFPPaintingState paintingState = afpInfo.getPaintingState();

        dataObjectInfo.setObjectAreaInfo(createObjectAreaInfo(paintingState, targetRect));

        return dataObjectInfo;
    }

    /**
     * Sets resource information on the data object info.
     * @param uri the image's URI (or null if no URI is available)
     * @param foreignAttributes a Map of foreign attributes (or null)
     * @return the resource information object
     */
    public static AFPResourceInfo createResourceInformation(
            String uri, Map foreignAttributes) {
        AFPResourceInfo resourceInfo
            = FOREIGN_ATTRIBUTE_READER.getResourceInfo(foreignAttributes);
        resourceInfo.setUri(uri);

       return resourceInfo;
    }

    /**
     * Creates and returns an {@link AFPObjectAreaInfo} instance for the placement of the image.
     * @param paintingState the painting state
     * @param targetRect the target rectangle in which to place the image (coordinates in mpt)
     * @return the newly created object area info instance
     */
    public static AFPObjectAreaInfo createObjectAreaInfo(AFPPaintingState paintingState,
            Rectangle targetRect) {
        AFPUnitConverter unitConv = paintingState.getUnitConverter();

        int[] coords = unitConv.mpts2units(new float[] {targetRect.x, targetRect.y});

        int width = (int) Math.ceil(unitConv.mpt2units(targetRect.width));
        int height = (int) Math.ceil(unitConv.mpt2units(targetRect.height));

        int resolution = paintingState.getResolution();
        return new AFPObjectAreaInfo(coords[X], coords[Y], width,
                height, resolution, paintingState.getRotation());
    }

    /**
     * Creates the data object information object
     *
     * @return the data object information object
     */
    protected abstract AFPDataObjectInfo createDataObjectInfo();
}
