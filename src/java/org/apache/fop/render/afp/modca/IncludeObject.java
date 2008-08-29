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

package org.apache.fop.render.afp.modca;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.fop.render.afp.modca.triplets.MappingOptionTriplet;
import org.apache.fop.render.afp.tools.BinaryUtils;

/**
 * An Include Object structured field references an object on a page or overlay.
 * It optionally contains parameters that identify the object and that specify
 * presentation parameters such as object position, size, orientation, mapping,
 * and default color.
 * <p>
 * Where the presentation parameters conflict with parameters specified in the
 * object's environment group (OEG), the parameters in the Include Object
 * structured field override. If the referenced object is a page segment, the
 * IOB parameters override the corresponding environment group parameters on all
 * data objects in the page segment.
 * </p>
 */
public class IncludeObject extends AbstractNamedAFPObject {

    /**
     * the include object is of type page segment
     */
    public static final byte TYPE_PAGE_SEGMENT = (byte)0x5F;

    /**
     * the include object is of type other
     */
    public static final byte TYPE_OTHER = (byte)0x92;

    /**
     * the include object is of type graphic
     */
    public static final byte TYPE_GRAPHIC = (byte)0xBB;

    /**
     * the included object is of type barcode
     */
    public static final byte TYPE_BARCODE = (byte)0xEB;

    /**
     * the included object is of type image
     */
    public static final byte TYPE_IMAGE = (byte)0xFB;

    /**
     * The object type (default is other)
     */
    private byte objectType = TYPE_OTHER;

    /**
     * The orientation on the include object
     */
    private int orientation = 0;

    /**
     * The X-axis origin of the object area
     */
    private int xOffset = 0;

    /**
     * The Y-axis origin of the object area
     */
    private int yOffset = 0;

    /**
     * The X-axis origin defined in the object
     */
    private int xContentOffset = 0;

    /**
     * The Y-axis origin defined in the object
     */
    private int yContentOffset = 0;

    /**
     * Constructor for the include object with the specified name, the name must
     * be a fixed length of eight characters and is the name of the referenced
     * object.
     *
     * @param name the name of this include object
     */
    public IncludeObject(String name) {
        super(name);
    }

    /**
     * Sets the orientation to use for the Include Object.
     *
     * @param orientation
     *            The orientation (0,90, 180, 270)
     */
    public void setOrientation(int orientation) {
        if (orientation == 0 || orientation == 90 || orientation == 180
            || orientation == 270) {
            this.orientation = orientation;
        } else {
            throw new IllegalArgumentException(
                "The orientation must be one of the values 0, 90, 180, 270");
        }
    }

    /**
     * Sets the x and y offset to the origin in the object area
     *
     * @param x the X-axis origin of the object area
     * @param y the Y-axis origin of the object area
     */
    public void setObjectArea(int x, int y) {
        this.xOffset = x;
        this.yOffset = y;
    }

    /**
     * Sets the x and y offset of the content area to the object area
     *
     * @param x the X-axis origin defined in the object
     * @param y the Y-axis origin defined in the object
     */
    public void setContentArea(int x, int y) {
        this.xContentOffset = x;
        this.yContentOffset = y;
    }

    /**
     * Sets the data object type
     *
     * @param type the data object type
     */
    public void setObjectType(byte type) {
        this.objectType = type;
    }

    /** {@inheritDoc} */
    public void writeToStream(OutputStream os) throws IOException {
        byte[] data = new byte[36];
        super.copySF(data, Type.INCLUDE, Category.DATA_RESOURCE);

        // Set the total record length
        byte[] len = BinaryUtils.convert(35 + getTripletDataLength(), 2); //Ignore first byte
        data[1] = len[0];
        data[2] = len[1];

        data[17] = 0x00; // reserved
        data[18] = objectType;

        //XoaOset (object area)
        if (xOffset >= -1) {
            byte[] x = BinaryUtils.convert(xOffset, 3);
            data[19] = x[0];
            data[20] = x[1];
            data[21] = x[2];
        } else {
            data[19] = (byte)0xFF;
            data[20] = (byte)0xFF;
            data[21] = (byte)0xFF;
        }

        // YoaOset (object area)
        if (yOffset > -1) {
            byte[] y = BinaryUtils.convert(yOffset, 3);
            data[22] = y[0];
            data[23] = y[1];
            data[24] = y[2];
        } else {
            data[22] = (byte)0xFF;
            data[23] = (byte)0xFF;
            data[24] = (byte)0xFF;
        }

        // XoaOrent/YoaOrent
        switch (orientation) {
            case -1: // use x/y axis orientation defined in object
                data[25] = (byte)0xFF; // x axis rotation
                data[26] = (byte)0xFF; //
                data[27] = (byte)0xFF; // y axis rotation
                data[28] = (byte)0xFF;
                break;
            case 90:
                data[25] = 0x2D;
                data[26] = 0x00;
                data[27] = 0x5A;
                data[28] = 0x00;
                break;
            case 180:
                data[25] = 0x5A;
                data[25] = 0x00;
                data[27] = (byte)0x87;
                data[28] = 0x00;
                break;
            case 270:
                data[25] = (byte)0x87;
                data[26] = 0x00;
                data[27] = 0x00;
                data[28] = 0x00;
                break;
            default:
                data[25] = 0x00;
                data[26] = 0x00;
                data[27] = 0x2D;
                data[28] = 0x00;
                break;
        }

        // XocaOset (object content)
        if (xContentOffset > -1) {
            byte[] y = BinaryUtils.convert(xContentOffset, 3);
            data[29] = y[0];
            data[30] = y[1];
            data[31] = y[2];
        } else {
            data[29] = (byte)0xFF;
            data[30] = (byte)0xFF;
            data[31] = (byte)0xFF;
        }

        // YocaOset (object content)
        if (yContentOffset > -1) {
            byte[] y = BinaryUtils.convert(yContentOffset, 3);
            data[32] = y[0];
            data[33] = y[1];
            data[34] = y[2];
        } else {
            data[32] = (byte)0xFF;
            data[33] = (byte)0xFF;
            data[34] = (byte)0xFF;
        }
        data[35] = 0x01;

        // Write structured field data
        os.write(data);

        // Write triplet for FQN internal/external object reference
        os.write(tripletData);
    }

    /** {@inheritDoc} */
    public String toString() {
        return "IOB: " + this.getName();
    }

    /**
     * Sets the mapping option
     *
     * @param optionValue the mapping option value
     */
    public void setMappingOption(byte optionValue) {
        addTriplet(new MappingOptionTriplet(optionValue));
    }
}