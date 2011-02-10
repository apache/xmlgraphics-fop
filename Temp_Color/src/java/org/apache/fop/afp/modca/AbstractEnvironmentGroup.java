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

/**
 * A base class that encapsulates common features of
 * ActiveEnvironmentGroup and ResourceEnvironmentGroup
 */
public abstract class AbstractEnvironmentGroup extends AbstractNamedAFPObject {

    /** the collection of MapDataResource objects */
    protected final List mapDataResources = null;

    /** the collection of MapPageOverlay objects */
    protected List mapPageOverlays = null;

    /**
     * Main constructor
     *
     * @param name the object name
     */
    public AbstractEnvironmentGroup(String name) {
        super(name);
    }

    private List getMapPageOverlays() {
        if (mapPageOverlays == null) {
            mapPageOverlays = new java.util.ArrayList();
        }
        return mapPageOverlays;
    }

    /**
     * Actually creates the MPO object.
     * Also creates the supporting object (an IPO)
     *
     * @param name the name of the overlay to be used
     */
    public void createOverlay(String name) {
        MapPageOverlay mpo = getCurrentMapPageOverlay();
        if (mpo == null) {
            mpo = new MapPageOverlay();
            getMapPageOverlays().add(mpo);
        }

        try {
            mpo.addOverlay(name);
        } catch (MaximumSizeExceededException msee) {
            mpo = new MapPageOverlay();
            getMapPageOverlays().add(mpo);
            try {
                mpo.addOverlay(name);
            } catch (MaximumSizeExceededException ex) {
                // Should never happen (but log just in case)
                LOG.error("createOverlay():: resulted in a MaximumSizeExceededException");
            }
        }
    }

    /**
     * Getter method for the most recent MapPageOverlay added to the
     * Active Environment Group (returns null if no MapPageOverlay exist)
     *
     * @return the most recent Map Coded Font
     */
    private MapPageOverlay getCurrentMapPageOverlay() {
        return (MapPageOverlay)getLastElement(this.mapPageOverlays);
    }

    /**
     * Get last element.
     * @param list of elements
     * @return last element or null if none
     */
    protected Object getLastElement(List list) {
        if (list != null && list.size() > 0) {
            return list.get(list.size() - 1);
        } else {
            return null;
        }
    }

    /** {@inheritDoc} */
    protected void writeContent(OutputStream os) throws IOException {
        super.writeContent(os);
    }
}
