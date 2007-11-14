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

package org.apache.fop.image2;

/**
 * The flavor of an image indicates in which form it is available. A bitmap image loaded into
 * memory might be represented as a BufferedImage (indicated by ImageFlavor.BUFFERED_IMAGE).
 * It is mostly used by consuming code to indicate what kind of flavors can be processed so a
 * processing pipeline can do the necessary loading operations and conversions.
 */
public class ImageFlavor {

    /** An image in form of a RenderedImage instance */
    public static final ImageFlavor RENDERED_IMAGE = new ImageFlavor("RenderedImage");
    /** An image in form of a BufferedImage instance */
    public static final ImageFlavor BUFFERED_IMAGE = new ImageFlavor("BufferedImage");
    /** An XML-based image in form of a W3C DOM instance */
    public static final ImageFlavor XML_DOM = new ImageFlavor("text/xml;form=dom");
    /** An image in form of a raw PNG file/stream */
    public static final ImageFlavor RAW_PNG = new ImageFlavor("RawPNG");
    /** An image in form of a raw JPEG/JFIF file/stream */
    public static final ImageFlavor RAW_JPEG = new ImageFlavor("RawJPEG");
    /** An image in form of a raw EMF (Windows Enhanced Metafile) file/stream */
    public static final ImageFlavor RAW_EMF = new ImageFlavor("RawEMF");
    /** An image in form of a Graphics2DImage (can be painted on a Graphics2D interface) */
    public static final ImageFlavor GRAPHICS2D = new ImageFlavor("Graphics2DImage");
    
    private String name;
    
    /**
     * Constructs a new ImageFlavor. Please reuse existing constants wherever possible!
     * @param name the name of the flavor (must be unique)
     */
    public ImageFlavor(String name) {
        this.name = name;
    }
    
    /**
     * Returns the name of the ImageFlavor.
     * @return the flavor name
     */
    public String getName() {
        return this.name;
    }

    /** {@inheritDoc} */
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        final ImageFlavor other = (ImageFlavor)obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    /** {@inheritDoc} */
    public String toString() {
        return getName();
    }
    
}
