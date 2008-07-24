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
import java.util.List;

import org.apache.fop.render.afp.modca.resource.ResourceManager;

/**
 * A page group is used in the data stream to define a named, logical grouping
 * of sequential pages. Page groups are delimited by begin-end structured fields
 * that carry the name of the page group. Page groups are defined so that the
 * pages that comprise the group can be referenced or processed as a single
 * entity. Page groups are often processed in stand-alone fashion; that is, they
 * are indexed, retrieved, and presented outside the context of the containing
 * document.
 */
public class PageGroup extends AbstractResourceEnvironmentGroupContainer {

    /** The tag logical elements contained within this group */
    private List tagLogicalElements = null;

    /** The page state */
    private boolean complete = false;

    /**
     * Constructor for the PageGroup.
     * 
     * @param manager the resource manager
     * @param name the name of the page group
     */
    public PageGroup(ResourceManager manager, String name) {
        super(manager, name);
    }

    private List getTagLogicalElements() {
        if (tagLogicalElements == null) {
            this.tagLogicalElements = new java.util.ArrayList();
        }
        return this.tagLogicalElements;
    }
    
    /**
     * Creates a TagLogicalElement on the page.
     *
     * @param name
     *            the name of the tag
     * @param value
     *            the value of the tag
     */
    public void createTagLogicalElement(String name, String value) {
        TagLogicalElement tle = new TagLogicalElement(name, value);
        if (!getTagLogicalElements().contains(tle)) {
            getTagLogicalElements().add(tle);
        }
    }

    /**
     * Method to mark the end of the page group.
     */
    protected void endPageGroup() {
        complete = true;
    }

    /**
     * Returns an indication if the page group is complete
     * @return whether or not this page group is complete or not
     */
    public boolean isComplete() {
        return complete;
    }

    /** {@inheritDoc} */
    protected void writeContent(OutputStream os) throws IOException {
        writeObjects(tagLogicalElements, os);
        super.writeContent(os);
    }

    /** {@inheritDoc} */
    protected void writeStart(OutputStream os) throws IOException {
        byte[] data = new byte[17];
        copySF(data, Type.BEGIN, Category.PAGE_GROUP);
        os.write(data);
    }

    /** {@inheritDoc} */
    protected void writeEnd(OutputStream os) throws IOException {
        byte[] data = new byte[17];
        copySF(data, Type.END, Category.PAGE_GROUP);
        os.write(data);
    }
    
    /** {@inheritDoc} */
    public String toString() {
        return this.getName();
    }
}