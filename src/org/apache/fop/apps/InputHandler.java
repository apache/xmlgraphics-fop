/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.apps;

// SAX
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

// Java
import java.net.URL;
import java.io.File;
import javax.xml.parsers.*;

// FOP
import org.apache.fop.messaging.MessageHandler;
import org.apache.fop.configuration.Configuration;


abstract public class InputHandler {


    abstract public InputSource getInputSource();
    abstract public XMLReader getParser() throws FOPException;
    abstract public void run(Driver driver) throws FOPException;


    static public InputSource urlInputSource(URL url) {
        return new InputSource(url.toString());
    }

    /**
     * create an InputSource from a File
     *
     * @param file the File
     * @return the InputSource created
     */
    static public InputSource fileInputSource(File file) {
        /* this code adapted from James Clark's in XT */
        String path = file.getAbsolutePath();
        String fSep = System.getProperty("file.separator");
        if (fSep != null && fSep.length() == 1)
            path = path.replace(fSep.charAt(0), '/');
        if (path.length() > 0 && path.charAt(0) != '/')
            path = '/' + path;
        try {
            return new InputSource(new URL("file", null, path).toString());
        } catch (java.net.MalformedURLException e) {
            throw new Error("unexpected MalformedURLException");
        }
    }

    /**
     * creates a SAX parser
     *
     * @return the created SAX parser
     */
    protected static XMLReader createParser() throws FOPException {
        try {
            SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
            spf.setNamespaceAware(true);
            XMLReader xmlReader = spf.newSAXParser().getXMLReader();
            MessageHandler.logln("Using " + xmlReader.getClass().getName() + " as SAX2 Parser"); 
            return xmlReader;
        } catch (javax.xml.parsers.ParserConfigurationException e) {
          throw new FOPException(e);
        } catch (org.xml.sax.SAXException e) {
          throw new FOPException( e);
        }
    }

}

