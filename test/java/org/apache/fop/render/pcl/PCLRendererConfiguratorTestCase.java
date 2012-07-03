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

package org.apache.fop.render.pcl;

import org.junit.Test;

import org.apache.fop.apps.AbstractRendererConfiguratorTest;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.render.pcl.PCLRendererConfig.PCLRendererConfigParser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class PCLRendererConfiguratorTestCase extends
        AbstractRendererConfiguratorTest<PCLRendererConfigurator, PCLRendererConfBuilder> {

    private PCLRenderingUtil pclUtil;

    public PCLRendererConfiguratorTestCase() {
        super(MimeConstants.MIME_PCL, PCLRendererConfBuilder.class, PCLDocumentHandler.class);
    }

    @Override
    public PCLRendererConfigurator createConfigurator() {
        return new PCLRendererConfigurator(userAgent, new PCLRendererConfigParser());
    }

    @Override
    public void setUpDocumentHandler() {
        pclUtil = new PCLRenderingUtil(userAgent);
        when(((PCLDocumentHandler) docHandler).getPCLUtil()).thenReturn(pclUtil);
    }

    @Test
    public void testSetRenderingMode() throws Exception {
        parseConfig(createBuilder().setRenderingMode("bitmap"));
        assertEquals(PCLRenderingMode.BITMAP, pclUtil.getRenderingMode());

        parseConfig(createBuilder().setRenderingMode("quality"));
        assertEquals(PCLRenderingMode.QUALITY, pclUtil.getRenderingMode());

        parseConfig(createBuilder().setRenderingMode("speed"));
        assertEquals(PCLRenderingMode.SPEED, pclUtil.getRenderingMode());

        parseConfig(createBuilder());
        assertEquals(PCLRenderingMode.SPEED, pclUtil.getRenderingMode());
    }

    @Test(expected = FOPException.class)
    public void testRenderingModeFailureCase() throws Exception {
        parseConfig(createBuilder().setRenderingMode("fail"));
        assertEquals(PCLRenderingMode.SPEED, pclUtil.getRenderingMode());
    }

    @Test
    public void testPJLDisabled() throws Exception {
        parseConfig(createBuilder().setDisablePjl(true));
        assertTrue(pclUtil.isPJLDisabled());

        parseConfig(createBuilder().setDisablePjl(false));
        assertFalse(pclUtil.isPJLDisabled());

        parseConfig(createBuilder());
        assertFalse(pclUtil.isPJLDisabled());
    }

    @Test
    public void testSetAllTextAsBitmaps() throws Exception {
        parseConfig(createBuilder().setTextRendering("bitmap"));
        assertTrue(pclUtil.isAllTextAsBitmaps());

        parseConfig(createBuilder().setTextRendering("auto"));
        assertFalse(pclUtil.isAllTextAsBitmaps());

        parseConfig(createBuilder());
        assertFalse(pclUtil.isAllTextAsBitmaps());
    }

    @Test(expected = FOPException.class)
    public void testSetAllTextAsBitmapsFailureCase() throws Exception {
        parseConfig(createBuilder().setTextRendering("fail"));
        assertFalse(pclUtil.isAllTextAsBitmaps());
    }
}
