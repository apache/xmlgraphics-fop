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

package org.apache.fop.events;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.Test;
import org.xml.sax.SAXException;

import org.apache.commons.io.output.NullOutputStream;

import org.apache.xmlgraphics.util.MimeConstants;

import org.apache.fop.ResourceEventProducer;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.area.AreaEventProducer;
import org.apache.fop.fo.FOValidationEventProducer;
import org.apache.fop.fo.flow.table.TableEventProducer;
import org.apache.fop.layoutmgr.BlockLevelEventProducer;
import org.apache.fop.layoutmgr.inline.InlineLevelEventProducer;

/**
 * Tests that the event notification system runs smoothly.
 */
public class EventProcessingTestCase {

    private final FopFactory fopFactory = FopFactory.newInstance();

    private final TransformerFactory tFactory = TransformerFactory.newInstance();

    private static final String BASE_DIR = "test/events/";

    public void doTest(InputStream inStream, String fopConf, String expectedEventID)
            throws FOPException, TransformerException, IOException, SAXException {
        EventChecker eventChecker = new EventChecker(expectedEventID);
        if (fopConf != null) {
            fopFactory.setUserConfig(fopConf);
        }
        FOUserAgent userAgent = fopFactory.newFOUserAgent();
        userAgent.getEventBroadcaster().addEventListener(eventChecker);
        Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, userAgent, new NullOutputStream());
        Transformer transformer = tFactory.newTransformer();
        Source src = new StreamSource(inStream);
        Result res = new SAXResult(fop.getDefaultHandler());
        transformer.transform(src, res);
        eventChecker.end();
    }

    public void doTest(String filename, String expectedEventID) throws
            FOPException, TransformerException, IOException, SAXException {
        doTest(filename, null, expectedEventID);
    }

    public void doTest(String filename, String fopConf, String expectedEventID) throws
            FOPException, TransformerException, IOException, SAXException {
        doTest(new FileInputStream(BASE_DIR + filename), fopConf, expectedEventID);
    }

    @Test
    public void testArea() throws TransformerException, IOException, SAXException {
        doTest("area.fo",
                AreaEventProducer.class.getName() + ".unresolvedIDReferenceOnPage");
    }

    @Test
    public void testResource() throws FOPException, TransformerException, IOException,
            SAXException {
        doTest("resource.fo",
                ResourceEventProducer.class.getName() + ".imageNotFound");
    }

    @Test
    public void testValidation() throws FOPException, TransformerException, IOException,
            SAXException {
        doTest("validation.fo",
                FOValidationEventProducer.class.getName() + ".invalidPropertyValue");
    }

    @Test
    public void testTable() throws FOPException, TransformerException, IOException, SAXException {
        doTest("table.fo",
                TableEventProducer.class.getName() + ".noTablePaddingWithCollapsingBorderModel");
    }

    @Test
    public void testBlockLevel() throws FOPException, TransformerException, IOException,
            SAXException {
        doTest("block-level.fo",
                BlockLevelEventProducer.class.getName() + ".overconstrainedAdjustEndIndent");
    }

    @Test
    public void testInlineLevel() throws FOPException, TransformerException, IOException,
            SAXException {
        doTest("inline-level.fo",
                InlineLevelEventProducer.class.getName() + ".lineOverflows");
    }
}
