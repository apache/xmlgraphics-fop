/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo;

// FOP
import org.apache.fop.fo.*;
import org.apache.fop.fo.flow.*;
import org.apache.fop.fo.properties.*;
import org.apache.fop.apps.FOPException;

public class ColorProfile extends FObj {

    public static class Maker extends FObj.Maker {
        public FObj make(FObj parent,
                         PropertyList propertyList) throws FOPException {
            return new ColorProfile(parent, propertyList);
        }
    }

    public static FObj.Maker maker() {
        return new ColorProfile.Maker();
    }

    protected ColorProfile(FObj parent,
                           PropertyList propertyList) throws FOPException {
        super(parent, propertyList);
        log.warn("color profile not implemented");
        // this.properties.get("src");
        // this.properties.get("color-profile-name");
        // this.properties.get("rendering-intent");
    }

    public String getName() {
        return "fo:color-profile";
    }
}
