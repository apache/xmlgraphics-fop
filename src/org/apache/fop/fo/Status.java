/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo;

/**
 * classes representating the status of laying out a formatting object
 */
public class Status {

    protected int code;

    public final static int OK = 1;
    public final static int AREA_FULL_NONE = 2;
    public final static int AREA_FULL_SOME = 3;
    public final static int FORCE_PAGE_BREAK = 4;
    public final static int FORCE_PAGE_BREAK_EVEN = 5;
    public final static int FORCE_PAGE_BREAK_ODD = 6;
    public final static int FORCE_COLUMN_BREAK = 7;
    public final static int KEEP_WITH_NEXT = 8;

    public Status(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    public boolean isIncomplete() {
        return ((this.code != OK) && (this.code != KEEP_WITH_NEXT));
    }

    public boolean laidOutNone() {
        return (this.code == AREA_FULL_NONE);
    }

    public boolean isPageBreak() {
        return ((this.code == FORCE_PAGE_BREAK)
                || (this.code == FORCE_PAGE_BREAK_EVEN)
                || (this.code == FORCE_PAGE_BREAK_ODD));
    }

}
