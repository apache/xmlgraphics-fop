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

package org.apache.fop.fo.expr;

import org.apache.fop.fo.properties.Property;
import org.apache.fop.datatypes.PercentBase;

/**
 * Interface for managing XSL-FO Functions
 */
public interface Function {

    /**
     * @return the number of arguments that must be passed to this function. For
     * example, if the function should determine the minimum of two numbers, it
     * must be passed two arguments, one for each of the two values.
     */
    int nbArgs();

    /**
     * @return the basis for percentage calculations
     */
    PercentBase getPercentBase();

    /**
     * Evaluate the function
     * @param args an array of Properties that should be evaluated
     * @param propInfo the PropertyInfo
     * @return the Property satisfying the function
     * @throws PropertyException for problems when evaluating the function
     */
    Property eval(Property[] args,
                  PropertyInfo propInfo) throws PropertyException;
}

