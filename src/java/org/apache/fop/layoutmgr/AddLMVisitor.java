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

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.apache.fop.area.Trait;
import org.apache.fop.area.inline.FilledArea;
import org.apache.fop.area.inline.ForeignObject;
import org.apache.fop.area.inline.Image;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.area.inline.InlineParent;
import org.apache.fop.area.inline.Space;
import org.apache.fop.area.inline.UnresolvedPageNumber;
import org.apache.fop.area.inline.Viewport;
import org.apache.fop.area.inline.TextArea;
import org.apache.fop.datatypes.Length;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.XMLObj;
import org.apache.fop.fo.flow.Block;
import org.apache.fop.fo.flow.Character;
import org.apache.fop.fo.flow.Inline;
import org.apache.fop.fo.flow.InstreamForeignObject;
import org.apache.fop.fo.flow.Leader;
import org.apache.fop.fo.flow.PageNumber;
import org.apache.fop.fo.flow.RetrieveMarker;
import org.apache.fop.fo.flow.Table;
import org.apache.fop.fo.flow.TableAndCaption;
import org.apache.fop.fo.flow.TableBody;
import org.apache.fop.fo.flow.TableCell;
import org.apache.fop.fo.flow.TableColumn;
import org.apache.fop.fo.flow.TableFooter;
import org.apache.fop.fo.flow.TableHeader;
import org.apache.fop.fo.flow.TableRow;
import org.apache.fop.fo.flow.Wrapper;
import org.apache.fop.fo.pagination.Title;
import org.apache.fop.fo.properties.CommonBackground;
import org.apache.fop.fo.properties.CommonBorderAndPadding;
import org.apache.fop.layoutmgr.table.Body;
import org.apache.fop.layoutmgr.table.Column;
import org.apache.fop.layoutmgr.table.TableLayoutManager;
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

     public void serveRetrieveMarker(RetrieveMarker node) {
         RetrieveMarkerLayoutManager rmlm;
         rmlm = new RetrieveMarkerLayoutManager(node);
         currentLMList.add(rmlm);
     }

     public void serveCharacter(Character node) {
         InlineArea inline = getCharacterInlineArea(node);
         if (inline != null) {
             LeafNodeLayoutManager lm = new LeafNodeLayoutManager(node);
             lm.setCurrentArea(inline);
             currentLMList.add(lm);
         }
     }

     public InlineArea getCharacterInlineArea(Character node) {
         String str = node.getProperty(Constants.PR_CHARACTER).getString();
         if (str.length() == 1) {
             org.apache.fop.area.inline.Character ch =
               new org.apache.fop.area.inline.Character(
                 str.charAt(0));
             return ch;
         }
         return null;
     }

     public void serveInstreamForeignObject(InstreamForeignObject node) {
         Viewport areaCurrent = getInstreamForeignObjectInlineArea(node);
         if (areaCurrent != null) {
             LeafNodeLayoutManager lm = new LeafNodeLayoutManager(node);
             lm.setCurrentArea(areaCurrent);
             lm.setAlignment(node.getProperty(Constants.PR_VERTICAL_ALIGN).getEnum());
             lm.setLead(areaCurrent.getHeight());
             currentLMList.add(lm);
         }
     }
     /**
      * Get the inline area created by this element.
      *
      * @return the viewport inline area
      */
     public Viewport getInstreamForeignObjectInlineArea(InstreamForeignObject node) {
         if (node.getChildNodes() == null) {
             return null;
         }

         if (node.childNodes.size() != 1) {
             // error
             return null;
         }
         FONode fo = (FONode) node.childNodes.get(0);
         if (!(fo instanceof XMLObj)) {
             // error
             return null;
         }
         XMLObj child = (XMLObj)fo;

         // viewport size is determined by block-progression-dimension
         // and inline-progression-dimension

         // if replaced then use height then ignore block-progression-dimension
         //int h = this.propertyList.get("height").getLength().mvalue();

         // use specified line-height then ignore dimension in height direction
         boolean hasLH = false;//propertyList.get("line-height").getSpecifiedValue() != null;

         Length len;

         int bpd = -1;
         int ipd = -1;
         boolean bpdauto = false;
         if (hasLH) {
             bpd = node.getProperty(Constants.PR_LINE_HEIGHT).getLength().getValue();
         } else {
             // this property does not apply when the line-height applies
             // isn't the block-progression-dimension always in the same
             // direction as the line height?
             len = node.getProperty(Constants.PR_BLOCK_PROGRESSION_DIMENSION | Constants.CP_OPTIMUM).getLength();
             if (!len.isAuto()) {
                 bpd = len.getValue();
             } else {
                 len = node.getProperty(Constants.PR_HEIGHT).getLength();
                 if (!len.isAuto()) {
                     bpd = len.getValue();
                 }
             }
         }

         len = node.getProperty(Constants.PR_INLINE_PROGRESSION_DIMENSION | Constants.CP_OPTIMUM).getLength();
         if (!len.isAuto()) {
             ipd = len.getValue();
         } else {
             len = node.getProperty(Constants.PR_WIDTH).getLength();
             if (!len.isAuto()) {
                 ipd = len.getValue();
             }
         }

         // if auto then use the intrinsic size of the content scaled
         // to the content-height and content-width
         int cwidth = -1;
         int cheight = -1;
         len = node.getProperty(Constants.PR_CONTENT_WIDTH).getLength();
         if (!len.isAuto()) {
             /*if(len.scaleToFit()) {
                 if(ipd != -1) {
                     cwidth = ipd;
                 }
             } else {*/
             cwidth = len.getValue();
         }
         len = node.getProperty(Constants.PR_CONTENT_HEIGHT).getLength();
         if (!len.isAuto()) {
             /*if(len.scaleToFit()) {
                 if(bpd != -1) {
                     cwidth = bpd;
                 }
             } else {*/
             cheight = len.getValue();
         }

         Point2D csize = new Point2D.Float(cwidth == -1 ? -1 : cwidth / 1000f,
                                           cheight == -1 ? -1 : cheight / 1000f);
         Point2D size = child.getDimension(csize);
         if (size == null) {
             // error
             return null;
         }
         if (cwidth == -1) {
             cwidth = (int)size.getX() * 1000;
         }
         if (cheight == -1) {
             cheight = (int)size.getY() * 1000;
         }
         int scaling = node.getProperty(Constants.PR_SCALING).getEnum();
         if (scaling == Constants.Scaling.UNIFORM) {
             // adjust the larger
             double rat1 = cwidth / (size.getX() * 1000f);
             double rat2 = cheight / (size.getY() * 1000f);
             if (rat1 < rat2) {
                 // reduce cheight
                 cheight = (int)(rat1 * size.getY() * 1000);
             } else {
                 cwidth = (int)(rat2 * size.getX() * 1000);
             }
         }

         if (ipd == -1) {
             ipd = cwidth;
         }
         if (bpd == -1) {
             bpd = cheight;
         }

         boolean clip = false;
         if (cwidth > ipd || cheight > bpd) {
             int overflow = node.getProperty(Constants.PR_OVERFLOW).getEnum();
             if (overflow == Constants.Overflow.HIDDEN) {
                 clip = true;
             } else if (overflow == Constants.Overflow.ERROR_IF_OVERFLOW) {
                 node.getLogger().error("Instream foreign object overflows the viewport: clipping");
                 clip = true;
             }
         }

         int xoffset = node.computeXOffset(ipd, cwidth);
         int yoffset = node.computeYOffset(bpd, cheight);

         Rectangle2D placement = new Rectangle2D.Float(xoffset, yoffset, cwidth, cheight);

         org.w3c.dom.Document doc = child.getDOMDocument();
         String ns = child.getDocumentNamespace();

         node.childNodes = null;
         ForeignObject foreign = new ForeignObject(doc, ns);

         Viewport areaCurrent = new Viewport(foreign);
         areaCurrent.setWidth(ipd);
         areaCurrent.setHeight(bpd);
         areaCurrent.setContentPosition(placement);
         areaCurrent.setClip(clip);
         areaCurrent.setOffset(0);

         return areaCurrent;
     }

     /**
      * Overridden from FObj
      * @param lms the list to which the layout manager(s) should be added
      */
     public void servePageNumber(final PageNumber node) {
         LayoutManager lm;
         lm = new LeafNodeLayoutManager(node) {
                     public InlineArea get(LayoutContext context) {
                         // get page string from parent, build area
                         TextArea inline = new TextArea();
                         String str = parentLM.getCurrentPageNumber();
                         int width = 0;
                     for (int count = 0; count < str.length(); count++) {
                             width += node.getFontState().getCharWidth(
                                        str.charAt(count));
                         }
                         inline.setTextArea(str);
                         inline.setIPD(width);
                         inline.setHeight(node.getFontState().getAscender()
                                          - node.getFontState().getDescender());
                         inline.setOffset(node.getFontState().getAscender());

                         inline.addTrait(Trait.FONT_NAME,
                                         node.getFontState().getFontName());
                         inline.addTrait(Trait.FONT_SIZE,
                                         new Integer(node.getFontState().getFontSize()));

                         return inline;
                     }

                     protected void offsetArea(LayoutContext context) {
                         curArea.setOffset(context.getBaseline());
                     }
                 };
         currentLMList.add(lm);
     }

     public void serveTable(Table node) {
         TableLayoutManager tlm = new TableLayoutManager(node);
         ArrayList columns = node.getColumns();
         if (columns != null) {
             ArrayList columnLMs = new ArrayList();
             ListIterator iter = columns.listIterator();
             while (iter.hasNext()) {
                 columnLMs.add(getTableColumnLayoutManager((TableColumn)iter.next()));
             }
             tlm.setColumns(columnLMs);
         }
         if (node.getTableHeader() != null) {
             tlm.setTableHeader(getTableBodyLayoutManager(node.getTableHeader()));
         }
         if (node.getTableFooter() != null) {
             tlm.setTableFooter(getTableBodyLayoutManager(node.getTableFooter()));
         }
         currentLMList.add(tlm);
     }

     public LayoutManager getTableColumnLayoutManager(TableColumn node) {
         node.initialize();
         Column clm = new Column(node);
         return clm;
     }

     public void serveTableBody(TableBody node) {
         currentLMList.add(getTableBodyLayoutManager(node));
     }

     public Body getTableBodyLayoutManager(TableBody node) {
         Body blm = new Body(node);
         return blm;
     }

    /**
     * @param node TableFooter object to process
     */
    public void serveTableFooter(TableFooter node) {
        serveTableBody((TableBody)node);
    }

    /**
     * @param node TableHeader object to process
     */
    public void serveTableHeader(TableHeader node) {
        serveTableBody((TableBody)node);
    }
}
