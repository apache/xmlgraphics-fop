/*
 * Copyright 2005 The Apache Software Foundation.
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

/* $Id $ */
 
package org.apache.fop.datatypes;

import org.apache.fop.fo.FObj;

/**
 * This interface is used by the layout managers to provide relevant information
 * back to the property percentage resolution logic, that is
 * the percentages based property getValue() functions expect an object implementing
 * this interface as an argument.
 */
public interface PercentBaseContext {

    /**
     * Returns the base length for the given length base.
     * Length base should be one of the constants defined in {@link LengthBase}.
     * @param lengthBase Indicates which type of the base length value is to be returned
     * @param fobj The FO object against which the percentage should be evaluated
     * @return The base length value of the given kind
     */
    public int getBaseLength(int lengthBase, FObj fobj);
    
}
