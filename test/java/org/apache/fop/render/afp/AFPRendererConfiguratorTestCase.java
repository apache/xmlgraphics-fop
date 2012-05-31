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

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.xml.sax.SAXException;

import org.apache.fop.afp.AFPPaintingState;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.render.afp.AFPRendererConfig.AFPRendererConfigParser;
import org.apache.fop.render.intermediate.IFContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Test case for {@link AFPRendererConfigurator}.
 */
public class AFPRendererConfiguratorTestCase {
    private FOUserAgent userAgent;

    private AFPRendererConfigurator sut;

    /**
     * Assigns an FOUserAgen with a config file at <code>uri</code>
     *
     * @param uri the URI of the config file
     */
    private void setConfigFile(String uri) {
        String confTestsDir = "test/resources/conf/afp/";
        try {
            userAgent = FopFactory.newInstance(new File(confTestsDir + uri)).newFOUserAgent();
            sut = new AFPRendererConfigurator(userAgent, new AFPRendererConfigParser());
        } catch (IOException ioe) {
            fail("IOException: " + ioe);
        } catch (SAXException se) {
            fail("SAXException: " + se);
        }
    }

    /**
     * Test several config files relating to JPEG images in AFP.
     *
     * @throws FOPException if an error is thrown
     */
    @Test
    public void testJpegImageConfig() throws FOPException {
        testJpegSettings("no_image_config.xconf", 1.0f, false);
        testJpegSettings("can_embed_jpeg.xconf", 1.0f, true);
        testJpegSettings("bitmap_encode_quality.xconf", 0.5f, false);
    }

    private void testJpegSettings(String uri, float bitmapEncodingQual, boolean canEmbed)
            throws FOPException {
        AFPDocumentHandler docHandler = new AFPDocumentHandler(new IFContext(userAgent));

        setConfigFile(uri);
        sut.configure(docHandler);

        AFPPaintingState paintingState = docHandler.getPaintingState();
        assertEquals(bitmapEncodingQual, paintingState.getBitmapEncodingQuality(), 0.01f);
        assertEquals(canEmbed, paintingState.canEmbedJpeg());
    }
}
