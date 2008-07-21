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

package org.apache.fop.render.afp;

/**
 * A list of parameters associated with an image
 */
public class ImageObjectInfo extends DataObjectInfo {
    private int bitsPerPixel;
    private boolean color;
    private int compression = -1;
    private byte[] data;
    private int dataWidth;
    private int dataHeight;
    private String mimeType;
    private boolean buffered;    

    /**
     * Default constructor
     */
    public ImageObjectInfo() {
        super();
    }

    /**
     * Sets the number of bits per pixel
     * 
     * @param bitsPerPixel the number of bits per pixel
     */
    public void setBitsPerPixel(int bitsPerPixel) {
        this.bitsPerPixel = bitsPerPixel;
    }

    /**
     * Sets if this image is color
     * 
     * @param color true if this is a color image
     */
    public void setColor(boolean color) {
        this.color = color;
    }

    /**
     * Sets the image data
     * 
     * @param data the image data
     */
    public void setData(byte[] data) {
        this.data = data;
    }

    /**
     * Sets the image mime type
     * 
     * @param mimeType the image mime type
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * Returns the number of bits used per pixel
     * 
     * @return the number of bits used per pixel
     */
    public int getBitsPerPixel() {
        return bitsPerPixel;
    }
    
    /**
     * Returns true if this is a color image
     * 
     * @return true if this is a color image
     */
    public boolean isColor() {
        return color;
    }

    /**
     * Returns the image data
     * 
     * @return the image data
     */
    public byte[] getData() {
        return data;
    }
    
    /**
     * Returns true if this image uses compression
     * 
     * @return true if this image uses compression
     */
    public boolean hasCompression() {
        return compression > -1;
    }
    
    /**
     * Returns the compression type
     * 
     * @return the compression type
     */
    public int getCompression() {
        return compression;
    }
    
    /**
     * Sets the compression used with this image
     * 
     * @param compression the type of compression used with this image
     */
    public void setCompression(int compression) {
        this.compression = compression;
    }
    
    /**
     * Returns the image data width
     * 
     * @return the image data width
     */
    public int getDataWidth() {
        return dataWidth;
    }

    /**
     * Sets the image data width
     * 
     * @param imageDataWidth the image data width
     */
    public void setDataWidth(int imageDataWidth) {
        this.dataWidth = imageDataWidth;
    }

    /**
     * Returns the image data height
     * 
     * @return the image data height
     */
    public int getDataHeight() {
        return dataHeight;
    }

    /**
     * Sets the image data height
     * 
     * @param imageDataHeight the image data height
     */
    public void setDataHeight(int imageDataHeight) {
        this.dataHeight = imageDataHeight;
    }

    /** {@inheritDoc} */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Sets whether or not this is info about a buffered image
     * 
     * @param buffered true if this is info about a buffered image
     */
    public void setBuffered(boolean buffered) {
        this.buffered = buffered;
    }

    /**
     * Returns true if this image info is about a buffered image
     * 
     * @return true if this image info is about a buffered image
     */
    public boolean isBuffered() {
        return this.buffered;
    }
    
    /** {@inheritDoc} */
    public String toString() {
        return "ImageObjectInfo{" + super.toString() 
            + ", dataWidth=" + dataWidth
            + ", dataHeight=" + dataHeight
            + ", color=" + color
            + ", bitPerPixel=" + bitsPerPixel
            + "}";
    }

}