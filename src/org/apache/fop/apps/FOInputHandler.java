/* 
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the 
 * LICENSE file included with these sources."
 */


package org.apache.fop.apps;

// Imported SAX classes
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

//fop
import org.apache.fop.messaging.MessageHandler;

//java
import java.io.File;

/**
 * Manages input if it is an xsl:fo file
 */

public class FOInputHandler extends InputHandler {

	File fofile;
    public FOInputHandler (File fofile) {
		this.fofile = fofile;
    }

    public InputSource getInputSource () {
        return super.fileInputSource(fofile);
    }

    public XMLReader getParser() {
        XMLReader parser = super.createParser();
        if (parser == null) {
            MessageHandler.errorln("ERROR: Unable to create SAX parser");
            System.exit(1);
        }
        return parser;
    }
}

