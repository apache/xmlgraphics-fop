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
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.extensions.ExtensionAttachment;

// CSOFF: LineLengthCheck

/**
 * Extension element for dictionaries: pdf:{catalog,page,dictionary}. The specific type
 * of dictionary is established at construction type.
 */
public class PDFDictionaryElement extends PDFCollectionEntryElement {

    public static final String ATT_ID = PDFDictionaryExtension.PROPERTY_ID;

    /**
     * Main constructor
     * @param parent parent FO node
     */
    PDFDictionaryElement(FONode parent, PDFDictionaryType type) {
        super(parent, PDFObjectType.Dictionary, createExtension(type));
    }

    private static PDFDictionaryExtension createExtension(PDFDictionaryType type) {
        if (type == PDFDictionaryType.Action) {
            return new PDFActionExtension();
        } else if (type == PDFDictionaryType.Catalog) {
            return new PDFCatalogExtension();
        } else if (type == PDFDictionaryType.Layer) {
            return new PDFLayerExtension();
        } else if (type == PDFDictionaryType.Navigator) {
            return new PDFNavigatorExtension();
        } else if (type == PDFDictionaryType.Page) {
            return new PDFPageExtension();
        } else {
            return new PDFDictionaryExtension(type);
        }
    }

    public PDFDictionaryExtension getDictionaryExtension() {
        assert getExtension() instanceof PDFDictionaryExtension;
        return (PDFDictionaryExtension) getExtension();
    }

    @Override
    public void processNode(String elementName, Locator locator, Attributes attlist, PropertyList propertyList) throws FOPException {
        PDFDictionaryExtension extension = getDictionaryExtension();
        if (extension.usesIDAttribute()) {
            String id = attlist.getValue(ATT_ID);
            if (id != null) {
                extension.setProperty(PDFDictionaryExtension.PROPERTY_ID, id);
            }
        }
        if (extension.getDictionaryType() == PDFDictionaryType.Dictionary) {
            String key = attlist.getValue(ATT_KEY);
            if (key == null) {
                if (parent instanceof PDFDictionaryElement) {
                    missingPropertyError(ATT_KEY);
                }
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
        if (localName.equals("action")) {
            // handled in PDFActionElement subclass
        } else if (localName.equals("catalog")) {
            // handled in PDFCatalogElement subclass
        } else if (localName.equals("layer")) {
            // handled in PDFLayerElement subclass
        } else if (localName.equals("navigator")) {
            // handled in PDFNavigattorElement subclass
        } else if (localName.equals("page")) {
            // handled in PDFPageElement subclass
        } else if (localName.equals("dictionary")) {
            if (!PDFDictionaryType.hasValueOfElementName(parent.getLocalName()) && !PDFObjectType.Array.elementName().equals(parent.getLocalName())) {
                invalidChildError(getLocator(), parent.getName(), getNamespaceURI(), getName(), null);
            }
        } else {
            throw new IllegalStateException("unknown name: " + localName);
        }
    }

    @Override
    protected void addChildNode(FONode child) throws FOPException {
        PDFDictionaryExtension extension = getDictionaryExtension();
        if (child instanceof PDFDictionaryElement) {
            PDFDictionaryExtension entry = ((PDFDictionaryElement) child).getDictionaryExtension();
            if (entry.getDictionaryType() == PDFDictionaryType.Dictionary) {
                extension.addEntry(entry);
            }
        } else if (child instanceof PDFCollectionEntryElement) {
            PDFCollectionEntryExtension entry = ((PDFCollectionEntryElement) child).getExtension();
            extension.addEntry(entry);
        }
    }

    @Override
    public void endOfNode() throws FOPException {
        super.endOfNode();
    }

    @Override
    public String getLocalName() {
        PDFDictionaryExtension extension = getDictionaryExtension();
        return extension.getDictionaryType().elementName();
    }

    @Override
    protected ExtensionAttachment instantiateExtensionAttachment() {
        return new PDFDictionaryAttachment(getDictionaryExtension());
    }

}
