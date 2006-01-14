/*
 * Copyright 1999-2004,2006 The Apache Software Foundation.
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
 * Class modelling the inherited-property-value Property Value function. See
 * Sec. 5.10.4 of the XSL-FO standard.
 */
public class InheritedPropFunction extends FunctionBase {

    /**
     * @return 1 (maximum number of arguments for the inherited-property-value
     * function)
     */
    public int nbArgs() {
        return 1;
    }

    /**
     * @return true (allow padding of arglist with property name)
     */
    public boolean padArgsWithPropertyName() {
        return true;
    }

    /**
     *
     * @param args arguments to be evaluated
     * @param pInfo PropertyInfo object to be evaluated
     * @return Property satisfying the inherited-property-value
     * @throws PropertyException for invalid parameter
     */
    public Property eval(Property[] args,
                         PropertyInfo pInfo) throws PropertyException {
        String propName = args[0].getString();
        if (propName == null) {
            throw new PropertyException("Incorrect parameter to inherited-property-value function");
        }

        int propId = FOPropertyMapping.getPropertyId(propName);
        return pInfo.getPropertyList().getInherited(propId);
    }

}
