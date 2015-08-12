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
import java.io.FileInputStream;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.commons.io.output.NullOutputStream;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.fo.FOValidationEventProducer;

public class FlowNamedNotMappedTestCase {


    private static class FlowNameNotMappedEventChecker implements EventListener  {
        private final String personalID = FOValidationEventProducer.class.getName() + ".flowNameNotMapped";

        public void processEvent(Event event) {
                Map<String, Object> t =  event.getParams();
                assertEquals("fo:flow", event.getParam("elementName"));
                assertEquals("ContentPage_Body", event.getParam("flowName"));
                assertEquals(personalID, event.getEventID());
        }
    }

    @Test
    public void testFlowNamedNotMapped() throws Exception {
        FlowNameNotMappedEventChecker flowChecker;
        Fop fop;
        FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI());
        FOUserAgent userAgent = fopFactory.newFOUserAgent();
        flowChecker = new FlowNameNotMappedEventChecker();
        userAgent.getEventBroadcaster().addEventListener(flowChecker);
        fop = fopFactory.newFop(MimeConstants.MIME_PDF, userAgent, new NullOutputStream());
        Source src = new StreamSource(new FileInputStream("test/events/flowNameNotMapped.fo"));
        SAXResult res = new SAXResult(fop.getDefaultHandler());
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        String expected = "on fo:flow could not be mapped to a region-name in the layout-master-set.";
        String test =  "";
        try {
            transformer.transform(src, res);
        } catch (TransformerException te) {
            test = te.getLocalizedMessage();
        }
        assertTrue(test.contains(expected));
    }
}
