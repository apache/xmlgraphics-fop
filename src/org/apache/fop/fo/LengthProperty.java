/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo;

import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.AutoLength;
import org.apache.fop.fo.expr.Numeric;
import org.apache.fop.apps.FOPException;

public class LengthProperty extends Property {

    public static class Maker extends Property.Maker {

        public /* protected */ Maker(String name) {
            super(name);
        }

        /**
         * protected Property checkPropertyKeywords(String value) {
         * if (isAutoLengthAllowed() && value.equals("auto")) {
         * return new LengthProperty(Length.AUTO);
         * }
         * return null;
         * }
         */

        protected boolean isAutoLengthAllowed() {
            return false;
        }

        public Property convertProperty(Property p,
                                        PropertyList propertyList,
                                        FObj fo) throws FOPException {
            if (isAutoLengthAllowed()) {
                String pval = p.getString();
                if (pval != null && pval.equals("auto"))
                    return new LengthProperty(new AutoLength());
            }
            if (p instanceof LengthProperty)
                return p;
            Length val = p.getLength();
            if (val != null)
                return new LengthProperty(val);
            return convertPropertyDatatype(p, propertyList, fo);
        }

    }

    /*
     * public static Property.Maker maker(String prop) {
     * return new Maker(prop);
     * }
     */

    /**
     * This object may be also be a subclass of Length, such
     * as PercentLength, TableColLength.
     */
    private Length length;

    public LengthProperty(Length length) {
        this.length = length;
        // System.err.println("Set LengthProperty: " + length.toString());
    }

    public Numeric getNumeric() {
        return length.asNumeric() ;
    }

    public Length getLength() {
        return this.length;
    }

    public Object getObject() {
        return this.length;
    }

}
