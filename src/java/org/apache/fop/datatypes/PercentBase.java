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

package org.apache.fop.datatypes;

/**
 * Interface for objects that can be used as base objects for percentage
 * computations
 */
public interface PercentBase {
    static Integer TABLE_UNITS = new Integer(1);
    static Integer BLOCK_IPD = new Integer(2);
    static Integer BLOCK_BPD = new Integer(3);
    static Integer REFERENCE_AREA_IPD = new Integer(4);
    static Integer REFERENCE_AREA_BPD = new Integer(5);
    
    int getDimension();
    double getBaseValue();

    /**
     * @return the integer size of the object (this will be used as the base to
     * which a percentage will be applied to compute the length of the
     * referencing item)
     */
    int getBaseLength();
}
