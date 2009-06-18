/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.flow;

// FOP
import org.apache.fop.fo.*;
import org.apache.fop.fo.properties.*;
import org.apache.fop.datatypes.*;
import org.apache.fop.layout.*;
import org.apache.fop.apps.FOPException;

public class TableFooter extends TableBody {

    public int getYPosition() {
        return areaContainer.getCurrentYPosition() - spaceBefore;
    }

    public void setYPosition(int value) {
        areaContainer.setYPosition(value + 2 * spaceBefore);
    }

    public TableFooter(FONode parent) {
        super(parent);
    }

}
