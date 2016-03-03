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

// CSOFF: LineLengthCheck

/**
 * Extension element for pdf:page.
 */
public class PDFPageElement extends PDFDictionaryElement {

    public static final String ATT_PAGE_NUMBERS = PDFDictionaryExtension.PROPERTY_PAGE_NUMBERS;

    /**
     * Main constructor
     * @param parent parent FO node
     */
    PDFPageElement(FONode parent) {
        super(parent, PDFDictionaryType.Page);
    }

    @Override
    public void processNode(String elementName, Locator locator, Attributes attlist, PropertyList propertyList) throws FOPException {
        super.processNode(elementName, locator, attlist, propertyList);
        String pageNumbers = attlist.getValue(ATT_PAGE_NUMBERS);
        if (pageNumbers != null) {
            getDictionaryExtension().setProperty(PDFDictionaryExtension.PROPERTY_PAGE_NUMBERS, pageNumbers);
        }
    }

    @Override
    public void startOfNode() throws FOPException {
        super.startOfNode();
        if (parent.getNameId() != Constants.FO_SIMPLE_PAGE_MASTER) {
            invalidChildError(getLocator(), parent.getName(), getNamespaceURI(), getName(), "rule.childOfSPM");
        }
    }

}
