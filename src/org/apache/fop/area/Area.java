/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area;

import java.io.Serializable;
import org.apache.fop.fo.FObj;

// If the area appears more than once in the output
// or if the area has external data it is cached
// to keep track of it and to minimize rendered output
// renderers can render the output once and display it
// for every occurence
// this should also extend to all outputs (including PDFGraphics2D)
// and all types of renderers

/**
 * Base object for all areas.
 */
public class Area implements Serializable {
    // stacking directions
    public static final int LR = 0;
    public static final int RL = 1;
    public static final int TB = 2;
    public static final int BT = 3;

    // orientations for reference areas
    public static final int ORIENT_0 = 0;
    public static final int ORIENT_90 = 1;
    public static final int ORIENT_180 = 2;
    public static final int ORIENT_270 = 3;

    // area class values
    public static final int CLASS_NORMAL = 0;
    public static final int CLASS_FIXED = 1;
    public static final int CLASS_ABSOLUTE = 2;
    public static final int CLASS_BEFORE_FLOAT = 3;
    public static final int CLASS_FOOTNOTE = 4;
    public static final int CLASS_SIDE_FLOAT = 5;
    // IMPORTANT: make sure this is the maximum + 1
    public static final int CLASS_MAX = CLASS_SIDE_FLOAT+1;

    private int areaClass=CLASS_NORMAL;
    private FObj genFObj;
    private int ipd;

    protected Area parent =null; // Doesn't need to be saved in serialization

    public int getAreaClass() {
	return areaClass;
    }

    public void setAreaClass(int areaClass) {
	this.areaClass = areaClass;
    }

    public void setIPD(int i) {
        ipd = i;
    }

    public int getIPD() {
        return ipd;
    }

    /**
     * Return a length range describing the minimum, optimum and maximum
     * lengths available for content in the block-progression-direction.
     * This is calculated from the theoretical maximum size of the area
     * and its current content.
     */
    public MinOptMax getAvailBPD() {
	return MinOptMax.subtract(getMaxBPD(), getContentBPD());
    }

    /**
     * Return a length range describing the theoretical maximum size of an
     * area in the block-progression-direction.
     * For areas holding normal flowing or floating content in paged media,
     * this depends on the size of the body. In general the answer is the
     * gotten from the parent. At the body level, the calculation accounts
     * for the sizes of the conditional areas.
     */
    public MinOptMax getMaxBPD() {
	if (parent != null) {
	    return parent.getMaxBPD();
	}
	else return new MinOptMax();
    }

    /**
     * Return a length range describing the minimum, optimum and maximum
     * lengths of all area content in the block-progression-direction.
     * This is based on the allocation rectangles of all content in
     * the area.
     */
    public MinOptMax getContentBPD() {
	return new MinOptMax();
    }

    /**
     * Return a length range describing the minimum, optimum and maximum
     * lengths of the area's allocation rectangle
     * in the block-progression-direction.
     * This is based on the allocation rectangles of all content in
     * the area.
     * The default implementation simply returns the same as the content BPD.
     * If an Area has before or after border and padding, these contribute
     * to the allocation BPD, depending on conditionality.
     */
    public MinOptMax getAllocationBPD() {
	return getContentBPD();
    }

    public void setParent(Area parent) {
	this.parent = parent;
    }

    public void setGeneratingFObj(FObj fobj) {
	this.genFObj = fobj;
    }

    public FObj getGeneratingFObj() {
	return this.genFObj;
    }
}
