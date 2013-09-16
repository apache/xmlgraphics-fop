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
import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.extensions.ExtensionAttachment;

// CSOFF: LineLengthCheck

/**
 * Extension element for dictionaries: pdf:{catalog,page,dictionary}. The specific type
 * of dictionary is established at construction type.
 */
public class PDFDictionaryElement extends AbstractPDFDictionaryElement {

    public static final String ATT_PAGE_NUMBERS = PDFDictionaryExtension.PROPERTY_PAGE_NUMBERS;

    private PDFDictionaryExtension extension;

    /**
     * Main constructor
     * @param parent parent FO node
     */
    PDFDictionaryElement(FONode parent, PDFDictionaryType type) {
        super(parent);
        this.extension = new PDFDictionaryExtension(type);
    }

    public PDFDictionaryExtension getExtension() {
        return extension;
    }

    @Override
    public void processNode(String elementName, Locator locator, Attributes attlist, PropertyList propertyList) throws FOPException {
        if (extension.getDictionaryType() == PDFDictionaryType.Catalog) {
            // no specific properties
        } else if (extension.getDictionaryType() == PDFDictionaryType.Page) {
            String pageNumbers = attlist.getValue(ATT_PAGE_NUMBERS);
            if (pageNumbers != null) {
                extension.setProperty(ATT_PAGE_NUMBERS, pageNumbers);
            }
        } else if (extension.getDictionaryType() == PDFDictionaryType.Dictionary) {
            String key = attlist.getValue(ATT_KEY);
            if (key == null) {
                missingPropertyError(ATT_KEY);
            } else if (key.length() == 0) {
                invalidPropertyValueError(ATT_KEY, key, null);
            } else {
                extension.setKey(key);
            }
        }
    }

    @Override
    public void startOfNode() throws FOPException {
        super.startOfNode();
        String localName = getLocalName();
        if (localName.equals("catalog")) {
            if (parent.getNameId() != Constants.FO_DECLARATIONS) {
                invalidChildError(getLocator(), parent.getName(), getNamespaceURI(), getName(), "rule.childOfDeclarations");
            }
        } else if (localName.equals("page")) {
            if (parent.getNameId() != Constants.FO_SIMPLE_PAGE_MASTER) {
                invalidChildError(getLocator(), parent.getName(), getNamespaceURI(), getName(), "rule.childOfSPM");
            }
        } else if (localName.equals("dictionary")) {
            if (!PDFDictionaryType.hasValueOfElementName(parent.getLocalName())) {
                invalidChildError(getLocator(), parent.getName(), getNamespaceURI(), getName(), null);
            }
        } else {
            throw new IllegalStateException("unknown name: " + localName);
        }
    }

    @Override
    protected void addChildNode(FONode child) throws FOPException {
        if (child instanceof PDFDictionaryElement) {
            PDFDictionaryExtension extension = ((PDFDictionaryElement) child).getExtension();
            if (extension.getDictionaryType() == PDFDictionaryType.Dictionary) {
                this.extension.addEntry(extension);
            }
        } else if (child instanceof PDFDictionaryEntryElement) {
            PDFDictionaryEntryExtension extension = ((PDFDictionaryEntryElement) child).getExtension();
            this.extension.addEntry(extension);
        }
    }

    @Override
    public void endOfNode() throws FOPException {
        super.endOfNode();
    }

    @Override
    public String getLocalName() {
        return extension.getDictionaryType().elementName();
    }

    @Override
    protected ExtensionAttachment instantiateExtensionAttachment() {
        return new PDFDictionaryAttachment(extension);
    }

}
