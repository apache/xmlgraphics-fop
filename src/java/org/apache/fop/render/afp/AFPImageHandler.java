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

import java.awt.Rectangle;
import java.util.Map;

import org.apache.fop.afp.AFPDataObjectInfo;
import org.apache.fop.afp.AFPObjectAreaInfo;
import org.apache.fop.afp.AFPPaintingState;
import org.apache.fop.afp.AFPResourceInfo;
import org.apache.fop.afp.AFPUnitConverter;
import org.apache.fop.render.ImageHandlerBase;

/**
 * A base abstract AFP image handler
 */
public abstract class AFPImageHandler implements ImageHandlerBase {
    private static final int X = 0;
    private static final int Y = 1;

    /** foreign attribute reader */
    private final AFPForeignAttributeReader foreignAttributeReader
        = new AFPForeignAttributeReader();

    /**
     * Sets resource information on the data object info.
     * @param dataObjectInfo the data object info instance
     * @param uri the image's URI (or null if no URI is available)
     * @param foreignAttributes a Map of foreign attributes (or null)
     */
    protected void setResourceInformation(AFPDataObjectInfo dataObjectInfo,
            String uri, Map foreignAttributes) {
        AFPResourceInfo resourceInfo
            = foreignAttributeReader.getResourceInfo(foreignAttributes);
        resourceInfo.setUri(uri);
        dataObjectInfo.setResourceInfo(resourceInfo);
    }

    /**
     * Creates and returns an {@link AFPObjectAreaInfo} instance for the placement of the image.
     * @param paintingState the painting state
     * @param targetRect the target rectangle in which to place the image (coordinates in mpt)
     * @return the newly created object area info instance
     */
    public static AFPObjectAreaInfo createObjectAreaInfo(AFPPaintingState paintingState,
            Rectangle targetRect) {
        AFPObjectAreaInfo objectAreaInfo = new AFPObjectAreaInfo();
        AFPUnitConverter unitConv = paintingState.getUnitConverter();

        int[] coords = unitConv.mpts2units(new float[] {targetRect.x, targetRect.y});
        objectAreaInfo.setX(coords[X]);
        objectAreaInfo.setY(coords[Y]);

        int width = Math.round(unitConv.mpt2units(targetRect.width));
        objectAreaInfo.setWidth(width);

        int height = Math.round(unitConv.mpt2units(targetRect.height));
        objectAreaInfo.setHeight(height);

        int resolution = paintingState.getResolution();
        objectAreaInfo.setHeightRes(resolution);
        objectAreaInfo.setWidthRes(resolution);

        objectAreaInfo.setRotation(paintingState.getRotation());
        return objectAreaInfo;
    }

    /**
     * Creates the data object information object
     *
     * @return the data object information object
     */
    protected abstract AFPDataObjectInfo createDataObjectInfo();
}
