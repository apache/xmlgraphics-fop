/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layoutmgr;

import org.apache.fop.area.Area;
import org.apache.fop.area.MinOptMax;
import org.apache.fop.datatypes.Space;


/**
 * Accumulate a sequence of space-specifiers (XSL space type) on
 * areas with a stacking constraint. Provide a way to resolve these into
 * a single MinOptMax value.
 */
public class SpaceSpecifier {

    /**
     * Combine passed space property value with any existing space.
     */
    public void addSpace(Space moreSpace) {
    }

    public MinOptMax resolve() {
        return new MinOptMax();
    }
}
