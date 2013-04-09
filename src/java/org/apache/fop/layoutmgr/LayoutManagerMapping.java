/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.area.AreaTreeHandler;
import org.apache.fop.fo.FOElementMapping;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FONode.FONodeIterator;
import org.apache.fop.fo.FOText;
import org.apache.fop.fo.FObjMixed;
import org.apache.fop.fo.extensions.ExternalDocument;
import org.apache.fop.fo.flow.BasicLink;
import org.apache.fop.fo.flow.BidiOverride;
import org.apache.fop.fo.flow.Block;
import org.apache.fop.fo.flow.BlockContainer;
import org.apache.fop.fo.flow.Character;
import org.apache.fop.fo.flow.ExternalGraphic;
import org.apache.fop.fo.flow.Footnote;
import org.apache.fop.fo.flow.Inline;
import org.apache.fop.fo.flow.InlineContainer;
import org.apache.fop.fo.flow.InlineLevel;
import org.apache.fop.fo.flow.InstreamForeignObject;
import org.apache.fop.fo.flow.Leader;
import org.apache.fop.fo.flow.ListBlock;
import org.apache.fop.fo.flow.ListItem;
import org.apache.fop.fo.flow.PageNumber;
import org.apache.fop.fo.flow.PageNumberCitation;
import org.apache.fop.fo.flow.PageNumberCitationLast;
import org.apache.fop.fo.flow.RetrieveMarker;
import org.apache.fop.fo.flow.RetrieveTableMarker;
import org.apache.fop.fo.flow.Wrapper;
import org.apache.fop.fo.flow.table.Table;
import org.apache.fop.fo.flow.table.TableBody;
import org.apache.fop.fo.flow.table.TableCell;
import org.apache.fop.fo.flow.table.TableColumn;
import org.apache.fop.fo.flow.table.TableFooter;
import org.apache.fop.fo.flow.table.TableHeader;
import org.apache.fop.fo.flow.table.TableRow;
import org.apache.fop.fo.pagination.Flow;
import org.apache.fop.fo.pagination.PageSequence;
import org.apache.fop.fo.pagination.SideRegion;
import org.apache.fop.fo.pagination.StaticContent;
import org.apache.fop.fo.pagination.Title;
import org.apache.fop.layoutmgr.inline.BasicLinkLayoutManager;
import org.apache.fop.layoutmgr.inline.BidiLayoutManager;
import org.apache.fop.layoutmgr.inline.CharacterLayoutManager;
import org.apache.fop.layoutmgr.inline.ContentLayoutManager;
import org.apache.fop.layoutmgr.inline.ExternalGraphicLayoutManager;
import org.apache.fop.layoutmgr.inline.FootnoteLayoutManager;
import org.apache.fop.layoutmgr.inline.ICLayoutManager;
import org.apache.fop.layoutmgr.inline.InlineLayoutManager;
import org.apache.fop.layoutmgr.inline.InstreamForeignObjectLM;
import org.apache.fop.layoutmgr.inline.LeaderLayoutManager;
import org.apache.fop.layoutmgr.inline.PageNumberCitationLastLayoutManager;
import org.apache.fop.layoutmgr.inline.PageNumberCitationLayoutManager;
import org.apache.fop.layoutmgr.inline.PageNumberLayoutManager;
import org.apache.fop.layoutmgr.inline.TextLayoutManager;
import org.apache.fop.layoutmgr.inline.WrapperLayoutManager;
import org.apache.fop.layoutmgr.list.ListBlockLayoutManager;
import org.apache.fop.layoutmgr.list.ListItemLayoutManager;
import org.apache.fop.layoutmgr.table.TableLayoutManager;
import org.apache.fop.util.CharUtilities;

/**
 * The default LayoutManager maker class
 */
public class LayoutManagerMapping implements LayoutManagerMaker {

    /** logging instance */
    private static final Log LOG = LogFactory.getLog(LayoutManagerMapping.class);

    /** The map of LayoutManagerMakers */
    private Map makers = new HashMap();

    /** default constructor */
    public LayoutManagerMapping() {
        initialize();
    }

