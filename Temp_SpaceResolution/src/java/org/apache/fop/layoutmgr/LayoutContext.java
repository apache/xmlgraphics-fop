/*
 * Copyright 1999-2005 The Apache Software Foundation.
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

import java.util.Collections;
import java.util.List;

import org.apache.fop.fo.Constants;
import org.apache.fop.layoutmgr.inline.HyphContext;
import org.apache.fop.traits.MinOptMax;
import org.apache.fop.layoutmgr.inline.AlignmentContext;


/**
 * This class is used to pass information to the getNextKnuthElements()
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

    /**
     * This flag indicates that there's a keep-with-next that hasn't
     * been processed, yet.
     */
    public static final int KEEP_WITH_NEXT_PENDING = 0x200;
    /**
     * This flag indicates that there's a keep-with-previous that hasn't
     * been processed, yet.
     */
    public static final int KEEP_WITH_PREVIOUS_PENDING = 0x400;


    public int flags; // Contains some set of flags defined above
    /**
     * Total available stacking dimension for a "galley-level" layout
     * manager (Line or Flow). It is passed by the parent LM. For LineLM,
     * the block LM determines this based on indent properties.
     * These LM <b>may</b> wish to pass this information down to lower
     * level LM to allow them to optimize returned break possibilities.
     */
    MinOptMax stackLimit;

    /** True if current element list is spanning in multi-column layout. */
    private int nextSpan = Constants.NOT_SET;

    /** inline-progression-dimension of nearest ancestor reference area */
    int refIPD;

    /** the writing mode established by the nearest ancestor reference area */
    private int writingMode = Constants.EN_LR_TB;

    /** Current pending space-after or space-end from preceding area */
    SpaceSpecifier trailingSpace;

    /** Current pending space-before or space-start from ancestor areas */
    SpaceSpecifier leadingSpace;
    
    /**
     * A list of pending marks (border and padding) on the after edge when a page break occurs. 
     * May be null.
     */
    private List pendingAfterMarks;
    
    /**
     * A list of pending marks (border and padding) on the before edge when a page break occurs. 
     * May be null.
     */
    private List pendingBeforeMarks;

    /** Current hyphenation context. May be null. */
    private HyphContext hyphContext = null;

    /** Alignment in BP direction */
    private int bpAlignment = Constants.EN_START;
    
    /** Stretch or shrink value when making areas. */
    private double ipdAdjust = 0.0;

    /** Stretch or shrink value when adding spaces. */
    private double dSpaceAdjust = 0.0;

    private AlignmentContext alignmentContext = null;
    
    /** Amount of space before / start */
    private int spaceBefore = 0;
    
    /** Amount of space after / end */
    private int spaceAfter = 0;
    
    /** Amount of space to reserve at the beginning of each line */
    private int lineStartBorderAndPaddingWidth = 0;
    /** Amount of space to reserve at the end of each line */
    private int lineEndBorderAndPaddingWidth = 0;

    /**
     * Copy constructor for creating child layout contexts.
     * @param parentLC the parent layout context to copy from
     */
    public LayoutContext(LayoutContext parentLC) {
        this.flags = parentLC.flags;
        this.refIPD = parentLC.refIPD;
        this.writingMode = parentLC.writingMode;
        this.stackLimit = null; // Don't reference parent MinOptMax!
        this.leadingSpace = parentLC.leadingSpace; //???
        this.trailingSpace = parentLC.trailingSpace; //???
        this.hyphContext = parentLC.hyphContext;
        this.bpAlignment = parentLC.bpAlignment;
        this.dSpaceAdjust = parentLC.dSpaceAdjust;
        this.ipdAdjust = parentLC.ipdAdjust;
        this.alignmentContext = parentLC.alignmentContext;
        this.lineStartBorderAndPaddingWidth = parentLC.lineStartBorderAndPaddingWidth;
        this.lineEndBorderAndPaddingWidth = parentLC.lineEndBorderAndPaddingWidth;
        copyPendingMarksFrom(parentLC);
        // Copy other fields as necessary.
    }

    /**
     * Main constructor.
     * @param flags the initial flags
     */
    public LayoutContext(int flags) {
        this.flags = flags;
        this.refIPD = 0;
        stackLimit = new MinOptMax(0);
        leadingSpace = null;
        trailingSpace = null;
    }

    public void copyPendingMarksFrom(LayoutContext source) {
        if (source.pendingAfterMarks != null) {
            this.pendingAfterMarks = new java.util.ArrayList(source.pendingAfterMarks); 
        }
        if (source.pendingBeforeMarks != null) {
            this.pendingBeforeMarks = new java.util.ArrayList(source.pendingBeforeMarks); 
        }
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

    public boolean isKeepWithNextPending() {
        return ((this.flags & KEEP_WITH_NEXT_PENDING) != 0);
    }
    
    public boolean isKeepWithPreviousPending() {
        return ((this.flags & KEEP_WITH_PREVIOUS_PENDING) != 0);
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

    /**
     * Adds a border or padding element to the pending list which will be used to generate
     * the right element list for break possibilities. Conditionality resolution will be done
     * elsewhere.
     * @param element the border, padding or space element
     */
    public void addPendingAfterMark(UnresolvedListElementWithLength element) {
        if (this.pendingAfterMarks == null) {
            this.pendingAfterMarks = new java.util.ArrayList();
        }
        this.pendingAfterMarks.add(element);
    }
    
    /**
     * @return the pending border and padding elements at the after edge
     * @see addPendingAfterMark(BorderOrPaddingElement)
     */
    public List getPendingAfterMarks() {
        if (this.pendingAfterMarks != null) {
            return Collections.unmodifiableList(this.pendingAfterMarks);
        } else {
            return null;
        }
    }
    
    /**
     * Adds a border or padding element to the pending list which will be used to generate
     * the right element list for break possibilities. Conditionality resolution will be done
     * elsewhere.
     * @param element the border, padding or space element
     */
    public void addPendingBeforeMark(UnresolvedListElementWithLength element) {
        if (this.pendingBeforeMarks == null) {
            this.pendingBeforeMarks = new java.util.ArrayList();
        }
        this.pendingBeforeMarks.add(element);
    }
    
    /**
     * @return the pending border and padding elements at the before edge
     * @see addPendingBeforeMark(BorderOrPaddingElement)
     */
    public List getPendingBeforeMarks() {
        if (this.pendingBeforeMarks != null) {
            return Collections.unmodifiableList(this.pendingBeforeMarks);
        } else {
            return null;
        }
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

    /**
     * Sets the currently applicable alignment in BP direction.
     * @param alignment one of EN_START, EN_JUSTIFY etc.
     */
    public void setBPAlignment(int alignment) {
        this.bpAlignment = alignment;
    }
    
    /** @return the currently applicable alignment in BP direction (EN_START, EN_JUSTIFY...) */
    public int getBPAlignment() {
        return this.bpAlignment;
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

    public void setAlignmentContext(AlignmentContext alignmentContext) {
        this.alignmentContext = alignmentContext;
    }
    
    public AlignmentContext getAlignmentContext() {
        return this.alignmentContext;
    }

    public void resetAlignmentContext() {
        if (this.alignmentContext != null) {
            this.alignmentContext = this.alignmentContext.getParentAlignmentContext();
        }
    }
    
    /**
     * Get the width to be reserved for border and padding at the start of the line.
     * @return the width to be reserved
     */
    public int getLineStartBorderAndPaddingWidth() {
        return lineStartBorderAndPaddingWidth;
    }
    
    /**
     * Set the width to be reserved for border and padding at the start of the line.
     * @param lineStartBorderAndPaddingWidth the width to be reserved
     */
    public void setLineStartBorderAndPaddingWidth(int lineStartBorderAndPaddingWidth) {
        this.lineStartBorderAndPaddingWidth = lineStartBorderAndPaddingWidth;
    }
    
    /**
     * Get the width to be reserved for border and padding at the end of the line.
     * @return the width to be reserved
     */
    public int getLineEndBorderAndPaddingWidth() {
        return lineEndBorderAndPaddingWidth;
    }
    
    /**
     * Set the width to be reserved for border and padding at the end of the line.
     * @param lineEndBorderAndPaddingWidth the width to be reserved
     */
    public void setLineEndBorderAndPaddingWidth(int lineEndBorderAndPaddingWidth) {
        this.lineEndBorderAndPaddingWidth = lineEndBorderAndPaddingWidth;
    }
    
    /**
     * @return true if the current element list ends early because of a span change
     * in multi-column layout.
     */
    public int getNextSpan() {
        return nextSpan;
    }
    
    /**
     * Used to signal the PSLM that the element list ends early because of a span change in
     * multi-column layout.
     * @param span the new span value (legal values: NOT_SET, EN_NONE, EN_ALL)
     */
    public void signalSpanChange(int span) {
        if (span == Constants.NOT_SET || span == Constants.EN_NONE || span == Constants.EN_ALL) {
            this.nextSpan = span;
        } else {
            throw new IllegalArgumentException("Illegal value on signalSpanChange() for span: "
                    + span);
        }
    }
    
    /** 
     * Get the writing mode of the relevant reference area.
     * @return the applicable writing mode
     */
    public int getWritingMode() {
        return writingMode;
    }

    /** 
     * Set the writing mode.
     * @param writingMode the writing mode
     */
    public void setWritingMode(int writingMode) {
        this.writingMode = writingMode;
    }

    /**
     * Get the current amount of space before / start
     * @return the space before / start amount
     */
    public int getSpaceBefore() {
        return spaceBefore;
    }

    /**
     * Set the amount of space before / start
     * @param spaceBefore the amount of space before / start
     */
    public void setSpaceBefore(int spaceBefore) {
        this.spaceBefore = spaceBefore;
    }

    /**
     * Get the current amount of space after / end
     * @return the space after / end amount
     */
    public int getSpaceAfter() {
        return spaceAfter;
    }

    /**
     * Set the amount of space after / end
     * @param spaceAfter the amount of space after / end
     */
    public void setSpaceAfter(int spaceAfter) {
        this.spaceAfter = spaceAfter;
    }
    
    /** @see java.lang.Object#toString() */
    public String toString() {
        return "Layout Context:" +
        "\nStack Limit: \t" + (getStackLimit() == null ? "null" : getStackLimit().toString()) +
        "\nTrailing Space: \t" + (getTrailingSpace() == null ? "null" : getTrailingSpace().toString()) +
        "\nLeading Space: \t" + (getLeadingSpace() == null ? "null" : getLeadingSpace().toString()) + 
        "\nReference IPD: \t" + getRefIPD() +
        "\nSpace Adjust: \t" + getSpaceAdjust() + 
        "\nIPD Adjust: \t" + getIPDAdjust() +
        "\nResolve Leading Space: \t" + resolveLeadingSpace() +
        "\nSuppress Leading Space: \t" + suppressLeadingSpace() +
        "\nIs First Area: \t" + isFirstArea() + 
        "\nStarts New Area: \t" + startsNewArea() + 
        "\nIs Last Area: \t" + isLastArea() +
        "\nTry Hyphenate: \t" + tryHyphenate() + 
        "\nKeeps: \t[" + (isKeepWithNextPending() ? "keep-with-next" : "") + "][" 
            + (isKeepWithPreviousPending() ? "keep-with-previous" : "") + "] pending";
    }

}

