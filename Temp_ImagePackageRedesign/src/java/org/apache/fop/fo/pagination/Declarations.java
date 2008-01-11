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

// Java
import java.util.Map;

import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;

/**
 * Declarations formatting object.
 * A declarations formatting object holds a set of color-profiles
 * and optionally additional non-XSL namespace elements.
 * The color-profiles are held in a hashmap for use with color-profile
 * references.
 */
public class Declarations extends FObj {

    private Map colorProfiles = null;

    /**
     * @param parent FONode that is the parent of this object
     */
    public Declarations(FONode parent) {
        super(parent);
        ((Root) parent).setDeclarations(this);
    }

    /**
     * {@inheritDoc}
     */
    public void bind(PropertyList pList) throws FOPException {
        // No properties defined for fo:declarations
    }

    /**
     * {@inheritDoc}
     * XSL 1.0: (color-profile)+ (and non-XSL NS nodes)
     * FOP/XSL 1.1: (color-profile)* (and non-XSL NS nodes)
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName) 
                throws ValidationException {
        if (FO_URI.equals(nsURI)) {
            if (!localName.equals("color-profile")) {   
                invalidChildError(loc, nsURI, localName);
            }
        } // anything outside of XSL namespace is OK.
    }

    /**
     * At the end of this element sort out the children into
     * a hashmap of color profiles and a list of extension attachments.
     */
    protected void endOfNode() throws FOPException {
        if (firstChild != null) {
            for (FONodeIterator iter = getChildNodes(); iter.hasNext();) {
                FONode node = iter.nextNode();
                if (node.getName().equals("fo:color-profile")) {
                    ColorProfile cp = (ColorProfile)node;
                    if (!"".equals(cp.getColorProfileName())) {
                        addColorProfile(cp);
                    } else {
                        log.warn("color-profile-name required for color profile");
                    }
                } else {
                    log.debug("Ignoring element " + node.getName() 
                            + " inside fo:declarations.");
                }
            }
        }
        firstChild = null;
    }

    private void addColorProfile(ColorProfile cp) {
        if (colorProfiles == null) {
            colorProfiles = new java.util.HashMap();
        }
        if (colorProfiles.get(cp.getColorProfileName()) != null) {
            // duplicate names
            log.warn("Duplicate fo:color-profile profile name: "
                    + cp.getColorProfileName());
        }
        colorProfiles.put(cp.getColorProfileName(), cp);
    }

    /**
     * {@inheritDoc}
     */
    public String getLocalName() {
        return "declarations";
    }
    
    /**
     * {@inheritDoc}
     */
    public int getNameId() {
        return FO_DECLARATIONS;
    }
    
    /**
     * Return ColorProfile with given name.
     * 
     * @param cpName Name of ColorProfile, i.e. the value of the color-profile-name attribute of 
     *               the fo:color-profile element
     * @return The org.apache.fop.fo.pagination.ColorProfile object associated with this 
     *         color-profile-name or null
     */
    public ColorProfile getColorProfile(String cpName) {
        ColorProfile profile = null;
        if (this.colorProfiles != null) {
            profile = (ColorProfile)this.colorProfiles.get(cpName);
        }
        return profile;
    }
    
    
}
