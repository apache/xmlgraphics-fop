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
import org.apache.fop.area.inline.ForeignObject;
import org.apache.fop.area.inline.Space;
import org.apache.fop.area.inline.UnresolvedPageNumber;
import org.apache.fop.area.inline.Word;
import org.apache.fop.area.inline.Image;
import org.apache.fop.area.inline.Viewport;

import org.apache.fop.datatypes.Length;

import org.apache.fop.fo.FOTreeVisitor;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.FObjMixed;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FOText;
import org.apache.fop.fo.TextInfo;
import org.apache.fop.fo.XMLObj;
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
import org.apache.fop.fo.flow.ListItemBody;
import org.apache.fop.fo.flow.ListItemLabel;
import org.apache.fop.fo.flow.InstreamForeignObject;
import org.apache.fop.fo.flow.InlineContainer;
import org.apache.fop.fo.flow.ListItem;
import org.apache.fop.fo.flow.PageNumber;
import org.apache.fop.fo.flow.PageNumberCitation;
import org.apache.fop.fo.flow.Table;
import org.apache.fop.fo.flow.TableBody;
import org.apache.fop.fo.flow.TableCell;
import org.apache.fop.fo.flow.TableColumn;
import org.apache.fop.fo.flow.TableRow;
import org.apache.fop.fo.pagination.Flow;

import org.apache.fop.fo.properties.LeaderPattern;
import org.apache.fop.fo.properties.CommonBorderAndPadding;
import org.apache.fop.fo.properties.CommonBackground;
import org.apache.fop.fo.properties.Overflow;
import org.apache.fop.fo.properties.Scaling;

import org.apache.fop.layoutmgr.BidiLayoutManager;
import org.apache.fop.layoutmgr.LayoutProcessor;
import org.apache.fop.layoutmgr.LMiter;
import org.apache.fop.layoutmgr.table.Cell;
import org.apache.fop.layoutmgr.table.Column;
import org.apache.fop.layoutmgr.table.Body;
import org.apache.fop.layoutmgr.table.Row;
import org.apache.fop.layoutmgr.table.TableLayoutManager;
import org.apache.fop.layoutmgr.list.Item;
import org.apache.fop.layoutmgr.list.ListBlockLayoutManager;
import org.apache.fop.layoutmgr.list.ListItemLayoutManager;
import org.apache.fop.util.CharUtilities;

