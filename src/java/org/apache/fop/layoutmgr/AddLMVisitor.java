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


import org.apache.fop.apps.Document;
import org.apache.fop.area.LinkResolver;
import org.apache.fop.area.PageViewport;
import org.apache.fop.area.Resolveable;
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
import org.apache.fop.fo.FOText;
import org.apache.fop.fo.FOTreeVisitor;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.FObjMixed;
import org.apache.fop.fo.TextInfo;
import org.apache.fop.fo.ToBeImplementedElement;
import org.apache.fop.fo.Unknown;
import org.apache.fop.fo.UnknownXMLObj;
import org.apache.fop.fo.XMLElement;
import org.apache.fop.fo.XMLObj;
import org.apache.fop.fo.extensions.Bookmarks;
import org.apache.fop.fo.extensions.ExtensionObj;
import org.apache.fop.fo.extensions.Label;
import org.apache.fop.fo.extensions.Outline;
import org.apache.fop.fo.extensions.svg.SVGElement;
import org.apache.fop.fo.extensions.svg.SVGObj;
import org.apache.fop.fo.flow.BasicLink;
import org.apache.fop.fo.flow.BidiOverride;
import org.apache.fop.fo.flow.Block;
import org.apache.fop.fo.flow.BlockContainer;
import org.apache.fop.fo.flow.Character;
import org.apache.fop.fo.flow.ExternalGraphic;
import org.apache.fop.fo.flow.Float;
import org.apache.fop.fo.flow.Footnote;
import org.apache.fop.fo.flow.FootnoteBody;
import org.apache.fop.fo.flow.InitialPropertySet;
import org.apache.fop.fo.flow.Inline;
import org.apache.fop.fo.flow.InlineContainer;
import org.apache.fop.fo.flow.InstreamForeignObject;
import org.apache.fop.fo.flow.Leader;
import org.apache.fop.fo.flow.ListBlock;
import org.apache.fop.fo.flow.ListItem;
import org.apache.fop.fo.flow.ListItemBody;
import org.apache.fop.fo.flow.ListItemLabel;
import org.apache.fop.fo.flow.Marker;
import org.apache.fop.fo.flow.MultiCase;
import org.apache.fop.fo.flow.MultiProperties;
import org.apache.fop.fo.flow.MultiPropertySet;
import org.apache.fop.fo.flow.MultiSwitch;
import org.apache.fop.fo.flow.MultiToggle;
import org.apache.fop.fo.flow.PageNumber;
import org.apache.fop.fo.flow.PageNumberCitation;
import org.apache.fop.fo.flow.RetrieveMarker;
import org.apache.fop.fo.flow.Table;
import org.apache.fop.fo.flow.TableAndCaption;
import org.apache.fop.fo.flow.TableBody;
import org.apache.fop.fo.flow.TableCaption;
import org.apache.fop.fo.flow.TableCell;
import org.apache.fop.fo.flow.TableColumn;
import org.apache.fop.fo.flow.TableFooter;
import org.apache.fop.fo.flow.TableHeader;
import org.apache.fop.fo.flow.TableRow;
import org.apache.fop.fo.flow.Wrapper;
import org.apache.fop.fo.pagination.ColorProfile;
import org.apache.fop.fo.pagination.ConditionalPageMasterReference;
import org.apache.fop.fo.pagination.Declarations;
import org.apache.fop.fo.pagination.Flow;
import org.apache.fop.fo.pagination.LayoutMasterSet;
import org.apache.fop.fo.pagination.PageMasterReference;
import org.apache.fop.fo.pagination.PageSequence;
import org.apache.fop.fo.pagination.PageSequenceMaster;
import org.apache.fop.fo.pagination.Region;
import org.apache.fop.fo.pagination.RegionAfter;
import org.apache.fop.fo.pagination.RegionBA;
import org.apache.fop.fo.pagination.RegionBASE;
import org.apache.fop.fo.pagination.RegionBefore;
import org.apache.fop.fo.pagination.RegionBody;
import org.apache.fop.fo.pagination.RegionEnd;
import org.apache.fop.fo.pagination.RegionSE;
import org.apache.fop.fo.pagination.RegionStart;
import org.apache.fop.fo.pagination.RepeatablePageMasterAlternatives;
import org.apache.fop.fo.pagination.RepeatablePageMasterReference;
import org.apache.fop.fo.pagination.Root;
import org.apache.fop.fo.pagination.SimplePageMaster;
import org.apache.fop.fo.pagination.SinglePageMasterReference;
import org.apache.fop.fo.pagination.StaticContent;
import org.apache.fop.fo.pagination.Title;
import org.apache.fop.fo.properties.CommonBackground;
import org.apache.fop.fo.properties.CommonBorderAndPadding;
import org.apache.fop.layoutmgr.list.Item;
import org.apache.fop.layoutmgr.list.ListBlockLayoutManager;
import org.apache.fop.layoutmgr.list.ListItemLayoutManager;
import org.apache.fop.layoutmgr.table.Body;
import org.apache.fop.layoutmgr.table.Cell;
import org.apache.fop.layoutmgr.table.Column;
import org.apache.fop.layoutmgr.table.Row;
import org.apache.fop.layoutmgr.table.TableLayoutManager;
import org.apache.fop.traits.MinOptMax;

