/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
 
package org.apache.fop.tools;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.InputHandler;
import org.apache.fop.tools.anttasks.FileCompare;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.apache.commons.logging.impl.SimpleLog;


/**
 * TestConverter is used to process a set of tests specified in
 * a testsuite.
 * This class retrieves the data in the testsuite and uses FOP
 * to convert the xml and xsl file into either an xml representation
 * of the area tree or a pdf document.
 * The area tree can be used for automatic comparisons between different
 * versions of FOP or the pdf can be view for manual checking and
 * pdf rendering.
 *
 */
public class TestConverter {
    
    private boolean failOnly = false;
    private int renderType = Fop.RENDER_XML;
    private File destdir;
    private File compare = null;
    private String baseDir = "./";
    private Map differ = new java.util.HashMap();

    /**
     * logging instance
     */
    protected SimpleLog logger = null;

    /**
     * This main method can be used to run the test converter from
     * the command line.
     * This will take a specified testsuite xml and process all
     * tests in it.
     * The command line options are:
     * -b to set the base directory for where the testsuite and associated files are
     * -failOnly to process only the tests which are specified as fail in the test results
     * -pdf to output the result as pdf
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            System.out.println("test suite file name required");
        }
        TestConverter tc = new TestConverter();

        String results = "results";
        String testFile = null;
        for (int count = 0; count < args.length; count++) {
            if (args[count].equals("-failOnly")) {
                tc.setFailOnly(true);
            } else if (args[count].equals("-pdf")) {
                tc.setRenderType(Fop.RENDER_PDF);
            } else if (args[count].equals("-rtf")) {
                tc.setRenderType(Fop.RENDER_RTF);
            } else if (args[count].equals("-ps")) {
                tc.setRenderType(Fop.RENDER_PS);
            } else if (args[count].equals("-d")) {
                tc.setDebug(true);
            } else if (args[count].equals("-b")) {
                tc.setBaseDir(args[++count]);
            } else if (args[count].equals("-results")) {
                results = args[++count];
            } else {
                testFile = args[count];
            }
        }
        if (testFile == null) {
            System.out.println("test suite file name required");
        }

        tc.runTests(testFile, results, null);
    }

    /**
     * Construct a new TestConverter
     */
    public TestConverter() {
        logger = new SimpleLog("FOP/Test");
        logger.setLevel(SimpleLog.LOG_LEVEL_ERROR);
    }

    /**
     * Controls output type to generate
     * @param renderType fo.Constants output constant (RENDER_PDF, RENDER_XML, etc.)
     */
    public void setRenderType(int renderType) {
        this.renderType = renderType;
    }

    /**
     * Controls whether to process only the tests which are specified as fail 
     * in the test results.
     * @param fail True if only fail tests should be processed
     */
    public void setFailOnly(boolean fail) {
        failOnly = fail;
    }

    /**
     * Sets the base directory.
     * @param str base directory
     */
    public void setBaseDir(String str) {
        baseDir = str;
    }

    /**
     * Controls whether to set logging to debug level
     * @param If true, debug level, if false, error level
     */
    public void setDebug(boolean debug) {
        if (debug) {
            logger.setLevel(SimpleLog.LOG_LEVEL_DEBUG);
        } else {
            logger.setLevel(SimpleLog.LOG_LEVEL_ERROR);
        }
    }

    /**
     * Run the Tests.
     * This runs the tests specified in the xml file fname.
     * The document is read as a dom and each testcase is covered.
     * @param fname filename of the input file
     * @param dest destination directory
     * @param compDir comparison directory
     * @return Map a Map containing differences
     */
    public Map runTests(String fname, String dest, String compDir) {
        logger.debug("running tests in file:" + fname);
        try {
            if (compDir != null) {
                compare = new File(baseDir + "/" + compDir);
            }
            destdir = new File(baseDir + "/" + dest);
            destdir.mkdirs();
            File f = new File(baseDir + "/" + fname);
            DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
            DocumentBuilder db = factory.newDocumentBuilder();
            Document doc = db.parse(f);

            NodeList suitelist = doc.getChildNodes();
            if (suitelist.getLength() == 0) {
                return differ;
            }

            Node testsuite = null;
            testsuite = doc.getDocumentElement();

            if (testsuite.hasAttributes()) {
                String profile =
                    testsuite.getAttributes().getNamedItem("profile").getNodeValue();
                logger.debug("testing test suite:" + profile);
            }

            NodeList testcases = testsuite.getChildNodes();
            for (int count = 0; count < testcases.getLength(); count++) {
                Node testcase = testcases.item(count);
                if (testcase.getNodeName().equals("testcases")) {
                    runTestCase(testcase);
                }
            }
        } catch (Exception e) {
            logger.error("Error while running tests", e);
        }
        return differ;
    }

