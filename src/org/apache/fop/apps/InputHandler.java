/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.apps;

// SAX
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;

// Java
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.net.URL;
import java.io.File;

public abstract class InputHandler {

    public abstract InputSource getInputSource();
    public abstract XMLReader getParser() throws FOPException;

    public static InputSource urlInputSource(URL url) {
        return new InputSource(url.toString());
    }

    /**
     * Creates an <code>InputSource</code> from a <code>File</code>
     * @param file the <code>File</code>
     * @return the <code>InputSource</code> created
     */
    public static InputSource fileInputSource(File file) {
        /* this code adapted from James Clark's in XT */
        String path = file.getAbsolutePath();
        String fSep = System.getProperty("file.separator");
        if (fSep != null && fSep.length() == 1) {
            path = path.replace(fSep.charAt(0), '/');
        }
        if (path.length() > 0 && path.charAt(0) != '/') {
            path = '/' + path;
        }
        try {
            return new InputSource(new URL("file", null, path).toString());
        } catch (java.net.MalformedURLException e) {
            throw new Error("unexpected MalformedURLException");
        }
    }

    /**
     * Creates <code>XMLReader</code> object using default
     * <code>SAXParserFactory</code>
     * @return the created <code>XMLReader</code>
     */
    protected static XMLReader createParser() throws FOPException {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            return factory.newSAXParser().getXMLReader();
        } catch (SAXException se) {
            throw new FOPException("Coudn't create XMLReader", se);
        } catch (ParserConfigurationException pce) {
            throw new FOPException("Coudn't create XMLReader", pce);
        }
    }
}

