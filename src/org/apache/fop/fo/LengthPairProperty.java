/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo;

import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.LengthPair;

public class LengthPairProperty extends Property {

    public static class Maker extends LengthProperty.Maker {

        protected Maker(String name) {
            super(name);
        }

    }

    private LengthPair lengthPair;

    public LengthPairProperty(LengthPair lengthPair) {
        this.lengthPair = lengthPair;
    }

    public LengthPair getLengthPair() {
        return this.lengthPair;
    }

    public Object getObject() {
        return this.lengthPair;
    }

}
