/*
 * Copyright 2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

/** Enum class for relative sides. */
public final class RelSide {

    /** the before side */
    public static final RelSide BEFORE = new RelSide("before");
    /** the after side */
    public static final RelSide AFTER = new RelSide("after");
    /** the start side */
    public static final RelSide START = new RelSide("start");
    /** the end side */
    public static final RelSide END = new RelSide("end");
    
    private String name;

    /**
     * Constructor to add a new named item.
     * @param name Name of the item.
     */
    private RelSide(String name) {
        this.name = name;
    }

    /** @return the name of the enum */
    public String getName() {
        return this.name;
    }
    
    /** @see java.lang.Object#toString() */
    public String toString() {
        return "RelSide:" + name;
    }
    
}
