/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.fo.pagination;

// Java
import java.awt.color.ICC_Profile;
import java.awt.color.ICC_ColorSpace;
import java.net.URL;
import java.io.IOException;
import java.io.InputStream;

// FOP
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.FOTreeVisitor;

/**
 * The fo:color-profile formatting object.
 * This loads the color profile when needed and resolves a requested color.
 */
public class ColorProfile extends FObj {
    private int intent;
    private String src;
    private String profileName;
    private ICC_ColorSpace colorSpace = null;

    /**
     * @see org.apache.fop.fo.FONode#FONode(FONode)
     */
    public ColorProfile(FONode parent) {
        super(parent);
    }

    /**
     * Special processing for the end of parsing an ColorProfile object.
     * Extract instance variables from the collection of properties for this
     * object.
     */
    public void end() {
        src = this.propertyList.get(PR_SRC).getString();
        profileName = this.propertyList.get(PR_COLOR_PROFILE_NAME).getString();
        intent = this.propertyList.get(PR_RENDERING_INTENT).getEnum();
        this.propertyList = null;
    }

    /**
     * @return the name of this color profile.
     */
    public String getProfileName() {
        return profileName;
    }

    /**
     * Get the color specified with the color values from the color profile.
     * The default values are used if the profile could not be loaded
     * or the value is not found.
     * @param colorVals integer array containing the color profile?
     * @param defR integer value for red channel (0-255)?
     * @param defG integer value for green channel (0-255)?
     * @param defB integer value for blue channel (0-255)?
     * @return the ColorType object corresponding to the input
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
        } catch (IOException ioe) {
            getLogger().error("Could not read Color Profile src", ioe);
        } catch (IllegalArgumentException iae) {
            getLogger().error("Color Profile src not an ICC Profile", iae);
        }
    }

    public void acceptVisitor(FOTreeVisitor fotv) {
        fotv.serveColorProfile(this);
    }

}
