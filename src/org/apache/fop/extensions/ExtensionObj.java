/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.extensions;

import org.apache.fop.fo.*;
import org.apache.fop.layout.*;
import org.apache.fop.apps.FOPException;

/**
 * base class for extension objects
 */
public abstract class ExtensionObj extends FObj {

    /**
     *
     * @param parent the parent formatting object
     * @param propertyList the explicit properties of this object
     */
    public ExtensionObj(FObj parent, PropertyList propertyList) {
        super(parent, propertyList);
    }

    /**
     * Called for extensions within a page sequence or flow. These extensions
     * are allowed to generate visible areas within the layout.
     *
     *
     * @param area
     */
    public Status layout(Area area) throws FOPException {
        ExtensionArea extArea = new ExtensionArea(this);
        area.addChild(extArea);
        return new Status(Status.OK);
    }


    /**
     * Called for root extensions. Root extensions aren't allowed to generate
     * any visible areas. They are used for extra items that don't show up in
     * the page layout itself. For example: pdf outlines
     *
     * @param areaTree
     */
    public void format(AreaTree areaTree) throws FOPException {
        ExtensionArea extArea = new ExtensionArea(this);
        areaTree.addExtension(this);
    }

}
