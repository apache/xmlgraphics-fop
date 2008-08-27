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

import org.apache.fop.render.afp.AFPObjectAreaInfo;

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

    private final Factory factory;

    /** the ObjectAreaDescriptor for the object environment group */
    private ObjectAreaDescriptor objectAreaDescriptor = null;

    /** the ObjectAreaPosition for the object environment group */
    private ObjectAreaPosition objectAreaPosition = null;

    /** the DataDescritpor for the object environment group */
    private AbstractDescriptor dataDescriptor;

    /**
     * Constructor for the ObjectEnvironmentGroup, this takes a
     * name parameter which must be 8 characters long.
     *
     * @param factory the object factory
     * @param name the object environment group name
     */
    public ObjectEnvironmentGroup(Factory factory, String name) {
        super(name);
        this.factory = factory;
    }

    /**
     * Sets the object area parameters.
     *
     * @param info the object area info
     */
    public void setObjectArea(AFPObjectAreaInfo info) {
        this.objectAreaDescriptor = factory.createObjectAreaDescriptor(
                info.getWidth(), info.getHeight(), info.getWidthRes(), info.getHeightRes());
        this.objectAreaPosition = factory.createObjectAreaPosition(
                info.getX(), info.getY(), info.getRotation());
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

        if (objectAreaDescriptor != null) {
            objectAreaDescriptor.writeToStream(os);
        }

        if (objectAreaPosition != null) {
            objectAreaPosition.writeToStream(os);
        }

        if (dataDescriptor != null) {
            dataDescriptor.writeToStream(os);
        }
    }

    /** {@inheritDoc} */
    protected void writeEnd(OutputStream os) throws IOException {
        byte[] data = new byte[17];
        copySF(data, Type.END, Category.OBJECT_ENVIRONMENT_GROUP);
        os.write(data);
    }

    /**
     * Sets the data descriptor
     *
     * @param dataDescriptor the data descriptor
     */
    public void setDataDescriptor(AbstractDescriptor dataDescriptor) {
        this.dataDescriptor = dataDescriptor;
    }
}
