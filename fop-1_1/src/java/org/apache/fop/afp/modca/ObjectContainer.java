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

import org.apache.fop.afp.AFPDataObjectInfo;
import org.apache.fop.afp.AFPObjectAreaInfo;
import org.apache.fop.afp.AFPResourceInfo;
import org.apache.fop.afp.AFPResourceLevel;
import org.apache.fop.afp.Factory;
import org.apache.fop.afp.modca.triplets.MappingOptionTriplet;
import org.apache.fop.afp.util.BinaryUtils;

/**
 * Object containers are MO:DCA objects that envelop and carry object data.
 */
public class ObjectContainer extends AbstractDataObject {

    /** the object container data maximum length */
    private static final int MAX_DATA_LEN = 32759;

    private byte[] data;

    /**
     * Main constructor
     *
     * @param factory the object factory
     * @param name the name of this object container
     */
    public ObjectContainer(Factory factory, String name) {
        super(factory, name);
    }

    /** {@inheritDoc} */
    protected void writeStart(OutputStream os) throws IOException {
        byte[] headerData = new byte[17];
        copySF(headerData, Type.BEGIN, Category.OBJECT_CONTAINER);

        // Set the total record length
        int containerLen = headerData.length + getTripletDataLength() - 1;
        byte[] len = BinaryUtils.convert(containerLen, 2);
        headerData[1] = len[0]; // Length byte 1
        headerData[2] = len[1]; // Length byte 2

        os.write(headerData);
    }

    /** {@inheritDoc} */
    protected void writeContent(OutputStream os) throws IOException {
        super.writeContent(os); // write triplets and OEG

        // write OCDs
        byte[] dataHeader = new byte[9];
        copySF(dataHeader, SF_CLASS, Type.DATA, Category.OBJECT_CONTAINER);
        final int lengthOffset = 1;

        if (data != null) {
            writeChunksToStream(data, dataHeader, lengthOffset, MAX_DATA_LEN, os);
        }
    }

    /** {@inheritDoc} */
    protected void writeEnd(OutputStream os) throws IOException {
        byte[] data = new byte[17];
        copySF(data, Type.END, Category.OBJECT_CONTAINER);
        os.write(data);
    }

    /** {@inheritDoc} */
    public void setViewport(AFPDataObjectInfo dataObjectInfo) {
        AFPResourceInfo resourceInfo = dataObjectInfo.getResourceInfo();
        AFPResourceLevel resourceLevel = resourceInfo.getLevel();

        // only need to set MCD and CDD when OC when is inlined (pre-2000 apps)
        if (resourceLevel.isInline()) {
            super.setViewport(dataObjectInfo);

            MapContainerData mapContainerData
            = factory.createMapContainerData(MappingOptionTriplet.SCALE_TO_FIT);
            getObjectEnvironmentGroup().setMapContainerData(mapContainerData);

            int dataWidth = dataObjectInfo.getDataWidth();
            int dataHeight = dataObjectInfo.getDataHeight();

            AFPObjectAreaInfo objectAreaInfo = dataObjectInfo.getObjectAreaInfo();
            int widthRes = objectAreaInfo.getWidthRes();
            int heightRes = objectAreaInfo.getHeightRes();

            ContainerDataDescriptor containerDataDescriptor
                = factory.createContainerDataDescriptor(
                    dataWidth, dataHeight, widthRes, heightRes);
            getObjectEnvironmentGroup().setDataDescriptor(containerDataDescriptor);
        }
    }

    /**
     * Sets the data for the object container
     *
     * @param data a byte array
     */
    public void setData(byte[] data) {
        this.data = data;
    }
}
