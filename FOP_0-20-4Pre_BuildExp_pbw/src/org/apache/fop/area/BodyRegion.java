/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area;

import java.awt.geom.Rectangle2D;

public class BodyRegion extends RegionReference {
    BeforeFloat beforeFloat;
    MainReference mainReference;
    Footnote footnote;
    private int columnGap;
    private int columnCount;

    /** Maximum block progression dimension. Note: min=opt=max */
    private MinOptMax maxBPD;

    /** Referenc inline progression dimension for the body. */
    private int refIPD;

    public BodyRegion() {
        super(BODY);
    }

    // Number of columns when not spanning
    public void setColumnCount(int colCount) {
	this.columnCount = colCount;
    }

    // Number of columns when not spanning
    public int getColumnCount() {
	return this.columnCount ;
    }

    // A length (mpoints)
    public void setColumnGap(int colGap) {
	this.columnGap = colGap;
    }

    public void setParent(Area area) {
	super.setParent(area);
	// Only if not scrolling or overflow !!!
	Rectangle2D refRect = ((RegionViewport)area).getViewArea();
	maxBPD = new MinOptMax((int)refRect.getHeight());
	refIPD = (int)refRect.getWidth();
    }

    public void setBeforeFloat(BeforeFloat bf) {
        beforeFloat = bf;
	beforeFloat.setParent(this);
    }

    public void setMainReference(MainReference mr) {
        mainReference = mr;
	mainReference.setParent(this);
    }

    public void setFootnote(Footnote foot) {
        footnote = foot;
	footnote.setParent(this);
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

    public MinOptMax getMaxBPD() {
	return maxBPD;
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
