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
 * @see KnuthGlue
 * @see "http://www.leverkruid.eu/GKPLinebreaking/elements.html"
 */
public final class Adjustment {

    /**
     * Adjustment class: no adjustment.
     */
    public static final Adjustment NO_ADJUSTMENT = new Adjustment("none");

    /**
     * Adjustment class: adjustment for space-before.
     */
    public static final Adjustment SPACE_BEFORE_ADJUSTMENT = new Adjustment("space-before");

    /**
     * Adjustment class: adjustment for space-after.
     */
    public static final Adjustment SPACE_AFTER_ADJUSTMENT = new Adjustment("space-after");

    /**
     * Adjustment class: adjustment for number of lines.
     */
    public static final Adjustment LINE_NUMBER_ADJUSTMENT = new Adjustment("line-number");

    /**
     * Adjustment class: adjustment for line height.
     */
    public static final Adjustment LINE_HEIGHT_ADJUSTMENT = new Adjustment("line-height");

    private final String name;

    private Adjustment(String name) {
        this.name = name;
    }

    /** {@inheritDoc} */
    public boolean equals(Object obj) {
        return this == obj;
    }

    /** {@inheritDoc} */
    public int hashCode() {
        return super.hashCode();
    }

    /** {@inheritDoc} */
    public String toString() {
        return name;
    }
}
