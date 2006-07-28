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
import java.io.UnsupportedEncodingException;


/**
 * An Object Environment Group (OEG) may be associated with an object and is contained
 * within the object's begin-end envelope.
 * The object environment group defines the object's origin and orientation on the page,
 * and can contain font and color attribute table information. The scope of an object
 * environment group is the scope of its containing object.
 *
 * An application that creates a data-stream document may omit some of the parameters
 * normally contained in the object environment group, or it may specify that one or
 * more default values are to be used.
 */
public final class ObjectEnvironmentGroup extends AbstractNamedAFPObject {

    /**
     * Default name for the object environment group
     */
    private static final String DEFAULT_NAME = "OEG00001";

    /**
     * The ObjectAreaDescriptor for the object environment group
     */
    private ObjectAreaDescriptor _objectAreaDescriptor = null;

    /**
     * The ObjectAreaPosition for the object environment group
     */
    private ObjectAreaPosition _objectAreaPosition = null;

    /**
     * The ImageDataDescriptor for the object environment group
     */
    private ImageDataDescriptor _imageDataDescriptor = null;

    /**
     * Default constructor for the ObjectEnvironmentGroup.
     */
    public ObjectEnvironmentGroup() {

        this(DEFAULT_NAME);

    }

    /**
     * Constructor for the ObjectEnvironmentGroup, this takes a
     * name parameter which must be 8 characters long.
     * @param name the object environment group name
     */
    public ObjectEnvironmentGroup(String name) {

        super(name);

    }

    /**
     * Sets the object area parameters.
     * @param x the x position of the object
     * @param y the y position of the object
     * @param width the object width
     * @param height the object height
     * @param rotation the object orientation
     */
    public void setObjectArea(int x, int y, int width, int height, int rotation) {

        _objectAreaDescriptor = new ObjectAreaDescriptor(width, height);
        _objectAreaPosition = new ObjectAreaPosition(x, y, rotation);

    }

    /**
     * Set the dimensions of the image.
     * @param xresol the x resolution of the image
     * @param yresol the y resolution of the image
     * @param width the image width
     * @param height the image height
     */
    public void setImageData(int xresol, int yresol, int width, int height) {
        _imageDataDescriptor = new ImageDataDescriptor(xresol, yresol,  width, height);
    }

    /**
     * Accessor method to obtain write the AFP datastream for
     * the object environment group.
     * @param os The stream to write to
     * @throws java.io.IOException
     */
    public void writeDataStream(OutputStream os)
        throws IOException {


        writeStart(os);

        _objectAreaDescriptor.writeDataStream(os);

        _objectAreaPosition.writeDataStream(os);

        if (_imageDataDescriptor != null) {
            _imageDataDescriptor.writeDataStream(os);
        }

        writeEnd(os);

    }

    /**
     * Helper method to write the start of the object environment group.
     * @param os The stream to write to
     */
    private void writeStart(OutputStream os)
        throws IOException {

        byte[] data = new byte[] {
            0x5A, // Structured field identifier
            0x00, // Length byte 1
            0x10, // Length byte 2
            (byte) 0xD3, // Structured field id byte 1
            (byte) 0xA8, // Structured field id byte 2
            (byte) 0xC7, // Structured field id byte 3
            0x00, // Flags
            0x00, // Reserved
            0x00, // Reserved
            0x00, // Name
            0x00, //
            0x00, //
            0x00, //
            0x00, //
            0x00, //
            0x00, //
            0x00, //
        };

        for (int i = 0; i < _nameBytes.length; i++) {

            data[9 + i] = _nameBytes[i];

        }

        os.write(data);

    }

    /**
     * Helper method to write the end of the object environment group.
     * @param os The stream to write to
     */
    private void writeEnd(OutputStream os)
        throws IOException {

        byte[] data = new byte[17];

        data[0] = 0x5A; // Structured field identifier
        data[1] = 0x00; // Length byte 1
        data[2] = 0x10; // Length byte 2
        data[3] = (byte) 0xD3; // Structured field id byte 1
        data[4] = (byte) 0xA9; // Structured field id byte 2
        data[5] = (byte) 0xC7; // Structured field id byte 3
        data[6] = 0x00; // Flags
        data[7] = 0x00; // Reserved
        data[8] = 0x00; // Reserved

        for (int i = 0; i < _nameBytes.length; i++) {

            data[9 + i] = _nameBytes[i];

        }

        os.write(data);

    }

}