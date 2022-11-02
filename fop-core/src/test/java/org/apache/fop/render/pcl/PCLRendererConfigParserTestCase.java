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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import org.apache.fop.apps.AbstractRendererConfigParserTester;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.render.pcl.PCLRendererConfig.PCLRendererConfigParser;

public class PCLRendererConfigParserTestCase
        extends AbstractRendererConfigParserTester<PCLRendererConfBuilder, PCLRendererConfig> {

    public PCLRendererConfigParserTestCase() {
        super(new PCLRendererConfigParser(), PCLRendererConfBuilder.class);
    }

    @Test
    public void testGetMimeType() throws Exception {
        assertEquals(MimeConstants.MIME_PCL, new PCLRendererConfigParser().getMimeType());
    }

    @Test
    public void testRenderingMode() throws Exception {
        parseConfig();
        assertEquals(null, conf.getRenderingMode());
        parseConfig(createRenderer().setRenderingMode(PCLRenderingMode.QUALITY));
        assertEquals(PCLRenderingMode.QUALITY, conf.getRenderingMode());
    }

    @Test(expected = FOPException.class)
    public void testRenderingModeException() throws Exception {
        parseConfig(createRenderer().setRenderingMode("whatever"));
    }

    @Test
    public void testTextRendering() throws Exception {
        parseConfig();
        assertEquals(false, conf.isTextRendering());
        parseConfig(createRenderer().setTextRendering("auto"));
        assertEquals(false, conf.isTextRendering());
        parseConfig(createRenderer().setTextRendering("bitmap"));
        assertEquals(true, conf.isTextRendering());
    }

    @Test(expected = FOPException.class)
    public void testTextRenderingException() throws Exception {
        parseConfig(createRenderer().setTextRendering("whatever"));
    }

    @Test
    public void testDisablePJL() throws Exception {
        parseConfig();
        assertEquals(false, conf.isDisablePjl());
        parseConfig(createRenderer().setDisablePjl(true));
        assertEquals(true, conf.isDisablePjl());
    }
}
