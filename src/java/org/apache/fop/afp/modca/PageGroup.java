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
 * A page group is used in the data stream to define a named, logical grouping
 * of sequential pages. Page groups are delimited by begin-end structured fields
 * that carry the name of the page group. Page groups are defined so that the
 * pages that comprise the group can be referenced or processed as a single
 * entity. Page groups are often processed in stand-alone fashion; that is, they
 * are indexed, retrieved, and presented outside the context of the containing
 * document.
 */
public class PageGroup extends AbstractResourceEnvironmentGroupContainer {

    /**
     * Sequence number for TLE's.
     */
    private int tleSequence = 0;

    /**
     * Constructor for the PageGroup.
     *
     * @param factory the resource manager
     * @param name the name of the page group
     * @param tleSequence current start tle sequence number within stream
     */
    public PageGroup(Factory factory, String name, int tleSequence) {
        super(factory, name);
        this.tleSequence = tleSequence;
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
        TagLogicalElement tle = factory.createTagLogicalElement(name, value, tleSequence);
        if (!getTagLogicalElements().contains(tle)) {
            getTagLogicalElements().add(tle);
            tleSequence++;
        }
    }

    /**
     * Method to mark the end of the page group.
     */
    public void endPageGroup() {
        complete = true;
    }

    /** {@inheritDoc} */
    protected void writeContent(OutputStream os) throws IOException {
        writeObjects(tagLogicalElements, os, true);
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

    public int getTleSequence() {
        return tleSequence;
    }
}