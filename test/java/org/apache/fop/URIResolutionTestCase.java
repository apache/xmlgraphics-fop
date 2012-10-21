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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.xpath.XPathAPI;
import org.apache.xpath.objects.XObject;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryBuilder;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.apps.io.Resource;
import org.apache.fop.apps.io.ResourceResolver;
import org.apache.fop.apps.io.ResourceResolverFactory;
import org.apache.fop.render.xml.XMLRenderer;

import static org.apache.fop.FOPTestUtils.getBaseDir;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests URI resolution facilities.
 */
public class URIResolutionTestCase {

    private SAXTransformerFactory tfactory = (SAXTransformerFactory) SAXTransformerFactory.newInstance();

    private static final File BACKUP_DIR = new File(getBaseDir(), "build/test-results");

    private static FopFactory fopFactory;

    @BeforeClass
    public static void makeDirs() {
        BACKUP_DIR.mkdirs();
        fopFactory = new FopFactoryBuilder(new File(".").getAbsoluteFile().toURI(),
                new CustomURIResolver()).build();
    }

    private static File getTestDir() {
        return new File(getBaseDir(), "test/xml/uri-testing/");
    }

    @Test
    public void innerTestFO1() throws Exception {
        File foFile = new File(getTestDir(), "custom-scheme/only-scheme-specific-part.fo");

        FOUserAgent ua = fopFactory.newFOUserAgent();

        Document doc = createAreaTree(foFile, ua);

        // XPath checking on the area tree
        assertEquals("viewport for external-graphic is missing",
                "true", evalXPath(doc, "boolean(//flow/block[1]/lineArea/viewport)"));
        assertEquals("46080", evalXPath(doc, "//flow/block[1]/lineArea/viewport/@ipd"));
        assertEquals("46080", evalXPath(doc, "//flow/block[1]/lineArea/viewport/@bpd"));
    }

    /**
     * Test custom URI resolution with a hand-written URIResolver.
     * @throws Exception if anything fails
     */
    @Test
    public void testFO2() throws Exception {
        File foFile = new File(getTestDir(), "custom-scheme/only-scheme-specific-part-svg.fo");

        FOUserAgent ua = fopFactory.newFOUserAgent();

        ByteArrayOutputStream baout = new ByteArrayOutputStream();

        Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, ua, baout);

        Transformer transformer = tfactory.newTransformer(); //Identity transf.
        Source src = new StreamSource(foFile);
        Result res = new SAXResult(fop.getDefaultHandler());
        transformer.transform(src, res);

        OutputStream out = new java.io.FileOutputStream(
                new File(BACKUP_DIR, foFile.getName() + ".pdf"));
        try {
            baout.writeTo(out);
        } finally {
            IOUtils.closeQuietly(out);
        }

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

        XMLRenderer atrenderer = new XMLRenderer(ua);
        atrenderer.setContentHandler(athandler);
        ua.setRendererOverride(atrenderer);

        Fop fop = fopFactory.newFop(ua);

        Transformer transformer = tfactory.newTransformer(); //Identity transf.
        Source src = new StreamSource(fo);
        Result res = new SAXResult(fop.getDefaultHandler());
        transformer.transform(src, res);

        Document doc = (Document) domres.getNode();
        saveAreaTreeXML(doc, new File(BACKUP_DIR, fo.getName() + ".at.xml"));
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

    private static final class CustomURIResolver implements ResourceResolver {
        private final ResourceResolver defaultImpl =  ResourceResolverFactory.createDefaultResourceResolver();

        public Resource getResource(URI uri) throws IOException {
            if (uri.getScheme().equals("funky") && uri.getSchemeSpecificPart().equals("myimage123")) {
                return new Resource("", new FileInputStream("test/resources/images/bgimg300dpi.jpg"));
            }

            return defaultImpl.getResource(uri);
        }

        public OutputStream getOutputStream(URI uri) throws IOException {
            return defaultImpl.getOutputStream(uri);
        }

    }
}
