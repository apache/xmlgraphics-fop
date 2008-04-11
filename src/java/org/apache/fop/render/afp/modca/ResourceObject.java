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

import org.apache.fop.render.afp.ResourceInfo;
import org.apache.fop.render.afp.modca.triplets.Triplet;
import org.apache.fop.render.afp.tools.BinaryUtils;

/**
 * This resource structured field begins an envelope that is used to carry
 * resource objects in print-file-level (external) resource groups. 
 */
public class ResourceObject extends AbstractPreparedAFPObject {
    
    /**
     * Resource object types
     */
    private static final byte GRAPHICS_OBJECT = 0x03;
    private static final byte BARCODE_OBJECT = 0x05;
    private static final byte IMAGE_OBJECT = 0x06;
    private static final byte FONT_CHARACTER_SET_OBJECT = 0x40;
    private static final byte CODE_PAGE_OBJECT = 0x41;
    private static final byte CODED_FONT_OBJECT = 0x42;
    private static final byte OBJECT_CONTAINER = (byte) 0x92;
    private static final byte DOCUMENT_OBJECT = (byte) 0xA8;
    private static final byte PAGE_SEGMENT_OBJECT = (byte) 0xFB;
    private static final byte OVERLAY_OBJECT = (byte) 0xFC;
    private static final byte PAGEDEF_OBJECT = (byte) 0xFD;
    private static final byte FORMDEF_OBJECT = (byte) 0xFE;
        
    /**
     * the referenced data object
     */    
    private AbstractNamedAFPObject dataObj = null;
        
    /**
     * Default constructor
     * 
     * @param name the name of this resource (reference id)
     * @param dataObj the resource object to be added
     */
    public ResourceObject(String name) {
        super(name);
    }
        
    /**
     * Sets the data object referenced by this resource object
     * @param dataObj the data object
     */
    public void setReferencedObject(AbstractNamedAFPObject dataObj) {
        this.dataObj = dataObj;
        setResourceObjectType(dataObj);
    }

    /**
     * @return the resource object contained in this envelope 
     */
    public AbstractNamedAFPObject getReferencedObject() {
        return this.dataObj;
    }
    
    private void setResourceObjectType(AbstractNamedAFPObject resourceObj) {
        byte type;
        if (resourceObj instanceof ImageObject) {
            type = IMAGE_OBJECT;
        } else if (resourceObj instanceof GraphicsObject) {
            type = GRAPHICS_OBJECT;
        } else if (resourceObj instanceof Document) {
            type = DOCUMENT_OBJECT;
        } else if (resourceObj instanceof PageSegment) {
            type = PAGE_SEGMENT_OBJECT;
        } else if (resourceObj instanceof Overlay) {
            type = OVERLAY_OBJECT;
        } else {
            throw new UnsupportedOperationException(
                    "Unsupported resource object type " + resourceObj);
        }
        getTriplets().add(new ResourceObjectTypeTriplet(type));        
    }
        
    /**
     * {@inheritDoc}
     */
    protected void writeStart(OutputStream os) throws IOException {
        super.writeStart(os);
        
        // Set the total record length
        byte[] len = BinaryUtils.convert(18 + getTripletDataLength(), 2);
        byte[] data = new byte[] {
            0x5A, // Structured field identifier
            len[0], // Length byte 1
            len[1], // Length byte 2
            (byte)0xD3, // Structured field id byte 1
            (byte)0xA8, // Structured field id byte 2
            (byte)0xCE, // Structured field id byte 3
            0x00, // Flags
            0x00, // Reserved
            0x00, // Reserved
            nameBytes[0],            
            nameBytes[1],
            nameBytes[2],
            nameBytes[3],
            nameBytes[4],
            nameBytes[5],
            nameBytes[6],
            nameBytes[7],
            0x00, // Reserved
            0x00, // Reserved
        };
        os.write(data);
    }

    /**
     * {@inheritDoc}
     */
    protected void writeContent(OutputStream os) throws IOException {
        super.writeContent(os); // write triplets
        if (dataObj != null) {
            dataObj.writeDataStream(os);
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void writeEnd(OutputStream os) throws IOException {
        byte[] data = new byte[] {
           0x5A, // Structured field identifier
           0x00, // Length byte 1
           0x10, // Length byte 2
           (byte)0xD3, // Structured field id byte 1
           (byte)0xA9, // Structured field id byte 2
           (byte)0xCE, // Structured field id byte 3
           0x00, // Flags
           0x00, // Reserved
           0x00, // Reserved
           nameBytes[0],            
           nameBytes[1],
           nameBytes[2],
           nameBytes[3],
           nameBytes[4],
           nameBytes[5],
           nameBytes[6],
           nameBytes[7],
        };
        os.write(data);
    }
    
    /**
     * {@inheritDoc}
     */
    public String toString() {
        return this.getName();
    }
    
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
                    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
                }
            );
        }
    }
}
