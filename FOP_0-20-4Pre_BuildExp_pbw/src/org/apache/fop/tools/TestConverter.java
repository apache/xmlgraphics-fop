/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.tools;

import org.apache.fop.apps.*;
import org.apache.fop.configuration.*;

import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.avalon.framework.logger.Logger;

import java.io.*;
import java.util.*;

import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

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
 * Modified by Mark Lillywhite mark-fop@inomial.com to use the new Driver
 * interface.
 */
public class TestConverter {
    boolean failOnly = false;
    boolean outputPDF = false;
    File destdir;
    File compare = null;
    String baseDir = "./";
    HashMap differ = new HashMap();
    private Logger log;

    /**
     * This main method can be used to run the test converter from
     * the command line.
     * This will take a specified testsuite xml and process all
     * tests in it.
     * The command line options are:
     * -b to set the base directory for where the testsuite and associated files are
     * -failOnly to process only the tests which are specified as fail in the test results
     * -pdf to output the result as pdf
     */
    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            System.out.println("test suite file name required");
        }
        TestConverter tc = new TestConverter();

        String testFile = null;
        for (int count = 0; count < args.length; count++) {
            if (args[count].equals("-failOnly")) {
                tc.setFailOnly(true);
            } else if (args[count].equals("-pdf")) {
                tc.setOutputPDF(true);
            } else if (args[count].equals("-b")) {
                tc.setBaseDir(args[count + 1]);
            } else {
                testFile = args[count];
            }
        }
        if (testFile == null) {
            System.out.println("test suite file name required");
        }

        tc.runTests(testFile, "results", null);
    }

    public TestConverter() {
        setupLogging();
    }

    private void setupLogging() {
	log = new ConsoleLogger(ConsoleLogger.LEVEL_ERROR);
    }

    public void setOutputPDF(boolean pdf) {
        outputPDF = pdf;
    }

    public void setFailOnly(boolean fail) {
        failOnly = fail;
    }

    public void setBaseDir(String str) {
        baseDir = str;
    }

    /**
     * Run the Tests.
     * This runs the tests specified in the xml file fname.
     * The document is read as a dom and each testcase is covered.
     */
    public HashMap runTests(String fname, String dest, String compDir) {
        log.debug("running tests in file:" + fname);
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
                log.debug("testing test suite:" + profile);
            }

            NodeList testcases = testsuite.getChildNodes();
            for (int count = 0; count < testcases.getLength(); count++) {
                Node testcase = testcases.item(count);
                if (testcase.getNodeName().equals("testcases")) {
                    runTestCase(testcase);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return differ;
    }

    /**
     * Run a test case.
     * This goes through a test case in the document.
     * A testcase can contain a test, a result or more test cases.
     * A test case is handled recursively otherwise the test is run.
     */
    protected void runTestCase(Node tcase) {
        if (tcase.hasAttributes()) {
            String profile =
                tcase.getAttributes().getNamedItem("profile").getNodeValue();
            log.debug("testing profile:" + profile);
        }

        NodeList cases = tcase.getChildNodes();
        for (int count = 0; count < cases.getLength(); count++) {
            Node node = cases.item(count);
            String nodename = node.getNodeName();
            if (nodename.equals("testcases")) {
                runTestCase(node);
            } else if (nodename.equals("test")) {
                runTest(tcase, node);
            } else if (nodename.equals("result")) {}

        }

    }

    /**
     * Run a particular test.
     * This runs a test defined by the xml and xsl documents.
     * If the test has a result specified it is checked.
     * This creates an XSLTInputHandler to provide the input
     * for FOP and writes the data out to an XML are tree.
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
        log.debug("converting xml:" + xml + " and xsl:" +
                  xsl + " to area tree");

        try {
            File xmlFile = new File(baseDir + "/" + xml);

            try {
                Configuration.put("baseDir",
                                  xmlFile.getParentFile().toURL().toExternalForm());
            } catch (Exception e) {
                log.error("Error setting base directory");
            }

            InputHandler inputHandler = null;
            if (xsl == null) {
                inputHandler = new FOInputHandler(xmlFile);
            } else {
                inputHandler = new XSLTInputHandler(xmlFile,
                                                    new File(baseDir + "/"
                                                             + xsl));
            }

            XMLReader parser = inputHandler.getParser();
            setParserFeatures(parser);

            Logger logger = log.getChildLogger("fop");
            Driver driver = new Driver();
            driver.setLogger(logger);
            if (outputPDF) {
                driver.setRenderer(Driver.RENDER_PDF);
            } else {
                driver.setRenderer(Driver.RENDER_XML);
            }

            HashMap rendererOptions = new HashMap();
            rendererOptions.put("fineDetail", new Boolean(false));
            rendererOptions.put("consistentOutput", new Boolean(true));
            driver.getRenderer().setOptions(rendererOptions);
            driver.getRenderer().setProducer("Testsuite Converter");

            String outname = xmlFile.getName();
            if (outname.endsWith(".xml")) {
                outname = outname.substring(0, outname.length() - 4);
            }
            driver.setOutputStream(new BufferedOutputStream(
                                       new FileOutputStream(new File(destdir,
                                       outname + (outputPDF ? ".pdf" : ".at.xml")))));
            log.debug("ddir:" + destdir + " on:" + outname + ".pdf");
            driver.render(parser, inputHandler.getInputSource());

            // check difference
            if (compare != null) {
                File f1 = new File(destdir, outname + ".at.xml");
                File f2 = new File(compare, outname + ".at.xml");
                if (!compareFiles(f1, f2)) {
                    differ.put(outname + ".at.xml", new Boolean(pass));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Compare files.
     * Returns true if equal.
     */
    protected boolean compareFiles(File f1, File f2) {
        if(f1.length() != f2.length()) {
            return false;
        }
        try {
            InputStream is1 = new BufferedInputStream(new FileInputStream(f1));
            InputStream is2 = new BufferedInputStream(new FileInputStream(f2));
            while (true) {
                int ch1 = is1.read();
                int ch2 = is2.read();
                if (ch1 == ch2) {
                    if (ch1 == -1) {
                        return true;
                    }
                } else {
                    return false;
                }
            }
        } catch (Exception e) {}

        return false;
    }

    public void setParserFeatures(XMLReader parser) throws FOPException {
        try {
            parser.setFeature("http://xml.org/sax/features/namespace-prefixes",
                              true);
        } catch (SAXException e) {
            throw new FOPException("Error in setting up parser feature namespace-prefixes\n"
                                   + "You need a parser which supports SAX version 2", e);
        }
    }

    protected Node locateResult(Node testcase, String id) {
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
