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
import org.apache.fop.afp.Completable;
import org.apache.fop.afp.Factory;
import org.apache.fop.afp.Startable;

/**
 * Abstract base class used by the ImageObject and GraphicsObject which both
 * have define an ObjectEnvironmentGroup
 */
public abstract class AbstractDataObject extends AbstractNamedAFPObject
        implements Startable, Completable {

    /** the object environment group */
    protected ObjectEnvironmentGroup objectEnvironmentGroup = null;

    /** the object factory */
    protected final Factory factory;

    /** the completion status of this object */
    private boolean complete;

    /** the starting status of this object */
    private boolean started;

    /**
     * Named constructor
     *
     * @param factory the object factory
     * @param name data object name
     */
    public AbstractDataObject(Factory factory, String name) {
        super(name);
        this.factory = factory;
    }

    /**
     * Sets the object view port (area position and size).
     *
     * @param dataObjectInfo
     *            the object area info
     */
    public void setViewport(AFPDataObjectInfo dataObjectInfo) {
        AFPObjectAreaInfo objectAreaInfo = dataObjectInfo.getObjectAreaInfo();

        // object area descriptor
        int width = objectAreaInfo.getWidth();
        int height = objectAreaInfo.getHeight();
        int widthRes = objectAreaInfo.getWidthRes();
        int heightRes = objectAreaInfo.getHeightRes();
        ObjectAreaDescriptor objectAreaDescriptor
            = factory.createObjectAreaDescriptor(width, height, widthRes, heightRes);
        getObjectEnvironmentGroup().setObjectAreaDescriptor(objectAreaDescriptor);

        // object area position
        AFPResourceInfo resourceInfo = dataObjectInfo.getResourceInfo();
        AFPResourceLevel resourceLevel = resourceInfo.getLevel();
        ObjectAreaPosition objectAreaPosition = null;
        int rotation = objectAreaInfo.getRotation();
        if (resourceLevel.isInline()) {
            int x = objectAreaInfo.getX();
            int y = objectAreaInfo.getY();
            objectAreaPosition = factory.createObjectAreaPosition(x, y, rotation);
        } else {
            // positional values are specified in the oaOffset of the include object
            objectAreaPosition = factory.createObjectAreaPosition(0, 0, rotation);
        }
        objectAreaPosition.setReferenceCoordinateSystem(
                ObjectAreaPosition.REFCSYS_PAGE_SEGMENT_RELATIVE);
        getObjectEnvironmentGroup().setObjectAreaPosition(objectAreaPosition);
    }

    /**
     * Gets the ObjectEnvironmentGroup
     *
     * @return the object environment group
     */
    public ObjectEnvironmentGroup getObjectEnvironmentGroup() {
        if (objectEnvironmentGroup == null) {
            this.objectEnvironmentGroup = factory.createObjectEnvironmentGroup();
        }
        return objectEnvironmentGroup;
    }

    /** {@inheritDoc} */
    protected void writeStart(OutputStream os) throws IOException {
        setStarted(true);
    }

    /** {@inheritDoc} */
    protected void writeContent(OutputStream os) throws IOException {
        writeTriplets(os);
        if (objectEnvironmentGroup != null) {
            objectEnvironmentGroup.writeToStream(os);
        }
    }

    /** {@inheritDoc} */
    public void setStarted(boolean started) {
        this.started = started;
    }

    /** {@inheritDoc} */
    public boolean isStarted() {
        return this.started;
    }

    /** {@inheritDoc} */
    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    /** {@inheritDoc} */
    public boolean isComplete() {
        return this.complete;
    }
}
