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

import org.apache.fop.render.afp.AFPDataObjectInfo;
import org.apache.fop.render.afp.AFPObjectAreaInfo;

/**
 * Abstract base class used by the ImageObject and GraphicsObject which both
 * have define an ObjectEnvironmentGroup
 */
public abstract class AbstractDataObject extends AbstractNamedAFPObject {

    /** the object environment group */
    protected ObjectEnvironmentGroup objectEnvironmentGroup = null;

    /** the object factory */
    protected final Factory factory;

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
        getObjectEnvironmentGroup().setObjectArea(objectAreaInfo);
    }

    /**
     * Gets the ObjectEnvironmentGroup
     *
     * @return the object environment group
     */
    protected ObjectEnvironmentGroup getObjectEnvironmentGroup() {
        if (objectEnvironmentGroup == null) {
            this.objectEnvironmentGroup = factory.createObjectEnvironmentGroup();
        }
        return objectEnvironmentGroup;
    }

    /** {@inheritDoc} */
    protected void writeContent(OutputStream os) throws IOException {
        super.writeContent(os);
        if (objectEnvironmentGroup != null) {
            objectEnvironmentGroup.writeToStream(os);
        }
    }
}
