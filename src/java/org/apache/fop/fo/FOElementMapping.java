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

package org.apache.fop.fo;

// Java
import java.util.HashMap;

import org.apache.xmlgraphics.util.QName;

/**
 * Element mapping class for all XSL-FO elements.
 */
public class FOElementMapping extends ElementMapping {

    /** The XSL-FO namespace URI */
    public static final String URI = "http://www.w3.org/1999/XSL/Format";

    /**
     * Basic constructor; inititializes the namespace URI for the fo: namespace
     */
    public FOElementMapping() {
        namespaceURI = URI;
    }

    /**
     * Initializes the collection of valid objects for the fo: namespace
     */
    protected void initialize() {
        if (foObjs == null) {
            foObjs = new HashMap<String, Maker>();

            // Declarations and Pagination and Layout Formatting Objects
            foObjs.put("root", new RootMaker());
            foObjs.put("declarations", new DeclarationsMaker());
            foObjs.put("color-profile", new ColorProfileMaker());
            foObjs.put("bookmark-tree", new BookmarkTreeMaker());
            foObjs.put("bookmark", new BookmarkMaker());
            foObjs.put("bookmark-title", new BookmarkTitleMaker());
//          foObjs.put("page-sequence-wrapper", new PageSequenceWrapperMaker());
            foObjs.put("page-sequence", new PageSequenceMaker());
            foObjs.put("layout-master-set", new LayoutMasterSetMaker());
            foObjs.put("page-sequence-master",
                           new PageSequenceMasterMaker());
            foObjs.put("single-page-master-reference",
                           new SinglePageMasterReferenceMaker());
            foObjs.put("repeatable-page-master-reference",
                           new RepeatablePageMasterReferenceMaker());
            foObjs.put("repeatable-page-master-alternatives",
                           new RepeatablePageMasterAlternativesMaker());
            foObjs.put("conditional-page-master-reference",
                           new ConditionalPageMasterReferenceMaker());
            foObjs.put("simple-page-master",
                           new SimplePageMasterMaker());
            foObjs.put("region-body", new RegionBodyMaker());
            foObjs.put("region-before", new RegionBeforeMaker());
            foObjs.put("region-after", new RegionAfterMaker());
            foObjs.put("region-start", new RegionStartMaker());
            foObjs.put("region-end", new RegionEndMaker());
            foObjs.put("flow", new FlowMaker());
            foObjs.put("static-content", new StaticContentMaker());
            foObjs.put("title", new TitleMaker());

            // Block-level Formatting Objects
            foObjs.put("block", new BlockMaker());
            foObjs.put("block-container", new BlockContainerMaker());

            // Inline-level Formatting Objects
            foObjs.put("bidi-override", new BidiOverrideMaker());
            foObjs.put("character",
                           new CharacterMaker());
            foObjs.put("initial-property-set",
                           new InitialPropertySetMaker());
            foObjs.put("external-graphic", new ExternalGraphicMaker());
            foObjs.put("instream-foreign-object",
                           new InstreamForeignObjectMaker());
            foObjs.put("inline", new InlineMaker());
            foObjs.put("inline-container", new InlineContainerMaker());
            foObjs.put("leader", new LeaderMaker());
            foObjs.put("page-number", new PageNumberMaker());
            foObjs.put("page-number-citation",
                           new PageNumberCitationMaker());
            foObjs.put("page-number-citation-last",
                    new PageNumberCitationLastMaker());

            // Formatting Objects for Tables
            foObjs.put("table-and-caption", new TableAndCaptionMaker());
            foObjs.put("table", new TableMaker());
            foObjs.put("table-column", new TableColumnMaker());
            foObjs.put("table-caption", new TableCaptionMaker());
            foObjs.put("table-header", new TableHeaderMaker());
            foObjs.put("table-footer", new TableFooterMaker());
            foObjs.put("table-body", new TableBodyMaker());
            foObjs.put("table-row", new TableRowMaker());
            foObjs.put("table-cell", new TableCellMaker());

            // Formatting Objects for Lists
            foObjs.put("list-block", new ListBlockMaker());
            foObjs.put("list-item", new ListItemMaker());
            foObjs.put("list-item-body", new ListItemBodyMaker());
            foObjs.put("list-item-label", new ListItemLabelMaker());

            // Dynamic Effects: Link and Multi Formatting Objects
            foObjs.put("basic-link", new BasicLinkMaker());
            foObjs.put("multi-switch", new MultiSwitchMaker());
            foObjs.put("multi-case", new MultiCaseMaker());
            foObjs.put("multi-toggle", new MultiToggleMaker());
            foObjs.put("multi-properties", new MultiPropertiesMaker());
            foObjs.put("multi-property-set",
                           new MultiPropertySetMaker());

            // Out-of-Line Formatting Objects
            foObjs.put("float",
                           new FloatMaker());
            foObjs.put("footnote", new FootnoteMaker());
            foObjs.put("footnote-body", new FootnoteBodyMaker());

            // Other Formatting Objects
            foObjs.put("wrapper", new WrapperMaker());
            foObjs.put("marker", new MarkerMaker());
            foObjs.put("retrieve-marker", new RetrieveMarkerMaker());
            foObjs.put("retrieve-table-marker", new RetrieveTableMarkerMaker());
        }
    }

