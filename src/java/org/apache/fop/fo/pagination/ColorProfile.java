/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;
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

    /**
     * Creates a new color-profile element.
     * @param parent the parent node
     * @see org.apache.fop.fo.FONode#FONode(FONode)
     */
    public ColorProfile(FONode parent) {
        super(parent);
    }

    /** {@inheritDoc} */
    public void bind(PropertyList pList) throws FOPException {
        src = pList.get(PR_SRC).getString();
        colorProfileName = pList.get(PR_COLOR_PROFILE_NAME).getString();
        renderingIntent = pList.get(PR_RENDERING_INTENT).getEnum();
    }

    /**
     * {@inheritDoc}
        XSL 1.0/FOP: EMPTY (no child nodes permitted)
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName) 
                throws ValidationException {
        if (FO_URI.equals(nsURI)) {
            invalidChildError(loc, nsURI, localName);
        }
    }

    /**
     * Return the "color-profile-name" property.
     * @return the color-profile-name property
     */
    public String getColorProfileName() {
        return colorProfileName;
    }

    /** {@inheritDoc} */
    public String getLocalName() {
        return "color-profile";
    }
    
    /** {@inheritDoc} */
    public int getNameId() {
        return FO_COLOR_PROFILE;
    }
    
    /** 
     * Get src attribute
     * 
     * @return Value of color-profile src attribute
     */
    public String getSrc() {
        return this.src;
    }
    
    /**
     * Get rendering-intent attribute
     * 
     * Returned value is one of
     *   Constants.EN_AUTO
     *   Constants.EN_PERCEPTUAL
     *   Constants.EN_RELATIVE_COLOMETRIC
     *   Constants.EN_SATURATION
     *   Constants.EN_ABSOLUTE_COLORMETRIC
     *    
     * @return Rendering intent attribute
     */
    public int getRenderingIntent() {
        return this.renderingIntent;
    }
}
