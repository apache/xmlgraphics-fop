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

import org.apache.fop.render.afp.DataObjectInfo;

/**
 * An abstract container of resource objects
 */
public abstract class AbstractResourceGroupContainer extends AbstractPageObject {
    /**
     * The resource group object
     */
    private ResourceGroup resourceGroup = null;

    /**
     * The data object factory
     */
    private DataObjectFactory dataObjectFactory = new DataObjectFactory();

    /**
     * Default constructor
     */
    public AbstractResourceGroupContainer() {
        super();
    }

    /**
     * Named constructor
     * @param name the name of this resource container
     */
    public AbstractResourceGroupContainer(String name) {
        super(name);
    }

    /**
     * Construct a new page object for the specified name argument, the page
     * name should be an 8 character identifier.
     *
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
    public AbstractResourceGroupContainer(String name, int width, int height,
            int rotation, int widthRes, int heightRes) {
        super(name, width, height, rotation, widthRes, heightRes);
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
     * Creates and returns a new data object
     * @param dataObjectInfo the data object info
     * @return a newly created data object
     */
    public AbstractNamedAFPObject createObject(DataObjectInfo dataObjectInfo) {
        return dataObjectFactory.create(dataObjectInfo);
    }

    /**
     * {@inheritDoc}
     */
    protected void writeContent(OutputStream os) throws IOException {
        if (resourceGroup != null) {
            resourceGroup.write(os);
        }
        super.writeContent(os);
    }
}
