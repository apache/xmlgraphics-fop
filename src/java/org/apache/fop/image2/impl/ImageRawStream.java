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

package org.apache.fop.image2.impl;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import org.apache.fop.image2.ImageFlavor;
import org.apache.fop.image2.ImageInfo;

/**
 * This class is an implementation of the Image interface exposing an InputStream for loading the
 * raw/undecoded image.
 */
public class ImageRawStream extends AbstractImage {

    private ImageFlavor flavor;
    private InputStreamFactory streamFactory;
    
    /**
     * Main constructor.
     * @param info the image info object
     * @param flavor the image flavor for the raw image
     * @param streamFactory the InputStreamFactory that is used to create InputStream instances
     */
    public ImageRawStream(ImageInfo info, ImageFlavor flavor, InputStreamFactory streamFactory) {
        super(info);
        this.flavor = flavor;
        setInputStreamFactory(streamFactory);
    }
    
    /**
     * Constructor for a simple InputStream as parameter.
     * @param info the image info object
     * @param flavor the image flavor for the raw image
     * @param in the InputStream with the raw content
     */
    public ImageRawStream(ImageInfo info, ImageFlavor flavor, InputStream in) {
        this(info, flavor, new SingleStreamFactory(in));
    }
    
    /** {@inheritDoc} */
    public ImageFlavor getFlavor() {
        return this.flavor;
    }

    /** {@inheritDoc} */
    public boolean isCacheable() {
        return !this.streamFactory.isUsedOnceOnly();
    }
    
    /**
     * Sets the InputStreamFactory to be used by this image. This method allows to replace the
     * original factory.
     * @param factory the new InputStreamFactory
     */
    public void setInputStreamFactory(InputStreamFactory factory) {
        if (this.streamFactory != null) {
            this.streamFactory.close();
        }
        this.streamFactory = factory;
    }
    
    /**
     * Returns a new InputStream to access the raw image.
     * @return the InputStream
     */
    public InputStream createInputStream() {
        return this.streamFactory.createInputStream();
    }
    
    /**
     * Represents a factory for InputStream objects. Make sure the class is thread-safe!
     */
    public interface InputStreamFactory {
        
        /**
         * Indicates whether this factory is only usable once or many times.
         * @return true if the factory can only be used once
         */
        boolean isUsedOnceOnly();
        
        /**
         * Creates and returns a new InputStream.
         * @return the new InputStream
         */
        InputStream createInputStream();
        
        /**
         * Closes the factory and releases any resources held open during the lifetime of this
         * object.
         */
        void close();
        
    }
    
    private static class SingleStreamFactory implements InputStreamFactory {
        
        private InputStream in;
        
        public SingleStreamFactory(InputStream in) {
            this.in = in;
        }
        
        public synchronized InputStream createInputStream() {
            if (this.in != null) {
                InputStream tempin = this.in;
                this.in = null; //Don't close, just remove the reference
                return tempin;
            } else {
                throw new IllegalStateException("Can only create an InputStream once!");
            }
        }

        public synchronized void close() {
            IOUtils.closeQuietly(this.in);
            this.in = null;
        }

        public boolean isUsedOnceOnly() {
            return true;
        }

        /** {@inheritDoc} */
        protected void finalize() {
            close();
        }
        
    }
    
}
