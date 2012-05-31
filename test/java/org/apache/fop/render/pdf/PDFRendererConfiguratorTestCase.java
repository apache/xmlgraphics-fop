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
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.xml.sax.SAXException;

import org.apache.xmlgraphics.util.MimeConstants;

import org.apache.fop.apps.AbstractRendererConfiguratorTest;
import org.apache.fop.apps.FopConfBuilder.RendererConfBuilder;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.PDFRendererConfBuilder;
import org.apache.fop.events.Event;
import org.apache.fop.events.EventListener;
import org.apache.fop.pdf.PDFAMode;
import org.apache.fop.pdf.PDFEncryptionParams;
import org.apache.fop.pdf.PDFXMode;
import org.apache.fop.pdf.Version;
import org.apache.fop.render.pdf.PDFRendererConfig.PDFRendererConfigParser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests that encryption length is properly set up.
 */
public class PDFRendererConfiguratorTestCase extends
        AbstractRendererConfiguratorTest<PDFRendererConfigurator, PDFRendererConfBuilder> {
    private boolean eventTriggered;
    private PDFRenderingUtil pdfUtil;

    public PDFRendererConfiguratorTestCase() {
        super(MimeConstants.MIME_PDF, PDFRendererConfBuilder.class, PDFDocumentHandler.class);
    }

    @Override
    protected PDFRendererConfigurator createConfigurator() {
        return new PDFRendererConfigurator(userAgent, new PDFRendererConfigParser());
    }

    @Override
    public void setUpDocumentHandler() {
        pdfUtil = new PDFRenderingUtil(userAgent);
        when(((PDFDocumentHandler) docHandler).getPDFUtil()).thenReturn(pdfUtil);
    }

    private void parseConfig(RendererConfBuilder builder, EventListener listener)
            throws SAXException, IOException {
        parseConfigWithUtil(builder, listener, false);
    }

    private void parseConfigWithUtil(RendererConfBuilder builder, EventListener listener,
            boolean mockUtil) throws SAXException, IOException {
        userAgent = FopFactory.newInstance(
                new File(".").toURI(), builder.endRendererConfig().build()).newFOUserAgent();
        userAgent.getEventBroadcaster().addEventListener(listener);
        if (mockUtil) {
            this.pdfUtil = mock(PDFRenderingUtil.class);
            when(((PDFDocumentHandler) docHandler).getPDFUtil()).thenReturn(pdfUtil);
        } else {
            setUpDocumentHandler();
        }
        sut = createConfigurator();
        sut.configure(docHandler);
    }

    private void parseConfigMockUtil(RendererConfBuilder builder)
            throws SAXException, IOException {
        parseConfigWithUtil(builder, null, true);
    }

    /**
     * Non-multiple of 8 should be rounded.
     *
     * @throws Exception if an error occurs
     */
    @Test
    public void testRoundUp() throws Exception {
        testEncryptionAndEvent(55, 56);
    }

    /**
     * Non-multiple of 8 should be rounded.
     *
     * @throws Exception if an error occurs
     */
    @Test
    public void testRoundDown() throws Exception {
        testEncryptionAndEvent(67, 64);
    }

    /**
     * Encryption length must be at least 40.
     *
     * @throws Exception if an error occurs
     */
    @Test
    public void testBelow40() throws Exception {
        testEncryptionAndEvent(32, 40);
    }

    /**
     * Encryption length must be at most 128.
     *
     * @throws Exception if an error occurs
     */
    @Test
    public void testAbove128() throws Exception {
        testEncryptionAndEvent(233, 128);
    }

    /**
     * A correct value must be properly set up.
     *
     * @throws Exception if an error occurs
     */
    @Test
    public void testCorrectValue() throws Exception {
        runEncryptionTest(128, 128);
    }

    private void testEncryptionAndEvent(int specifiedEncryptionLength,
            int actualEncryptionLength) throws Exception {
        runEncryptionTest(specifiedEncryptionLength, actualEncryptionLength);
        assertTrue(eventTriggered);
    }

    private void runEncryptionTest(int specifiedEncryptionLength, int actualEncryptionLength)
            throws Exception {
        parseConfig(createBuilder().startEncryptionParams()
                                       .setEncryptionLength(specifiedEncryptionLength)
                                   .endEncryptionParams(),
                new EncryptionEventFilter(specifiedEncryptionLength, actualEncryptionLength));
        thenEncryptionLengthShouldBe(actualEncryptionLength);
    }

    private void thenEncryptionLengthShouldBe(int expectedEncryptionLength) {
        PDFEncryptionParams encryptionParams = pdfUtil.getEncryptionParams();
        assertEquals(expectedEncryptionLength, encryptionParams.getEncryptionLengthInBits());
    }

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

    @Test
    public void testFilterMaps() throws Exception {
        parseConfig(createBuilder().createFilterList("image", "flate", "ascii-85"));
        OutputStream outStream = mock(OutputStream.class);
        Map<String, List<String>> filterMap = pdfUtil.setupPDFDocument(outStream).getFilterMap();
        assertEquals("flate", filterMap.get("image").get(0));
        assertEquals("ascii-85", filterMap.get("image").get(1));
    }

    @Test
    public void testPDFAMode() throws Exception {
        parseConfigMockUtil(createBuilder().setPDFAMode(PDFAMode.DISABLED.getName()));
        // DISABLED is the default setting, it doesn't need to be set
        verify(pdfUtil, times(0)).setAMode(PDFAMode.DISABLED);

        parseConfigMockUtil(createBuilder().setPDFAMode(PDFAMode.PDFA_1A.getName()));
        verify(pdfUtil, times(1)).setAMode(PDFAMode.PDFA_1A);

        parseConfigMockUtil(createBuilder().setPDFAMode(PDFAMode.PDFA_1B.getName()));
        verify(pdfUtil, times(1)).setAMode(PDFAMode.PDFA_1B);
    }

    @Test
    public void testPDFXMode() throws Exception {
        parseConfigMockUtil(createBuilder().setPDFXMode(PDFXMode.DISABLED.getName()));
        // DISABLED is the default setting, it doesn't need to be set
        verify(pdfUtil, times(0)).setXMode(PDFXMode.DISABLED);

        parseConfigMockUtil(createBuilder().setPDFXMode(PDFXMode.PDFX_3_2003.getName()));
        verify(pdfUtil, times(1)).setXMode(PDFXMode.PDFX_3_2003);
    }

    @Test
    public void testSetProfile() throws Exception {
        String testString = "this string is purely for testing and has no contextual meaning";
        parseConfigMockUtil(createBuilder().setOutputProfile(testString));
        verify(pdfUtil).setOutputProfileURI(testString);
    }

    @Test
    public void testDisableSRGBColourspace() throws Exception {
        parseConfigMockUtil(createBuilder().disableSRGBColorSpace(true));
        verify(pdfUtil).setDisableSRGBColorSpace(true);

        parseConfigMockUtil(createBuilder().disableSRGBColorSpace(false));
        verify(pdfUtil, times(0)).setDisableSRGBColorSpace(false);
    }

    @Test
    public void testPDFVersion() throws Exception {
        for (Version version : Version.values()) {
            parseConfigMockUtil(createBuilder().setPDFVersion(version.toString()));
            verify(pdfUtil).setPDFVersion(version);
        }
    }
}
