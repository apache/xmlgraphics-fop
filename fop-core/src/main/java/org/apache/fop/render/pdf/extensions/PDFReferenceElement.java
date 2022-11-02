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
 * Extension element for pdf:reference.
 */
public class PDFReferenceElement extends PDFCollectionEntryElement {

    public static final String ATT_REFID = PDFReferenceExtension.PROPERTY_REFID;

    /**
     * Main constructor
     * @param parent parent FO node
     */
    PDFReferenceElement(FONode parent) {
        super(parent, PDFObjectType.Reference);
    }

    @Override
    public void processNode(String elementName, Locator locator, Attributes attlist, PropertyList propertyList) throws FOPException {
        super.processNode(elementName, locator, attlist, propertyList);
        String refid = attlist.getValue(ATT_REFID);
        if (refid == null) {
            missingPropertyError(ATT_REFID);
        } else if (refid.length() == 0) {
            invalidPropertyValueError(ATT_REFID, refid, null);
        } else {
            PDFCollectionEntryExtension extension = getExtension();
            assert (extension instanceof PDFReferenceExtension);
            ((PDFReferenceExtension) extension).setReferenceId(refid);
        }
    }
}
