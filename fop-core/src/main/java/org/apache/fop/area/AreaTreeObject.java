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

package org.apache.fop.area;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.xmlgraphics.util.QName;

import org.apache.fop.fo.extensions.ExtensionAttachment;

/**
 * Abstract base class for all area tree objects.
 */
public abstract class AreaTreeObject implements Cloneable {

    /** Foreign attributes */
    protected Map<QName, String> foreignAttributes;

    /** Extension attachments */
    protected List<ExtensionAttachment> extensionAttachments;

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public Object clone() throws CloneNotSupportedException {
        AreaTreeObject ato = (AreaTreeObject) super.clone();
        if (foreignAttributes != null) {
            // @SuppressFBWarnings("BC_BAD_CAST_TO_CONCRETE_COLLECTION")
            ato.foreignAttributes = (Map<QName, String>)
                    ((HashMap<QName, String>)foreignAttributes).clone();
        }
        if (extensionAttachments != null) {
            // @SuppressFBWarnings("BC_BAD_CAST_TO_CONCRETE_COLLECTION")
            ato.extensionAttachments = (List<ExtensionAttachment>)
                    ((ArrayList<ExtensionAttachment>) extensionAttachments).clone();
        }
        return ato;
    }

    /**
     * Sets a foreign attribute.
     * @param name the qualified name of the attribute
     * @param value the attribute value
     */
    public void setForeignAttribute(QName name, String value) {
        if (this.foreignAttributes == null) {
            this.foreignAttributes = new HashMap<QName, String>();
        }
        this.foreignAttributes.put(name, value);
    }

    /**
     * Add foreign attributes from a Map.
     *
     * @param atts a Map with attributes (keys: QName, values: String)
     */
    public void setForeignAttributes(Map<QName, String> atts) {
        if (atts == null || atts.size() == 0) {
            return;
        }
        for (Map.Entry<QName, String> e : atts.entrySet()) {
            setForeignAttribute(e.getKey(), e.getValue());
        }
    }

    /**
     * Returns the value of a foreign attribute on the area.
     * @param name the qualified name of the attribute
     * @return the attribute value or null if it isn't set
     */
    public String getForeignAttributeValue(QName name) {
        if (this.foreignAttributes != null) {
            return this.foreignAttributes.get(name);
        } else {
            return null;
        }
    }

    /** @return the foreign attributes associated with this area */
    public Map<QName, String> getForeignAttributes() {
        if (this.foreignAttributes != null) {
            return Collections.unmodifiableMap(this.foreignAttributes);
        } else {
            return Collections.emptyMap();
        }
    }

    private void prepareExtensionAttachmentContainer() {
        if (this.extensionAttachments == null) {
            this.extensionAttachments = new ArrayList<ExtensionAttachment>();
        }
    }

    /**
     * Adds a new ExtensionAttachment instance to this page.
     * @param attachment the ExtensionAttachment
     */
    public void addExtensionAttachment(ExtensionAttachment attachment) {
        prepareExtensionAttachmentContainer();
        extensionAttachments.add(attachment);
    }

    /**
     * Set extension attachments from a List
     * @param extensionAttachments a List with extension attachments
     */
    public void setExtensionAttachments(List<ExtensionAttachment> extensionAttachments) {
        prepareExtensionAttachmentContainer();
        this.extensionAttachments.addAll(extensionAttachments);
    }

    /** @return the extension attachments associated with this area */
    public List<ExtensionAttachment> getExtensionAttachments() {
        if (this.extensionAttachments != null) {
            return Collections.unmodifiableList(this.extensionAttachments);
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Indicates whether this area tree object has any extension attachments.
     * @return true if there are extension attachments
     */
    public boolean hasExtensionAttachments() {
        return this.extensionAttachments != null && !this.extensionAttachments.isEmpty();
    }

}
