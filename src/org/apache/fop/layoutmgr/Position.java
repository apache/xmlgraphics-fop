/*
 * $Id$
 * Copyright (C) 2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layoutmgr;

public class Position {
    private BPLayoutManager m_lm;

    public Position(BPLayoutManager lm) {
        m_lm = lm;
    }

    public BPLayoutManager getLM() {
        return m_lm;
    }

    /**
     * Overridden by NonLeafPosition to return the Position of its
     * child LM.
     */
    public Position getPosition() {
        return null;
    }
}

