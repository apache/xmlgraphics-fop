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
public abstract class Status {

    public final static int OK = 1;
    public final static int AREA_FULL_NONE = 2;
    public final static int AREA_FULL_SOME = 3;
    public final static int FORCE_PAGE_BREAK = 4;
    public final static int FORCE_PAGE_BREAK_EVEN = 5;
    public final static int FORCE_PAGE_BREAK_ODD = 6;
    public final static int FORCE_COLUMN_BREAK = 7;
    public final static int KEEP_WITH_NEXT = 8;

    public static boolean isIncomplete(int code) {
        return ((code != OK) && (code != KEEP_WITH_NEXT));
    }

    public static boolean laidOutNone(int code) {
        return (code == AREA_FULL_NONE);
    }

    public static boolean isPageBreak(int code) {
        switch( code) {
        case FORCE_PAGE_BREAK:
        case FORCE_PAGE_BREAK_EVEN:
        case FORCE_PAGE_BREAK_ODD:
            return true;
        }
        return false;
    }
}
