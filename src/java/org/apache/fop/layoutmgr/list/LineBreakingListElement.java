package org.apache.fop.layoutmgr.list;

import java.util.LinkedList;

import org.apache.fop.layoutmgr.ListElement;
import org.apache.fop.layoutmgr.Position;

public abstract class LineBreakingListElement extends ListElement {

    /**
     * Main constructor
     * @param position the Position instance needed by the addAreas stage of the LMs.
     */
    public LineBreakingListElement(Position position) {
        super(position);
    }

    /* (non-Javadoc)
     * @see org.apache.fop.layoutmgr.ListElement#isUnresolvedElement()
     */
    public boolean isUnresolvedElement() {
        return false;
    }

    public abstract LinkedList doLineBreaking();

    public abstract boolean lineBreakingIsFinished();

}