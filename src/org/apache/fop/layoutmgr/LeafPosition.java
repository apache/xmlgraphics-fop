/*
 * $Id$
 * Copyright (C) 2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layoutmgr;

public class LeafPosition extends Position {

    private int iLeafPos;

    public LeafPosition(LayoutManager lm, int pos) {
        super(lm);
        iLeafPos = pos;
    }

    public int getLeafPos() {
        return iLeafPos;
    }
}

