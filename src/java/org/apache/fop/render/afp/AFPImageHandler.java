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
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.Map;

import org.apache.fop.afp.AFPDataObjectInfo;
import org.apache.fop.afp.AFPObjectAreaInfo;
import org.apache.fop.afp.AFPPaintingState;
import org.apache.fop.afp.AFPResourceInfo;
import org.apache.fop.afp.AFPUnitConverter;
import org.apache.fop.render.ImageHandler;

/**
 * A base abstract AFP image handler
 */
public abstract class AFPImageHandler implements ImageHandler {
    private static final int X = 0;
    private static final int Y = 1;

    /** foreign attribute reader */
    private final AFPForeignAttributeReader foreignAttributeReader
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
        Map foreignAttributes = rendererImageInfo.getForeignAttributes();
        AFPResourceInfo resourceInfo
            = foreignAttributeReader.getResourceInfo(foreignAttributes);
        resourceInfo.setUri(rendererImageInfo.getURI());
        dataObjectInfo.setResourceInfo(resourceInfo);

        // set object area
        AFPObjectAreaInfo objectAreaInfo = new AFPObjectAreaInfo();

        Point origin = rendererImageInfo.getOrigin();
        Rectangle2D position = rendererImageInfo.getPosition();
        float srcX = origin.x + (float)position.getX();
        float srcY = origin.y + (float)position.getY();

        AFPRendererContext rendererContext
            = (AFPRendererContext)rendererImageInfo.getRendererContext();
        AFPInfo afpInfo = rendererContext.getInfo();
        AFPPaintingState paintingState = afpInfo.getPaintingState();
        AFPUnitConverter unitConv = paintingState.getUnitConverter();
        int[] coords = unitConv.mpts2units(new float[] {srcX, srcY});
        objectAreaInfo.setX(coords[X]);
        objectAreaInfo.setY(coords[Y]);

        int width = Math.round(unitConv.mpt2units((float)position.getWidth()));
        objectAreaInfo.setWidth(width);

        int height = Math.round(unitConv.mpt2units((float)position.getHeight()));
        objectAreaInfo.setHeight(height);

        int resolution = paintingState.getResolution();
        objectAreaInfo.setHeightRes(resolution);
        objectAreaInfo.setWidthRes(resolution);

        objectAreaInfo.setRotation(paintingState.getRotation());

        dataObjectInfo.setObjectAreaInfo(objectAreaInfo);

        return dataObjectInfo;
    }

    /**
     * Creates the data object information object
     *
     * @return the data object information object
     */
    protected abstract AFPDataObjectInfo createDataObjectInfo();
}
