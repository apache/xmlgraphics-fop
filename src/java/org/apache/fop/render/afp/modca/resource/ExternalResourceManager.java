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
package org.apache.fop.render.afp.modca.resource;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.render.afp.ResourceLevel;
import org.apache.fop.render.afp.modca.ResourceGroup;

/**
 * Manages resource groups (external)
 */
public class ExternalResourceManager {

    /** Static logging instance */
    static final Log log = LogFactory.getLog(ExternalResourceManager.class);

    /** the resource manager */
    private final ResourceManager resourceManager;

    /**
     * Main constructor
     * 
     * @param resourceManager the resource manager
     */
    public ExternalResourceManager(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    /** A mapping of external resource destinations to resource groups */
    private Map/*<String,ResourceGroup>*/pathResourceGroupMap
        = new java.util.HashMap/*<String,ResourceGroup>*/();

    /** Sets the default resource group file */
    private String defaultFilePath;

    /**
     * Sets the default resource group file
     *
     * @param filePath the default resource group file path
     */
    public void setDefaultFilePath(String filePath) {
        this.defaultFilePath = filePath;
    }

    /**
     * Returns the corresponding resource group for the given resource level
     *
     * @param level the resource level
     * @return the corresponding resource group for the given resource level
     * or null if not found.
     */
    public ResourceGroup getResourceGroup(ResourceLevel level) {
        ResourceGroup resourceGroup = null;
        // this resource info does not have an external resource group
        // file definition
        String filePath = level.getExternalFilePath();
        if (filePath != null) {
            filePath = level.getExternalFilePath();
            resourceGroup = (ResourceGroup)pathResourceGroupMap.get(filePath);
            if (resourceGroup == null) {
                ResourceFactory factory = resourceManager.getFactory();
                resourceGroup = factory.createResourceGroup();
                pathResourceGroupMap.put(filePath, resourceGroup);
            }
        } else if (defaultFilePath != null) {
            // fallback to default resource group file
            level.setExternalFilePath(defaultFilePath);
            resourceGroup = getResourceGroup(level);
        }
        return resourceGroup;
    }

    /**
     * Writes out all resource groups to external files
     * 
     * @throws java.io.IOException an I/O exception of some sort has occurred.
     */
    public void write() throws IOException {
        // write any external resources
        Iterator it = pathResourceGroupMap.keySet().iterator();
        while (it.hasNext()) {
            String filePath = (String)it.next();
            ResourceGroup resourceGroup
                = (ResourceGroup)pathResourceGroupMap.get(filePath);
            OutputStream os = null;
            try {
                log.debug("Writing external AFP resource file " + filePath);
                os = new java.io.FileOutputStream(filePath);
                resourceGroup.write(os);
            } finally {
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException e) {
                        log.error("Failed to close outputstream for external AFP resource file "
                                        + filePath);
                    }
                }
            }
        }
    }
}