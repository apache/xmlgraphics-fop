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

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.render.afp.extensions.AFPElementMapping;
import org.apache.fop.util.QName;

/**
 * A list of parameters associated with an AFP data objects
 */
public class DataObjectParameters {
    private static final ResourceLevel DEFAULT_RESOURCE_LEVEL = new ResourceLevel();
    
    private String uri;
    private int x;
    private int y;
    private int width;
    private int height;
    private int widthRes;
    private int heightRes;
    private ResourceLevel resourceLevel = DEFAULT_RESOURCE_LEVEL;
    
    /**
     * Main constructor
     * 
     * @param uri the data object uri
     * @param x the data object x coordinate
     * @param y the data object y coordinate
     * @param width the data object width
     * @param height the data object height
     * @param widthRes the data object width resolution
     * @param heightRes the data object height resolution
     */
    public DataObjectParameters(String uri, int x, int y, int width, int height,
            int widthRes, int heightRes) {
        this.uri = uri;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.widthRes = widthRes;
        this.heightRes = heightRes;
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
     * @return returns the resource level at which this data object should reside
     */
    public ResourceLevel getResourceLevel() {
        return resourceLevel;
    }

    /**
     * Sets the resource level at which this object should reside
     * @param resourceLevel the resource level at which this data object should reside
     */
    public void setResourceLevel(ResourceLevel resourceLevel) {
        this.resourceLevel = resourceLevel;
    }
      
    /**
     * Sets the resource level using the given foreign attributes
     * @param foreignAttributes a mapping of element attributes names to values
     */
    public void setResourceLevelFromForeignAttributes(Map/*<QName, String>*/ foreignAttributes) {
        if (foreignAttributes != null) {
            QName resourceLevelKey = new QName(
                    AFPElementMapping.NAMESPACE,
                    "afp:resource-level");
            if (foreignAttributes.containsKey(resourceLevelKey)) {
                String level = (String)foreignAttributes.get(resourceLevelKey);              
                this.resourceLevel = new ResourceLevel();
                if (resourceLevel.setLevel(level)) {
                    if (resourceLevel.isExternal()) {
                        QName resourceDestKey = new QName(
                                AFPElementMapping.NAMESPACE,
                                "afp:resource-dest");
                        String resourceExternalDest
                            = (String)foreignAttributes.get(resourceDestKey);
                        resourceLevel.setExternalDest(resourceExternalDest);
                    }
                } else {
                    Log log = LogFactory.getLog("org.apache.fop.afp");
                    log.warn("invalid resource level '" + level
                            + "', using default document level");
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
            + (resourceLevel != null ? ", resourceLevel=" + resourceLevel : "");
    }
}
