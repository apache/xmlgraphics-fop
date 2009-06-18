/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo;

import org.apache.fop.datatypes.Keep;

public class KeepProperty extends Property {

    public static class Maker extends Property.Maker {

        protected Maker(String name) {
            super(name);
        }

    }

    private Keep keep;

    public KeepProperty(Keep keep) {
        this.keep = keep;
    }

    public Keep getKeep() {
        return this.keep;
    }

    public Object getObject() {
        return this.keep;
    }

}
