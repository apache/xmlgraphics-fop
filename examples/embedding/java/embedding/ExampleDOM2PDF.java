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
package embedding;

//Java
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

//JAXP
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

// DOM
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

// Avalon
import org.apache.avalon.framework.ExceptionUtil;
import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.avalon.framework.logger.Logger;

//FOP
import org.apache.fop.apps.Driver;
import org.apache.fop.apps.FOPException;

/**
 * This class demonstrates the conversion of a DOM Document to PDF
 * using JAXP (XSLT) and FOP (XSL-FO).
 */
public class ExampleDOM2PDF {

    /** xsl-fo namespace URI */
    protected static String foNS = "http://www.w3.org/1999/XSL/Format";

    /**
     * Converts a DOM Document to a PDF file using FOP.
     * @param doc the DOM Document
     * @param pdf the target PDF file
     * @throws IOException In case of an I/O problem
     * @throws FOPException In case of a FOP problem
     */
    public void convertDOM2PDF(Document xslfoDoc, File pdf) 
                throws IOException, FOPException {
        //Construct driver
        Driver driver = new Driver();
        
        //Setup logger
        Logger logger = new ConsoleLogger(ConsoleLogger.LEVEL_INFO);
        driver.enableLogging(logger);
        driver.initialize();
        
        //Setup Renderer (output format)        
        driver.setRenderer(Driver.RENDER_PDF);
        
        //Setup output
        OutputStream out = new java.io.FileOutputStream(pdf);
        out = new java.io.BufferedOutputStream(out);
        
        try {
            driver.setOutputStream(out);
            driver.render(xslfoDoc);
        } finally {
            out.close();
        }
    }

    /**
     * Main method.
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        try {
            System.out.println("FOP ExampleDOM2PDF\n");
            
            //Setup directories
            File baseDir = new File(".");
            File outDir = new File(baseDir, "out");
            outDir.mkdirs();
            
            //Setup output file
            File pdffile = new File(outDir, "ResultDOM2PDF.pdf");
            System.out.println("PDF Output File: " + pdffile);
            System.out.println();
            
            // Create a sample XSL-FO DOM document
            Document foDoc = null;
            Element root = null, ele1 = null, ele2 = null, ele3 = null;
            Text elementText = null;
            
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            foDoc = db.newDocument();
            
            root = foDoc.createElementNS(foNS, "fo:root");
            foDoc.appendChild(root);
            
            ele1 = foDoc.createElementNS(foNS, "fo:layout-master-set");
            root.appendChild(ele1);
            ele2 = foDoc.createElementNS(foNS, "fo:simple-page-master");
            ele1.appendChild(ele2);
            ele2.setAttributeNS(null, "master-name", "letter");
            ele2.setAttributeNS(null, "page-height", "11in");
            ele2.setAttributeNS(null, "page-width", "8.5in");
            ele2.setAttributeNS(null, "margin-top", "1in");
            ele2.setAttributeNS(null, "margin-bottom", "1in");
            ele2.setAttributeNS(null, "margin-left", "1in");
            ele2.setAttributeNS(null, "margin-right", "1in");
            ele3 = foDoc.createElementNS(foNS, "fo:region-body");
            ele2.appendChild(ele3);
            ele1 = foDoc.createElementNS(foNS, "fo:page-sequence");
            root.appendChild(ele1);
            ele1.setAttributeNS(null, "master-reference", "letter");
            ele2 = foDoc.createElementNS(foNS, "fo:flow");
            ele1.appendChild(ele2);
            ele2.setAttributeNS(null, "flow-name", "xsl-region-body");
            AddElement(ele2, "fo:block", "Hello World!");
            
            ExampleDOM2PDF app = new ExampleDOM2PDF();
            app.convertDOM2PDF(foDoc, pdffile);
            
            System.out.println("Success!");
        } catch (Exception e) {
            System.err.println(ExceptionUtil.printStackTrace(e));
            System.exit(-1);
        }
    }

	protected static void AddElement(Node parent, String newNodeName, 
        String textVal)
	{
		if (textVal == null) return;  // use only with text nodes
		Element newElement = 
            parent.getOwnerDocument().createElementNS(foNS, newNodeName);
		Text elementText = parent.getOwnerDocument().createTextNode(textVal);
		newElement.appendChild(elementText);
		parent.appendChild(newElement);
	}
}

