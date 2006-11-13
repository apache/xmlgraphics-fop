package org.apache.fop.layoutmgr.breaking;

import org.apache.fop.layoutmgr.LayoutManager;
import org.apache.fop.layoutmgr.LeafPosition;

/**
 * Private class to store information about inline breaks.
 * Each value holds the start and end indexes into a List of
 * inline break positions.
 */
public class LineBreakPosition extends LeafPosition {
    /*
     * TODO vh: fields temporarily made public to ease the moving of
     * LineBreakPosition from a LineLayoutManager inner class to a top-level
     * class.
     */
    public int iParIndex; // index of the Paragraph this Position refers to
    public int iStartIndex; //index of the first element this Position refers to
    public int availableShrink;
    public int availableStretch;
    public int difference;
    public double dAdjust; // Percentage to adjust (stretch or shrink)
    public double ipdAdjust; // Percentage to adjust (stretch or shrink)
    public int startIndent;
    public int lineHeight;
    public int lineWidth;
    public int spaceBefore;
    public int spaceAfter;
    public int baseline;

    LineBreakPosition(LayoutManager lm, int index, int iStartIndex, int iBreakIndex,
                      int shrink, int stretch, int diff,
                      double ipdA, double adjust, int ind,
                      int lh, int lw, int sb, int sa, int bl) {
        super(lm, iBreakIndex);
        availableShrink = shrink;
        availableStretch = stretch;
        difference = diff;
        iParIndex = index;
        this.iStartIndex = iStartIndex;
        ipdAdjust = ipdA;
        dAdjust = adjust;
        startIndent = ind;
        lineHeight = lh;
        lineWidth = lw;
        spaceBefore = sb;
        spaceAfter = sa;
        baseline = bl;
    }
    
}