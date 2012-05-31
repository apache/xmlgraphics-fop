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

package org.apache.fop.render.bitmap;

import java.awt.image.BufferedImage;

import org.junit.Test;

import org.apache.fop.apps.FopConfBuilder;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.apps.TIFFRendererConfBuilder;
import org.apache.fop.render.bitmap.TIFFRendererConfig.TIFFRendererConfigParser;

import static org.apache.fop.render.bitmap.TIFFCompressionValues.CCITT_T4;
import static org.apache.fop.render.bitmap.TIFFCompressionValues.CCITT_T6;
import static org.junit.Assert.assertEquals;

public class TIFFRendererConfiguratorTestCase extends AbstractBitmapRendererConfiguratorTest {

    public TIFFRendererConfiguratorTestCase() {
        super(MimeConstants.MIME_TIFF, TIFFDocumentHandler.class);
    }

    @Override
    public TIFFRendererConfigurator createConfigurator() {
        return new TIFFRendererConfigurator(userAgent, new TIFFRendererConfigParser());
    }

    @Override
    protected TIFFRendererConfBuilder createBuilder() {
        return new FopConfBuilder().startRendererConfig(TIFFRendererConfBuilder.class);
    }

    @Test
    @Override
    public void testColorModes() throws Exception {
        for (TIFFCompressionValues value : TIFFCompressionValues.values()) {
            parseConfig(createBuilder().setCompressionMode(value.getName()));
            if (value == CCITT_T6 || value == CCITT_T4) {
                assertEquals(BufferedImage.TYPE_BYTE_BINARY, settings.getBufferedImageType());
            } else {
                assertEquals(BufferedImage.TYPE_INT_ARGB, settings.getBufferedImageType());
            }
        }
    }
}
