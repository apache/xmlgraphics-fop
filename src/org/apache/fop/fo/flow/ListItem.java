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

public class ListItem extends FObj {

    public static class Maker extends FObj.Maker {
        public FObj make(FObj parent, PropertyList propertyList,
                         String systemId, int line, int column)
            throws FOPException {
            return new ListItem(parent, propertyList, systemId, line, column);
        }

    }

    public static FObj.Maker maker() {
        return new ListItem.Maker();
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
    String id;
    BlockArea blockArea;

    public ListItem(FObj parent, PropertyList propertyList,
                    String systemId, int line, int column) {
        super(parent, propertyList, systemId, line, column);
    }

    public String getName() {
        return "fo:list-item";
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
            // this.properties.get("relative-align");

            this.align = this.properties.get("text-align").getEnum();
            this.alignLast = this.properties.get("text-align-last").getEnum();
            this.lineHeight =
                this.properties.get("line-height").getLength().mvalue();
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

            this.marker = 0;
        }

        /* not sure this is needed given we know area is from list block */
        if (area instanceof BlockArea) {
            area.end();
        }

        if (spaceBefore != 0) {
            area.addDisplaySpace(spaceBefore);
        }

        this.blockArea =
            new BlockArea(propMgr.getFontState(area.getFontInfo()),
                          area.getAllocationWidth(), area.spaceLeft(), 0, 0,
                          0, align, alignLast, lineHeight);

        // Fix links in lists in tables problem
        blockArea.setTableCellXOffset(area.getTableCellXOffset());

        this.blockArea.setGeneratedBy(this);
        this.areasGenerated++;
        if (this.areasGenerated == 1)
            this.blockArea.isFirst(true);
            // for normal areas this should be the only pair
//        this.blockArea.addLineagePair(this, this.areasGenerated);

        // markers
//         if (this.hasMarkers())
//             this.blockArea.addMarkers(this.getMarkers());

        blockArea.setParent(area);
        blockArea.setPage(area.getPage());
        blockArea.start();

        blockArea.setAbsoluteHeight(area.getAbsoluteHeight());
        blockArea.setIDReferences(area.getIDReferences());

        int numChildren = this.children.size();
        if (numChildren != 2) {
            throw new FOPException("list-item must have exactly two children",
                                   systemId, line, column);
        }
        ListItemLabel label = (ListItemLabel)children.get(0);
        ListItemBody body = (ListItemBody)children.get(1);

        int status;

        // what follows doesn't yet take into account whether the
        // body failed completely or only got some text in

        if (this.marker == 0) {
            // configure id
            area.getIDReferences().configureID(id, area);

            status = label.layout(blockArea);
            if (Status.isIncomplete(status)) {
                return status;
            }
        }

        status = body.layout(blockArea);
        if (Status.isIncomplete(status)) {
            blockArea.end();
            area.addChild(blockArea);
            area.increaseHeight(blockArea.getHeight());
            this.marker = 1;
            return status;
        }

        blockArea.end();
        area.addChild(blockArea);
        area.increaseHeight(blockArea.getHeight());

        if (spaceAfter != 0) {
            area.addDisplaySpace(spaceAfter);
        }

        /* not sure this is needed given we know area is from list block */
        if (area instanceof BlockArea) {
            area.start();
        }
        this.blockArea.isLast(true);
        return Status.OK;
    }

    /**
     * Return the content width of the boxes generated by this FO.
     */
    public int getContentWidth() {
        if (blockArea != null)
            return blockArea.getContentWidth();    // getAllocationWidth()??
        else
            return 0;                              // not laid out yet
    }

}
