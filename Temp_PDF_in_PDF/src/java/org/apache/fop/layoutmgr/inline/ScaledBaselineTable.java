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

import org.apache.fop.datatypes.Length;

/**
 * The FOP specific incarnation of the XSL-FO scaled baseline table.
 * All baseline tables are scaled to the font size of the font they 
 * apply to. This interface uses a coordinate system with its origin 
 * where the dominant baseline intersects the start edge of the box.
 * All measurements are in mpt.
 */
public interface ScaledBaselineTable {
    
    /**
     * Return the dominant baseline identifer for this alignment context.
     * @return the dominant baseline identifier
     */
    int getDominantBaselineIdentifier();
    
    /**
     * Return the writing mode for this aligment context.
     * @return the writing mode
     */
    int getWritingMode();

    /**
     * Return the offset measured from the dominant
     * baseline for the given baseline identifier.
     * @param baselineIdentifier the baseline identifier
     * @return the baseline offset
     */
    int getBaseline(int baselineIdentifier);
    
    /**
     * Sets the position of the before and after baselines.
     * This is usually only done for line areas. For other
     * areas the position of the before and after baselines
     * are fixed when the table is constructed.
     * @param beforeBaseline the offset of the before-edge baseline from the dominant baseline
     * @param afterBaseline the offset of the after-edge baseline from the dominant baseline
     */
    void setBeforeAndAfterBaselines(int beforeBaseline, int afterBaseline);
    
    /**
     * Return a new baseline table for the given baseline based
     * on the current baseline table.
     * @param baselineIdentifier the baseline identifer
     * @return a new baseline with the new baseline
     */
    ScaledBaselineTable deriveScaledBaselineTable(int baselineIdentifier);

}
