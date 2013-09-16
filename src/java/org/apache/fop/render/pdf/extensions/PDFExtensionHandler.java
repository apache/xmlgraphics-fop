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

import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.util.ContentHandlerFactory;
import org.apache.fop.util.ContentHandlerFactory.ObjectBuiltListener;

// CSOFF: LineLengthCheck

/**
 * ContentHandler (parser) for restoring PDF extension objects from XML.
 */
public class PDFExtensionHandler extends DefaultHandler implements ContentHandlerFactory.ObjectSource {

    /** Logger instance */
    protected static final Log log = LogFactory.getLog(PDFExtensionHandler.class);

    private PDFExtensionAttachment returnedObject;
    private ObjectBuiltListener listener;

    // PDFEmbeddedFileAttachment related state
    private Attributes lastAttributes;

    // PDFDictionaryAttachment related
    private Stack<PDFDictionaryExtension> dictionaries = new Stack<PDFDictionaryExtension>();
    private boolean captureContent;
    private StringBuffer characters;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (PDFExtensionAttachment.CATEGORY.equals(uri)) {
            if (localName.equals(PDFEmbeddedFileAttachment.ELEMENT)) {
                lastAttributes = new AttributesImpl(attributes);
            } else if (PDFDictionaryType.hasValueOfElementName(localName)) {
                PDFDictionaryExtension dictionary = new PDFDictionaryExtension(PDFDictionaryType.valueOfElementName(localName));
                String key = attributes.getValue(PDFDictionaryEntryExtension.PROPERTY_KEY);
                if (key != null) {
                    dictionary.setKey(key);
                }
                if (dictionary.getDictionaryType() == PDFDictionaryType.Page) {
                    String pageNumbers = attributes.getValue(PDFDictionaryElement.ATT_PAGE_NUMBERS);
                    if (pageNumbers != null) {
                        dictionary.setProperty(PDFDictionaryElement.ATT_PAGE_NUMBERS, pageNumbers);
                    }
                }
                dictionaries.push(dictionary);
            } else if (PDFDictionaryEntryType.hasValueOfElementName(localName)) {
                PDFDictionaryEntryExtension entry = new PDFDictionaryEntryExtension(PDFDictionaryEntryType.valueOfElementName(localName));
                String key = attributes.getValue(PDFDictionaryEntryElement.ATT_KEY);
                if (key != null) {
                    entry.setKey(key);
                }
                if (!dictionaries.empty()) {
                    PDFDictionaryExtension dictionary = dictionaries.peek();
                    dictionary.addEntry(entry);
                    captureContent = true;
                }
            } else {
                throw new SAXException("Unhandled element " + localName + " in namespace: " + uri);
            }
        } else {
            log.warn("Unhandled element " + localName + " in namespace: " + uri);
        }
    }

    @Override
    public void characters(char[] data, int start, int length) throws SAXException {
        if (captureContent) {
            if (characters == null) {
                characters = new StringBuffer((length < 16) ? 16 : length);
            }
            characters.append(data, start, length);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (PDFExtensionAttachment.CATEGORY.equals(uri)) {
            if (PDFEmbeddedFileAttachment.ELEMENT.equals(localName)) {
                String name = lastAttributes.getValue("name");
                String src = lastAttributes.getValue("src");
                String desc = lastAttributes.getValue("description");
                this.returnedObject = new PDFEmbeddedFileAttachment(name, src, desc);
            } else if (PDFDictionaryType.hasValueOfElementName(localName)) {
                if (!dictionaries.empty()) {
                    PDFDictionaryExtension dictionary = dictionaries.pop();
                    if ((dictionary.getDictionaryType() == PDFDictionaryType.Catalog) || (dictionary.getDictionaryType() == PDFDictionaryType.Page)) {
                        this.returnedObject = new PDFDictionaryAttachment(dictionary);
                    } else if (!dictionaries.empty()) {
                        PDFDictionaryExtension dictionaryOuter = dictionaries.peek();
                        dictionaryOuter.addEntry(dictionary);
                    }
                } else {
                    throw new SAXException(new IllegalStateException("no active dictionary"));
                }
            } else if (PDFDictionaryEntryType.hasValueOfElementName(localName)) {
                if (!dictionaries.empty()) {
                    PDFDictionaryExtension dictionary = dictionaries.peek();
                    PDFDictionaryEntryExtension entry = dictionary.getLastEntry();
                    if (entry != null) {
                        if (characters != null) {
                            entry.setValue(characters.toString());
                            characters = null;
                        }
                    } else {
                        throw new SAXException(new IllegalStateException("no active entry"));
                    }
                } else {
                    throw new SAXException(new IllegalStateException("no active dictionary"));
                }
            }
        }
        captureContent = false;
    }

    @Override
    public void endDocument() throws SAXException {
        if (listener != null) {
            listener.notifyObjectBuilt(getObject());
        }
    }

    public Object getObject() {
        return returnedObject;
    }

    public void setObjectBuiltListener(ObjectBuiltListener listener) {
        this.listener = listener;
    }
}
