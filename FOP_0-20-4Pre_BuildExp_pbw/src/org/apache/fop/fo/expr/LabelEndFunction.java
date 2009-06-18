/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.expr;

import org.apache.fop.datatypes.*;
import org.apache.fop.fo.Property;
import org.apache.fop.fo.LengthProperty;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.flow.ListItem;

public class LabelEndFunction extends FunctionBase {

    public int nbArgs() {
        return 0;
    }

    public Property eval(Property[] args,
                         PropertyInfo pInfo) throws PropertyException {

        Length distance =
            pInfo.getPropertyList().get("provisional-distance-between-starts").getLength();
        Length separation =
            pInfo.getPropertyList().getNearestSpecified("provisional-label-separation").getLength();

        FONode item = pInfo.getFO();
        while (item != null &&!(item instanceof ListItem)) {
            item = item.getParent();
        }
        if (item == null) {
            throw new PropertyException("label-end() called from outside an fo:list-item");
        }
        Length startIndent = ((ListItem)item).properties.get("start-indent").getLength();

        LinearCombinationLength labelEnd = new LinearCombinationLength();

        // Should be CONTAINING_REFAREA but that doesn't work
        LengthBase base = new LengthBase((ListItem)item, pInfo.getPropertyList(),
                                         LengthBase.CONTAINING_BOX);
        PercentLength refWidth = new PercentLength(1.0, base);

        labelEnd.addTerm(1.0, refWidth);
        labelEnd.addTerm(-1.0, distance);
        labelEnd.addTerm(-1.0, startIndent);
        labelEnd.addTerm(1.0, separation);

        return new LengthProperty(labelEnd);
    }

}
