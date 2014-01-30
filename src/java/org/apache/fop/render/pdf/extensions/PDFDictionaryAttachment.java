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

    public void toSAX(ContentHandler handler) throws SAXException {
        int pageNumber = 0;
        if (extension instanceof PDFPageExtension) {
            if (handler instanceof GenerationHelperContentHandler) {
                Object context = ((GenerationHelperContentHandler) handler).getContentHandlerContext();
                if (context instanceof IFContext) {
                    int pageIndex = ((IFContext) context).getPageIndex();
                    if ((pageIndex >= 0) && ((PDFPageExtension) extension).matchesPageNumber(pageIndex + 1)) {
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
        for (PDFCollectionEntryExtension entry : dictionary.getEntries()) {
            toSAX(handler, entry);
        }
        handler.endElement(CATEGORY, ln, qn);
    }

    private void toSAX(ContentHandler handler, PDFArrayExtension array) throws SAXException {
        AttributesImpl attributes = new AttributesImpl();
        String ln = array.getElementName();
        String qn = PREFIX + ":" + ln;
        attributes = extractIFAttributes(attributes, array);
        handler.startElement(CATEGORY, ln, qn, attributes);
        for (PDFCollectionEntryExtension entry : array.getEntries()) {
            toSAX(handler, entry);
        }
        handler.endElement(CATEGORY, ln, qn);
    }

    private void toSAX(ContentHandler handler, PDFCollectionEntryExtension entry) throws SAXException {
        if (entry instanceof PDFDictionaryExtension) {
            toSAX(handler, (PDFDictionaryExtension) entry);
        } else if (entry instanceof PDFArrayExtension) {
            toSAX(handler, (PDFArrayExtension) entry);
        } else {
            AttributesImpl attributes = new AttributesImpl();
            String ln = entry.getElementName();
            String qn = PREFIX + ":" + ln;
            attributes = extractIFAttributes(attributes, entry);
            handler.startElement(CATEGORY, ln, qn, attributes);
            if (!(entry instanceof PDFReferenceExtension)) {
                char[] characters = entry.getValueAsXMLEscapedString().toCharArray();
                if (characters.length > 0) {
                    handler.characters(characters, 0, characters.length);
                }
            }
            handler.endElement(CATEGORY, ln, qn);
        }
    }

    private static AttributesImpl extractIFAttributes(AttributesImpl attributes, PDFDictionaryExtension dictionary) {
        PDFDictionaryType type = dictionary.getDictionaryType();
        if (dictionary.usesIDAttribute()) {
            String idName = PDFDictionaryElement.ATT_ID;
            String id = dictionary.getProperty(PDFDictionaryExtension.PROPERTY_ID);
            if (id != null) {
                attributes.addAttribute(null, idName, idName, "ID", id);
            }
        }
        if (type == PDFDictionaryType.Action) {
            String actionTypeName = PDFActionElement.ATT_TYPE;
            String actionType = dictionary.getProperty(PDFActionExtension.PROPERTY_TYPE);
            if (actionType != null) {
                attributes.addAttribute(null, actionTypeName, actionTypeName, "CDATA", actionType);
            }
        } else if (type == PDFDictionaryType.Page) {
            String pageNumbersName = PDFPageExtension.PROPERTY_PAGE_NUMBERS;
            String pageNumbers = dictionary.getProperty(pageNumbersName);
            if (pageNumbers != null) {
                attributes.addAttribute(null, pageNumbersName, pageNumbersName, "CDATA", pageNumbers);
            }
        } else if (type == PDFDictionaryType.Dictionary) {
            String keyName = PDFCollectionEntryElement.ATT_KEY;
            String key = dictionary.getKey();
            if (key != null) {
                attributes.addAttribute(null, keyName, keyName, "CDATA", key);
            }
        }
        return attributes;
    }

    private static AttributesImpl extractIFAttributes(AttributesImpl attributes, PDFArrayExtension array) {
        String keyName = PDFCollectionEntryExtension.PROPERTY_KEY;
        String key = array.getKey();
        if (key != null) {
            attributes.addAttribute(null, keyName, keyName, "CDATA", key);
        }
        return attributes;
    }

    private static AttributesImpl extractIFAttributes(AttributesImpl attributes, PDFCollectionEntryExtension entry) {
        String keyName = PDFCollectionEntryElement.ATT_KEY;
        String key = entry.getKey();
        if (key != null) {
            attributes.addAttribute(null, keyName, keyName, "CDATA", key);
        }
        if (entry instanceof PDFReferenceExtension) {
            String refid = ((PDFReferenceExtension) entry).getReferenceId();
            if (refid != null) {
                String refidName = PDFReferenceElement.ATT_REFID;
                attributes.addAttribute(null, refidName, refidName, "IDREF", refid);
            }
        }
        return attributes;
    }

}
