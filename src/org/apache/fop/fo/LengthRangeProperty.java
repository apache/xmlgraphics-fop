/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo;

import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.LengthRange;

public class LengthRangeProperty extends Property {

    public static class Maker extends LengthProperty.Maker {

        protected Maker(String name) {
            super(name);
        }

    }

    private LengthRange lengthRange;

    public LengthRangeProperty(LengthRange lengthRange) {
        this.lengthRange = lengthRange;
    }

    public LengthRange getLengthRange() {
        return this.lengthRange;
    }

    public Object getObject() {
        return this.lengthRange;
    }

}
