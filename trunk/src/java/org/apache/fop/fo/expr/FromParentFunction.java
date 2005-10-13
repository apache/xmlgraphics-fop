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
 * Class modelling the from-parent Property Value function. See Sec. 5.10.4 of
 * the XSL-FO spec.
 */
public class FromParentFunction extends FunctionBase {

    /**
     * @return 1 (maximum arguments for the from-parent function)
     */
    public int nbArgs() {
        return 1;
    }

    /**
     * @param args array of arguments, which should either be empty, or the
     * first of which should contain an NCName corresponding to property name
     * @param pInfo PropertyInfo object to be evaluated
     * @return property containing the computed value
     * @throws PropertyException if the arguments are incorrect
     */
    public Property eval(Property[] args,
                         PropertyInfo pInfo) throws PropertyException {
        String propName = args[0].getString();
        if (propName == null) {
            throw new PropertyException("Incorrect parameter to from-parent function");
        }
        // NOTE: special cases for shorthand property
        // Should return COMPUTED VALUE
        /*
         * For now, this is the same as inherited-property-value(propName)
         * (The only difference I can see is that this could work for
         * non-inherited properties too. Perhaps the result is different for
         * a property line line-height which "inherits specified"???
         */
        return pInfo.getPropertyList().getFromParent(FOPropertyMapping.getPropertyId(propName));
    }

}
