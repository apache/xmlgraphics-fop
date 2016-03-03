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

package org.apache.fop.fo;

import java.util.HashSet;
import java.util.Set;

/**
 * Context class providing information needed while building the FO tree.
 */
public class FOTreeBuilderContext {

    /**
     * The current set of id's in the FO tree.
     * This is used so we know if the FO tree contains duplicates.
     */
    private Set idReferences = new HashSet();

    /**
     * The property list maker.
     */
    protected PropertyListMaker propertyListMaker;

    /**
     * The XMLWhitespaceHandler for this tree
     */
    protected XMLWhiteSpaceHandler whiteSpaceHandler = new XMLWhiteSpaceHandler();

    /**
     * Indicates whether processing descendants of a marker
     */
    private boolean inMarker;

    /**
     * Returns the set of ID references.
     * @return the ID references
     */
    public Set getIDReferences() {
        return idReferences;
    }

    /**
     * Return the propertyListMaker.
     *
     * @return the currently active {@link PropertyListMaker}
     */
    public PropertyListMaker getPropertyListMaker() {
        return propertyListMaker;
    }

    /**
     * Set a new propertyListMaker.
     *
     * @param propertyListMaker the new {@link PropertyListMaker} to use
     */
    public void setPropertyListMaker(PropertyListMaker propertyListMaker) {
        this.propertyListMaker = propertyListMaker;
    }

    /**
     * Return the XMLWhiteSpaceHandler
     * @return the whiteSpaceHandler
     */
    public XMLWhiteSpaceHandler getXMLWhiteSpaceHandler() {
        return whiteSpaceHandler;
    }

    /**
     * Switch to or from marker context
     * (used by FOTreeBuilder when processing
     *  a marker)
     *
     * @param inMarker  true if a marker is being processed;
     *                  false otherwise
     *
     */
    protected void switchMarkerContext(boolean inMarker) {
        this.inMarker = inMarker;
    }

    /**
     * Check whether in marker context
     *
     * @return true if a marker is being processed
     */
    protected boolean inMarker() {
        return this.inMarker;
    }

}
