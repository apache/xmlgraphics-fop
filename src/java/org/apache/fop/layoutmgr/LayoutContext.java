/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.layoutmgr;

import org.apache.fop.traits.MinOptMax;


/**
 * This class is used to pass information to the getNextBreakPoss()
 * method. It is set up by higher level LM and used by lower level LM.
 */
public class LayoutContext {
    /**
     * Values for flags.
     */
    public static final int LINEBREAK_AT_LF_ONLY = 0x01;
    /** Generated break possibility is first in a new area */
    public static final int NEW_AREA = 0x02;
    public static final int IPD_UNKNOWN = 0x04;
    /** Signal to a Line LM that a higher level LM may provoke a change
     *  in the reference area, thus ref area IPD. The LineLM should return
     *  without looking for a line break.
     */
    public static final int CHECK_REF_AREA = 0x08;

    /**
     * If this flag is set, it indicates that any leading fo:character
     * objects with suppress-at-line-break="suppress" should not generate
     * areas. This is the case at the beginning of each new LineArea
     * except the first.
     */
    public static final int SUPPRESS_LEADING_SPACE = 0x10;
    public static final int FIRST_AREA = 0x20;
    public static final int TRY_HYPHENATE = 0x40;
    public static final int LAST_AREA = 0x80;

    public static final int RESOLVE_LEADING_SPACE = 0x100;


    public int flags; // Contains some set of flags defined above
    /**
     * Total available stacking dimension for a "galley-level" layout
     * manager (Line or Flow). It is passed by the parent LM. For LineLM,
     * the block LM determines this based on indent properties.
     * These LM <b>may</b> wish to pass this information down to lower
     * level LM to allow them to optimize returned break possibilities.
     */
    MinOptMax stackLimit;

    /** True if current top-level reference area is spanning. */
    boolean bIsSpan;

    /** inline-progression-dimension of nearest ancestor reference area */
    int refIPD;

    /** Current pending space-after or space-end from preceding area */
    SpaceSpecifier trailingSpace;

    /** Current pending space-before or space-start from ancestor areas */
    SpaceSpecifier leadingSpace;

    /** Current hyphenation context. May be null. */
    private HyphContext hyphContext = null;

    /** Stretch or shrink value when making areas. */
    private double ipdAdjust = 0.0;

    /** Stretch or shrink value when adding spaces. */
    private double dSpaceAdjust = 0.0;

    private int iLineHeight;
    private int iBaseline;
    private int iMiddleShift;
    private int iTopShift; /*LF*/
    private int iBottomShift; /*LF*/
    private int iSpaceBefore; /*LF*/
    private int iSpaceAfter; /*LF*/

    public LayoutContext(LayoutContext parentLC) {
        this.flags = parentLC.flags;
        this.refIPD = parentLC.refIPD;
        this.stackLimit = null; // Don't reference parent MinOptMax!
        this.leadingSpace = parentLC.leadingSpace; //???
        this.trailingSpace = parentLC.trailingSpace; //???
        this.hyphContext = parentLC.hyphContext;
        this.dSpaceAdjust = parentLC.dSpaceAdjust;
        this.ipdAdjust = parentLC.ipdAdjust;
        this.iLineHeight = parentLC.iLineHeight;
        this.iBaseline = parentLC.iBaseline;
        this.iMiddleShift = parentLC.iMiddleShift;
/*LF*/  this.iTopShift = parentLC.iTopShift;
/*LF*/  this.iBottomShift = parentLC.iBottomShift;
/*LF*/  this.iSpaceBefore = parentLC.iSpaceBefore;
/*LF*/  this.iSpaceAfter = parentLC.iSpaceAfter;
        // Copy other fields as necessary. Use clone???
    }

    public LayoutContext(int flags) {
        this.flags = flags;
        this.refIPD = 0;
        stackLimit = new MinOptMax(0);
        leadingSpace = null;
        trailingSpace = null;
    }

    public void setFlags(int flags) {
        setFlags(flags, true);
    }

    public void setFlags(int flags, boolean bSet) {
        if (bSet) {
            this.flags |= flags;
        } else {
            this.flags &= ~flags;
        }
    }

    public void unsetFlags(int flags) {
        setFlags(flags, false);
    }

