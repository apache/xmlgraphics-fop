/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.datatypes;

import org.apache.fop.fo.Property;

/**
 * a length quantity in XSL which is specified as "auto"
 */
public class AutoLength extends Length {

    public boolean isAuto() {
        return true;
    }

    // Should we do something intelligent here to set the actual size?
    // Would need a reference object!
    //    protected void computeValue() {
    //    }

    public String toString() {
        return "auto";
    }

}
