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
 * This appears to be an artificial function, which handles the specified
 * or initial value of the property on this object.
 */
public class FopPropValFunction extends FunctionBase {

    /**
     * @return 1 (the maximum number of arguments)
     */
    public int nbArgs() {
        return 1;
    }

    /**
     *
     * @param args array of arguments, which should either be empty, or the
     * first of which should be an NCName corresponding to a property name
     * @param pInfo PropertyInfo object to be evaluated
     * @return the Property corresponding to the input
     * @throws PropertyException for incorrect parameters
     */
    public Property eval(Property[] args,
                         PropertyInfo pInfo) throws PropertyException {
        String propName = args[0].getString();
        if (propName == null) {
            throw new PropertyException("Incorrect parameter to _int-property-value function");
        }

        int propId = FOPropertyMapping.getPropertyId(propName);
        return pInfo.getPropertyList().get(propId);
    }

}
