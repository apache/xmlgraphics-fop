/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */
package org.apache.fop.fo;

import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.Space;
import org.apache.fop.datatypes.LengthRange;

public class SpaceProperty extends Property {

    public static class Maker extends LengthRangeProperty.Maker {
        protected Maker(String name) {
            super(name);
        }

    }

    private Space space;

    public SpaceProperty(Space space) {
        this.space = space;
    }

    public Space getSpace() {
        return this.space;
    }

    /* Space extends LengthRange */
    public LengthRange getLengthRange() {
        return this.space;
    }

    public Object getObject() {
        return this.space;
    }

}
