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

import org.apache.fop.fo.FOPropertyMapping;
import org.apache.fop.fo.properties.Property;

/**
 * Class modelling the from-nearest-specified-value function. See Sec. 5.10.4
 * of the XSL-FO standard.
 */
public class NearestSpecPropFunction extends FunctionBase {

    /**
     * @return 1 (maximum number of arguments for from-nearest-specified-value
     * function)
     */
    public int nbArgs() {
        return 1;
    }

    /**
     *
     * @param args array of arguments for the function
     * @param pInfo PropertyInfo for the function
     * @return Property containing the nearest-specified-value
     * @throws PropertyException for invalid arguments to the function
     */
    public Property eval(Property[] args,
                         PropertyInfo pInfo) throws PropertyException {
        String propName = args[0].getString();
        if (propName == null) {
            throw new PropertyException(
                    "Incorrect parameter to from-nearest-specified-value function");
        }
        // NOTE: special cases for shorthand property
        // Should return COMPUTED VALUE
        int propId = FOPropertyMapping.getPropertyId(propName);
        return pInfo.getPropertyList().getNearestSpecified(propId);
    }

}
