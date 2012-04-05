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

package org.apache.fop.afp;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.transform.TransformerException;

import org.junit.Test;
import org.xml.sax.SAXException;

import org.apache.xmlgraphics.util.MimeConstants;

import org.apache.fop.apps.FOPException;
import org.apache.fop.events.EventProcessingTestCase;

/**
 * A test class for testing AFP events.
 */
public class AFPEventProcessingTestCase {

    private EventProcessingTestCase eventsTests = new EventProcessingTestCase();
    private static final String CONFIG_BASE_DIR = EventProcessingTestCase.CONFIG_BASE_DIR;

    private void testInvalidConfigEvent(String xconf, String eventId)
            throws FOPException, TransformerException, IOException, SAXException {
        InputStream inStream = getClass().getResourceAsStream("simple.fo");
        eventsTests.doTest(inStream, CONFIG_BASE_DIR + xconf,
                AFPEventProducer.class.getName() + eventId, MimeConstants.MIME_AFP);
    }

    @Test
    public void testMissingFontConfigurationElement() throws FOPException, TransformerException,
            IOException, SAXException {
        testInvalidConfigEvent("afp-font-missing.xconf", ".fontConfigMissing");
    }

    @Test
    public void testInvalidCharactersetName() throws FOPException, TransformerException,
            IOException, SAXException {
        testInvalidConfigEvent("afp-invalid-characterset.xconf", ".characterSetNameInvalid");
    }

    @Test
    public void testinvalidConfig() throws FOPException, TransformerException, IOException,
            SAXException {
        testInvalidConfigEvent("afp-invalid-config.xconf", ".invalidConfiguration");
    }

    @Test
    public void testRasterFontElementMissing() throws FOPException, TransformerException,
            IOException, SAXException {
        testInvalidConfigEvent("afp-raster-font-missing.xconf", ".fontConfigMissing");
    }

    @Test
    public void testTripletElementMissing() throws FOPException, TransformerException,
            IOException, SAXException {
        testInvalidConfigEvent("afp-triplet-missing.xconf", ".fontConfigMissing");
    }
}
