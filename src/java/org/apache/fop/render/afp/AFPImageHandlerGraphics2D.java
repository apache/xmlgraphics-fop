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

import java.io.IOException;

import org.apache.fop.afp.AFPDataObjectInfo;
import org.apache.fop.afp.AFPGraphics2D;
import org.apache.fop.afp.AFPGraphicsObjectInfo;
import org.apache.fop.afp.AFPPaintingState;
import org.apache.fop.afp.AFPResourceInfo;
import org.apache.fop.afp.AFPResourceLevel;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.impl.ImageGraphics2D;
import org.apache.xmlgraphics.java2d.Graphics2DImagePainter;
import org.apache.xmlgraphics.util.MimeConstants;

/**
 * PDFImageHandler implementation which handles Graphics2D images.
 */
public class AFPImageHandlerGraphics2D extends AFPImageHandler {

    private static final ImageFlavor[] FLAVORS = new ImageFlavor[] {
        ImageFlavor.GRAPHICS2D
    };

    /** {@inheritDoc} */
    public AFPDataObjectInfo generateDataObjectInfo(
            AFPRendererImageInfo rendererImageInfo) throws IOException {

        AFPRendererContext rendererContext
            = (AFPRendererContext)rendererImageInfo.getRendererContext();
        AFPInfo afpInfo = rendererContext.getInfo();
        ImageGraphics2D imageG2D = (ImageGraphics2D)rendererImageInfo.getImage();
        Graphics2DImagePainter painter = imageG2D.getGraphics2DImagePainter();

        if (afpInfo.paintAsBitmap()) {
            int x = afpInfo.getX();
            int y = afpInfo.getY();
            int width = afpInfo.getWidth();
            int height = afpInfo.getHeight();
            AFPPaintingState paintingState = afpInfo.getPaintingState();
            AFPGraphics2DAdapter g2dAdapter = new AFPGraphics2DAdapter(paintingState);
            g2dAdapter.paintImage(painter, rendererContext, x, y, width, height);
            return null;
        } else {
            AFPGraphicsObjectInfo graphicsObjectInfo
                = (AFPGraphicsObjectInfo)super.generateDataObjectInfo(rendererImageInfo);

            AFPResourceInfo resourceInfo = graphicsObjectInfo.getResourceInfo();
            //level not explicitly set/changed so default to inline for GOCA graphic objects
            // (due to a bug in the IBM AFP Workbench Viewer (2.04.01.07), hard copy works just fine)
            if (!resourceInfo.levelChanged()) {
                resourceInfo.setLevel(new AFPResourceLevel(AFPResourceLevel.INLINE));
            }

            // set mime type (unsupported by MOD:CA registry)
            graphicsObjectInfo.setMimeType(MimeConstants.MIME_AFP_GOCA);

            // set g2d
            boolean textAsShapes = false;

            AFPGraphics2D g2d = afpInfo.createGraphics2D(textAsShapes);

            graphicsObjectInfo.setGraphics2D(g2d);

            // set painter
            graphicsObjectInfo.setPainter(painter);

            return graphicsObjectInfo;
        }
    }

    /** {@inheritDoc} */
    public int getPriority() {
        return 200;
    }

    /** {@inheritDoc} */
    public Class getSupportedImageClass() {
        return ImageGraphics2D.class;
    }

    /** {@inheritDoc} */
    public ImageFlavor[] getSupportedImageFlavors() {
        return FLAVORS;
    }

    /** {@inheritDoc} */
    protected AFPDataObjectInfo createDataObjectInfo() {
        return new AFPGraphicsObjectInfo();
    }
}
