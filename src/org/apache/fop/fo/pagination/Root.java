/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.pagination;

// FOP
import org.apache.fop.fo.*;
import org.apache.fop.fo.flow.*;
import org.apache.fop.fo.properties.*;
import org.apache.fop.layout.AreaTree;
import org.apache.fop.apps.FOPException;
import org.apache.fop.extensions.ExtensionObj;

/**
 * Class modeling the fo:root object. Contains page masters, root extensions,
 * page-sequences.
 *
 * @see <a href="@XSLFO-STD@#fo_root" target="_xslfostd">@XSLFO-STDID@
 *     &para;6.4.2</a>
 */
public class Root extends FObj {

    public static class Maker extends FObj.Maker {
        public FObj make(FObj parent,
                         PropertyList propertyList) throws FOPException {
            return new Root(parent, propertyList);
        }

    }

    public static FObj.Maker maker() {
        return new Root.Maker();
    }

    LayoutMasterSet layoutMasterSet;
    /**
     * Store a page sequence over a sequence change. The next sequence will
     * get the page number from this and also take care of forced pages.
     */
    PageSequence pageSequence;

    protected Root(FObj parent,
                   PropertyList propertyList) throws FOPException {
        super(parent, propertyList);
        // this.properties.get("media-usage");

        if (parent != null) {
            throw new FOPException("root must be root element");
        }
    }

    public void setPageSequence(PageSequence pageSequence) {
        this.pageSequence=pageSequence;
    }

    public PageSequence getPageSequence() {
        return this.pageSequence;
    }

    public LayoutMasterSet getLayoutMasterSet() {
        return this.layoutMasterSet;
    }

    public void setLayoutMasterSet(LayoutMasterSet layoutMasterSet) {
        this.layoutMasterSet = layoutMasterSet;
    }

    public String getName() {
        return "fo:root";
    }
}
