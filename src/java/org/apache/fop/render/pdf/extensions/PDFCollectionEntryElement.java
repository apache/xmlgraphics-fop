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

// CSOFF: LineLengthCheck

/**
 * Extension element for collection entries: pdf:{array,boolean,dictionary,name,number,reference,string}. The specific type
 * of entry is established at construction type.
 */
public class PDFCollectionEntryElement extends AbstractPDFExtensionElement {

    public static final String ATT_KEY = PDFCollectionEntryExtension.PROPERTY_KEY;

    private PDFCollectionEntryExtension extension;
    private StringBuffer characters;

    PDFCollectionEntryElement(FONode parent, PDFObjectType type, PDFCollectionEntryExtension extension) {
        super(parent);
        this.extension = extension;
    }

    PDFCollectionEntryElement(FONode parent, PDFObjectType type) {
        this(parent, type, createExtension(type));
    }

    private static PDFCollectionEntryExtension createExtension(PDFObjectType type) {
        if (type == PDFObjectType.Reference) {
            return new PDFReferenceExtension();
        } else {
            return new PDFCollectionEntryExtension(type);
        }
    }

    public PDFCollectionEntryExtension getExtension() {
        return extension;
    }

    @Override
    public void processNode(String elementName, Locator locator, Attributes attlist, PropertyList propertyList) throws FOPException {
        if (parent instanceof PDFDictionaryElement) {
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
        if (parent instanceof PDFDictionaryElement) {
            if (!PDFDictionaryType.hasValueOfElementName(parent.getLocalName())) {
                invalidChildError(getLocator(), parent.getName(), getNamespaceURI(), getName(), null);
            }
        }
    }

    @Override
    protected void characters(char[] data, int start, int length, PropertyList pList, Locator locator) throws FOPException {
        if (capturePCData(extension.getType())) {
            if (characters == null) {
                characters = new StringBuffer((length < 16) ? 16 : length);
            }
            characters.append(data, start, length);
        }
    }

    private boolean capturePCData(PDFObjectType type) {
        if (type == PDFObjectType.Array) {
            return false;
        } else if (type == PDFObjectType.Dictionary) {
            return false;
        } else {
            return (type != PDFObjectType.Reference);
        }
    }

    @Override
    public void endOfNode() throws FOPException {
        if (capturePCData(extension.getType())) {
            if (extension.getType() == PDFObjectType.Boolean) {
                String value = (characters != null) ? characters.toString() : "";
                if (!value.equals("true") && !value.equals("false")) {
                    invalidPropertyValueError("<value>", value, null);
                }
                extension.setValue(Boolean.valueOf(value));
            } else if (extension.getType() == PDFObjectType.Name) {
                String value = (characters != null) ? characters.toString() : "";
                if (value.length() == 0) {
                    invalidPropertyValueError("<value>", value, null);
                }
                extension.setValue(value);
            } else if (extension.getType() == PDFObjectType.Number) {
                String value = (characters != null) ? characters.toString() : "";
                try {
                    double d = Double.parseDouble(value);
                    if (Math.abs(Math.floor(d) - d) < 1E-10) {
                        extension.setValue(Long.valueOf((long) d));
                    } else {
                        extension.setValue(Double.valueOf(d));
                    }
                } catch (NumberFormatException e) {
                    invalidPropertyValueError("<value>", value, null);
                }
            } else if (extension.getType() == PDFObjectType.String) {
                String value = (characters != null) ? characters.toString() : "";
                extension.setValue(value);
            }
        }
        super.endOfNode();
    }

    @Override
    public String getLocalName() {
        return extension.getType().elementName();
    }
}
