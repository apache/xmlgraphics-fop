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
 * Extension element for pdf:action.
 */
public class PDFActionElement extends PDFDictionaryElement {

    public static final String ATT_TYPE = PDFActionExtension.PROPERTY_TYPE;

    /**
     * Main constructor
     * @param parent parent FO node
     */
    PDFActionElement(FONode parent) {
        super(parent, PDFDictionaryType.Action);
    }

    @Override
    public void processNode(String elementName, Locator locator, Attributes attlist, PropertyList propertyList) throws FOPException {
        super.processNode(elementName, locator, attlist, propertyList);
        String type = attlist.getValue(ATT_TYPE);
        if (type != null) {
            getDictionaryExtension().setProperty(PDFActionExtension.PROPERTY_TYPE, type);
        }
    }

    @Override
    public void startOfNode() throws FOPException {
        super.startOfNode();
        if (parent.getNameId() != Constants.FO_DECLARATIONS) {
            invalidChildError(getLocator(), parent.getName(), getNamespaceURI(), getName(), "rule.childOfDeclarations");
        }
    }

}
