/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.layoutmgr;

import java.util.List;
import java.util.ListIterator;

import org.apache.fop.area.Trait;
import org.apache.fop.area.inline.FilledArea;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.area.inline.Space;
import org.apache.fop.area.inline.TextArea;
import org.apache.fop.datatypes.Length;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.flow.Inline;
import org.apache.fop.fo.flow.Leader;
import org.apache.fop.fo.flow.Wrapper;
import org.apache.fop.traits.MinOptMax;
import org.apache.fop.fo.LMVisited;

/**
 * Visitor pattern for the purpose of adding
 * Layout Managers to nodes in the FOTree.
 * Each method is responsible to return a LayoutManager 
 * responsible for laying out this FObj's content.
 */
public class AddLMVisitor {

    /** The List object to which methods in this class should add Layout
     *  Managers */
    protected List currentLMList;

    /** A List object which can be used to save and restore the currentLMList if
     * another List should temporarily be used */
    protected List saveLMList;

    /**
     *
     * @param fobj the FObj object for which a layout manager should be created
     * @param lmList the list to which the newly created layout manager(s)
     * should be added
     */
    public void addLayoutManager(FObj fobj, List lmList) {
        /* Store the List in a global variable so that it can be accessed by the
           Visitor methods */
        currentLMList = lmList;
        if (fobj instanceof LMVisited) {
            ((LMVisited) fobj).acceptVisitor(this);
        } else {
            fobj.addLayoutManager(currentLMList);
        }
    }

    /**
     * Accessor for the currentLMList.
     * @return the currentLMList.
     */
    public List getCurrentLMList() {
        return currentLMList;
    }

    /**
     * Accessor for the saveLMList.
     * @return the saveLMList.
     */
    public List getSaveLMList() {
        return saveLMList;
    }

    /**
     * @param node Wrapper object to process
     */
    public void serveWrapper(Wrapper node) {
        ListIterator baseIter;
        baseIter = node.getChildNodes();
        if (baseIter == null) return;
        while (baseIter.hasNext()) {
            FObj child = (FObj) baseIter.next();
            if (child instanceof LMVisited) {
                ((LMVisited) child).acceptVisitor(this);
            } else {
                child.addLayoutManager(currentLMList);
            }
        }
    }

     public void serveLeader(final Leader node) {
         LeafNodeLayoutManager lm = new LeafNodeLayoutManager(node) {
             public InlineArea get(LayoutContext context) {
                 return getLeaderInlineArea(node, this);
             }

             protected MinOptMax getAllocationIPD(int refIPD) {
                return getLeaderAllocIPD(node, refIPD);
             }

             /*protected void offsetArea(LayoutContext context) {
                 if(leaderPattern == LeaderPattern.DOTS) {
                     curArea.setOffset(context.getBaseline());
                 }
             }*/
         };
         lm.setAlignment(node.getProperty(Constants.PR_LEADER_ALIGNMENT).getEnum());
         currentLMList.add(lm);
     }

     public MinOptMax getLeaderAllocIPD(Leader node, int ipd) {
         // length of the leader
         int opt = node.getLength(Constants.PR_LEADER_LENGTH | Constants.CP_OPTIMUM, ipd);
         int min = node.getLength(Constants.PR_LEADER_LENGTH | Constants.CP_MINIMUM, ipd);
         int max = node.getLength(Constants.PR_LEADER_LENGTH | Constants.CP_MAXIMUM, ipd);

         return new MinOptMax(min, opt, max);
     }

     private InlineArea getLeaderInlineArea(Leader node, LayoutManager parentLM) {
         InlineArea leaderArea = null;

         if (node.getLeaderPattern() == Constants.LeaderPattern.RULE) {
             org.apache.fop.area.inline.Leader leader = new org.apache.fop.area.inline.Leader();
             leader.setRuleStyle(node.getRuleStyle());
             leader.setRuleThickness(node.getRuleThickness());
             leaderArea = leader;
         } else if (node.getLeaderPattern() == Constants.LeaderPattern.SPACE) {
             leaderArea = new Space();
         } else if (node.getLeaderPattern() == Constants.LeaderPattern.DOTS) {
             TextArea t = new TextArea();
             char dot = '.'; // userAgent.getLeaderDotCharacter();

             t.setTextArea("" + dot);
             t.addTrait(Trait.FONT_NAME, node.getFontState().getFontName());
             t.addTrait(Trait.FONT_SIZE,
                              new Integer(node.getFontState().getFontSize()));
             // set offset of dot within inline parent
             t.setOffset(node.getFontState().getAscender());
             int width = node.getFontState().getCharWidth(dot);
             Space spacer = null;
             if (node.getPatternWidth() > width) {
                 spacer = new Space();
                 spacer.setWidth(node.getPatternWidth() - width);
                 width = node.getPatternWidth();
             }
             FilledArea fa = new FilledArea();
             fa.setUnitWidth(width);
             fa.addChild(t);
             if (spacer != null) {
                 fa.addChild(spacer);
             }
             fa.setHeight(node.getFontState().getAscender());

             leaderArea = fa;
         } else if (node.getLeaderPattern() == Constants.LeaderPattern.USECONTENT) {
             if (node.getChildNodes() == null) {
                 node.getLogger().error("Leader use-content with no content");
                 return null;
             }
             InlineStackingLayoutManager lm;
             lm = new InlineStackingLayoutManager(node);
             lm.setLMiter(new LMiter(lm, node.getChildNodes()));
             lm.initialize();

             // get breaks then add areas to FilledArea
             FilledArea fa = new FilledArea();

             ContentLayoutManager clm = new ContentLayoutManager(fa);
             clm.setParent(parentLM);
             clm.setUserAgent(node.getUserAgent());
             lm.setParent(clm);

             clm.fillArea(lm);
             int width = clm.getStackingSize();
             Space spacer = null;
             if (node.getPatternWidth() > width) {
                 spacer = new Space();
                 spacer.setWidth(node.getPatternWidth() - width);
                 width = node.getPatternWidth();
             }
             fa.setUnitWidth(width);
             if (spacer != null) {
                 fa.addChild(spacer);
             }
             leaderArea = fa;
         }
         return leaderArea;
     }
}
