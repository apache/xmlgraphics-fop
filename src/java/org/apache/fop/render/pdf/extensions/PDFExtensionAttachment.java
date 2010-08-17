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

import org.apache.xmlgraphics.util.XMLizable;

import org.apache.fop.fo.extensions.ExtensionAttachment;

/**
 * This is the pass-through value object for the PDF extension.
 */
public abstract class PDFExtensionAttachment implements ExtensionAttachment, XMLizable {

    /** The category URI for this extension attachment. */
    public static final String CATEGORY = "apache:fop:extensions:pdf";

    /**
     * Default constructor.
     */
    public PDFExtensionAttachment() {
        //nop
    }

    /**
     * @return the category URI
     * @see org.apache.fop.fo.extensions.ExtensionAttachment#getCategory()
     */
    public String getCategory() {
        return CATEGORY;
    }

    /** @return type name */
    public String getType() {
        String className = getClass().getName();
        return className.substring(className.lastIndexOf('.') + 3);
    }

    /**
     * @return a string representation of this object
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return getType();
    }

    /** @return element */
    protected abstract String getElement();
}
