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

package org.apache.fop.render.pcl;

import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * Enumeration class for PCL rendering modes.
 */
public enum PCLRenderingMode implements Serializable {
    /** "Quality" rendering (mixed native and bitmap for improved quality) */
    QUALITY("quality", 1.0f),
    /** "Speed" rendering (maximum speed with native rendering, reduced visual quality) */
    SPEED("speed", 0.25f),
    /**
     * "Bitmap" rendering (pages are painted entirely as bitmaps, maximum quality,
     * reduced performance)
     */
    BITMAP("bitmap", 1.0f);

    private static final long serialVersionUID = 6359884255324755026L;
    private String name;
    private float defaultDitheringQuality;

    /**
     * Constructor to add a new named item.
     * @param name Name of the item.
     * @param defaultDitheringQuality the default dithering quality (0.0f..1.0f)
     */
    private PCLRenderingMode(String name, float defaultDitheringQuality) {
        this.name = name;
        this.defaultDitheringQuality = defaultDitheringQuality;
    }

    /** @return the name of the enum */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the default dithering quality for this rendering mode.
     * @return the default dithering quality (0.0f..1.0f)
     */
    public float getDefaultDitheringQuality() {
        return this.defaultDitheringQuality;
    }

    /**
     * Returns the enumeration/singleton object based on its name.
     * @param name the name of the enumeration value
     * @return the enumeration object
     */
    public static PCLRenderingMode getValueOf(String name) {
        for (PCLRenderingMode mode : PCLRenderingMode.values()) {
            if (mode.getName().equalsIgnoreCase(name)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Illegal value for enumeration: " + name);
    }

    private Object readResolve() throws ObjectStreamException {
        return getValueOf(getName());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "PCLRenderingMode:" + name;
    }
}
