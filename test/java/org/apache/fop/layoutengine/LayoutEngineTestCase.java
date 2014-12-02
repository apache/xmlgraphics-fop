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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.TransformerHandler;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.fop.DebugHelper;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FormattingResults;
import org.apache.fop.area.AreaTreeModel;
import org.apache.fop.area.AreaTreeParser;
import org.apache.fop.area.RenderPagesModel;
import org.apache.fop.events.Event;
import org.apache.fop.events.EventListener;
import org.apache.fop.events.model.EventSeverity;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.intermediate.IFTester;
import org.apache.fop.intermediate.TestAssistant;
import org.apache.fop.layoutmgr.ElementListObserver;
import org.apache.fop.render.intermediate.IFContext;
import org.apache.fop.render.intermediate.IFRenderer;
import org.apache.fop.render.intermediate.IFSerializer;
import org.apache.fop.render.xml.XMLRenderer;
import org.apache.fop.util.ConsoleEventListenerForTests;
import org.apache.fop.util.DelegatingContentHandler;

/**
 * Class for testing the FOP's layout engine using testcases specified in XML
 * files.
 */
@RunWith(Parameterized.class)
public class LayoutEngineTestCase {
    private static File areaTreeBackupDir;

    @BeforeClass
    public static void makeDirAndRegisterDebugHelper() throws IOException {
        DebugHelper.registerStandardElementListObservers();
        areaTreeBackupDir = new File("build/test-results/layoutengine");
        if (!areaTreeBackupDir.mkdirs() && !areaTreeBackupDir.exists()) {
            throw new IOException("Failed to create the layout engine directory at "
                    + "build/test-results/layoutengine");
        }
    }

    /**
     * Creates the parameters for this test.
     *
     * @return the list of file arrays populated with test files
     * @throws IOException if an I/O error occurs while reading the test file
     */
    @Parameters
    public static Collection<File[]> getParameters() throws IOException {
        return LayoutEngineTestUtils.getLayoutTestFiles();
    }

    private TestAssistant testAssistant = new TestAssistant();

    private LayoutEngineChecksFactory layoutEngineChecksFactory = new LayoutEngineChecksFactory();

    private IFTester ifTester;
    private File testFile;

    private TransformerFactory tfactory = TransformerFactory.newInstance();

    /**
     * Constructs a new instance.
     *
     * @param testFile the test file
     */
    public LayoutEngineTestCase(File testFile) {
        this.ifTester = new IFTester(tfactory, areaTreeBackupDir);
        this.testFile = testFile;
    }

    /**
     * Runs a single layout engine test case.
     * @throws TransformerException In case of an XSLT/JAXP problem
     * @throws IOException In case of an I/O problem
     * @throws SAXException In case of a problem during SAX processing
     * @throws ParserConfigurationException In case of a problem with the XML parser setup
     */
    @Test
    public void runTest() throws TransformerException, SAXException, IOException,
            ParserConfigurationException {

        DOMResult domres = new DOMResult();

        ElementListCollector elCollector = new ElementListCollector();
        ElementListObserver.addObserver(elCollector);

        Fop fop;
        FopFactory effFactory;
        EventsChecker eventsChecker = new EventsChecker(
                new ConsoleEventListenerForTests(testFile.getName(), EventSeverity.WARN));
        try {
            Document testDoc = testAssistant.loadTestCase(testFile);
            effFactory = testAssistant.getFopFactory(testDoc);

            //Setup Transformer to convert the testcase XML to XSL-FO
            Transformer transformer = testAssistant.getTestcase2FOStylesheet().newTransformer();
            Source src = new DOMSource(testDoc);

            //Setup Transformer to convert the area tree to a DOM
            TransformerHandler athandler;
            athandler = testAssistant.getTransformerFactory().newTransformerHandler();
            athandler.setResult(domres);

            //Setup FOP for area tree rendering
            FOUserAgent ua = effFactory.newFOUserAgent();
            ua.getEventBroadcaster().addEventListener(eventsChecker);

            XMLRenderer atrenderer = new XMLRenderer(ua);
            atrenderer.setContentHandler(athandler);
            ua.setRendererOverride(atrenderer);
            fop = effFactory.newFop(ua);

            SAXResult fores = new SAXResult(fop.getDefaultHandler());
            transformer.transform(src, fores);
        } finally {
            ElementListObserver.removeObserver(elCollector);
        }

        Document doc = (Document)domres.getNode();
        if (areaTreeBackupDir != null) {
            testAssistant.saveDOM(doc,
                    new File(areaTreeBackupDir, testFile.getName() + ".at.xml"));
        }
        FormattingResults results = fop.getResults();
        LayoutResult result = new LayoutResult(doc, elCollector, results);
        checkAll(effFactory, testFile, result, eventsChecker);
    }

    private static class EventsChecker implements EventListener {

        private final List<Event> events = new ArrayList<Event>();

        private final EventListener defaultListener;

        /**
         * @param fallbackListener the listener to which this class will pass through
         * events that are not being checked
         */
        public EventsChecker(EventListener fallbackListener) {
            this.defaultListener = fallbackListener;
        }

        public void processEvent(Event event) {
            events.add(event);
        }

