/*
 * $Id$
 * Copyright (C) 2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layoutmgr;

public class Position {
    private LayoutProcessor layoutManager;

    public Position(LayoutProcessor lm) {
        layoutManager = lm;
    }

    public LayoutProcessor getLM() {
        return layoutManager;
    }

    /**
     * Overridden by NonLeafPosition to return the Position of its
     * child LM.
     */
    public Position getPosition() {
        return null;
    }
}

