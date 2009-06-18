/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
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

    File fofile = null;
    URL foURL = null;
    public FOInputHandler (File fofile) {
        this.fofile = fofile;
    }

    public FOInputHandler (URL url) {
        this.foURL = url;
    }

    public InputSource getInputSource () {
        if (fofile != null)
            return super.fileInputSource(fofile);
        return super.urlInputSource(foURL);
    }

    public XMLReader getParser() throws FOPException {
        return super.createParser();
    }

}

