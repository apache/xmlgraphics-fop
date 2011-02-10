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

package org.apache.fop.afp.ioca;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.fop.afp.Streamable;

/**
 * This class represents the IOCA IDE Structure parameter (X'9B').
 */
public class IDEStructureParameter implements Streamable {

    /** The RGB color model used by the IDE Structure parameter */
    public static final byte COLOR_MODEL_RGB = (byte)0x01;
    /** The YCrCb color model used by the IDE Structure parameter */
    public static final byte COLOR_MODEL_YCRCB = (byte)0x02;
    /** The CMYK color model used by the IDE Structure parameter */
    public static final byte COLOR_MODEL_CMYK = (byte)0x04;
    /** The YCbCr color model used by the IDE Structure parameter */
    public static final byte COLOR_MODEL_YCBCR = (byte)0x12;

    /** additive/subtractive setting for ASFLAG */
    private boolean subtractive = false;

    /** setting for GRAYCODE flag */
    private boolean grayCoding = false;

    /** the image color model */
    private byte colorModel = COLOR_MODEL_YCRCB;

    /** the array with the number of bits/IDE for each component */
    private byte[] bitsPerIDE = new byte[] {(byte)1}; //1-bit by default

    /**
     * Creates a new IDE Structure parameter. The values are initialized for a bi-level image
     * using the RGB color model.
     */
    public IDEStructureParameter() {
        //nop
    }

    /**
     * Sets the image IDE color model.
     *
     * @param color    the IDE color model.
     */
    public void setColorModel(byte color) {
        this.colorModel = color;
    }

    /**
     * Establishes the parameter values for the normal RGB 24bit color model.
     */
    public void setDefaultRGBColorModel() {
        this.colorModel = COLOR_MODEL_RGB;
        setUniformBitsPerComponent(3, 8);
    }

    /**
     * Establishes the parameter values for the normal CMYK 32bit color model.
     */
    public void setDefaultCMYKColorModel() {
        this.colorModel = COLOR_MODEL_CMYK;
        setUniformBitsPerComponent(4, 8);
    }

    /**
     * Sets uniform bits per component.
     * @param numComponents the number of components
     * @param bitsPerComponent number of bits per component
     */
    public void setUniformBitsPerComponent(int numComponents, int bitsPerComponent) {
        if (bitsPerComponent < 0 || bitsPerComponent >= 256) {
            throw new IllegalArgumentException(
                    "The number of bits per component must be between 0 and 255");
        }
        this.bitsPerIDE = new byte[numComponents];
        for (int i = 0; i < numComponents; i++) {
            this.bitsPerIDE[i] = (byte)bitsPerComponent;
        }
    }

    /**
     * Sets the array for the bits/IDE, one entry per component.
     * @param bitsPerComponent the
     */
    public void setBitsPerComponent(int[] bitsPerComponent) {
        int numComponents = bitsPerComponent.length;
        this.bitsPerIDE = new byte[numComponents];
        for (int i = 0; i < numComponents; i++) {
            int bits = bitsPerComponent[i];
            if (bits < 0 || bits >= 256) {
                throw new IllegalArgumentException(
                        "The number of bits per component must be between 0 and 255");
            }
            this.bitsPerIDE[i] = (byte)bits;
        }
    }

    /**
     * Set either additive or subtractive mode (used for ASFLAG).
     * @param subtractive true for subtractive mode, false for additive mode
     */
    public void setSubtractive(boolean subtractive) {
        this.subtractive = subtractive;
    }

    /** {@inheritDoc} */
    public void writeToStream(OutputStream os) throws IOException {
        int length = 7 + bitsPerIDE.length;

        byte flags = 0x00;
        if (subtractive) {
            flags |= 1 << 7;
        }
        if (grayCoding) {
            flags |= 1 << 6;
        }

        DataOutputStream dout = new DataOutputStream(os);
        dout.writeByte(0x9B); //ID
        dout.writeByte(length - 2); //LENGTH
        dout.writeByte(flags); //FLAGS
        dout.writeByte(this.colorModel); //FORMAT
        for (int i = 0; i < 3; i++) {
            dout.writeByte(0); //RESERVED
        }
        dout.write(this.bitsPerIDE); //component sizes
    }

}
