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

import org.apache.fop.datatypes.Numeric;
import org.apache.fop.fo.properties.Property;

/**
 * Class for managing the "max" Number Function. See Sec. 5.10.1 in the XSL-FO
 * standard.
 */
public class MaxFunction extends FunctionBase {

    /**
     * @return 2 (the number of arguments required for the max function)
     */
    public int nbArgs() {
        return 2;
    }

    /**
     * Handle "numerics" if no proportional/percent parts
     * @param args array of arguments to be processed
     * @param pInfo PropertyInfo to be processed
     * @return the maximum of the two args elements passed
     * @throws PropertyException for invalid operands
     */
    public Property eval(Property[] args,
                         PropertyInfo pInfo) throws PropertyException {
        Numeric n1 = args[0].getNumeric();
        Numeric n2 = args[1].getNumeric();
        if (n1 == null || n2 == null) {
            throw new PropertyException("Non numeric operands to max function");
        }
        return (Property) NumericOp.max(n1, n2);
    }

}

