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
import org.apache.fop.fo.properties.Position;
import org.apache.fop.datatypes.KeepValue;
import org.apache.fop.layout.Area;
import org.apache.fop.layout.AreaContainer;
import org.apache.fop.layout.AccessibilityProps;
import org.apache.fop.layout.AuralProps;
import org.apache.fop.layout.BorderAndPadding;
import org.apache.fop.layout.BackgroundProps;
import org.apache.fop.layout.RelativePositionProps;
import org.apache.fop.layout.BlockArea;
import org.apache.fop.apps.FOPException;

// Java
import java.util.ArrayList;

public abstract class AbstractTableBody extends FObj {

    int spaceBefore;
    int spaceAfter;
    String id;

    ArrayList columns;
    RowSpanMgr rowSpanMgr;    // manage information about spanning rows

    // public AreaContainer areaContainer;
    public java.lang.ref.WeakReference areaContainerRef;

    public AbstractTableBody(FObj parent, PropertyList propertyList,
                             String systemId, int line, int column)
        throws FOPException {
        super(parent, propertyList, systemId, line, column);
        if (!(parent instanceof Table)) {
          throw new FOPException("A table body must be child of fo:table,"
                                   + " not " + parent.getName(),
                                 systemId, line, column);
        }
    }

    public void setColumns(ArrayList columns) {
        this.columns = columns;
    }

    public void setYPosition(int value) {
        ((AreaContainer)areaContainerRef.get()).setYPosition(value);
    }

    public int getYPosition() {
        return ((AreaContainer)areaContainerRef.get()).getCurrentYPosition();
    }

    public int getHeight() {
      return ((AreaContainer)areaContainerRef.get()).getHeight() + spaceBefore + spaceAfter;
    }

