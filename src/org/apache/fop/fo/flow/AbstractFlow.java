/*
 * $Id$
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 *
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 *
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */
package org.apache.fop.fo.flow;

// FOP
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.Status;
import org.apache.fop.fo.pagination.PageSequence;
import org.apache.fop.fo.pagination.Region;
import org.apache.fop.layout.Area;
import org.apache.fop.layout.BodyAreaContainer;
import org.apache.fop.apps.FOPException;

// Java
import java.util.ArrayList;

public abstract class AbstractFlow extends FObj {

    /**
     * PageSequence container
     */
    protected PageSequence pageSequence;

    /**
     * Area in which we lay out our kids
     */
    private Area area;

    /**
     * ArrayList to store snapshot
     */
    private ArrayList markerSnapshot;

    /**
     * flow-name attribute
     */
    protected String _flowName;

    /**
     * Content-width of current column area during layout
     */
    private int contentWidth;

    private int _status = Status.AREA_FULL_NONE;


    protected AbstractFlow(FObj parent, PropertyList propertyList,
                           String systemId, int line, int column) throws FOPException {
        super(parent, propertyList, systemId, line, column);

        if (parent.getName().equals("fo:page-sequence")) {
            this.pageSequence = (PageSequence)parent;
        } else {
            throw new FOPException("flow must be child of page-sequence, not "
                                   + parent.getName(), systemId, line, column);
        }
    }

    public String getFlowName() {
        return _flowName;
    }

    public int layout(Area area) throws FOPException {
        return layout(area, null);

    }

    public int layout(Area area, Region region) throws FOPException {
        if (this.marker == START) {
            this.marker = 0;
        }

        // flow is *always* laid out into a BodyAreaContainer
        BodyAreaContainer bac = (BodyAreaContainer)area;

        boolean prevChildMustKeepWithNext = false;
        ArrayList pageMarker = this.getMarkerSnapshot(new ArrayList());

        int numChildren = this.children.size();
        if (numChildren == 0) {
            throw new FOPException("fo:flow must contain block-level children",
                                   systemId, line, column);
        }
        for (int i = this.marker; i < numChildren; i++) {
            FObj fo = (FObj)children.get(i);

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
                markerSnapshot = this.getMarkerSnapshot(new ArrayList());
            }
            // Set current content width for percent-based lengths in children
            setContentWidth(currentArea.getContentWidth());

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
            if (Status.isIncomplete(_status)) {
                if ((prevChildMustKeepWithNext) && (Status.laidOutNone(_status))) {
                    this.marker = i - 1;
                    FObj prevChild = (FObj)children.get(this.marker);
                    prevChild.removeAreas();
                    prevChild.resetMarker();
                    prevChild.removeID(area.getIDReferences());
                    _status = Status.AREA_FULL_SOME;
                    return _status;
                    // should probably return AREA_FULL_NONE if first
                    // or perhaps an entirely new status code
                }
                if (bac.isLastColumn())
                    if (_status == Status.FORCE_COLUMN_BREAK) {
                        this.marker = i;
                        _status = Status.FORCE_PAGE_BREAK;    // same thing
                        return _status;
                    } else {
                        this.marker = i;
                        return _status;
                    }
                else {
                    // not the last column, but could be page breaks
                    if (Status.isPageBreak(_status)) {
                        this.marker = i;
                        return _status;
                    }
                    // I don't much like exposing this. (AHS 001217)
                    ((org.apache.fop.layout.ColumnArea)currentArea).incrementSpanIndex();
                    i--;
                }
            }
            if (_status == Status.KEEP_WITH_NEXT) {
                prevChildMustKeepWithNext = true;
            } else {
                prevChildMustKeepWithNext = false;
            }
        }
        return _status;
    }

    protected void setContentWidth(int contentWidth) {
        this.contentWidth = contentWidth;
    }
    /**
     * Return the content width of this flow (really of the region
     * in which it is flowing).
     */
    public int getContentWidth() {
        return this.contentWidth;
    }

    public int getStatus() {
        return _status;
    }


    public boolean generatesReferenceAreas() {
        return true;
    }

}
