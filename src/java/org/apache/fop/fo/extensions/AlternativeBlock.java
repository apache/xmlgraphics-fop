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

package org.apache.fop.fo.extensions;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;

/**
 * A class modeling fox:alternative-block
 */

public class AlternativeBlock extends FObj {

    public static final String OBJECT_NAME = "alternative-block";

    public AlternativeBlock(FONode parent) {
        super(parent);
    }

    public void processNode(String elementName, Locator locator,
            Attributes attlist, PropertyList pList) throws FOPException {
        if (log.isDebugEnabled()) {
            log.debug("org.apache.fop.fo.extensions.AlternativeBlock: " + elementName
                    + (locator != null ? " at " + getLocatorString(locator) : ""));
        }
    }

    public void startOfNode() throws FOPException {
        if (log.isDebugEnabled())
            log.debug("AlternativeBlock.startOfNode()");
    }

    public void endOfNode() throws FOPException {
        if (log.isDebugEnabled())
            log.debug("AlternativeBlock.endOfNode()");
    }

    /**
     * Content model: see {@link Block}
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName)
            throws ValidationException {
        if (FOX_URI.equals(nsURI)) {
            if ("best-fit".equals(localName) || "alternative-block".equals(localName)) {
                invalidChildError(loc, FOX_URI, localName);
            }
        }
        else if (FO_URI.equals(nsURI)) {
            if (!isBlockOrInlineItem(nsURI, localName)) {
                invalidChildError(loc, FO_URI, localName);
            }
        }
    }

    @Override
    public String getLocalName() {
        return OBJECT_NAME;
    }

    @Override
    public String getNormalNamespacePrefix() {
        return ExtensionElementMapping.STANDARD_PREFIX;
    }

    public String getNamespaceURI() {
        return ExtensionElementMapping.URI;
    }

}
