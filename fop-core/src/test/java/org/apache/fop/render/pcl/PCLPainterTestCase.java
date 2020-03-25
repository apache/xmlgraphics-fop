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
package org.apache.fop.render.pcl;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.transform.stream.StreamResult;

import org.junit.Assert;
import org.junit.Test;

import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageSize;
import org.apache.xmlgraphics.image.loader.impl.ImageGraphics2D;
import org.apache.xmlgraphics.java2d.Graphics2DImagePainter;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.fonts.EmbeddingMode;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontType;
import org.apache.fop.fonts.MultiByteFont;
import org.apache.fop.render.intermediate.IFContext;
import org.apache.fop.render.intermediate.IFException;
import org.apache.fop.render.java2d.CustomFontMetricsMapper;



public class PCLPainterTestCase {
    private FOUserAgent ua = FopFactory.newInstance(new File(".").toURI()).newFOUserAgent();

    @Test
    public void testFillRect() throws IFException {
        Rectangle size = new Rectangle(1, 1);
        PCLPageDefinition pclPageDef = new PCLPageDefinition("", 0, new Dimension(), size, true);
        PCLDocumentHandler documentHandler = new PCLDocumentHandler(new IFContext(ua));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        documentHandler.setResult(new StreamResult(output));
        documentHandler.startDocument();
        PCLPainter pclPainter = new PCLPainter(documentHandler, pclPageDef);
        pclPainter.fillRect(size, Color.RED);
        Assert.assertTrue(output.toString().contains("*c4Q\u001B*c0.01h0.01V\u001B*c32G\u001B*c4P"));
        output.reset();

        pclPainter.getPCLUtil().setColorEnabled(true);
        pclPainter.fillRect(size, Color.RED);
        Assert.assertFalse(output.toString().contains("*c4P"));
        Assert.assertTrue(output.toString().contains("*v255a0b0c0I\u001B*v0S\u001B*c0.01h0.01V\u001B*c0P"));
    }

    @Test
    public void testDrawImage() throws IFException {
        Rectangle size = new Rectangle(1, 1);
        PCLPageDefinition pclPageDef = new PCLPageDefinition("", 0, new Dimension(), size, true);
        PCLDocumentHandler documentHandler = new PCLDocumentHandler(new IFContext(ua));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        documentHandler.setResult(new StreamResult(output));
        documentHandler.startDocument();
        PCLPainter pclPainter = new PCLPainter(documentHandler, pclPageDef);
        pclPainter.getPCLUtil().setColorEnabled(true);
        pclPainter.drawImage("test/resources/images/fop-logo-color-24bit.png", new Rectangle(100, 100));
        Assert.assertTrue(output.toString(), output.toString().contains("*r0f1t1S"));
    }

    @Test
    public void testDrawGraphics() throws IOException, IFException {
        Rectangle size = new Rectangle(1, 1);
        PCLPageDefinition pclPageDef = new PCLPageDefinition("", 0, new Dimension(), size, true);
        PCLDocumentHandler documentHandler = new PCLDocumentHandler(new IFContext(ua));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        documentHandler.setResult(new StreamResult(output));
        documentHandler.startDocument();
        PCLPainter pclPainter = new PCLPainter(documentHandler, pclPageDef);
        PCLImageHandlerGraphics2D graphics2D = new PCLImageHandlerGraphics2D();
        ImageInfo info = new ImageInfo(null, null);
        info.setSize(new ImageSize());
        ImageGraphics2D imageGraphics2D = new ImageGraphics2D(info, new MyGraphics2DImagePainter());
        graphics2D.handleImage(pclPainter.createRenderingContext(), imageGraphics2D, new Rectangle(50, 100));
        Assert.assertTrue(output.toString().contains("*c0.5x1Y"));
        output.reset();
        pclPainter.startGroup(AffineTransform.getRotateInstance(-Math.PI / 2), null);
        graphics2D.handleImage(pclPainter.createRenderingContext(), imageGraphics2D, new Rectangle(50, 100));
        Assert.assertTrue(output.toString().contains("*c1x0.5Y"));
    }

    static class MyGraphics2DImagePainter implements Graphics2DImagePainter {
        public void paint(Graphics2D g2d, Rectangle2D area) {
        }
        public Dimension getImageSize() {
            return null;
        }
    }

    @Test
    public void testTruetype() throws IFException, IOException, FontFormatException, URISyntaxException {
        String optimizeResources = getPCL(true).toString();
        String notOptimizeResources = getPCL(false).toString();
        Assert.assertTrue(notOptimizeResources.contains("DejaVu"));
        Assert.assertFalse(optimizeResources.contains("DejaVu"));
        Assert.assertTrue(optimizeResources.length() > 900);
    }

    private ByteArrayOutputStream getPCL(boolean optimizeResources)
            throws IFException, URISyntaxException, IOException, FontFormatException {
        Rectangle size = new Rectangle(1, 1);
        PCLPageDefinition pclPageDef = new PCLPageDefinition("", 0, new Dimension(), size, true);
        PCLDocumentHandler documentHandler = new PCLDocumentHandler(new IFContext(ua));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        documentHandler.setResult(new StreamResult(output));
        documentHandler.startDocument();
        PCLPainter pclPainter = new PCLPainter(documentHandler, pclPageDef);
        pclPainter.getPCLUtil().setOptimizeResources(optimizeResources);
        FontInfo fi = new FontInfo();
        fi.addFontProperties("", "", "", 0);
        MultiByteFont mbf = new MultiByteFont(ua.getResourceResolver(), EmbeddingMode.AUTO);
        mbf.setEmbedURI(new URI("test/resources/fonts/ttf/DejaVuLGCSerif.ttf"));
        mbf.setFontType(FontType.TRUETYPE);
        fi.addMetrics("", new CustomFontMetricsMapper(mbf));
        documentHandler.setFontInfo(fi);
        pclPainter.setFont("", "", 0, "", 0, Color.BLACK);
        pclPainter.drawText(0, 0, 0, 0, null, "test");
        return output;
    }

}
