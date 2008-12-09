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

package org.apache.fop.render.afp.extensions;

// FOP
import org.xml.sax.Attributes;
import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.extensions.ExtensionAttachment;

/**
 * Base class for the AFP-specific extension elements.
 */
public abstract class AbstractAFPExtensionObject extends FONode {

    /**
     * the AFP extension attachment
     */
    protected AFPExtensionAttachment extensionAttachment;

    /**
     * the element name of this extension
     */
    protected String name;

    /**
     * @see org.apache.fop.fo.FONode#FONode(FONode)
     * @param parent the parent formatting object
     * @param name the name of the afp element
     */
    public AbstractAFPExtensionObject(FONode parent, String name) {
        super(parent);
        this.name = name;
    }

    /**
     * {@inheritDoc}
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName)
                throws ValidationException {
        if (FO_URI.equals(nsURI)) {
            invalidChildError(loc, nsURI, localName);
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void characters(char[] data, int start, int length,
                                 PropertyList pList, Locator locator) throws FOPException {
        ((AFPExtensionAttachment)getExtensionAttachment()).setContent(
                new String(data, start, length));       
    }

    /**
     * {@inheritDoc}
     */
    public String getNamespaceURI() {
        return AFPElementMapping.NAMESPACE;
    }

    /**
     * {@inheritDoc}
     */
    public String getNormalNamespacePrefix() {
        return AFPElementMapping.NAMESPACE_PREFIX;
    }

    /**
     * {@inheritDoc}
     */
    public void processNode(String elementName, Locator locator,
                            Attributes attlist, PropertyList propertyList)
                                throws FOPException {
        getExtensionAttachment();
        String attr = attlist.getValue("name");
        if (attr != null && attr.length() > 0) {
            extensionAttachment.setName(attr);
        } else {
            throw new FOPException(elementName + " must have a name attribute.");
        }
        if (AFPElementMapping.INCLUDE_PAGE_SEGMENT.equals(elementName)) {
            attr = attlist.getValue("src");
            if (attr != null && attr.length() > 0) {
                extensionAttachment.setValue(attr);
            } else {
                throw new FOPException(elementName + " must have a src attribute.");
            }
        } else if (AFPElementMapping.TAG_LOGICAL_ELEMENT.equals(elementName)) {
            attr = attlist.getValue("value");
            if (attr != null && attr.length() > 0) {
                extensionAttachment.setValue(attr);
            } else {
                throw new FOPException(elementName + " must have a value attribute.");
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    protected void endOfNode() throws FOPException {
        super.endOfNode();
    }

    /**
     * Instantiates extension attachment object
     * @return extension attachment
     */
    protected abstract ExtensionAttachment instantiateExtensionAttachment();

    /**
     * {@inheritDoc}
     */
    public ExtensionAttachment getExtensionAttachment() {
        if (extensionAttachment == null) {
            this.extensionAttachment = (AFPExtensionAttachment)instantiateExtensionAttachment();
        }
        return this.extensionAttachment;
    }

    /**
     * {@inheritDoc}
     */
    public String getLocalName() {
        return name;
    }
}
