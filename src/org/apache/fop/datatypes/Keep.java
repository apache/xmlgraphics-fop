/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.datatypes;

import org.apache.fop.fo.Property;

/**
 * XSL FO Keep Property datatype (keep-together, etc)
 */
public class Keep implements CompoundDatatype {
    private Property withinLine;
    private Property withinColumn;
    private Property withinPage;

    public Keep() {}

    // From CompoundDatatype
    public void setComponent(String sCmpnName, Property cmpnValue,
                             boolean bIsDefault) {
        if (sCmpnName.equals("within-line"))
            setWithinLine(cmpnValue, bIsDefault);
        else if (sCmpnName.equals("within-column"))
            setWithinColumn(cmpnValue, bIsDefault);
        else if (sCmpnName.equals("within-page"))
            setWithinPage(cmpnValue, bIsDefault);
    }

    // From CompoundDatatype
    public Property getComponent(String sCmpnName) {
        if (sCmpnName.equals("within-line"))
            return getWithinLine();
        else if (sCmpnName.equals("within-column"))
            return getWithinColumn();
        else if (sCmpnName.equals("within-page"))
            return getWithinPage();
        else
            return null;
    }

    public void setWithinLine(Property withinLine, boolean bIsDefault) {
        this.withinLine = withinLine;
    }

    protected void setWithinColumn(Property withinColumn,
                                   boolean bIsDefault) {
        this.withinColumn = withinColumn;
    }

    public void setWithinPage(Property withinPage, boolean bIsDefault) {
        this.withinPage = withinPage;
    }

    public Property getWithinLine() {
        return this.withinLine;
    }

    public Property getWithinColumn() {
        return this.withinColumn;
    }

    public Property getWithinPage() {
        return this.withinPage;
    }

    /**
     * What to do here?? There isn't really a meaningful single value.
     */
    public String toString() {
        return "Keep";
    }

}
