/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.expr;

import org.apache.fop.fo.Property;


/**
 * Return the specified or initial value of the property on this object.
 */
public class FopPropValFunction extends FunctionBase {

    public int nbArgs() {
        return 1;
    }

    public Property eval(Property[] args,
                         PropertyInfo pInfo) throws PropertyException {
        String propName = args[0].getString();
        if (propName == null) {
            throw new PropertyException("Incorrect parameter to _int-property-value function");
        }
        // System.err.println("Get property-value for " + propName);
        return pInfo.getPropertyList().get(propName);
    }

}
