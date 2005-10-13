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
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;

/**
 * The fo:color-profile formatting object.
 * This loads the color profile when needed and resolves a requested color.
 */
public class ColorProfile extends FObj {
    // The value of properties relevant for fo:color-profile.
    private String src;
    private String colorProfileName;
    private int renderingIntent;
    // End of property values
    
    private ICC_ColorSpace colorSpace = null;

    /**
     * @see org.apache.fop.fo.FONode#FONode(FONode)
     */
    public ColorProfile(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FObj#bind(PropertyList)
     */
    public void bind(PropertyList pList) throws FOPException {
        src = pList.get(PR_SRC).getString();
        colorProfileName = pList.get(PR_COLOR_PROFILE_NAME).getString();
        renderingIntent = pList.get(PR_RENDERING_INTENT).getEnum();
    }

    /**
     * @see org.apache.fop.fo.FONode#validateChildNode(Locator, String, String)
        XSL 1.0/FOP: EMPTY (no child nodes permitted)
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName) 
        throws ValidationException {
        invalidChildError(loc, nsURI, localName);
    }

    /**
     * Return the "color-profile-name" property.
     */
    public String getColorProfileName() {
        return colorProfileName;
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

    /**
     * @see org.apache.fop.fo.FObj#getName()
     */
    public String getName() {
        return "fo:color-profile";
    }
    
    /**
     * @see org.apache.fop.fo.FObj#getNameId()
     */
    public int getNameId() {
        return FO_COLOR_PROFILE;
    }
}
