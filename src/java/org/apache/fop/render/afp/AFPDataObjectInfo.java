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

import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.render.afp.modca.Registry;

/**
 * A list of parameters associated with an AFP data objects
 */
public class AFPDataObjectInfo {
    private static final Log log = LogFactory.getLog("org.apache.fop.afp");

    /** the object area info */
    private AFPObjectAreaInfo objectAreaInfo;

    /** resource info */
    private AFPResourceInfo resourceInfo;

    /** the data object width */
    private int dataWidth;

    /** the data object height */
    private int dataHeight;

    /** the object data in an inputstream */
    private InputStream inputStream;

    /** the object registry mimetype */
    private String mimeType;

    /**
     * Default constructor
     */
    public AFPDataObjectInfo() {
    }

    /**
     * Sets the image mime type
     *
     * @param mimeType the image mime type
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * Returns the mime type of this data object
     *
     * @return the mime type of this data object
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Convenience method to return the object type
     *
     * @return the object type
     */
    public Registry.ObjectType getObjectType() {
        return Registry.getInstance().getObjectType(getMimeType());
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
        return "AFPDataObjectInfo{"
            + "mimeType=" + mimeType
            + ", dataWidth=" + dataWidth
            + ", dataHeight=" + dataHeight
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
     * Returns the image data width
     *
     * @return the image data width
     */
    public int getDataWidth() {
        return dataWidth;
    }

    /**
     * Sets the image data width
     *
     * @param imageDataWidth the image data width
     */
    public void setDataWidth(int imageDataWidth) {
        this.dataWidth = imageDataWidth;
    }

    /**
     * Returns the image data height
     *
     * @return the image data height
     */
    public int getDataHeight() {
        return dataHeight;
    }

    /**
     * Sets the image data height
     *
     * @param imageDataHeight the image data height
     */
    public void setDataHeight(int imageDataHeight) {
        this.dataHeight = imageDataHeight;
    }

    /**
     * Sets the object data inputstream
     *
     * @param inputStream the object data inputstream
     */
    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    /**
     * Returns the object data inputstream
     *
     * @return the object data inputstream
     */
    public InputStream getInputStream() {
        return this.inputStream;
    }

}
