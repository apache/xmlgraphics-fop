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

package org.apache.fop.layoutmgr;

import org.apache.fop.area.LinkResolver;
import org.apache.fop.area.PageViewport;
import org.apache.fop.area.Resolveable;
import org.apache.fop.area.Trait;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.area.inline.InlineParent;
import org.apache.fop.area.inline.FilledArea;
import org.apache.fop.area.inline.Space;
import org.apache.fop.area.inline.Word;

import org.apache.fop.fo.FOTreeVisitor;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.FObjMixed;
import org.apache.fop.fo.FOText;
import org.apache.fop.fo.TextInfo;
import org.apache.fop.fo.flow.BidiOverride;
import org.apache.fop.fo.flow.Inline;
import org.apache.fop.fo.flow.BasicLink;
import org.apache.fop.fo.flow.Block;
import org.apache.fop.fo.flow.Leader;
import org.apache.fop.fo.flow.RetrieveMarker;
import org.apache.fop.fo.flow.Character;
import org.apache.fop.fo.flow.ExternalGraphic;
import org.apache.fop.fo.flow.BlockContainer;
import org.apache.fop.fo.flow.Footnote;
import org.apache.fop.fo.flow.ListBlock;
import org.apache.fop.fo.flow.InstreamForeignObject;
import org.apache.fop.fo.flow.InlineContainer;
import org.apache.fop.fo.flow.ListItem;
import org.apache.fop.fo.flow.PageNumber;
import org.apache.fop.fo.flow.PageNumberCitation;
import org.apache.fop.fo.flow.Table;
import org.apache.fop.fo.flow.TableBody;
import org.apache.fop.fo.flow.TableCell;
import org.apache.fop.fo.flow.TableRow;
import org.apache.fop.fo.pagination.Flow;

import org.apache.fop.fo.properties.LeaderPattern;

import org.apache.fop.layoutmgr.BidiLayoutManager;
import org.apache.fop.layoutmgr.LayoutProcessor;
import org.apache.fop.layoutmgr.LMiter;
import org.apache.fop.layoutmgr.table.Cell;
import org.apache.fop.layoutmgr.table.Body;
import org.apache.fop.layoutmgr.table.Row;
import org.apache.fop.layoutmgr.table.TableLayoutManager;
import org.apache.fop.layoutmgr.list.ListBlockLayoutManager;
import org.apache.fop.layoutmgr.list.ListItemLayoutManager;
import org.apache.fop.util.CharUtilities;

import java.util.List;
import java.util.ListIterator;
import java.util.ArrayList;

/**
 * Concrete implementation of FOTreeVisitor for the purpose of adding
 * Layout Managers for nodes in the FOTree.
 * Each method is responsible to return a LayoutManager responsible for laying
 * out this FObj's content.
 * @see org.apache.fop.fo.FOTreeVisitor
 */

public class AddLMVisitor extends FOTreeVisitor {

    /** The List object to which methods in this class should add Layout
     *  Managers */
    private List currentLMList;

