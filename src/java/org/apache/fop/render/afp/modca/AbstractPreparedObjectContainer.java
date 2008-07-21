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

package org.apache.fop.render.afp.modca;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

/**
 * A base container of prepared structured AFP objects
 */
public abstract class AbstractPreparedObjectContainer extends AbstractNamedAFPObject
implements PreparedAFPObject {
    
    /** list of objects contained within this container */
    protected List/*<PreparedAFPObject>*/ objects = null;

    /**
     * Default constructor
     */
    protected AbstractPreparedObjectContainer() {
    }

    /**
     * Named constructor
     * 
     * @param name the name of the container
     */
    protected AbstractPreparedObjectContainer(String name) {
        super(name);
    }
    
    /** {@inheritDoc} */
    protected void writeContent(OutputStream os) throws IOException {
        if (objects != null) {
            super.writeObjects(objects, os);
        }
    }

    private List/*<PreparedAFPObject>*/ getObjects() {
        if (objects == null) {
            this.objects = new java.util.ArrayList/*<PreparedAFPObject>*/();
        }
        return this.objects;
    }
    
    /**
     * Adds a given prepared object to this container
     * 
     * @param preparedObject the prepared object
     * @return the drawingOrder if it was added, null otherwise
     */
    public PreparedAFPObject addObject(PreparedAFPObject preparedObject) {
        getObjects().add(preparedObject);
        return preparedObject;
    }
    
    /**
     * Returns the current data length
     * 
     * @return the current data length of this container including
     * all enclosed objects (and their containers)
     */
    public int getDataLength() {
        int dataLen = 0;
        if (objects != null) {
            Iterator it = objects.iterator();
            while (it.hasNext()) {
                Object obj = it.next();
                if (obj instanceof PreparedAFPObject) {
                    dataLen += ((PreparedAFPObject)obj).getDataLength();
                }
            }
        }
        return dataLen;
    } 
}
