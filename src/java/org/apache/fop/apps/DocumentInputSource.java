/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */
 
package org.apache.fop.apps;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * This is an InputSource to be used with DocumentReader.
 *
 * @author Kelly A Campbell
 */
class DocumentInputSource extends InputSource {
    
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


