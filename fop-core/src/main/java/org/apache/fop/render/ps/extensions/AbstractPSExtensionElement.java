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

package org.apache.fop.render.ps.extensions;

// FOP
import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.extensions.ExtensionAttachment;

/**
 * Base class for the PostScript-specific extension elements.
 */
public abstract class AbstractPSExtensionElement extends FONode {

    /**
     * extension attachment
     */
    protected PSExtensionAttachment attachment;

    /**
     * Default constructor
     *
     * @param parent parent of this node
     * @see org.apache.fop.fo.FONode#FONode(FONode)
     */
    public AbstractPSExtensionElement(FONode parent) {
        super(parent);
    }

    /**
     * Blocks XSL FO's from having non-FO parents.
     *
     * @param loc location in the FO source file
     * @param nsURI namespace of incoming node
     * @param localName (e.g. "table" for "fo:table")
     * @throws ValidationException if incoming node not valid for parent
     * @see org.apache.fop.fo.FONode#validateChildNode(Locator, String, String)
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName)
                throws ValidationException {
        if (FO_URI.equals(nsURI)) {
            invalidChildError(loc, nsURI, localName);
        }
    }

    /** {@inheritDoc} */
    protected void characters(char[] data, int start, int length,
                                 PropertyList pList, Locator locator) {
        PSExtensionAttachment a = (PSExtensionAttachment)getExtensionAttachment();
        if (a.getContent() != null) {
            StringBuffer sb = new StringBuffer(a.getContent());
            sb.append(data, start, length);
            a.setContent(sb.toString());
        } else {
            a.setContent(new String(data, start, length));
        }
    }

    /**
     * @return a String representation of this object
     * @see org.apache.fop.fo.FONode#getNamespaceURI()
     */
    public String getNamespaceURI() {
        return PSExtensionElementMapping.NAMESPACE;
    }

    /**
     * @return a String representation of this object
     * @see org.apache.fop.fo.FONode#getNormalNamespacePrefix()
     */
    public String getNormalNamespacePrefix() {
        return "ps";
    }

    /**
     * @see org.apache.fop.fo.FONode#endOfNode()
     * @throws FOPException if there's a problem during processing
     */
    public void endOfNode() throws FOPException {
        super.endOfNode();
        String s = ((PSExtensionAttachment)getExtensionAttachment()).getContent();
        if (s == null || s.length() == 0) {
            missingChildElementError("#PCDATA");
        }
    }

    /**
     * @return the extension attachment if one is created by the extension element, null otherwise.
     * @see org.apache.fop.fo.FONode#getExtensionAttachment()
     */
    public ExtensionAttachment getExtensionAttachment() {
        if (attachment == null) {
            this.attachment = (PSExtensionAttachment)instantiateExtensionAttachment();
        }
        return this.attachment;
    }

    /**
     * Instantiates extension attachment object
     * @return extension attachment
     */
    protected abstract ExtensionAttachment instantiateExtensionAttachment();
}

