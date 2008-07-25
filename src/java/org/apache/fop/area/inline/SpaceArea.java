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
package org.apache.fop.area.inline;

/**
 * A space
 */
public class SpaceArea extends InlineArea {

    /**
     * The space for this space area
     */
    protected String space;

    /**
     * Is this space adjustable?
     */
    protected boolean isAdjustable;

    /**
     * Create a space area
     * @param s the space character
     * @param o the offset for the next area
     * @param a is this space adjustable?
     */
    public SpaceArea(char s, int o, boolean a) {
        space = new String() + s;
        offset = o;
        isAdjustable = a;
    }

    /**
     * @return Returns the space.
     */
    public String getSpace() {
        return new String(space);
    }

    /** @return true if the space is adjustable (WRT word-space processing) */
    public boolean isAdjustable() {
        return this.isAdjustable;
    }
}
