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

import java.io.File;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.render.afp.extensions.AFPElementMapping;
import org.apache.xmlgraphics.util.QName;

/**
 * Parses any AFP foreign attributes
 */
public class AFPForeignAttributeReader {
    private static final Log log = LogFactory.getLog("org.apache.xmlgraphics.afp");

    /** the resource-name attribute */
    public static final String RESOURCE_NAME = "afp:resource-name";

    /** the resource-level attribute */
    public static final String RESOURCE_LEVEL = "afp:resource-level";

    /** the resource-group-file attribute */
    public static final String RESOURCE_GROUP_FILE = "afp:resource-group-file";

    /**
     * Main constructor
     */
    public AFPForeignAttributeReader() {
    }

    /**
     * Returns the resource information
     *
     * @param foreignAttributes the foreign attributes
     * @return the resource information
     */
    public AFPResourceInfo getResourceInfo(Map/*<QName, String>*/ foreignAttributes) {
        AFPResourceInfo resourceInfo = new AFPResourceInfo();
        if (foreignAttributes != null && !foreignAttributes.isEmpty()) {
            QName resourceNameKey = new QName(AFPElementMapping.NAMESPACE, RESOURCE_NAME);
            String resourceName = (String)foreignAttributes.get(resourceNameKey);
            if (resourceName != null) {
                resourceInfo.setName(resourceName);
            }
            AFPResourceLevel level = getResourceLevel(foreignAttributes);
            if (level != null) {
                resourceInfo.setLevel(level);
            }
        }
        return resourceInfo;
    }

    /**
     * Returns the resource level
     *
     * @param foreignAttributes the foreign attributes
     * @return the resource level
     */
    public AFPResourceLevel getResourceLevel(Map/*<QName, String>*/ foreignAttributes) {
        AFPResourceLevel resourceLevel = null;
        if (foreignAttributes != null && !foreignAttributes.isEmpty()) {
            QName resourceLevelKey = new QName(AFPElementMapping.NAMESPACE, RESOURCE_LEVEL);
            if (foreignAttributes.containsKey(resourceLevelKey)) {
                String levelString = (String)foreignAttributes.get(resourceLevelKey);
                resourceLevel = AFPResourceLevel.valueOf(levelString);
                // if external get resource group file attributes
                if (resourceLevel != null && resourceLevel.isExternal()) {
                    QName resourceGroupFileKey = new QName(AFPElementMapping.NAMESPACE,
                            RESOURCE_GROUP_FILE);
                    String resourceGroupFile
                        = (String)foreignAttributes.get(resourceGroupFileKey);
                    if (resourceGroupFile == null) {
                        String msg = RESOURCE_GROUP_FILE + " not specified";
                        log.error(msg);
                        throw new UnsupportedOperationException(msg);
                    }
                    File resourceExternalGroupFile = new File(resourceGroupFile);
                    SecurityManager security = System.getSecurityManager();
                    try {
                        if (security != null) {
                            security.checkWrite(resourceExternalGroupFile.getPath());
                        }
                    } catch (SecurityException ex) {
                        String msg = "unable to gain write access to external resource file: "
                        + resourceGroupFile;
                        log.error(msg);
                    }

                    try {
                        boolean exists = resourceExternalGroupFile.exists();
                        if (exists) {
                            log.warn("overwriting external resource file: "
                                    + resourceGroupFile);
                        }
                        resourceLevel.setExternalFilePath(resourceGroupFile);
                    } catch (SecurityException ex) {
                        String msg = "unable to gain read access to external resource file: "
                            + resourceGroupFile;
                        log.error(msg);
                    }
                }
            }
        }
        return resourceLevel;
    }
}
