/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.expr;

import org.apache.fop.fo.Property;

public class AbsFunction extends FunctionBase {

    public int nbArgs() {
        return 1;
    }

    public Property eval(Property[] args,
                         PropertyInfo propInfo) throws PropertyException {
        Numeric num = args[0].getNumeric();
        if (num == null)
            throw new PropertyException("Non numeric operand to abs function");
            // What if has relative composants (percent, table-col units)?
        return new NumericProperty(num.abs());
    }

}

