/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.datatypes;

import org.apache.fop.fo.Property;

/**
 * a space quantity in XSL (space-before, space-after)
 */
public class Space extends LengthRange {

    private Property precedence;
    private Property conditionality;

    // From CompoundDatatype
    public void setComponent(String sCmpnName, Property cmpnValue,
                             boolean bIsDefault) {
        if (sCmpnName.equals("precedence"))
            setPrecedence(cmpnValue, bIsDefault);
        else if (sCmpnName.equals("conditionality"))
            setConditionality(cmpnValue, bIsDefault);
        else
            super.setComponent(sCmpnName, cmpnValue, bIsDefault);
    }

    // From CompoundDatatype
    public Property getComponent(String sCmpnName) {
        if (sCmpnName.equals("precedence"))
            return getPrecedence();
        else if (sCmpnName.equals("conditionality"))
            return getConditionality();
        else
            return super.getComponent(sCmpnName);
    }

    protected void setPrecedence(Property precedence, boolean bIsDefault) {
        this.precedence = precedence;
    }

    protected void setConditionality(Property conditionality,
                                     boolean bIsDefault) {
        this.conditionality = conditionality;
    }

    public Property getPrecedence() {
        return this.precedence;
    }

    public Property getConditionality() {
        return this.conditionality;
    }

}
