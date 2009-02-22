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

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FormattingResults;
import org.apache.fop.events.model.EventSeverity;
import org.apache.fop.intermediate.IFTester;
import org.apache.fop.layoutmgr.ElementListObserver;
import org.apache.fop.render.xml.XMLRenderer;
import org.apache.fop.util.ConsoleEventListenerForTests;

/**
 * Class for testing the FOP's layout engine using testcases specified in XML
 * files.
 */
public class LayoutEngineTester {

    private static final Map AT_CHECK_CLASSES = new java.util.HashMap();

    private TestEnvironment env = new TestEnvironment();

    private File areaTreeBackupDir;
    private IFTester ifTester;

    static {
        AT_CHECK_CLASSES.put("true", TrueCheck.class);
        AT_CHECK_CLASSES.put("eval", EvalCheck.class);
        AT_CHECK_CLASSES.put("element-list", ElementListCheck.class);
        AT_CHECK_CLASSES.put("result", ResultCheck.class);
    }

    /**
     * Constructs a new instance.
     * @param areaTreeBackupDir Optional directory that receives the generated
     *     area tree XML files. May be null.
     */
    public LayoutEngineTester(File areaTreeBackupDir) {
        this.areaTreeBackupDir = areaTreeBackupDir;
        this.ifTester = new IFTester(areaTreeBackupDir);
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
            Document testDoc = env.loadTestCase(testFile);
            FopFactory effFactory = env.getFopFactory(testDoc);

            //Setup Transformer to convert the testcase XML to XSL-FO
            Transformer transformer = env.getTestcase2FOStylesheet().newTransformer();
            Source src = new DOMSource(testDoc);

            //Setup Transformer to convert the area tree to a DOM
            TransformerHandler athandler;
            athandler = env.getTransformerFactory().newTransformerHandler();
            athandler.setResult(domres);

            //Setup FOP for area tree rendering
            FOUserAgent ua = effFactory.newFOUserAgent();
            ua.setBaseURL(testFile.getParentFile().toURL().toString());
            ua.getEventBroadcaster().addEventListener(
                    new ConsoleEventListenerForTests(testFile.getName(), EventSeverity.WARN));

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
            env.saveDOM(doc,
                    new File(this.areaTreeBackupDir, testFile.getName() + ".at.xml"));
        }
        FormattingResults results = fop.getResults();
        LayoutResult result = new LayoutResult(doc, elCollector, results);
        checkAll(testFile, result);
    }

    /**
     * Factory method to create AT checks from DOM elements.
     * @param el DOM element to create the check from
     * @return The newly create check
     */
    protected LayoutEngineCheck createATCheck(Element el) {
        String name = el.getTagName();
        Class clazz = (Class)AT_CHECK_CLASSES.get(name);
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
     * Perform all checks on the area tree and, optionally, on the intermediate format.
     * @param testFile Test case XML file
     * @param result The layout results
     * @throws TransformerException if a problem occurs in XSLT/JAXP
     */
    protected void checkAll(File testFile, LayoutResult result) throws TransformerException {
        Transformer transformer = env.getTestcase2ChecksStylesheet().newTransformer();
        Source src = new StreamSource(testFile);
        DOMResult res = new DOMResult();
        transformer.transform(src, res);

        Document doc = (Document)res.getNode();
        Element root = doc.getDocumentElement();

        NodeList nodes;
        //AT tests only when checks are available
        nodes = root.getElementsByTagName("at-checks");
        if (nodes.getLength() > 0) {
            Element atChecks = (Element)nodes.item(0);
            doATChecks(atChecks, result);
        }

        //IF tests only when checks are available
        nodes = root.getElementsByTagName("if-checks");
        if (nodes.getLength() > 0) {
            Element ifChecks = (Element)nodes.item(0);
            ifTester.doIFChecks(testFile, ifChecks, result.getAreaTree());
        }
    }

    private void doATChecks(Element checksRoot, LayoutResult result) {
        //First create check before actually running them
        List checks = new java.util.ArrayList();
        NodeList nodes = checksRoot.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node instanceof Element) {
                checks.add(createATCheck((Element)node));
            }
        }

        if (checks.size() == 0) {
            throw new RuntimeException("No checks are available!");
        }

        //Run the actual tests now that we know that the checks themselves are ok
        doATChecks(checks, result);
    }

    private void doATChecks(List checks, LayoutResult result) {
        Iterator i = checks.iterator();
        while (i.hasNext()) {
            LayoutEngineCheck check = (LayoutEngineCheck)i.next();
            check.check(result);
        }
    }

}