    /** {@inheritDoc} */
    public String getStandardPrefix() {
        return "fo";
    }

    /** {@inheritDoc} */
    public boolean isAttributeProperty(QName attributeName) {
        return true; //All XSL-FO attributes are to be converted to properties.
    }

    static class RootMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.pagination.Root(parent);
        }
    }

    static class DeclarationsMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.pagination.Declarations(parent);
        }
    }

    static class ColorProfileMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.pagination.ColorProfile(parent);
        }
    }

    static class BookmarkTreeMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.pagination.bookmarks.BookmarkTree(parent);
        }
    }

    static class BookmarkMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.pagination.bookmarks.Bookmark(parent);
        }
    }

    static class BookmarkTitleMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.pagination.bookmarks.BookmarkTitle(parent);
        }
    }

    static class PageSequenceWrapperMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.pagination.PageSequenceWrapper(parent);
        }
    }

    static class PageSequenceMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.pagination.PageSequence(parent);
        }
    }

    static class LayoutMasterSetMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.pagination.LayoutMasterSet(parent);
        }
    }

    static class PageSequenceMasterMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.pagination.PageSequenceMaster(parent);
        }
    }

    static class SinglePageMasterReferenceMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.pagination.SinglePageMasterReference(parent);
        }
    }

    static class RepeatablePageMasterReferenceMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.pagination.RepeatablePageMasterReference(parent);
        }
    }

    static class RepeatablePageMasterAlternativesMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.pagination.RepeatablePageMasterAlternatives(parent);
        }
    }

    static class ConditionalPageMasterReferenceMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.pagination.ConditionalPageMasterReference(parent);
        }
    }

    static class SimplePageMasterMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.pagination.SimplePageMaster(parent);
        }
    }

    static class RegionBodyMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.pagination.RegionBody(parent);
        }
    }

    static class RegionBeforeMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.pagination.RegionBefore(parent);
        }
    }

    static class RegionAfterMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.pagination.RegionAfter(parent);
        }
    }

    static class RegionStartMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.pagination.RegionStart(parent);
        }
    }

    static class RegionEndMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.pagination.RegionEnd(parent);
        }
    }

    static class FlowMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.pagination.Flow(parent);
        }
    }

    static class StaticContentMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.pagination.StaticContent(parent);
        }
    }

    static class TitleMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.pagination.Title(parent);
        }
    }

    static class BlockMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.Block(parent);
        }
    }

    static class BlockContainerMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.BlockContainer(parent);
        }
    }

    static class BidiOverrideMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.BidiOverride(parent);
        }
    }

    static class CharacterMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.Character(parent);
        }
    }

    static class InitialPropertySetMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.InitialPropertySet(parent);
        }
    }

    static class ExternalGraphicMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.ExternalGraphic(parent);
        }
    }

    static class InstreamForeignObjectMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.InstreamForeignObject(parent);
        }
    }

    static class InlineMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.Inline(parent);
        }
    }

    static class InlineContainerMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.InlineContainer(parent);
        }
    }

    static class LeaderMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.Leader(parent);
        }
    }

    static class PageNumberMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.PageNumber(parent);
        }
    }

    static class PageNumberCitationMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.PageNumberCitation(parent);
        }
    }

    static class PageNumberCitationLastMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.PageNumberCitationLast(parent);
        }
    }

    static class TableAndCaptionMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.table.TableAndCaption(parent);
        }
    }

    static class TableMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.table.Table(parent);
        }
    }

    static class TableColumnMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.table.TableColumn(parent);
        }
    }

    static class TableCaptionMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.table.TableCaption(parent);
        }
    }

    static class TableBodyMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.table.TableBody(parent);
        }
    }

    static class TableHeaderMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.table.TableHeader(parent);
        }
    }

    static class TableFooterMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.table.TableFooter(parent);
        }
    }

    static class TableRowMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.table.TableRow(parent);
        }
    }

    static class TableCellMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.table.TableCell(parent);
        }
    }

    static class ListBlockMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.ListBlock(parent);
        }
    }

    static class ListItemMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.ListItem(parent);
        }
    }

    static class ListItemBodyMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.ListItemBody(parent);
        }
    }

    static class ListItemLabelMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.ListItemLabel(parent);
        }
    }

    static class BasicLinkMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.BasicLink(parent);
        }
    }

    static class MultiSwitchMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.MultiSwitch(parent);
        }
    }

    static class MultiCaseMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.MultiCase(parent);
        }
    }

    static class MultiToggleMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.MultiToggle(parent);
        }
    }

    static class MultiPropertiesMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.MultiProperties(parent);
        }
    }

    static class MultiPropertySetMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.MultiPropertySet(parent);
        }
    }

    static class FloatMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.Float(parent);
        }
    }

    static class FootnoteMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.Footnote(parent);
        }
    }

    static class FootnoteBodyMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.FootnoteBody(parent);
        }
    }

    static class WrapperMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.Wrapper(parent);
        }
    }

    static class MarkerMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.Marker(parent);
        }
    }

    static class RetrieveMarkerMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.RetrieveMarker(parent);
        }
    }

    static class RetrieveTableMarkerMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.RetrieveTableMarker(parent);
        }
    }
}
