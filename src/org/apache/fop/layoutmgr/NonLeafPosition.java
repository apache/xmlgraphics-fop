/*
 * $Id$
 * Copyright (C) 2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layoutmgr;

public class NonLeafPosition extends Position {

    private Position subPos;

    public NonLeafPosition(LayoutManager lm, Position sub) {
        super(lm);
        subPos = sub;
    }

    public Position getPosition() {
        return subPos;
    }
}

