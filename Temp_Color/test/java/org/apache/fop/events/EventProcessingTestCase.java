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

import java.io.File;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.io.output.NullOutputStream;

import org.apache.xmlgraphics.util.MimeConstants;

import org.apache.fop.ResourceEventProducer;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.area.AreaEventProducer;
import org.apache.fop.fo.FOValidationEventProducer;
import org.apache.fop.fo.flow.table.TableEventProducer;
import org.apache.fop.fonts.FontEventProducer;
import org.apache.fop.layoutmgr.BlockLevelEventProducer;
import org.apache.fop.layoutmgr.inline.InlineLevelEventProducer;

/**
 * Tests that the event notification system runs smoothly.
 */
public class EventProcessingTestCase extends TestCase {

    private final FopFactory fopFactory = FopFactory.newInstance();

    private final TransformerFactory tFactory = TransformerFactory.newInstance();

    private final File basedir;

    public EventProcessingTestCase(String name) {
        super(name);
        String base = System.getProperty("basedir");
        if (base != null) {
            basedir = new File(base);
        } else {
            basedir = new File(".");
        }
    }

    private void doTest(String filename, String expectedEventID)
            throws FOPException, TransformerException {
        Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, new NullOutputStream());
        EventChecker eventChecker = new EventChecker(expectedEventID);
        fop.getUserAgent().getEventBroadcaster().addEventListener(eventChecker);
        Transformer transformer = tFactory.newTransformer();
        Source src = new StreamSource(new File(basedir, filename));
        Result res = new SAXResult(fop.getDefaultHandler());
        transformer.transform(src, res);
        eventChecker.end();
    }

    public void testArea() throws FOPException, TransformerException {
        doTest("area.fo",
                AreaEventProducer.class.getName() + ".unresolvedIDReferenceOnPage");
    }

    public void testResource() throws FOPException, TransformerException {
        doTest("resource.fo",
                ResourceEventProducer.class.getName() + ".imageNotFound");
    }

    public void testValidation() throws FOPException, TransformerException {
        doTest("validation.fo",
                FOValidationEventProducer.class.getName() + ".invalidPropertyValue");
    }

    public void testTable() throws FOPException, TransformerException {
        doTest("table.fo",
                TableEventProducer.class.getName() + ".noTablePaddingWithCollapsingBorderModel");
    }

    public void testBlockLevel() throws FOPException, TransformerException {
        doTest("block-level.fo",
                BlockLevelEventProducer.class.getName() + ".overconstrainedAdjustEndIndent");
    }

    public void testInlineLevel() throws FOPException, TransformerException {
        doTest("inline-level.fo",
                InlineLevelEventProducer.class.getName() + ".lineOverflows");
    }

    public void testFont() throws FOPException, TransformerException {
        doTest("font.fo",
                FontEventProducer.class.getName() + ".fontSubstituted");
    }


    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(EventProcessingTestCase.class);
        return suite;
    }
}
