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
import org.apache.fop.datatypes.*;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.layout.*;
import org.apache.fop.layout.BlockArea;
import org.apache.fop.layout.inline.LeaderArea;
import org.apache.fop.layout.LineArea;
import org.apache.fop.layout.FontState;
import org.apache.fop.apps.FOPException;
import org.apache.fop.layoutmgr.LayoutManager;
import org.apache.fop.layoutmgr.LeafNodeLayoutManager;
import org.apache.fop.area.MinOptMax;

import java.util.List;

/**
 * Implements fo:leader; main property of leader leader-pattern.
 * The following patterns are treated: rule, space, dots.
 * The pattern use-content is ignored, i.e. it still must be implemented.
 */
public class Leader extends FObjMixed {

    public Leader(FONode parent) {
        super(parent);
    }

    public void addLayoutManager(List list) {
        list.add(new LeafNodeLayoutManager(this) {
            public InlineArea get(int index) {
                if(index > 0)
                    return null;
                int contentIPD = parentLM.getContentIPD();
                return getInlineArea(contentIPD);
            }
        });
    }

    protected InlineArea getInlineArea(int maxIPD) {
        org.apache.fop.area.inline.Leader leader = new org.apache.fop.area.inline.Leader();
        leader.setWidth(maxIPD / 2);
        leader.setAllocationIPD(new MinOptMax(0, maxIPD / 2, maxIPD));
        return leader;
    }

    public Status layout(Area area) throws FOPException {
        BlockArea blockArea;
        // restriction in this version
        if (!(area instanceof BlockArea)) {
            log.warn("in this version of Fop fo:leader must be a direct child of fo:block ");
            return new Status(Status.OK);
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
        int leaderLengthOptimum =
            this.properties.get("leader-length.optimum").getLength().mvalue();
        int leaderLengthMinimum =
            this.properties.get("leader-length.minimum").getLength().mvalue();
        Length maxlength = this.properties.get("leader-length.maximum").getLength();
        int leaderLengthMaximum;
        if(maxlength instanceof PercentLength) {
            leaderLengthMaximum = (int)(((PercentLength)maxlength).value()
                                      * area.getAllocationWidth());
        } else {
            leaderLengthMaximum = maxlength.mvalue();
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
        blockArea.getIDReferences().initializeID(id, blockArea);

        // adds leader to blockarea, there the leaderArea is generated
        int succeeded = addLeader(blockArea,
                                  propMgr.getFontState(area.getFontInfo()),
                                  red, green, blue, leaderPattern,
                                  leaderLengthMinimum, leaderLengthOptimum,
                                  leaderLengthMaximum, ruleThickness,
                                  ruleStyle, leaderPatternWidth,
                                  leaderAlignment);
        if (succeeded == 1) {
            return new Status(Status.OK);
        } else {
            // not sure that this is the correct Status here
            return new Status(Status.AREA_FULL_SOME);
        }
    }

    /*
     * //should only be necessary for use-content
     * protected void addCharacters(char data[], int start, int length) {
     * FOText textNode = new FOText(data,start,length, this);
     * textNode.setLogger(log);
     * children.addElement(textNode);
     * }
     */


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
        if (leaderLengthOptimum <= (la.getRemainingWidth())) {
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
                             leaderLengthOptimum, leaderLengthMaximum,
                             ruleStyle, ruleThickness, leaderPatternWidth,
                             leaderAlignment);
            }
        }
        // this.hasLines = true;
        return 1;
    }
}

