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

package org.apache.fop.complexscripts.layout;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.PrefixFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import org.apache.fop.DebugHelper;

import org.apache.fop.apps.EnvironmentProfile;
import org.apache.fop.apps.EnvironmentalProfileFactory;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopConfBuilder;
import org.apache.fop.apps.FopConfParser;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryBuilder;
import org.apache.fop.apps.FormattingResults;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.apps.PDFRendererConfBuilder;
import org.apache.fop.apps.io.ResourceResolverFactory;
import org.apache.fop.area.AreaTreeModel;
import org.apache.fop.area.AreaTreeParser;
import org.apache.fop.area.RenderPagesModel;
import org.apache.fop.events.Event;
import org.apache.fop.events.EventListener;
import org.apache.fop.events.model.EventSeverity;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.intermediate.IFTester;
import org.apache.fop.intermediate.TestAssistant;
import org.apache.fop.layoutengine.ElementListCollector;
import org.apache.fop.layoutengine.LayoutEngineCheck;
import org.apache.fop.layoutengine.LayoutEngineChecksFactory;
import org.apache.fop.layoutengine.LayoutResult;
import org.apache.fop.layoutengine.TestFilesConfiguration;
import org.apache.fop.layoutmgr.ElementListObserver;
import org.apache.fop.render.Renderer;
import org.apache.fop.render.intermediate.IFContext;
import org.apache.fop.render.intermediate.IFRenderer;
import org.apache.fop.render.intermediate.IFSerializer;
import org.apache.fop.render.xml.XMLRenderer;
import org.apache.fop.util.ConsoleEventListenerForTests;
import org.apache.fop.util.DelegatingContentHandler;

// CSOFF: LineLengthCheck

/**
 * Test complex script layout (end-to-end) functionality.
 */
@RunWith(Parameterized.class)
public class ComplexScriptsLayoutTestCase {

    private static final boolean DEBUG = false;
    private static final String AREA_TREE_OUTPUT_DIRECTORY = "build/test-results/complexscripts";
    private static File areaTreeOutputDir;

    private TestAssistant testAssistant = new TestAssistant();
    private LayoutEngineChecksFactory layoutEngineChecksFactory = new LayoutEngineChecksFactory();
    private TestFilesConfiguration testConfig;
    private File testFile;
    private IFTester ifTester;
    private TransformerFactory tfactory = TransformerFactory.newInstance();

    public ComplexScriptsLayoutTestCase(TestFilesConfiguration testConfig, File testFile) {
        this.testConfig = testConfig;
        this.testFile = testFile;
        this.ifTester = new IFTester(tfactory, areaTreeOutputDir);
    }

    @Parameters
    public static Collection<Object[]> getParameters() throws IOException {
        return getTestFiles();
    }

    @BeforeClass
    public static void makeDirAndRegisterDebugHelper() throws IOException {
        DebugHelper.registerStandardElementListObservers();
        areaTreeOutputDir = new File(AREA_TREE_OUTPUT_DIRECTORY);
        if (!areaTreeOutputDir.mkdirs() && !areaTreeOutputDir.exists()) {
            throw new IOException("Failed to create the AT output directory at " + AREA_TREE_OUTPUT_DIRECTORY);
        }
    }

    @Test
    public void runTest() throws TransformerException, SAXException, IOException, ParserConfigurationException {
        DOMResult domres = new DOMResult();
        ElementListCollector elCollector = new ElementListCollector();
        ElementListObserver.addObserver(elCollector);
        Fop fop;
        FopFactory effFactory;
        EventsChecker eventsChecker = new EventsChecker(new ConsoleEventListenerForTests(testFile.getName(), EventSeverity.WARN));
        try {
            Document testDoc = testAssistant.loadTestCase(testFile);
            effFactory = getFopFactory(testConfig, testDoc);
            // Setup Transformer to convert the testcase XML to XSL-FO
            Transformer transformer = testAssistant.getTestcase2FOStylesheet().newTransformer();
            Source src = new DOMSource(testDoc);
            // Setup Transformer to convert the area tree to a DOM
            TransformerHandler athandler;
            athandler = testAssistant.getTransformerFactory().newTransformerHandler();
            athandler.setResult(domres);
            // Setup FOP for area tree rendering
            FOUserAgent ua = effFactory.newFOUserAgent();
            ua.getEventBroadcaster().addEventListener(eventsChecker);
            XMLRenderer atrenderer = new XMLRenderer(ua);
            Renderer targetRenderer = ua.getRendererFactory().createRenderer(ua, MimeConstants.MIME_PDF);
            atrenderer.mimicRenderer(targetRenderer);
            atrenderer.setContentHandler(athandler);
            ua.setRendererOverride(atrenderer);
            fop = effFactory.newFop(ua);
            SAXResult fores = new SAXResult(fop.getDefaultHandler());
            transformer.transform(src, fores);
        } finally {
            ElementListObserver.removeObserver(elCollector);
        }
        Document doc = (Document)domres.getNode();
        if (areaTreeOutputDir != null) {
            testAssistant.saveDOM(doc, new File(areaTreeOutputDir, testFile.getName() + ".at.xml"));
        }
        FormattingResults results = fop.getResults();
        LayoutResult result = new LayoutResult(doc, elCollector, results);
        checkAll(effFactory, testFile, result, eventsChecker);
    }

