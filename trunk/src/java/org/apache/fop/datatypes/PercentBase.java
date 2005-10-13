/*
 * Copyright 1999-2005 The Apache Software Foundation.
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

import org.apache.fop.fo.expr.PropertyException;

/**
 * Interface for objects that can be used as base objects for percentage
 * computations
 */
public interface PercentBase {
    
    /**
     * Determines whether a numeric property is created or one with a percentage
     * base.
     * @return 0 for length, 1 for percentage
     */
    int getDimension();
    
    /**
     * @return the base value (this will be used as the base to which a percentage will be 
     *         applied to compute the length of the referencing item)
     */
    double getBaseValue();

    /**
     * @param context The context for percentage evaluation
     * @return the integer size in millipoints of the object (this will be used 
     * as the base to which a percentage will be applied to compute the length 
     * of the referencing item)
     * @throws PropertyException if a problem occurs during evaluation of this
     *     value.
     */
    int getBaseLength(PercentBaseContext context) throws PropertyException;
    
}
