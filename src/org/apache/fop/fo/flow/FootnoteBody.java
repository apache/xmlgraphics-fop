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

public class FootnoteBody extends FObj {

    int align;
    int alignLast;
    int lineHeight;
    int startIndent;
    int endIndent;
    int textIndent;

    public static class Maker extends FObj.Maker {
        public FObj make(FObj parent,
                         PropertyList propertyList) throws FOPException {
            return new FootnoteBody(parent, propertyList);
        }

    }

    public static FObj.Maker maker() {
        return new FootnoteBody.Maker();
    }

    public FootnoteBody(FObj parent,
                        PropertyList propertyList) throws FOPException {
        super(parent, propertyList);
        this.areaClass = AreaClass.setAreaClass(AreaClass.XSL_FOOTNOTE);
    }

    public String getName() {
        return "fo:footnote-body";
    }

    public int layout(Area area) throws FOPException {
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
        blockArea.setParent(area);
        blockArea.setPage(area.getPage());
        blockArea.start();

        blockArea.setAbsoluteHeight(area.getAbsoluteHeight());
        blockArea.setIDReferences(area.getIDReferences());

        blockArea.setTableCellXOffset(area.getTableCellXOffset());

        int numChildren = this.children.size();
        for (int i = this.marker; i < numChildren; i++) {
            FONode fo = (FONode)children.get(i);
            int status;
            if (Status.isIncomplete((status = fo.layout(blockArea)))) {
                this.resetMarker();
                return status;
            }
        }
        blockArea.end();
        area.addChild(blockArea);
        area.increaseHeight(blockArea.getHeight());
        blockArea.isLast(true);
        return Status.OK;
    }

}
