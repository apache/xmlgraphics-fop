/* $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.plan;

// FOP
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.XMLObj;

/**
 * Since SVG objects are not layed out then this class checks
 * that this element is not being layed out inside some incorrect
 * element.
 */
public class PlanObj extends XMLObj {

    /**
     *
     * @param parent the parent formatting object
     */
    public PlanObj(FONode parent) {
        super(parent);
    }

    public String getNameSpace() {
        return "http://xml.apache.org/fop/plan";
    }
}