    public boolean isStart() {
        return ((this.flags & NEW_AREA) != 0);
    }

    public boolean startsNewArea() {
        return ((this.flags & NEW_AREA) != 0 && leadingSpace != null);
    }

    public boolean isFirstArea() {
        return ((this.flags & FIRST_AREA) != 0);
    }

    public boolean isLastArea() {
        return ((this.flags & LAST_AREA) != 0);
    }

    public boolean suppressLeadingSpace() {
        return ((this.flags & SUPPRESS_LEADING_SPACE) != 0);
    }

    public void setLeadingSpace(SpaceSpecifier space) {
        leadingSpace = space;
    }

    public SpaceSpecifier getLeadingSpace() {
        return leadingSpace;
    }

    public boolean resolveLeadingSpace() {
        return ((this.flags & RESOLVE_LEADING_SPACE) != 0);
    }

    public void setTrailingSpace(SpaceSpecifier space) {
        trailingSpace = space;
    }

    public SpaceSpecifier getTrailingSpace() {
        return trailingSpace;
    }

    public void setStackLimit(MinOptMax limit) {
        stackLimit = limit;
    }

    public MinOptMax getStackLimit() {
        return stackLimit;
    }

    public void setRefIPD(int ipd) {
        refIPD = ipd;
    }

    public int getRefIPD() {
        return refIPD;
    }

    public void setHyphContext(HyphContext hyph) {
        hyphContext = hyph;
    }

    public HyphContext getHyphContext() {
        return hyphContext;
    }

    public boolean tryHyphenate() {
        return ((this.flags & TRY_HYPHENATE) != 0);
    }

    public void setSpaceAdjust(double adjust) {
        dSpaceAdjust = adjust;
    }

    public double getSpaceAdjust() {
        return dSpaceAdjust;
    }

    public void setIPDAdjust(double ipdA) {
        ipdAdjust = ipdA;
    }

    public double getIPDAdjust() {
        return ipdAdjust;
    }

    public void setLineHeight(int lh) {
        iLineHeight = lh;
    }

    public int getLineHeight() {
        return iLineHeight;
    }

    public void setBaseline(int bl) {
        iBaseline = bl;
    }

    public int getBaseline() {
        return iBaseline;
    }
    
    public void setMiddleShift(int ms) {
        iMiddleShift = ms;
    }

    public int getMiddleBaseline() {
        return iBaseline + iMiddleShift;
    }
    
    public void setTopShift(int ts) {
        iTopShift = ts;
    }

    public int getTopBaseline() {
        return iBaseline + iTopShift;
    }

    public void setBottomShift(int bs) {
        iBottomShift = bs;
    }

    public int getBottomBaseline() {
        return iBaseline + iBottomShift;
    }

    public int getSpaceBefore() {
        return iSpaceBefore;
    }
    
    public void setSpaceBefore(int sp) {
        iSpaceBefore = sp;
    }

    public int getSpaceAfter() {
        return iSpaceAfter;
    }

    public void setSpaceAfter(int sp) {
        iSpaceAfter = sp;
    }
    
    public String toString() {
        return "Layout Context:" +
        "\nStack Limit: \t" + (getStackLimit() == null ? "null" : getStackLimit().toString()) +
        "\nTrailing Space: \t" + (getTrailingSpace() == null ? "null" : getTrailingSpace().toString()) +
        "\nLeading Space: \t" + (getLeadingSpace() == null ? "null" : getLeadingSpace().toString()) + 
        "\nReference IPD: \t" + getRefIPD() +
        "\nSpace Adjust: \t" + getSpaceAdjust() + 
        "\nIPD Adjust: \t" + getIPDAdjust() +
        "\nLine Height: \t" + getLineHeight() +
        "\nBaseline: \t" + getBaseline() +
        "\nMiddle Baseline: \t" + getMiddleBaseline() +
        "\nResolve Leading Space: \t" + resolveLeadingSpace() +
        "\nSuppress Leading Space: \t" + suppressLeadingSpace() +
        "\nIs First Area: \t" + isFirstArea() + 
        "\nStarts New Area: \t" + startsNewArea() + 
        "\nIs Last Area: \t" + isLastArea() +
        "\nTry Hyphenate: \t" + tryHyphenate();
    }
}

