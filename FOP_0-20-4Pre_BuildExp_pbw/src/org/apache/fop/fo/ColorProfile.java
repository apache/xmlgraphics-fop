/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo;

// FOP
import org.apache.fop.datatypes.ColorType;

import java.awt.color.ICC_Profile;
import java.awt.color.ICC_ColorSpace;
import java.net.URL;
import java.io.IOException;
import java.io.InputStream;

/**
 * The fo:color-profile formatting object.
 * This loads the color profile when needed and resolves a requested color.
 */
public class ColorProfile extends FObj {
    int intent;
    String src;
    String profileName;
    ICC_ColorSpace colorSpace = null;

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
        // float[] rgbvals = colorSpace.toRGB(colorVals);
        // return new ColorType(rgbvals);
        return null;
    }

    /**
     * Load the color profile.
     */
    private void load() {
        try {
            URL url = new URL(src);
            InputStream is = url.openStream();
            ICC_Profile iccProfile = ICC_Profile.getInstance(is);
            colorSpace = new ICC_ColorSpace(iccProfile);
        } catch(IOException ioe) {
            log.error("Could not read Color Profile src", ioe);
        } catch(IllegalArgumentException iae) {
            log.error("Color Profile src not an ICC Profile", iae);
        }
    }
}
