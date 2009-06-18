/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.datatypes;

import org.apache.fop.fo.Property;
import org.apache.fop.fo.properties.Constants;

/**
 * a space quantity in XSL (space-before, space-after)
 */
public class CondLength implements CompoundDatatype {

    private Property length;
    private Property conditionality;

    // From CompoundDatatype
    public void setComponent(String sCmpnName, Property cmpnValue,
                             boolean bIsDefault) {
        if (sCmpnName.equals("length"))
            length = cmpnValue;
        else if (sCmpnName.equals("conditionality"))
            conditionality = cmpnValue;
    }

    public Property getComponent(String sCmpnName) {
        if (sCmpnName.equals("length"))
            return length;
        else if (sCmpnName.equals("conditionality"))
            return conditionality;
        else
            return null;
    }

    public Property getConditionality() {
        return this.conditionality;
    }

    public Property getLength() {
        return this.length;
    }

    public boolean isDiscard() {
        return this.conditionality.getEnum() == Constants.DISCARD;
    }

    public int mvalue() {
        return this.length.getLength().mvalue();
    }

}