    /**
     * Initializes the set of maker objects associated with this LayoutManagerMapping
     */
    protected void initialize() {
        registerMaker(FOText.class, new FOTextLayoutManagerMaker());
        registerMaker(FObjMixed.class, new Maker());
        registerMaker(BidiOverride.class, new BidiOverrideLayoutManagerMaker());
        registerMaker(Inline.class, new InlineLayoutManagerMaker());
        registerMaker(Footnote.class, new FootnodeLayoutManagerMaker());
        registerMaker(InlineContainer.class,
                   new InlineContainerLayoutManagerMaker());
        registerMaker(BasicLink.class, new BasicLinkLayoutManagerMaker());
        registerMaker(Block.class, new BlockLayoutManagerMaker());
        registerMaker(Leader.class, new LeaderLayoutManagerMaker());
        registerMaker(RetrieveMarker.class, new RetrieveMarkerLayoutManagerMaker());
        registerMaker(RetrieveTableMarker.class, new RetrieveTableMarkerLayoutManagerMaker());
        registerMaker(Character.class, new CharacterLayoutManagerMaker());
        registerMaker(ExternalGraphic.class,
                   new ExternalGraphicLayoutManagerMaker());
        registerMaker(BlockContainer.class,
                   new BlockContainerLayoutManagerMaker());
        registerMaker(ListItem.class, new ListItemLayoutManagerMaker());
        registerMaker(ListBlock.class, new ListBlockLayoutManagerMaker());
        registerMaker(InstreamForeignObject.class,
                   new InstreamForeignObjectLayoutManagerMaker());
        registerMaker(PageNumber.class, new PageNumberLayoutManagerMaker());
        registerMaker(PageNumberCitation.class,
                   new PageNumberCitationLayoutManagerMaker());
        registerMaker(PageNumberCitationLast.class,
                new PageNumberCitationLastLayoutManagerMaker());
        registerMaker(Table.class, new TableLayoutManagerMaker());
        registerMaker(TableBody.class, new Maker());
        registerMaker(TableColumn.class, new Maker());
        registerMaker(TableRow.class, new Maker());
        registerMaker(TableCell.class, new Maker());
        registerMaker(TableFooter.class, new Maker());
        registerMaker(TableHeader.class, new Maker());
        registerMaker(Wrapper.class, new WrapperLayoutManagerMaker());
        registerMaker(Title.class, new InlineLayoutManagerMaker());
    }

    /**
     * Registers a Maker class for a specific formatting object.
     * @param clazz the formatting object class
     * @param maker the maker for the layout manager
     */
    protected void registerMaker(Class clazz, Maker maker) {
        makers.put(clazz, maker);
    }

