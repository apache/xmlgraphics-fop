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
     * @param x
     *            the x position of the object
     * @param y
     *            the y position of the object
     * @param width
     *            the width of the object
     * @param height
     *            the height of the object
     * @param widthRes 
     *            the resolution width 
     * @param heightRes
     *            the resolution height 
     * @param rotation
     *            the rotation of the object
     */
    public void setViewport(int x, int y, int width, int height,
            int widthRes, int heightRes, int rotation) {
        if (objectEnvironmentGroup == null) {
            objectEnvironmentGroup = new ObjectEnvironmentGroup();
        }
        objectEnvironmentGroup.setObjectArea(x, y, width, height,
                widthRes, heightRes, rotation);
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
            objectEnvironmentGroup.writeDataStream(os);
        }
        super.writeContent(os);
    }
}
