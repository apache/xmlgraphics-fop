/* 
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the 
 * LICENSE file included with these sources."
 */


package org.apache.fop.configuration;

//sax
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;

//java
import java.util.Hashtable;
import java.util.Vector;

//fop
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
	private final int IN_FONTS = 64;
	private final int IN_FONT = 128;

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

    //stores fonts
    private Vector fontList = null;
	
    //stores information on one font
    private FontInfo fontInfo = null;

	//stores information on a font triplet
	private FontTriplet fontTriplet = null;
	
	//information on a font
	private String fontName, metricsFile, embedFile, kerningAsString;
	private boolean kerning;
	private Vector fontTriplets;
	
	//information on a font triplet
	private String fontTripletName, weight, style;
	
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
		} else if (localName.equals("fonts") ) {  //list of fonts starts
			fontList = new Vector (10);
		} else if (localName.equals("font") ) {
			kerningAsString = attributes.getValue("kerning");
			if (kerningAsString.equalsIgnoreCase("yes")) {
				kerning = true;
			} else {
				kerning = false;
			}
			metricsFile = attributes.getValue("metrics-file");
			embedFile = attributes.getValue("embed-file");
			fontName = attributes.getValue("name");
			fontTriplets = new Vector(5);
		} else if (localName.equals("font-triplet") ) {			
			fontTripletName = attributes.getValue("name");
			weight = attributes.getValue("weight"); 
			style = attributes.getValue("style");
			fontTriplet = new FontTriplet(fontTripletName,weight,style);
			fontTriplets.addElement(fontTriplet);
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
		    key = "";
		    value = "";
		} else if (localName.equals("subentry")) {
	            map.put(subkey, value);
	            status -= IN_SUBENTRY;
		    key = "";
		    value = "";
	        } else if (localName.equals("key")) {
	            status -= IN_KEY;
	        } else if (localName.equals("list")) {
	            status -= IN_LIST;
		    value = "";
		} else if (localName.equals("value")) {
	            status -= IN_VALUE;
		} else if (localName.equals("fonts") ) {
			this.store("standard", "fonts", fontList);
		} else if (localName.equals("font") ) {			
			fontInfo = new FontInfo(fontName,metricsFile,kerning,fontTriplets,embedFile);
			fontList.addElement(fontInfo);
			fontTriplets = null;
			metricsFile = null;
			embedFile = null;
			fontName = null;
			kerningAsString = "";			
		} else if (localName.equals("font-triplet") ) {			
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
                list.addElement(text);
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
