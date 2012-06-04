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

import org.apache.commons.io.output.ByteArrayOutputStream;

import org.apache.xmlgraphics.util.MimeConstants;

import org.apache.fop.afp.AFPDataObjectInfo;
import org.apache.fop.afp.AFPImageObjectInfo;
import org.apache.fop.afp.Factory;
import org.apache.fop.afp.ioca.ImageSegment;

/**
 * An IOCA Image Data Object
 */
public class ImageObject extends AbstractDataObject {

    private static final int MAX_DATA_LEN = 8192;

    /** the image segment */
    private ImageSegment imageSegment = null;

    /**
     * Constructor for the image object with the specified name,
     * the name must be a fixed length of eight characters.
     *
     * @param name The name of the image.
     * @param factory the resource manager
     */
    public ImageObject(Factory factory, String name) {
        super(factory, name);
    }

    /**
     * Returns the image segment object associated with this image object.
     * @return the image segment
     */
    public ImageSegment getImageSegment() {
        if (imageSegment == null) {
            this.imageSegment = factory.createImageSegment();
        }
        return imageSegment;
    }

    /** {@inheritDoc} */
    public void setViewport(AFPDataObjectInfo dataObjectInfo) {
        super.setViewport(dataObjectInfo);

        AFPImageObjectInfo imageObjectInfo = (AFPImageObjectInfo)dataObjectInfo;
        int dataWidth = imageObjectInfo.getDataWidth();
        int dataHeight = imageObjectInfo.getDataHeight();

        int dataWidthRes = imageObjectInfo.getDataWidthRes();
        int dataHeightRes = imageObjectInfo.getDataWidthRes();
        ImageDataDescriptor imageDataDescriptor
            = factory.createImageDataDescriptor(dataWidth, dataHeight, dataWidthRes, dataHeightRes);

        if (MimeConstants.MIME_AFP_IOCA_FS45.equals(imageObjectInfo.getMimeType())) {
            imageDataDescriptor.setFunctionSet(ImageDataDescriptor.FUNCTION_SET_FS45);
        } else if (imageObjectInfo.getBitsPerPixel() == 1) {
            imageDataDescriptor.setFunctionSet(ImageDataDescriptor.FUNCTION_SET_FS10);
        }
        getObjectEnvironmentGroup().setDataDescriptor(imageDataDescriptor);
        getObjectEnvironmentGroup().setMapImageObject(
                new MapImageObject(dataObjectInfo.getMappingOption()));

        getImageSegment().setImageSize(dataWidth, dataHeight, dataWidthRes, dataHeightRes);
    }

    /**
     * Sets the image encoding.
     *
     * @param encoding The image encoding.
     */
    public void setEncoding(byte encoding) {
        getImageSegment().setEncoding(encoding);
    }

    /**
     * Sets the image compression.
     *
     * @param compression The image compression.
     */
    public void setCompression(byte compression) {
        getImageSegment().setCompression(compression);
    }

    /**
     * Sets the image IDE size.
     *
     * @param size The IDE size.
     */
    public void setIDESize(byte size) {
        getImageSegment().setIDESize(size);
    }

    /**
     * Sets the image IDE color model.
     *
     * @param colorModel    the IDE color model.
     * @deprecated Use {@link org.apache.fop.afp.ioca.IDEStructureParameter#setColorModel(byte)}
     * instead.
     */
    public void setIDEColorModel(byte colorModel) {
        getImageSegment().setIDEColorModel(colorModel);
    }

    /**
     * Set either additive or subtractive mode (used for ASFLAG).
     * @param subtractive true for subtractive mode, false for additive mode
     * @deprecated Use {@link org.apache.fop.afp.ioca.IDEStructureParameter#setSubtractive(boolean)}
     * instead.
     */
    public void setSubtractive(boolean subtractive) {
        getImageSegment().setSubtractive(subtractive);
    }

    /**
     * Set the data of the image.
     *
     * @param imageData the image data
     */
    public void setData(byte[] imageData) {
        getImageSegment().setData(imageData);
    }

    /** {@inheritDoc} */
    protected void writeStart(OutputStream os) throws IOException {
        byte[] data = new byte[17];
        copySF(data, Type.BEGIN, Category.IMAGE);
        os.write(data);
    }

    /** {@inheritDoc} */
    protected void writeContent(OutputStream os) throws IOException {
        super.writeContent(os);

        if (imageSegment != null) {
            final byte[] dataHeader = new byte[9];
            copySF(dataHeader, SF_CLASS, Type.DATA, Category.IMAGE);
            final int lengthOffset = 1;

            // TODO save memory!
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            imageSegment.writeToStream(baos);
            byte[] data = baos.toByteArray();
            writeChunksToStream(data, dataHeader, lengthOffset, MAX_DATA_LEN, os);
        }
    }

    /** {@inheritDoc} */
    protected void writeEnd(OutputStream os) throws IOException {
        byte[] data = new byte[17];
        copySF(data, Type.END, Category.IMAGE);
        os.write(data);
    }
}
