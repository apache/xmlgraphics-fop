/*
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * $Id$
 */

package org.apache.fop.configuration;

import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
import java.io.IOException;
import org.xml.sax.InputSource;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.Fop;

/**
 * entry class for reading configuration from file and creating a configuration
 * class. */
public class ConfigurationReader {

    /** inputsource for configuration file */
    private InputSource filename;

    private Configuration configuration;

    /**
     * creates a configuration reader and starts parsing of config file
     * @param filename the file which contains the configuration information
     */
    public ConfigurationReader(
            InputSource filename, Configuration configuration)
    throws FOPException {
        this.filename = filename;
        this.configuration = configuration;
        XMLReader parser = createParser();

        // setting the parser features
        try {
            parser.setFeature("http://xml.org/sax/features/namespace-prefixes",
                              false);
        } catch (SAXException e) {
            throw new FOPException("You need a parser which supports SAX version 2",
                                   e);
        }
        ConfigurationParser configurationParser =
            new ConfigurationParser(configuration);
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
     * creates a SAX parser, using the value of org.xml.sax.parser
     * defaulting to org.apache.xerces.parsers.SAXParser
     *
     * @return the created SAX parser
     */
    public XMLReader createParser() throws FOPException {
        String parserClassName = Fop.getParserClassName();
        configuration.logger.config(
                "configuration reader using SAX parser "
                + parserClassName);

        try {
            return (XMLReader)Class.forName(parserClassName).newInstance();
        } catch (ClassNotFoundException e) {
            throw new FOPException("Could not find " + parserClassName, e);
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
