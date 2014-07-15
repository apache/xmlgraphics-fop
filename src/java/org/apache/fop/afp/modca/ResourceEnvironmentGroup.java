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
import java.util.List;

import org.apache.fop.afp.Completable;

/**
 * A Resource Environment Group contains a set of resources for a document
 * or for a group of pages in a document.
 */
public class ResourceEnvironmentGroup extends AbstractEnvironmentGroup implements Completable {

    /** default name for the resource group */
    private static final String DEFAULT_NAME = "REG00001";

    /** the pre-process presentation objects contained in this resource environment group */
    private List/*<PreprocessPresentationObject>*/ preProcessPresentationObjects;

    /** the resource environment group state */
    private boolean complete;

    /**
     * Default constructor
     */
    public ResourceEnvironmentGroup() {
        this(DEFAULT_NAME);
    }

    private List/*<PreprocessPresentationObject>*/ getPreprocessPresentationObjects() {
        if (preProcessPresentationObjects == null) {
            this.preProcessPresentationObjects
                = new java.util.ArrayList/*<PreprocessPresentationObject>*/();
        }
        return this.preProcessPresentationObjects;
    }

    /**
     * Constructor for the ResourceEnvironmentGroup, this takes a
     * name parameter which must be 8 characters long.
     * @param name the resource environment group name
     */
    public ResourceEnvironmentGroup(String name) {
        super(name);
    }

//    /**
//     * Adds an AFP object mapping reference to this resource environment group
//     * @param obj the object to add
//     */
//    public void addObject(AbstractStructuredAFPObject obj) {
//        getMapDataResources().add(new MapDataResource(obj));
//        createOverlay(obj.get);
//        getPreprocessPresentationObjects().add(new PreprocessPresentationObject(obj));
//    }

    /** {@inheritDoc} */
    protected void writeStart(OutputStream os) throws IOException {
        byte[] data = new byte[17];
        copySF(data, Type.BEGIN, Category.RESOURCE_ENVIROMENT_GROUP);
        os.write(data);
    }

    /** {@inheritDoc} */
    protected void writeEnd(OutputStream os) throws IOException {
        byte[] data = new byte[17];
        copySF(data, Type.END, Category.RESOURCE_ENVIROMENT_GROUP);
        os.write(data);
    }

    /** {@inheritDoc} */
    protected void writeContent(OutputStream os) throws IOException {
        writeObjects(mapDataResources, os);
        writeObjects(mapPageOverlays, os);
        writeObjects(preProcessPresentationObjects, os);
    }

    /** {@inheritDoc} */
    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    /** {@inheritDoc} */
    public boolean isComplete() {
        return complete;
    }

}
