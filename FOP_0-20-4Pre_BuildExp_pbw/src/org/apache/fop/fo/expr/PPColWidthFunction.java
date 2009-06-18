/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.expr;


import org.apache.fop.fo.Property;
import org.apache.fop.fo.LengthProperty;
import org.apache.fop.datatypes.TableColLength;

public class PPColWidthFunction extends FunctionBase {

    public int nbArgs() {
        return 1;
    }

    public Property eval(Property[] args,
                         PropertyInfo pInfo) throws PropertyException {
        Number d = args[0].getNumber();
        if (d == null) {
            throw new PropertyException("Non number operand to proportional-column-width function");
        }
        if (!pInfo.getPropertyList().getElement().equals("table-column")) {
            throw new PropertyException("proportional-column-width function may only be used on table-column FO");
        }
        // Check if table-layout is "fixed"...
        return new LengthProperty(new TableColLength(d.doubleValue()));
    }

}
