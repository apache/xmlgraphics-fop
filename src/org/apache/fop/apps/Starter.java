/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.apps;

// Avalon
import org.apache.avalon.framework.logger.AbstractLogEnabled;

// SAX
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;

// Java
import java.io.*;
import java.net.URL;

/**
 * abstract super class
 * Creates a SAX Parser (defaulting to Xerces).
 *
 */
public abstract class Starter extends AbstractLogEnabled {

    InputHandler inputHandler;

    public Starter() throws FOPException {
    }

    public void setInputHandler(InputHandler inputHandler) {
        this.inputHandler = inputHandler;
    }

    abstract public void run() throws FOPException;

    // setting the parser features
    public void setParserFeatures(XMLReader parser) throws FOPException {
        try {
            parser.setFeature("http://xml.org/sax/features/namespace-prefixes",
                              true);
        } catch (SAXException e) {
            throw new FOPException("Error: You need a parser which allows the"
                   + " http://xml.org/sax/features/namespace-prefixes"
                   + " feature to be set to true to support namespaces", e);
        }
    }

}
