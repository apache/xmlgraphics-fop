/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.expr;

import java.util.Stack;

import org.apache.fop.fo.Property;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.FObj;
import org.apache.fop.datatypes.PercentBase;


/**
 * This class holds context information needed during property expression
 * evaluation.
 * It holds the Maker object for the property, the PropertyList being
 * built, and the FObj parent of the FObj for which the property is being set.
 */
public class PropertyInfo {
    private Property.Maker maker;
    private PropertyList plist;
    private FObj fo;
    private Stack stkFunction;    // Stack of functions being evaluated

    public PropertyInfo(Property.Maker maker, PropertyList plist, FObj fo) {
        this.maker = maker;
        this.plist = plist;
        this.fo = fo;
    }

    /**
     * Return whether this property inherits specified values.
     * Propagates to the Maker.
     * @return true if the property inherits specified values, false if it
     * inherits computed values.
     */
    public boolean inheritsSpecified() {
        return maker.inheritsSpecified();
    }

    /**
     * Return the PercentBase object used to calculate the absolute value from
     * a percent specification.
     * Propagates to the Maker.
     * @return The PercentBase object or null if percentLengthOK()=false.
     */
    public PercentBase getPercentBase() {
        PercentBase pcbase = getFunctionPercentBase();
        return (pcbase != null) ? pcbase : maker.getPercentBase(fo, plist);
    }

    /**
     * Return the current font-size value as base units (milli-points).
     */
    public int currentFontSize() {
        return plist.get("font-size").getLength().mvalue();
    }

    public FObj getFO() {
        return fo;
    }

    public PropertyList getPropertyList() {
        return plist;
    }

    public void pushFunction(Function func) {
        if (stkFunction == null) {
            stkFunction = new Stack();
        }
        stkFunction.push(func);
    }

    public void popFunction() {
        if (stkFunction != null)
            stkFunction.pop();
    }

    private PercentBase getFunctionPercentBase() {
        if (stkFunction != null) {
            Function f = (Function)stkFunction.peek();
            if (f != null) {
                return f.getPercentBase();
            }
        }
        return null;
    }

}