    /**
     * Run a test case.
     * This goes through a test case in the document.
     * A testcase can contain a test, a result or more test cases.
     * A test case is handled recursively otherwise the test is run.
     * @param tcase Test case node to run
     */
    protected void runTestCase(Node tcase) {
        if (tcase.hasAttributes()) {
            String profile =
                tcase.getAttributes().getNamedItem("profile").getNodeValue();
            logger.debug("testing profile:" + profile);
        }

        NodeList cases = tcase.getChildNodes();
        for (int count = 0; count < cases.getLength(); count++) {
            Node node = cases.item(count);
            String nodename = node.getNodeName();
            if (nodename.equals("testcases")) {
                runTestCase(node);
            } else if (nodename.equals("test")) {
                runTest(tcase, node);
            } else if (nodename.equals("result")) {
                //nop
            }

        }

    }

    /**
     * Run a particular test.
     * This runs a test defined by the xml and xsl documents.
     * If the test has a result specified it is checked.
     * This creates an XSLTInputHandler to provide the input
     * for FOP and writes the data out to an XML are tree.
     * @param testcase Test case to run
     * @param test Test
     */
    protected void runTest(Node testcase, Node test) {
        String id = test.getAttributes().getNamedItem("id").getNodeValue();
        Node result = locateResult(testcase, id);
        boolean pass = false;
        if (result != null) {
            String agreement =
                result.getAttributes().getNamedItem("agreement").getNodeValue();
            pass = agreement.equals("full");
        }

        if (pass && failOnly) {
            return;
        }

        String xml = test.getAttributes().getNamedItem("xml").getNodeValue();
        Node xslNode = test.getAttributes().getNamedItem("xsl");
        String xsl = null;
        if (xslNode != null) {
            xsl = xslNode.getNodeValue();
        }
        logger.debug("converting xml:" + xml + " and xsl:" 
                  + xsl + " to area tree");

        String res = xml;
        Node resNode =  test.getAttributes().getNamedItem("results");
        if (resNode != null) {
            res = resNode.getNodeValue();
        }
        try {
            File xmlFile = new File(baseDir + "/" + xml);
            String baseURL = null;
            try {
                baseURL = xmlFile.getParentFile().toURL().toExternalForm();
            } catch (Exception e) {
                logger.error("Error setting base directory");
            }

            InputHandler inputHandler = null;
            if (xsl == null) {
                inputHandler = new InputHandler(xmlFile);
            } else {
                inputHandler = new InputHandler(xmlFile,
                                                new File(baseDir + "/"
                                                         + xsl), null);
            }

            FOUserAgent userAgent = new FOUserAgent();
            userAgent.setBaseURL(baseURL);
            Fop fop = new Fop(renderType, userAgent);

            userAgent.getRendererOptions().put("fineDetail", new Boolean(false));
            userAgent.getRendererOptions().put("consistentOutput", new Boolean(true));
            userAgent.setProducer("Testsuite Converter");

            String outname = res;
            if (outname.endsWith(".xml") || outname.endsWith(".pdf")) {
                outname = outname.substring(0, outname.length() - 4);
            }
            File outputFile = new File(destdir, 
                                       outname + makeResultExtension());

            outputFile.getParentFile().mkdirs();
            OutputStream outStream = new java.io.BufferedOutputStream(
                                 new java.io.FileOutputStream(outputFile));
            fop.setOutputStream(outStream);
            logger.debug("ddir:" + destdir + " on:" + 
                              outputFile.getName());
            inputHandler.render(fop);
            outStream.close();

            // check difference
            if (compare != null) {
                File f1 = new File(destdir, outname + ".at.xml");
                File f2 = new File(compare, outname + ".at.xml");
                if (!compareFiles(f1, f2)) {
                    differ.put(outname + ".at.xml", new Boolean(pass));
                }
            }
        } catch (Exception e) {
            logger.error("Error while running tests", e);
        }
    }

    /**
     * Return a suitable file extension for the output format.
     */
    private String makeResultExtension() {
        if (renderType == Fop.RENDER_PDF) {
           return ".pdf";
        } else if (renderType == Fop.RENDER_RTF) {
           return ".rtf";
        } else if (renderType == Fop.RENDER_PS) {
           return ".ps";
        } else {
            return ".at.xml";
        }
    }

    /**
     * Compare files.
     * @param f1 first file
     * @param f2 second file
     * @return true if equal
     */
    protected boolean compareFiles(File f1, File f2) {
        try {
            return FileCompare.compareFiles(f1, f2);
        } catch (Exception e) {
            logger.error("Error while comparing files", e);
            return false;
        }
    }

    private Node locateResult(Node testcase, String id) {
        NodeList cases = testcase.getChildNodes();
        for (int count = 0; count < cases.getLength(); count++) {
            Node node = cases.item(count);
            String nodename = node.getNodeName();
            if (nodename.equals("result")) {
                String resultid =
                    node.getAttributes().getNamedItem("id").getNodeValue();
                if (id.equals(resultid)) {
                    return node;
                }
            }
        }
        return null;
    }

}
