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

package org.apache.fop.render.afp;

import java.net.URI;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.util.QName;

import org.apache.fop.afp.AFPResourceInfo;
import org.apache.fop.afp.AFPResourceLevel;
import org.apache.fop.render.afp.extensions.AFPElementMapping;

/**
 * Parses any AFP foreign attributes
 */
public class AFPForeignAttributeReader {
    private static final Log LOG = LogFactory.getLog("org.apache.xmlgraphics.afp");

    /** the resource-name attribute */
    public static final QName RESOURCE_NAME = new QName(
            AFPElementMapping.NAMESPACE, "afp:resource-name");

    /** the resource-level attribute */
    public static final QName RESOURCE_LEVEL = new QName(
            AFPElementMapping.NAMESPACE, "afp:resource-level");

    /** the resource-group-file attribute */
    public static final QName RESOURCE_GROUP_URI = new QName(
            AFPElementMapping.NAMESPACE, "afp:resource-group-file");

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
            String resourceName = (String) foreignAttributes.get(RESOURCE_NAME);
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
    public AFPResourceLevel getResourceLevel(Map<QName, String> foreignAttributes) {
        AFPResourceLevel resourceLevel = null;
        if (foreignAttributes != null && !foreignAttributes.isEmpty()) {
            if (foreignAttributes.containsKey(RESOURCE_LEVEL)) {
                String levelString = foreignAttributes.get(RESOURCE_LEVEL);
                resourceLevel = AFPResourceLevel.valueOf(levelString);
                // if external get resource group file attributes
                if (resourceLevel != null && resourceLevel.isExternal()) {
                    String resourceGroupUri = foreignAttributes.get(RESOURCE_GROUP_URI);
                    if (resourceGroupUri == null) {
                        String msg = RESOURCE_GROUP_URI + " not specified";
                        throw new UnsupportedOperationException(msg);
                    }
                    resourceLevel.setExternalUri(URI.create(resourceGroupUri));
                }
            }
        }
        return resourceLevel;
    }
}
