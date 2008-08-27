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

import org.apache.fop.render.afp.ioca.ImageCellPosition;
import org.apache.fop.render.afp.ioca.ImageInputDescriptor;
import org.apache.fop.render.afp.ioca.ImageOutputControl;
import org.apache.fop.render.afp.ioca.ImageRasterData;

/**
 * An IM image data object specifies the contents of a raster image and
 * its placement on a page, overlay, or page segment. An IM image can be
 * either simple or complex. A simple image is composed of one or more Image
 * Raster Data (IRD) structured fields that define the raster pattern for the
 * entire image. A complex image is divided into regions called image cells.
 * Each image cell is composed of one or more IRD structured fields that define
 * the raster pattern for the image cell, and one Image Cell Position (ICP)
 * structured field that defines the position of the image cell relative to
 * the origin of the entire image. Each ICP also specifies the size of the
 * image cell and a fill rectangle into which the cell is replicated.
 * <p/>
 */
public class IMImageObject extends AbstractNamedAFPObject {

    /**
     * The image output control
     */
    private ImageOutputControl imageOutputControl = null;

    /**
     * The image input descriptor
     */
    private ImageInputDescriptor imageInputDescriptor = null;

    /**
     * The image cell position
     */
    private ImageCellPosition imageCellPosition = null;

    /**
     * The image rastor data
     */
    private ImageRasterData imageRasterData = null;

    /**
     * Constructor for the image object with the specified name,
     * the name must be a fixed length of eight characters.
     *
     * @param name The name of the image.
     */
    public IMImageObject(String name) {
        super(name);
    }

    /**
     * Sets the ImageOutputControl.
     *
     * @param imageOutputControl The imageOutputControl to set
     */
    public void setImageOutputControl(ImageOutputControl imageOutputControl) {
        this.imageOutputControl = imageOutputControl;
    }

    /**
     * Sets the ImageCellPosition.
     *
     * @param imageCellPosition The imageCellPosition to set
     */
    public void setImageCellPosition(ImageCellPosition imageCellPosition) {
        this.imageCellPosition = imageCellPosition;
    }

    /**
     * Sets the ImageInputDescriptor.
     *
     * @param imageInputDescriptor The imageInputDescriptor to set
     */
    public void setImageInputDescriptor(ImageInputDescriptor imageInputDescriptor) {
        this.imageInputDescriptor = imageInputDescriptor;
    }

    /**
     * Sets the ImageRastorData.
     *
     * @param imageRasterData The imageRasterData to set
     */
    public void setImageRasterData(ImageRasterData imageRasterData) {
        this.imageRasterData = imageRasterData;
    }

    /** {@inheritDoc} */
    protected void writeContent(OutputStream os) throws IOException {
        super.writeContent(os);
        if (imageOutputControl != null) {
            imageOutputControl.writeToStream(os);
        }
        if (imageInputDescriptor != null) {
            imageInputDescriptor.writeToStream(os);
        }
        if (imageCellPosition != null) {
            imageCellPosition.writeToStream(os);
        }
        if (imageRasterData != null) {
            imageRasterData.writeToStream(os);
        }
    }

    /** {@inheritDoc} */
    protected void writeStart(OutputStream os) throws IOException {
        byte[] data = new byte[17];
        copySF(data, Type.BEGIN, Category.IM_IMAGE);
        os.write(data);
    }

    /** {@inheritDoc} */
    protected void writeEnd(OutputStream os) throws IOException {
        byte[] data = new byte[17];
        copySF(data, Type.END, Category.IM_IMAGE);
        os.write(data);
    }
}
