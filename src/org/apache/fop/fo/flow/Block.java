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
import org.apache.fop.fo.FObjMixed;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.Status;
import org.apache.fop.fo.FONode;
import org.apache.fop.layout.Area;
import org.apache.fop.layout.BlockArea;
import org.apache.fop.layout.AccessibilityProps;
import org.apache.fop.layout.AuralProps;
import org.apache.fop.layout.BorderAndPadding;
import org.apache.fop.layout.BackgroundProps;
import org.apache.fop.layout.HyphenationProps;
import org.apache.fop.layout.MarginProps;
import org.apache.fop.layout.RelativePositionProps;
import org.apache.fop.apps.FOPException;

/*
  Modified by Mark Lillywhite mark-fop@inomial.com. The changes
  here are based on memory profiling and do not change functionality.
  Essentially, the Block object had a pointer to a BlockArea object
  that it created. The BlockArea was not referenced after the Block
  was finished except to determine the size of the BlockArea, however
  a reference to the BlockArea was maintained and this caused a lot of
  GC problems, and was a major reason for FOP memory leaks. So,
  the reference to BlockArea was made local, the required information
  is now stored (instead of a reference to the complex BlockArea object)
  and it appears that there are a lot of changes in this file, in fact
  there are only a few sematic changes; mostly I just got rid of
  "this." from blockArea since BlockArea is now local.
  */

public class Block extends FObjMixed {

    public static class Maker extends FObj.Maker {
        public FObj make(FObj parent, PropertyList propertyList,
                         String systemId, int line, int column)
            throws FOPException {
            return new Block(parent, propertyList, systemId, line, column);
        }

    }

    public static FObj.Maker maker() {
        return new Block.Maker();
    }

    int align;
    int alignLast;
    int breakAfter;
    int lineHeight;
    int startIndent;
    int endIndent;
    int spaceBefore;
    int spaceAfter;
    int textIndent;
    int keepWithNext;

    int areaHeight = 0;
    int contentWidth = 0;
    int infLoopThreshhold = 50;

    String id;
    int span;
    boolean breakStatusBeforeChecked = false;

    // this may be helpful on other FOs too
    boolean anythingLaidOut = false;
    //Added to see how long it's been since nothing was laid out.
    int noLayoutCount = 0;

    public Block(FObj parent, PropertyList propertyList,
                 String systemId, int line, int column)
        throws FOPException {
        super(parent, propertyList, systemId, line, column);
        this.span = this.properties.get("span").getEnum();
    }

    public String getName() {
        return "fo:block";
    }

