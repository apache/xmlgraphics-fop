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
package org.apache.fop.pdf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.Assert;
import org.junit.Test;

import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.cache.ImageCache;
import org.apache.xmlgraphics.image.loader.impl.ImageRawPNG;
import org.apache.xmlgraphics.image.loader.impl.ImageRawStream;
import org.apache.xmlgraphics.io.Resource;
import org.apache.xmlgraphics.io.ResourceResolver;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryBuilder;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.apps.io.ResourceResolverFactory;

public class PDFImageCacheTestCase {
    @Test
    public void testCache() throws Exception {
        final int[] count = {0};
        ResourceResolver customResolver = new ResourceResolver() {
            ResourceResolver resourceResolver = ResourceResolverFactory.createDefaultResourceResolver();
            public Resource getResource(URI uri) throws IOException {
                count[0]++;
                return resourceResolver.getResource(uri);
            }
            public OutputStream getOutputStream(URI uri) throws IOException {
                return resourceResolver.getOutputStream(uri);
            }
        };
        FopFactoryBuilder builder = new FopFactoryBuilder(new File(".").toURI(), customResolver);
        FopFactory fopFactory = builder.build();
        ByteArrayOutputStream bos = buildPDF(fopFactory);
        Assert.assertTrue(bos.toString().contains("/Subtype /Image"));
        Assert.assertEquals(1, count[0]);
        deleteImageData(fopFactory);
        bos = buildPDF(fopFactory);
        Assert.assertTrue(bos.toString().contains("/Subtype /Image"));
        Assert.assertEquals(1, count[0]);
    }

    private void deleteImageData(FopFactory fopFactory) {
        ImageCache imageCache = fopFactory.newFOUserAgent().getImageManager().getCache();
        Image image = imageCache.getImage("test/resources/images/fop-logo-color-24bit.png", ImageFlavor.RAW_PNG);
        ImageRawPNG raw = new ImageRawPNG(image.getInfo(), null, null, 1, null);
        raw.setInputStreamFactory(new ImageRawStream.ByteArrayStreamFactory(new byte[0]));
        imageCache.putImage(raw);
    }

    private ByteArrayOutputStream buildPDF(FopFactory fopFactory) throws Exception {
        String fo = "<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\">\n"
                + "  <fo:layout-master-set>\n"
                + "    <fo:simple-page-master master-name=\"simple\" page-height=\"27.9cm\" page-width=\"21.6cm\">\n"
                + "      <fo:region-body />\n"
                + "    </fo:simple-page-master>\n"
                + "  </fo:layout-master-set>\n"
                + "  <fo:page-sequence master-reference=\"simple\">\n"
                + "    <fo:flow flow-name=\"xsl-region-body\">\n"
                + "<fo:block><fo:external-graphic src=\"test/resources/images/fop-logo-color-24bit.png\"/></fo:block>\n"
                + "</fo:flow>\n"
                + "  </fo:page-sequence>\n"
                + "</fo:root>\n";
        FOUserAgent userAgent = fopFactory.newFOUserAgent();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, userAgent, bos);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        Source src = new StreamSource(new ByteArrayInputStream(fo.getBytes()));
        Result res = new SAXResult(fop.getDefaultHandler());
        transformer.transform(src, res);
        return bos;
    }
}
