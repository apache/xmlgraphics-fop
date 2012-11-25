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

import org.junit.Test;

import org.apache.xmlgraphics.image.writer.Endianness;

import org.apache.fop.apps.FopConfBuilder;
import org.apache.fop.apps.TIFFRendererConfBuilder;
import org.apache.fop.render.bitmap.TIFFRendererConfig.TIFFRendererConfigParser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TIFFRendererConfigParserTestCase
extends AbstractBitmapRendererConfigParserTester {

    public TIFFRendererConfigParserTestCase() {
        super(new TIFFRendererConfigParser());
    }

    @Override
    protected TIFFRendererConfBuilder createRenderer() {
        builder = new FopConfBuilder().setStrictValidation(true).startRendererConfig(
                TIFFRendererConfBuilder.class);
        return (TIFFRendererConfBuilder) builder;
    }

    private TIFFRendererConfig getConfig() {
        return (TIFFRendererConfig) conf;
    }

    @Test
    public void testCompression() throws Exception {
        for (TIFFCompressionValue value : TIFFCompressionValue.values()) {
            parseConfig(createRenderer().setCompressionMode(value.getName()));
            assertEquals(value, getConfig().getCompressionType());
        }
    }

    @Test
    public void testSingleStrip() throws Exception {
        parseConfig(createRenderer().setSingleStrip(true));
        assertTrue(getConfig().isSingleStrip());
        parseConfig(createRenderer().setSingleStrip(false));
        assertFalse(getConfig().isSingleStrip());
    }

    @Test
    public void testEndianness() throws Exception {
        for (Endianness value : Endianness.values()) {
            parseConfig(createRenderer().setEndianness(value.toString()));
            assertEquals(value, getConfig().getEndianness());
        }
    }
}
