/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.extensions;

import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;

/**
 * base class for extension objects
 */
public abstract class ExtensionObj extends FObj {

    /**
     *
     * @param parent the parent formatting object
     * @param propertyList the explicit properties of this object
     */
    public ExtensionObj(FONode parent) {
        super(parent);
    }

}

