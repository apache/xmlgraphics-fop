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

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import org.apache.fop.render.intermediate.IFContext;
import org.apache.fop.util.GenerationHelperContentHandler;

// CSOFF: LineLengthCheck

public class PDFDictionaryAttachment extends PDFExtensionAttachment {

    private static final long serialVersionUID = -5576832955238384505L;

    private PDFDictionaryExtension extension;

    public PDFDictionaryAttachment(PDFDictionaryExtension extension) {
        this.extension = extension;
    }

    public PDFDictionaryExtension getExtension() {
        return extension;
    }

    @Override
    public void toSAX(ContentHandler handler) throws SAXException {
        PDFDictionaryType dictionaryType = extension.getDictionaryType();
        int pageNumber = 0;
        if (dictionaryType == PDFDictionaryType.Page) {
            if (handler instanceof GenerationHelperContentHandler) {
                Object context = ((GenerationHelperContentHandler) handler).getContentHandlerContext();
                if (context instanceof IFContext) {
                    int pageIndex = ((IFContext) context).getPageIndex();
                    if ((pageIndex >= 0) && extension.matchesPageNumber(pageIndex + 1)) {
                        pageNumber = pageIndex + 1;
                    } else {
                        pageNumber = -1;
                    }
                }
            }
        }
        if (pageNumber >= 0) {
            toSAX(handler, extension);
        }
    }

    private void toSAX(ContentHandler handler, PDFDictionaryExtension dictionary) throws SAXException {
        AttributesImpl attributes = new AttributesImpl();
        String ln = dictionary.getElementName();
        String qn = PREFIX + ":" + ln;
        attributes = extractIFAttributes(attributes, dictionary);
        handler.startElement(CATEGORY, ln, qn, attributes);
        for (PDFDictionaryEntryExtension entry : dictionary.getEntries()) {
            toSAX(handler, entry);
        }
        handler.endElement(CATEGORY, ln, qn);
    }

    private void toSAX(ContentHandler handler, PDFDictionaryEntryExtension entry) throws SAXException {
        if (entry instanceof PDFDictionaryExtension) {
            toSAX(handler, (PDFDictionaryExtension) entry);
        } else {
            AttributesImpl attributes = new AttributesImpl();
            String ln = entry.getElementName();
            String qn = PREFIX + ":" + ln;
            attributes = extractIFAttributes(attributes, entry);
            handler.startElement(CATEGORY, ln, qn, attributes);
            char[] characters = entry.getValueAsXMLEscapedString().toCharArray();
            if (characters.length > 0) {
                handler.characters(characters, 0, characters.length);
            }
            handler.endElement(CATEGORY, ln, qn);
        }
    }

    private static AttributesImpl extractIFAttributes(AttributesImpl attributes, PDFDictionaryExtension dictionary) {
        PDFDictionaryType type = dictionary.getDictionaryType();
        if (type == PDFDictionaryType.Catalog) {
            // no specific attriburtes
        } else if (type == PDFDictionaryType.Page) {
            String pageNumbersName = PDFDictionaryExtension.PROPERTY_PAGE_NUMBERS;
            String pageNumbers = dictionary.getProperty(pageNumbersName);
            if (pageNumbers != null) {
                attributes.addAttribute(null, pageNumbersName, pageNumbersName, "CDATA", pageNumbers);
            }
        } else if (type == PDFDictionaryType.Dictionary) {
            String keyName = PDFDictionaryEntryExtension.PROPERTY_KEY;
            String key = dictionary.getKey();
            if (key != null) {
                attributes.addAttribute(null, keyName, keyName, "CDATA", key);
            }
        }
        return attributes;
    }

    private static AttributesImpl extractIFAttributes(AttributesImpl attributes, PDFDictionaryEntryExtension entry) {
        String keyName = PDFDictionaryEntryExtension.PROPERTY_KEY;
        String key = entry.getKey();
        if (key != null) {
            attributes.addAttribute(null, keyName, keyName, "CDATA", key);
        }
        return attributes;
    }

}