    /** {@inheritDoc} */
    public void makeLayoutManagers(FONode node, List lms) {
        Maker maker = (Maker) makers.get(node.getClass());
        if (maker == null) {
            if (FOElementMapping.URI.equals(node.getNamespaceURI())) {
                LOG.error("No LayoutManager maker for class " + node.getClass());
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Skipping the creation of a layout manager for " + node.getClass());
                }
            }
        } else {
            maker.make(node, lms);
        }
    }

    /** {@inheritDoc} */
    public LayoutManager makeLayoutManager(FONode node) {
        List lms = new ArrayList();
        makeLayoutManagers(node, lms);
        if (lms.size() == 0) {
            throw new IllegalStateException("LayoutManager for class "
                                   + node.getClass()
                                   + " is missing.");
        } else if (lms.size() > 1) {
            throw new IllegalStateException("Duplicate LayoutManagers for class "
                                   + node.getClass()
                                   + " found, only one may be declared.");
        }
        return (LayoutManager) lms.get(0);
    }

    /** {@inheritDoc} */
    public PageSequenceLayoutManager makePageSequenceLayoutManager(
        AreaTreeHandler ath, PageSequence ps) {
        return new PageSequenceLayoutManager(ath, ps);
    }

    /** {@inheritDoc} */
    public ExternalDocumentLayoutManager makeExternalDocumentLayoutManager(
        AreaTreeHandler ath, ExternalDocument ed) {
        return new ExternalDocumentLayoutManager(ath, ed);
    }

    /** {@inheritDoc} */
    public FlowLayoutManager makeFlowLayoutManager(
            PageSequenceLayoutManager pslm, Flow flow) {
        return new FlowLayoutManager(pslm, flow);
    }

    /** {@inheritDoc} */
    public ContentLayoutManager makeContentLayoutManager(PageSequenceLayoutManager pslm,
                                                         Title title) {
        return new ContentLayoutManager(pslm, title);
    }

    /** {@inheritDoc} */
    public StaticContentLayoutManager makeStaticContentLayoutManager(
            PageSequenceLayoutManager pslm, StaticContent sc, SideRegion reg) {
        return new StaticContentLayoutManager(pslm, sc, reg);
    }

    /** {@inheritDoc} */
    public StaticContentLayoutManager makeStaticContentLayoutManager(
        PageSequenceLayoutManager pslm, StaticContent sc, org.apache.fop.area.Block block) {
        return new StaticContentLayoutManager(pslm, sc, block);
    }

    /** a layout manager maker base class */
    public static class Maker {
        /**
         * Create a layout manager.
         * @param node the associated FO node
         * @param lms a list of layout managers to which new manager is to be added
         */
        public void make(FONode node, List lms) {
            // no layout manager
        }
    }

    /** a layout manager maker */
    public static class FOTextLayoutManagerMaker extends Maker {
        /** {@inheritDoc} */
        public void make(FONode node, List lms) {
            FOText foText = (FOText) node;
            if (foText.length() > 0) {
                lms.add(new TextLayoutManager(foText));
            }
        }
    }

    /** a layout manager maker */
    public static class BidiOverrideLayoutManagerMaker extends Maker {
        /** {@inheritDoc} */
        public void make(FONode node, List lms) {
            if (node instanceof BidiOverride) {
                lms.add(new BidiLayoutManager((BidiOverride) node));
            }
        }
    }

    /** a layout manager maker */
    public static class InlineLayoutManagerMaker extends Maker {
        /** {@inheritDoc} */
         public void make(FONode node, List lms) {
             lms.add(new InlineLayoutManager((InlineLevel) node));
         }
    }

    /** a layout manager maker */
    public static class FootnodeLayoutManagerMaker extends Maker {
        /** {@inheritDoc} */
        public void make(FONode node, List lms) {
            lms.add(new FootnoteLayoutManager((Footnote) node));
        }
    }

    /** a layout manager maker */
    public static class InlineContainerLayoutManagerMaker extends Maker {
        /** {@inheritDoc} */
        public void make(FONode node, List lms) {
            ArrayList childList = new ArrayList();
            super.make(node, childList);
            lms.add(new ICLayoutManager((InlineContainer) node, childList));
        }
    }

    /** a layout manager maker */
    public static class BasicLinkLayoutManagerMaker extends Maker {
        /** {@inheritDoc} */
        public void make(FONode node, List lms) {
            lms.add(new BasicLinkLayoutManager((BasicLink) node));
        }
    }

    /** a layout manager maker */
    public static class BlockLayoutManagerMaker extends Maker {
        /** {@inheritDoc} */
         public void make(FONode node, List lms) {
             lms.add(new BlockLayoutManager((Block) node));
         }
    }

    /** a layout manager maker */
    public static class LeaderLayoutManagerMaker extends Maker {
        /** {@inheritDoc} */
        public void make(FONode node, List lms) {
            lms.add(new LeaderLayoutManager((Leader) node));
        }
    }

    /** a layout manager maker */
    public static class CharacterLayoutManagerMaker extends Maker {
        /** {@inheritDoc} */
        public void make(FONode node, List lms) {
            Character foCharacter = (Character) node;
            if (foCharacter.getCharacter() != CharUtilities.CODE_EOT) {
                lms.add(new CharacterLayoutManager(foCharacter));
            }
        }
    }

    /** a layout manager maker */
    public static class ExternalGraphicLayoutManagerMaker extends Maker {
        /** {@inheritDoc} */
        public void make(FONode node, List lms) {
            ExternalGraphic eg = (ExternalGraphic) node;
            if (!eg.getSrc().equals("")) {
                lms.add(new ExternalGraphicLayoutManager(eg));
            }
        }
    }

    /** a layout manager maker */
    public static class BlockContainerLayoutManagerMaker extends Maker {
        /** {@inheritDoc} */
        public void make(FONode node, List lms) {
            lms.add(new BlockContainerLayoutManager((BlockContainer) node));
         }
    }

    /** a layout manager maker */
    public static class ListItemLayoutManagerMaker extends Maker {
        /** {@inheritDoc} */
         public void make(FONode node, List lms) {
             lms.add(new ListItemLayoutManager((ListItem) node));
         }
    }

    /** a layout manager maker */
    public static class ListBlockLayoutManagerMaker extends Maker {
        /** {@inheritDoc} */
        public void make(FONode node, List lms) {
            lms.add(new ListBlockLayoutManager((ListBlock) node));
        }
    }

    /** a layout manager maker */
    public static class InstreamForeignObjectLayoutManagerMaker extends Maker {
        /** {@inheritDoc} */
        public void make(FONode node, List lms) {
            lms.add(new InstreamForeignObjectLM((InstreamForeignObject) node));
        }
    }

    /** a layout manager maker */
    public static class PageNumberLayoutManagerMaker extends Maker {
        /** {@inheritDoc} */
         public void make(FONode node, List lms) {
             lms.add(new PageNumberLayoutManager((PageNumber) node));
         }
    }

    /** a layout manager maker */
    public static class PageNumberCitationLayoutManagerMaker extends Maker {
        /** {@inheritDoc} */
         public void make(FONode node, List lms) {
            lms.add(new PageNumberCitationLayoutManager((PageNumberCitation) node));
         }
    }

    /** a layout manager maker */
    public static class PageNumberCitationLastLayoutManagerMaker extends Maker {
        /** {@inheritDoc} */
        public void make(FONode node, List lms) {
           lms.add(new PageNumberCitationLastLayoutManager((PageNumberCitationLast) node));
        }
    }

    /** a layout manager maker */
    public static class TableLayoutManagerMaker extends Maker {
        /** {@inheritDoc} */
        public void make(FONode node, List lms) {
            Table table = (Table) node;
            TableLayoutManager tlm = new TableLayoutManager(table);
            lms.add(tlm);
        }
    }

    /** a layout manager maker */
    public class RetrieveMarkerLayoutManagerMaker extends Maker {
        /** {@inheritDoc} */
        public void make(FONode node, List lms) {
            Iterator baseIter;
            baseIter = node.getChildNodes();
            if (baseIter == null) {
                return;
            }
            while (baseIter.hasNext()) {
                FONode child = (FONode) baseIter.next();
                makeLayoutManagers(child, lms);
            }
        }
    }

    public class RetrieveTableMarkerLayoutManagerMaker extends Maker {
        public void make(FONode node, List lms) {
            FONodeIterator baseIter = node.getChildNodes();
            if (baseIter == null) {
                // this happens when the retrieve-table-marker cannot be resolved yet
                RetrieveTableMarker rtm = (RetrieveTableMarker) node;
                RetrieveTableMarkerLayoutManager rtmlm = new RetrieveTableMarkerLayoutManager(rtm);
                lms.add(rtmlm);
                return;
            }
            while (baseIter.hasNext()) {
                // this happens when the retrieve-table-marker has been resolved
                FONode child = (FONode) baseIter.next();
                makeLayoutManagers(child, lms);
            }
        }
    }

    /** a layout manager maker */
    public class WrapperLayoutManagerMaker extends Maker {
        /** {@inheritDoc} */
        public void make(FONode node, List lms) {
            //We insert the wrapper LM before it's children so an ID
            //on the node can be registered on a page.
            lms.add(new WrapperLayoutManager((Wrapper)node));
            Iterator baseIter;
            baseIter = node.getChildNodes();
            if (baseIter == null) {
                return;
            }
            while (baseIter.hasNext()) {
                FONode child = (FONode) baseIter.next();
                makeLayoutManagers(child, lms);
            }
        }
    }

}
