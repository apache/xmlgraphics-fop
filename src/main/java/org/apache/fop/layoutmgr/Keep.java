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

import org.apache.fop.fo.Constants;
import org.apache.fop.fo.properties.KeepProperty;
import org.apache.fop.fo.properties.Property;

/**
 * Object representing a keep constraint, corresponding
 * to the XSL-FO <a href="http://www.w3.org/TR/xsl/#d0e26492">keep properties</a>.
 */
public final class Keep {

    /** The integer value for "auto" keep strength. */
    private static final int STRENGTH_AUTO = Integer.MIN_VALUE;

    /** The integer value for "always" keep strength. */
    private static final int STRENGTH_ALWAYS = Integer.MAX_VALUE;

    /** keep auto */
    public static final Keep KEEP_AUTO = new Keep(STRENGTH_AUTO, Constants.EN_AUTO);

    /** keep always */
    public static final Keep KEEP_ALWAYS = new Keep(STRENGTH_ALWAYS, Constants.EN_LINE);

    private int strength;

    private int context;

    private Keep(int strength, int context) {
        this.strength = strength;
        this.context = context;
    }

    private static int getKeepStrength(Property keep) {
        if (keep.isAuto()) {
            return STRENGTH_AUTO;
        } else if (keep.getEnum() == Constants.EN_ALWAYS) {
            return STRENGTH_ALWAYS;
        } else {
            return keep.getNumber().intValue();
        }
    }

    /**
     * Obtain a Keep instance corresponding to the given {@link KeepProperty}
     *
     * @param keepProperty  the {@link KeepProperty}
     * @return  a new instance corresponding to the given property
     */
    public static Keep getKeep(KeepProperty keepProperty) {
        Keep keep = new Keep(STRENGTH_AUTO, Constants.EN_AUTO);
        keep.update(keepProperty.getWithinPage(),   Constants.EN_PAGE);
        keep.update(keepProperty.getWithinColumn(), Constants.EN_COLUMN);
        keep.update(keepProperty.getWithinLine(),   Constants.EN_LINE);
        return keep;
    }

    private void update(Property keep, int context) {
        if (!keep.isAuto()) {
            this.strength = getKeepStrength(keep);
            this.context = context;
        }
    }

    /** @return {@code true} if the keep property was specified as "auto" */
    public boolean isAuto() {
        return strength == STRENGTH_AUTO;
    }

    /**
     * Returns the context of this keep.
     *
     * @return one of {@link Constants#EN_LINE}, {@link Constants#EN_COLUMN} or
     * {@link Constants#EN_PAGE}
     */
    public int getContext() {
        return context;
    }

    /** @return the penalty value corresponding to the strength of this Keep */
    public int getPenalty() {
        if (strength == STRENGTH_AUTO) {
            return 0;
        } else if (strength == STRENGTH_ALWAYS) {
            return KnuthElement.INFINITE;
        } else {
            return KnuthElement.INFINITE - 1;
        }
    }

    private static int getKeepContextPriority(int context) {
        switch (context) {
        case Constants.EN_LINE:   return 0;
        case Constants.EN_COLUMN: return 1;
        case Constants.EN_PAGE:   return 2;
        case Constants.EN_AUTO:   return 3;
        default: throw new IllegalArgumentException();
        }
    }

    /**
     * Compare this Keep instance to another one, and return the
     * stronger one if the context is the same
     *
     * @param other     the instance to compare to
     * @return  the winning Keep instance
     */
    public Keep compare(Keep other) {

        /* check strength "always" first, regardless of priority */
        if (this.strength == STRENGTH_ALWAYS
                && this.strength > other.strength) {
            return this;
        } else if (other.strength == STRENGTH_ALWAYS
                && other.strength > this.strength) {
            return other;
        }

        int pThis = getKeepContextPriority(this.context);
        int pOther = getKeepContextPriority(other.context);

        /* equal priority: strongest wins */
        if (pThis == pOther) {
            return (strength >= other.strength) ? this : other;
        }

        /* different priority: lowest priority wins */
        return (pThis < pOther) ? this : other;
    }

    /** {@inheritDoc} */
    public String toString() {
        return (strength == STRENGTH_AUTO) ? "auto"
                : (strength == STRENGTH_ALWAYS) ? "always"
                        : Integer.toString(strength);
    }
}
