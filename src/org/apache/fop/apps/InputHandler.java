/* 
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the 
 * LICENSE file included with these sources."
 */


package org.apache.fop.apps;

// SAX
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

// Java
import java.net.URL;
import java.io.File;

// FOP
import org.apache.fop.messaging.MessageHandler;
import org.apache.fop.configuration.Configuration;


abstract public class InputHandler {


    abstract public InputSource getInputSource();
    abstract public XMLReader getParser();


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
    static XMLReader createParser() {
		boolean debugMode = Configuration.getBooleanValue("debugMode").booleanValue();
        String parserClassName = System.getProperty("org.xml.sax.parser");
        if (parserClassName == null) {
            parserClassName = "org.apache.xerces.parsers.SAXParser";
        }
        MessageHandler.logln("using SAX parser " + parserClassName);

        try {
            return (XMLReader) Class.forName(
                     parserClassName).newInstance();
        } catch (ClassNotFoundException e) {
            MessageHandler.errorln("Could not find " + parserClassName);
            if (debugMode) {
                e.printStackTrace();
            }
        }
        catch (InstantiationException e) {
            MessageHandler.errorln("Could not instantiate " +
                                   parserClassName);
            if (debugMode) {
                e.printStackTrace();
            }
        }
        catch (IllegalAccessException e) {
            MessageHandler.errorln("Could not access " + parserClassName);
            if (debugMode) {
                e.printStackTrace();
            }
        }
        catch (ClassCastException e) {
            MessageHandler.errorln(parserClassName + " is not a SAX driver");
            if (debugMode) {
                e.printStackTrace();
            }
        }
        return null;
    }
}

