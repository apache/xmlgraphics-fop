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
public class DataObjectInfo {
    private static final Log log = LogFactory.getLog("org.apache.fop.afp");

    private static final String RESOURCE_NAME = "afp:resource-name";
    private static final String RESOURCE_LEVEL = "afp:resource-level";
    private static final String RESOURCE_GROUP_FILE = "afp:resource-group-file";

    private static final ResourceInfo DEFAULT_RESOURCE_INFO = new ResourceInfo();
    
    private String uri;
    private int x;
    private int y;
    private int width;
    private int height;
    private int widthRes;
    private int heightRes;
    private int rotation = 0;
    
    /** object type entry */
    private ObjectType objectType;
    
    /** resource info */
    private ResourceInfo resourceInfo = DEFAULT_RESOURCE_INFO;
    
    /**
     * Sets the data object uri
     * @param uri the data object uri
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * Sets the x position of the data object
     * @param x the x position of the data object
     */
    protected void setX(int x) {
        this.x = x;
    }

    /**
     * Sets the y position of the data object
     * @param y the y position of the data object
     */
    protected void setY(int y) {
        this.y = y;
    }

    /**
     * Sets the data object width
     * @param width the width of the data object
     */
    protected void setWidth(int width) {
        this.width = width;
    }

    /**
     * Sets the data object height
     * @param height the height of the data object
     */
    protected void setHeight(int height) {
        this.height = height;
    }

    /**
     * Sets the width resolution
     * @param widthRes the width resolution
     */
    protected void setWidthRes(int widthRes) {
        this.widthRes = widthRes;
    }

    /**
     * Sets the height resolution
     * @param heightRes the height resolution
     */
    protected void setHeightRes(int heightRes) {
        this.heightRes = heightRes;
    }

    /**
     * Default constructor
     */
    public DataObjectInfo() {
    }
    
    /**
     * @return the uri of this data object
     */
    public String getUri() {
        return uri;
    }

    /**
     * @return the x coordinate of this data object
     */
    public int getX() {
        return x;
    }

    /**
     * @return the y coordinate of this data object
     */
    public int getY() {
        return y;
    }

    /**
     * @return the width of this data object
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return the height of this data object
     */
    public int getHeight() {
        return height;
    }

    /**
     * @return the width resolution of this data object
     */
    public int getWidthRes() {
        return widthRes;
    }

    /**
     * @return the height resolution of this data object
     */
    public int getHeightRes() {
        return heightRes;
    }
    
    /**
     * @return the rotation of this data object
     */
    public int getRotation() {
        return rotation;
    }

    /**
     * Sets the data object rotation
     * @param rotation the data object rotation
     */
    protected void setRotation(int rotation) {
        this.rotation = rotation;
    }

    /**
     * Sets the object type
     * @param objectType the object type
     */    
    public void setObjectType(Registry.ObjectType objectType) {
        this.objectType = objectType;
    }

    /**
     * @return the object type MOD:CA Registry entry
     */
    public ObjectType getObjectType() {
        return objectType;
    }

    /**
     * @return the resource level at which this data object should reside
     */
    public ResourceInfo getResourceInfo() {
        return resourceInfo;
    }

    /**
     * Sets the resource level at which this object should reside
     * @param resourceInfo the resource level at which this data object should reside
     */
    public void setResourceInfo(ResourceInfo resourceInfo) {
        this.resourceInfo = resourceInfo;
    }

    /**
     * Sets the resource group settings using the given foreign attributes
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
                if (resourceInfo.setLevel(level)) {
                    if (resourceInfo.isExternal()) {
                        QName resourceGroupFileKey = new QName(AFPElementMapping.NAMESPACE,
                                RESOURCE_GROUP_FILE);
                        String resourceExternalDest
                            = (String)foreignAttributes.get(resourceGroupFileKey);
                        if (resourceExternalDest == null) {
                            String msg = RESOURCE_GROUP_FILE + " not specified";
                            log.warn(msg);
                            throw new UnsupportedOperationException(msg);
                        }
                        File resourceExternalGroupFile = new File(resourceExternalDest);
                        SecurityManager security = System.getSecurityManager();
                        try {
                            if (security != null) {
                                security.checkWrite(resourceExternalGroupFile.getPath());
                            }
                        } catch (SecurityException ex) {
                            log.warn("unable to gain write access to external resource file: "
                                    + resourceExternalDest);                            
                        }
                        
                        try {
                            boolean exists = resourceExternalGroupFile.exists();
                            if (exists) {
                                log.warn("overwritting external resource file: "
                                        + resourceExternalDest);
                            }
                            resourceInfo.setExternalResourceGroupFile(resourceExternalGroupFile);
                        } catch (SecurityException ex) {
                            log.warn("unable to gain read access to external resource file: "
                                    + resourceExternalDest);
                        }
                    }
                } else {
                    String msg = RESOURCE_LEVEL + " is null or not valid";
                    log.warn(msg);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return "uri=" + uri
            + ", x=" + x
            + ", y=" + y
            + ", width=" + width
            + ", height=" + height
            + ", widthRes=" + widthRes
            + ", heightRes=" + heightRes
            + ", rotation=" + rotation
            + (resourceInfo != null ? ", resourceInfo=" + resourceInfo : "")
            + (objectType != null ? ", objectTypeEntry=" + objectType : "");
    }
}
