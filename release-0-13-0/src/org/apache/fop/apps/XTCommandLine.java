/*-- $Id$ -- 

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================
 
    Copyright (C) 1999 The Apache Software Foundation. All rights reserved.
 
 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:
 
 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.
 
 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.
 
 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.
 
 4. The names "FOP" and  "Apache Software Foundation"  must not be used to
    endorse  or promote  products derived  from this  software without  prior
    written permission. For written permission, please contact
    apache@apache.org.
 
 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.
 
 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 James Tauber <jtauber@jtauber.com>. For more  information on the Apache 
 Software Foundation, please see <http://www.apache.org/>.
 
 */

package org.apache.fop.apps;

import org.apache.fop.render.pdf.PDFRenderer;
import org.apache.fop.fo.StandardElementMapping;
import org.apache.fop.svg.SVGElementMapping;

// James Clark
import com.jclark.xsl.sax.XSLProcessor;
import com.jclark.xsl.sax.XSLProcessorImpl;

// SAX
import org.xml.sax.Parser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

// Java
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.net.URL;

/**
 * mainline class for full transformation (via XT) + formatting/rendering.
 *
 * gets input, stylesheet and output filenames from the command line
 * creates an implementation of XSLProcessor, passing it the stylesheet
 * treats XSLProcessor as SAXParser
 *
 */
public class XTCommandLine extends CommandLine {

    /**
     * mainline method.
     *
     * first command line argument is XML input file
     * second command line argument is XSL stylesheet file
     * third command line argument is outputfile
     */
    public static void main(String[] args) {
	String version = Version.getVersion();
	System.err.println(version);
		
	if (args.length != 3) {
	    System.err.println("usage: java org.apache.fop.apps.XTCommandLine xml-file xsl-stylesheet pdf-file");
	    System.exit(1);
	}
	
	Parser parser = createParser();
	
	if (parser == null) {
	    System.err.println("ERROR: Unable to create SAX parser");
	    System.exit(1);
	}
	
	XSLProcessor xslProcessor = new XSLProcessorImpl();
	xslProcessor.setParser(parser);
	
	try {
	    xslProcessor.loadStylesheet(fileInputSource(args[1]));

	    Driver driver = new Driver();
	    driver.setRenderer("org.apache.fop.render.pdf.PDFRenderer",
			       version);
	    driver.addElementMapping("org.apache.fop.fo.StandardElementMapping");
	    driver.addElementMapping("org.apache.fop.svg.SVGElementMapping");
	    driver.setWriter(new PrintWriter(new FileWriter(args[2])));
	    driver.buildFOTree(xslProcessor, fileInputSource(args[0]));
	    driver.format();
	    driver.render();
	} catch (Exception e) {
	    System.err.println("FATAL ERROR: " + e.getMessage());
	    System.exit(1);
	}
    }
}
