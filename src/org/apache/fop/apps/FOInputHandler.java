/*
 * $Id$
 * Copyright (C) 2001-2003 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.apps;

// Imported SAX classes
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

// java
import java.io.File;
import java.net.URL;

/**
 * Manages input if it is an xsl:fo file
 */
public class FOInputHandler extends InputHandler {
    private File fofile = null;
    private URL foURL = null;

    /*
     * Create a FOInputHandler for a file.
     * @param file the file to read the FO document.
     */
    public FOInputHandler (File fofile) {
        this.fofile = fofile;
    }

    /*
     * Create a FOInputHandler for an URL.
     * @param file the URL to read the FO document.
     */
    public FOInputHandler (URL url) {
        this.foURL = url;
    }

    
    /*
     * Get the input source associated with this input handler.
     */
    public InputSource getInputSource () {
        if (fofile != null) {
            return super.fileInputSource(fofile);
        }
        return super.urlInputSource(foURL);
    }

    /*
     * Get the SAX parser associated with this input handler.
     */
    public XMLReader getParser() throws FOPException {
        return super.createParser();
    }

}

