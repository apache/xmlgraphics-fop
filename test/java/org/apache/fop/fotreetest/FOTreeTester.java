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

package org.apache.fop.fotreetest;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.PrefixFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import org.apache.fop.DebugHelper;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryConfigurator;
import org.apache.fop.fotreetest.ext.TestElementMapping;
import org.apache.fop.layoutengine.LayoutEngineTestUtils;
import org.apache.fop.util.ConsoleEventListenerForTests;

/**
 * Test driver class for FO tree tests.
 */
@RunWith(Parameterized.class)
public class FOTreeTester {

    @BeforeClass
    public static void registerElementListObservers() {
        DebugHelper.registerStandardElementListObservers();
    }

    @Parameters
    public static Collection<File[]> testXMLTestCases() throws Exception {
        File mainDir = new File("test/fotree");

        IOFileFilter filter;
        String single = System.getProperty("fop.fotree.single");
        String startsWith = System.getProperty("fop.fotree.starts-with");
        if (single != null) {
            filter = new NameFileFilter(single);
        } else if (startsWith != null) {
            filter = new PrefixFileFilter(startsWith);
            filter = new AndFileFilter(filter, new SuffixFileFilter(".fo"));
        } else {
            filter = new SuffixFileFilter(".fo");
            filter = LayoutEngineTestUtils.decorateWithDisabledList(filter);
        }
        Collection<File> files = FileUtils.listFiles(new File(mainDir, "testcases"), filter,
                TrueFileFilter.INSTANCE);
        String privateTests = System.getProperty("fop.fotree.private");
        if ("true".equalsIgnoreCase(privateTests)) {
            Collection privateFiles = FileUtils.listFiles(new File(mainDir, "private-testcases"),
                    filter, TrueFileFilter.INSTANCE);
            files.addAll(privateFiles);
        }
        // Unfortunately JUnit forces us to return a collection of arrays.
        Collection<File[]> parameters = new ArrayList<File[]>();
        for (File f : files) {
            parameters.add(new File[] { f });
        }
        return parameters;
    }

    private FopFactory fopFactory = FopFactory.newInstance();

    private final File testFile;

    /**
     * Main constructor
     *
     * @param testFile the FO file to test
     */
    public FOTreeTester(File testFile) {
        fopFactory.addElementMapping(new TestElementMapping());
        this.testFile = testFile;
    }

    /**
     * Runs a test.
     * @throws Exception if a test or FOP itself fails
     */
    @Test
    public void runTest() throws Exception {
        try {
            ResultCollector collector = ResultCollector.getInstance();
            collector.reset();

            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(true);
            spf.setValidating(false);
            SAXParser parser = spf.newSAXParser();
            XMLReader reader = parser.getXMLReader();

            // Resetting values modified by processing instructions
            fopFactory.setBreakIndentInheritanceOnReferenceAreaBoundary(
                    FopFactoryConfigurator.DEFAULT_BREAK_INDENT_INHERITANCE);
            fopFactory.setSourceResolution(FopFactoryConfigurator.DEFAULT_SOURCE_RESOLUTION);

            FOUserAgent ua = fopFactory.newFOUserAgent();
            ua.setBaseURL(testFile.getParentFile().toURI().toURL().toString());
            ua.setFOEventHandlerOverride(new DummyFOEventHandler(ua));
            ua.getEventBroadcaster().addEventListener(
                    new ConsoleEventListenerForTests(testFile.getName()));

            // Used to set values in the user agent through processing instructions
            reader = new PIListener(reader, ua);

            Fop fop = fopFactory.newFop(ua);

            reader.setContentHandler(fop.getDefaultHandler());
            reader.setDTDHandler(fop.getDefaultHandler());
            reader.setErrorHandler(fop.getDefaultHandler());
            reader.setEntityResolver(fop.getDefaultHandler());
            try {
                reader.parse(testFile.toURI().toURL().toExternalForm());
            } catch (Exception e) {
                collector.notifyError(e.getLocalizedMessage());
                throw e;
            }

            List results = collector.getResults();
            if (results.size() > 0) {
                for (int i = 0; i < results.size(); i++) {
                    System.out.println((String) results.get(i));
                }
                throw new IllegalStateException((String) results.get(0));
            }
        } catch (Exception e) {
            org.apache.commons.logging.LogFactory.getLog(this.getClass()).info(
                    "Error on " + testFile.getName());
            throw e;
        }
    }

    private static class PIListener extends XMLFilterImpl {

        private FOUserAgent userAgent;

        public PIListener(XMLReader parent, FOUserAgent userAgent) {
            super(parent);
            this.userAgent = userAgent;
        }

        /** @see org.xml.sax.helpers.XMLFilterImpl */
        public void processingInstruction(String target, String data) throws SAXException {
            if ("fop-useragent-break-indent-inheritance".equals(target)) {
                userAgent.getFactory().setBreakIndentInheritanceOnReferenceAreaBoundary(
                        Boolean.valueOf(data).booleanValue());
            } else if ("fop-source-resolution".equals(target)) {
                userAgent.getFactory().setSourceResolution(Float.parseFloat(data));
            }
            super.processingInstruction(target, data);
        }

    }

}
