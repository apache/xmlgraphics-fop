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

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.Map;

import org.apache.fop.afp.AFPDataObjectInfo;
import org.apache.fop.render.RendererContext;
import org.apache.fop.render.RendererContextConstants;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.impl.ImageXMLDOM;
import org.w3c.dom.Document;

/**
 * PDFImageHandler implementation which handles XML-based images.
 */
public class AFPImageHandlerXML extends AFPImageHandler {

    private static final ImageFlavor[] FLAVORS = new ImageFlavor[] {
        ImageFlavor.XML_DOM,
    };

    private static final Class[] CLASSES = new Class[] {
        ImageXMLDOM.class,
    };

    /** {@inheritDoc} */
    public AFPDataObjectInfo generateDataObjectInfo(AFPRendererImageInfo rendererImageInfo)
            throws IOException {
        RendererContext rendererContext = rendererImageInfo.getRendererContext();
        AFPRenderer renderer = (AFPRenderer)rendererContext.getRenderer();
        ImageXMLDOM imgXML = (ImageXMLDOM)rendererImageInfo.getImage();
        Document doc = imgXML.getDocument();
        String ns = imgXML.getRootNamespace();
        Map foreignAttributes = (Map)rendererContext.getProperty(
                RendererContextConstants.FOREIGN_ATTRIBUTES);
        Rectangle2D pos = rendererImageInfo.getPosition();
        renderer.renderDocument(doc, ns, pos, foreignAttributes);
        return null;
    }

    /** {@inheritDoc} */
    public int getPriority() {
        return 400;
    }

    /** {@inheritDoc} */
    public Class[] getSupportedImageClasses() {
        return CLASSES;
    }

    /** {@inheritDoc} */
    public ImageFlavor[] getSupportedImageFlavors() {
        return FLAVORS;
    }

    /** {@inheritDoc} */
    protected AFPDataObjectInfo createDataObjectInfo() {
        return null;
    }

}
