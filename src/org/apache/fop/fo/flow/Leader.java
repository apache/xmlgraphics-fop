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
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.PercentLength;
import org.apache.fop.layout.Area;
import org.apache.fop.layout.AccessibilityProps;
import org.apache.fop.layout.AuralProps;
import org.apache.fop.layout.BorderAndPadding;
import org.apache.fop.layout.BackgroundProps;
import org.apache.fop.layout.MarginInlineProps;
import org.apache.fop.layout.RelativePositionProps;
import org.apache.fop.layout.BlockArea;
import org.apache.fop.layout.LineArea;
import org.apache.fop.layout.FontState;
import org.apache.fop.apps.FOPException;

/**
 * Implements fo:leader; main property of leader leader-pattern.
 * The following patterns are treated: rule, space, dots.
 * The pattern use-content is ignored, i.e. it still must be implemented.
 */

public class Leader extends FObjMixed {

    public static class Maker extends FObj.Maker {
        public FObj make(FObj parent, PropertyList propertyList,
                         String systemId, int line, int column)
            throws FOPException {
            return new Leader(parent, propertyList, systemId, line, column);
        }

    }

    public static FObj.Maker maker() {
        return new Leader.Maker();
    }

    public Leader(FObj parent, PropertyList propertyList,
                  String systemId, int line, int column)
        throws FOPException {
        super(parent, propertyList, systemId, line, column);
    }

    public String getName() {
        return "fo:leader";
    }

    public int layout(Area area) throws FOPException {
        BlockArea blockArea;
        // restriction in this version
        if (!(area instanceof BlockArea)) {
            log.warn("in this version of Fop fo:leader must be a direct child of fo:block ");
            return Status.OK;
        } else {
            blockArea = (BlockArea)area;
        }

        // Common Accessibility Properties
        AccessibilityProps mAccProps = propMgr.getAccessibilityProps();

        // Common Aural Properties
        AuralProps mAurProps = propMgr.getAuralProps();

        // Common Border, Padding, and Background Properties
        BorderAndPadding bap = propMgr.getBorderAndPadding();
        BackgroundProps bProps = propMgr.getBackgroundProps();

        // Common Font Properties
        //this.fontState = propMgr.getFontState(area.getFontInfo());

        // Common Margin Properties-Inline
        MarginInlineProps mProps = propMgr.getMarginInlineProps();

        // Common Relative Position Properties
        RelativePositionProps mRelProps = propMgr.getRelativePositionProps();

        // this.properties.get("alignment-adjust");
        // this.properties.get("alignment-baseline");
        // this.properties.get("baseline-shift");
        // this.properties.get("color");
        // this.properties.get("dominant-baseline");
        // this.properties.get("text-depth");
        // this.properties.get("text-altitude");
        // this.properties.get("id");
        // this.properties.get("leader-alignment");
        // this.properties.get("leader-length");
        // this.properties.get("leader-pattern");
        // this.properties.get("leader-pattern-width");
        // this.properties.get("rule-style");
        // this.properties.get("rule-thickness");
        // this.properties.get("letter-spacing");
        // this.properties.get("line-height");
        // this.properties.get("line-height-shift-adjustment");
        // this.properties.get("text-shadow");
        // this.properties.get("visibility");
        // this.properties.get("word-spacing");
        // this.properties.get("z-index");

        // color properties
        ColorType c = this.properties.get("color").getColorType();
        float red = c.red();
        float green = c.green();
        float blue = c.blue();

        // fo:leader specific properties
        // determines the pattern of leader; allowed values: space, rule,dots, use-content
        int leaderPattern = this.properties.get("leader-pattern").getEnum();
        // length of the leader
        Length length = this.properties.get("leader-length.minimum").getLength();
        int leaderLengthMinimum;
        if (length instanceof PercentLength) {
            leaderLengthMinimum = (int)(((PercentLength)length).value()
                                        * area.getAllocationWidth());
        } else {
            leaderLengthMinimum = length.mvalue();
        }
        length = this.properties.get("leader-length.optimum").getLength();
        int leaderLengthOptimum;
        if (length instanceof PercentLength) {
            leaderLengthOptimum = (int)(((PercentLength)length).value()
                                      * area.getAllocationWidth());
        } else {
            leaderLengthOptimum = length.mvalue();
        }
        length = this.properties.get("leader-length.maximum").getLength();
        int leaderLengthMaximum;
        if (length instanceof PercentLength) {
            leaderLengthMaximum = (int)(((PercentLength)length).value()
                                      * area.getAllocationWidth());
        } else {
            leaderLengthMaximum = length.mvalue();
        }
        // the following properties only apply for leader-pattern = "rule"
        int ruleThickness =
            this.properties.get("rule-thickness").getLength().mvalue();
        int ruleStyle = this.properties.get("rule-style").getEnum();
        // if leaderPatternWidth = 0 = default = use-font-metric
        int leaderPatternWidth =
            this.properties.get("leader-pattern-width").getLength().mvalue();
        int leaderAlignment =
            this.properties.get("leader-alignment").getEnum();

        // initialize id
        String id = this.properties.get("id").getString();
        try {
            blockArea.getIDReferences().initializeID(id, blockArea);
        }
        catch(FOPException e) {
            if (!e.isLocationSet()) {
                e.setLocation(systemId, line, column);
            }
            throw e;
        }

        // adds leader to blockarea, there the leaderArea is generated
        int succeeded = addLeader(blockArea,
                                  propMgr.getFontState(area.getFontInfo()),
                                  red, green, blue, leaderPattern,
                                  leaderLengthMinimum, leaderLengthOptimum,
                                  leaderLengthMaximum, ruleThickness,
                                  ruleStyle, leaderPatternWidth,
                                  leaderAlignment);
        if (succeeded == 1) {
            return Status.OK;
        } else {
            // not sure that this is the correct Status here
            return Status.AREA_FULL_SOME;
        }
    }

