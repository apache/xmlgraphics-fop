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

package org.apache.fop.render.ps;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.apache.fop.apps.AbstractRendererConfiguratorTest;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.apps.PSRendererConfBuilder;
import org.apache.fop.render.ps.PSRendererConfig.PSRendererConfigParser;

public class PSRendererConfiguratorTestCase extends
        AbstractRendererConfiguratorTest<PSRendererConfigurator, PSRendererConfBuilder> {
    private PSRenderingUtil psUtil;

    public PSRendererConfiguratorTestCase() {
        super(MimeConstants.MIME_POSTSCRIPT, PSRendererConfBuilder.class, PSDocumentHandler.class);
    }

    @Override
    public PSRendererConfigurator createConfigurator() {
        return new PSRendererConfigurator(userAgent, new PSRendererConfigParser());
    }

    @Override
    public void setUpDocumentHandler() {
        psUtil = new PSRenderingUtil(userAgent);
        when(((PSDocumentHandler) docHandler).getPSUtil()).thenReturn(psUtil);
    }

    @Test
    public void testAutoRotateLandscape() throws Exception {
        parseConfig(createBuilder().setAutoRotateLandscape(true));
        assertTrue(psUtil.isAutoRotateLandscape());

        parseConfig(createBuilder().setAutoRotateLandscape(false));
        assertFalse(psUtil.isAutoRotateLandscape());

        parseConfig(createBuilder());
        assertFalse(psUtil.isAutoRotateLandscape());
    }

    @Test
    public void testLanguageLevel() throws Exception {
        parseConfig(createBuilder().setLanguageLevel(2));
        assertEquals(2, psUtil.getLanguageLevel());

        parseConfig(createBuilder().setLanguageLevel(3));
        assertEquals(3, psUtil.getLanguageLevel());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLanguageLevelTestCase() throws Exception {
        parseConfig(createBuilder().setLanguageLevel(1));
        assertEquals(1, psUtil.getLanguageLevel());
    }

    @Test
    public void testOptimizeResources() throws Exception {
        parseConfig(createBuilder().setOptimizeResources(true));
        assertTrue(psUtil.isOptimizeResources());

        parseConfig(createBuilder().setOptimizeResources(false));
        assertFalse(psUtil.isOptimizeResources());

        parseConfig(createBuilder());
        assertFalse(psUtil.isOptimizeResources());
    }

    @Test
    public void testSafeSetPageDevice() throws Exception {
        parseConfig(createBuilder().setSafeSetPageDevice(true));
        assertTrue(psUtil.isSafeSetPageDevice());

        parseConfig(createBuilder().setSafeSetPageDevice(false));
        assertFalse(psUtil.isSafeSetPageDevice());

        parseConfig(createBuilder());
        assertFalse(psUtil.isSafeSetPageDevice());
    }

    @Test
    public void testDscComplianceEnabled() throws Exception {
        parseConfig(createBuilder().setDscCompliant(true));
        assertTrue(psUtil.isDSCComplianceEnabled());

        parseConfig(createBuilder().setDscCompliant(false));
        assertFalse(psUtil.isDSCComplianceEnabled());

        parseConfig(createBuilder());
        assertTrue(psUtil.isDSCComplianceEnabled());
    }
}
