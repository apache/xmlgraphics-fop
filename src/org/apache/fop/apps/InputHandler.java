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

// FOP
import org.apache.fop.configuration.Configuration;


abstract public class InputHandler {


    abstract public InputSource getInputSource();
    abstract public XMLReader getParser() throws FOPException;


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
     * creates a SAX parser, using the value of org.xml.sax.parser
     * defaulting to org.apache.xerces.parsers.SAXParser
     *
     * @return the created SAX parser
     */
    protected static XMLReader createParser() throws FOPException {
        String parserClassName = Driver.getParserClassName();
        //log.debug("using SAX parser " + parserClassName);

        try {
            return (XMLReader)Class.forName(parserClassName).newInstance();
        } catch (ClassNotFoundException e) {
            throw new FOPException(e);
        } catch (InstantiationException e) {
            throw new FOPException("Could not instantiate "
                                   + parserClassName, e);
        } catch (IllegalAccessException e) {
            throw new FOPException("Could not access " + parserClassName, e);
        } catch (ClassCastException e) {
            throw new FOPException(parserClassName + " is not a SAX driver",
                                   e);
        }
    }

}