/**
 * Concrete implementation of FOTreeVisitor for the purpose of adding
 * Layout Managers for nodes in the FOTree.
 * Each method is responsible to return a LayoutManager responsible for laying
 * out this FObj's content.
 * @see org.apache.fop.fo.FOTreeVisitor
 */

public class AddLMVisitor implements FOTreeVisitor {

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
        fobj.acceptVisitor(this);
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

    public void serveFOText(FOText foText) {
        if (foText.ca.length - foText.start > 0) {
            currentLMList.add(new TextLayoutManager(foText));
        }
    }

    public void serveFObjMixed(FObjMixed node) {
        if (node.getChildren() != null) {
            InlineStackingLayoutManager lm;
            lm = new InlineStackingLayoutManager();
            Document doc = (Document)node.getFOTreeControl();
            lm.setUserAgent(node.getUserAgent());
            lm.setFObj(node);
            lm.setLMiter(new LMiter(lm, node.getChildren()));
            currentLMList.add(lm);
        }
    }

    public void serveBidiOverride(BidiOverride node) {
        if (false) {
            serveFObjMixed((FObjMixed)node);
        } else {
            ArrayList childList = new ArrayList();
            saveLMList = currentLMList;
            currentLMList = childList;
            serveFObjMixed((FObjMixed)node);
            currentLMList = saveLMList;
            for (int count = childList.size() - 1; count >= 0; count--) {
                LayoutProcessor lm = (LayoutProcessor) childList.get(count);
                if (lm.generatesInlineAreas()) {
                    LayoutProcessor blm = new BidiLayoutManager((InlineStackingLayoutManager) lm);
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
    public void serveInline(Inline node) {
        serveFObjMixed((FObjMixed)node);
    }

    public void serveFootnote(Footnote node) {
        if (node.getInlineFO() == null) {
            node.getLogger().error("inline required in footnote");
            return;
        }
        serveInline(node.getInlineFO());
    }

    public void serveInlineContainer(InlineContainer node) {
        ArrayList childList = new ArrayList();
        saveLMList = currentLMList;
        currentLMList = childList;
        serveFObj((FObj)node);
        currentLMList = saveLMList;
        LayoutManager lm = new ICLayoutManager(childList);
        lm.setUserAgent(node.getUserAgent());
        lm.setFObj(node);
        currentLMList.add(lm);
    }

    /**
     * Add start and end properties for the link
     */
    public void serveBasicLink(final BasicLink node) {
        node.setup();
        InlineStackingLayoutManager lm;
        lm = new InlineStackingLayoutManager() {
            protected InlineParent createArea() {
                InlineParent area = super.createArea();
                setupBasicLinkArea(node, parentLM, area);
                return area;
            }
        };
        lm.setUserAgent(node.getUserAgent());
        lm.setFObj(node);
        lm.setLMiter(new LMiter(lm, node.getChildren()));
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

     public void serveBlock(Block node) {
         BlockLayoutManager blm = new BlockLayoutManager();
         blm.setUserAgent(node.getUserAgent());
         blm.setFObj(node);
         TextInfo ti = node.getPropertyManager().getTextLayoutProps(node.getFOTreeControl());
         blm.setBlockTextInfo(ti);
         currentLMList.add(blm);
     }

     public void serveLeader(final Leader node) {
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
         lm.setAlignment(node.propertyList.get(Constants.PR_LEADER_ALIGNMENT).getEnum());
         currentLMList.add(lm);
     }

     public MinOptMax getLeaderAllocIPD(Leader node, int ipd) {
         // length of the leader
         int opt = node.getLength(Constants.PR_LEADER_LENGTH | Constants.CP_OPTIMUM, ipd);
         int min = node.getLength(Constants.PR_LEADER_LENGTH | Constants.CP_MINIMUM, ipd);
         int max = node.getLength(Constants.PR_LEADER_LENGTH | Constants.CP_MAXIMUM, ipd);

         return new MinOptMax(min, opt, max);
     }

     private InlineArea getLeaderInlineArea(Leader node) {
         node.setup();
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
             if (node.getChildren() == null) {
                 node.getLogger().error("Leader use-content with no content");
                 return null;
             }
             InlineStackingLayoutManager lm;
             lm = new InlineStackingLayoutManager();
             lm.setUserAgent(node.getUserAgent());
             lm.setFObj(node);
             lm.setLMiter(new LMiter(lm, node.getChildren()));
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

     public void serveRetrieveMarker(RetrieveMarker node) {
         RetrieveMarkerLayoutManager rmlm;
         rmlm = new RetrieveMarkerLayoutManager(node.getRetrieveClassName(),
                 node.getRetrievePosition(),
                 node.getRetrieveBoundary());
         rmlm.setUserAgent(node.getUserAgent());
         rmlm.setFObj(node);
         currentLMList.add(rmlm);
     }

     public void serveCharacter(Character node) {
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
         String str = node.propertyList.get(Constants.PR_CHARACTER).getString();
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
     public void serveExternalGraphic(ExternalGraphic node) {
         InlineArea area = getExternalGraphicInlineArea(node);
         if (area != null) {
             node.setupID();
             LeafNodeLayoutManager lm = new LeafNodeLayoutManager();
             lm.setUserAgent(node.getUserAgent());
             lm.setFObj(node);
             lm.setCurrentArea(area);
             lm.setAlignment(node.propertyList.get(Constants.PR_VERTICAL_ALIGN).getEnum());
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

     public void serveBlockContainer(BlockContainer node) {
         BlockContainerLayoutManager blm = new BlockContainerLayoutManager();
         blm.setUserAgent(node.getUserAgent());
         blm.setFObj(node);
         blm.setOverflow(node.propertyList.get(Constants.PR_OVERFLOW).getEnum());
         currentLMList.add(blm);
     }

     public void serveListBlock(ListBlock node) {
         ListBlockLayoutManager blm = new ListBlockLayoutManager();
         blm.setUserAgent(node.getUserAgent());
         blm.setFObj(node);
         currentLMList.add(blm);
     }

     public void serveInstreamForeignObject(InstreamForeignObject node) {
         Viewport areaCurrent = getInstreamForeignObjectInlineArea(node);
         if (areaCurrent != null) {
             LeafNodeLayoutManager lm = new LeafNodeLayoutManager();
             lm.setUserAgent(node.getUserAgent());
             lm.setFObj(node);
             lm.setCurrentArea(areaCurrent);
             lm.setAlignment(node.propertyList.get(Constants.PR_VERTICAL_ALIGN).getEnum());
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
         //int h = this.propertyList.get("height").getLength().mvalue();

         // use specified line-height then ignore dimension in height direction
         boolean hasLH = false;//propertyList.get("line-height").getSpecifiedValue() != null;

         Length len;

         int bpd = -1;
         int ipd = -1;
         boolean bpdauto = false;
         if (hasLH) {
             bpd = node.propertyList.get(Constants.PR_LINE_HEIGHT).getLength().getValue();
         } else {
             // this property does not apply when the line-height applies
             // isn't the block-progression-dimension always in the same
             // direction as the line height?
             len = node.propertyList.get(Constants.PR_BLOCK_PROGRESSION_DIMENSION | Constants.CP_OPTIMUM).getLength();
             if (!len.isAuto()) {
                 bpd = len.getValue();
             } else {
                 len = node.propertyList.get(Constants.PR_HEIGHT).getLength();
                 if (!len.isAuto()) {
                     bpd = len.getValue();
                 }
             }
         }

         len = node.propertyList.get(Constants.PR_INLINE_PROGRESSION_DIMENSION | Constants.CP_OPTIMUM).getLength();
         if (!len.isAuto()) {
             ipd = len.getValue();
         } else {
             len = node.propertyList.get(Constants.PR_WIDTH).getLength();
             if (!len.isAuto()) {
                 ipd = len.getValue();
             }
         }

         // if auto then use the intrinsic size of the content scaled
         // to the content-height and content-width
         int cwidth = -1;
         int cheight = -1;
         len = node.propertyList.get(Constants.PR_CONTENT_WIDTH).getLength();
         if (!len.isAuto()) {
             /*if(len.scaleToFit()) {
                 if(ipd != -1) {
                     cwidth = ipd;
                 }
             } else {*/
             cwidth = len.getValue();
         }
         len = node.propertyList.get(Constants.PR_CONTENT_HEIGHT).getLength();
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
         int scaling = node.propertyList.get(Constants.PR_SCALING).getEnum();
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
             int overflow = node.propertyList.get(Constants.PR_OVERFLOW).getEnum();
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

     public void serveListItem(ListItem node) {
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
     public void servePageNumber(final PageNumber node) {
         node.setup();
         LayoutManager lm;
         lm = new LeafNodeLayoutManager() {
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
         lm.setUserAgent(node.getUserAgent());
         lm.setFObj(node);
         currentLMList.add(lm);
     }

     public void servePageNumberCitation(final PageNumberCitation node) {
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
             TextArea text = new TextArea();
             inline = text;
             int width = node.getStringWidth(str);
             text.setTextArea(str);
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

     public void serveTable(Table node) {
         TableLayoutManager tlm = new TableLayoutManager();
         tlm.setUserAgent(node.getUserAgent());
         tlm.setFObj(node);
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
         node.doSetup();
         Column clm = new Column();
         clm.setUserAgent(node.getUserAgent());
         clm.setFObj(node);
         return clm;
     }

     public void serveTableBody(TableBody node) {
         currentLMList.add(getTableBodyLayoutManager(node));
     }

     public Body getTableBodyLayoutManager(TableBody node) {
         Body blm = new Body();
         blm.setUserAgent(node.getUserAgent());
         blm.setFObj(node);
         return blm;
     }

     public void serveTableCell(TableCell node) {
         Cell clm = new Cell();
         clm.setUserAgent(node.getUserAgent());
         clm.setFObj(node);
         currentLMList.add(clm);
     }

     public void serveTableRow(TableRow node) {
         Row rlm = new Row();
         rlm.setUserAgent(node.getUserAgent());
         rlm.setFObj(node);
         currentLMList.add(rlm);
     }

     public void serveFlow(Flow node) {
         FlowLayoutManager lm = new FlowLayoutManager();
         lm.setUserAgent(node.getUserAgent());
         lm.setFObj(node);
         currentLMList.add(lm);
     }

    /**
     * @param node FONode object to process
     */
    public void serveFONode(FONode node) {
    }

    /**
     * @param node FObj object to process
     */
    public void serveFObj(FObj node) {
        serveFONode((FONode)node);
    }

    /**
     * @param node ColorProfile object to process
     */
    public void serveColorProfile(ColorProfile node) {
        serveFObj((FObj)node);
    }

    /**
     * @param node ConditionalPageMasterReference object to process
     */
    public void serveConditionalPageMasterReference(ConditionalPageMasterReference node) {
        serveFObj((FObj)node);
    }

    /**
     * @param node Declarations object to process
     */
    public void serveDeclarations(Declarations node) {
        serveFObj((FObj)node);
    }

    /**
     * @param node ExtensionObj object to process
     */
    public void serveExtensionObj(ExtensionObj node) {
        serveFObj((FObj)node);
    }

    /**
     * @param node Bookmarks object to process
     */
    public void serveBookmarks(Bookmarks node) {
        serveExtensionObj((ExtensionObj)node);
    }

    /**
     * @param node Label object to process
     */
    public void serveLabel(Label node) {
        serveExtensionObj((ExtensionObj)node);
    }

    /**
     * @param node Outline object to process
     */
    public void serveOutline(Outline node) {
        serveExtensionObj((ExtensionObj)node);
    }

    /**
     * @param node StaticContent object to process
     */
    public void serveStaticContent(StaticContent node) {
        serveFlow((Flow)node);
    }

    /**
     * @param node Marker object to process
     */
    public void serveMarker(Marker node) {
        serveFObjMixed((FObjMixed)node);
    }

    /**
     * @param node Title object to process
     */
    public void serveTitle(Title node) {
        serveFObjMixed((FObjMixed)node);
    }

    /**
     * @param node Wrapper object to process
     */
    public void serveWrapper(Wrapper node) {
        ListIterator baseIter;
        baseIter = node.getChildren();
        if (baseIter == null) return;
        while (baseIter.hasNext()) {
            FObj child = (FObj) baseIter.next();
            child.acceptVisitor(this);
        }
    }
    
    /**
     * @param node FootnoteBody object to process
     */
    public void serveFootnoteBody(FootnoteBody node) {
        serveFObj((FObj)node);
    }

    /**
     * @param node LayoutMasterSet object to process
     */
    public void serveLayoutMasterSet(LayoutMasterSet node) {
        serveFObj((FObj)node);
    }

    /**
     * @param node ListItemBody object to process
     */
    public void serveListItemBody(ListItemBody node) {
        serveFObj((FObj)node);
    }

    /**
     * @param node ListItemLabel object to process
     */
    public void serveListItemLabel(ListItemLabel node) {
        serveFObj((FObj)node);
    }

    /**
     * @param node PageMasterReference object to process
     */
    public void servePageMasterReference(PageMasterReference node) {
        serveFObj((FObj)node);
    }

    /**
     * @param node RepeatablePageMasterReference object to process
     */
    public void serveRepeatablePageMasterReference(RepeatablePageMasterReference node) {
        servePageMasterReference((PageMasterReference)node);
    }

    /**
     * @param node SinglePageMasterReference object to process
     */
    public void serveSinglePageMasterReference(SinglePageMasterReference node) {
        servePageMasterReference((PageMasterReference)node);
    }

    /**
     * @param node PageSequence object to process
     */
    public void servePageSequence(PageSequence node) {
        serveFObj((FObj)node);
    }

    /**
     * @param node PageSequenceMaster object to process
     */
    public void servePageSequenceMaster(PageSequenceMaster node) {
        serveFObj((FObj)node);
    }

    /**
     * @param node Region object to process
     */
    public void serveRegion(Region node) {
        serveFObj((FObj)node);
    }

    /**
     * @param node RegionBASE object to process
     */
    public void serveRegionBASE(RegionBASE node) {
        serveRegion((Region)node);
    }

    /**
     * @param node RegionBA object to process
     */
    public void serveRegionBA(RegionBA node) {
        serveRegionBASE((RegionBASE)node);
    }

    /**
     * @param node RegionAfter object to process
     */
    public void serveRegionAfter(RegionAfter node) {
        serveRegionBA((RegionBA)node);
    }

    /**
     * @param node RegionBefore object to process
     */
    public void serveRegionBefore(RegionBefore node) {
        serveRegionBA((RegionBA)node);
    }

    /**
     * @param node RegionSE object to process
     */
    public void serveRegionSE(RegionSE node) {
        serveRegionBASE((RegionBASE)node);
    }

    /**
     * @param node RegionEnd object to process
     */
    public void serveRegionEnd(RegionEnd node) {
        serveRegionSE((RegionSE)node);
    }

    /**
     * @param node RegionStart object to process
     */
    public void serveRegionStart(RegionStart node) {
        serveRegionSE((RegionSE)node);
    }

    /**
     * @param node RegionBody object to process
     */
    public void serveRegionBody(RegionBody node) {
        serveRegion((Region)node);
    }

    /**
     * @param node RepeatablePageMasterAlternatives object to process
     */
    public void serveRepeatablePageMasterAlternatives(RepeatablePageMasterAlternatives node) {
        serveFObj((FObj)node);
    }

    /**
     * @param node Root object to process
     */
    public void serveRoot(Root node) {
        serveFObj((FObj)node);
    }

    /**
     * @param node SimplePageMaster object to process
     */
    public void serveSimplePageMaster(SimplePageMaster node) {
        serveFObj((FObj)node);
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

    /**
     * @param node TableColumn object to process
     */
    public void serveTableColumn(TableColumn node) {
        serveFObj((FObj)node);
    }

    /**
     * @param node ToBeImplementedElement object to process
     */
    public void serveToBeImplementedElement(ToBeImplementedElement node) {
        serveFObj((FObj)node);
    }

    /**
     * @param node Float object to process
     */
    public void serveFloat(Float node) {
        serveToBeImplementedElement((ToBeImplementedElement)node);
    }

    /**
     * @param node InitialPropertySet object to process
     */
    public void serveInitialPropertySet(InitialPropertySet node) {
        serveToBeImplementedElement((ToBeImplementedElement)node);
    }

    /**
     * @param node MultiCase object to process
     */
    public void serveMultiCase(MultiCase node) {
        serveToBeImplementedElement((ToBeImplementedElement)node);
    }

    /**
     * @param node MultiProperties object to process
     */
    public void serveMultiProperties(MultiProperties node) {
        serveToBeImplementedElement((ToBeImplementedElement)node);
    }

    /**
     * @param node MultiPropertySet object to process
     */
    public void serveMultiPropertySet(MultiPropertySet node) {
        serveToBeImplementedElement((ToBeImplementedElement)node);
    }

    /**
     * @param node MultiSwitch object to process
     */
    public void serveMultiSwitch(MultiSwitch node) {
        serveToBeImplementedElement((ToBeImplementedElement)node);
    }

    /**
     * @param node MultiToggle object to process
     */
    public void serveMultiToggle(MultiToggle node) {
        serveToBeImplementedElement((ToBeImplementedElement)node);
    }

    /**
     * @param node TableAndCaption object to process
     */
    public void serveTableAndCaption(TableAndCaption node) {
        serveToBeImplementedElement((ToBeImplementedElement)node);
    }

    /**
     * @param node TableCaption object to process
     */
    public void serveTableCaption(TableCaption node) {
        serveToBeImplementedElement((ToBeImplementedElement)node);
    }

    /**
     * @param node Unknown object to process
     */
    public void serveUnknown(Unknown node) {
        serveFONode((FONode)node);
    }

    /**
     * @param node XMLObj object to process
     */
    public void serveXMLObj(XMLObj node) {
        serveFONode((FONode)node);
    }

    /**
     * @param node SVGObj object to process
     */
    public void serveSVGObj(SVGObj node) {
        serveXMLObj((XMLObj)node);
    }

    /**
     * @param node SVGElement object to process
     */
    public void serveSVGElement(SVGElement node) {
        serveSVGObj((SVGObj)node);
    }

    /**
     * @param node UnknownXMLObj object to process
     */
    public void serveUnknownXMLObj(UnknownXMLObj node) {
        serveXMLObj((XMLObj)node);
    }

    /**
     * @param node XMLElement object to process
     */
    public void serveXMLElement(XMLElement node) {
        serveXMLObj((XMLObj)node);
    }

}
