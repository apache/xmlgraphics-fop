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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.render.afp.modca.Registry;

/**
 * A list of parameters associated with an AFP data objects
 */
public abstract class AFPDataObjectInfo {
    private static final Log log = LogFactory.getLog("org.apache.fop.afp");

    /** the object area info */
    private AFPObjectAreaInfo objectAreaInfo;

    /** resource info */
    private AFPResourceInfo resourceInfo;

    private byte[] data;

    /**
     * Default constructor
     */
    public AFPDataObjectInfo() {
    }

    /**
     * Returns the resource level at which this data object should reside
     *
     * @return the resource level at which this data object should reside
     */
    public AFPResourceInfo getResourceInfo() {
        if (resourceInfo == null) {
            this.resourceInfo = new AFPResourceInfo();
        }
        return resourceInfo;
    }

    /**
     * Sets the resource level at which this object should reside
     *
     * @param resourceInfo the resource level at which this data object should reside
     */
    public void setResourceInfo(AFPResourceInfo resourceInfo) {
        this.resourceInfo = resourceInfo;
    }

    /**
     * Sets the object area info
     *
     * @param objectAreaInfo the object area info
     */
    public void setObjectAreaInfo(AFPObjectAreaInfo objectAreaInfo) {
        this.objectAreaInfo = objectAreaInfo;
    }

    /**
     * Returns the object area info
     *
     * @return the object area info
     */
    public AFPObjectAreaInfo getObjectAreaInfo() {
        return this.objectAreaInfo;
    }

    /** {@inheritDoc} */
    public String toString() {
        return "mimeType=" + getMimeType()
            + (objectAreaInfo != null ? ", objectAreaInfo=" + objectAreaInfo : "")
            + (resourceInfo != null ? ", resourceInfo=" + resourceInfo : "");
    }

    /**
     * Returns the uri of this data object
     *
     * @return the uri of this data object
     */
    public String getUri() {
        return getResourceInfo().getUri();
    }

    /**
     * Sets the data object uri
     *
     * @param uri the data object uri
     */
    public void setUri(String uri) {
        getResourceInfo().setUri(uri);
    }

    /**
     * Sets the object data
     *
     * @param data a data byte array
     */
    public void setData(byte[] data) {
        this.data = data;
    }

    /**
     * Returns the object data
     *
     * @return the object data as byte array
     */
    public byte[] getData() {
        return this.data;
    }

    /**
     * Returns the mime type of this data object
     *
     * @return the mime type of this data object
     */
    public abstract String getMimeType();

    /**
     * Convenience method to return the object type
     *
     * @return the object type
     */
    public Registry.ObjectType getObjectType() {
        return Registry.getInstance().getObjectType(getMimeType());
    }
}
