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
 
package org.apache.fop.image2.cache;

import org.apache.fop.image2.ImageFlavor;

/**
 * Key class for Image instances in the cache.
 */
public class ImageKey {
    
    private String uri;
    private ImageFlavor flavor;
    
    /**
     * Main constructor.
     * @param uri the original URI
     * @param flavor the image flavor
     */
    public ImageKey(String uri, ImageFlavor flavor) {
        if (uri == null) {
            throw new NullPointerException("URI must not be null");
        }
        if (flavor == null) {
            throw new NullPointerException("flavor must not be null");
        }
        this.uri = uri;
        this.flavor = flavor;
    }

    /** {@inheritDoc} */
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((flavor == null) ? 0 : flavor.hashCode());
        result = prime * result + ((uri == null) ? 0 : uri.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ImageKey other = (ImageKey)obj;
        if (!uri.equals(other.uri)) {
            return false;
        }
        if (!flavor.equals(other.flavor)) {
            return false;
        }
        return true;
    }

    /** {@inheritDoc} */
    public String toString() {
        return uri + " (" + flavor + ")";
    }
    
}