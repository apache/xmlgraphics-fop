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

package org.apache.fop.render.pdf.extensions;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import org.apache.xmlgraphics.util.QName;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.events.EventChecker;
import org.apache.fop.fo.FODocumentParser;
import org.apache.fop.fo.FODocumentParser.FOEventHandlerFactory;
import org.apache.fop.fo.FOEventHandler;
import org.apache.fop.fo.FOValidationEventProducer;
import org.apache.fop.fo.LoadingException;
import org.apache.fop.fotreetest.DummyFOEventHandler;

public class PDFDocumentInformationElementTestCase {

    private FODocumentParser parser = FODocumentParser.newInstance(new FOEventHandlerFactory() {

        public FOEventHandler newFOEventHandler(FOUserAgent foUserAgent) {
            return new DummyFOEventHandler(foUserAgent);
        }
    });

    @Test(expected = LoadingException.class)
    public void illegalChild() throws Exception {
        Map<String, Object> expectedParams = new HashMap<String, Object>();
        expectedParams.put("offendingNode", new QName(PDFElementMapping.NAMESPACE, "dictionary"));
        runTest("invalid-child.fo", FOValidationEventProducer.class.getName() + ".invalidChild", expectedParams);
    }

    @Test
    public void standardKeyword() throws Exception {
        Map<String, Object> expectedParams = new HashMap<String, Object>();
        expectedParams.put("keyword", "Creator");
        runTest("reserved-keyword.fo", PDFExtensionEventProducer.class.getName() + ".reservedKeyword", expectedParams);
    }

    private void runTest(String testCase, String expectedEventKey, Map<String, Object> expectedEventParams)
            throws Exception {
        EventChecker eventChecker = new EventChecker(expectedEventKey, expectedEventParams);
        parser.setEventListener(eventChecker);
        try {
            parser.parse(new FileInputStream("test/pdf/extensions/document-information/" + testCase));
        } finally {
            eventChecker.end();
        }
    }
}
