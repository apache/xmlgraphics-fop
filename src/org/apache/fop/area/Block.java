/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.awt.geom.Rectangle2D;

// block areas hold either more block areas or line
// areas can also be used as a block spacer
// a block area may have children positioned by stacking
// or by relative to the parent for floats, tables and lists
// cacheable object
// has id information
public class Block extends BlockParent implements Serializable {
    // normally stacked with other blocks
    public static final int STACK = 0;
    // placed relative to the parent area
    public static final int RELATIVE = 1;
    // placed relative to the page or viewport
    public static final int ABSOLUTE = 2;

    int stacking = TB;

    // list of marker fo objects that are associated with this area
    // if a retrieve marker resolves this area it will format the
    // available markers, markers are discarded once page complete
    private ArrayList markers = null;

    // a block with may contain the dominant styling info in
    // terms of most lines or blocks with info


    int positioning = STACK;


    public void addBlock(Block block) {
        if (children == null) {
            children = new ArrayList();
        }
        children.add(block);
    }

    public void addLineArea(LineArea line) {
        if (children == null) {
            children = new ArrayList();
        }
        children.add(line);
    }

    public MinOptMax getContentBPD() {
        MinOptMax bpd = new MinOptMax();
        if(children != null) {
        for(Iterator iter = children.iterator(); iter.hasNext(); ) {
            Area area = (Area)iter.next();
            MinOptMax mom = area.getContentBPD();
            bpd.add(mom);
        }
        }
        return bpd;
    }

    public int getPositioning() {
        return positioning;
    }

    // store properties in array list, need better solution
    ArrayList traits = null;

    public void addTrait(Trait prop) {
        if (traits == null) {
            traits = new ArrayList();
        }
        traits.add(prop);
    }

    public List getTraitList() {
        return traits;
    }
}

