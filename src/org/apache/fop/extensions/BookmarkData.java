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

import java.util.*;

public class BookmarkData implements Resolveable, TreeExt {
    private ArrayList subData = new ArrayList();
    private HashMap idRefs = new HashMap();

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

    public String getID() {
        return idRef;
    }

    public void addSubData(BookmarkData sub) {
        subData.add(sub);
        idRefs.put(sub.getID(), sub);
    }

    public void setLabel(String l) {
        label = l;
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

    public void resolve(String id, ArrayList pages) {
        if(!id.equals(idRef)) {
            BookmarkData bd = (BookmarkData)idRefs.get(id);
            bd.resolve(id, pages);
            if(bd.isResolved()) {
                idRefs.remove(id);
                if(idRefs.size() == 0) {
                    idRefs = null;
                }
            }
        } else {
            if(pages != null) {
                pageRef = (PageViewport)pages.get(0);
            }
            // TODO
            // get rect area of id on page

            idRefs.remove(idRef);
            if(idRefs.size() == 0) {
                idRefs = null;
            }
        }
    }
}

