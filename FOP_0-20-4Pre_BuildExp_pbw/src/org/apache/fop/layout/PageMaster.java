/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layout;

import java.io.*;

import org.apache.fop.area.PageViewport;

public class PageMaster {

    private PageViewport pageVP ;

    public PageMaster(PageViewport pageVP) {
        this.pageVP = pageVP;
    }

    // make a clone of the master
    public PageViewport makePage() {
        return (PageViewport)pageVP.clone();
    }

}
