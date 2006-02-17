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

import org.apache.fop.fo.ElementMapping;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.XMLObj;
import org.apache.fop.fo.extensions.ExtensionAttachment;
import org.apache.fop.util.ContentHandlerFactory;
import org.apache.fop.util.DOMBuilderContentHandlerFactory;

/**
 * Represents the top-level "RDF" element used by XMP metadata.
 */
public class RDFElement extends XMLObj {

    private XMPMetadata attachment;
    
    /**
     * Main constructor.
     * @param parent the parent formatting object
     */
    public RDFElement(FONode parent) {
        super(parent);
    }
    
    /** @see org.apache.fop.fo.FONode#getNormalNamespacePrefix() */
    public String getNormalNamespacePrefix() {
        return "rdf";
    }

    /** @see org.apache.fop.fo.FONode#getNamespaceURI() */
    public String getNamespaceURI() {
        return XMPConstants.RDF_NAMESPACE;
    }

    /**
     * @see org.apache.fop.fo.FONode#getContentHandlerFactory()
     */
    public ContentHandlerFactory getContentHandlerFactory() {
        return new DOMBuilderContentHandlerFactory(getNamespaceURI(), 
                ElementMapping.getDefaultDOMImplementation());
    }
    
    /** @see org.apache.fop.fo.FONode#getExtensionAttachment() */
    public ExtensionAttachment getExtensionAttachment() {
        if (parent instanceof FObj) {
            if (attachment == null) {
                attachment = new XMPMetadata(doc);
            }
            return attachment;
        } else {
            return super.getExtensionAttachment();
        }
    }

    /**
     * @see org.apache.fop.fo.XMLObj#notifyObjectBuilt(java.lang.Object)
     */
    public void notifyObjectBuilt(Object obj) {
        super.notifyObjectBuilt(obj);
        attachment.setDocument(doc);
    }

}
