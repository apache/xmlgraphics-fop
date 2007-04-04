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

package org.apache.fop.fo.extensions.destination;

import org.apache.fop.fo.ValidationException;
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.pagination.Root;
import org.apache.fop.fo.extensions.ExtensionElementMapping;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;

/**
 * Class for named destinations in PDF.
 */
public class Destination extends FONode {

    private String internalDestination;
    private Root root;

    /**
     * Constructs a Destination object (called by Maker).
     *
     * @param parent the parent formatting object
     */
    public Destination(FONode parent) {
        super(parent);
        root = parent.getRoot();
    }

    /**
     * @see org.apache.fop.fo.FONode#processNode(java.lang.String, org.xml.sax.Locator, 
     *          org.xml.sax.Attributes, org.apache.fop.fo.PropertyList)
     */
    public void processNode(String elementName, Locator locator, 
            Attributes attlist, PropertyList pList) throws FOPException {
        internalDestination = attlist.getValue("internal-destination");
        if (internalDestination == null || internalDestination.length() == 0) {
            attributeError("Missing attribute:  internal-destination must be specified.");
        }
    }
    
    /**
     * @see org.apache.fop.fo.FONode#endOfNode
     */
    protected void endOfNode() throws FOPException {
        root.addDestination(this);
    }

    /**
     * @see org.apache.fop.fo.FONode#validateChildNode(Locator, String, String)
        XSL/FOP: empty
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName)
        throws ValidationException {
            invalidChildError(loc, nsURI, localName);
    }

    /**
     * Returns the internal destination (an reference of the id property of any FO).
     * @return the internal destination
     */
    public String getInternalDestination() {
        return internalDestination;
    }

    /** @see org.apache.fop.fo.FONode#getNamespaceURI() */
    public String getNamespaceURI() {
        return ExtensionElementMapping.URI;
    }

    /** @see org.apache.fop.fo.FONode#getNormalNamespacePrefix() */
    public String getNormalNamespacePrefix() {
        return "fox";
    }

    /** @see org.apache.fop.fo.FONode#getLocalName() */
    public String getLocalName() {
        return "destination";
    }

}

