/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in
 *  the documentation and/or other materials provided with the
 *  distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *  any, must include the following acknowlegement:
 *     "This product includes software developed by the
 *    Apache Software Foundation (http://www.apache.org/)."
 *  Alternately, this acknowlegement may appear in the software itself,
 *  if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *  Foundation" must not be used to endorse or promote products derived
 *  from this software without prior written permission. For written
 *  permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *  nor may "Apache" appear in their names without prior written
 *  permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.fop.tools.xslt;

import org.apache.xalan.xslt.*;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Hashtable;
import org.w3c.dom.Document;

/**
 * Handles xslt tranformations via Xalan1 (non-trax)
 */

public class Xalan1Transform
{
    /** Cache of compiled stylesheets (filename, StylesheetRoot) */
    private static Hashtable _stylesheetCache = new Hashtable();

    public static StylesheetRoot getStylesheet(String xsltFilename, 
					       boolean cache)
	throws org.xml.sax.SAXException
    {
	if (cache && _stylesheetCache.containsKey(xsltFilename)) {
	    return (StylesheetRoot)_stylesheetCache.get(xsltFilename);
	}
	
	// Use XSLTProcessor to instantiate an XSLTProcessor.
	XSLTProcessor processor = XSLTProcessorFactory.getProcessor
	    (new org.apache.xalan.xpath.xdom.XercesLiaison());
	
	
	XSLTInputSource xslSheet = new XSLTInputSource (xsltFilename);

	// Perform the transformation.
	StylesheetRoot compiledSheet =
	    processor.processStylesheet(xslSheet);
	if (cache) {
	    _stylesheetCache.put(xsltFilename, compiledSheet);
	}
	return compiledSheet;
    }

    public static void transform(String xmlSource, String xslURL,
                           String outputFile) 
	throws java.io.IOException,
	       java.net.MalformedURLException,
	       org.xml.sax.SAXException
    {
	try {
	    javax.xml.parsers.DocumentBuilder docBuilder =
		javax.xml.parsers.DocumentBuilderFactory.newInstance().
		newDocumentBuilder();
	    Document doc = docBuilder.parse(new FileInputStream(xmlSource));
	    transform(doc,xslURL,outputFile);
	}
	catch (javax.xml.parsers.ParserConfigurationException ex){
	    throw new org.xml.sax.SAXException(ex);
	}

    }
    
    public static void transform(Document xmlSource, 
				 String xslURL,
				 String outputFile) 
	throws java.io.IOException,
	       java.net.MalformedURLException, 
	       org.xml.sax.SAXException
    {

        XSLTResultTarget xmlResult = new XSLTResultTarget (outputFile);

	StylesheetRoot stylesheet = getStylesheet(xslURL,true);
	
        // Perform the transformation.
        stylesheet.process(XSLTProcessorFactory.getProcessor
			   (new org.apache.xalan.xpath.xdom.XercesLiaison()),
			   xmlSource, xmlResult);
    }
    
       public static void transform(String  xmlSource, 
				 String xslURL,
				 java.io.Writer outputFile) 
	throws java.io.IOException,
	       java.net.MalformedURLException, 
	       org.xml.sax.SAXException
    {

        XSLTInputSource source = new XSLTInputSource (xmlSource);
        XSLTResultTarget xmlResult = new XSLTResultTarget (outputFile);

	StylesheetRoot stylesheet = getStylesheet(xslURL,true);
	
        // Perform the transformation.
        stylesheet.process(XSLTProcessorFactory.getProcessor
			   (new org.apache.xalan.xpath.xdom.XercesLiaison()),
			  source, xmlResult);
    }

    public static void transform(Document xmlSource, 
				 InputStream xsl,
				 Document outputDoc) 
	throws java.io.IOException,
	       java.net.MalformedURLException, 
	       org.xml.sax.SAXException
    {

	XSLTInputSource source = new XSLTInputSource (xmlSource); 
	XSLTInputSource xslSheet = new XSLTInputSource(xsl);
	XSLTResultTarget xmlResult = new XSLTResultTarget (outputDoc);
	
	
        // Perform the transformation.
	XSLTProcessor processor =
	    XSLTProcessorFactory.getProcessor(new org.apache.xalan.xpath.xdom.XercesLiaison());

        processor.process(source, xslSheet, xmlResult);
    }
}
