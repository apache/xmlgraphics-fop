/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area;

public class BodyRegion extends Region {
    BeforeFloat beforeFloat;
    MainReference mainReference;
    Footnote footnote;

    public BodyRegion() {
        super(BODY);
    }

    public BeforeFloat getBeforeFloat() {
        return beforeFloat;
    }

    public MainReference getMainReference() {
        return mainReference;
    }

    public Footnote getFootnote() {
        return footnote;
    }
}
