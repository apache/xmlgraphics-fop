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

package org.apache.fop.afp.goca;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.fop.afp.Completable;
import org.apache.fop.afp.Startable;
import org.apache.fop.afp.StructuredData;
import org.apache.fop.afp.modca.AbstractNamedAFPObject;

/**
 * A base container of prepared structured AFP objects
 */
public abstract class AbstractGraphicsDrawingOrderContainer extends AbstractNamedAFPObject
implements StructuredData, Completable, Startable {

    /** list of objects contained within this container */
    protected List/*<StructuredDataObject>*/ objects
        = new java.util.ArrayList/*<StructuredDataObject>*/();

    /** object is complete */
    private boolean complete = false;

    /** object has started */
    private boolean started = false;

    /**
     * Default constructor
     */
    protected AbstractGraphicsDrawingOrderContainer() {
    }

    /**
     * Named constructor
     *
     * @param name the name of the container
     */
    protected AbstractGraphicsDrawingOrderContainer(String name) {
        super(name);
    }

    /** {@inheritDoc} */
    protected void writeStart(OutputStream os) throws IOException {
        setStarted(true);
    }

    /** {@inheritDoc} */
    protected void writeContent(OutputStream os) throws IOException {
        writeObjects(objects, os);
    }

    /**
     * Adds a given graphics object to this container
     *
     * @param object the structured data object
     */
    public void addObject(StructuredData object) {
        objects.add(object);
    }

    /**
     * Adds all the contents of a given graphics container to this container
     *
     * @param graphicsContainer a graphics container
     */
    public void addAll(AbstractGraphicsDrawingOrderContainer graphicsContainer) {
        Collection/*<StructuredDataObject>*/ objects = graphicsContainer.getObjects();
        objects.addAll(objects);
    }

    /**
     * Returns all the objects in this container
     *
     * @return all the objects in this container
     */
    private Collection getObjects() {
        return this.objects;
    }

    /**
     * Removes the last drawing order from this container and returns it
     *
     * @return the last drawing order from this container or null if empty
     */
    public StructuredData removeLast() {
        int lastIndex = objects.size() - 1;
        StructuredData object = null;
        if (lastIndex > -1) {
            object = (StructuredData)objects.get(lastIndex);
            objects.remove(lastIndex);
        }
        return object;
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
            dataLen += ((StructuredData)it.next()).getDataLength();
        }
        return dataLen;
    }

    /** {@inheritDoc} */
    public void setComplete(boolean complete) {
        Iterator it = objects.iterator();
        while (it.hasNext()) {
            Object object = it.next();
            if (object instanceof Completable) {
                ((Completable)object).setComplete(true);
            }
        }
        this.complete = true;
    }

    /** {@inheritDoc} */
    public boolean isComplete() {
        return this.complete;
    }

    /** {@inheritDoc} */
    public boolean isStarted() {
        return this.started;
    }

    /** {@inheritDoc} */
    public void setStarted(boolean started) {
        this.started = started;
    }
}