    public int layout(Area area) throws FOPException {
        if (this.marker == BREAK_AFTER) {
            return Status.OK;
        }

        if (this.marker == START) {

            // Common Accessibility Properties
            AccessibilityProps mAccProps = propMgr.getAccessibilityProps();

            // Common Aural Properties
            AuralProps mAurProps = propMgr.getAuralProps();

            // Common Border, Padding, and Background Properties
            BorderAndPadding bap = propMgr.getBorderAndPadding();
            BackgroundProps bProps = propMgr.getBackgroundProps();

            // Common Relative Position Properties
            RelativePositionProps mRelProps = propMgr.getRelativePositionProps();

            // this.properties.get("id");

            this.spaceBefore =
                this.properties.get("space-before.optimum").getLength().mvalue();
            this.spaceAfter =
                this.properties.get("space-after.optimum").getLength().mvalue();
            this.id = this.properties.get("id").getString();

            try {
                area.getIDReferences().createID(id);
            }
            catch(FOPException e) {
                if (!e.isLocationSet()) {
                    e.setLocation(systemId, line, column);
                }
                throw e;
            }

            if (area instanceof BlockArea) {
                area.end();
            }

            if (rowSpanMgr == null) {
                rowSpanMgr = new RowSpanMgr(columns.size());
            }

            // if (this.isInListBody) {
            // startIndent += bodyIndent + distanceBetweenStarts;
            // }

            this.marker = 0;

        }

        if ((spaceBefore != 0) && (this.marker == 0)) {
            area.increaseHeight(spaceBefore);
        }

        if (marker == 0) {
            // configure id
            area.getIDReferences().configureID(id, area);
        }

        int spaceLeft = area.spaceLeft();

        /*
         * Note: the parent FO must be a Table. The parent Area is the Block
         * type area created by the Table, which is also a reference area.
         * The content "width" (IPD) of the TableBody is the same as that
         * of the containing table area, and its relative position is 0,0.
         * Strictly speaking (CR), this FO should generate no areas!
         */
        AreaContainer areaContainer =
            new AreaContainer(propMgr.getFontState(area.getFontInfo()), 0,
                              area.getContentHeight(),
                              area.getContentWidth(),    // IPD
        area.spaceLeft(), Position.RELATIVE);
        areaContainer.foCreator = this;                  // G Seshadri
        areaContainer.setPage(area.getPage());
        areaContainer.setParent(area);
        areaContainer.setBackground(propMgr.getBackgroundProps());
        areaContainer.setBorderAndPadding(propMgr.getBorderAndPadding());
        areaContainer.start();

        areaContainer.setAbsoluteHeight(area.getAbsoluteHeight());
        areaContainer.setIDReferences(area.getIDReferences());

        this.areaContainerRef = new java.lang.ref.WeakReference(areaContainer);

        ArrayList keepWith = new ArrayList();
        int numChildren = this.children.size();
        TableRow lastRow = null;
        boolean endKeepGroup = true;
        for (int i = this.marker; i < numChildren; i++) {
            Object child = children.get(i);
            if (child instanceof Marker) {
                ((Marker)child).layout(area);
                continue;
            }
            if (!(child instanceof TableRow)) {
                throw new FOPException("Currently only Table Rows are supported in table body, header and footer", systemId, line, column);
            }
            TableRow row = (TableRow)child;

            row.setRowSpanMgr(rowSpanMgr);
            row.setColumns(columns);
            row.doSetup(areaContainer);
            if (row.getKeepWithPrevious().getType()
                    != KeepValue.KEEP_WITH_AUTO && lastRow != null
                                                && keepWith.indexOf(lastRow)
                                                   == -1) {
                keepWith.add(lastRow);
            } else {
                /* This row has no keep-with-previous, or it is the first
                 * row in this area.
                 */
                if (endKeepGroup && keepWith.size() > 0) {
                    keepWith = new ArrayList();
                }
                // If we have composed at least one complete row which is not part
                // of a keep set, we can take following keeps into account again
                if (endKeepGroup && i > this.marker) {
                   rowSpanMgr.setIgnoreKeeps(false);
                }
            }

            /* Tell the row whether it is at the top of this area: if so, the row
             * should not honor keep-together.
             */
            boolean bRowStartsArea = (i == this.marker);
            if (bRowStartsArea == false && keepWith.size() > 0) {
                if (children.indexOf(keepWith.get(0)) == this.marker) {
                   bRowStartsArea = true;
                }
            }
            row.setIgnoreKeepTogether(bRowStartsArea  && startsAC(area));
            int status;
            if (Status.isIncomplete((status = row.layout(areaContainer)))) {
                // BUG!!! don't distinguish between break-before and after!
                if (Status.isPageBreak(status)) {
                    this.marker = i;
                    area.addChild(areaContainer);
                    // areaContainer.end();

                    area.increaseHeight(areaContainer.getHeight());
                    if (i == numChildren - 1) {
                        this.marker = BREAK_AFTER;
                        if (spaceAfter != 0) {
                            area.increaseHeight(spaceAfter);
                        }
                    }
                    return status;
                }
                if ((keepWith.size() > 0)
                    && (!rowSpanMgr.ignoreKeeps())) {
                    // && status.getCode() == Status.AREA_FULL_NONE
                    // FIXME!!! Handle rows spans!!!
                    row.removeLayout(areaContainer);
                    for (int j = 0; j < keepWith.size(); j++) {
                        TableRow tr = (TableRow)keepWith.get(j);
                        tr.removeLayout(areaContainer);
                        i--;
                    }
                    if (i == 0) {
                        resetMarker();

                        // Fix for infinite loop bug if keeps are too big for page
                        rowSpanMgr.setIgnoreKeeps(true);

                        return Status.AREA_FULL_NONE;
                    }
                }
                this.marker = i;
                if ((i != 0) && (status == Status.AREA_FULL_NONE)) {
                    status = Status.AREA_FULL_SOME;
                }
                if (!((i == 0) && (areaContainer.getContentHeight() <= 0))) {
                    area.addChild(areaContainer);
                    // areaContainer.end();

                    area.increaseHeight(areaContainer.getHeight());
                }

                // Fix for infinite loop bug if spanned rows are too big for page
                rowSpanMgr.setIgnoreKeeps(true);

                return status;
            } else if (status == Status.KEEP_WITH_NEXT
                       || rowSpanMgr.hasUnfinishedSpans()) {
                keepWith.add(row);
                endKeepGroup = false;
            } else {
                endKeepGroup = true;
            }
            lastRow = row;
            area.setMaxHeight(area.getMaxHeight() - spaceLeft
                              + areaContainer.getMaxHeight());
            spaceLeft = area.spaceLeft();
        }
        area.addChild(areaContainer);
        areaContainer.end();

        area.increaseHeight(areaContainer.getHeight());

        if (spaceAfter != 0) {
            area.increaseHeight(spaceAfter);
            area.setMaxHeight(area.getMaxHeight() - spaceAfter);
        }

        if (area instanceof BlockArea) {
            area.start();
        }

        return Status.OK;
    }

    public void removeLayout(Area area) {
        if (areaContainerRef != null) {
            area.removeChild((AreaContainer)areaContainerRef.get());
        }
        if (spaceBefore != 0) {
            area.increaseHeight(-spaceBefore);
        }
        if (spaceAfter != 0) {
            area.increaseHeight(-spaceAfter);
        }
        this.resetMarker();
        this.removeID(area.getIDReferences());
    }

    /**
     * Return true if the passed area is on the left edge of its nearest
     * absolute AreaContainer (generally a page column).
     */
    private boolean startsAC(Area area) {
        Area parent=null;

        while ((parent = area.getParent()) != null &&
               parent.hasNonSpaceChildren() == false) {
            // The area will be the first non-space child in its parent
            // Note: it's not added yet!
            if (parent instanceof AreaContainer &&
                ((AreaContainer)parent).getPosition() == Position.ABSOLUTE) {
                return true;
            }
            area = parent;
        }
        return false;
    }
}
