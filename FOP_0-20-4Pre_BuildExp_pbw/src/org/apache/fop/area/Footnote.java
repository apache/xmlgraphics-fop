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
public class Footnote extends BlockParent {
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

    public MinOptMax getMaxBPD() {
	MinOptMax maxbpd = parent.getMaxBPD();
	BodyRegion body = (BodyRegion)parent;
	Area a =  body.getMainReference();
	if (a != null) {
	    maxbpd = MinOptMax.subtract(maxbpd, a.getContentBPD());
	}
	if ((a=body.getBeforeFloat()) != null) {
	    maxbpd = MinOptMax.subtract(maxbpd, a.getContentBPD());
	}
	return maxbpd;
    }
}
