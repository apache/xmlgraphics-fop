/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo;

// FOP
import org.apache.fop.datatypes.ColorType;

/**
 * The fo:color-profile formatting object.
 * This loads the color profile when needed and resolves a requested color.
 */
public class ColorProfile extends FObj {
    int intent;
    String src;
    String profileName;

    protected ColorProfile(FONode parent) {
        super(parent);
    }

    public void end() {
        src = this.properties.get("src").getString();
        profileName = this.properties.get("color-profile-name").getString();
        intent = this.properties.get("rendering-intent").getEnum();
        this.properties = null;
    }

    /**
     * Get the name of this color profile.
     */
    public String getProfileName() {
        return profileName;
    }

    /**
     * Get the color specified with the color values from the color profile.
     * The default values are used if the profile could not be loaded
     * or the value is not found.
     */
    public ColorType getColor(int[] colorVals, int defR, int defG, int defB) {
        return null;
    }

    /**
     * Load the color profile.
     */
    private void load() {
        
    }
}
