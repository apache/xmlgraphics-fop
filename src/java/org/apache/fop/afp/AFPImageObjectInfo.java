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

package org.apache.fop.afp;


/**
 * A list of parameters associated with an image
 */
public class AFPImageObjectInfo extends AFPDataObjectInfo {

    /** number of bits per pixel used */
    private int bitsPerPixel;

    /** is this a color image? */
    private boolean color;

    /** compression type if any */
    private int compression = -1;

    private boolean subtractive;

    /**
     * Default constructor
     */
    public AFPImageObjectInfo() {
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
     * Set either additive or subtractive mode (used for ASFLAG).
     * @param subtractive true for subtractive mode, false for additive mode
     */
    public void setSubtractive(boolean subtractive) {
        this.subtractive = subtractive;
    }

    /**
     * Indicates whether additive or subtractive mode is set.
     * @return true for subtractive mode, false for additive mode
     */
    public boolean isSubtractive() {
        return subtractive;
    }

    /** {@inheritDoc} */
    public String toString() {
        return "AFPImageObjectInfo{" + super.toString()
            + ", compression=" + compression
            + ", color=" + color
            + ", bitsPerPixel=" + bitsPerPixel
            + ", " + (isSubtractive() ? "subtractive" : "additive")
            + "}";
    }
}