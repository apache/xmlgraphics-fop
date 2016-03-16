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

package org.apache.fop.layoutmgr.inline;

/**
 * This class is used to pass information to the getNextBreakPoss()
 * method concerning hyphenation. A reference to an instance of the
 * class is contained in the LayoutContext object passed to each
 * LayoutManager. It contains information concerning the hyphenation
 * points in a word and the how many of those have previously been
 * processed by a Layout Manager to generate size information.
 */
public class HyphContext {
    private int[] hyphPoints;
    private int currentOffset;
    private int currentIndex;

    /**
     * @param hyphPoints number of hyphenation points
     */
    public HyphContext(int[] hyphPoints) {
        this.hyphPoints = hyphPoints;
    }

    /** @return next hyphenation point */
    public int getNextHyphPoint() {
        for (; currentIndex < hyphPoints.length; currentIndex++) {
            if (hyphPoints[currentIndex] > currentOffset) {
                return (hyphPoints[currentIndex] - currentOffset);
            }
        }
        return -1; // AT END!
    }

    /** @return true if more hyphenation points */
    public boolean hasMoreHyphPoints() {
        for (; currentIndex < hyphPoints.length; currentIndex++) {
            if (hyphPoints[currentIndex] > currentOffset) {
                return true;
            }
        }
        return false;
    }

    /** @param iCharsProcessed amount to extend offset */
    public void updateOffset(int iCharsProcessed) {
        currentOffset += iCharsProcessed;
    }
}
