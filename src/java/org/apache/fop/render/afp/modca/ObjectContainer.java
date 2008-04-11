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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.fop.render.afp.DataObjectInfo;
import org.apache.fop.render.afp.modca.triplets.ObjectClassificationTriplet;
import org.apache.fop.render.afp.modca.triplets.StrucFlgs;
import org.apache.fop.render.afp.tools.BinaryUtils;

/**
 * Object containers are MO:DCA objects that envelop and carry object data.
 */
public class ObjectContainer extends AbstractNamedAFPObject {
    
    /**
     * the object container data
     */
    private ObjectContainerData objectContainerData;

    /**
     * Main constructor
     * @param name the name of this object container
     * @param dataObj the data object to reside within this object container
     * @param info the data object info about the data object
     */
    public ObjectContainer(String name, AbstractDataObject dataObj, DataObjectInfo info) {
        super(name);
        
        final boolean dataInContainer = true;
        final boolean containerHasOEG = false;
        final boolean dataInOCD = true;
        StrucFlgs strucFlgs = new StrucFlgs(
            dataInContainer, containerHasOEG, dataInOCD
        );
        ObjectTypeRegistry registry = ObjectTypeRegistry.getInstance();
        ObjectTypeRegistry.ObjectType entry = registry.getObjectType(info);
        super.setObjectClassification(
            ObjectClassificationTriplet.CLASS_TIME_VARIANT_PRESENTATION_OBJECT,
            entry, strucFlgs
        );
        
        // write data object to object container data
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        // TODO: AC - fix
//        dataObj.writeDataStream(bos);
        this.objectContainerData = new ObjectContainerData(bos.toByteArray());
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
            (byte)0x92, // Structured field id byte 3
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
        if (objectContainerData != null) {
            objectContainerData.writeDataStream(os);
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
           (byte)0x92, // Structured field id byte 3
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
    
    private class ObjectContainerData extends AbstractStructuredAFPObject {
        /** the object data */
        private byte[] objData = null;
        
        /**
         * Main constructor
         * @param objData the object data
         */
        public ObjectContainerData(byte[] objData) {
            this.objData = objData;
        }
        
        /**
         * {@inheritDoc}
         */
        public void writeDataStream(OutputStream os) throws IOException {
            // Set the total record length
            byte[] len = BinaryUtils.convert(8 + objData.length, 2);
            byte[] data = new byte[] {
                0x5A, // Structured field identifier
                len[0], // Length byte 1
                len[1], // Length byte 2
                (byte)0xD3, // Structured field id byte 1
                (byte)0xEE, // Structured field id byte 2
                (byte)0x92, // Structured field id byte 3
                0x00, // Flags
                0x00, // Reserved
                0x00, // Reserved
            };

            os.write(data);
        }
    }
}
