/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area;

import java.util.List;
import java.util.ArrayList;

public class BeforeFloat extends Area {
    // this is an optional block area that will be rendered
    // as the separator only if there are float areas
    Block separator = null;

    // before float area
    // has an optional separator
    // and a list of sub block areas

    ArrayList blocks = null;

    public List getBlocks() {
        return blocks;
    }

    public Block getSeparator() {
        return separator;
    }

    public int getHeight() {
        if (blocks == null) {
            return 0;
        }
        int h = 0;
        return h;
    }
}
