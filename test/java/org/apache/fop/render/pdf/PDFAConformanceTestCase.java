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
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.xml.sax.SAXException;

import static org.junit.Assert.fail;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.events.EventChecker;
import org.apache.fop.pdf.PDFAMode;
import org.apache.fop.pdf.PDFConformanceException;
import org.apache.fop.svg.SVGEventProducer;

/**
 * Tests PDF/A-1 functionality.
 */
public class PDFAConformanceTestCase extends BasePDFTest {

    public PDFAConformanceTestCase() throws SAXException, IOException {
        super(getDefaultConfFile());
    }

    private File foBaseDir = new File("test/xml/pdf-a");
    private boolean dumpPDF = Boolean.getBoolean("PDFAConformanceTestCase.dumpPDF");


    /** create an FOUserAgent for our tests
     *  @return an initialized FOUserAgent
     * */
    protected FOUserAgent getUserAgent() {
        final FOUserAgent userAgent = fopFactory.newFOUserAgent();
        userAgent.getRendererOptions().put("pdf-a-mode", "PDF/A-1b");
        return userAgent;
    }

    /**
     * Test exception when PDF/A-1 is enabled and everything is as it should.
     * @throws Exception if the test fails
     */
    @Test
    public void testAllOk() throws Exception {
        File foFile = new File(foBaseDir, "minimal-pdf-a.fo");
        convertFO(foFile, getUserAgent(), dumpPDF);
    }

    /**
     * Test exception when PDF/A-1 is enabled together with encryption.
     * @throws Exception if the test fails
     */
    @Test(expected = PDFConformanceException.class)
    public void testNoEncryption() throws Exception {
        final FOUserAgent ua = getUserAgent();
        ua.getRendererOptions().put("owner-password", "mypassword"); //To enabled encryption
        File foFile = new File(foBaseDir, "minimal-pdf-a.fo");
        convertFO(foFile, ua, dumpPDF);
    }

    /**
     * Test exception when PDF/A-1 is enabled and a font is used which is not embedded.
     * @throws Exception if the test fails
     */
    @Test
    public void testFontNotEmbedded() throws Exception {
        File foFile = new File(foBaseDir, "base14-font.fo");
        try {
            convertFO(foFile, getUserAgent(), dumpPDF);
            fail("Expected PDFConformanceException. PDF/A-1 wants all fonts embedded.");
        } catch (PDFConformanceException e) {
            //Good!
        }
    }

    /**
     * Test exception when PDF/A-1 is enabled and images.
     * @throws Exception if the test fails
     */
    @Test
    public void testImages() throws Exception {
        File foFile = new File(foBaseDir, "with-rgb-images.fo");
        convertFO(foFile, getUserAgent(), dumpPDF);

        foFile = new File(foBaseDir, "with-cmyk-images.fo");
        try {
            convertFO(foFile, getUserAgent(), dumpPDF);
            fail("Expected PDFConformanceException."
                    + " PDF/A-1 does not allow mixing DeviceRGB and DeviceCMYK.");
        } catch (PDFConformanceException e) {
            //Good!
        }
    }

    @Test
    public void svgTransparency() throws Exception {
        FOUserAgent ua = getUserAgent();
        EventChecker eventChecker = setupEventChecker(ua, "transparencyIgnored");
        File foFile = new File(foBaseDir, "svg-transparency.fo");
        convertFO(foFile, ua, dumpPDF);
        eventChecker.end();
    }

    @Test
    public void svgContainingBitmapWithTransparency() throws Exception {
        FOUserAgent ua = getUserAgent();
        EventChecker eventChecker = setupEventChecker(ua, "bitmapWithTransparency");
        File foFile = new File(foBaseDir, "svg-with-transparent-image.fo");
        convertFO(foFile, ua, dumpPDF);
        eventChecker.end();
    }

    private EventChecker setupEventChecker(FOUserAgent ua, String expectedEvent) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("pdfProfile", PDFAMode.PDFA_1B);
        EventChecker eventChecker = new EventChecker(SVGEventProducer.class.getName()
                + "." + expectedEvent, params);
        ua.getEventBroadcaster().addEventListener(eventChecker);
        return eventChecker;
    }

}
