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
import org.apache.fop.messaging.*;

// Java
import java.util.Iterator;
import java.util.ArrayList;

public class Footnote extends FObj {

    public static class Maker extends FObj.Maker {
        public FObj make(FObj parent,
                         PropertyList propertyList) throws FOPException {
            return new Footnote(parent, propertyList);
        }

    }

    public static FObj.Maker maker() {
        return new Footnote.Maker();
    }

    public Footnote(FObj parent,
                    PropertyList propertyList) throws FOPException {
        super(parent, propertyList);
    }

    public String getName() {
        return "fo:footnote";
    }

    public int layout(Area area) throws FOPException {
        FONode inline = null;
        FONode fbody = null;
        if (this.marker == START) {
            this.marker = 0;
        }
        int numChildren = this.children.size();
        for (int i = this.marker; i < numChildren; i++) {
            FONode fo = (FONode)children.get(i);
            if (fo instanceof Inline) {
                inline = fo;
                int status = fo.layout(area);
                if (Status.isIncomplete(status)) {
                    return status;
                }
            } else if (inline != null && fo instanceof FootnoteBody) {
                // add footnote to current page or next if it can't fit
                fbody = fo;
                if (area instanceof BlockArea) {
                    ((BlockArea)area).addFootnote((FootnoteBody)fbody);
                } else {
                    Page page = area.getPage();
                    layoutFootnote(page, (FootnoteBody)fbody, area);
                }
            }
        }
        if (fbody == null) {
            log.error("no footnote-body in footnote");
        }
        if (area instanceof BlockArea) {}
        return Status.OK;
    }

    public static boolean layoutFootnote(Page p, FootnoteBody fb, Area area) {
        try {
            BodyAreaContainer bac = p.getBody();
            AreaContainer footArea = bac.getFootnoteReferenceArea();
            footArea.setIDReferences(bac.getIDReferences());
            int basePos = footArea.getCurrentYPosition()
                          - footArea.getHeight();
            int oldHeight = footArea.getHeight();
            if (area != null) {
                footArea.setMaxHeight(area.getMaxHeight() - area.getHeight()
                                      + footArea.getHeight());
            } else {
                footArea.setMaxHeight(bac.getMaxHeight()
                                      + footArea.getHeight());
            }
            int status = fb.layout(footArea);
            if (Status.isIncomplete(status)) {
                // add as a pending footnote
                return false;
            } else {
                if (area != null) {
                    area.setMaxHeight(area.getMaxHeight()
                                      - footArea.getHeight() + oldHeight);
                }
                // bac.setMaxHeight(bac.getMaxHeight() - footArea.getHeight() + oldHeight);
                if (bac.getFootnoteState() == 0) {
                    Area ar = bac.getMainReferenceArea();
                    decreaseMaxHeight(ar, footArea.getHeight() - oldHeight);
                    footArea.setYPosition(basePos + footArea.getHeight());
                }
            }
        } catch (FOPException fope) {
            return false;
        }
        return true;
    }

    protected static void decreaseMaxHeight(Area ar, int change) {
        ar.setMaxHeight(ar.getMaxHeight() - change);
        ArrayList children = ar.getChildren();
        for (int i = 0; i < children.size(); i++) {
            Object obj = children.get(i);
            if (obj instanceof Area) {
                Area childArea = (Area)obj;
                decreaseMaxHeight(childArea, change);
            }
        }
    }

}
