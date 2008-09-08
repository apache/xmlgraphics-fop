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

/* $Id: $ */

package org.apache.fop.render.afp;

/**
 * The level at which a resource is to reside in the AFP output
 */
public class AFPResourceInfo {
    private static final AFPResourceLevel DEFAULT_LEVEL
        = new AFPResourceLevel(AFPResourceLevel.PRINT_FILE);

    /** the uri of this resource */
    private String uri = null;

    /** the reference name of this resource */
    private String name = null;

    /** the resource level of this resource */
    private AFPResourceLevel level = DEFAULT_LEVEL;

    /**
     * Sets the data object uri
     *
     * @param uri the data object uri
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * Returns the uri of this data object
     *
     * @return the uri of this data object
     */
    public String getUri() {
        return uri;
    }

    /**
     * Sets the resource reference name
     *
     * @param resourceName the resource reference name
     */
    public void setName(String resourceName) {
        this.name = resourceName;
    }

    /**
     * Returns the resource reference name
     *
     * @return the resource reference name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the resource level
     *
     * @return the resource level
     */
    public AFPResourceLevel getLevel() {
        if (level == null) {
            return DEFAULT_LEVEL;
        }
        return this.level;
    }

    /**
     * Sets the resource level
     *
     * @param resourceLevel the resource level
     */
    public void setLevel(AFPResourceLevel resourceLevel) {
        this.level = resourceLevel;
    }

    /** {@inheritDoc} */
    public String toString() {
        return "AFPResourceInfo{uri=" + uri
            + (name != null ? ", name=" + name : "")
            + (level != null ? ", level=" + level : "")
            + "}";

    }

    /** {@inheritDoc} */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || !(obj instanceof AFPResourceInfo)) {
            return false;
        }

        AFPResourceInfo ri = (AFPResourceInfo)obj;
        return (uri == ri.uri || uri != null && uri.equals(ri.uri))
            && (name == ri.name || name != null && name.equals(ri.name))
            && (level == ri.level || level != null && level.equals(ri.level));
    }

    /** {@inheritDoc} */
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (null == uri ? 0 : uri.hashCode());
        hash = 31 * hash + (null == name ? 0 : name.hashCode());
        hash = 31 * hash + (null == level ? 0 : level.hashCode());
        return hash;
    }
}