/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area;

import java.util.ArrayList;
import java.util.List;

// this is a normal flow reference area
// it containts a list of block areas from the flow
public class Flow extends Area {
    // the list of blocks created from the flow
    ArrayList blocks = new ArrayList();
    int stacking = TB;
    int width;

    public List getBlocks() {
        return blocks;
    }

}
