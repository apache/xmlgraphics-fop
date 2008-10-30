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
public final class PCLRenderingMode implements Serializable {

    private static final long serialVersionUID = 6359884255324755026L;

    /** "Quality" rendering (mixed native and bitmap for improved quality) */
    public static final PCLRenderingMode QUALITY = new PCLRenderingMode("quality");
    /** "Speed" rendering (maximum speed with native rendering, reduced visual quality) */
    public static final PCLRenderingMode SPEED = new PCLRenderingMode("speed");
    /**
     * "Bitmap" rendering (pages are painted entirely as bitmaps, maximum quality,
     * reduced performance)
     */
    public static final PCLRenderingMode BITMAP = new PCLRenderingMode("bitmap");

    private String name;

    /**
     * Constructor to add a new named item.
     * @param name Name of the item.
     */
    private PCLRenderingMode(String name) {
        this.name = name;
    }

    /** @return the name of the enum */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the enumeration/singleton object based on its name.
     * @param name the name of the enumeration value
     * @return the enumeration object
     */
    public static PCLRenderingMode valueOf(String name) {
        if (QUALITY.getName().equalsIgnoreCase(name)) {
            return QUALITY;
        } else if (SPEED.getName().equalsIgnoreCase(name)) {
            return SPEED;
        } else if (BITMAP.getName().equalsIgnoreCase(name)) {
            return BITMAP;
        } else {
            throw new IllegalArgumentException("Illegal value for enumeration: " + name);
        }
    }

    private Object readResolve() throws ObjectStreamException {
        return valueOf(getName());
    }

    /** {@inheritDoc} */
    public String toString() {
        return "PCLRenderingMode:" + name;
    }
}
