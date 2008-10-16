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
import java.io.IOException;

import org.apache.xmlgraphics.image.loader.impl.ImageGraphics2D;
import org.apache.xmlgraphics.java2d.Graphics2DImagePainter;


/**
 * An AFP image graphics 2d factory
 */
public class AFPImageGraphics2DFactory extends AFPDataObjectInfoFactory {

    /**
     * Main constructor
     *
     * @param state the afp state
     */
    public AFPImageGraphics2DFactory(AFPState state) {
        super(state);
    }

    /** {@inheritDoc} */
    protected AFPDataObjectInfo createDataObjectInfo() {
        return new AFPGraphicsObjectInfo();
    }

    /** {@inheritDoc} */
    public AFPDataObjectInfo create(AFPImageInfo afpImageInfo) throws IOException {
        AFPGraphicsObjectInfo graphicsObjectInfo
            = (AFPGraphicsObjectInfo)super.create(afpImageInfo);

        AFPGraphics2DAdapter g2dAdapter = afpImageInfo.g2dAdapter;
        AFPGraphics2D g2d = g2dAdapter.getGraphics2D();
        AFPInfo afpInfo = AFPSVGHandler.getAFPInfo(afpImageInfo.rendererContext);
        g2d.setAFPInfo(afpInfo);
        g2d.setGraphicContext(new org.apache.xmlgraphics.java2d.GraphicContext());
        g2d.setState(state);
        graphicsObjectInfo.setGraphics2D(g2d);

        ImageGraphics2D imageG2D = (ImageGraphics2D)afpImageInfo.img;
        Graphics2DImagePainter painter = imageG2D.getGraphics2DImagePainter();
        graphicsObjectInfo.setPainter(painter);

        AFPObjectAreaInfo objectAreaInfo = graphicsObjectInfo.getObjectAreaInfo();
        Rectangle area = new Rectangle(objectAreaInfo.getWidth(), objectAreaInfo.getHeight());
        graphicsObjectInfo.setArea(area);

        return graphicsObjectInfo;
    }

}
