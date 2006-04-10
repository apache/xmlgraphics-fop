/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.pdf.PDFConformanceException;

import junit.framework.TestCase;

/**
 * Tests PDF/A-1 functionality.
 */
public class PDFAConformanceTestCase extends TestCase {

    private TransformerFactory tFactory = TransformerFactory.newInstance();
    private FopFactory fopFactory = FopFactory.newInstance();
    private File foBaseDir = new File("test/xml/pdf-a");
    
    /**
     * Main constructor
     * @param name the name of the test case
     */
    public PDFAConformanceTestCase(String name) {
        super(name);
        try {
            fopFactory.setUserConfig(new File("test/test.xconf"));
        } catch (Exception e) {
            throw new RuntimeException("Configuring the FopFactory failed: " + e.getMessage());
        }
    }
    
    /**
     * Convert the test file
     * @param foFile the FO file
     * @param ua the preconfigured user agent
     * @throws Exception if the conversion fails
     */
    protected void convertFO(File foFile, FOUserAgent ua) throws Exception {
        ua.getRendererOptions().put("pdf-a-mode", "PDF/A-1b");
        File outFile = null;
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, ua, baout);
        Transformer transformer = tFactory.newTransformer();
        Source src = new StreamSource(foFile);
        SAXResult res = new SAXResult(fop.getDefaultHandler());
        transformer.transform(src, res);
        if (false) {
            //Write to file for debugging
            outFile = new File(foFile.getParentFile(), foFile.getName() + ".pdf");
            FileUtils.writeByteArrayToFile(outFile, baout.toByteArray());
        }
    }
    
    /**
     * Test exception when PDF/A-1 is enabled and everything is as it should.
     * @throws Exception if the test fails
     */
    public void testAllOk() throws Exception {
        FOUserAgent ua = fopFactory.newFOUserAgent();
        File foFile = new File(foBaseDir, "minimal-pdf-a.fo");
        convertFO(foFile, ua);
    }
    
    /**
     * Test exception when PDF/A-1 is enabled together with encryption.
     * @throws Exception if the test fails
     */
    public void testNoEncryption() throws Exception {
        FOUserAgent ua = fopFactory.newFOUserAgent();
        ua.getRendererOptions().put("owner-password", "mypassword"); //To enabled encryption
        File foFile = new File(foBaseDir, "minimal-pdf-a.fo");
        try {
            convertFO(foFile, ua);
            fail("Expected PDFConformanceException. PDF/A-1 and PDF encryption don't go together.");
        } catch (PDFConformanceException e) {
            //Good!
        }
    }
    
    /**
     * Test exception when PDF/A-1 is enabled and a font is used which is not embedded.
     * @throws Exception if the test fails
     */
    public void testFontNotEmbedded() throws Exception {
        FOUserAgent ua = fopFactory.newFOUserAgent();
        File foFile = new File(foBaseDir, "base14-font.fo");
        try {
            convertFO(foFile, ua);
            fail("Expected PDFConformanceException. PDF/A-1 wants all fonts embedded.");
        } catch (PDFConformanceException e) {
            //Good!
        }
    }
    
    /**
     * Test exception when PDF/A-1 is enabled and an EPS is used.
     * @throws Exception if the test fails
     */
    public void testEPSUsage() throws Exception {
        FOUserAgent ua = fopFactory.newFOUserAgent();
        File foFile = new File(foBaseDir, "with-eps.fo");
        try {
            convertFO(foFile, ua);
            fail("Expected PDFConformanceException. PDF/A-1 does not allow PostScript XObjects.");
        } catch (PDFConformanceException e) {
            //Good!
        }
    }
    
    /**
     * Test exception when PDF/A-1 is enabled and images.
     * @throws Exception if the test fails
     */
    public void testImages() throws Exception {
        FOUserAgent ua = fopFactory.newFOUserAgent();
        File foFile = new File(foBaseDir, "with-rgb-images.fo");
        convertFO(foFile, ua);

        foFile = new File(foBaseDir, "with-cmyk-images.fo");
        try {
            convertFO(foFile, ua);
            fail("Expected PDFConformanceException. PDF/A-1 does not allow PostScript XObjects.");
        } catch (PDFConformanceException e) {
            //Good!
        }
    }
    
}
