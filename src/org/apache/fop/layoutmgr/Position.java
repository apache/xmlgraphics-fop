/*
 * $Id$
 * Copyright (C) 2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layoutmgr;

public class Position {
    private LayoutManager m_lm;

    public Position(LayoutManager lm) {
        m_lm = lm;
    }

    public LayoutManager getLM() {
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

