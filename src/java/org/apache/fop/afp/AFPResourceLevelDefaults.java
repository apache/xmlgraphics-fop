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

import java.util.Iterator;
import java.util.Map;

import org.apache.fop.afp.modca.ResourceObject;

/**
 * This class holds resource levels defaults for the various resource types.
 */
public class AFPResourceLevelDefaults {

    private static final Map RESOURCE_TYPE_NAMES = new java.util.HashMap();

    static {
        //Map to be extended as need arises:
        registerResourceTypeName("goca", ResourceObject.TYPE_GRAPHIC);
        registerResourceTypeName("bitmap", ResourceObject.TYPE_IMAGE);
    }

    private static void registerResourceTypeName(String name, byte type) {
        RESOURCE_TYPE_NAMES.put(name.toLowerCase(), new Byte(type));
    }

    private static byte getResourceType(String resourceTypeName) {
        Byte result = (Byte)RESOURCE_TYPE_NAMES.get(resourceTypeName.toLowerCase());
        if (result == null) {
            throw new IllegalArgumentException("Unknown resource type name: " + resourceTypeName);
        }
        return result.byteValue();
    }

    private Map defaultResourceLevels = new java.util.HashMap();

    /**
     * Creates a new instance with default values.
     */
    public AFPResourceLevelDefaults() {
        // level not explicitly set/changed so default to inline for GOCA graphic objects
        // (due to a bug in the IBM AFP Workbench Viewer (2.04.01.07), hard copy works just fine)
        setDefaultResourceLevel(ResourceObject.TYPE_GRAPHIC,
                new AFPResourceLevel(AFPResourceLevel.INLINE));
    }

    /**
     * Sets the default resource level for a particular resource type.
     * @param type the resource type name
     * @param level the resource level
     */
    public void setDefaultResourceLevel(String type, AFPResourceLevel level) {
        setDefaultResourceLevel(getResourceType(type), level);
    }

    /**
     * Sets the default resource level for a particular resource type.
     * @param type the resource type ({@link ResourceObject}.TYPE_*)
     * @param level the resource level
     */
    public void setDefaultResourceLevel(byte type, AFPResourceLevel level) {
        this.defaultResourceLevels.put(new Byte(type), level);
    }

    /**
     * Returns the default resource level for a particular resource type.
     * @param type the resource type ({@link ResourceObject}.TYPE_*)
     * @return the default resource level
     */
    public AFPResourceLevel getDefaultResourceLevel(byte type) {
        AFPResourceLevel result = (AFPResourceLevel)this.defaultResourceLevels.get(new Byte(type));
        if (result == null) {
            result = AFPResourceInfo.DEFAULT_LEVEL;
        }
        return result;
    }

    /**
     * Allows to merge the values from one instance into another. Values from the instance passed
     * in as a parameter override values of this instance.
     * @param other the other instance to get the defaults from
     */
    public void mergeFrom(AFPResourceLevelDefaults other) {
        Iterator iter = other.defaultResourceLevels.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            Byte type = (Byte)entry.getKey();
            AFPResourceLevel level = (AFPResourceLevel)entry.getValue();
            this.defaultResourceLevels.put(type, level);
        }
    }

}
