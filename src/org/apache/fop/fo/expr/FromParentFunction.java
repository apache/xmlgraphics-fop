/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.expr;

import org.apache.fop.fo.Property;


public class FromParentFunction extends FunctionBase {

    public int nbArgs() {
        return 1;
    }

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
        return pInfo.getPropertyList().getFromParent(propName);
    }

}
