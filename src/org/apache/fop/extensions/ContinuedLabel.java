/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */
package org.apache.fop.extensions;

import org.apache.fop.datatypes.IDReferences;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.Status;
import org.apache.fop.layout.Area;
import org.apache.fop.layout.AreaTree;
import org.apache.fop.apps.FOPException;

/**
 * Implement continued labels for table header/footer.
 * Content of this element must be an fo:inline.
 */
public class ContinuedLabel extends ExtensionObj {

    private FObj containingTable=null;

    public static class Maker extends FObj.Maker {
        public FObj make(FObj parent, PropertyList propertyList) {
            return new ContinuedLabel(parent, propertyList);
        }

    }

    public static FObj.Maker maker() {
        return new ContinuedLabel.Maker();
    }

    public ContinuedLabel(FObj parent, PropertyList propertyList) {
        super(parent, propertyList);

        // Find ancestor table
        for (; parent!=null ; parent = parent.getParent()) {
            if (parent.getName().equals("fo:table")) {
                this.containingTable=parent;
                break;
            }
        }
    }


    public String getName() {
        return "fop:continued-label";
    }


    /**
     * If we are within a cell in a table-header or table-footer object
     * and this is not the first generated area for the table, then generate
     * an inline area and put the content in it.
     * @param area The parent area.
     * @return Value indicating where all, some or none of the content
     * was placed in the current parent area.
     */
    public int layout(Area area) throws FOPException {
        if (this.marker == START) {
            this.marker = 0;
        }

        // See if ancestor table has generated any areas yet.
        // Note: areasGenerated was already public so I use it, but this
        // is definitely not very good style!
        if (containingTable!=null && containingTable.areasGenerated > 0) {
            int numChildren = this.children.size();
            for (int i = this.marker; i < numChildren; i++) {
                FONode fo = (FONode)children.get(i);
                int status;
                if (Status.isIncomplete(status = fo.layout(area))) {
                    this.marker = i;
                    return status;
                }
            }
        }
        return Status.OK;
    }

    /**
     * Null implementation.
     */
    public void format(AreaTree areaTree) throws FOPException {
    }

    /**
     * Removes property id from IDReferences.
     * This overrides the generic FObj function since ID has no meaning
     * on a continued-label. However, for now, it propagates to its children
     * since we don't prevent them from creating IDs. This should probably be
     * fixed!
     * @param idReferences the id to remove
     */
    public void removeID(IDReferences idReferences) {
        int numChildren = this.children.size();
        for (int i = 0; i < numChildren; i++) {
            FONode child = (FONode)children.get(i);
            if ((child instanceof FObj)) {
                ((FObj)child).removeID(idReferences);
            }
        }
    }
}
