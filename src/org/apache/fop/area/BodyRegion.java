/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area;

/**
 * The body region area.
 * This area contains a main reference area and optionally a
 * before float and footnote area.
 */
public class BodyRegion extends RegionReference {
    private BeforeFloat beforeFloat;
    private MainReference mainReference;
    private Footnote footnote;
    private int columnGap;
    private int columnCount;

    /** Referenc inline progression dimension for the body. */
    private int refIPD;

    /**
     * Create a new body region area.
     * This sets the region reference area class to BODY.
     */
    public BodyRegion() {
        super(BODY);
    }

    // Number of columns when not spanning
    public void setColumnCount(int colCount) {
        this.columnCount = colCount;
    }

    // Number of columns when not spanning
    public int getColumnCount() {
        return this.columnCount;
    }

    // A length (mpoints)
    public void setColumnGap(int colGap) {
        this.columnGap = colGap;
    }

    public void setBeforeFloat(BeforeFloat bf) {
        beforeFloat = bf;
    }

    public void setMainReference(MainReference mr) {
        mainReference = mr;
    }

    public void setFootnote(Footnote foot) {
        footnote = foot;
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

    public Object clone() {
        BodyRegion br = new BodyRegion();
        br.setCTM(getCTM());
        br.setIPD(getIPD());
        br.columnGap = columnGap;
        br.columnCount = columnCount;
        return br;
    }
}