import java.util.List;
import java.util.ListIterator;
import java.util.ArrayList;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.apache.fop.apps.*;
import org.apache.fop.traits.*;

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
            Document doc = (Document)node.getFOTreeControl();
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
                return getLeaderAllocIPD(node, refIPD);
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

     public MinOptMax getLeaderAllocIPD(Leader node, int ipd) {
         // length of the leader
         int opt = node.getLength("leader-length.optimum", ipd);
         int min = node.getLength("leader-length.minimum", ipd);
         int max = node.getLength("leader-length.maximum", ipd);

         return new MinOptMax(min, opt, max);
     }

     private InlineArea getLeaderInlineArea(Leader node) {
         node.setup();
         InlineArea leaderArea = null;

         if (node.getLeaderPattern() == LeaderPattern.RULE) {
             org.apache.fop.area.inline.Leader leader = new org.apache.fop.area.inline.Leader();
             leader.setRuleStyle(node.getRuleStyle());
             leader.setRuleThickness(node.getRuleThickness());
             leaderArea = leader;
         } else if (node.getLeaderPattern() == LeaderPattern.SPACE) {
             leaderArea = new Space();
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

             leaderArea = fa;
         } else if (node.getLeaderPattern() == LeaderPattern.USECONTENT) {
             if (node.getChildren() == null) {
                 node.getLogger().error("Leader use-content with no content");
                 return null;
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
             leaderArea = fa;
         }
         return leaderArea;
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
         InlineArea inline = getCharacterInlineArea(node);
         if (inline != null) {
             LeafNodeLayoutManager lm = new LeafNodeLayoutManager();
             lm.setUserAgent(node.getUserAgent());
             lm.setFObj(node);
             lm.setCurrentArea(inline);
             currentLMList.add(lm);
         }
     }

     public InlineArea getCharacterInlineArea(Character node) {
         String str = node.properties.get("character").getString();
         if (str.length() == 1) {
             org.apache.fop.area.inline.Character ch =
               new org.apache.fop.area.inline.Character(
                 str.charAt(0));
             return ch;
         }
         return null;
     }

     /**
      * This adds a leafnode layout manager that deals with the
      * created viewport/image area.
      */
     public void serveVisitor(ExternalGraphic node) {
         InlineArea area = getExternalGraphicInlineArea(node);
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

     /**
      * Get the inline area for this external grpahic.
      * This creates the image area and puts it inside a viewport.
      *
      * @return the viewport containing the image area
      */
     public InlineArea getExternalGraphicInlineArea(ExternalGraphic node) {
         node.setup();
         if (node.getURL() == null) {
             return null;
         }
         Image imArea = new Image(node.getURL());
         Viewport vp = new Viewport(imArea);
         vp.setWidth(node.getViewWidth());
         vp.setHeight(node.getViewHeight());
         vp.setClip(node.getClip());
         vp.setContentPosition(node.getPlacement());
         vp.setOffset(0);

         // Common Border, Padding, and Background Properties
         CommonBorderAndPadding bap = node.getPropertyManager().getBorderAndPadding();
         CommonBackground bProps = node.getPropertyManager().getBackgroundProps();
         TraitSetter.addBorders(vp, bap);
         TraitSetter.addBackground(vp, bProps);

         return vp;
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
         Viewport areaCurrent = getInstreamForeignObjectInlineArea(node);
         if (areaCurrent != null) {
             LeafNodeLayoutManager lm = new LeafNodeLayoutManager();
             lm.setUserAgent(node.getUserAgent());
             lm.setFObj(node);
             lm.setCurrentArea(areaCurrent);
             lm.setAlignment(node.properties.get("vertical-align").getEnum());
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
         if (node.getChildren() == null) {
             return null;
         }

         if (node.children.size() != 1) {
             // error
             return null;
         }
         FONode fo = (FONode)node.children.get(0);
         if (!(fo instanceof XMLObj)) {
             // error
             return null;
         }
         XMLObj child = (XMLObj)fo;

         // viewport size is determined by block-progression-dimension
         // and inline-progression-dimension

         // if replaced then use height then ignore block-progression-dimension
         //int h = this.properties.get("height").getLength().mvalue();

         // use specified line-height then ignore dimension in height direction
         boolean hasLH = false;//properties.get("line-height").getSpecifiedValue() != null;

         Length len;

         int bpd = -1;
         int ipd = -1;
         boolean bpdauto = false;
         if (hasLH) {
             bpd = node.properties.get("line-height").getLength().getValue();
         } else {
             // this property does not apply when the line-height applies
             // isn't the block-progression-dimension always in the same
             // direction as the line height?
             len = node.properties.get("block-progression-dimension.optimum").getLength();
             if (!len.isAuto()) {
                 bpd = len.getValue();
             } else {
                 len = node.properties.get("height").getLength();
                 if (!len.isAuto()) {
                     bpd = len.getValue();
                 }
             }
         }

         len = node.properties.get("inline-progression-dimension.optimum").getLength();
         if (!len.isAuto()) {
             ipd = len.getValue();
         } else {
             len = node.properties.get("width").getLength();
             if (!len.isAuto()) {
                 ipd = len.getValue();
             }
         }

         // if auto then use the intrinsic size of the content scaled
         // to the content-height and content-width
         int cwidth = -1;
         int cheight = -1;
         len = node.properties.get("content-width").getLength();
         if (!len.isAuto()) {
             /*if(len.scaleToFit()) {
                 if(ipd != -1) {
                     cwidth = ipd;
                 }
             } else {*/
             cwidth = len.getValue();
         }
         len = node.properties.get("content-height").getLength();
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
         int scaling = node.properties.get("scaling").getEnum();
         if (scaling == Scaling.UNIFORM) {
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
             int overflow = node.properties.get("overflow").getEnum();
             if (overflow == Overflow.HIDDEN) {
                 clip = true;
             } else if (overflow == Overflow.ERROR_IF_OVERFLOW) {
                 node.getLogger().error("Instream foreign object overflows the viewport: clipping");
                 clip = true;
             }
         }

         int xoffset = node.computeXOffset(ipd, cwidth);
         int yoffset = node.computeYOffset(bpd, cheight);

         Rectangle2D placement = new Rectangle2D.Float(xoffset, yoffset, cwidth, cheight);

         org.w3c.dom.Document doc = child.getDOMDocument();
         String ns = child.getDocumentNamespace();

         node.children = null;
         ForeignObject foreign = new ForeignObject(doc, ns);

         Viewport areaCurrent = new Viewport(foreign);
         areaCurrent.setWidth(ipd);
         areaCurrent.setHeight(bpd);
         areaCurrent.setContentPosition(placement);
         areaCurrent.setClip(clip);
         areaCurrent.setOffset(0);

         return areaCurrent;
     }

     public void serveVisitor(ListItem node) {
         if (node.getLabel() != null && node.getBody() != null) {
             ListItemLayoutManager blm = new ListItemLayoutManager();
             blm.setUserAgent(node.getUserAgent());
             blm.setFObj(node);
             blm.setLabel(getListItemLabelLayoutManager(node.getLabel()));
             blm.setBody(getListItemBodyLayoutManager(node.getBody()));
             currentLMList.add(blm);
         } else {
             node.getLogger().error("list-item requires list-item-label and list-item-body");
         }
     }

     /**
      * @return this object's Item layout manager
      */
     public Item getListItemLabelLayoutManager(ListItemLabel node) {
         Item itemLabel = new Item();
         itemLabel.setUserAgent(node.getUserAgent());
         itemLabel.setFObj(node);
         return itemLabel;
     }

     /**
      * @return Item layout manager
      */
     public Item getListItemBodyLayoutManager(ListItemBody node) {
         Item item = new Item();
         item.setUserAgent(node.getUserAgent());
         item.setFObj(node);
         return item;
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
                         curArea = getPageNumberCitationInlineArea(node, parentLM);
                         return curArea;
                     }

                     public void addAreas(PositionIterator posIter,
                                          LayoutContext context) {
                         super.addAreas(posIter, context);
                         if (node.getUnresolved()) {
                             parentLM.addUnresolvedArea(node.getRefId(),
                                                        (Resolveable) curArea);
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

     // if id can be resolved then simply return a word, otherwise
     // return a resolveable area
     public InlineArea getPageNumberCitationInlineArea(PageNumberCitation node,
             LayoutProcessor parentLM) {
         if (node.getRefId().equals("")) {
             node.getLogger().error("page-number-citation must contain \"ref-id\"");
             return null;
         }
         PageViewport page = parentLM.resolveRefID(node.getRefId());
         InlineArea inline = null;
         if (page != null) {
             String str = page.getPageNumber();
             // get page string from parent, build area
             Word word = new Word();
             inline = word;
             int width = node.getStringWidth(str);
             word.setWord(str);
             inline.setIPD(width);
             inline.setHeight(node.getFontState().getAscender()
                              - node.getFontState().getDescender());
             inline.setOffset(node.getFontState().getAscender());

             inline.addTrait(Trait.FONT_NAME, node.getFontState().getFontName());
             inline.addTrait(Trait.FONT_SIZE,
                             new Integer(node.getFontState().getFontSize()));
             node.setUnresolved(false);
         } else {
             node.setUnresolved(true);
             inline = new UnresolvedPageNumber(node.getRefId());
             String str = "MMM"; // reserve three spaces for page number
             int width = node.getStringWidth(str);
             inline.setIPD(width);
             inline.setHeight(node.getFontState().getAscender()
                              - node.getFontState().getDescender());
             inline.setOffset(node.getFontState().getAscender());

             inline.addTrait(Trait.FONT_NAME, node.getFontState().getFontName());
             inline.addTrait(Trait.FONT_SIZE,
                             new Integer(node.getFontState().getFontSize()));
         }
         return inline;
     }

     public void serveVisitor(Table node) {
         TableLayoutManager tlm = new TableLayoutManager();
         tlm.setUserAgent(node.getUserAgent());
         tlm.setFObj(node);
         ArrayList columnLMs = new ArrayList();
         ListIterator iter = node.getColumns().listIterator();
         while (iter.hasNext()) {
             columnLMs.add(getTableColumnLayoutManager((TableColumn)iter.next()));
         }
         tlm.setColumns(columnLMs);
         if (node.getTableHeader() != null) {
             tlm.setTableHeader(getTableBodyLayoutManager(node.getTableHeader()));
         }
         if (node.getTableFooter() != null) {
             tlm.setTableFooter(getTableBodyLayoutManager(node.getTableFooter()));
         }
         currentLMList.add(tlm);
     }

     public LayoutManager getTableColumnLayoutManager(TableColumn node) {
         node.doSetup();
         Column clm = new Column();
         clm.setUserAgent(node.getUserAgent());
         clm.setFObj(node);
         return clm;
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
