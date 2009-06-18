/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
import java.util.Iterator;
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
     * @see org.apache.fop.fo.FObj#bind(PropertyList)
     */
    public void bind(PropertyList pList) throws FOPException {
        // No properties defined for fo:declarations
    }

    /**
     * @see org.apache.fop.fo.FONode#validateChildNode(Locator, String, String)
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
     * @see org.apache.fop.fo.FONode#endOfNode()
     */
    protected void endOfNode() throws FOPException {
        if (childNodes != null) {
            for (Iterator iter = childNodes.iterator(); iter.hasNext();) {
                FONode node = (FONode)iter.next();
                if (node.getName().equals("fo:color-profile")) {
                    ColorProfile cp = (ColorProfile)node;
                    if (!"".equals(cp.getColorProfileName())) {
                        if (colorProfiles == null) {
                            colorProfiles = new java.util.HashMap();
                        }
                        if (colorProfiles.get(cp.getColorProfileName()) != null) {
                            // duplicate names
                            getLogger().warn("Duplicate fo:color-profile profile name : "
                                    + cp.getColorProfileName());
                        }
                        colorProfiles.put(cp.getColorProfileName(), cp);
                    } else {
                        getLogger().warn("color-profile-name required for color profile");
                    }
                } else {
                    getLogger().debug("Ignoring element " + node.getName() 
                            + " inside fo:declarations.");
                }
            }
        }
        childNodes = null;
    }

    /**
     * @see org.apache.fop.fo.FObj#getName()
     */
    public String getName() {
        return "fo:declarations";
    }
    
    /**
     * @see org.apache.fop.fo.FObj#getNameId()
     */
    public int getNameId() {
        return FO_DECLARATIONS;
    }
}
