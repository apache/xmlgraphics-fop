/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.expr;

import org.apache.fop.fo.Property;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.flow.ListItem;

public class BodyStartFunction extends FunctionBase {

    public int nbArgs() {
        return 0;
    }

    public Property eval(Property[] args,
                         PropertyInfo pInfo) throws PropertyException {
        Numeric distance =
            pInfo.getPropertyList().get("provisional-distance-between-starts").getNumeric();

        FONode item = pInfo.getFO();
        while (item != null &&!(item instanceof ListItem)) {
            item = item.getParent();
        }
        if (item == null) {
            throw new PropertyException("body-start() called from outside an fo:list-item");
        }

        Numeric startIndent =
            ((ListItem)item).properties.get("start-indent").getNumeric();

        return new NumericProperty(distance.add(startIndent));
    }

}
