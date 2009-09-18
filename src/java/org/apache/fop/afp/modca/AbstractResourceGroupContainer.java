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
import java.util.Collection;
import java.util.Iterator;

import org.apache.fop.afp.Completable;
import org.apache.fop.afp.Factory;
import org.apache.fop.afp.Streamable;


/**
 * An abstract container of resource objects
 */
public abstract class AbstractResourceGroupContainer extends AbstractPageObject
implements Streamable {

    /** The container started state */
    protected boolean started = false;

    /** the resource group object */
    protected ResourceGroup resourceGroup = null;

    /**
     * Default constructor
     *
     * @param factory the object factory
     */
    public AbstractResourceGroupContainer(Factory factory) {
        super(factory);
    }

    /**
     * Named constructor
     *
     * @param factory the object factory
     * @param name the name of this resource container
     */
    public AbstractResourceGroupContainer(Factory factory, String name) {
        super(factory, name);
    }

    /**
     * Construct a new page object for the specified name argument, the page
     * name should be an 8 character identifier.
     *
     * @param factory
     *            the object factory
     * @param name
     *            the name of the page.
     * @param width
     *            the width of the page.
     * @param height
     *            the height of the page.
     * @param rotation
     *            the rotation of the page.
     * @param widthRes
     *            the width resolution of the page.
     * @param heightRes
     *            the height resolution of the page.
     */
    public AbstractResourceGroupContainer(Factory factory,
            String name, int width, int height, int rotation, int widthRes, int heightRes) {
        super(factory, name, width, height, rotation, widthRes, heightRes);
    }

    /**
     * Return the number of resources in this container
     *
     * @return the number of resources in this container
     */
    protected int getResourceCount() {
        if (resourceGroup != null) {
            return resourceGroup.getResourceCount();
        }
        return 0;
    }

    /**
     * Returns true if this resource group container contains resources
     *
     * @return true if this resource group container contains resources
     */
    protected boolean hasResources() {
        return resourceGroup != null && resourceGroup.getResourceCount() > 0;
    }

    /**
     * Returns the resource group in this resource group container
     *
     * @return the resource group in this resource group container
     */
    public ResourceGroup getResourceGroup() {
        if (resourceGroup == null) {
            resourceGroup = factory.createResourceGroup();
        }
        return resourceGroup;
    }

//    /** {@inheritDoc} */
//    protected void writeContent(OutputStream os) throws IOException {
//        if (resourceGroup != null) {
//            resourceGroup.writeToStream(os);
//        }
//        super.writeContent(os);
//    }

    /** {@inheritDoc} */
    public void writeToStream(OutputStream os) throws IOException {
        if (!started) {
            writeStart(os);
            started = true;
        }

        writeContent(os);

        if (complete) {
            writeEnd(os);
        }
    }

    /** {@inheritDoc} */
    protected void writeObjects(Collection/*<AbstractAFPObject>*/ objects, OutputStream os)
            throws IOException {
        writeObjects(objects, os, false);
    }

    /**
     * Writes a collection of {@link AbstractAFPObject}s to the AFP Datastream.
     *
     * @param objects a list of AFPObjects
     * @param os The stream to write to
     * @param forceWrite true if writing should happen in any case
     * @throws java.io.IOException an I/O exception of some sort has occurred.
     */
    protected void writeObjects(Collection/*<AbstractAFPObject>*/ objects, OutputStream os,
            boolean forceWrite) throws IOException {
        if (objects != null && objects.size() > 0) {
            Iterator it = objects.iterator();
            while (it.hasNext()) {
                AbstractAFPObject ao = (AbstractAFPObject)it.next();
                if (forceWrite || canWrite(ao)) {
                    ao.writeToStream(os);
                    it.remove();
                } else {
                    break;
                }
            }
        }
    }

    /**
     * Returns true if this object can be written
     *
     * @param obj an AFP object
     * @return true if this object can be written
     */
    protected boolean canWrite(AbstractAFPObject obj) {
        if (obj instanceof AbstractPageObject) {
            return ((Completable)obj).isComplete();
        }
        else {
            return this.isComplete();
        }
    }
}
