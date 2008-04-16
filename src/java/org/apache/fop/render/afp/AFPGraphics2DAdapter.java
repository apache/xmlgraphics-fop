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

import java.awt.image.BufferedImage;
import java.io.IOException;

import org.apache.xmlgraphics.image.loader.ImageException;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageManager;
import org.apache.xmlgraphics.image.loader.ImageSessionContext;
import org.apache.xmlgraphics.java2d.Graphics2DImagePainter;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.render.AbstractGraphics2DAdapter;
import org.apache.fop.render.RendererContext;

/**
 * Graphics2DAdapter implementation for AFP.
 */
public class AFPGraphics2DAdapter extends AbstractGraphics2DAdapter {

    /**
     * Main constructor
     */
    public AFPGraphics2DAdapter() {
    }
    
    /** {@inheritDoc} */
    public void paintImage(Graphics2DImagePainter painter, 
            RendererContext context,
            int x, int y, int width, int height) throws IOException {
        RendererContext.RendererContextWrapper wrappedContext
                = new RendererContext.RendererContextWrapper(context);
        AFPRenderer afp = (AFPRenderer)context.getRenderer();
        Boolean grayObj = (Boolean)context.getProperty(AFPRendererContextConstants.AFP_GRAYSCALE);
        boolean gray = (grayObj != null ? grayObj.booleanValue() : false);
        
        FOUserAgent userAgent = context.getUserAgent();
        
        //Paint to a BufferedImage
        int resolution = (int)Math.round(userAgent.getTargetResolution());
        BufferedImage bi = paintToBufferedImage(painter, wrappedContext, resolution, gray, false);
        
        ImageManager manager = userAgent.getFactory().getImageManager();
        ImageSessionContext sessionContext = userAgent.getImageSessionContext();
        
        //TODO: AC - fix
        String uri = null;
        
        try {
            ImageInfo info = manager.getImageInfo(uri, sessionContext);
            java.util.Map foreignAttributes = null;
            afp.drawBufferedImage(info, bi, resolution, x, y, width, height, foreignAttributes);
        } catch (ImageException e) {
        }
    }
}
