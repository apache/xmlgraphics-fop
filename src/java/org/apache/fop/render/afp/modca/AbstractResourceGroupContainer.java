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
import java.util.Set;

import org.apache.fop.render.afp.DataObjectParameters;
import org.apache.fop.render.afp.ImageObjectParameters;

/**
 * An abstract container of resource objects
 */
public abstract class AbstractResourceGroupContainer extends AbstractNamedAFPObject {
    /**
     * The resource group object
     */
    private ResourceGroup resourceGroup = null;
    
    /**
     * The list of objects within this resource container
     */
    protected Set/*<AbstractStructuredAFPObject>*/ objects = null;

    /**
     * Unnamed constructor
     */
    public AbstractResourceGroupContainer() {
    }

    /**
     * Named constructor
     * @param name the name of this resource container
     */
    public AbstractResourceGroupContainer(String name) {
        super(name);
    }

    /**
     * @return the number of resources in this container
     */
    protected int getResourceCount() {
        if (resourceGroup != null) {
            return resourceGroup.getResourceCount();
        }
        return 0;
    }
    
    /**
     * Adds an AFP object to the resource group in this container
     * @param obj an AFP object
     */
    protected void addObject(AbstractAFPObject obj) {
        if (objects == null) {
            this.objects = new java.util.LinkedHashSet/*<AbstractAFPObject>*/();
        }
        objects.add(obj);
    }

    /**
     * @return true if this resource group container contains resources
     */
    protected boolean hasResources() {
        return resourceGroup != null && resourceGroup.getResourceCount() > 0;
    }
    
    /**
     * @return the resource group in this resource group container
     */
    protected ResourceGroup getResourceGroup() {
        if (resourceGroup == null) {
            resourceGroup = new ResourceGroup();
        }
        return resourceGroup;
    }
    
    /**
     * Helper method to create an image on the current container and to return
     * the object.
     * @param params the image object parameters
     * @return the image object
     */
    public IncludeObject createImageObject(ImageObjectParameters params) {
        return getResourceGroup().addObject(params);
    }
   
    /**
     * Helper method to create a graphic in the current container and to return
     * the object.
     * @param params the data object parameters
     * @return the graphics object
     */
    public IncludeObject createGraphicsObject(DataObjectParameters params) {
        return getResourceGroup().addObject(params);
    }
    
    /**
     * {@inheritDoc}
     */
    protected void writeContent(OutputStream os) throws IOException {
        super.writeContent(os);
        if (resourceGroup != null) {
            resourceGroup.writeDataStream(os);
        }
    }
}
