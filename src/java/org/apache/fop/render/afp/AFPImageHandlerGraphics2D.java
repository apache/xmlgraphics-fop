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
import java.awt.geom.AffineTransform;
import java.io.IOException;

import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.impl.ImageGraphics2D;
import org.apache.xmlgraphics.java2d.Graphics2DImagePainter;
import org.apache.xmlgraphics.util.MimeConstants;

import org.apache.fop.afp.AFPDataObjectInfo;
import org.apache.fop.afp.AFPGraphics2D;
import org.apache.fop.afp.AFPGraphicsObjectInfo;
import org.apache.fop.afp.AFPPaintingState;
import org.apache.fop.afp.AFPResourceInfo;
import org.apache.fop.afp.AFPResourceManager;
import org.apache.fop.afp.modca.ResourceObject;
import org.apache.fop.render.ImageHandler;
import org.apache.fop.render.ImageHandlerUtil;
import org.apache.fop.render.RenderingContext;

/**
 * PDFImageHandler implementation which handles Graphics2D images.
 */
public class AFPImageHandlerGraphics2D extends AFPImageHandler implements ImageHandler {

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

            setDefaultResourceLevel(graphicsObjectInfo, afpInfo.getResourceManager());

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

    private void setDefaultResourceLevel(AFPGraphicsObjectInfo graphicsObjectInfo,
            AFPResourceManager resourceManager) {
        AFPResourceInfo resourceInfo = graphicsObjectInfo.getResourceInfo();
        if (!resourceInfo.levelChanged()) {
            resourceInfo.setLevel(resourceManager.getResourceLevelDefaults()
                    .getDefaultResourceLevel(ResourceObject.TYPE_GRAPHIC));
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

    /** {@inheritDoc} */
    public void handleImage(RenderingContext context, Image image, Rectangle pos)
            throws IOException {
        AFPRenderingContext afpContext = (AFPRenderingContext)context;

        AFPGraphicsObjectInfo graphicsObjectInfo = (AFPGraphicsObjectInfo)createDataObjectInfo();

        // set resource information
        setResourceInformation(graphicsObjectInfo,
                image.getInfo().getOriginalURI(),
                afpContext.getForeignAttributes());

        // Positioning
        graphicsObjectInfo.setObjectAreaInfo(
                createObjectAreaInfo(afpContext.getPaintingState(), pos));

        setDefaultResourceLevel(graphicsObjectInfo, afpContext.getResourceManager());

        AFPPaintingState paintingState = afpContext.getPaintingState();
        paintingState.save(); // save
        AffineTransform placement = new AffineTransform();
        placement.translate(pos.x, pos.y);
        paintingState.concatenate(placement);

        // Image content
        ImageGraphics2D imageG2D = (ImageGraphics2D)image;
        boolean textAsShapes = false; //TODO Make configurable
        AFPGraphics2D g2d = new AFPGraphics2D(
                textAsShapes,
                afpContext.getPaintingState(),
                afpContext.getResourceManager(),
                graphicsObjectInfo.getResourceInfo(),
                afpContext.getFontInfo());
        g2d.setGraphicContext(new org.apache.xmlgraphics.java2d.GraphicContext());

        graphicsObjectInfo.setGraphics2D(g2d);
        graphicsObjectInfo.setPainter(imageG2D.getGraphics2DImagePainter());

        // Create image
        afpContext.getResourceManager().createObject(graphicsObjectInfo);

        paintingState.restore(); // resume
    }

    /** {@inheritDoc} */
    public boolean isCompatible(RenderingContext targetContext, Image image) {
        boolean supported = (image == null || image instanceof ImageGraphics2D)
                && targetContext instanceof AFPRenderingContext;
        if (supported) {
            String mode = (String)targetContext.getHint(ImageHandlerUtil.CONVERSION_MODE);
            if (ImageHandlerUtil.isConversionModeBitmap(mode)) {
                //Disabling this image handler automatically causes a bitmap to be generated
                return false;
            }
        }
        return supported;
    }
}
