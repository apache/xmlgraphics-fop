/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area.inline;

import org.apache.fop.area.Resolveable;

public abstract class Unresolved extends InlineArea implements Resolveable {
    boolean resolved = false;

    public boolean isResolved() {
       return resolved;
    }

}
