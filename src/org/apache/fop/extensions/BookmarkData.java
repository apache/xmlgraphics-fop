/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.extensions;

import org.apache.fop.area.PageViewport;
import org.apache.fop.area.Resolveable;
import org.apache.fop.area.TreeExt;
import org.apache.fop.area.AreaTree;

import java.util.*;

public class BookmarkData implements Resolveable, TreeExt {
    private ArrayList subData = new ArrayList();
    private HashMap idRefs = new HashMap();

    // area tree for the top level object to notify when resolved
    private AreaTree areaTree = null;

    String idRef;
    PageViewport pageRef = null;
    String label = null;

    public BookmarkData() {
        idRef = null;
    }

    public BookmarkData(String id) {
        idRef = id;
        idRefs.put(idRef, this);
    }

    public void setAreaTree(AreaTree at) {
        areaTree = at;
    }

    public String getID() {
        return idRef;
    }

    public void addSubData(BookmarkData sub) {
        subData.add(sub);
        idRefs.put(sub.getID(), sub);
        String[] ids = sub.getIDs();
        for(int count = 0; count < ids.length; count++) {
            idRefs.put(ids[count], sub);
        }
    }

    public void setLabel(String l) {
        label = l;
    }

    public String getLabel() {
        return label;
    }

    public int getCount() {
        return subData.size();
    }

    public BookmarkData getSubData(int count) {
        return (BookmarkData)subData.get(count);
    }

    public PageViewport getPage() {
        return pageRef;
    }

    public boolean isResolveable() {
        return true;
    }

    public String getMimeType() {
        return "application/pdf";
    }

    public String getName() {
        return "Bookmark";
    }

    public boolean isResolved() {
        return idRefs == null;
    }

    public String[] getIDs() {
        return (String[])idRefs.keySet().toArray(new String[] {});
    }

    public void resolve(String id, List pages) {
        if(!id.equals(idRef)) {
            BookmarkData bd = (BookmarkData)idRefs.get(id);
            idRefs.remove(id);
            if(bd != null) {
                bd.resolve(id, pages);
                if(bd.isResolved()) {
                    checkFinish();
                }
            } else if (idRef == null) {
                checkFinish();
            }
        } else {
            if(pages != null) {
                pageRef = (PageViewport)pages.get(0);
            }
            // TODO
            // get rect area of id on page

            idRefs.remove(idRef);
            checkFinish();
        }
    }

    private void checkFinish() {
        if(idRefs.size() == 0) {
            idRefs = null;
            if(areaTree != null) {
                areaTree.handleTreeExtension(this, TreeExt.AFTER_PAGE);
            }
        }
    }
}

