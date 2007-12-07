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

import java.io.InputStream;

import javax.imageio.stream.ImageInputStream;
import javax.xml.transform.Source;

import org.apache.fop.image2.util.ImageInputStreamAdapter;

/**
 * Acts as a holder for the input to image loading operations.
 */
public class ImageSource implements Source {
    
    private String systemId;
    private ImageInputStream iin;
    private boolean fastSource;

    /**
     * Main constructor.
     * @param in the ImageInputStream to load from
     * @param systemId the system identifier (resolved URI) of the image
     * @param fastSource true if it's a fast source (accessing local files)
     */
    public ImageSource(ImageInputStream in, String systemId, boolean fastSource) {
        this.iin = in;
        this.systemId = systemId;
        this.fastSource = fastSource;
    }

    /**
     * Returns an InputStream which operates on the underlying ImageInputStream.
     * @return the InputStream or null if the stream has been closed
     */
    public InputStream getInputStream() {
        if (this.iin == null) {
            return null;
        } else {
            return new ImageInputStreamAdapter(this.iin);
        }
    }

    /**
     * Returns the ImageInputStream.
     * @return the ImageInputStream or null if the stream has been closed
     */
    public ImageInputStream getImageInputStream() {
        return this.iin;
    }
    
    /**
     * Sets the ImageInputStream.
     * @param in the ImageInputStream
     */
    public void setImageInputStream(ImageInputStream in) {
        this.iin = in;
    }
    
    /** {@inheritDoc} */
    public String getSystemId() {
        return this.systemId;
    }

    /** {@inheritDoc} */
    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }
    
    /**
     * Indicates whether this ImageSource is a fast source, i.e. accesses local files rather than
     * network resources.
     * @return true if it's a fast source
     */
    public boolean isFastSource() {
        return this.fastSource;
    }

    /** {@inheritDoc} */
    public String toString() {
        return (isFastSource() ? "FAST " : "") + "ImageSource: "
            + getSystemId() + " " + getImageInputStream();
    }
    
}
