/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.flow;

// FOP
import org.apache.fop.fo.*;
import org.apache.fop.layout.Area;
import org.apache.fop.layout.AreaClass;
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.properties.*;
import org.apache.fop.layout.*;

// Java
import java.util.Iterator;

public class FootnoteBody extends FObj {

    int align;
    int alignLast;
    int lineHeight;
    int startIndent;
    int endIndent;
    int textIndent;

    public FootnoteBody(FONode parent) {
        super(parent);
    }

    public Status layout(Area area) throws FOPException {
        this.areaClass = AreaClass.setAreaClass(AreaClass.XSL_FOOTNOTE);
        if (this.marker == START) {
            this.marker = 0;
        }
        BlockArea blockArea =
            new BlockArea(propMgr.getFontState(area.getFontInfo()),
                          area.getAllocationWidth(), area.spaceLeft(),
                          startIndent, endIndent, textIndent, align,
                          alignLast, lineHeight);
        blockArea.setGeneratedBy(this);
        blockArea.isFirst(true);
        blockArea.setPage(area.getPage());
        blockArea.start();

        blockArea.setAbsoluteHeight(area.getAbsoluteHeight());
        blockArea.setIDReferences(area.getIDReferences());

        blockArea.setTableCellXOffset(area.getTableCellXOffset());

        int numChildren = this.children.size();
        for (int i = this.marker; i < numChildren; i++) {
            FONode fo = (FONode)children.get(i);
            Status status;
            if ((status = fo.layout(blockArea)).isIncomplete()) {
                this.resetMarker();
                return status;
            }
        }
        blockArea.end();
        area.addChild(blockArea);
        area.increaseHeight(blockArea.getHeight());
        blockArea.isLast(true);
        return new Status(Status.OK);
    }

}
