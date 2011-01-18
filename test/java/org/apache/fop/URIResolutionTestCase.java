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

package org.apache.fop;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.xpath.XPathAPI;
import org.apache.xpath.objects.XObject;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.render.xml.XMLRenderer;

/**
 * Tests URI resolution facilities.
 */
public class URIResolutionTestCase extends AbstractFOPTestCase {

    // configure fopFactory as desired
    private FopFactory fopFactory = FopFactory.newInstance();

    private SAXTransformerFactory tfactory
            = (SAXTransformerFactory)SAXTransformerFactory.newInstance();

    private File backupDir = new File(getBaseDir(), "build/test-results");

    /** @see junit.framework.TestCase#TestCase(String) */
    public URIResolutionTestCase(String name) {
        super(name);
        backupDir.mkdirs();
    }

    /**
     * Test custom URI resolution with a hand-written URIResolver.
     * @throws Exception if anything fails
     */
    public void testFO1a() throws Exception {
        innerTestFO1(false);
    }

    /**
     * Test custom URI resolution with a hand-written URIResolver.
     * @throws Exception if anything fails
     */
    public void testFO1b() throws Exception {
        innerTestFO1(true);
    }

    private void innerTestFO1(boolean withStream) throws Exception {
        FOUserAgent ua = fopFactory.newFOUserAgent();

        File foFile = new File(getBaseDir(), "test/xml/uri-resolution1.fo");

        MyURIResolver resolver = new MyURIResolver(withStream);
        ua.setURIResolver(resolver);
        ua.setBaseURL(foFile.getParentFile().toURI().toURL().toString());

        Document doc = createAreaTree(foFile, ua);

        //Check how many times the resolver was consulted
        assertEquals("Expected resolver to do 1 successful URI resolution",
                1, resolver.successCount);
        assertEquals("Expected resolver to do 0 failed URI resolution",
                0, resolver.failureCount);
        //Additional XPath checking on the area tree
        assertEquals("viewport for external-graphic is missing",
                "true", evalXPath(doc, "boolean(//flow/block[1]/lineArea/viewport)"));
        assertEquals("46080", evalXPath(doc, "//flow/block[1]/lineArea/viewport/@ipd"));
        assertEquals("46080", evalXPath(doc, "//flow/block[1]/lineArea/viewport/@bpd"));
    }

    /**
     * Test custom URI resolution with a hand-written URIResolver.
     * @throws Exception if anything fails
     */
    public void DISABLEDtestFO2() throws Exception {
        //TODO This will only work when we can do URI resolution inside Batik!
        File foFile = new File(getBaseDir(), "test/xml/uri-resolution2.fo");

        FOUserAgent ua = fopFactory.newFOUserAgent();
        MyURIResolver resolver = new MyURIResolver(false);
        ua.setURIResolver(resolver);
        ua.setBaseURL(foFile.getParentFile().toURI().toURL().toString());

        ByteArrayOutputStream baout = new ByteArrayOutputStream();

        Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, ua, baout);

        Transformer transformer = tfactory.newTransformer(); //Identity transf.
        Source src = new StreamSource(foFile);
        Result res = new SAXResult(fop.getDefaultHandler());
        transformer.transform(src, res);

        OutputStream out = new java.io.FileOutputStream(
                new File(backupDir, foFile.getName() + ".pdf"));
        try {
            baout.writeTo(out);
        } finally {
            IOUtils.closeQuietly(out);
        }

        //Check how many times the resolver was consulted
        assertEquals("Expected resolver to do 1 successful URI resolution",
                1, resolver.successCount);
        assertEquals("Expected resolver to do 0 failed URI resolutions",
                0, resolver.failureCount);
        //Test using PDF as the area tree doesn't invoke Batik so we could check
        //if the resolver is actually passed to Batik by FOP
        assertTrue("Generated PDF has zero length", baout.size() > 0);
    }

    private Document createAreaTree(File fo, FOUserAgent ua)
                throws TransformerException, FOPException {
        DOMResult domres = new DOMResult();
        //Setup Transformer to convert the area tree to a DOM
        TransformerHandler athandler = tfactory.newTransformerHandler();
        athandler.setResult(domres);

        XMLRenderer atrenderer = new XMLRenderer();
        atrenderer.setUserAgent(ua);
        atrenderer.setContentHandler(athandler);
        ua.setRendererOverride(atrenderer);

        Fop fop = fopFactory.newFop(ua);

        Transformer transformer = tfactory.newTransformer(); //Identity transf.
        Source src = new StreamSource(fo);
        Result res = new SAXResult(fop.getDefaultHandler());
        transformer.transform(src, res);

        Document doc = (Document)domres.getNode();
        saveAreaTreeXML(doc, new File(backupDir, fo.getName() + ".at.xml"));
        return doc;
    }

    private String evalXPath(Document doc, String xpath) {
        XObject res;
        try {
            res = XPathAPI.eval(doc, xpath);
        } catch (TransformerException e) {
            throw new RuntimeException("XPath evaluation failed: " + e.getMessage());
        }
        return res.str();
    }

    /**
     * Save the area tree XML for later inspection.
     * @param doc area tree as a DOM document
     * @param target target file
     * @throws TransformerException if a problem occurs during serialization
     */
    protected void saveAreaTreeXML(Document doc, File target) throws TransformerException {
        Transformer transformer = tfactory.newTransformer();
        Source src = new DOMSource(doc);
        Result res = new StreamResult(target);
        transformer.transform(src, res);
    }

    private class MyURIResolver implements URIResolver {

        private static final String PREFIX = "funky:";

        private boolean withStream;
        private int successCount = 0;
        private int failureCount = 0;

        public MyURIResolver(boolean withStream) {
            this.withStream = withStream;
        }

        /**
         * @see javax.xml.transform.URIResolver#resolve(java.lang.String, java.lang.String)
         */
        public Source resolve(String href, String base) throws TransformerException {
            if (href.startsWith(PREFIX)) {
                String name = href.substring(PREFIX.length());
                if ("myimage123".equals(name)) {
                    File image = new File(getBaseDir(), "test/resources/images/bgimg300dpi.jpg");
                    Source src;
                    if (withStream) {
                        try {
                            src = new StreamSource(new java.io.FileInputStream(image));
                        } catch (FileNotFoundException e) {
                            throw new TransformerException(e.getMessage(), e);
                        }
                    } else {
                        src = new StreamSource(image);
                    }
                    successCount++;
                    return src;
                } else {
                    failureCount++;
                    throw new TransformerException("funky image not found");
                }
            } else {
                failureCount++;
                return null;
            }
        }

    }

}
