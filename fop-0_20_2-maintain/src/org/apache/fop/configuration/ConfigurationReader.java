/*
 * $Id$
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 *
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 *
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */
package org.apache.fop.configuration;

// SAX
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;

// Java
import java.io.IOException;
import javax.xml.parsers.SAXParserFactory;

// FOP
import org.apache.fop.messaging.MessageHandler;
import org.apache.fop.apps.FOPException;

/**
 * Entry class for reading configuration from file and creating a configuration
 * class. Example of typical use:
 *
 * <pre>ConfigurationReader reader = new ConfigurationReader ("config.xml","standard");
 * try {
 *   reader.start();
 * }
 * catch (org.apache.fop.apps.FOPException error) {
 *   reader.dumpError(error);
 * }</pre>
 *
 * Once the configuration has been setup, the information can be accessed with
 * the methods of StandardConfiguration.
 *
 * @version $Revision$
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
