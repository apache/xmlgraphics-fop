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

// FOP
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.extensions.ExtensionAttachment;

/**
 * Base class for the PDF-specific extension elements.
 */
public abstract class AbstractPDFExtensionElement extends FONode {

    /**Extension attachment. */
    protected PDFExtensionAttachment attachment;

    /**
     * Default constructor
     *
     * @param parent parent of this node
     * @see org.apache.fop.fo.FONode#FONode(FONode)
     */
    public AbstractPDFExtensionElement(FONode parent) {
        super(parent);
    }

    /** {@inheritDoc} */
    public String getNamespaceURI() {
        return PDFElementMapping.NAMESPACE;
    }

    /** {@inheritDoc} */
    public String getNormalNamespacePrefix() {
        return "pdf";
    }

    /**
     * Returns the extension attachment.
     * @return the extension attachment if one is created by the extension element, null otherwise.
     * @see org.apache.fop.fo.FONode#getExtensionAttachment()
     */
    public ExtensionAttachment getExtensionAttachment() {
        if (attachment == null) {
            this.attachment = (PDFExtensionAttachment)instantiateExtensionAttachment();
        }
        return this.attachment;
    }

    /**
     * Instantiates extension attachment object.
     * @return extension attachment
     */
    protected abstract ExtensionAttachment instantiateExtensionAttachment();

}

