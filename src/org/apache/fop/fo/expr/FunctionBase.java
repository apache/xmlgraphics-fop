/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.expr;

import org.apache.fop.fo.Property;
import org.apache.fop.datatypes.PercentBase;

public abstract class FunctionBase implements Function {
    // abstract int nbArgs() ;

    /**
     * By default, functions have no percent-based arguments.
     */
    public PercentBase getPercentBase() {
        return null;
    }

    /*
     * abstract Property eval(Property[] args, PropertyInfo propInfo)
     * throws PropertyException;
     */
}

