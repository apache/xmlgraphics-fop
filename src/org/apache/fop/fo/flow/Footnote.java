/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.flow;

// FOP
import org.apache.fop.fo.*;
import org.apache.fop.layout.*;
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.properties.*;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.layoutmgr.LayoutManager;
import org.apache.fop.layoutmgr.LeafNodeLayoutManager;

import java.util.List;

// Java
import java.util.ArrayList;

public class Footnote extends FObj {

    public Footnote(FONode parent) {
        super(parent);
    }

    public void addLayoutManager(List lms) {
        // add inlines layout manager
        //inline.addLayoutManager(lms);
        // set start and end footnote reference
    }

}

