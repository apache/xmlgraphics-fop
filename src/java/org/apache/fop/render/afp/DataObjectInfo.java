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

import java.io.File;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.render.afp.extensions.AFPElementMapping;
import org.apache.fop.render.afp.modca.Registry;
import org.apache.fop.render.afp.modca.Registry.ObjectType;
import org.apache.xmlgraphics.util.QName;

/**
 * A list of parameters associated with an AFP data objects
 */
public abstract class DataObjectInfo {
    private static final Log log = LogFactory.getLog("org.apache.fop.afp");

    private static final String RESOURCE_NAME = "afp:resource-name";
    private static final String RESOURCE_LEVEL = "afp:resource-level";
    private static final String RESOURCE_GROUP_FILE = "afp:resource-group-file";

    /** the object area info */
    private ObjectAreaInfo objectAreaInfo;    
    
    /** object type entry */
    private Registry.ObjectType objectType;
    
    /** resource info */
    private ResourceInfo resourceInfo;
    
    /**
     * Default constructor
     */
    public DataObjectInfo() {
    }

    /**
     * Sets the object type
     * 
     * @param objectType the object type
     */    
    public void setObjectType(Registry.ObjectType objectType) {
        this.objectType = objectType;
    }

    /**
     * Returns the object type MOD:CA Registry entry
     * 
     * @return the object type MOD:CA Registry entry
     */
    public ObjectType getObjectType() {
        return objectType;
    }

    /**
     * Returns the resource level at which this data object should reside
     * 
     * @return the resource level at which this data object should reside
     */
    public ResourceInfo getResourceInfo() {
        if (resourceInfo == null) {
            this.resourceInfo = new ResourceInfo();
        }
        return resourceInfo;
    }

    /**
     * Sets the resource level at which this object should reside
     * 
     * @param resourceInfo the resource level at which this data object should reside
     */
    public void setResourceInfo(ResourceInfo resourceInfo) {
        this.resourceInfo = resourceInfo;
    }

    /**
     * Sets the object area info
     * 
     * @param objectAreaInfo the object area info
     */
    public void setObjectAreaInfo(ObjectAreaInfo objectAreaInfo) {
        this.objectAreaInfo = objectAreaInfo;
    }

    /**
     * Returns the object area info
     * 
     * @return the object area info
     */
    public ObjectAreaInfo getObjectAreaInfo() {
        return this.objectAreaInfo;
    }

    /**
     * Sets the resource group settings using the given foreign attributes
     * 
     * @param foreignAttributes a mapping of element attributes names to values
     */
    public void setResourceInfoFromForeignAttributes(Map/*<QName, String>*/ foreignAttributes) {
        if (foreignAttributes != null && !foreignAttributes.isEmpty()) {
            this.resourceInfo = new ResourceInfo();
            QName resourceNameKey = new QName(AFPElementMapping.NAMESPACE, RESOURCE_NAME);
            String resourceName = (String)foreignAttributes.get(resourceNameKey);
            if (resourceName != null) {
                resourceInfo.setName(resourceName);
            }
            QName resourceLevelKey = new QName(AFPElementMapping.NAMESPACE, RESOURCE_LEVEL);
            if (foreignAttributes.containsKey(resourceLevelKey)) {
                String level = (String)foreignAttributes.get(resourceLevelKey);
                ResourceLevel resourceLevel = null;
                try {
                    resourceLevel = ResourceLevel.valueOf(level);
                    resourceInfo.setLevel(resourceLevel);
                    if (resourceLevel.isExternal()) {
                        QName resourceGroupFileKey = new QName(AFPElementMapping.NAMESPACE,
                                RESOURCE_GROUP_FILE);
                        String resourceExternalDest
                            = (String)foreignAttributes.get(resourceGroupFileKey);
                        if (resourceExternalDest == null) {
                            String msg = RESOURCE_GROUP_FILE + " not specified";
                            log.error(msg);
                            throw new UnsupportedOperationException(msg);
                        }
                        File resourceExternalGroupFile = new File(resourceExternalDest);
                        SecurityManager security = System.getSecurityManager();
                        try {
                            if (security != null) {
                                security.checkWrite(resourceExternalGroupFile.getPath());
                            }
                        } catch (SecurityException ex) {
                            log.error("unable to gain write access to external resource file: "
                                    + resourceExternalDest);                            
                        }
                            
                        try {
                            boolean exists = resourceExternalGroupFile.exists();
                            if (exists) {
                                log.warn("overwritting external resource file: "
                                        + resourceExternalDest);
                            }
                            resourceLevel.setExternalFilePath(resourceExternalDest);
                        } catch (SecurityException ex) {
                            log.error("unable to gain read access to external resource file: "
                                    + resourceExternalDest);
                        }
                    }
                } catch (IllegalArgumentException e) {
                    // default to print-file resource level if invalid resource level provided
                    resourceLevel = new ResourceLevel(ResourceLevel.PRINT_FILE);
                    log.error(e.getMessage() + ", defaulting to '" + resourceLevel + "' level");
                }
            }
        }
    }

    /** {@inheritDoc} */
    public String toString() {
        return "mimeType=" + getMimeType()
            + (objectAreaInfo != null ? ", objectAreaInfo=" + objectAreaInfo : "")
            + (objectType != null ? ", objectType=" + objectType : "")
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
     * Returns the mime type of this data object
     * 
     * @return the mime type of this data object
     */
    public abstract String getMimeType();

}