    private FopFactory getFopFactory(TestFilesConfiguration testConfig, Document testDoc)  throws SAXException, IOException {
        EnvironmentProfile profile = EnvironmentalProfileFactory.createRestrictedIO(
            testConfig.getTestDirectory().getParentFile().toURI(),
            ResourceResolverFactory.createDefaultResourceResolver());
        InputStream confStream =
            new FopConfBuilder().setStrictValidation(true)
                                .setFontBaseURI("test/resources/fonts/ttf/")
                                .startRendererConfig(PDFRendererConfBuilder.class)
                                  .startFontsConfig()
                                    .startFont(null, "DejaVuLGCSerif.ttf")
                                      .addTriplet("DejaVu LGC Serif", "normal", "normal")
                                    .endFont()
                                  .endFontConfig()
                                .endRendererConfig().build();
        FopFactoryBuilder builder =
            new FopConfParser(confStream, new File(".").toURI(), profile).getFopFactoryBuilder();
        // builder.setStrictFOValidation(isStrictValidation(testDoc));
        // builder.getFontManager().setBase14KerningEnabled(isBase14KerningEnabled(testDoc));
        return builder.build();
    }

    private void checkAll(FopFactory fopFactory, File testFile, LayoutResult result, EventsChecker eventsChecker) throws TransformerException {
        Element testRoot = testAssistant.getTestRoot(testFile);
        NodeList nodes;
        nodes = testRoot.getElementsByTagName("at-checks");
        if (nodes.getLength() > 0) {
            Element atChecks = (Element)nodes.item(0);
            doATChecks(atChecks, result);
        }
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

    private Document createIF(FopFactory fopFactory, File testFile, Document areaTreeXML) throws TransformerException {
        try {
            FOUserAgent ua = fopFactory.newFOUserAgent();
            ua.getEventBroadcaster().addEventListener(new ConsoleEventListenerForTests(testFile.getName(), EventSeverity.WARN));
            IFRenderer ifRenderer = new IFRenderer(ua);
            IFSerializer serializer = new IFSerializer(new IFContext(ua));
            DOMResult result = new DOMResult();
            serializer.setResult(result);
            ifRenderer.setDocumentHandler(serializer);
            ua.setRendererOverride(ifRenderer);
            FontInfo fontInfo = new FontInfo();
            //Construct the AreaTreeModel that will received the individual pages
            final AreaTreeModel treeModel = new RenderPagesModel(ua, null, fontInfo, null);
            //Iterate over all intermediate files
            AreaTreeParser parser = new AreaTreeParser();
            ContentHandler handler = parser.getContentHandler(treeModel, ua);
            DelegatingContentHandler proxy = new DelegatingContentHandler() {
                public void endDocument() throws SAXException {
                    super.endDocument();
                    treeModel.endDocument();
                }
            };
            proxy.setDelegateContentHandler(handler);
            Transformer transformer = tfactory.newTransformer();
            transformer.transform(new DOMSource(areaTreeXML), new SAXResult(proxy));
            return (Document)result.getNode();
        } catch (Exception e) {
            throw new TransformerException("Error while generating intermediate format file: " + e.getMessage(), e);
        }
    }

    private void doATChecks(Element checksRoot, LayoutResult result) {
        List<LayoutEngineCheck> checks = layoutEngineChecksFactory.createCheckList(checksRoot);
        if (checks.size() == 0) {
            throw new RuntimeException("No available area tree check");
        }
        for (LayoutEngineCheck check : checks) {
            check.check(result);
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

    private static Collection<Object[]> getTestFiles(TestFilesConfiguration testConfig) {
        File mainDir = testConfig.getTestDirectory();
        IOFileFilter filter;
        String single = testConfig.getSingleTest();
        String startsWith = testConfig.getStartsWith();
        if (single != null) {
            filter = new NameFileFilter(single);
        } else if (startsWith != null) {
            filter = new PrefixFileFilter(startsWith);
            filter = new AndFileFilter(filter, new SuffixFileFilter(testConfig.getFileSuffix()));
        } else {
            filter = new SuffixFileFilter(testConfig.getFileSuffix());
        }
        String testset = testConfig.getTestSet();
        Collection<File> files = FileUtils.listFiles(new File(mainDir, testset), filter, TrueFileFilter.INSTANCE);
        if (testConfig.hasPrivateTests()) {
            Collection<File> privateFiles =
                FileUtils.listFiles(new File(mainDir, "private-testcases"), filter, TrueFileFilter.INSTANCE);
            files.addAll(privateFiles);
        }
        Collection<Object[]> parametersForJUnit4 = new ArrayList<Object[]>();
        int index = 0;
        for (File f : files) {
            parametersForJUnit4.add(new Object[] { testConfig, f });
            if (DEBUG) {
                System.out.println(String.format("%3d %s", index++, f));
            }
        }
        return parametersForJUnit4;
    }

    private static Collection<Object[]> getTestFiles() {
        String testSet = System.getProperty("fop.complexscripts.testset");
        testSet = (testSet != null ? testSet : "standard") + "-testcases";
        return getTestFiles(testSet);
    }

    private static Collection<Object[]> getTestFiles(String testSetName) {
        TestFilesConfiguration.Builder builder = new TestFilesConfiguration.Builder();
        builder.testDir("test/resources/complexscripts/layout")
               .singleProperty("fop.complexscripts.single")
               .startsWithProperty("fop.complexscripts.starts-with")
               .suffix(".xml")
               .testSet(testSetName)
               .privateTestsProperty("fop.complexscripts.private");
        return getTestFiles(builder.build());
    }

    private static class EventsChecker implements EventListener {

        private final List<Event> events = new ArrayList<Event>();
        private final EventListener defaultListener;

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

}
