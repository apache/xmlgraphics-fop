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

import org.apache.fop.render.afp.ObjectAreaInfo;


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
    private ObjectAreaDescriptor objectAreaDescriptor = null;

    /**
     * The ObjectAreaPosition for the object environment group
     */
    private ObjectAreaPosition objectAreaPosition = null;

    /**
     * The ImageDataDescriptor for the object environment group
     */
    private ImageDataDescriptor imageDataDescriptor = null;

    /**
     * The GraphicsDataDescriptor for the object environment group
     */
    private GraphicsDataDescriptor graphicsDataDescriptor = null;

    /**
     * Default constructor for the ObjectEnvironmentGroup.
     */
    public ObjectEnvironmentGroup() {
        this(DEFAULT_NAME);
    }

    /**
     * Constructor for the ObjectEnvironmentGroup, this takes a
     * name parameter which must be 8 characters long.
     * 
     * @param name the object environment group name
     */
    public ObjectEnvironmentGroup(String name) {
        super(name);
    }

    /**
     * Sets the object area parameters.
     * 
     * @param info the object area info
     */
    public void setObjectArea(ObjectAreaInfo info) {
        this.objectAreaDescriptor = new ObjectAreaDescriptor(
                info.getWidth(), info.getHeight(),
                info.getWidthRes(), info.getHeightRes());
        this.objectAreaPosition = new ObjectAreaPosition(
                info.getX(), info.getY(), info.getRotation());
    }

    /**
     * Set the dimensions of the image.
     * 
     * @param xresol the x resolution of the image
     * @param yresol the y resolution of the image
     * @param width the image width
     * @param height the image height
     */
    public void setImageData(int xresol, int yresol, int width, int height) {
        this.imageDataDescriptor = new ImageDataDescriptor(xresol, yresol,  width, height);
    }

    /**
     * Set the graphics data descriptor.
     * 
     * @param xresol the x resolution of the graphics window
     * @param yresol the y resolution of the graphics window
     * @param xlwind the left edge of the graphics window 
     * @param xrwind the right edge of the graphics window
     * @param ybwind the top edge of the graphics window
     * @param ytwind the bottom edge of the graphics window
     */
    public void setGraphicsData(int xresol, int yresol,
            int xlwind, int xrwind, int ybwind, int ytwind) {
        this.graphicsDataDescriptor = new GraphicsDataDescriptor(xresol, yresol,
                xlwind, xrwind, ybwind, ytwind);
    }

    /** {@inheritDoc} */
    protected void writeStart(OutputStream os) throws IOException {
        byte[] data = new byte[17];
        copySF(data, Type.BEGIN, Category.OBJECT_ENVIRONMENT_GROUP);
        os.write(data);
    }
    
    /** {@inheritDoc} */
    protected void writeContent(OutputStream os) throws IOException {
        super.writeContent(os);
        
        objectAreaDescriptor.write(os);
        objectAreaPosition.write(os);

        if (imageDataDescriptor != null) {
            imageDataDescriptor.write(os);
        }

        if (graphicsDataDescriptor != null) {
            graphicsDataDescriptor.write(os);
        }
    }
    
    /** {@inheritDoc} */
    protected void writeEnd(OutputStream os) throws IOException {
        byte[] data = new byte[17];
        copySF(data, Type.END, Category.OBJECT_ENVIRONMENT_GROUP);
        os.write(data);
    }
}
