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

package org.apache.fop.layoutmgr;

/**
 * This class is the base class for all kinds of elements that are added to element lists. There
 * are basically two kinds of list elements: Knuth elements and unresolved elements like spaces,
 * border and padding elements which are converted to Knuth elements prior to the breaking
 * process.
 */
public abstract class ListElement {

    private Position position;

    /**
     * Main constructor
     * @param position the Position instance needed by the addAreas stage of the LMs.
     */
    public ListElement(Position position) {
        this.position = position;
    }

    /**
     * @return the Position instance for this element.
     */
    public Position getPosition() {
        return this.position;
    }

    /**
     * Change the Position stored in this element.
     * @param position the Position instance
     */
    public void setPosition(Position position) {
        this.position = position;
    }

    /**
     * @return the LayoutManager responsible for this element.
     */
    public LayoutManager getLayoutManager() {
        if (position != null) {
            return position.getLM();
        } else {
            return null;
        }
    }

    /** @return true if this element is a KnuthBox. */
    public boolean isBox() {
        return false;
    }

    /** @return true if this element is a KnuthGlue. */
    public boolean isGlue() {
        return false;
    }

    /** @return true if this element is a KnuthPenalty. */
    public boolean isPenalty() {
        return false;
    }

    /** @return true if the element is a penalty and represents a forced break. */
    public boolean isForcedBreak() {
        return false;
    }

    /** @return true if the element is an unresolved element such as a space or a border. */
    public boolean isUnresolvedElement() {
        return true;
    }

}
