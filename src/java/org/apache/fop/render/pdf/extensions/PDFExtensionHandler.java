/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

package org.apache.fop.render.pdf.extensions;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.util.ContentHandlerFactory;
import org.apache.fop.util.ContentHandlerFactory.ObjectBuiltListener;

/**
 * ContentHandler (parser) for restoring PDF extension objects from XML.
 */
public class PDFExtensionHandler extends DefaultHandler
            implements ContentHandlerFactory.ObjectSource {

    /** Logger instance */
    protected static Log log = LogFactory.getLog(PDFExtensionHandler.class);

    private Attributes lastAttributes;

    private PDFExtensionAttachment returnedObject;
    private ObjectBuiltListener listener;

    /** {@inheritDoc} */
    public void startElement(String uri, String localName, String qName, Attributes attributes)
                throws SAXException {
        boolean handled = false;
        if (PDFExtensionAttachment.CATEGORY.equals(uri)) {
            lastAttributes = new AttributesImpl(attributes);
            handled = false;
            if (localName.equals(PDFEmbeddedFileExtensionAttachment.ELEMENT)) {
                //handled in endElement
                handled = true;
            }
        }
        if (!handled) {
            if (PDFExtensionAttachment.CATEGORY.equals(uri)) {
                throw new SAXException("Unhandled element " + localName
                        + " in namespace: " + uri);
            } else {
                log.warn("Unhandled element " + localName
                        + " in namespace: " + uri);
            }
        }
    }

    /** {@inheritDoc} */
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (PDFExtensionAttachment.CATEGORY.equals(uri)) {
            if (PDFEmbeddedFileExtensionAttachment.ELEMENT.equals(localName)) {
                String name = lastAttributes.getValue("name");
                String src = lastAttributes.getValue("src");
                String desc = lastAttributes.getValue("description");
                this.returnedObject = new PDFEmbeddedFileExtensionAttachment(name, src, desc);
            }
        }
    }

    /** {@inheritDoc} */
    public void endDocument() throws SAXException {
        if (listener != null) {
            listener.notifyObjectBuilt(getObject());
        }
    }

    /** {@inheritDoc} */
    public Object getObject() {
        return returnedObject;
    }

    /** {@inheritDoc} */
    public void setObjectBuiltListener(ObjectBuiltListener listener) {
        this.listener = listener;
    }
}