        public void checkEvent(String expectedKey, Map<String, String> expectedParams) {
            boolean eventFound = false;
            for (Iterator<Event> iter = events.iterator(); !eventFound && iter.hasNext();) {
                Event event = iter.next();
                if (event.getEventKey().equals(expectedKey)) {
                    eventFound = true;
                    iter.remove();
                    checkParameters(event, expectedParams);
                }
            }
            if (!eventFound) {
                fail("Event did not occur but was expected to: " + expectedKey + expectedParams);
            }
        }

        private void checkParameters(Event event, Map<String, String> expectedParams) {
            Map<String, Object> actualParams = event.getParams();
            for (Map.Entry<String, String> expectedParam : expectedParams.entrySet()) {
                assertTrue("Event \"" + event.getEventKey()
                        + "\" is missing parameter \"" + expectedParam.getKey() + '"',
                        actualParams.containsKey(expectedParam.getKey()));
                assertEquals("Event \"" + event.getEventKey()
                        + "\" has wrong value for parameter \"" + expectedParam.getKey() + "\";",
                        actualParams.get(expectedParam.getKey()).toString(),
                        expectedParam.getValue());
            }
        }

        public void emitUncheckedEvents() {
            for (Event event : events) {
                defaultListener.processEvent(event);
            }
        }
    }

    /**
     * Perform all checks on the area tree and, optionally, on the intermediate format.
     * @param fopFactory the FOP factory
     * @param testFile Test case XML file
     * @param result The layout results
     * @throws TransformerException if a problem occurs in XSLT/JAXP
     */
    protected void checkAll(FopFactory fopFactory, File testFile, LayoutResult result,
            EventsChecker eventsChecker) throws TransformerException {
        Element testRoot = testAssistant.getTestRoot(testFile);

        NodeList nodes;
        //AT tests only when checks are available
        nodes = testRoot.getElementsByTagName("at-checks");
        if (nodes.getLength() > 0) {
            Element atChecks = (Element)nodes.item(0);
            doATChecks(atChecks, result);
        }

        //IF tests only when checks are available
        nodes = testRoot.getElementsByTagName("if-checks");
        if (nodes.getLength() > 0) {
            Element ifChecks = (Element)nodes.item(0);
            Document ifDocument = createIF(fopFactory, testFile, result.getAreaTree());
            ifTester.doIFChecks(testFile.getName(), ifChecks, ifDocument);
        }

        nodes = testRoot.getElementsByTagName("event-checks");
        if (nodes.getLength() > 0) {
            Element eventChecks = (Element) nodes.item(0);
            doEventChecks(eventChecks, eventsChecker);
        }
        eventsChecker.emitUncheckedEvents();
    }

    private Document createIF(FopFactory fopFactory, File testFile, Document areaTreeXML)
            throws TransformerException {
        try {
            FOUserAgent ua = fopFactory.newFOUserAgent();
            ua.getEventBroadcaster().addEventListener(
                    new ConsoleEventListenerForTests(testFile.getName(), EventSeverity.WARN));

            IFRenderer ifRenderer = new IFRenderer(ua);

            IFSerializer serializer = new IFSerializer(new IFContext(ua));
            DOMResult result = new DOMResult();
            serializer.setResult(result);
            ifRenderer.setDocumentHandler(serializer);

            ua.setRendererOverride(ifRenderer);
            FontInfo fontInfo = new FontInfo();
            //Construct the AreaTreeModel that will received the individual pages
            final AreaTreeModel treeModel = new RenderPagesModel(ua,
                    null, fontInfo, null);

            //Iterate over all intermediate files
            AreaTreeParser parser = new AreaTreeParser();
            ContentHandler handler = parser.getContentHandler(treeModel, ua);

            DelegatingContentHandler proxy = new DelegatingContentHandler() {

                public void endDocument() throws SAXException {
                    super.endDocument();
                    //Signal the end of the processing.
                    //The renderer can finalize the target document.
                    treeModel.endDocument();
                }

            };
            proxy.setDelegateContentHandler(handler);

            Transformer transformer = tfactory.newTransformer();
            transformer.transform(new DOMSource(areaTreeXML), new SAXResult(proxy));

            return (Document)result.getNode();
        } catch (Exception e) {
            throw new TransformerException(
                    "Error while generating intermediate format file: " + e.getMessage(), e);
        }
    }

    private void doATChecks(Element checksRoot, LayoutResult result) {
        List<LayoutEngineCheck> checks = layoutEngineChecksFactory.createCheckList(checksRoot);
        if (checks.size() == 0) {
            throw new RuntimeException("No available area tree check");
        }
        for (LayoutEngineCheck check : checks) {
            try {
                check.check(result);
            } catch (RuntimeException rte) {
                throw new RuntimeException("Layout test (" + testFile.getName() + "): " + rte.getMessage());
            }
        }
    }

    private void doEventChecks(Element eventChecks, EventsChecker eventsChecker) {
        NodeList events = eventChecks.getElementsByTagName("event");
        for (int i = 0; i < events.getLength(); i++) {
            Element event = (Element) events.item(i);
            NamedNodeMap attributes = event.getAttributes();
            Map<String, String> params = new HashMap<String, String>();
            String key = null;
            for (int j = 0; j < attributes.getLength(); j++) {
                Node attribute = attributes.item(j);
                String name = attribute.getNodeName();
                String value = attribute.getNodeValue();
                if ("key".equals(name)) {
                    key = value;
                } else {
                    params.put(name, value);
                }
            }
            if (key == null) {
                throw new RuntimeException("An event element must have a \"key\" attribute");
            }
            eventsChecker.checkEvent(key, params);
        }
    }

}
