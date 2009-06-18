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
 * The interface for LayoutManagers which generate block areas
 */
public interface BlockLevelLayoutManager extends LayoutManager {

    /** Adjustment class: no adjustment */
    int NO_ADJUSTMENT = -1;
    /** Adjustment class: adjustment for space-before */
    int SPACE_BEFORE_ADJUSTMENT = 0;
    /** Adjustment class: adjustment for space-after */
    int SPACE_AFTER_ADJUSTMENT = 1;
    /** Adjustment class: adjustment for number of lines */
    int LINE_NUMBER_ADJUSTMENT = 2;
    /** Adjustment class: adjustment for line height */
    int LINE_HEIGHT_ADJUSTMENT = 3;

    int negotiateBPDAdjustment(int adj, KnuthElement lastElement);

    void discardSpace(KnuthGlue spaceGlue);

    /**
     * @return true if this element must be kept together
     */
    boolean mustKeepTogether();

    /**
     * @return true if this element must be kept with the previous element.
     */
    boolean mustKeepWithPrevious();

    /**
     * @return true if this element must be kept with the next element.
     */
    boolean mustKeepWithNext();

}
