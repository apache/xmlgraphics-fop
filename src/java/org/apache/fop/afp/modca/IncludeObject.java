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

package org.apache.fop.afp.modca;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.fop.afp.modca.triplets.MappingOptionTriplet;
import org.apache.fop.afp.modca.triplets.MeasurementUnitsTriplet;
import org.apache.fop.afp.modca.triplets.ObjectAreaSizeTriplet;
import org.apache.fop.afp.util.BinaryUtils;

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

    /** the object referenced is of type page segment */
    public static final byte TYPE_PAGE_SEGMENT = (byte)0x5F;

    /** the object referenced is of type other */
    public static final byte TYPE_OTHER = (byte)0x92;

    /** the object referenced is of type graphic */
    public static final byte TYPE_GRAPHIC = (byte)0xBB;

    /** the object referenced is of type barcode */
    public static final byte TYPE_BARCODE = (byte)0xEB;

    /** the object referenced is of type image */
    public static final byte TYPE_IMAGE = (byte)0xFB;

    /** the object type referenced (default is other) */
    private byte objectType = TYPE_OTHER;

    /** the X-axis origin of the object area */
    private int xoaOset = 0;

    /** the Y-axis origin of the object area */
    private int yoaOset = 0;

    /** the orientation of the referenced object */
    private ObjectAreaRotation oaOrent = ObjectAreaRotation.RIGHT_HANDED_0;

    /** the X-axis origin defined in the object */
    private int xocaOset = -1;

    /** the Y-axis origin defined in the object */
    private int yocaOset = -1;

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
    public void setObjectAreaOrientation(int orientation) {
        this.oaOrent = ObjectAreaRotation.objectAreaRotationFor(orientation);
    }

    /**
     * Sets the x and y offset to the origin in the object area
     *
     * @param x the X-axis origin of the object area
     * @param y the Y-axis origin of the object area
     */
    public void setObjectAreaOffset(int x, int y) {
        this.xoaOset = x;
        this.yoaOset = y;
    }

    /**
     * Sets the x and y offset of the content area to the object area
     * used in conjunction with the
     * {@link MappingOptionTriplet#POSITION} and
     * {@link MappingOptionTriplet#POSITION_AND_TRIM}.
     *
     * @param x the X-axis origin defined in the object
     * @param y the Y-axis origin defined in the object
     */
    public void setContentAreaOffset(int x, int y) {
        this.xocaOset = x;
        this.yocaOset = y;
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
        int tripletDataLength = getTripletDataLength();
        byte[] len = BinaryUtils.convert(35 + tripletDataLength, 2); //Ignore first byte
        data[1] = len[0];
        data[2] = len[1];

        data[17] = 0x00; // reserved
        data[18] = objectType;

        writeOsetTo(data, 19, xoaOset);

        writeOsetTo(data, 22, yoaOset);

        oaOrent.writeTo(data, 25);

        writeOsetTo(data, 29, xocaOset);

        writeOsetTo(data, 32, yocaOset);

        // RefCSys (Reference coordinate system)
        data[35] = 0x01; // Page or overlay coordinate system

        // Write structured field data
        os.write(data);

        // Write triplet for FQN internal/external object reference
        writeTriplets(os);
    }

    private static void writeOsetTo(byte[] out, int offset, int oset) {
        if (oset > -1) {
            byte[] y = BinaryUtils.convert(oset, 3);
            out[offset] = y[0];
            out[offset + 1] = y[1];
            out[offset + 2] = y[2];
        } else {
            out[offset] = (byte)0xFF;
            out[offset + 1] = (byte)0xFF;
            out[offset + 2] = (byte)0xFF;
        }
    }

    private String getObjectTypeName() {
        String objectTypeName = null;
        if (objectType == TYPE_PAGE_SEGMENT) {
            objectTypeName = "page segment";
        } else if (objectType == TYPE_OTHER) {
            objectTypeName = "other";
        } else if (objectType == TYPE_GRAPHIC) {
            objectTypeName = "graphic";
        } else if (objectType == TYPE_BARCODE) {
            objectTypeName = "barcode";
        } else if (objectType == TYPE_IMAGE) {
            objectTypeName = "image";
        }
        return objectTypeName;
    }

    /** {@inheritDoc} */
    public String toString() {
        return "IncludeObject{name=" + this.getName()
            + ", objectType=" + getObjectTypeName()
            + ", xoaOset=" + xoaOset
            + ", yoaOset=" + yoaOset
            + ", oaOrent" + oaOrent
            + ", xocaOset=" + xocaOset
            + ", yocaOset=" + yocaOset
            + "}";
    }

    /**
     * Sets the mapping option
     *
     * @param optionValue the mapping option value
     */
    public void setMappingOption(byte optionValue) {
        addTriplet(new MappingOptionTriplet(optionValue));
    }

    /**
     * Sets the extent of an object area in the X and Y directions
     *
     * @param x the x direction extent
     * @param y the y direction extent
     */
    public void setObjectAreaSize(int x, int y) {
        addTriplet(new ObjectAreaSizeTriplet(x, y));
    }

    /**
     * Sets the measurement units used to specify the units of measure
     *
     * @param xRes units per base on the x-axis
     * @param yRes units per base on the y-axis
     */
    public void setMeasurementUnits(int xRes, int yRes) {
        addTriplet(new MeasurementUnitsTriplet(xRes, xRes));
    }

    /**
     * Represents the 4 bytes that specify the area rotation reference coordinate system
     *
     */
    private enum ObjectAreaRotation {

        RIGHT_HANDED_0(Rotation.ROTATION_0, Rotation.ROTATION_90),
        RIGHT_HANDED_90(Rotation.ROTATION_90, Rotation.ROTATION_180),
        RIGHT_HANDED_180(Rotation.ROTATION_180, Rotation.ROTATION_270),
        RIGHT_HANDED_270(Rotation.ROTATION_270, Rotation.ROTATION_0);

        /**
         * The object area’s X-axis rotation from the X axis of the reference coordinate system
         */
        private final Rotation xoaOrent;
        /**
         * The object area’s Y-axis rotation from the Y axis of the reference coordinate system
         */
        private final Rotation yoaOrent;

        public void writeTo(byte[] out, int offset) {
            xoaOrent.writeTo(out, offset);
            yoaOrent.writeTo(out, offset + 2);
        }

        ObjectAreaRotation(Rotation xoaOrent, Rotation yoaOrent) {
            this.xoaOrent = xoaOrent;
            this.yoaOrent = yoaOrent;
        }

        private static ObjectAreaRotation objectAreaRotationFor(int orientation) {
            switch (orientation) {
                case 0: return RIGHT_HANDED_0;
                case 90: return RIGHT_HANDED_90;
                case 180: return RIGHT_HANDED_180;
                case 270: return RIGHT_HANDED_270;
                default: throw new IllegalArgumentException(
                "The orientation must be one of the values 0, 90, 180, 270");
            }
        }
    }

    /**
     * Represents a rotation value
     *
     */
    private enum Rotation {

        ROTATION_0(0),
        ROTATION_90(0x2D),
        ROTATION_180(0x5A),
        ROTATION_270(0x87);

        private final byte firstByte;

        public void writeTo(byte[] out, int offset) {
            out[offset] = firstByte;
            out[offset + 1] = (byte)0;
        }

        Rotation(int firstByte) {
            this.firstByte = (byte) firstByte;
        }
    }

}
