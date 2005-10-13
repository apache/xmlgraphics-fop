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

/**
 * Class modelling the from-table-column Property Value function. See Sec.
 * 5.10.4 of the XSL-FO spec.
 */
public class FromTableColumnFunction extends FunctionBase {

    /**
     * @return 1 (maximum argumenst for the from-table-column function)
     */
    public int nbArgs() {
        return 1;
    }

    /**
     *
     * @param args array of arguments, which should either be empty, or the
     * first of which should contain an NCName corresponding to a property name
     * @param pInfo PropertyInfo object to be evaluated
     * @return the Property corresponding to the property name specified, or, if
     * none, for the property for which the expression is being evaluated
     * @throws PropertyException for incorrect arguments, and (for now) in all
     * cases, because this function is not implemented
     */
    public Property eval(Property[] args,
                         PropertyInfo pInfo) throws PropertyException {
        String propName = args[0].getString();
        if (propName == null) {
            throw new PropertyException("Incorrect parameter to from-table-column function");
        }
        throw new PropertyException("from-table-column unimplemented!");
    }

}
