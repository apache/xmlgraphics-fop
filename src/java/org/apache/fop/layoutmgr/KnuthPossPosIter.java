package org.apache.fop.layoutmgr;

import java.util.List;

public class KnuthPossPosIter extends PositionIterator {

    private int iterCount;

    /**
     * Main constructor
     * @param bpList List of break possibilities
     * @param startPos starting position
     * @param endPos ending position
     */
    public KnuthPossPosIter(List bpList, int startPos, int endPos) {
        super(bpList.listIterator(startPos));
        iterCount = endPos - startPos;
    }

    // Check position < endPos
    
    /**
     * @see org.apache.fop.layoutmgr.PositionIterator#checkNext()
     */
    protected boolean checkNext() {
        if (iterCount > 0) {
            return super.checkNext();
        } else {
            endIter();
            return false;
        }
    }

    /**
     * @see java.util.Iterator#next()
     */
    public Object next() {
        --iterCount;
        return super.next();
    }

    public KnuthElement getKE() {
        return (KnuthElement) peekNext();
    }

    protected LayoutManager getLM(Object nextObj) {
        return ((KnuthElement) nextObj).getLayoutManager();
    }

    protected Position getPos(Object nextObj) {
        return ((KnuthElement) nextObj).getPosition();
    }
}
