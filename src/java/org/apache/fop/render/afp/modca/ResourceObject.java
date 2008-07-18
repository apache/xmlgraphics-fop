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

package org.apache.fop.render.afp.modca;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.fop.render.afp.modca.triplets.Triplet;
import org.apache.fop.render.afp.tools.BinaryUtils;

/**
 * This resource structured field begins an envelope that is used to carry
 * resource objects in print-file-level (external) resource groups. 
 */
public class ResourceObject extends AbstractPreparedAFPObject {
    
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
     * @param obj the named data object
     */
    public void setDataObject(AbstractNamedAFPObject obj) {
        this.namedObject = obj;
    }
        
    /** {@inheritDoc} */
    protected void writeStart(OutputStream os) throws IOException {
        super.writeStart(os);

        byte[] data = new byte[19];
        copySF(data, Type.BEGIN, Category.NAME_RESOURCE);

        // Set the total record length
        byte[] len = BinaryUtils.convert(18 + getTripletDataLength(), 2);
        data[1] = len[0]; // Length byte 1
        data[2] = len[1]; // Length byte 2
            
        // Set reserved bits
        data[17] = 0x00; // Reserved
        data[18] = 0x00; // Reserved
        os.write(data);
    }

    /** {@inheritDoc} */
    protected void writeContent(OutputStream os) throws IOException {
        super.writeContent(os); // write triplets
        if (namedObject != null) {
            namedObject.write(os);
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

    /**
     * Resource object types
     */
    protected static final byte GRAPHICS_OBJECT = 0x03;
//    private static final byte BARCODE_OBJECT = 0x05;
    protected static final byte IMAGE_OBJECT = 0x06;
//    private static final byte FONT_CHARACTER_SET_OBJECT = 0x40;
//    private static final byte CODE_PAGE_OBJECT = 0x41;
//    private static final byte CODED_FONT_OBJECT = 0x42;
    protected static final byte OBJECT_CONTAINER = (byte) 0x92;
    protected static final byte DOCUMENT_OBJECT = (byte) 0xA8;
    protected static final byte PAGE_SEGMENT_OBJECT = (byte) 0xFB;
    protected static final byte OVERLAY_OBJECT = (byte) 0xFC;
//    private static final byte PAGEDEF_OBJECT = (byte) 0xFD;
//    private static final byte FORMDEF_OBJECT = (byte) 0xFE;

    private class ResourceObjectTypeTriplet extends Triplet {

        private static final byte RESOURCE_OBJECT = 0x21;

        /**
         * Main constructor
         * 
         * @param type
         *            the resource type
         */
        public ResourceObjectTypeTriplet(byte type) {
            super(RESOURCE_OBJECT,
                new byte[] {
                    type,
                    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 // Constant Data
                }
            );
        }
    }
}
