/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.configuration;

// sax
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;

// java
import java.io.IOException;
import javax.xml.parsers.*;

// fop
import org.apache.fop.apps.Driver;
import org.apache.fop.messaging.MessageHandler;
import org.apache.fop.apps.FOPException;
import org.apache.fop.configuration.Configuration;

/**
 * entry class for reading configuration from file and creating a configuration
 * class. typical use looks like that: <br>
 *
 * <code>ConfigurationReader reader = new ConfigurationReader ("config.xml","standard");
 * try {
 * reader.start();
 * } catch (org.apache.fop.apps.FOPException error) {
 * reader.dumpError(error);
 * }
 * </code>
 * Once the configuration has been setup, the information can be accessed with
 * the methods of StandardConfiguration.
 */
public class ConfigurationReader {

    /**
     * show a full dump on error
     */
    private static boolean errorDump = false;

    /**
     * inputsource for configuration file
     */
    private InputSource filename;


    /**
     * creates a configuration reader
     * @param filename the file which contains the configuration information
     */
    public ConfigurationReader(InputSource filename) {
        this.filename = filename;
    }

    /**
     * intantiates parser and starts parsing of config file
     */
    public void start() throws FOPException {
        XMLReader parser = createParser();

        ConfigurationParser configurationParser = new ConfigurationParser();
        parser.setContentHandler(configurationParser);

        try {
            parser.parse(filename);
        } catch (SAXException e) {
            if (e.getException() instanceof FOPException) {
                throw (FOPException)e.getException();
            } else {
                throw new FOPException(e);
            }
        } catch (IOException e) {
            throw new FOPException(e);
        }
    }

    /**
     * creates a SAX parser
     *
     * @return the created SAX parser
     */
    public static XMLReader createParser() throws FOPException {
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

    /**
     * Dumps an error
     */
    public void dumpError(Exception e) {
        if (errorDump) {
            if (e instanceof SAXException) {
                e.printStackTrace();
                if (((SAXException)e).getException() != null) {
                    ((SAXException)e).getException().printStackTrace();
                }
            } else {
                e.printStackTrace();
            }
        }
    }

    /**
     * long or short error messages
     *
     */
    public void setDumpError(boolean dumpError) {
        errorDump = dumpError;
    }

}
