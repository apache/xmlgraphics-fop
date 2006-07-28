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

package org.apache.fop.render.ps.extensions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.util.ContentHandlerFactory;
import org.apache.fop.util.ContentHandlerFactory.ObjectBuiltListener;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * ContentHandler (parser) for restoring PSExtension objects from XML.
 */
public class PSExtensionHandler extends DefaultHandler 
            implements ContentHandlerFactory.ObjectSource {

    /** Logger instance */
    protected static Log log = LogFactory.getLog(PSExtensionHandler.class);

    private StringBuffer content = new StringBuffer();
    private Attributes lastAttributes;
    
    private PSSetupCode returnedObject;
    private ObjectBuiltListener listener;
    
    /** @see org.xml.sax.helpers.DefaultHandler */
    public void startElement(String uri, String localName, String qName, Attributes attributes) 
                throws SAXException {
        boolean handled = false;
        if (PSSetupCode.CATEGORY.equals(uri)) {
            lastAttributes = attributes;
            handled = true; 
            if ("ps-setup-code".equals(localName)) {
                //handled in endElement
            } else {
                handled = false;
            }
        }
        if (!handled) {
            if (PSSetupCode.CATEGORY.equals(uri)) {
                throw new SAXException("Unhandled element " + localName 
                        + " in namespace: " + uri);
            } else {
                log.warn("Unhandled element " + localName 
                        + " in namespace: " + uri);
            }
        }
    }

    /** @see org.xml.sax.helpers.DefaultHandler */
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (PSSetupCode.CATEGORY.equals(uri)) {
            if ("ps-setup-code".equals(localName)) {
                String name = lastAttributes.getValue("name");
                this.returnedObject = new PSSetupCode(name, content.toString());
            }
        }    
        content.setLength(0); //Reset text buffer (see characters())
    }

    /** @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int) */
    public void characters(char[] ch, int start, int length) throws SAXException {
        content.append(ch, start, length);
    }

    /**
     * @see org.xml.sax.helpers.DefaultHandler#endDocument()
     */
    public void endDocument() throws SAXException {
        if (listener != null) {
            listener.notifyObjectBuilt(getObject());
        }
    }

    /**
     * @see org.apache.fop.util.ContentHandlerFactory.ObjectSource#getObject()
     */
    public Object getObject() {
        return returnedObject;
    }

    /**
     * @see org.apache.fop.util.ContentHandlerFactory.ObjectSource
     */
    public void setObjectBuiltListener(ObjectBuiltListener listener) {
        this.listener = listener;
    }

}
