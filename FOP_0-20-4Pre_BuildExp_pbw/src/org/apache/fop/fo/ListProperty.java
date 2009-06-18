/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo;

import java.util.Vector;

public class ListProperty extends Property {

    public static class Maker extends Property.Maker {

        public Maker(String name) {
            super(name);
        }

        public Property convertProperty(Property p,
                                        PropertyList propertyList, FObj fo) {
            if (p instanceof ListProperty)
                return p;
            else
                return new ListProperty(p);
        }

    }

    protected Vector list;

    public ListProperty(Property prop) {
        list = new Vector();
        list.addElement(prop);
    }

    public void addProperty(Property prop) {
        list.addElement(prop);
    }

    public Vector getList() {
        return list;
    }

    public Object getObject() {
        return list;
    }

}
