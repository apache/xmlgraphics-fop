/*
 * $Id$
 * Copyright (C) 2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layoutmgr;

public class NonLeafPosition extends Position {

    private Position m_subPos ;

    public NonLeafPosition(BPLayoutManager lm, Position subPos) {
        super(lm);
        m_subPos = subPos;
    }

    public Position getPosition() {
        return m_subPos;
    }
}

