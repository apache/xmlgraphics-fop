/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.fo.expr;

import java.util.Stack;

import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.PercentBase;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.properties.PropertyMaker;


/**
 * This class holds context information needed during property expression
 * evaluation.
 * It holds the Maker object for the property, the PropertyList being
 * built, and the FObj parent of the FObj for which the property is being set.
 */
public class PropertyInfo {
    private PropertyMaker maker;
    private PropertyList plist;
    private FObj fo;
    private Stack stkFunction;    // Stack of functions being evaluated

    /**
     * Constructor
     * @param maker Property.Maker object
     * @param plist PropertyList object
     * @param fo FObj
     */
    public PropertyInfo(PropertyMaker maker, PropertyList plist, FObj fo) {
        this.maker = maker;
        this.plist = plist;
        this.fo = fo;
    }

    /**
     * Return the PercentBase object used to calculate the absolute value from
     * a percent specification.
     * Propagates to the Maker.
     * @return The PercentBase object or null if percentLengthOK()=false.
     */
    public PercentBase getPercentBase() throws PropertyException {
        PercentBase pcbase = getFunctionPercentBase();
        return (pcbase != null) ? pcbase : maker.getPercentBase(fo, plist);
    }

    /**
     * @return the current font-size value as base units (milli-points).
     */
    public Length currentFontSize() throws PropertyException {
        return plist.get(Constants.PR_FONT_SIZE).getLength();
    }

    /**
     * accessor for FObj
     * @return FObj
     */
    public FObj getFO() {
        return fo;
    }

    /**
     * accessor for PropertyList
     * @return PropertyList object
     */
    public PropertyList getPropertyList() {
        return plist;
    }

    /**
     * accessor for PropertyMaker
     * @return PropertyMaker object
     */
    public PropertyMaker getPropertyMaker() {
        return maker;
    }
    
    /**
     * push a function onto the function stack
     * @param func function to push onto stack
     */
    public void pushFunction(Function func) {
        if (stkFunction == null) {
            stkFunction = new Stack();
        }
        stkFunction.push(func);
    }

    /**
     * pop a function off of the function stack
     */
    public void popFunction() {
        if (stkFunction != null) {
            stkFunction.pop();
        }
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
