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

import org.apache.fop.afp.modca.triplets.ResourceObjectTypeTriplet;
import org.apache.fop.afp.util.BinaryUtils;

/**
 * This resource structured field begins an envelope that is used to carry
 * resource objects in print-file-level (external) resource groups.
 */
public class ResourceObject extends AbstractNamedAFPObject {

    /** graphics object type */
    public static final byte TYPE_GRAPHIC = 0x03;

    /** barcode object type */
    public static final byte TYPE_BARCODE = 0x05;

    /** image object type */
    public static final byte TYPE_IMAGE = 0x06;

    /** font character set type */
    public static final byte TYPE_FONT_CHARACTER_SET = 0x40;

    /** code page type */
    public static final byte TYPE_CODE_PAGE = 0x41;

    /** coded font type */
    public static final byte TYPE_CODED_FONT = 0x42;

    /** object container type */
    public static final byte TYPE_OBJECT_CONTAINER = (byte) 0x92;

    /** document object type */
    public static final byte TYPE_DOCUMENT = (byte) 0xA8;

    /** page segment object type */
    public static final byte TYPE_PAGE_SEGMENT = (byte) 0xFB;

    /** overlay object type */
    public static final byte TYPE_OVERLAY_OBJECT = (byte) 0xFC;

    /** page def type */
    public static final byte TYPE_PAGEDEF = (byte) 0xFD;

    /** form def type */
    public static final byte TYPE_FORMDEF = (byte) 0xFE;

    private AbstractNamedAFPObject namedObject;

    /**
     * Default constructor
     *
     * @param name the name of this resource (reference id)
     */
    public ResourceObject(String name) {
        super(name);
    }

    /**
     * Sets the data object referenced by this resource object
     *
     * @param namedObject the named data object
     */
    public void setDataObject(AbstractNamedAFPObject namedObject) {
        this.namedObject = namedObject;
    }

    /**
     * Returns the data object referenced by this resource object
     *
     * @return the data object referenced by this resource object
     */
    public AbstractNamedAFPObject getDataObject() {
        return namedObject;
    }

    /** {@inheritDoc} */
    protected void writeStart(OutputStream os) throws IOException {
        super.writeStart(os);

        byte[] data = new byte[19];
        copySF(data, Type.BEGIN, Category.NAME_RESOURCE);

        // Set the total record length
        int tripletDataLength = getTripletDataLength();
        byte[] len = BinaryUtils.convert(18 + tripletDataLength, 2);
        data[1] = len[0]; // Length byte 1
        data[2] = len[1]; // Length byte 2

        // Set reserved bits
        data[17] = 0x00; // Reserved
        data[18] = 0x00; // Reserved

        os.write(data);

        // Write triplets
        writeTriplets(os);
    }

    /** {@inheritDoc} */
    protected void writeContent(OutputStream os) throws IOException {
        if (namedObject != null) {
            namedObject.writeToStream(os);
        }
    }

    /** {@inheritDoc} */
    protected void writeEnd(OutputStream os) throws IOException {
        byte[] data = new byte[17];
        copySF(data, Type.END, Category.NAME_RESOURCE);
        os.write(data);
    }

    /** {@inheritDoc} */
    public String toString() {
        return this.getName();
    }

    /**
     * Sets Resource Object Type triplet
     *
     * @param type the resource object type
     */
    public void setType(byte type) {
        getTriplets().add(new ResourceObjectTypeTriplet(type));
    }

}