    public int layout(Area area) throws FOPException {
        if (!breakStatusBeforeChecked) {
            breakStatusBeforeChecked = true;
            // no break if first in area tree, or leading in context
            // area
            int breakBeforeStatus = propMgr.checkBreakBefore(area);
            if (breakBeforeStatus != Status.OK) {
                return breakBeforeStatus;
            }
        }

        BlockArea blockArea;

        if (!anythingLaidOut) {
            noLayoutCount++;
        }
        if (noLayoutCount > infLoopThreshhold) {
            throw new FOPException(
                "No meaningful layout in block after many attempts.  "+
                "Infinite loop is assumed.  Processing halted.",
                systemId, line, column);
        }

        // log.error(" b:LAY[" + marker + "] ");

        if (this.marker == BREAK_AFTER) {
            return Status.OK;
        }

        if (this.marker == START) {
            noLayoutCount=0; // Reset the "loop counter".

            // Common Accessibility Properties
            AccessibilityProps mAccProps = propMgr.getAccessibilityProps();

            // Common Aural Properties
            AuralProps mAurProps = propMgr.getAuralProps();

            // Common Border, Padding, and Background Properties
            BorderAndPadding bap = propMgr.getBorderAndPadding();
            BackgroundProps bProps = propMgr.getBackgroundProps();

            // Common Font Properties
            //this.fontState = propMgr.getFontState(area.getFontInfo());

            // Common Margin Properties-Block
            MarginProps mProps = propMgr.getMarginProps();

            // Common Relative Position Properties
            RelativePositionProps mRelProps = propMgr.getRelativePositionProps();

            this.align = this.properties.get("text-align").getEnum();
            this.alignLast = this.properties.get("text-align-last").getEnum();
            this.breakAfter = this.properties.get("break-after").getEnum();
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
            this.textIndent =
                this.properties.get("text-indent").getLength().mvalue();
            this.keepWithNext =
                this.properties.get("keep-with-next").getEnum();

            this.id = this.properties.get("id").getString();

            if (area instanceof BlockArea) {
                area.end();
            }

            if (area.getIDReferences() != null) {
                try {
                    area.getIDReferences().createID(id);
                }
                catch(FOPException e) {
                    if (!e.isLocationSet()) {
                        e.setLocation(systemId, line, column);
                    }
                    throw e;
                }
            }

            this.marker = 0;
        }

        if ((spaceBefore != 0) && (this.marker == 0)) {
            area.addDisplaySpace(spaceBefore);
        }

        if (anythingLaidOut) {
            this.textIndent = 0;
        }

        if (marker == 0 && area.getIDReferences() != null) {
            area.getIDReferences().configureID(id, area);
        }

        int spaceLeft = area.spaceLeft();
        blockArea =
            new BlockArea(propMgr.getFontState(area.getFontInfo()),
                          area.getAllocationWidth(), area.spaceLeft(),
                          startIndent, endIndent, textIndent, align,
                          alignLast, lineHeight);
        blockArea.setGeneratedBy(this);
        this.areasGenerated++;
        if (this.areasGenerated == 1)
            blockArea.isFirst(true);
        // markers
//         if (this.hasMarkers())
//             blockArea.addMarkers(this.getMarkers());

        blockArea.setParent(area);    // BasicLink needs it
        blockArea.setPage(area.getPage());
        blockArea.setBackground(propMgr.getBackgroundProps());
        blockArea.setBorderAndPadding(propMgr.getBorderAndPadding());
        blockArea.setHyphenation(propMgr.getHyphenationProps());
        blockArea.start();

        blockArea.setAbsoluteHeight(area.getAbsoluteHeight());
        blockArea.setIDReferences(area.getIDReferences());

        blockArea.setTableCellXOffset(area.getTableCellXOffset());

        int numChildren = this.children.size();
        for (int i = this.marker; i < numChildren; i++) {
            FONode fo = (FONode)children.get(i);
            int status = fo.layout(blockArea);
            if (Status.isIncomplete(status)) {
                this.marker = i;
                if (status == Status.AREA_FULL_NONE) {
                    if (i == 0) {
                        // Nothing was laid out.
                        anythingLaidOut = false;
                        return status;
                    } else {
                        // A previous child has already been laid out.
                        area.addChild(blockArea);
                        area.setMaxHeight(area.getMaxHeight() - spaceLeft
                                          + blockArea.getMaxHeight());
                        area.increaseHeight(blockArea.getHeight());
                        anythingLaidOut = true;
                        return Status.AREA_FULL_SOME;
                    }
                }
                // Something has been laid out.
                area.addChild(blockArea);
                area.setMaxHeight(area.getMaxHeight() - spaceLeft
                                  + blockArea.getMaxHeight());
                area.increaseHeight(blockArea.getHeight());
                anythingLaidOut = true;
                return status;
            }
            anythingLaidOut = true;
        }

        blockArea.end();
        blockArea.isLast(true);
        area.addChild(blockArea);
        area.setMaxHeight(area.getMaxHeight() - spaceLeft
                          + blockArea.getMaxHeight());
        area.increaseHeight(blockArea.getHeight());

        if (spaceAfter != 0) {
            area.addDisplaySpace(spaceAfter);
        }

        if (area instanceof BlockArea) {
            area.start();
        }
        areaHeight= blockArea.getHeight();
        contentWidth= blockArea.getContentWidth();

        // no break if last in area tree, or trailing in context
        // area
        int breakAfterStatus = propMgr.checkBreakAfter(area);
        if (breakAfterStatus != Status.OK) {
            this.marker = BREAK_AFTER;
            blockArea = null;
            return breakAfterStatus;
        }
        if (keepWithNext != 0) {
            return Status.KEEP_WITH_NEXT;
        }
        return Status.OK;
    }

    public int getAreaHeight() {
        return areaHeight;
    }


    /**
     * Return the content width of the boxes generated by this FO.
     */
    public int getContentWidth() {
        return contentWidth;    // getAllocationWidth()??
    }


    public int getSpan() {
        return this.span;
    }

    public void resetMarker() {
        anythingLaidOut = false;
        super.resetMarker();
    }

}
