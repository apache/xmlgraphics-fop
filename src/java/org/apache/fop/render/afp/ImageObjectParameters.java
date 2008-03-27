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
public class ImageObjectParameters extends DataObjectParameters {
    private int bitsPerPixel;
    private boolean color;
    private int compression = -1;
    private byte[] imageData;
    private int imageDataWidth;
    private int imageDataHeight;
    
    /**
     * Main constructor
     * 
     * @param uri the image uri
     * @param x the image x coordinate
     * @param y the image y coordinate
     * @param width the image width
     * @param height the image height
     * @param widthRes the image width resolution
     * @param heightRes the image height resolution
     */
    public ImageObjectParameters(String uri, int x, int y, int width, int height,
            int widthRes, int heightRes, byte[] imageData,
            int imageDataWidth, int imageDataHeight, boolean color, int bitsPerPixel) {
        super(uri, x, y, width, height, widthRes, heightRes);
        this.imageData = imageData;
        this.imageDataWidth = imageDataWidth;
        this.imageDataHeight = imageDataHeight;
        this.color = color;
        this.bitsPerPixel = bitsPerPixel;
    }

    /**
     * @return the numner of bits used per pixel
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
        return imageData;
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
    public int getImageDataWidth() {
        return imageDataWidth;
    }

    /**
     * Sets the image data width
     * @param imageDataWidth the image data width
     */
    protected void setImageDataWidth(int imageDataWidth) {
        this.imageDataWidth = imageDataWidth;
    }

    /**
     * @return the image data height
     */
    public int getImageDataHeight() {
        return imageDataHeight;
    }

    /**
     * Sets the image data height
     * @param imageDataHeight the image data height
     */
    protected void setImageDataHeight(int imageDataHeight) {
        this.imageDataHeight = imageDataHeight;
    }
    
    /**
     * {@inheritDoc}
     */
    public String toString() {
        return super.toString() 
            + ", imageDataWidth=" + imageDataWidth
            + ", imageDataHeight=" + imageDataHeight
            + ", color=" + color
            + ", bitPerPixel=" + bitsPerPixel;
    }
}