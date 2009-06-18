/*
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
 4. The names "Fop" and  "Apache Software Foundation"  must not be used to
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

package org.apache.fop.image;

// Java
import java.net.URL;
import org.w3c.dom.svg.SVGDocument;

// FOP
import org.apache.fop.svg.SVGDriver;
import org.apache.fop.messaging.*;
import org.apache.fop.datatypes.ColorSpace;
import org.apache.fop.pdf.PDFColor;
import org.apache.fop.image.analyser.ImageReader;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/**
 * @see AbstractFopImage
 * @see FopImage
 */
public class SVGImage extends AbstractFopImage {
  SVGDocument doc;

  public SVGImage(URL href) throws FopImageException {
    super(href);
  }

  public SVGImage(URL href, ImageReader imgReader) {
    super(href, imgReader);
  }

    /**
     * creates a SAX parser, using the value of org.xml.sax.parser
     * defaulting to org.apache.xerces.parsers.SAXParser
     *
     * @return the created SAX parser
     */
    public static XMLReader createParser() {
	String parserClassName =
	    System.getProperty("org.xml.sax.parser");
	if (parserClassName == null) {
	    parserClassName = "org.apache.xerces.parsers.SAXParser";
	}
	MessageHandler.logln("using SAX parser " + parserClassName);

	try {
	    return (XMLReader)
		Class.forName(parserClassName).newInstance();
	} catch (ClassNotFoundException e) {
	    MessageHandler.errorln("Could not find " + parserClassName);
	} catch (InstantiationException e) {
	    MessageHandler.errorln("Could not instantiate "
			       + parserClassName);
	} catch (IllegalAccessException e) {
	    MessageHandler.errorln("Could not access " + parserClassName);
	} catch (ClassCastException e) {
	    MessageHandler.errorln(parserClassName + " is not a SAX driver"); 
	}
	return null;
    }

  protected void loadImage() throws FopImageException {
  try {
	    SVGDriver driver = new SVGDriver();
	    driver.addElementMapping("org.apache.fop.svg.SVGElementMapping");
	    driver.addPropertyList("org.apache.fop.svg.SVGPropertyListMapping");
		XMLReader parser = createParser();
	    driver.buildSVGTree(parser, new InputSource(this.m_href.toString()));
	    doc = driver.getSVGDocument();
	} catch (Exception e) {
	    MessageHandler.errorln("ERROR LOADING EXTERNAL SVG: " + e.getMessage());
	}
  }

  public SVGDocument getSVGDocument() throws FopImageException
  {
    if(doc == null)
        this.loadImage();
    return doc;
  }
}
