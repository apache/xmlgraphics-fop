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

package org.apache.fop.afp.goca;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

import org.apache.fop.afp.modca.AbstractNamedAFPObject;
import org.apache.fop.afp.modca.StructuredDataObject;

/**
 * A base container of prepared structured AFP objects
 */
public abstract class AbstractGraphicsObjectContainer extends AbstractNamedAFPObject
implements StructuredDataObject {

    /** list of objects contained within this container */
    protected List/*<StructuredDataObject>*/ objects
        = new java.util.ArrayList/*<StructuredDataObject>*/();

    /**
     * Default constructor
     */
    protected AbstractGraphicsObjectContainer() {
    }

    /**
     * Named constructor
     *
     * @param name the name of the container
     */
    protected AbstractGraphicsObjectContainer(String name) {
        super(name);
    }

    /** {@inheritDoc} */
    protected void writeContent(OutputStream os) throws IOException {
        writeObjects(objects, os);
    }

    /**
     * Adds a given graphics object to this container
     *
     * @param drawingOrder the graphics object
     */
    public void addObject(StructuredDataObject drawingOrder) {
        objects.add(drawingOrder);
    }

    /**
     * Returns the current data length
     *
     * @return the current data length of this container including
     * all enclosed objects (and their containers)
     */
    public int getDataLength() {
        int dataLen = 0;
        Iterator it = objects.iterator();
        while (it.hasNext()) {
            dataLen += ((StructuredDataObject)it.next()).getDataLength();
        }
        return dataLen;
    }
}
