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

package org.apache.fop.accessibility.pdf;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.commons.io.output.NullOutputStream;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.events.Event;
import org.apache.fop.events.EventListener;
import org.apache.fop.render.pdf.PDFEventProducer;

public class MissingLanguageWarningTestCase {

    private Fop fop;

    private MissingLanguageEventChecker eventChecker;

    private static class MissingLanguageEventChecker implements EventListener {

        private final String unknownLanguageEventID = PDFEventProducer.class.getName() + ".unknownLanguage";

        private final LinkedList<String> expectedLocations = new LinkedList<String>(
                Arrays.asList("30:37", "34:40"));

        public void processEvent(Event event) {
            if (event.getEventID().equals(unknownLanguageEventID)) {
                assertFalse("Too many unknownLanguage events", expectedLocations.isEmpty());
                assertEquals(expectedLocations.removeFirst(), event.getParam("location"));
            }
        }

        void end() {
            assertTrue("Expected more unknownLanguage events", expectedLocations.isEmpty());
        }

    }

    @Before
    public void setUp() throws Exception {
        FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI());
        FOUserAgent userAgent = fopFactory.newFOUserAgent();
        userAgent.setAccessibility(true);
        eventChecker = new MissingLanguageEventChecker();
        userAgent.getEventBroadcaster().addEventListener(eventChecker);
        fop = fopFactory.newFop(MimeConstants.MIME_PDF, userAgent, new NullOutputStream());
    }

    @Test
    public void testMissingLanguage() throws Exception {
        Source src = new StreamSource(getClass().getResourceAsStream("missing-language.fo"));
        SAXResult res = new SAXResult(fop.getDefaultHandler());
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(src, res);
        eventChecker.end();
    }

}
