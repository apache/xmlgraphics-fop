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

package org.apache.fop.render.pdf;

import java.io.File;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.events.Event;
import org.apache.fop.events.EventListener;
import org.apache.fop.pdf.PDFEncryptionParams;

/**
 * Tests that encryption length is properly set up.
 */
public class PDFRendererConfiguratorTestCase extends TestCase {

    private FOUserAgent foUserAgent;

    private PDFDocumentHandler documentHandler;

    private boolean eventTriggered;

    private class EncryptionEventFilter implements EventListener {

        private final int specifiedEncryptionLength;

        private final int correctedEncryptionLength;

        EncryptionEventFilter(int specifiedEncryptionLength, int correctedEncryptionLength) {
            this.specifiedEncryptionLength = specifiedEncryptionLength;
            this.correctedEncryptionLength = correctedEncryptionLength;
        }

        public void processEvent(Event event) {
            assertEquals(PDFEventProducer.class.getName() + ".incorrectEncryptionLength",
                    event.getEventID());
            assertEquals(specifiedEncryptionLength, event.getParam("originalValue"));
            assertEquals(correctedEncryptionLength, event.getParam("correctedValue"));
            eventTriggered = true;
        }
    }

    /**
     * Non-multiple of 8 should be rounded.
     *
     * @throws Exception if an error occurs
     */
    public void testRoundUp() throws Exception {
        runTest("roundUp", 55, 56);
    }

    /**
     * Non-multiple of 8 should be rounded.
     *
     * @throws Exception if an error occurs
     */
    public void testRoundDown() throws Exception {
        runTest("roundDown", 67, 64);
    }

    /**
     * Encryption length must be at least 40.
     *
     * @throws Exception if an error occurs
     */
    public void testBelow40() throws Exception {
        runTest("below40", 32, 40);
    }

    /**
     * Encryption length must be at most 128.
     *
     * @throws Exception if an error occurs
     */
    public void testAbove128() throws Exception {
        runTest("above128", 233, 128);
    }

    /**
     * A correct value must be properly set up.
     *
     * @throws Exception if an error occurs
     */
    public void testCorrectValue() throws Exception {
        givenAConfigurationFile("correct", new EventListener() {

            public void processEvent(Event event) {
                throw new AssertionFailedError("No event was expected");
            }
        });
        whenCreatingAndConfiguringDocumentHandler();
        thenEncryptionLengthShouldBe(128);

    }

    private void runTest(String configFilename,
            final int specifiedEncryptionLength,
            final int correctedEncryptionLength) throws Exception {
        givenAConfigurationFile(configFilename,
                new EncryptionEventFilter(specifiedEncryptionLength, correctedEncryptionLength));
        whenCreatingAndConfiguringDocumentHandler();
        assertTrue(eventTriggered);
    }

    private void givenAConfigurationFile(String filename, EventListener eventListener)
            throws Exception {
        FopFactory fopFactory = FopFactory.newInstance();
        fopFactory.setUserConfig(new File("test/resources/org/apache/fop/render/pdf/"
                + filename + ".xconf"));
        foUserAgent = fopFactory.newFOUserAgent();
        foUserAgent.getEventBroadcaster().addEventListener(eventListener);
    }

    private void whenCreatingAndConfiguringDocumentHandler() throws FOPException {
        PDFDocumentHandlerMaker maker = new PDFDocumentHandlerMaker();
        documentHandler = (PDFDocumentHandler) maker.makeIFDocumentHandler(foUserAgent);
        new PDFRendererConfigurator(foUserAgent).configure(documentHandler);
    }

    private void thenEncryptionLengthShouldBe(int expectedEncryptionLength) {
        PDFEncryptionParams encryptionParams = documentHandler.getPDFUtil().getEncryptionParams();
        assertEquals(expectedEncryptionLength, encryptionParams.getEncryptionLengthInBits());
    }
}
