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
 * Abstract base class used by the ImageObject and GraphicsObject which both
 * have define an ObjectEnvironmentGroup
 */
public abstract class AbstractDataObject extends AbstractPreparedObjectContainer {

    /**
     * The object environment group
     */
    protected ObjectEnvironmentGroup objectEnvironmentGroup = null;

    /**
     * Named constructor
     * @param name data object name
     */
    public AbstractDataObject(String name) {
        super(name);
    }
    
    /**
     * Sets the object display area position and size.
     *
     * @param objectAreaInfo
     *            the object area info
     */
    public void setViewport(ObjectAreaInfo objectAreaInfo) {
        getObjectEnvironmentGroup().setObjectArea(objectAreaInfo);
    }
    
    /**
     * Gets the ObjectEnvironmentGroup
     * @return the object environment group
     */
    protected ObjectEnvironmentGroup getObjectEnvironmentGroup() {
        if (objectEnvironmentGroup == null) {
            objectEnvironmentGroup = new ObjectEnvironmentGroup();
        }
        return objectEnvironmentGroup;
    }
    
    /**
     * Sets the ObjectEnvironmentGroup.
     * @param objectEnvironmentGroup The objectEnvironmentGroup to set
     */
    public void setObjectEnvironmentGroup(ObjectEnvironmentGroup objectEnvironmentGroup) {
        this.objectEnvironmentGroup = objectEnvironmentGroup;
    }

    /**
     * {@inheritDoc}
     */
    protected void writeContent(OutputStream os) throws IOException {
        if (objectEnvironmentGroup != null) {
            objectEnvironmentGroup.write(os);
        }
        super.writeContent(os);
    }
}
