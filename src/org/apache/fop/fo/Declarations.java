/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo;

// FOP
import org.apache.fop.fo.flow.*;
import org.apache.fop.apps.FOPException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Declarations formatting object.
 * A declarations formatting object holds a set of color-profiles
 * and optionally additional non-XSL namespace elements.
 * The color-profiles are held in a hashmap for use with color-profile
 * references.
 */
public class Declarations extends FObj {
    HashMap colorProfiles = null;
    ArrayList external = null;

    protected Declarations(FONode parent) {
        super(parent);
    }

    /**
     * At then end of this element sort out the child into
     * a hashmap of color profiles and a list of external xml.
     */
    public void end() {
        for(Iterator iter = children.iterator(); iter.hasNext(); ) {
            FONode node = (FONode)iter.next();
            if(node.getName().equals("fo:color-profile")) {
                ColorProfile cp = (ColorProfile)node;
                if(!"".equals(cp.getProfileName())) {
                    if(colorProfiles == null) {
                        colorProfiles = new HashMap();
                    }
                    if(colorProfiles.get(cp.getProfileName()) != null) {
                        // duplicate names
                        log.warn("Duplicate fo:color-profile profile name : " + cp.getProfileName());
                    }
                    colorProfiles.put(cp.getProfileName(), cp);
                } else {
                    log.warn("color-profile-name required for color profile");
                }
            } else if(node instanceof XMLObj) {
                if(external == null) {
                    external = new ArrayList();
                }
                external.add(node);
            } else {
                log.warn("invalid element " + node.getName() + "inside declarations");
            }
        }
        children = null;
    }
}
