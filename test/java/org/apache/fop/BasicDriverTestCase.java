/*
 * $Id$
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 * 
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */ 
package org.apache.fop;

import java.io.File;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.InputSource;

import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.NullLogger;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.fop.apps.Driver;
import org.apache.fop.apps.InputHandler;
import org.apache.fop.apps.TraxInputHandler;
import org.apache.fop.apps.XSLTInputHandler;
import org.w3c.dom.Document;

/**
 * Basic runtime test for the old Driver class. It is used to verify that 
 * nothing obvious is broken after compiling.
 * @author <a href="mailto:jeremias@apache.org">Jeremias Maerki</a>
 */
public class BasicDriverTestCase extends AbstractFOPTestCase {

    private Logger logger = new NullLogger();

    /**
     * @see junit.framework.TestCase#TestCase(String)
     */
    public BasicDriverTestCase(String name) {
        super(name);
    }

    /**
     * Tests Driver with its special constructor for FO-->PDF conversion.
     * @throws Exception if anything fails
     */
    public void testFO2PDFWithConstructorSetup() throws Exception {
        File foFile = new File(getBaseDir(), "test/xml/bugtests/block.fo");
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        Driver driver = new Driver(
            new InputSource(foFile.toURL().toExternalForm()),
            baout);
        //Use deprecated method with purpose to validate backwards-compatibility.
        driver.setLogger(this.logger);
        driver.setRenderer(Driver.RENDER_PDF);
        driver.run();
        assertTrue("Generated PDF has zero length", baout.size() > 0);
    }

    /**
     * Tests Driver with InputSource and OutputStream.
     * @throws Exception if anything fails
     */
    public void testFO2PDFWithInputSource() throws Exception {
        File foFile = new File(getBaseDir(), "test/xml/bugtests/block.fo");
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        Driver driver = new Driver();
        //Use deprecated method with purpose to validate backwards-compatibility.
        driver.setLogger(this.logger);
        driver.setInputSource(new InputSource(foFile.toURL().toExternalForm()));
        driver.setOutputStream(baout);
        driver.setRenderer(Driver.RENDER_PDF);
        driver.run();
        assertTrue("Generated PDF has zero length", baout.size() > 0);
    }

    private Document loadDocument(File foFile) 
                throws TransformerException {
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer();
        Source src = new StreamSource(foFile);
        DOMResult res = new DOMResult();
        transformer.transform(src, res);
        return (Document)res.getNode();                                
    }

    /**
     * Tests Driver with Document and OutputStream.
     * @throws Exception if anything fails
     */
    public void testFO2PDFWithDOM() throws Exception {
        File foFile = new File(getBaseDir(), "test/xml/bugtests/block.fo");
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        Driver driver = new Driver();
        //Use deprecated method with purpose to validate backwards-compatibility.
        driver.setLogger(this.logger);
        driver.setOutputStream(baout);
        driver.setRenderer(Driver.RENDER_PDF);
        driver.render(loadDocument(foFile));
        assertTrue("Generated PDF has zero length", baout.size() > 0);
    }

    /**
     * Tests Driver with XMLReader, InputSource and OutputStream.
     * @throws Exception if anything fails
     */
    public void testFO2PDFWithXMLReader() throws Exception {
        File foFile = new File(getBaseDir(), "test/xml/bugtests/block.fo");
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        Driver driver = new Driver();
        //Use deprecated method with purpose to validate backwards-compatibility.
        driver.setLogger(this.logger);
        driver.setOutputStream(baout);
        driver.setRenderer(Driver.RENDER_PDF);
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        SAXParser parser = factory.newSAXParser();
        driver.render(parser.getXMLReader(),
            new InputSource(foFile.toURL().toExternalForm()));
        assertTrue("Generated PDF has zero length", baout.size() > 0);
    }

    /**
     * Tests Driver with JAXP and OutputStream.
     * @throws Exception if anything fails
     */
    public void testFO2PDFWithJAXP() throws Exception {
        File foFile = new File(getBaseDir(), "test/xml/bugtests/block.fo");
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        Driver driver = new Driver();
        ContainerUtil.enableLogging(driver, this.logger);
        driver.setOutputStream(baout);
        driver.setRenderer(Driver.RENDER_PDF);
        
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer(); //Identity transf.
        Source src = new StreamSource(foFile);
        Result res = new SAXResult(driver.getContentHandler());
        transformer.transform(src, res);
        
        assertTrue("Generated PDF has zero length", baout.size() > 0);
    }

    /**
     * Tests Driver with XsltInputHandler and OutputStream.
     * @throws Exception if anything fails
     */
    public void testFO2PDFWithXSLTInputHandler() throws Exception {
        File xmlFile = new File(getBaseDir(), "test/xml/1.xml");
        File xsltFile = new File(getBaseDir(), "test/xsl/doc.xsl");
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        Driver driver = new Driver();
        ContainerUtil.enableLogging(driver, this.logger);
        driver.setOutputStream(baout);
        driver.setRenderer(Driver.RENDER_PDF);
        
        InputHandler handler = new XSLTInputHandler(xmlFile, xsltFile);
        handler.run(driver);
        
        assertTrue("Generated PDF has zero length", baout.size() > 0);
    }

    /**
     * Tests Driver with TraxInputHandler and OutputStream.
     * @throws Exception if anything fails
     */
    public void testFO2PDFWithTraxInputHandler() throws Exception {
        File xmlFile = new File(getBaseDir(), "test/xml/1.xml");
        File xsltFile = new File(getBaseDir(), "test/xsl/doc.xsl");
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        Driver driver = new Driver();
        ContainerUtil.enableLogging(driver, this.logger);
        driver.setOutputStream(baout);
        driver.setRenderer(Driver.RENDER_PDF);
        
        InputHandler handler = new TraxInputHandler(xmlFile, xsltFile);
        handler.run(driver);
        
        assertTrue("Generated PDF has zero length", baout.size() > 0);
    }


}
