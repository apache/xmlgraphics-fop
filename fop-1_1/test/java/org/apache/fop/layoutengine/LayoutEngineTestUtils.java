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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.PrefixFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

/**
 * Utility class for layout engine tests.
 */
public final class LayoutEngineTestUtils {

    /** Set this to true to get the correspondence between test number and test file. */
    private static final boolean DEBUG = false;

    private LayoutEngineTestUtils() {
    }

    private static class FilenameHandler extends DefaultHandler {
        private StringBuffer buffer = new StringBuffer(128);
        private boolean readingFilename = false;
        private List<String> filenames;

        public FilenameHandler(List<String> filenames) {
            this.filenames = filenames;
        }

        public void startElement(String namespaceURI, String localName, String qName,
                Attributes atts) throws SAXException {
            if (qName != null && qName.equals("file")) {
                buffer.setLength(0);
                readingFilename = true;
            } else {
                throw new RuntimeException(
                        "Unexpected element while reading disabled testcase file names: " + qName);
            }
        }

        public void endElement(String namespaceURI, String localName, String qName)
                throws SAXException {
            if (qName != null && qName.equals("file")) {
                readingFilename = false;
                filenames.add(buffer.toString());
            } else {
                throw new RuntimeException(
                        "Unexpected element while reading disabled testcase file names: " + qName);
            }
        }

        public void characters(char[] ch, int start, int length) throws SAXException {
            if (readingFilename) {
                buffer.append(ch, start, length);
            }
        }
    }

    /**
     * Removes from {@code filter} any tests that have been disabled.
     *
     * @param filter the filter populated with tests
     * @param disabled name of the file containing disabled test cases. If null or empty,
     * no file is read
     * @return {@code filter} minus any disabled tests
     */
    public static IOFileFilter decorateWithDisabledList(IOFileFilter filter, String disabled) {
        if (disabled != null && disabled.length() > 0) {
            filter = new AndFileFilter(new NotFileFilter(new NameFileFilter(
                    LayoutEngineTestUtils.readDisabledTestcases(new File(disabled)))), filter);
        }
        return filter;
    }

    private static String[] readDisabledTestcases(File f) {
        List<String> lines = new ArrayList<String>();
        Source stylesheet = new StreamSource(
                new File("test/layoutengine/disabled-testcase2filename.xsl"));
        Source source = new StreamSource(f);
        Result result = new SAXResult(new FilenameHandler(lines));
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer(stylesheet);
            transformer.transform(source, result);
        } catch (TransformerConfigurationException tce) {
            throw new RuntimeException(tce);
        } catch (TransformerException te) {
            throw new RuntimeException(te);
        }
        return (String[]) lines.toArray(new String[lines.size()]);
    }

    /**
     * Returns the test files matching the given configuration.
     *
     * @param testConfig the test configuration
     * @return the applicable test cases
     */
    public static Collection<File[]> getTestFiles(TestFilesConfiguration testConfig) {
        File mainDir = testConfig.getTestDirectory();
        IOFileFilter filter;
        String single = testConfig.getSingleTest();
        String startsWith = testConfig.getStartsWith();
        if (single != null) {
            filter = new NameFileFilter(single);
        } else if (startsWith != null) {
            filter = new PrefixFileFilter(startsWith);
            filter = new AndFileFilter(filter, new SuffixFileFilter(testConfig.getFileSuffix()));
            filter = decorateWithDisabledList(filter, testConfig.getDisabledTests());
        } else {
            filter = new SuffixFileFilter(testConfig.getFileSuffix());
            filter = decorateWithDisabledList(filter, testConfig.getDisabledTests());
        }
        String testset = testConfig.getTestSet();

        Collection<File> files = FileUtils.listFiles(new File(mainDir, testset), filter,
                TrueFileFilter.INSTANCE);
        if (testConfig.hasPrivateTests()) {
            Collection<File> privateFiles = FileUtils.listFiles(new File(mainDir,
                    "private-testcases"), filter, TrueFileFilter.INSTANCE);
            files.addAll(privateFiles);
        }

        Collection<File[]> parametersForJUnit4 = new ArrayList<File[]>();
        int index = 0;
        for (File f : files) {
            parametersForJUnit4.add(new File[] { f });
            if (DEBUG) {
                System.out.println(String.format("%3d %s", index++, f));
            }
        }

        return parametersForJUnit4;
    }

    /**
     * This is a helper method that uses the standard parameters for FOP's layout engine tests and
     * returns a set of test files. These pull in System parameters to configure the layout tests
     * to run.
     *
     * @return A collection of file arrays that contain the test files
     */
    public static Collection<File[]> getLayoutTestFiles() {
        String testSet = System.getProperty("fop.layoutengine.testset");
        testSet = (testSet != null ? testSet : "standard") + "-testcases";
        return getLayoutTestFiles(testSet);
    }

    /**
     * This is a helper method that uses the standard parameters for FOP's layout engine tests,
     * given a test set name returns a set of test files.
     *
     * @param testSetName the name of the test set
     * @return A collection of file arrays that contain the test files
     */
    public static Collection<File[]> getLayoutTestFiles(String testSetName) {
        TestFilesConfiguration.Builder builder = new TestFilesConfiguration.Builder();

        builder.testDir("test/layoutengine")
               .singleProperty("fop.layoutengine.single")
               .startsWithProperty("fop.layoutengine.starts-with")
               .suffix(".xml")
               .testSet(testSetName)
               .disabledProperty("fop.layoutengine.disabled",
                       "test/layoutengine/disabled-testcases.xml")
               .privateTestsProperty("fop.layoutengine.private");

        TestFilesConfiguration testConfig = builder.build();
        return getTestFiles(testConfig);
    }

}
