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
 * Utility class for working with keeps.
 */
public class KeepUtil {

    /**
     * Converts a keep property into an integer value.
     * <p>
     * Note: The conversion restricts the effectively available integer range by two values.
     * Integer.MIN_VALUE is used to represent the value "auto" and
     * Integer.MAX_VALUE is used to represebt the value "always".
     * @param keep the keep property
     * @return the keep value as an integer
     */
    public static int getKeepStrength(Property keep) {
        if (keep.isAuto()) {
            return BlockLevelLayoutManager.KEEP_AUTO;
        } else if (keep.getEnum() == Constants.EN_ALWAYS) {
            return BlockLevelLayoutManager.KEEP_ALWAYS;
        } else {
            return keep.getNumber().intValue();
        }
    }

    /**
     * Returns the combined block-level keep strength from a keep property.
     * <p>
     * Note: This is a temporary method to be used until it is possible to differentiate between
     * page and column keeps!
     * @param keep the keep property
     * @return the combined keep strength
     */
    public static int getCombinedBlockLevelKeepStrength(KeepProperty keep) {
        return Math.max(
                getKeepStrength(keep.getWithinPage()),
                getKeepStrength(keep.getWithinColumn()));
    }

    /**
     * Indicates whether a keep strength indicates a keep constraint.
     * @param strength the keep strength
     * @return true if the keep is not "auto"
     */
    public static boolean hasKeep(int strength) {
        return strength > BlockLevelLayoutManager.KEEP_AUTO;
    }

    /**
     * Returns the penalty value to be used for a certain keep strength.
     * <ul>
     *   <li>"auto": returns 0</li>
     *   <li>"always": returns KnuthElement.INFINITE</li>
     *   <li>otherwise: returns KnuthElement.INFINITE - 1</li>
     * </ul>
     * @param keepStrength the keep strength
     * @return the penalty value
     */
    public static int getPenaltyForKeep(int keepStrength) {
        if (keepStrength == BlockLevelLayoutManager.KEEP_AUTO) {
            return 0;
        }
        int penalty = KnuthElement.INFINITE;
        if (keepStrength < BlockLevelLayoutManager.KEEP_ALWAYS) {
            penalty--;
        }
        return penalty;
    }

    /**
     * Returns a string representation of a keep strength value.
     * @param keepStrength the keep strength
     * @return the string representation
     */
    public static String keepStrengthToString(int keepStrength) {
        if (keepStrength == BlockLevelLayoutManager.KEEP_AUTO) {
            return "auto";
        } else if (keepStrength == BlockLevelLayoutManager.KEEP_ALWAYS) {
            return "always";
        } else {
            return Integer.toString(keepStrength);
        }
    }

}
