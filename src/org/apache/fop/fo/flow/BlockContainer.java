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
import org.apache.fop.fo.pagination.PageSequence;
import org.apache.fop.layout.Area;
import org.apache.fop.layout.AreaContainer;
import org.apache.fop.layout.AbsolutePositionProps;
import org.apache.fop.layout.BorderAndPadding;
import org.apache.fop.layout.BackgroundProps;
import org.apache.fop.layout.MarginProps;
import org.apache.fop.apps.FOPException;

public class BlockContainer extends FObj {

    int position;

    int top;
    int bottom;
    int left;
    int right;
    int width;
    int height;

    int span;

    AreaContainer areaContainer;

    public static class Maker extends FObj.Maker {
        public FObj make(FObj parent, PropertyList propertyList,
                         String systemId, int line, int column)
            throws FOPException {
            return new BlockContainer(parent, propertyList,
                                      systemId, line, column);
        }

    }

    public static FObj.Maker maker() {
        return new BlockContainer.Maker();
    }

    PageSequence pageSequence;

    protected BlockContainer(FObj parent, PropertyList propertyList,
                             String systemId, int line, int column)
        throws FOPException {
        super(parent, propertyList, systemId, line, column);
        this.span = this.properties.get("span").getEnum();
    }

    public String getName() {
        return "fo:block-container";
    }

    public int layout(Area area) throws FOPException {
        if (this.marker == START) {

            // Common Accessibility Properties
            AbsolutePositionProps mAbsProps = propMgr.getAbsolutePositionProps();

            // Common Border, Padding, and Background Properties
            BorderAndPadding bap = propMgr.getBorderAndPadding();
            BackgroundProps bProps = propMgr.getBackgroundProps();

            // Common Margin-Block Properties
            MarginProps mProps = propMgr.getMarginProps();

            // this.properties.get("block-progression-dimension");
            // this.properties.get("break-after");
            // this.properties.get("break-before");
            // this.properties.get("clip");
            // this.properties.get("display-align");
            // this.properties.get("height");
            // this.properties.get("id");
            // this.properties.get("keep-together");
            // this.properties.get("keep-with-next");
            // this.properties.get("keep-with-previous");
            // this.properties.get("overflow");
            // this.properties.get("reference-orientation");
            // this.properties.get("span");
            // this.properties.get("width");
            // this.properties.get("writing-mode");

            this.marker = 0;

            this.position = this.properties.get("position").getEnum();
            this.top = this.properties.get("top").getLength().mvalue();
            this.bottom = this.properties.get("bottom").getLength().mvalue();
            this.left = this.properties.get("left").getLength().mvalue();
            this.right = this.properties.get("right").getLength().mvalue();
            this.width = this.properties.get("width").getLength().mvalue();
            this.height = this.properties.get("height").getLength().mvalue();
            span = this.properties.get("span").getEnum();

            // initialize id
            String id = this.properties.get("id").getString();
            try {
                area.getIDReferences().initializeID(id, area);
            }
            catch(FOPException e) {
                if (!e.isLocationSet()) {
                    e.setLocation(systemId, line, column);
                }
                throw e;
            }
        }

        boolean prevChildMustKeepWithNext = false;

        AreaContainer container = (AreaContainer)area;
        if ((this.width == 0) && (this.height == 0)) {
            width = right - left;
            height = bottom - top;
        }

        this.areaContainer =
            new AreaContainer(propMgr.getFontState(container.getFontInfo()),
                              container.getXPosition() + left,
                              container.getYPosition() - top, width, height,
                              position);

        areaContainer.setPage(area.getPage());
        areaContainer.setBackground(propMgr.getBackgroundProps());
        areaContainer.setBorderAndPadding(propMgr.getBorderAndPadding());
        areaContainer.start();

        //areaContainer.setAbsoluteHeight(top);
        areaContainer.setAbsoluteHeight(0);
        areaContainer.setIDReferences(area.getIDReferences());

        int numChildren = this.children.size();
        for (int i = this.marker; i < numChildren; i++) {
            FObj fo = (FObj)children.get(i);
            int status;
            if (Status.isIncomplete((status = fo.layout(areaContainer)))) {
                /*
                 * if ((prevChildMustKeepWithNext) && (status.laidOutNone())) {
                 * this.marker = i - 1;
                 * FObj prevChild = (FObj) children.get(this.marker);
                 * prevChild.removeAreas();
                 * prevChild.resetMarker();
                 * return new Status(Status.AREA_FULL_SOME);
                 * // should probably return AREA_FULL_NONE if first
                 * // or perhaps an entirely new status code
                 * } else {
                 * this.marker = i;
                 * return status;
                 * }
                 */
            }
            if (status == Status.KEEP_WITH_NEXT) {
                prevChildMustKeepWithNext = true;
            }
        }

        areaContainer.end();
        if (position == Position.ABSOLUTE)
            areaContainer.setHeight(height);
        area.addChild(areaContainer);

        return Status.OK;
    }

    /**
     * Return the content width of the boxes generated by this block
     * container FO.
     */
    public int getContentWidth() {
        if (areaContainer != null)
            return areaContainer.getContentWidth();    // getAllocationWidth()??
        else
            return 0;    // not laid out yet
    }

    public boolean generatesReferenceAreas() {
        return true;
    }

    public int getSpan() {
        return this.span;
    }

}
