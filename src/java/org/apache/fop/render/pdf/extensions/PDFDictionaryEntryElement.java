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
 * Extension element for dictionary entries: pdf:{boolean,name,number,string}. The specific type
 * of entry is established at construction type.
 */
public class PDFDictionaryEntryElement extends AbstractPDFDictionaryElement {

    private PDFDictionaryEntryExtension extension;
    private StringBuffer characters;

    /**
     * Main constructor
     * @param parent parent FO node
     */
    PDFDictionaryEntryElement(FONode parent, PDFDictionaryEntryType type) {
        super(parent);
        this.extension = new PDFDictionaryEntryExtension(type);
    }

    public PDFDictionaryEntryExtension getExtension() {
        return extension;
    }

    @Override
    public void processNode(String elementName, Locator locator, Attributes attlist, PropertyList propertyList) throws FOPException {
        String key = attlist.getValue("key");
        if (key == null) {
            missingPropertyError("key");
        } else if (key.length() == 0) {
            invalidPropertyValueError("key", key, null);
        } else {
            extension.setKey(key);
        }
    }

    @Override
    public void startOfNode() throws FOPException {
        super.startOfNode();
        if (!PDFDictionaryType.hasValueOfElementName(parent.getLocalName())) {
            invalidChildError(getLocator(), parent.getName(), getNamespaceURI(), getName(), null);
        }
    }

    @Override
    protected void characters(char[] data, int start, int length, PropertyList pList, Locator locator) throws FOPException {
        if (characters == null) {
            characters = new StringBuffer((length < 16) ? 16 : length);
        }
        characters.append(data, start, length);
    }

    @Override
    public void endOfNode() throws FOPException {
        String value = (characters != null) ? characters.toString() : "";
        if (extension.getType() == PDFDictionaryEntryType.Boolean) {
            if (!value.equals("true") && !value.equals("false")) {
                invalidPropertyValueError("<value>", value, null);
            }
        } else if (extension.getType() == PDFDictionaryEntryType.Name) {
            if (value.length() == 0) {
                invalidPropertyValueError("<value>", value, null);
            }
        } else if (extension.getType() == PDFDictionaryEntryType.Number) {
            try {
                Double.valueOf(value);
            } catch (NumberFormatException e) {
                invalidPropertyValueError("<value>", value, null);
            }
        } else if (extension.getType() != PDFDictionaryEntryType.String) {
            throw new IllegalStateException();
        }
        extension.setValue(value);
        super.endOfNode();
    }

    @Override
    public String getLocalName() {
        return extension.getType().elementName();
    }
}
