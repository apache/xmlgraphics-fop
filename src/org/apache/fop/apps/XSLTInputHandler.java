/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.apps;

// Imported SAX classes
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

// Imported java.io classes
import java.io.*;

/**
 * XSLTInputHandler takes an XML input, transforms it with XSLT
 * and provides the resulting xsl:fo document as input for the
 * FOP driver.
 * Use TraxInputHandler instead.
 */
public class XSLTInputHandler extends InputHandler {
    private TraxInputHandler traxInputHandler;

    public XSLTInputHandler(File xmlfile, File xsltfile)
      throws FOPException {
        traxInputHandler = new TraxInputHandler(xmlfile, xsltfile);
    }

    public XSLTInputHandler(String xmlURL, String xsltURL)
      throws FOPException {
        traxInputHandler = new TraxInputHandler(xmlURL, xsltURL);
    }

    public XSLTInputHandler(InputSource xmlSource, InputSource xsltSource)
      throws FOPException {
        traxInputHandler = new TraxInputHandler(xmlSource, xsltSource);
    }

    /**
     * Get the InputSource.
     * Use TraxInputHandler run(Driver driver) instead.
     * @deprecated
     */
    public InputSource getInputSource() {
        return traxInputHandler.getInputSource();
    }

    /**
     * Get the parser, actually an XML filter.
     * Use TraxInputHandler run(Driver driver) instead.
     * @deprecated
     */
    public XMLReader getParser() throws FOPException {
        return traxInputHandler.getParser();
    }

    public void run(Driver driver) throws FOPException {
        traxInputHandler.run(driver);
    }

    public void setParameter(String name, Object value) {
        traxInputHandler.setParameter(name, value);
    }
}

