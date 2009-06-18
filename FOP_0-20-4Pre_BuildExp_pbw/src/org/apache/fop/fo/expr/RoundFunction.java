/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.expr;


import org.apache.fop.fo.Property;
import org.apache.fop.fo.NumberProperty;

class RoundFunction extends FunctionBase {
    public int nbArgs() {
        return 1;
    }

    public Property eval(Property[] args,
                         PropertyInfo pInfo) throws PropertyException {
        Number dbl = args[0].getNumber();
        if (dbl == null)
            throw new PropertyException("Non number operand to round function");
        double n = dbl.doubleValue();
        double r = Math.floor(n + 0.5);
        if (r == 0.0 && n < 0.0)
            r = -r;    // round(-0.2) returns -0 not 0
        return new NumberProperty(r);
    }

}
