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

import org.apache.fop.render.afp.tools.BinaryUtils;

/**
 * Object containers are MO:DCA objects that envelop and carry object data.
 */
public class ObjectContainer extends AbstractNamedAFPObject /*implements DataObjectAccessor*/ {
                
    private static final String DEFAULT_NAME = "OC000001";

    /** the data object */
    private AbstractDataObject dataObj = null;
    
    /** the object data */
    private byte[] objectData = null;

    /**
     * Default constructor
     */
    public ObjectContainer() {
        super(DEFAULT_NAME);
    }

    /**
     * Main constructor
     * 
     * @param name the name of this object container
     */
    public ObjectContainer(String name) {
        super(name);        
    }
    
    /**
     * Sets the data object for this object container
     * 
     * @param dataObj the data object to reside within this object container
     */
    public void setDataObject(AbstractDataObject dataObj) {
        this.dataObj = dataObj;        
    }

    /** {@inheritDoc} */
    protected void writeStart(OutputStream os) throws IOException {
        // create object data from data object
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        dataObj.write(bos);
        this.objectData = bos.toByteArray();

        byte[] data = new byte[17];
        copySF(data, Type.BEGIN, Category.OBJECT_CONTAINER);

        // Set the total record length
        byte[] len = BinaryUtils.convert(16 + getTripletDataLength(), 2);
        data[1] = len[0]; // Length byte 1
        data[2] = len[1]; // Length byte 2
        
        os.write(data);
    }
    
    /** {@inheritDoc} */
    protected void writeContent(OutputStream os) throws IOException {
        super.writeContent(os);
        
        // write out object data in chunks of object container data
        for (int i = 0; i <= objectData.length; i += ObjectContainerData.MAX_DATA_LEN) {
            new ObjectContainerData(objectData, i).write(os);
        }
    }
    
    /** {@inheritDoc} */
    protected void writeEnd(OutputStream os) throws IOException {
        byte[] data = new byte[17];
        copySF(data, Type.END, Category.OBJECT_CONTAINER);
        os.write(data);
    }
    
    /**
     * An Object Container Data holds a chunk of the data object
     */
    private class ObjectContainerData extends AbstractPreparedAFPObject {
        
        /** The maximum object container data length */ 
        private static final int MAX_DATA_LEN = 32759;
        
        private byte[] objData = null;
        
        private int startIndex;
        
        /**
         * Main constructor
         * 
         * @param objData the object data
         */
        public ObjectContainerData(byte[] objData, int startIndex) {
            this.objData = objData;
            this.startIndex = startIndex;
        }
        
        /** {@inheritDoc} */
        public void write(OutputStream os) throws IOException {
            int dataLen = MAX_DATA_LEN;
            if (startIndex + MAX_DATA_LEN >= objData.length) {
                dataLen = objData.length - startIndex - 1;
            }
            byte[] data = new byte[9 + dataLen];
            copySF(data, Type.DATA, Category.OBJECT_CONTAINER);

            byte[] len = BinaryUtils.convert(8 + dataLen, 2);
            data[1] = len[0]; // Length byte 1
            data[2] = len[1]; // Length byte 2
            
            // copy object data chunk
            System.arraycopy(objData, startIndex, data, 9, dataLen);
            os.write(data);
        }
        
        /** {@inheritDoc} */
        public String toString() {
            return "ObjectContainerData("
                + (data != null ? "" + (data.length - 2) : "null")
                + ")";
        }

    }
}
