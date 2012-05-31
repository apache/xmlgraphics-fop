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

package org.apache.fop.render.ps;

import org.junit.Test;

import org.apache.xmlgraphics.ps.PSGenerator;

import org.apache.fop.apps.AbstractRendererConfigParserTester;
import org.apache.fop.apps.PSRendererConfBuilder;
import org.apache.fop.render.ps.PSRendererConfig.PSRendererConfigParser;

import static org.junit.Assert.assertEquals;

public class PSRendererConfigParserTestCase
        extends AbstractRendererConfigParserTester<PSRendererConfBuilder, PSRendererConfig> {

    public PSRendererConfigParserTestCase() {
        super(new PSRendererConfigParser(), PSRendererConfBuilder.class);
    }

    @Test
    public void testAutoRotateLandscape() throws Exception {
        boolean defaultVal = false;
        boolean configuredVal = !defaultVal;
        parseConfig(createRenderer());
        assertEquals(defaultVal, conf.isAutoRotateLandscape());
        parseConfig(createRenderer().setAutoRotateLandscape(configuredVal));
        assertEquals(configuredVal, conf.isAutoRotateLandscape());
    }

    @Test
    public void testSafeSetPageDevice() throws Exception {
        boolean defaultVal = false;
        boolean configuredVal = !defaultVal;
        parseConfig(createRenderer());
        assertEquals(defaultVal, conf.isSafeSetPageDevice());
        parseConfig(createRenderer().setSafeSetPageDevice(configuredVal));
        assertEquals(configuredVal, conf.isSafeSetPageDevice());
    }

    @Test
    public void testDscCompliant() throws Exception {
        boolean defaultVal = true;
        boolean configuredVal = !defaultVal;
        parseConfig(createRenderer());
        assertEquals(defaultVal, conf.isDscComplianceEnabled());
        parseConfig(createRenderer().setDscCompliant(configuredVal));
        assertEquals(configuredVal, conf.isDscComplianceEnabled());
    }

    @Test
    public void testLanguageLevel() throws Exception {
        Integer defaultVal = PSGenerator.DEFAULT_LANGUAGE_LEVEL;
        Integer configuredVal = defaultVal + 1;
        parseConfig(createRenderer());
        assertEquals(defaultVal, conf.getLanguageLevel());
        parseConfig(createRenderer().setLanguageLevel(configuredVal));
        assertEquals(configuredVal, conf.getLanguageLevel());
    }

    @Test
    public void testOptimizeResources() throws Exception {
        boolean defaultVal = false;
        boolean configuredVal = !defaultVal;
        parseConfig(createRenderer());
        assertEquals(defaultVal, conf.isOptimizeResources());
        parseConfig(createRenderer().setOptimizeResources(configuredVal));
        assertEquals(configuredVal, conf.isOptimizeResources());
    }
}
