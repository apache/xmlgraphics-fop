/* 
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the 
 * LICENSE file included with these sources."
 */


package org.apache.fop.apps;

// SAX
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;

// Java
import java.io.*;
import java.net.URL;

// FOP
import org.apache.fop.messaging.MessageHandler;


/**
 * 
 * abstract super class 
 * Creates a SAX Parser (defaulting to Xerces).
 *
 */
public abstract class Starter {

	Options options;
	InputHandler inputHandler;
	
	public Starter() {
		options = new Options ();		
	}

	public void setInputHandler(InputHandler inputHandler) {
		this.inputHandler = inputHandler;
	}

	abstract public void run();

	// setting the parser features	
	public void setParserFeatures (XMLReader parser) {
		setParserFeatures (parser,true);
	}
	

    public void setParserFeatures (XMLReader parser,boolean errorDump) {
        try {
            parser.setFeature("http://xml.org/sax/features/namespace-prefixes",true);
        } catch (SAXException e) {
            MessageHandler.errorln("Error in setting up parser feature namespace-prefixes");
            MessageHandler.errorln("You need a parser which supports SAX version 2");
            if (errorDump) {
                e.printStackTrace();
            }
            System.exit(1);
        }
    }
}
