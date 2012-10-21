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

package org.apache.fop.render.print;

/** Enum class for pages mode (all, even, odd pages). */
public final class PagesMode {

    /** the all pages mode */
    public static final PagesMode ALL = new PagesMode("all");
    /** the even pages mode */
    public static final PagesMode EVEN = new PagesMode("even");
    /** the odd pages mode */
    public static final PagesMode ODD = new PagesMode("odd");

    private String name;

    /**
     * Constructor to add a new named item.
     * @param name Name of the item.
     */
    private PagesMode(String name) {
        this.name = name;
    }

    /** @return the name of the enum */
    public String getName() {
        return this.name;
    }

    /**
     * Returns a PagesMode instance by name.
     * @param name the name of the pages mode
     * @return the pages mode
     */
    public static PagesMode byName(String name) {
        if (PagesMode.ALL.getName().equalsIgnoreCase(name)) {
            return PagesMode.ALL;
        } else if (PagesMode.EVEN.getName().equalsIgnoreCase(name)) {
            return PagesMode.EVEN;
        } else if (PagesMode.ODD.getName().equalsIgnoreCase(name)) {
            return PagesMode.ODD;
        } else {
            throw new IllegalArgumentException("Invalid value for PagesMode: " + name);
        }
    }

    /** {@inheritDoc} */
    public String toString() {
        return "PagesMode:" + name;
    }

}
