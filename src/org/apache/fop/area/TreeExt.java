/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area;

/**
 */
public interface TreeExt {
    public final static int IMMEDIATELY = 0;
    public final static int AFTER_PAGE = 1;
    public final static int END_OF_DOC = 2;

    public boolean isResolveable();
    public String getMimeType();
    public String getName();
}
