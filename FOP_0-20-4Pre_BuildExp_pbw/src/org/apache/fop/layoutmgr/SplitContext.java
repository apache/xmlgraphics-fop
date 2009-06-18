/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layoutmgr;

import org.apache.fop.area.Area;
import org.apache.fop.area.MinOptMax;

public class SplitContext {

    Area nextArea;
    MinOptMax targetBPD;

    public SplitContext(MinOptMax targetBPD) {
        this.targetBPD = targetBPD;
        nextArea = null;
    }

}
