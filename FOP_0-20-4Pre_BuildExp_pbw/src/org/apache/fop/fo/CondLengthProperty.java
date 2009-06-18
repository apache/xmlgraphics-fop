/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo;

import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.CondLength;

public class CondLengthProperty extends Property {

    public static class Maker extends Property.Maker {

        public Maker(String name) {
            super(name);
        }

    }

    private CondLength condLength = null;

    public CondLengthProperty(CondLength condLength) {
        this.condLength = condLength;
    }

    public CondLength getCondLength() {
        return this.condLength;
    }

    /* Question, should we allow this? */
    public Length getLength() {
        return this.condLength.getLength().getLength();
    }

    public Object getObject() {
        return this.condLength;
    }

}
