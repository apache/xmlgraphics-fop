/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

// may combine with before float into a conditional area
public class Footnote implements Serializable {
    Block separator = null;

    // footnote has an optional separator
    // and a list of sub block areas that can be added/removed

    // this is the relative position of the footnote inside
    // the body region
    int top;

    ArrayList blocks = null;

    public void setSeparator(Block sep) {
        separator = sep;
    }

    public void addBlock(Block block) {
        if (blocks == null) {
            blocks = new ArrayList();
        }
        blocks.add(block);
    }

    public Block getSeparator() {
        return separator;
    }

    public List getBlocks() {
        return blocks;
    }
}
