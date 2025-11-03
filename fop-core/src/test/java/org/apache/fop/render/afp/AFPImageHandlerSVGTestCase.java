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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Base64;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.apache.commons.io.IOUtils;

import org.apache.batik.anim.dom.SVGDOMImplementation;

import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageSize;
import org.apache.xmlgraphics.image.loader.impl.ImageXMLDOM;

import org.apache.fop.afp.AFPPaintingState;
import org.apache.fop.afp.AFPResourceLevel;
import org.apache.fop.afp.AFPResourceManager;
import org.apache.fop.afp.modca.ResourceObject;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.apps.io.ResourceResolverFactory;
import org.apache.fop.image.loader.batik.BatikImageFlavors;

public class AFPImageHandlerSVGTestCase {

    private Document createSVGDocument(float width, float height) throws IOException {
        DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
        Document doc = impl.createDocument(SVGDOMImplementation.SVG_NAMESPACE_URI, "svg", null);
        Element svgRoot = doc.getDocumentElement();
        svgRoot.setAttributeNS(null, "width", "" + width);
        svgRoot.setAttributeNS(null, "height", "" + height);
        Element e = doc.createElementNS(SVGDOMImplementation.SVG_NAMESPACE_URI, "svg:image");
        String img = Base64.getEncoder().encodeToString(IOUtils.toByteArray(
                new FileInputStream("test/resources/images/cmyk.jpg")));
        e.setAttributeNS("http://www.w3.org/1999/xlink", "xlink:href", "data:image/jpeg;base64," + img);
        e.setAttribute("width", "1px");
        e.setAttribute("height", "1px");
        svgRoot.appendChild(e);
        return doc;
    }

    @Test
    public void testAFPImageHandlerSVG() throws Exception {
        String out = drawSVG(false);
        Assert.assertEquals(out, "BEGIN RESOURCE_GROUP RG000001\n"
                + "BEGIN NAME_RESOURCE RES00001 Triplets: OBJECT_FUNCTION_SET_SPECIFICATION,\n"
                + "BEGIN IMAGE IMG00001\n"
                + "BEGIN OBJECT_ENVIRONMENT_GROUP OEG00002\n"
                + "DESCRIPTOR OBJECT_AREA Triplets: DESCRIPTOR_POSITION,MEASUREMENT_UNITS,OBJECT_AREA_SIZE,\n"
                + "POSITION OBJECT_AREA\n"
                + "MAP IMAGE Triplets: MAPPING_OPTION,\n"
                + "DESCRIPTOR IMAGE\n"
                + "END OBJECT_ENVIRONMENT_GROUP OEG00002\n"
                + "DATA IMAGE\n"
                + "END IMAGE IMG00001\n"
                + "END NAME_RESOURCE RES00001\n");
    }

    private String drawSVG(boolean svgPrintFileLevel) throws Exception {
        AFPImageHandlerSVG handler = new AFPImageHandlerSVG();
        FOUserAgent agent = FopFactory.newInstance(new URI(".")).newFOUserAgent();
        InternalResourceResolver resourceResolver =
                ResourceResolverFactory.createDefaultInternalResourceResolver(new URI("."));
        AFPResourceManager manager = new AFPResourceManager(resourceResolver);
        manager.getResourceLevelDefaults().setDefaultResourceLevel(ResourceObject.TYPE_GRAPHIC,
                new AFPResourceLevel(AFPResourceLevel.ResourceType.PRINT_FILE));
        if (svgPrintFileLevel) {
            manager.getResourceLevelDefaults().setDefaultResourceLevel("svg",
                    new AFPResourceLevel(AFPResourceLevel.ResourceType.PRINT_FILE));
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        manager.createDataStream(null, stream).startPage(1, 1, 0, 1, 1);
        AFPRenderingContext context =
                new AFPRenderingContext(agent, manager, new AFPPaintingState(), null, new HashMap());
        context.getPaintingState().setWrapGocaPSeg(true);
        Document doc = createSVGDocument(1, 2);
        ImageInfo info = new ImageInfo("", "");
        info.setSize(new ImageSize());
        ImageXMLDOM img = new ImageXMLDOM(info, doc, BatikImageFlavors.SVG_DOM);
        handler.handleImage(context, img, new Rectangle());
        StringBuilder sb = new StringBuilder();
        InputStream bis = new ByteArrayInputStream(stream.toByteArray());
        new AFPParser(false).read(bis, sb);
        return sb.toString();
    }

    @Test
    public void testAFPImageHandlerSVGResourceLevel() throws Exception {
        String out = drawSVG(true);
        Assert.assertEquals(out, "BEGIN RESOURCE_GROUP RG000001\n"
                + "BEGIN NAME_RESOURCE RES00001 Triplets: OBJECT_FUNCTION_SET_SPECIFICATION,\n"
                + "BEGIN IMAGE IMG00001\n"
                + "BEGIN OBJECT_ENVIRONMENT_GROUP OEG00002\n"
                + "DESCRIPTOR OBJECT_AREA Triplets: DESCRIPTOR_POSITION,MEASUREMENT_UNITS,OBJECT_AREA_SIZE,\n"
                + "POSITION OBJECT_AREA\n"
                + "MAP IMAGE Triplets: MAPPING_OPTION,\n"
                + "DESCRIPTOR IMAGE\n"
                + "END OBJECT_ENVIRONMENT_GROUP OEG00002\n"
                + "DATA IMAGE\n"
                + "END IMAGE IMG00001\n"
                + "END NAME_RESOURCE RES00001\n"
                + "BEGIN NAME_RESOURCE RES00002 Triplets: OBJECT_FUNCTION_SET_SPECIFICATION,\n"
                + "BEGIN PAGE_SEGMENT S1000001\n"
                + "BEGIN GRAPHICS S1000001\n"
                + "BEGIN OBJECT_ENVIRONMENT_GROUP OEG00001\n"
                + "DESCRIPTOR OBJECT_AREA Triplets: DESCRIPTOR_POSITION,MEASUREMENT_UNITS,OBJECT_AREA_SIZE,\n"
                + "POSITION OBJECT_AREA\n"
                + "DESCRIPTOR GRAPHICS\n"
                + "END OBJECT_ENVIRONMENT_GROUP OEG00001\n"
                + "END GRAPHICS S1000001\n"
                + "END PAGE_SEGMENT S1000001\n"
                + "END NAME_RESOURCE RES00002\n");
    }
}
