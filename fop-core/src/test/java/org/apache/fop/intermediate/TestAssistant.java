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

package org.apache.fop.intermediate;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.apache.fop.apps.EnvironmentProfile;
import org.apache.fop.apps.EnvironmentalProfileFactory;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryBuilder;
import org.apache.fop.apps.io.ResourceResolverFactory;

/**
 * Helper class for running FOP tests.
 */
public class TestAssistant {

    // configure fopFactory as desired
    protected final File testDir = new File("test/layoutengine/standard-testcases");

    private SAXTransformerFactory tfactory = (SAXTransformerFactory) SAXTransformerFactory.newInstance();

    private DocumentBuilderFactory domBuilderFactory;

    private Templates testcase2fo;
    private Templates testcase2checks;

    /**
     * Main constructor.
     */
    public TestAssistant() {
        domBuilderFactory = DocumentBuilderFactory.newInstance();
        domBuilderFactory.setNamespaceAware(true);
        domBuilderFactory.setValidating(false);
    }

    /**
     * Returns the stylesheet for convert extracting the XSL-FO part from the test case.
     * @return the stylesheet
     * @throws TransformerConfigurationException if an error occurs loading the stylesheet
     */
    public Templates getTestcase2FOStylesheet() throws TransformerConfigurationException {
        if (testcase2fo == null) {
            //Load and cache stylesheet
            Source src = new StreamSource(new File("test/layoutengine/testcase2fo.xsl"));
            testcase2fo = tfactory.newTemplates(src);
        }
        return testcase2fo;
    }

    /**
     * Returns the stylesheet for convert extracting the checks from the test case.
     * @return the stylesheet
     * @throws TransformerConfigurationException if an error occurs loading the stylesheet
     */
    private Templates getTestcase2ChecksStylesheet() throws TransformerConfigurationException {
        if (testcase2checks == null) {
            //Load and cache stylesheet
            Source src = new StreamSource(new File("test/layoutengine/testcase2checks.xsl"));
            testcase2checks = tfactory.newTemplates(src);
        }
        return testcase2checks;
    }

    /**
     * Returns the element from the given XML file that encloses the tests.
     *
     * @param testFile a test case
     * @return the parent element of the group(s) of checks
     * @throws TransformerException if an error occurs while extracting the test element
     */
    public Element getTestRoot(File testFile) throws TransformerException {
        Transformer transformer = getTestcase2ChecksStylesheet().newTransformer();
        DOMResult res = new DOMResult();
        transformer.transform(new StreamSource(testFile), res);
        Document doc = (Document) res.getNode();
        return doc.getDocumentElement();
    }

    public FopFactory getFopFactory(Document testDoc) {
        EnvironmentProfile envProfile = EnvironmentalProfileFactory.createRestrictedIO(
                testDir.getParentFile().toURI(),
                ResourceResolverFactory.createDefaultResourceResolver());
        FopFactoryBuilder builder = new FopFactoryBuilder(envProfile);
        builder.setStrictFOValidation(isStrictValidation(testDoc));
        builder.getFontManager().setBase14KerningEnabled(isBase14KerningEnabled(testDoc));
        builder.setTableBorderOverpaint(isTableBorderOverpaint(testDoc));
        builder.setSimpleLineBreaking(isSimpleLineBreaking(testDoc));
        builder.setSkipPagePositionOnlyAllowed(isSkipPagePositionOnlyAllowed(testDoc));
        builder.setLegacySkipPagePositionOnly(isLegacySkipPagePositionOnly(testDoc));
        builder.setLegacyLastPageChangeIPD(isLegacyLastPageChangeIPD(testDoc));
        builder.setLegacyFoWrapper(isLegacyFoWrapper(testDoc));
        return builder.build();
    }

    private boolean isBase14KerningEnabled(Document testDoc) {
        try {
            String s = eval(testDoc, "/testcase/cfg/base14kerning");
            return ("true".equalsIgnoreCase(s));
        } catch (XPathExpressionException e) {
            throw new RuntimeException("Error while evaluating XPath expression", e);
        }
    }

    private boolean isStrictValidation(Document testDoc) {
        try {
            String s = eval(testDoc, "/testcase/cfg/strict-validation");
            return !("false".equalsIgnoreCase(s));
        } catch (XPathExpressionException e) {
            throw new RuntimeException("Error while evaluating XPath expression", e);
        }
    }

    private boolean isTableBorderOverpaint(Document testDoc) {
        try {
            String s = eval(testDoc, "/testcase/cfg/table-border-overpaint");
            return "true".equalsIgnoreCase(s);
        } catch (XPathExpressionException e) {
            throw new RuntimeException("Error while evaluating XPath expression", e);
        }
    }

    private String eval(Document doc, String xpath) throws XPathExpressionException {
        XPath xPath = XPathFactory.newInstance().newXPath();
        return (String) xPath.compile(xpath).evaluate(doc, XPathConstants.STRING);
    }

    private boolean isSimpleLineBreaking(Document testDoc) {
        try {
            String s = eval(testDoc, "/testcase/cfg/simple-line-breaking");
            return "true".equalsIgnoreCase(s);
        } catch (XPathExpressionException e) {
            throw new RuntimeException("Error while evaluating XPath expression", e);
        }
    }

    private boolean isSkipPagePositionOnlyAllowed(Document testDoc) {
        try {
            String s = eval(testDoc, "/testcase/cfg/skip-page-position-only-allowed");
            return !"false".equalsIgnoreCase(s);
        } catch (XPathExpressionException e) {
            throw new RuntimeException("Error while evaluating XPath expression", e);
        }
    }

    private boolean isLegacySkipPagePositionOnly(Document testDoc) {
        try {
            String s = eval(testDoc, "/testcase/cfg/legacy-skip-page-position-only");
            return "true".equalsIgnoreCase(s);
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isLegacyLastPageChangeIPD(Document testDoc) {
        try {
            String s = eval(testDoc, "/testcase/cfg/legacy-last-page-change-ipd");
            return "true".equalsIgnoreCase(s);
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isLegacyFoWrapper(Document testDoc) {
        try {
            String s = eval(testDoc, "/testcase/cfg/legacy-fo-wrapper");
            return "true".equalsIgnoreCase(s);
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Loads a test case into a DOM document.
     * @param testFile the test file
     * @return the loaded test case
     * @throws IOException if an I/O error occurs loading the test case
     */
    public Document loadTestCase(File testFile)
            throws IOException {
        try {
            DocumentBuilder builder = domBuilderFactory.newDocumentBuilder();
            return builder.parse(testFile);
        } catch (Exception e) {
            throw new IOException("Error while loading test case: " + e.getMessage());
        }
    }

    /**
     * Serialize the DOM for later inspection.
     * @param doc the DOM document
     * @param target target file
     * @throws TransformerException if a problem occurs during serialization
     */
    public void saveDOM(Document doc, File target) throws TransformerException {
        Transformer transformer = getTransformerFactory().newTransformer();
        Source src = new DOMSource(doc);
        Result res = new StreamResult(target);
        transformer.transform(src, res);
    }

    /**
     * Returns the SAXTransformerFactory.
     * @return the SAXTransformerFactory
     */
    public SAXTransformerFactory getTransformerFactory() {
        return tfactory;
    }
}
