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
import org.apache.fop.layout.Area;
import org.apache.fop.layout.AccessibilityProps;
import org.apache.fop.layout.AuralProps;
import org.apache.fop.layout.BorderAndPadding;
import org.apache.fop.layout.BackgroundProps;
import org.apache.fop.layout.MarginProps;
import org.apache.fop.layout.RelativePositionProps;
import org.apache.fop.layout.BlockArea;
import org.apache.fop.apps.FOPException;

public class ListBlock extends FObj {

    public static class Maker extends FObj.Maker {
        public FObj make(FObj parent, PropertyList propertyList,
                         String systemId, int line, int column)
            throws FOPException {
            return new ListBlock(parent, propertyList,
                                 systemId, line, column);
        }

    }

    public static FObj.Maker maker() {
        return new ListBlock.Maker();
    }

    int align;
    int alignLast;
    int breakBefore;
    int breakAfter;
    int lineHeight;
    int startIndent;
    int endIndent;
    int spaceBefore;
    int spaceAfter;
    int spaceBetweenListRows = 0;

    public ListBlock(FObj parent, PropertyList propertyList,
                     String systemId, int line, int column) {
        super(parent, propertyList, systemId, line, column);
    }

    public String getName() {
        return "fo:list-block";
    }

    public int layout(Area area) throws FOPException {
        if (this.marker == START) {

            // Common Accessibility Properties
            AccessibilityProps mAccProps = propMgr.getAccessibilityProps();

            // Common Aural Properties
            AuralProps mAurProps = propMgr.getAuralProps();

            // Common Border, Padding, and Background Properties
            BorderAndPadding bap = propMgr.getBorderAndPadding();
            BackgroundProps bProps = propMgr.getBackgroundProps();

            // Common Margin Properties-Block
            MarginProps mProps = propMgr.getMarginProps();

            // Common Relative Position Properties
            RelativePositionProps mRelProps = propMgr.getRelativePositionProps();

            // this.properties.get("break-after");
            // this.properties.get("break-before");
            // this.properties.get("id");
            // this.properties.get("keep-together");
            // this.properties.get("keep-with-next");
            // this.properties.get("keep-with-previous");
            // this.properties.get("provisional-distance-between-starts");
            // this.properties.get("provisional-label-separation");

            this.align = this.properties.get("text-align").getEnum();
            this.alignLast = this.properties.get("text-align-last").getEnum();
            this.lineHeight =
                this.properties.get("line-height").getLength().mvalue();
            this.startIndent =
                this.properties.get("start-indent").getLength().mvalue();
            this.endIndent =
                this.properties.get("end-indent").getLength().mvalue();
            this.spaceBefore =
                this.properties.get("space-before.optimum").getLength().mvalue();
            this.spaceAfter =
                this.properties.get("space-after.optimum").getLength().mvalue();
            this.spaceBetweenListRows = 0;    // not used at present

            this.marker = 0;

            if (area instanceof BlockArea) {
                area.end();
            }

            if (spaceBefore != 0) {
                area.addDisplaySpace(spaceBefore);
            }

            if (this.isInTableCell) {
                startIndent += forcedStartOffset;
                endIndent += area.getAllocationWidth() - forcedWidth
                             - forcedStartOffset;
            }

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

        BlockArea blockArea =
            new BlockArea(propMgr.getFontState(area.getFontInfo()),
                          area.getAllocationWidth(), area.spaceLeft(),
                          startIndent, endIndent, 0, align, alignLast,
                          lineHeight);
        // Fix links in lists in tables problem
        blockArea.setTableCellXOffset(area.getTableCellXOffset());
        blockArea.setGeneratedBy(this);
        this.areasGenerated++;
        if (this.areasGenerated == 1)
            blockArea.isFirst(true);
            // for normal areas this should be the only pair
//        blockArea.addLineagePair(this, this.areasGenerated);

        // markers
//         if (this.hasMarkers())
//             blockArea.addMarkers(this.getMarkers());


        blockArea.setParent(area);
        blockArea.setPage(area.getPage());
        blockArea.setBackground(propMgr.getBackgroundProps());
        blockArea.start();

        blockArea.setAbsoluteHeight(area.getAbsoluteHeight());
        blockArea.setIDReferences(area.getIDReferences());

        int numChildren = this.children.size();
        for (int i = this.marker; i < numChildren; i++) {
            if (!(children.get(i) instanceof ListItem)) {
                log.error("children of list-blocks must be list-items");
                return Status.OK;
            }
            ListItem listItem = (ListItem)children.get(i);
            int status;
            if (Status.isIncomplete((status = listItem.layout(blockArea)))) {
                if (status == Status.AREA_FULL_NONE && i > 0) {
                    status = Status.AREA_FULL_SOME;
                }
                this.marker = i;
                blockArea.end();
                area.addChild(blockArea);
                area.increaseHeight(blockArea.getHeight());
                return status;
            }
        }

        blockArea.end();
        area.addChild(blockArea);
        area.increaseHeight(blockArea.getHeight());

        if (spaceAfter != 0) {
            area.addDisplaySpace(spaceAfter);
        }

        if (area instanceof BlockArea) {
            area.start();
        }

        blockArea.isLast(true);
        return Status.OK;
    }

}
