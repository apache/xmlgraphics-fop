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

package org.apache.fop.afp.modca;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.fop.afp.modca.triplets.FullyQualifiedNameTriplet;
import org.apache.fop.afp.util.BinaryUtils;

/**
 * The Preprocess Presentation Object structured field specifies presentation
 * parameters for a data object that has been mapped as a resource.
 */
public class PreprocessPresentationObject extends AbstractTripletStructuredObject {
    private static final byte TYPE_OTHER = (byte)0x92;
    private static final byte TYPE_OVERLAY = (byte)0xDF;
    private static final byte TYPE_IMAGE = (byte)0xFB;

    private byte objType = TYPE_OTHER;
    private byte objOrent = 0; // object always processed at 0 degree orientation
    private int objXOffset = -1;
    private int objYOffset = -1;

    /**
     * Main constructor
     *
     * @param prePresObj the presentation object to be preprocessed
     */
    public PreprocessPresentationObject(AbstractTripletStructuredObject prePresObj) {
        if (prePresObj instanceof ImageObject || prePresObj instanceof Overlay) {
            if (prePresObj instanceof ImageObject) {
                this.objType = TYPE_IMAGE;
            } else {
                this.objType = TYPE_OVERLAY;
            }
            setFullyQualifiedName(
                    FullyQualifiedNameTriplet.TYPE_BEGIN_RESOURCE_OBJECT_REF,
                    FullyQualifiedNameTriplet.FORMAT_CHARSTR,
                    prePresObj.getFullyQualifiedName());
        } else {
            this.objType = TYPE_OTHER;
        }
    }

    public static final byte ORIENTATION_ZERO_DEGREES = 1;
    public static final byte ORIENTATION_90_DEGREES = 2;
    public static final byte ORIENTATION_180_DEGREES = 4;
    public static final byte ORIENTATION_270_DEGREES = 8;

    /**
     * Sets the object orientations relative to media leading edge
     *
     * @param orientation the object orientations relative to media leading edge
     */
    public void setOrientation(byte orientation) {
        objOrent = orientation;
    }

    /**
     * Sets the X axis origin for object content
     *
     * @param xOffset the X axis origin for object content
     */
    public void setXOffset(int xOffset) {
        this.objXOffset = xOffset;
    }

    /**
     * Sets the Y axis origin for object content
     *
     * @param yOffset the Y axis origin for object content
     */
    public void setYOffset(int yOffset) {
        this.objYOffset = yOffset;
    }

    /** {@inheritDoc} */
    public void writeStart(OutputStream os) throws IOException {
        super.writeStart(os);

        byte[] data = new byte[9];
        copySF(data, Type.PROCESS, Category.DATA_RESOURCE);

        byte[] l = BinaryUtils.convert(19 + getTripletDataLength(), 2);
        data[1] = l[0]; // Length byte 1
        data[2] = l[1]; // Length byte 1

        os.write(data);
    }

    /** {@inheritDoc} */
    public void writeContent(OutputStream os) throws IOException {
        byte[] data = new byte[12];
        byte[] l = BinaryUtils.convert(12 + getTripletDataLength(), 2);
        data[0] = l[0]; // RGLength
        data[1] = l[1]; // RGLength
        data[2] = objType; // ObjType
        data[3] = 0x00; // Reserved
        data[4] = 0x00; // Reserved
        data[5] = objOrent; // ObjOrent
        if (objXOffset > 0) {
            byte[] xOff = BinaryUtils.convert(objYOffset, 3);
            data[6] = xOff[0]; // XocaOset (not specified)
            data[7] = xOff[1]; // XocaOset
            data[8] = xOff[2]; // XocaOset
        } else {
            data[6] = (byte)0xFF; // XocaOset (not specified)
            data[7] = (byte)0xFF; // XocaOset
            data[8] = (byte)0xFF; // XocaOset
        }
        if (objYOffset > 0) {
            byte[] yOff = BinaryUtils.convert(objYOffset, 3);
            data[9] = yOff[0]; // YocaOset (not specified)
            data[10] = yOff[1]; // YocaOset
            data[11] = yOff[2]; // YocaOset
        } else {
            data[9] = (byte)0xFF; // YocaOset (not specified)
            data[10] = (byte)0xFF; // YocaOset
            data[11] = (byte)0xFF; // YocaOset
        }
        os.write(data);

        // Triplets
        super.writeContent(os);
    }

}