    /** A List object which can be used to save and restore the currentLMList if
     * another List should temporarily be used */
    private List saveLMList;

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
        fobj.acceptVisitor(this);
    }

    public void serveVisitor(FOText node) {
        if (node.length == 0) {
            return;
        }
        if (node.length < node.ca.length) {
            char[] tmp = node.ca;
            node.ca = new char[node.length];
            System.arraycopy(tmp, 0, node.ca, 0, node.length);
        }
        LayoutManager lm = new TextLayoutManager(node.ca, node.textInfo);
        lm.setFObj(node);
        currentLMList.add(lm);
    }

    public void serveVisitor(FObjMixed node) {
        if (node.getChildren() != null) {
            InlineStackingLayoutManager lm;
            lm = new InlineStackingLayoutManager();
            lm.setUserAgent(node.getUserAgent());
            lm.setFObj(node);
            lm.setLMiter(new LMiter(node.getChildren()));
            currentLMList.add(lm);
        }
    }

    public void serveVisitor(BidiOverride node) {
        if (false) {
            serveVisitor((FObjMixed)node);
        } else {
            ArrayList childList = new ArrayList();
            saveLMList = currentLMList;
            currentLMList = childList;
            serveVisitor((FObjMixed)node);
            currentLMList = saveLMList;
            for (int count = childList.size() - 1; count >= 0; count--) {
                LayoutProcessor lm = (LayoutProcessor) childList.get(count);
                if (lm.generatesInlineAreas()) {
                    LayoutProcessor blm = new BidiLayoutManager((LeafNodeLayoutManager) lm);
                    blm.setFObj(node);
                    currentLMList.add(blm);
                } else {
                    currentLMList.add(lm);
                }
            }
        }
    }

    /**
     * @param node Inline object to process
     */
    public void serveVisitor(Inline node) {
        serveVisitor((FObjMixed)node);
    }

    public void serveVisitor(Footnote node) {
        if (node.getInlineFO() == null) {
            node.getLogger().error("inline required in footnote");
            return;
        }
        serveVisitor(node.getInlineFO());
    }

    public void serveVisitor(InlineContainer node) {
        ArrayList childList = new ArrayList();
        saveLMList = currentLMList;
        currentLMList = childList;
        serveVisitor((FObj)node);
        currentLMList = saveLMList;
        LayoutManager lm = new ICLayoutManager(childList);
        lm.setUserAgent(node.getUserAgent());
        lm.setFObj(node);
        currentLMList.add(lm);
    }

    /**
     * Add start and end properties for the link
     */
    public void serveVisitor(BasicLink node) {
        node.setup();
        InlineStackingLayoutManager lm;
        lm = new InlineStackingLayoutManager() {
            protected InlineParent createArea(BasicLink node) {
                InlineParent area = super.createArea();
                setupBasicLinkArea(node, parentLM, area);
                return area;
            }
        };
        lm.setUserAgent(node.getUserAgent());
        lm.setFObj(node);
        lm.setLMiter(new LMiter(node.getChildren()));
        currentLMList.add(lm);
    }

    protected void setupBasicLinkArea(BasicLink node, LayoutProcessor parentLM,
                                      InlineParent area) {
         if (node.getLink() == null) {
             return;
         }
         if (node.getExternal()) {
             area.addTrait(Trait.EXTERNAL_LINK, node.getLink());
         } else {
             PageViewport page = parentLM.resolveRefID(node.getLink());
             if (page != null) {
                 area.addTrait(Trait.INTERNAL_LINK, page.getKey());
             } else {
                 LinkResolver res = new LinkResolver(node.getLink(), area);
                 parentLM.addUnresolvedArea(node.getLink(), res);
             }
         }
     }

     public void serveVisitor(Block node) {
         BlockLayoutManager blm = new BlockLayoutManager();
         blm.setUserAgent(node.getUserAgent());
         blm.setFObj(node);
         TextInfo ti = node.getPropertyManager().getTextLayoutProps(node.getFOTreeControl());
         blm.setBlockTextInfo(ti);
         currentLMList.add(blm);
     }

     public void serveVisitor(final Leader node) {
         LeafNodeLayoutManager lm = new LeafNodeLayoutManager() {
             public InlineArea get(LayoutContext context) {
                 return getLeaderInlineArea(node);
             }

             protected MinOptMax getAllocationIPD(int refIPD) {
                return node.getAllocIPD(refIPD);
             }

             /*protected void offsetArea(LayoutContext context) {
                 if(leaderPattern == LeaderPattern.DOTS) {
                     curArea.setOffset(context.getBaseline());
                 }
             }*/
         };
         lm.setUserAgent(node.getUserAgent());
         lm.setFObj(node);
         lm.setAlignment(node.properties.get("leader-alignment").getEnum());
         currentLMList.add(lm);
     }

     private InlineArea getLeaderInlineArea(Leader node) {
         if (node.leaderArea == null) {
             createLeaderArea(node);
         }
         return node.leaderArea;
     }

     protected void createLeaderArea(Leader node) {
         node.setup();

         if (node.getLeaderPattern() == LeaderPattern.RULE) {
             org.apache.fop.area.inline.Leader leader = new org.apache.fop.area.inline.Leader();

             leader.setRuleStyle(node.getRuleStyle());
             leader.setRuleThickness(node.getRuleThickness());

             node.leaderArea = leader;
         } else if (node.getLeaderPattern() == LeaderPattern.SPACE) {
             node.leaderArea = new Space();
         } else if (node.getLeaderPattern() == LeaderPattern.DOTS) {
             Word w = new Word();
             char dot = '.'; // userAgent.getLeaderDotCharacter();

             w.setWord("" + dot);
             w.addTrait(Trait.FONT_NAME, node.getFontState().getFontName());
             w.addTrait(Trait.FONT_SIZE,
                              new Integer(node.getFontState().getFontSize()));
             // set offset of dot within inline parent
             w.setOffset(node.getFontState().getAscender());
             int width = CharUtilities.getCharWidth(dot, node.getFontState());
             Space spacer = null;
             if (node.getPatternWidth() > width) {
                 spacer = new Space();
                 spacer.setWidth(node.getPatternWidth() - width);
                 width = node.getPatternWidth();
             }
             FilledArea fa = new FilledArea();
             fa.setUnitWidth(width);
             fa.addChild(w);
             if (spacer != null) {
                 fa.addChild(spacer);
             }
             fa.setHeight(node.getFontState().getAscender());

             node.leaderArea = fa;
         } else if (node.getLeaderPattern() == LeaderPattern.USECONTENT) {
             if (node.getChildren() == null) {
                 node.getLogger().error("Leader use-content with no content");
                 return;
             }
             InlineStackingLayoutManager lm;
             lm = new InlineStackingLayoutManager();
             lm.setUserAgent(node.getUserAgent());
             lm.setFObj(node);
             lm.setLMiter(new LMiter(node.getChildren()));
             lm.init();

             // get breaks then add areas to FilledArea
             FilledArea fa = new FilledArea();

             ContentLayoutManager clm = new ContentLayoutManager(fa);
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
             node.leaderArea = fa;
         }
     }

     public void serveVisitor(RetrieveMarker node) {
         RetrieveMarkerLayoutManager rmlm;
         rmlm = new RetrieveMarkerLayoutManager(node.getRetrieveClassName(),
                 node.getRetrievePosition(),
                 node.getRetrieveBoundary());
         rmlm.setUserAgent(node.getUserAgent());
         rmlm.setFObj(node);
         currentLMList.add(rmlm);
     }

     public void serveVisitor(Character node) {
         InlineArea inline = node.getInlineArea();
         if (inline != null) {
             LeafNodeLayoutManager lm = new LeafNodeLayoutManager();
             lm.setUserAgent(node.getUserAgent());
             lm.setFObj(node);
             lm.setCurrentArea(inline);
             currentLMList.add(lm);
         }
     }

     /**
      * This adds a leafnode layout manager that deals with the
      * created viewport/image area.
      */
     public void serveVisitor(ExternalGraphic node) {
         InlineArea area = node.getInlineArea();
         if (area != null) {
             node.setupID();
             LeafNodeLayoutManager lm = new LeafNodeLayoutManager();
             lm.setUserAgent(node.getUserAgent());
             lm.setFObj(node);
             lm.setCurrentArea(area);
             lm.setAlignment(node.properties.get("vertical-align").getEnum());
             lm.setLead(node.getViewHeight());
             currentLMList.add(lm);
         }
     }

     public void serveVisitor(BlockContainer node) {
         BlockContainerLayoutManager blm = new BlockContainerLayoutManager();
         blm.setUserAgent(node.getUserAgent());
         blm.setFObj(node);
         blm.setOverflow(node.properties.get("overflow").getEnum());
         currentLMList.add(blm);
     }

     public void serveVisitor(ListBlock node) {
         ListBlockLayoutManager blm = new ListBlockLayoutManager();
         blm.setUserAgent(node.getUserAgent());
         blm.setFObj(node);
         currentLMList.add(blm);
     }

     public void serveVisitor(InstreamForeignObject node) {
         node.areaCurrent = node.getInlineArea();
         if (node.areaCurrent != null) {
             LeafNodeLayoutManager lm = new LeafNodeLayoutManager();
             lm.setUserAgent(node.getUserAgent());
             lm.setFObj(node);
             lm.setCurrentArea(node.areaCurrent);
             lm.setAlignment(node.properties.get("vertical-align").getEnum());
             lm.setLead(node.areaCurrent.getHeight());
             currentLMList.add(lm);
         }
     }

     public void serveVisitor(ListItem node) {
         if (node.getLabel() != null && node.getBody() != null) {
             ListItemLayoutManager blm = new ListItemLayoutManager();
             blm.setUserAgent(node.getUserAgent());
             blm.setFObj(node);
             blm.setLabel(node.getLabel().getItemLayoutManager());
             blm.setBody(node.getBody().getItemLayoutManager());
             currentLMList.add(blm);
         } else {
             node.getLogger().error("list-item requires list-item-label and list-item-body");
         }
     }

     /**
      * Overridden from FObj
      * @param lms the list to which the layout manager(s) should be added
      */
     public void serveVisitor(final PageNumber node) {
         node.setup();
         LayoutManager lm;
         lm = new LeafNodeLayoutManager() {
                     public InlineArea get(LayoutContext context) {
                         // get page string from parent, build area
                         Word inline = new Word();
                         String str = parentLM.getCurrentPageNumber();
                         int width = 0;
                     for (int count = 0; count < str.length(); count++) {
                             width += CharUtilities.getCharWidth(
                                        str.charAt(count), node.getFontState());
                         }
                         inline.setWord(str);
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
         lm.setUserAgent(node.getUserAgent());
         lm.setFObj(node);
         currentLMList.add(lm);
     }

     public void serveVisitor(final PageNumberCitation node) {
         node.setup();
         LayoutManager lm;
         lm = new LeafNodeLayoutManager() {
                     public InlineArea get(LayoutContext context) {
                         return node.getInlineArea(parentLM);
                     }

                     public void addAreas(PositionIterator posIter,
                                          LayoutContext context) {
                         super.addAreas(posIter, context);
                         if (node.getUnresolved()) {
                             parentLM.addUnresolvedArea(node.getRefId(),
                                                        (Resolveable) node.getInline());
                         }
                     }

                     protected void offsetArea(LayoutContext context) {
                         curArea.setOffset(context.getBaseline());
                     }
                 };
         lm.setUserAgent(node.getUserAgent());
         lm.setFObj(node);
         currentLMList.add(lm);
     }

     public void serveVisitor(Table node) {
         TableLayoutManager tlm = new TableLayoutManager();
         tlm.setUserAgent(node.getUserAgent());
         tlm.setFObj(node);
         tlm.setColumns(node.getColumns());
         if (node.getTableHeader() != null) {
             tlm.setTableHeader(getTableBodyLayoutManager(node.getTableHeader()));
         }
         if (node.getTableFooter() != null) {
             tlm.setTableFooter(getTableBodyLayoutManager(node.getTableFooter()));
         }
         currentLMList.add(tlm);
     }

     public void serveVisitor(TableBody node) {
         currentLMList.add(getTableBodyLayoutManager(node));
     }

     public Body getTableBodyLayoutManager(TableBody node) {
         Body blm = new Body();
         blm.setUserAgent(node.getUserAgent());
         blm.setFObj(node);
         return blm;
     }

     public void serveVisitor(TableCell node) {
         Cell clm = new Cell();
         clm.setUserAgent(node.getUserAgent());
         clm.setFObj(node);
         currentLMList.add(clm);
     }

     public void serveVisitor(TableRow node) {
         Row rlm = new Row();
         rlm.setUserAgent(node.getUserAgent());
         rlm.setFObj(node);
         currentLMList.add(rlm);
     }

     public void serveVisitor(Flow node) {
         FlowLayoutManager lm = new FlowLayoutManager();
         lm.setUserAgent(node.getUserAgent());
         lm.setFObj(node);
         currentLMList.add(lm);
     }

}
