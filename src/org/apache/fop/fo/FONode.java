package org.apache.xml.fop.fo;

// FOP
import org.apache.xml.fop.apps.FOPException;
import org.apache.xml.fop.layout.Area;

// Java
import java.util.Vector;

/**
 * base class for nodes in the formatting object tree
 */
abstract public class FONode {

    protected FObj parent;
    protected Vector children = new Vector();

    /** value of marker before layout begins */
    public final static int START = -1000;

    /** value of marker after break-after */
    public final static int BREAK_AFTER = -1001;

    /** 
     * where the layout was up to.
     *  for FObjs it is the child number
     *  for FOText it is the character number
     */
    protected int marker = START;

    protected boolean isInLabel = false;
    protected boolean isInListBody = false;
    protected boolean isInTableCell = false;

    protected int bodyIndent;
    protected int distanceBetweenStarts;
    protected int labelSeparation;

    protected int forcedStartOffset = 0;
    protected int forcedWidth = 0;

    protected FONode(FObj parent) {
	this.parent = parent;
    }

    public void setIsInLabel() {
	this.isInLabel = true;
    }

    public void setIsInListBody() {
	this.isInListBody = true;
    }

    public void setIsInTableCell() {
	this.isInTableCell = true;
    }

    public void setDistanceBetweenStarts(int distance) {
	this.distanceBetweenStarts = distance;
    }

    public void setLabelSeparation(int separation) {
	this.labelSeparation = separation;
    }

    public void setBodyIndent(int indent) {
	this.bodyIndent = indent;
    }

    public void forceStartOffset(int offset) {
	this.forcedStartOffset = offset;
    }

    public void forceWidth(int width) {
	this.forcedWidth = width;
    }

    public void resetMarker() {
	this.marker = START;
	int numChildren = this.children.size();
	for (int i = 0; i < numChildren; i++) {
	    ((FONode) children.elementAt(i)).resetMarker();
	}
    }

    protected void addChild(FONode child) {
	children.addElement(child);
    }

    public FObj getParent() {
	return this.parent;
    }

    /* status */
    /* layout was fully completed */
    public final static int OK = 1;
    /* none of the formatting object could be laid out because the
       containing area was full (end of page) */
    public final static int AREA_FULL_NONE = 2;
    /* some of the formatting object could not be laid out because the
       containing area was full (end of page) */
    public final static int AREA_FULL_SOME = 3;
    /* force page break */
    public final static int FORCE_PAGE_BREAK = 4;
    public final static int FORCE_PAGE_BREAK_EVEN = 5;
    public final static int FORCE_PAGE_BREAK_ODD = 6;

    abstract public int layout(Area area)
	throws FOPException;
}
