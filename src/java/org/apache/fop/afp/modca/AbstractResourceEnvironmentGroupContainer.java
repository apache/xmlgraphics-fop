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

import org.apache.fop.afp.Factory;


/**
 * An abstract class which encapsulates the common features of
 * Document and PageGroup resource containers
 */
public abstract class AbstractResourceEnvironmentGroupContainer
    extends AbstractResourceGroupContainer {

    /**
     * The resource environment group used to store complex resources
     */
    protected ResourceEnvironmentGroup resourceEnvironmentGroup = null;

    /**
     * Main constructor
     *
     * @param factory the object factory
     * @param name the name of this resource container
     */
    public AbstractResourceEnvironmentGroupContainer(
            Factory factory, String name) {
        super(factory, name);
    }

    /**
     * Adds a page to the resource container.
     *
     * @param page - the Page object
     */
    public void addPage(PageObject page) {
        addObject(page);
    }

    /**
     * Adds a PageGroup to the resource container.
     *
     * @param pageGroup the PageGroup object
     */
    public void addPageGroup(PageGroup pageGroup) {
        addObject(pageGroup);
    }

    /**
     * Creates an InvokeMediaMap on the page.
     *
     * @param name
     *            the name of the media map
     */
    public void createInvokeMediumMap(String name) {
        InvokeMediumMap invokeMediumMap = factory.createInvokeMediumMap(name);
        addObject(invokeMediumMap);
    }

    /** {@inheritDoc} */
    protected void writeContent(OutputStream os) throws IOException {
        super.writeContent(os);
        if (resourceEnvironmentGroup != null) {
            resourceEnvironmentGroup.writeToStream(os);
        }
    }

    /**
     * Returns the resource environment group
     *
     * @return the resource environment group
     */
    protected ResourceEnvironmentGroup getResourceEnvironmentGroup() {
        if (resourceEnvironmentGroup == null) {
            this.resourceEnvironmentGroup = factory.createResourceEnvironmentGroup();
        }
        return this.resourceEnvironmentGroup;
    }
}
