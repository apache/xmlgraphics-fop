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
import java.util.List;
import java.util.Map;
import java.util.Iterator;

// FOP
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.FOTreeVisitor;
import org.apache.fop.fo.XMLObj;


/**
 * Declarations formatting object.
 * A declarations formatting object holds a set of color-profiles
 * and optionally additional non-XSL namespace elements.
 * The color-profiles are held in a hashmap for use with color-profile
 * references.
 */
public class Declarations extends FObj {

    private Map colorProfiles = null;
    private List external = null;

    /**
     * @param parent FONode that is the parent of this object
     */
    public Declarations(FONode parent) {
        super(parent);
    }

    /**
     * At then end of this element sort out the child into
     * a hashmap of color profiles and a list of external xml.
     */
    public void end() {
        for (Iterator iter = children.iterator(); iter.hasNext();) {
            FONode node = (FONode)iter.next();
            if (node.getName().equals("fo:color-profile")) {
                ColorProfile cp = (ColorProfile)node;
                if (!"".equals(cp.getProfileName())) {
                    if (colorProfiles == null) {
                        colorProfiles = new java.util.HashMap();
                    }
                    if (colorProfiles.get(cp.getProfileName()) != null) {
                        // duplicate names
                        getLogger().warn("Duplicate fo:color-profile profile name : "
                                + cp.getProfileName());
                    }
                    colorProfiles.put(cp.getProfileName(), cp);
                } else {
                    getLogger().warn("color-profile-name required for color profile");
                }
            } else if (node instanceof XMLObj) {
                if (external == null) {
                    external = new java.util.ArrayList();
                }
                external.add(node);
            } else {
                getLogger().warn("invalid element " + node.getName() + "inside declarations");
            }
        }
        children = null;
    }

    public void acceptVisitor(FOTreeVisitor fotv) {
        fotv.serveDeclarations(this);
    }

}
