/*
 * $Id$
 * Copyright (C) 2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layoutmgr;

public class LeafPosition extends Position {

    private int m_iLeafPos;

    public LeafPosition(BPLayoutManager lm, int iLeafPos) {
	super(lm);
	m_iLeafPos = iLeafPos;
    }

    public int getLeafPos() {
	return m_iLeafPos;
    }
}

