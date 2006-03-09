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

package org.apache.fop.intermediate;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.area.AreaTreeModel;
import org.apache.fop.area.AreaTreeParser;
import org.apache.fop.area.RenderPagesModel;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.render.Renderer;
import org.apache.fop.render.xml.XMLRenderer;

//XML Unit 1.0: See http://xmlunit.sourceforge.net (BSD-style License)
import org.custommonkey.xmlunit.XMLTestCase;
import org.w3c.dom.Document;

/**
 * Tests the area tree parser.
 */
public class AreaTreeParserTestCase extends XMLTestCase {

    // configure fopFactory as desired
    private FopFactory fopFactory = FopFactory.newInstance();
    
    private static SAXTransformerFactory tFactory 
            = (SAXTransformerFactory)SAXTransformerFactory.newInstance();
    private static Templates stylesheet = null;
    
    private File mainDir = new File("test/layoutengine");
    private File testDir = new File(mainDir, "standard-testcases");
    
    private String name;
    private File testFile;

    private File outputDir;
    private Document intermediate;
    
    /** @see junit.framework.TestCase#TestCase(String) */
    public AreaTreeParserTestCase(String name) {
        super(name);
    }
    
    /**
     * Constructor for the test suite that is used for each test file.
     * @param testFile the test file to run
     */
    public AreaTreeParserTestCase(File testFile) {
        super(testFile.getName());
        this.testFile = testFile;
    }
 
    private Templates getStylesheet() throws TransformerConfigurationException {
        if (stylesheet == null) {
            File xsltFile = new File(mainDir, "testcase2fo.xsl");
            stylesheet = tFactory.newTemplates(new StreamSource(xsltFile));
        }
        return stylesheet;
    }
    
    /** @see junit.framework.TestCase#setUp() */
    protected void setUp() throws Exception {
        super.setUp();
        String s = System.getProperty("fop.intermediate.outdir");
        if (s != null && s.length() > 0) {
            outputDir = new File(s);
            outputDir.mkdirs();
        }
        File srcFile;
        if (testFile != null) {
            srcFile = testFile;
        } else {
            srcFile = new File(testDir, "block_font-style.xml");
        }
        this.name = srcFile.getName();
        intermediate = buildAreaTreeXML(new StreamSource(srcFile), getStylesheet());
        if (outputDir != null) {
            saveDOM(intermediate, new File(outputDir, name + ".at1.xml"));
        }
    }


    /**
     * Tests the area tree parser by running the parsed area tree again through the area tree
     * renderer. The source and result documents are compared to each other.
     * @throws Exception if the test fails
     */
    public void testParserToAT() throws Exception {
                
        Source src = new DOMSource(intermediate);
        Document doc = parseAndRenderToAreaTree(src);
        if (outputDir != null) {
            File tgtFile = new File(outputDir, name + ".at2.xml");
            saveDOM(doc, tgtFile);
        }
        
        assertXMLEqual(intermediate, doc);
    }
    
    private void saveDOM(Document doc, File tgtFile) throws Exception {
        Transformer transformer = tFactory.newTransformer();
        Source src = new DOMSource(doc);
        Result res = new StreamResult(tgtFile);
        transformer.transform(src, res);
    }

    /**
     * Tests the area tree parser by sending the parsed area tree to the PDF Renderer. Some
     * errors might be caught by the PDFRenderer.
     * @throws Exception if the test fails
     */
    public void testParserToPDF() throws Exception {
        OutputStream out;
        if (outputDir != null) {
            File tgtFile = new File(outputDir, name + ".pdf");
            out = new FileOutputStream(tgtFile);
            out = new BufferedOutputStream(out);
        } else {
            out = new ByteArrayOutputStream();
        }
        try {
            Source src = new DOMSource(intermediate);
            parseAndRender(src, out, MimeConstants.MIME_PDF);
        } finally {
            IOUtils.closeQuietly(out);
        }
    }
    
    private FOUserAgent createUserAgent() {
        FOUserAgent userAgent = fopFactory.newFOUserAgent();
        try {
            userAgent.setBaseURL(testDir.toURL().toExternalForm());
        } catch (MalformedURLException e) {
            //ignore, won't happen
        }
        return userAgent;
    }

    private Document buildAreaTreeXML(Source src, Templates stylesheet) throws Exception {
        Transformer transformer;
        if (stylesheet != null) {
            transformer = stylesheet.newTransformer();
        } else {
            transformer = tFactory.newTransformer();
        }

        //Set up XMLRenderer to render to a DOM
        TransformerHandler handler = tFactory.newTransformerHandler();
        DOMResult domResult = new DOMResult();
        handler.setResult(domResult);
        
        FOUserAgent userAgent = createUserAgent();

        //Create an instance of the target renderer so the XMLRenderer can use its font setup
        Renderer targetRenderer = userAgent.getRendererFactory().createRenderer(
                userAgent, MimeConstants.MIME_PDF); 
        
        XMLRenderer renderer = new XMLRenderer();
        renderer.mimicRenderer(targetRenderer);
        renderer.setContentHandler(handler);
        renderer.setUserAgent(userAgent);

        userAgent.setRendererOverride(renderer);
        
        Fop fop = new Fop(MimeConstants.MIME_FOP_AREA_TREE, userAgent);
        Result res = new SAXResult(fop.getDefaultHandler());
        transformer.transform(src, res);
        
        return (Document)domResult.getNode();
    }
    
    private void parseAndRender(Source src, OutputStream out, String mime) throws Exception {
        AreaTreeParser parser = new AreaTreeParser();
                
        FOUserAgent userAgent = createUserAgent();
        FontInfo fontInfo = new FontInfo();
        AreaTreeModel treeModel = new RenderPagesModel(userAgent, 
                mime, fontInfo, out); 
        parser.parse(src, treeModel, userAgent);
        treeModel.endDocument();
    }
    
    private Document parseAndRenderToAreaTree(Source src) throws Exception {
        AreaTreeParser parser = new AreaTreeParser();
                
        //Set up XMLRenderer to render to a DOM
        TransformerHandler handler = tFactory.newTransformerHandler();
        DOMResult domResult = new DOMResult();
        handler.setResult(domResult);
        XMLRenderer renderer = new XMLRenderer();
        renderer.setContentHandler(handler);

        FOUserAgent userAgent = createUserAgent();
        userAgent.setRendererOverride(renderer);
        renderer.setUserAgent(userAgent);

        FontInfo fontInfo = new FontInfo();
        AreaTreeModel treeModel = new RenderPagesModel(userAgent, 
                MimeConstants.MIME_FOP_AREA_TREE, fontInfo, null); 
        parser.parse(src, treeModel, userAgent);
        treeModel.endDocument();

        return (Document)domResult.getNode();
    }
    
}
