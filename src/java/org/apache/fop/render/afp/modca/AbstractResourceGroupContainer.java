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


/**
 * An abstract container of resource objects
 */
public abstract class AbstractResourceGroupContainer extends AbstractPageObject {

    /** the resource group object */
    private ResourceGroup resourceGroup = null;

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
    protected ResourceGroup getResourceGroup() {
        if (resourceGroup == null) {
            resourceGroup = factory.createResourceGroup();
        }
        return resourceGroup;
    }

    /** {@inheritDoc} */
    protected void writeContent(OutputStream os) throws IOException {
        if (resourceGroup != null) {
            resourceGroup.writeToStream(os);
        }
        super.writeContent(os);
    }
}
