/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.expr;

import org.apache.fop.fo.Property;


public class MaxFunction extends FunctionBase {
    public int nbArgs() {
        return 2;
    }

    // Handle "numerics" if no proportional/percent parts!
    public Property eval(Property[] args,
                         PropertyInfo pInfo) throws PropertyException {
        Numeric n1 = args[0].getNumeric();
        Numeric n2 = args[1].getNumeric();
        if (n1 == null || n2 == null)
            throw new PropertyException("Non numeric operands to max function");
        return new NumericProperty(n1.max(n2));
    }

}
