/*
 * Copyright 1999-2004 The Apache Software Foundation.
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

import org.apache.fop.area.Area;


/**
 * Evaluate and store the cost of breaking an Area at a given point.
 */
public class BreakCost {
    private Area breakArea;
    private BreakPoss bp;

    private int cost; // Will be more complicated than this!

    public BreakCost(Area breakArea, int cost) {
        this.breakArea = breakArea;
        this.cost = cost;
    }

    public BreakCost(BreakPoss bp, int cost) {
        this.bp = bp;
        this.cost = cost;
    }

    BreakPoss getBP() {
        return this.bp;
    }

    Area getArea() {
        return this.breakArea;
    }

    int getCost() {
        return this.cost;
    }

    public BreakCost chooseLowest(BreakCost otherCost) {
        return this;
    }
}
