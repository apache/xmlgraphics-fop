/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.configuration;

// sax
import org.xml.sax.Attributes;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.AttributesImpl;

// fop
import org.apache.fop.messaging.MessageHandler;
import org.apache.fop.apps.FOPException;
import org.apache.fop.configuration.ConfigurationReader;

import java.io.IOException;

import java.util.HashMap;
import java.util.ArrayList;

/**
 */
public class LanguageFileReader {

    /**
     * inputsource for languageFile file
     */
    private InputSource filename;

    /**
     * Map of languages keyed on ISO 639 code
     */
    private HashMap languages;

    /**
     * Map of countries keyed on ISO 3166 code
     */
    private HashMap countries;

    /**
     * Map of scripts keyed on ISO 15924 code
     */
    private HashMap scripts;

    /**
     * @return HashMap of language code mappings
     */
    public HashMap getLanguagesHashMap() {
        return languages;
    }

    /**
     * @return HashMap of country code mappings
     */
    public HashMap getCountriesHashMap() {
        return countries;
    }

    /**
     * @return HashMap of script code mappings
     */
    public HashMap getScriptsHashMap() {
        return scripts;
    }

    /**
     * creates a languageFile reader
     * @param filename the file which contains the languageFile information
     */
    public LanguageFileReader(InputSource filename) {
        this.filename = filename;
    }

    /**
     * intantiates parser and starts parsing of config file
     */
    public void start() throws FOPException {
        XMLReader parser = ConfigurationReader.createParser();

        // setting the parser features
        try {
            parser.setFeature
                    ("http://xml.org/sax/features/namespace-prefixes",
                     false);
        } catch (SAXException e) {
            throw new FOPException
                    ("You need a parser which supports SAX version 2",
                     e);
        }
        LanguageFileParser languageFileParser = new LanguageFileParser();
        parser.setContentHandler(languageFileParser);

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
     * SAX2 Handler which retrieves the configuration information and stores
     * them in Configuration.
     * Normally this class doesn't need to be accessed directly.
     */
    public class LanguageFileParser extends DefaultHandler {

        /**
         * Initializes empty languages and countries maps.
         */
        public void startDocument() {
            languages = new HashMap(140);
            countries = new HashMap(240);
            scripts = new HashMap(200);
        }

        /**
         * Check the element type, and for <tt>language</tt> and
         * <tt>country</tt>
         * elements, extract the <tt>code</tt> and <tt>name</tt> attribute
         * values, and put this pair in the appropriate HashMap.
         * 
         */
        public void startElement(String uri, String localName, String qName,
                                 Attributes attributes) {
            String name;
            String code;
            AttributesImpl attrs = new AttributesImpl(attributes);
            if (localName.equals("xml-lang")) {
            } else if (localName.equals("languagecodes")) {
            } else if (localName.equals("countrycodes")) {
            } else if (localName.equals("scriptcodes")) {
            } else if (localName.equals("language")
                       | localName.equals("country")
                       | localName.equals("script")) {
                // A live one
                // Extract the attribute values
                name = null;
                code = null;
                for (int i = 0; i < attrs.getLength(); i++) {
                    String attrLName = attrs.getLocalName(i);
                    if (attrLName.equals("name"))
                        name = attrs.getValue(i);
                    else if (attrLName.equals("code"))
                        code = attrs.getValue(i);
                    else MessageHandler.errorln
                                 ("Unknown attribute in "
                                  + localName + " language tag");
                }
                if (name != null && code != null) {
                    if (localName.equals("language"))
                        languages.put((Object)code, (Object)name);
                    else if (localName.equals("country"))
                        countries.put((Object)code, (Object)name);
                    else if (localName.equals("script")) {
                        scripts.put((Object)code, (Object)name);
                    }
                }
            } else {
                // to make sure that user knows about false tag
                MessageHandler.errorln("Unknown tag in languages file: "
                                       + localName);
            }
        }
    }
}
