/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.flow;

// FOP
import org.apache.fop.fo.*;
import org.apache.fop.fo.properties.*;
import org.apache.fop.fo.pagination.*;
import org.apache.fop.layout.Area;
import org.apache.fop.layout.BodyAreaContainer;
import org.apache.fop.apps.FOPException;

// Java
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;

public class Flow extends FObj {

    public static class Maker extends FObj.Maker {
        public FObj make(FObj parent,
                         PropertyList propertyList) throws FOPException {
            return new Flow(parent, propertyList);
        }

    }

    public static FObj.Maker maker() {
        return new Flow.Maker();
    }

    /**
     * PageSequence container
     */
    private PageSequence pageSequence;

    /**
     * Area in which we lay out our kids
     */
    private Area area;

    /**
     * Vector to store snapshot
     */
    private Vector markerSnapshot;

    /**
     * flow-name attribute
     */
    private String _flowName;

    private Status _status = new Status(Status.AREA_FULL_NONE);


    protected Flow(FObj parent,
                   PropertyList propertyList) throws FOPException {
        super(parent, propertyList);
        this.name = getElementName();

        if (parent.getName().equals("fo:page-sequence")) {
            this.pageSequence = (PageSequence)parent;
        } else {
            throw new FOPException("flow must be child of "
                                   + "page-sequence, not "
                                   + parent.getName());
        }
        setFlowName(getProperty("flow-name").getString());

        // according to communication from Paul Grosso (XSL-List,
        // 001228, Number 406), confusion in spec section 6.4.5 about
        // multiplicity of fo:flow in XSL 1.0 is cleared up - one (1)
        // fo:flow per fo:page-sequence only.

        if (pageSequence.isFlowSet()) {
            if (this.name.equals("fo:flow")) {
                throw new FOPException("Only a single fo:flow permitted"
                                       + " per fo:page-sequence");
            } else {
                throw new FOPException(this.name
                                       + " not allowed after fo:flow");
            }
        }
        pageSequence.addFlow(this);
    }

    protected void setFlowName(String name) throws FOPException {
        if (name == null || name.equals("")) {
            log.warn("A 'flow-name' is required for "
                                   + getElementName()
                                   + ". This constraint will be enforced in future versions of FOP");
            _flowName = "xsl-region-body";
        } else {
            _flowName = name;
        }

    }

    public String getFlowName() {
        return _flowName;
    }

    public Status layout(Area area) throws FOPException {
        return layout(area, null);

    }

    public Status layout(Area area, Region region) throws FOPException {
        if (this.marker == START) {
            this.marker = 0;
        }

        // flow is *always* laid out into a BodyAreaContainer
        BodyAreaContainer bac = (BodyAreaContainer)area;

        boolean prevChildMustKeepWithNext = false;
        Vector pageMarker = this.getMarkerSnapshot(new Vector());

        int numChildren = this.children.size();
        if (numChildren == 0) {
            throw new FOPException("fo:flow must contain block-level children");
        }
        for (int i = this.marker; i < numChildren; i++) {
            FObj fo = (FObj)children.elementAt(i);

            if (bac.isBalancingRequired(fo)) {
                // reset the the just-done span area in preparation
                // for a backtrack for balancing
                bac.resetSpanArea();

                this.rollback(markerSnapshot);
                // one less because of the "continue"
                i = this.marker - 1;
                continue;
            }
            // current column area
            Area currentArea = bac.getNextArea(fo);
            // temporary hack for IDReferences
            currentArea.setIDReferences(bac.getIDReferences());
            if (bac.isNewSpanArea()) {
                this.marker = i;
                markerSnapshot = this.getMarkerSnapshot(new Vector());
            }

            _status = fo.layout(currentArea);

            /*
             * if((_status.isPageBreak() || i == numChildren - 1) && bac.needsFootnoteAdjusting()) {
             * bac.adjustFootnoteArea();
             * this.rollback(pageMarker);
             * i = this.marker - 1;
             * Area mainReferenceArea = bac.getMainReferenceArea();
             * // remove areas
             * continue;
             * }
             */
            if (_status.isIncomplete()) {
                if ((prevChildMustKeepWithNext) && (_status.laidOutNone())) {
                    this.marker = i - 1;
                    FObj prevChild = (FObj)children.elementAt(this.marker);
                    prevChild.removeAreas();
                    prevChild.resetMarker();
                    prevChild.removeID(area.getIDReferences());
                    _status = new Status(Status.AREA_FULL_SOME);
                    return _status;
                    // should probably return AREA_FULL_NONE if first
                    // or perhaps an entirely new status code
                }
                if (bac.isLastColumn())
                    if (_status.getCode() == Status.FORCE_COLUMN_BREAK) {
                        this.marker = i;
                        _status =
                            new Status(Status.FORCE_PAGE_BREAK);    // same thing
                        return _status;
                    } else {
                        this.marker = i;
                        return _status;
                    }
                else {
                    // not the last column, but could be page breaks
                    if (_status.isPageBreak()) {
                        this.marker = i;
                        return _status;
                    }
                    // I don't much like exposing this. (AHS 001217)
                    ((org.apache.fop.layout.ColumnArea)currentArea).incrementSpanIndex();
                    i--;
                }
            }
            if (_status.getCode() == Status.KEEP_WITH_NEXT) {
                prevChildMustKeepWithNext = true;
            } else {
                prevChildMustKeepWithNext = false;
            }
        }
        return _status;
    }

    /**
     * Return the content width of this flow (really of the region
     * in which it is flowing).
     */
    public int getContentWidth() {
        if (area != null)
            return area.getContentWidth();    // getAllocationWidth()??
        else
            return 0;                         // not laid out yet
    }

    protected String getElementName() {
        return "fo:flow";
    }

    public Status getStatus() {
        return _status;
    }


    public boolean generatesReferenceAreas() {
        return true;
    }

}
