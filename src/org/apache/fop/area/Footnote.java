/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area;

// may combine with before float into a conditional area

/**
 * Footnote reference area.
 * This areas holds footnote areas and an optional separator area.
 */
public class Footnote extends BlockParent {
    private Block separator = null;

    // footnote has an optional separator
    // and a list of sub block areas that can be added/removed

    // this is the relative position of the footnote inside
    // the body region
    private int top;

    /**
     * Set the separator area for this footnote.
     *
     * @param sep the separator area
     */
    public void setSeparator(Block sep) {
        separator = sep;
    }

    /**
     * Get the separator area for this footnote area.
     *
     * @return the separator area
     */
    public Block getSeparator() {
        return separator;
    }

}

