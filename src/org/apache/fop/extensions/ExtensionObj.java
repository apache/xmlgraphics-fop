/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.extensions;

import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;

/**
 * Base class for pdf bookmark extension objects.
 */
public abstract class ExtensionObj extends FObj {

    /**
     * Create a new extension object.
     *
     * @param parent the parent formatting object
     */
    public ExtensionObj(FONode parent) {
        super(parent);
    }

}

