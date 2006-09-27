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

package org.apache.fop.layoutengine;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FormattingResults;
import org.apache.fop.layoutmgr.ElementListObserver;
import org.apache.fop.render.xml.XMLRenderer;
import org.apache.xpath.XPathAPI;
import org.apache.xpath.objects.XObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Class for testing the FOP's layout engine using testcases specified in XML
 * files.
 */
public class LayoutEngineTester {

    private static final Map CHECK_CLASSES = new java.util.HashMap();
    
    // configure fopFactory as desired
    private FopFactory fopFactory = FopFactory.newInstance();
    private FopFactory fopFactoryWithBase14Kerning = FopFactory.newInstance();
    
    private SAXTransformerFactory tfactory 
            = (SAXTransformerFactory)SAXTransformerFactory.newInstance();

    private Templates testcase2fo;
    private Templates testcase2checks;
    
    private File areaTreeBackupDir;
    
    static {
        CHECK_CLASSES.put("true", TrueCheck.class);
        CHECK_CLASSES.put("eval", EvalCheck.class);
        CHECK_CLASSES.put("element-list", ElementListCheck.class);
        CHECK_CLASSES.put("result", ResultCheck.class);
    }
    
    /**
     * Constructs a new instance.
     * @param areaTreeBackupDir Optional directory that receives the generated
     *     area tree XML files. May be null.
     */
    public LayoutEngineTester(File areaTreeBackupDir) {
        this.areaTreeBackupDir = areaTreeBackupDir;
        fopFactory.setBase14KerningEnabled(false);
        fopFactoryWithBase14Kerning.setBase14KerningEnabled(true);
    }
    
    private Templates getTestcase2FOStylesheet() throws TransformerConfigurationException {
        if (testcase2fo == null) {
            //Load and cache stylesheet
            Source src = new StreamSource(new File("test/layoutengine/testcase2fo.xsl"));
            testcase2fo = tfactory.newTemplates(src);
        }
        return testcase2fo;
    }
    
    private Templates getTestcase2ChecksStylesheet() throws TransformerConfigurationException {
        if (testcase2checks == null) {
            //Load and cache stylesheet
            Source src = new StreamSource(new File("test/layoutengine/testcase2checks.xsl"));
            testcase2checks = tfactory.newTemplates(src);
        }
        return testcase2checks;
    }
    
    /**
     * Runs a single layout engine test case.
     * @param testFile Test case to run
     * @throws TransformerException In case of an XSLT/JAXP problem
     * @throws IOException In case of an I/O problem
     * @throws SAXException In case of a problem during SAX processing
     * @throws ParserConfigurationException In case of a problem with the XML parser setup
     */
    public void runTest(File testFile) 
            throws TransformerException, SAXException, IOException, ParserConfigurationException {
        
        DOMResult domres = new DOMResult();

        ElementListCollector elCollector = new ElementListCollector();
        ElementListObserver.addObserver(elCollector);
        
        Fop fop;

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            dbf.setValidating(false);
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document testDoc = builder.parse(testFile);
            
            XObject xo = XPathAPI.eval(testDoc, "/testcase/cfg/base14kerning");
            String s = xo.str();
            boolean base14kerning = ("true".equalsIgnoreCase(s));
            FopFactory effFactory = (base14kerning ? fopFactoryWithBase14Kerning : fopFactory);
            
            //Setup Transformer to convert the testcase XML to XSL-FO
            Transformer transformer = getTestcase2FOStylesheet().newTransformer();
            Source src = new DOMSource(testDoc);
            
            //Setup Transformer to convert the area tree to a DOM
            TransformerHandler athandler = tfactory.newTransformerHandler();
            athandler.setResult(domres);
            
            //Setup FOP for area tree rendering
            FOUserAgent ua = effFactory.newFOUserAgent();
            ua.setBaseURL(testFile.getParentFile().toURL().toString());
            File fontCfgFile = new File("conf/fonts.xconf");
//            File fontCfgFile = new File("../Tests/Config/axsl-font-conf.xml");
            ua.setFontCfgURL(fontCfgFile.toURL());
            XMLRenderer atrenderer = new XMLRenderer();
            atrenderer.setUserAgent(ua);
            atrenderer.setContentHandler(athandler);
            ua.setRendererOverride(atrenderer);
            fop = effFactory.newFop(ua);
            
            SAXResult fores = new SAXResult(fop.getDefaultHandler());
            transformer.transform(src, fores);
        } finally {
            ElementListObserver.removeObserver(elCollector);
        }
        
        Document doc = (Document)domres.getNode();
        if (this.areaTreeBackupDir != null) {
            saveAreaTreeXML(doc, new File(this.areaTreeBackupDir, testFile.getName() + ".at.xml"));
        }
        FormattingResults results = fop.getResults();        
        LayoutResult result = new LayoutResult(doc, elCollector, results);
        checkAll(testFile, result);
    }
    
    /**
     * Factory method to create checks from DOM elements.
     * @param el DOM element to create the check from
     * @return The newly create check
     */
    protected LayoutEngineCheck createCheck(Element el) {
        String name = el.getTagName();
        Class clazz = (Class)CHECK_CLASSES.get(name);
        if (clazz != null) {
            try {
                Constructor c = clazz.getDeclaredConstructor(new Class[] {Node.class});
                LayoutEngineCheck instance = (LayoutEngineCheck)c.newInstance(new Object[] {el});
                return instance;
            } catch (Exception e) {
                throw new RuntimeException("Error while instantiating check '" 
                        + name + "': " + e.getMessage());
            }
        } else {
            throw new IllegalArgumentException("No check class found: " + name);
        }
    }
    
    /**
     * Perform all checks on the area tree.
     * @param testFile Test case XML file
     * @param result The layout results
     * @throws TransformerException if a problem occurs in XSLT/JAXP
     */
    protected void checkAll(File testFile, LayoutResult result) throws TransformerException {
        Transformer transformer = getTestcase2ChecksStylesheet().newTransformer();
        Source src = new StreamSource(testFile);
        DOMResult res = new DOMResult();
        transformer.transform(src, res);
        
        List checks = new java.util.ArrayList();
        Document doc = (Document)res.getNode();
        NodeList nodes = doc.getDocumentElement().getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node instanceof Element) {
                checks.add(createCheck((Element)node));
            }
        }
        
        if (checks.size() == 0) {
            throw new RuntimeException("No checks are available!");
        }
        Iterator i = checks.iterator();
        while (i.hasNext()) {
            LayoutEngineCheck check = (LayoutEngineCheck)i.next();
            check.check(result);
        }
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
}
