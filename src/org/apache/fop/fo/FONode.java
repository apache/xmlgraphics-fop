/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */


package org.apache.fop.fo;

// FOP
import org.apache.fop.apps.FOPException;
import org.apache.fop.layout.Area;
import org.apache.fop.layout.AreaClass;
import org.apache.fop.layout.LinkSet;
import org.apache.fop.system.BufferManager;
import org.apache.fop.fo.flow.Marker;

// Java
import java.util.Vector;
import java.util.Hashtable;

/**
 * base class for nodes in the formatting object tree
 *
 * Modified by Mark Lillywhite mark-fop@inomial.com. Made
 * Vector a protected member. (/me things this should be
 * a private member with an API for adding children;
 * this woudl save a lot of memory because the Vector
 * would not have to be instantiated unless the node had
 * children).
 */
abstract public class FONode {

    protected FObj parent;

    protected String areaClass = AreaClass.UNASSIGNED;

    public BufferManager bufferManager;

    protected Vector children = new Vector();    // made public for searching for id's

    /**
     * value of marker before layout begins
     */
    public final static int START = -1000;

    /**
     * value of marker after break-after
     */
    public final static int BREAK_AFTER = -1001;

    /**
     * where the layout was up to.
     * for FObjs it is the child number
     * for FOText it is the character number
     */
    protected int marker = START;

    protected boolean isInTableCell = false;

    protected int forcedStartOffset = 0;
    protected int forcedWidth = 0;

    protected int widows = 0;
    protected int orphans = 0;

    protected LinkSet linkSet;

    // count of areas generated-by/returned-by
    public int areasGenerated = 0;

    // markers
    protected Hashtable markers;

    protected FONode(FObj parent) {
        this.parent = parent;
        if (parent != null) {
            this.bufferManager = parent.bufferManager;
        }

        markers = new Hashtable();

        if (null != parent)
            this.areaClass = parent.areaClass;
    }

    public void setIsInTableCell() {
        this.isInTableCell = true;
        // made recursive by Eric Schaeffer
        for (int i = 0; i < this.children.size(); i++) {
            FONode child = (FONode)this.children.elementAt(i);
            child.setIsInTableCell();
        }
    }

    public void forceStartOffset(int offset) {
        this.forcedStartOffset = offset;
        // made recursive by Eric Schaeffer
        for (int i = 0; i < this.children.size(); i++) {
            FONode child = (FONode)this.children.elementAt(i);
            child.forceStartOffset(offset);
        }
    }

    public void forceWidth(int width) {
        this.forcedWidth = width;
        // made recursive by Eric Schaeffer
        for (int i = 0; i < this.children.size(); i++) {
            FONode child = (FONode)this.children.elementAt(i);
            child.forceWidth(width);
        }
    }

    public void resetMarker() {
        this.marker = START;
        int numChildren = this.children.size();
        for (int i = 0; i < numChildren; i++) {
            ((FONode)children.elementAt(i)).resetMarker();
        }
    }

    public void setWidows(int wid) {
        widows = wid;
    }

    public void setOrphans(int orph) {
        orphans = orph;
    }

    public void removeAreas() {
        // still to do
    }


    protected void addChild(FONode child) {
        children.addElement(child);
    }

    public FObj getParent() {
        return this.parent;
    }

    public void setBufferManager(BufferManager bufferManager) {
        this.bufferManager = bufferManager;
    }

    public BufferManager getBufferManager() {
        return this.bufferManager;
    }

    public void setLinkSet(LinkSet linkSet) {
        this.linkSet = linkSet;
        for (int i = 0; i < this.children.size(); i++) {
            FONode child = (FONode)this.children.elementAt(i);
            child.setLinkSet(linkSet);
        }
    }

    public LinkSet getLinkSet() {
        return this.linkSet;
    }

    abstract public Status layout(Area area) throws FOPException;

    /**
     * lets outside sources access the property list
     * first used by PageNumberCitation to find the "id" property
     * returns null by default, overide this function when there is a property list
     * @param name - the name of the desired property to obtain
     * @returns the property
     */
    public Property getProperty(String name) {
        return (null);
    }

    /**
     * At the start of a new span area layout may be partway through a
     * nested FO, and balancing requires rollback to this known point.
     * The snapshot records exactly where layout is at.
     * @param snapshot a Vector of markers (Integer)
     * @returns the updated Vector of markers (Integers)
     */
    public Vector getMarkerSnapshot(Vector snapshot) {
        snapshot.addElement(new Integer(this.marker));

        // terminate if no kids or child not yet accessed
        if (this.marker < 0)
            return snapshot;
        else if (children.isEmpty())
            return snapshot;
        else
            return ((FONode)children.elementAt(this.marker)).getMarkerSnapshot(snapshot);
    }

    /**
     * When balancing occurs, the flow layout() method restarts at the
     * point specified by the current marker snapshot, which is retrieved
     * and restored using this method.
     * @param snapshot the Vector of saved markers (Integers)
     */
    public void rollback(Vector snapshot) {
        this.marker = ((Integer)snapshot.elementAt(0)).intValue();
        snapshot.removeElementAt(0);

        if (this.marker == START) {
            // make sure all the children of this FO are also reset
            resetMarker();
            return;
        } else if ((this.marker == -1) || children.isEmpty())
            return;

        int numChildren = this.children.size();

        if (this.marker <= START) {
            return;
        }

        for (int i = this.marker + 1; i < numChildren; i++) {
            FONode fo = (FONode)children.elementAt(i);
            fo.resetMarker();
        }
        ((FONode)children.elementAt(this.marker)).rollback(snapshot);
    }


    public void addMarker(Marker marker) throws FOPException {
        String mcname = marker.getMarkerClassName();
        if (!markers.containsKey(mcname) && children.isEmpty())
            markers.put(mcname, marker);
        else
            throw new FOPException("fo:marker must be an initial child,"
                                   + "and 'marker-class-name' must be unique for same parent");
    }

    public boolean hasMarkers() {
        return !markers.isEmpty();
    }

    public Vector getMarkers() {
        return new Vector(markers.values());
    }

}
