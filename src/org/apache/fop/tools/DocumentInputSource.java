/*
 * $Id$
 * Copyright (C) 2001-2003 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.tools;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * This is an InputSource to be used with DocumentReader.
 *
 * @author Kelly A Campbell
 */
public class DocumentInputSource extends InputSource {
    
    private Document document;

    /**
     * Default constructor.
     */
    public DocumentInputSource() {
        super();
    }

    /**
     * Main constructor
     * @param document the DOM document to use as input
     */
    public DocumentInputSource(Document document) {
        this();
        setDocument(document);
    }

    /**
     * Returns the input document.
     * @return the input DOM document.
     */
    public Document getDocument() {
        return this.document;
    }

    /**
     * Sets the input document.
     * @param document the DOM document to use as input
     */
    public void setDocument(Document document) {
        this.document = document;
    }

}


