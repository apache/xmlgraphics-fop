/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area;

import java.io.Serializable;

import java.util.HashMap;

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
    /**
     * Stacking left to right
     */
    public static final int LR = 0;

    /**
     * Stacking right to left
     */
    public static final int RL = 1;

    /**
     * Stacking top to bottom
     */
    public static final int TB = 2;

    /**
     * Stacking bottom to top
     */
    public static final int BT = 3;

    // orientations for reference areas
    /**
     * Normal orientation
     */
    public static final int ORIENT_0 = 0;

    /**
     * Rotated 90 degrees clockwise
     */
    public static final int ORIENT_90 = 1;
    
    /**
     * Rotate 180 degrees
     */
    public static final int ORIENT_180 = 2;
    
    /**
     * Rotated 270 degrees clockwise
     */
    public static final int ORIENT_270 = 3;

    // area class values
    
    /**
     * Normal class
     */
    public static final int CLASS_NORMAL = 0;

    /**
     * Fixed position class
     */
    public static final int CLASS_FIXED = 1;

    /**
     * Absolute position class
     */
    public static final int CLASS_ABSOLUTE = 2;

    /**
     * Before float class
     */
    public static final int CLASS_BEFORE_FLOAT = 3;

    /**
     * Footnote class
     */
    public static final int CLASS_FOOTNOTE = 4;

    /**
     * Side float class
     */
    public static final int CLASS_SIDE_FLOAT = 5;

    // IMPORTANT: make sure this is the maximum + 1
    /**
     * Maximum class count
     */
    public static final int CLASS_MAX = CLASS_SIDE_FLOAT + 1;

    private int areaClass = CLASS_NORMAL;
    private int ipd;

    protected Area parent = null; // Doesn't need to be saved in serialization

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
        } else {
            return new MinOptMax();
        }
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

    // Do nothing! Let subclasses do something if they can have child areas.
    public void addChild(Area child) {
    }


    HashMap props = null;

    public void addTrait(Trait prop) {
        if (props == null) {
            props = new HashMap(20);
        }
        props.put(prop.propType, prop.data);
    }

    public void addTrait(Object traitCode, Object prop) {
        if (props == null) {
            props = new HashMap(20);
        }
        props.put(traitCode, prop);
    }

    public HashMap getTraits() {
        return this.props;
    }

    public Object getTrait(Object oTraitCode) {
        return (props != null ? props.get(oTraitCode) : null);
    }
}

