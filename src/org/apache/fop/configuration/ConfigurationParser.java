/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

    Copyright (C) 1999 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Fop" and  "Apache Software Foundation"  must not be used to
    endorse  or promote  products derived  from this  software without  prior
    written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 James Tauber <jtauber@jtauber.com>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

 */

package org.apache.fop.configuration;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.fop.messaging.MessageHandler;

/**
 * SAX2 Handler which retrieves the configuration information and stores them in Configuration.
 * Normally this class doesn't need to be accessed directly.
 */

public class ConfigurationParser extends DefaultHandler {
    private final int OUT = 0;
    private final int IN_ENTRY = 1;
    private final int IN_KEY = 2;
    private final int IN_VALUE = 4;
    private final int IN_LIST = 8;
    private final int IN_SUBENTRY = 16;
    private final int IN_SUBKEY = 32;

    private final int STRING = 0;
    private final int LIST = 1;
    private final int MAP = 2;

    //state of parser
    private int status = OUT;
    private int datatype = -1;

    //store the result configuration
    private static Hashtable configuration;
    private static Hashtable activeConfiguration;

    //stores key for new config entry
    private String key = "";

    //stores string value
    private String value = "";

    //stores key for new config entry
    private String subkey = "";

    //stores list value
    private Vector list = new Vector (15);

    //stores hashtable value
    private Hashtable map = new Hashtable(15);

    /** locator for line number information */
    private Locator locator;

    /** determines role / target of configuration information, default is standard */
    private String role = "standard";

    public void startDocument() {
        configuration = Configuration.getConfiguration();
    }

    /** get locator for position information */
    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }

    /**
      * extracts the element and attribute name and sets the fitting status and datatype values
      * */
    public void startElement(String uri, String localName,
                             String qName, Attributes attributes) {
        if (localName.equals("key")) {
            status += IN_KEY;
        } else if (localName.equals("value")) {
            status += IN_VALUE;
        } else if (localName.equals("list")) {
            status += IN_LIST;
        } else if (localName.equals("subentry")) {
            status += IN_SUBENTRY;
        } else if (localName.equals("entry"))   {
            //role=standard as default
            if (attributes.getLength() == 0) {
                role = "standard";
            //retrieve attribute value for "role" which determines configuration target
            } else {
                role = attributes.getValue("role");
            }
        } else if (localName.equals("configuration") ) {
        } else {
            //to make sure that user knows about false tag
            MessageHandler.errorln(
              "Unknown tag in configuration file: " + localName);
        }
    } //end startElement

    /**
     * stores subentries or entries into their hashes (map for subentries, configuration for entry)
     */
    public void endElement(String uri, String localName, String qName) {
        if (localName.equals("entry")) {
            switch (datatype) {
                case STRING:
                    this.store(role, key, value);
                    break;
                case LIST:
                    this.store(role, key, list);
                    break;
                case MAP:
                    this.store(role, key, map);
            }
            status = OUT;
            role = "standard";
        } else if (localName.equals("subentry")) {
            map.put(subkey, value);
            status -= IN_SUBENTRY;
        } else if (localName.equals("key")) {
            status -= IN_KEY;
        } else if (localName.equals("list")) {
            status -= IN_LIST;
        } else if (localName.equals("value")) {
            status -= IN_VALUE;
        }
    }

    /**
      * extracts characters from text nodes and puts them into their respective
      * variables
      */
    public void characters(char[] ch, int start, int length) {
        char characters [] = new char [length];
        System.arraycopy(ch, start, characters, 0, length);
        String text = new String(characters);
        switch (status) {
            case IN_KEY:
                key = text;
                break;
            case IN_LIST + IN_SUBENTRY + IN_KEY:
                subkey = text;
                break;
            case IN_VALUE:
                value = text;
                datatype = STRING;
                break;
            case IN_LIST + IN_VALUE:
                list.add(text);
                datatype = LIST;
                break;
            case IN_LIST + IN_SUBENTRY + IN_VALUE:
                value = text;
                datatype = MAP;
                break;
        }
    } //end characters


    /**
      * stores configuration entry into configuration hashtable according to the role
      *  
      * @param role a string containing the role / target for this configuration information
      * @param key a string containing the key value for the configuration 
      * @param value a string containing the value for the configuration 
      */
    private void store (String role, String key, Object value) {
        activeConfiguration = (Hashtable) configuration.get(role);
        if (activeConfiguration != null) {
            activeConfiguration.put(key,value);
        } else {
            MessageHandler.errorln("Unknown role >" + role + "< for new configuration entry. \n" 
              +"Putting configuration with key:" + key + " into standard configuration.");
        } 
    }


}
