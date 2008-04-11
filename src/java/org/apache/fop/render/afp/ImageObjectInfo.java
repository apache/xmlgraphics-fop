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

    /**
     * Default constructor
     */
    public ImageObjectInfo() {
    }

    /**
     * Sets the number of bits per pixel
     * @param bitsPerPixel the number of bits per pixel
     */
    public void setBitsPerPixel(int bitsPerPixel) {
        this.bitsPerPixel = bitsPerPixel;
    }

    /**
     * Sets if this image is color
     * @param color true if this is a color image
     */
    public void setColor(boolean color) {
        this.color = color;
    }

    /**
     * Sets the image data
     * @param data the image data
     */
    public void setData(byte[] data) {
        this.data = data;
    }

    /**
     * Sets the image mime type
     * @param mimeType the image mime type
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * @return the number of bits used per pixel
     */
    public int getBitsPerPixel() {
        return bitsPerPixel;
    }
    
    /**
     * @return true if this is a color image
     */
    public boolean isColor() {
        return color;
    }

    /**
     * @return the image data
     */
    public byte[] getData() {
        return data;
    }
    
    /**
     * @return true of this image uses compression
     */
    public boolean hasCompression() {
        return compression > -1;
    }
    
    /**
     * @return the compression type
     */
    public int getCompression() {
        return compression;
    }
    
    /**
     * Sets the compression used with this image 
     * @param compression the type of compression used with this image
     */
    public void setCompression(int compression) {
        this.compression = compression;
    }
    
    /**
     * @return the image data width
     */
    public int getDataWidth() {
        return dataWidth;
    }

    /**
     * Sets the image data width
     * @param imageDataWidth the image data width
     */
    public void setDataWidth(int imageDataWidth) {
        this.dataWidth = imageDataWidth;
    }

    /**
     * @return the image data height
     */
    public int getDataHeight() {
        return dataHeight;
    }

    /**
     * Sets the image data height
     * @param imageDataHeight the image data height
     */
    public void setDataHeight(int imageDataHeight) {
        this.dataHeight = imageDataHeight;
    }

    /**
     * @return the mime type of this image
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return super.toString() 
            + ", mimeType=" + mimeType
            + ", dataWidth=" + dataWidth
            + ", dataHeight=" + dataHeight
            + ", color=" + color
            + ", bitPerPixel=" + bitsPerPixel;
    }
}