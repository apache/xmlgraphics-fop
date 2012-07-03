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

package org.apache.fop.afp;

import java.net.URI;

import static org.apache.fop.afp.AFPResourceLevel.ResourceType.DOCUMENT;
import static org.apache.fop.afp.AFPResourceLevel.ResourceType.EXTERNAL;
import static org.apache.fop.afp.AFPResourceLevel.ResourceType.INLINE;
import static org.apache.fop.afp.AFPResourceLevel.ResourceType.PAGE;
import static org.apache.fop.afp.AFPResourceLevel.ResourceType.PAGE_GROUP;
import static org.apache.fop.afp.AFPResourceLevel.ResourceType.PRINT_FILE;

/**
 * A resource level
 */
public class AFPResourceLevel {
    public enum ResourceType {
        /** directly in page **/
        INLINE("inline"),
        /** page level **/
        PAGE("page"),
        /** page group level **/
        PAGE_GROUP("page-group"),
        /** document level **/
        DOCUMENT("document"),
        /** print file level **/
        PRINT_FILE("print-file"),
        /** external level **/
        EXTERNAL("external");

        private final String name;

        private ResourceType(String name) {
            this.name = name;
        }

        public static ResourceType getValueOf(String levelString) {
            for (ResourceType resType : ResourceType.values()) {
                if (resType.name.equalsIgnoreCase(levelString)) {
                    return resType;
                }
            }
            return null;
        }

        public String getName() {
            return name;
        }
    }

    /** the external resource group file path */
    private URI extUri = null;
    private ResourceType resourceType;

    /**
     * Sets the resource placement level within the AFP output
     *
     * @param levelString the resource level (page, page-group, document, print-file or external)
     * @return true if the resource level was successfully set
     */
    public static AFPResourceLevel valueOf(String levelString) {
        ResourceType resType = ResourceType.getValueOf(levelString);
        return resType != null ? new AFPResourceLevel(resType) : null;
    }

    /**
     * Main constructor
     *
     * @param level the resource level
     */
    public AFPResourceLevel(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    /**
     * Returns true if this is at page level
     *
     * @return true if this is at page level
     */
    public boolean isPage() {
        return resourceType == PAGE;
    }

    /**
     * Returns true if this is at page group level
     *
     * @return true if this is at page group level
     */
    public boolean isPageGroup() {
        return resourceType == PAGE_GROUP;
    }

    /**
     * Returns true if this is at document level
     *
     * @return true if this is at document level
     */
    public boolean isDocument() {
        return resourceType == DOCUMENT;
    }

    /**
     * Returns true if this is at external level
     *
     * @return true if this is at external level
     */
    public boolean isExternal() {
        return resourceType == EXTERNAL;
    }

    /**
     * Returns true if this is at print-file level
     *
     * @return true if this is at print-file level
     */
    public boolean isPrintFile() {
        return resourceType == PRINT_FILE;
    }

    /**
     * Returns true if this resource level is inline
     *
     * @return true if this resource level is inline
     */
    public boolean isInline() {
        return resourceType == INLINE;
    }

    /**
     * Returns the URI of the external resource group.
     *
     * @return the destination URI of the external resource group
     */
    public URI getExternalURI() {
        return this.extUri;
    }

    /**
     * Sets the URI of the external resource group.
     *
     * @param filePath the URI of the external resource group
     */
    public void setExternalUri(URI uri) {
        this.extUri = uri;
    }

    /** {@inheritDoc} */
    public String toString() {
        return resourceType + (isExternal() ? ", uri=" + extUri : "");
    }

    /** {@inheritDoc} */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || !(obj instanceof AFPResourceLevel)) {
            return false;
        }

        AFPResourceLevel rl = (AFPResourceLevel)obj;
        return (resourceType == rl.resourceType)
            && (extUri == rl.extUri
                    || extUri != null && extUri.equals(rl.extUri));
    }

    /** {@inheritDoc} */
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + resourceType.hashCode();
        hash = 31 * hash + (null == extUri ? 0 : extUri.hashCode());
        return hash;
    }
}
