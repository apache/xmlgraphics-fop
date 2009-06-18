/*
 * Copyright 2006 The Apache Software Foundation.
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

package org.apache.fop.fo.extensions.xmp;

import java.io.Serializable;

import org.apache.fop.fo.extensions.ExtensionAttachment;
import org.apache.fop.util.DOM2SAX;
import org.apache.fop.util.XMLizable;
import org.w3c.dom.Document;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * This is the pass-through value object for the XMP metadata extension.
 */
public class XMPMetadata implements ExtensionAttachment, Serializable, XMLizable {

    /** The category URI for this extension attachment. */
    public static final String CATEGORY = XMPConstants.XMP_NAMESPACE;
    
    private Document doc;
    private boolean readOnly = true;

    /**
     * No-argument contructor.
     */
    public XMPMetadata() {
        //nop
    }
    
    /**
     * Default constructor.
     * @param doc the DOM document containing the XMP metadata
     */
    public XMPMetadata(Document doc) {
        this.doc = doc;
    }
    
    /** @return the DOM document containing the XMP metadata */
    public Document getDocument() {
        return this.doc;
    }
    
    /**
     * Sets the DOM document containing the XMP metadata.
     * @param document the DOM document
     */
    public void setDocument(Document document) {
        this.doc = document;
    }
    
    /** @return true if the XMP metadata is marked read-only. */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * Sets the flag that decides whether a metadata packet may be modified.
     * @param readOnly true if the XMP metadata packet should be marked read-only.
     */
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    /** @see org.apache.fop.fo.extensions.ExtensionAttachment#getCategory() */
    public String getCategory() {
        return CATEGORY;
    }
    
    /** @see org.apache.fop.util.XMLizable#toSAX(org.xml.sax.ContentHandler) */
    public void toSAX(ContentHandler handler) throws SAXException {
        new DOM2SAX(handler).writeDocument(getDocument(), true);
    }
    
}
