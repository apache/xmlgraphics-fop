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

package org.apache.fop.render.afp.extensions;

import java.net.URI;
import java.net.URISyntaxException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.render.afp.extensions.AFPPageSegmentElement.AFPPageSegmentSetup;
import org.apache.fop.util.ContentHandlerFactory;
import org.apache.fop.util.ContentHandlerFactory.ObjectBuiltListener;

/**
 * ContentHandler (parser) for restoring AFPExtension objects from XML.
 */
public class AFPExtensionHandler extends DefaultHandler
            implements ContentHandlerFactory.ObjectSource {

    /** Logger instance */
    protected static final Log log = LogFactory.getLog(AFPExtensionHandler.class);

    private StringBuffer content = new StringBuffer();
    private Attributes lastAttributes;

    private AFPExtensionAttachment returnedObject;
    private ObjectBuiltListener listener;

    /** {@inheritDoc} */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
                throws SAXException {
        boolean handled = false;
        if (AFPExtensionAttachment.CATEGORY.equals(uri)) {
            lastAttributes = new AttributesImpl(attributes);
            handled = true;
            if (localName.equals(AFPElementMapping.NO_OPERATION)
                    || localName.equals(AFPElementMapping.TAG_LOGICAL_ELEMENT)
                    || localName.equals(AFPElementMapping.INCLUDE_PAGE_OVERLAY)
                    || localName.equals(AFPElementMapping.INCLUDE_PAGE_SEGMENT)
                    || localName.equals(AFPElementMapping.INCLUDE_FORM_MAP)
                    || localName.equals(AFPElementMapping.INVOKE_MEDIUM_MAP)) {
                //handled in endElement
            } else {
                handled = false;
            }
        }
        if (!handled) {
            if (AFPExtensionAttachment.CATEGORY.equals(uri)) {
                throw new SAXException("Unhandled element " + localName
                        + " in namespace: " + uri);
            } else {
                log.warn("Unhandled element " + localName
                        + " in namespace: " + uri);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (AFPExtensionAttachment.CATEGORY.equals(uri)) {
            if (AFPElementMapping.INCLUDE_FORM_MAP.equals(localName)) {
                AFPIncludeFormMap formMap = new AFPIncludeFormMap();
                String name = lastAttributes.getValue("name");
                formMap.setName(name);
                String src = lastAttributes.getValue("src");
                try {
                    formMap.setSrc(new URI(src));
                } catch (URISyntaxException e) {
                    throw new SAXException("Invalid URI: " + src, e);
                }
                this.returnedObject = formMap;
            } else if (AFPElementMapping.INCLUDE_PAGE_OVERLAY.equals(localName)) {
                this.returnedObject = new AFPPageOverlay();
                String name = lastAttributes.getValue("name");
                if (name != null) {
                    returnedObject.setName(name);
                }
            } else if (AFPElementMapping.INCLUDE_PAGE_SEGMENT.equals(localName)) {
                AFPPageSegmentSetup pageSetupExtn = null;

                pageSetupExtn = new AFPPageSegmentSetup(localName);
                this.returnedObject = pageSetupExtn;

                String name = lastAttributes.getValue("name");
                if (name != null) {
                    returnedObject.setName(name);
                }
                String value = lastAttributes.getValue("value");
                if (value != null && pageSetupExtn != null) {
                    pageSetupExtn.setValue(value);
                }

                String resourceSrc = lastAttributes.getValue("resource-file");
                if (resourceSrc != null && pageSetupExtn != null) {
                    pageSetupExtn.setResourceSrc(resourceSrc);
                }

                if (content.length() > 0 && pageSetupExtn != null) {
                    pageSetupExtn.setContent(content.toString());
                    content.setLength(0); //Reset text buffer (see characters())
                }
            } else {
                AFPPageSetup pageSetupExtn = null;
                if (AFPElementMapping.INVOKE_MEDIUM_MAP.equals(localName)) {
                    this.returnedObject = new AFPInvokeMediumMap();
                } else {
                    pageSetupExtn = new AFPPageSetup(localName);
                    this.returnedObject = pageSetupExtn;
                }
                String name = lastAttributes.getValue(AFPPageSetup.ATT_NAME);
                if (name != null) {
                    returnedObject.setName(name);
                }
                String value = lastAttributes.getValue(AFPPageSetup.ATT_VALUE);
                if (value != null && pageSetupExtn != null) {
                    pageSetupExtn.setValue(value);
                }
                String placement = lastAttributes.getValue(AFPPageSetup.ATT_PLACEMENT);
                if (placement != null && placement.length() > 0) {
                    pageSetupExtn.setPlacement(ExtensionPlacement.fromXMLValue(placement));
                }

                String encoding =  lastAttributes.getValue("encoding");
                if (encoding != null && pageSetupExtn != null) {
                    pageSetupExtn.setEncoding(Integer.parseInt(encoding));
                }

                if (content.length() > 0 && pageSetupExtn != null) {
                    pageSetupExtn.setContent(content.toString());
                    content.setLength(0); //Reset text buffer (see characters())
                }
            }

        }
    }

    /** {@inheritDoc} */
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        content.append(ch, start, length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endDocument() throws SAXException {
        if (listener != null) {
            listener.notifyObjectBuilt(getObject());
        }
    }

    /**
     * {@inheritDoc}
     */
    public Object getObject() {
        return returnedObject;
    }

    /**
     * {@inheritDoc}
     */
    public void setObjectBuiltListener(ObjectBuiltListener listen) {
        this.listener = listen;
    }

}