    /**
     * adds a leader to current line area of containing block area
     * the actual leader area is created in the line area
     *
     * @return int +1 for success and -1 for none
     */
    public int addLeader(BlockArea ba, FontState fontState, float red,
                         float green, float blue, int leaderPattern,
                         int leaderLengthMinimum, int leaderLengthOptimum,
                         int leaderLengthMaximum, int ruleThickness,
                         int ruleStyle, int leaderPatternWidth,
                         int leaderAlignment) {

        LineArea la = ba.getCurrentLineArea();
        // this should start a new page
        if (la == null) {
            return -1;
        }

        la.changeFont(fontState);
        la.changeColor(red, green, blue);

        // check whether leader fits into the (rest of the) line
        // using length.optimum to determine where to break the line as defined
        // in the xsl:fo spec: "User agents may choose to use the value of 'leader-length.optimum'
        // to determine where to break the line" (7.20.4)
        // if leader is longer then create a new LineArea and put leader there
        if (leaderLengthOptimum <= la.getRemainingWidth()) {
            la.addLeader(leaderPattern, leaderLengthMinimum,
                         leaderLengthOptimum, leaderLengthMaximum, ruleStyle,
                         ruleThickness, leaderPatternWidth, leaderAlignment);
        } else {
            la = ba.createNextLineArea();
            if (la == null) {
                // not enough room
                return -1;
            }
            la.changeFont(fontState);
            la.changeColor(red, green, blue);

            // check whether leader fits into LineArea at all, otherwise
            // clip it (should honor the clip option of containing area)
            if (leaderLengthMinimum <= la.getContentWidth()) {
                la.addLeader(leaderPattern, leaderLengthMinimum,
                             leaderLengthOptimum, leaderLengthMaximum,
                             ruleStyle, ruleThickness, leaderPatternWidth,
                             leaderAlignment);
            } else {
                log.error("Leader doesn't fit into line, it will be clipped to fit.");
                la.addLeader(leaderPattern, la.getRemainingWidth(),
                             la.getRemainingWidth(), la.getRemainingWidth(),
                             ruleStyle, ruleThickness, leaderPatternWidth,
                             leaderAlignment);
            }
        }
        return 1;
    }

}
