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

package org.apache.fop.render.pdf;

import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;

import javax.xml.XMLConstants;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

import org.apache.fop.accessibility.StructureTreeElement;
import org.apache.fop.accessibility.StructureTreeEventHandler;
import org.apache.fop.events.EventBroadcaster;
import org.apache.fop.fo.extensions.ExtensionElementMapping;
import org.apache.fop.fo.extensions.InternalElementMapping;
import org.apache.fop.fo.pagination.Flow;
import org.apache.fop.pdf.PDFFactory;
import org.apache.fop.pdf.PDFParentTree;
import org.apache.fop.pdf.PDFStructElem;
import org.apache.fop.pdf.PDFStructTreeRoot;
import org.apache.fop.pdf.StandardStructureAttributes.Table.Scope;
import org.apache.fop.pdf.StandardStructureTypes;
import org.apache.fop.pdf.StandardStructureTypes.Grouping;
import org.apache.fop.pdf.StandardStructureTypes.Table;
import org.apache.fop.pdf.StructureHierarchyMember;
import org.apache.fop.pdf.StructureType;
import org.apache.fop.util.LanguageTags;
import org.apache.fop.util.XMLUtil;

public class PDFStructureTreeBuilder implements StructureTreeEventHandler {

    private static final String ROLE = "role";

    private static final Map<String, StructureElementBuilder> BUILDERS
            = new java.util.HashMap<String, StructureElementBuilder>();

    private static final StructureElementBuilder DEFAULT_BUILDER
            = new DefaultStructureElementBuilder(Grouping.NON_STRUCT);

    static {
        // Declarations and Pagination and Layout Formatting Objects
        StructureElementBuilder regionBuilder = new RegionBuilder();
        addBuilder("root",                      StandardStructureTypes.Grouping.DOCUMENT);
        addBuilder("page-sequence",             new PageSequenceBuilder());
        addBuilder("static-content",            regionBuilder);
        addBuilder("flow",                      regionBuilder);
        // Block-level Formatting Objects
        addBuilder("block", new LanguageHolderBuilder(StandardStructureTypes.Paragraphlike.P));
        addBuilder("block-container",           StandardStructureTypes.Grouping.DIV);
        // Inline-level Formatting Objects
        addBuilder("character", new LanguageHolderBuilder(StandardStructureTypes.InlineLevelStructure.SPAN));
        addBuilder("external-graphic",          new ImageBuilder());
        addBuilder("instream-foreign-object",   new ImageBuilder());
        addBuilder("inline",                    new InlineHolderBuilder());
        addBuilder("inline-container",          StandardStructureTypes.Grouping.DIV);
        addBuilder("page-number",               StandardStructureTypes.InlineLevelStructure.QUOTE);
        addBuilder("page-number-citation",      StandardStructureTypes.InlineLevelStructure.QUOTE);
        addBuilder("page-number-citation-last", StandardStructureTypes.InlineLevelStructure.QUOTE);
        // Formatting Objects for Tables
        addBuilder("table-and-caption",         StandardStructureTypes.Grouping.DIV);
        addBuilder("table",                     new TableBuilder());
        addBuilder("table-caption",             StandardStructureTypes.Grouping.CAPTION);
        addBuilder("table-header",              StandardStructureTypes.Table.THEAD);
        addBuilder("table-footer",              new TableFooterBuilder());
        addBuilder("table-body",                StandardStructureTypes.Table.TBODY);
        addBuilder("table-row",                 StandardStructureTypes.Table.TR);
        addBuilder("table-cell",                new TableCellBuilder());
        // Formatting Objects for Lists
        addBuilder("list-block",                StandardStructureTypes.List.L);
        addBuilder("list-item",                 StandardStructureTypes.List.LI);
        addBuilder("list-item-body",            StandardStructureTypes.List.LBODY);
        addBuilder("list-item-label",           StandardStructureTypes.List.LBL);
        // Dynamic Effects: Link and Multi Formatting Objects
        addBuilder("basic-link",                new LinkBuilder());
        // Out-of-Line Formatting Objects
        addBuilder("float",                     StandardStructureTypes.Grouping.DIV);
        addBuilder("footnote",                  StandardStructureTypes.InlineLevelStructure.NOTE);
        addBuilder("footnote-body",             StandardStructureTypes.Grouping.SECT);
        // Other Formatting Objects
        addBuilder("wrapper",                   StandardStructureTypes.InlineLevelStructure.SPAN);
        addBuilder("marker",                    StandardStructureTypes.Grouping.PRIVATE);
        addBuilder("retrieve-marker",           new PlaceholderBuilder());
        addBuilder("retrieve-table-marker",     new PlaceholderBuilder());

        addBuilder("#PCDATA", new PlaceholderBuilder());
    }

    private static void addBuilder(String fo, StructureType structureType) {
        addBuilder(fo, new DefaultStructureElementBuilder(structureType));
    }

    private static void addBuilder(String fo, StructureElementBuilder mapper) {
        BUILDERS.put(fo, mapper);
    }

    private interface StructureElementBuilder {

        PDFStructElem build(StructureHierarchyMember parent, Attributes attributes, PDFFactory pdfFactory,
                EventBroadcaster eventBroadcaster);

    }

    private static class DefaultStructureElementBuilder implements StructureElementBuilder {

        private final StructureType defaultStructureType;

        DefaultStructureElementBuilder(StructureType structureType) {
            this.defaultStructureType = structureType;
        }

        public PDFStructElem build(StructureHierarchyMember parent, Attributes attributes,
                PDFFactory pdfFactory, EventBroadcaster eventBroadcaster) {
            String role = attributes.getValue(ROLE);
            StructureType structureType;
            if (role == null) {
                structureType = defaultStructureType;
            } else {
                structureType = StandardStructureTypes.get(role);
                if (structureType == null) {
                    structureType = defaultStructureType;
                    PDFEventProducer.Provider.get(eventBroadcaster).nonStandardStructureType(role, role,
                            structureType.toString());
                }
            }
            PDFStructElem structElem = createStructureElement(parent, structureType);
            setAttributes(structElem, attributes);
            addKidToParent(structElem, parent, attributes);
            registerStructureElement(structElem, pdfFactory, attributes);
            return structElem;
        }

        protected PDFStructElem createStructureElement(StructureHierarchyMember parent,
                StructureType structureType) {
            return new PDFStructElem(parent, structureType);
        }

        protected void setAttributes(PDFStructElem structElem, Attributes attributes) {
        }

        protected void addKidToParent(PDFStructElem kid, StructureHierarchyMember parent,
                Attributes attributes) {
            parent.addKid(kid);
        }

        protected void registerStructureElement(PDFStructElem structureElement, PDFFactory pdfFactory,
                Attributes attributes) {
            pdfFactory.getDocument().registerStructureElement(structureElement);
        }

    }

    private static class PageSequenceBuilder extends DefaultStructureElementBuilder {

        PageSequenceBuilder() {
            super(StandardStructureTypes.Grouping.PART);
        }

        @Override
        protected PDFStructElem createStructureElement(StructureHierarchyMember parent,
                StructureType structureType) {
            return new PageSequenceStructElem(parent, structureType);
        }

    }

    private static class RegionBuilder extends DefaultStructureElementBuilder {

        RegionBuilder() {
            super(StandardStructureTypes.Grouping.SECT);
        }

        @Override
        protected void addKidToParent(PDFStructElem kid, StructureHierarchyMember parent,
                Attributes attributes) {
            String flowName = attributes.getValue(Flow.FLOW_NAME);
            ((PageSequenceStructElem) parent).addContent(flowName, kid);
        }

        public PDFStructElem build(StructureHierarchyMember parent, Attributes attributes,
                                         PDFFactory pdfFactory, EventBroadcaster eventBroadcaster) {
            if (pdfFactory.getDocument().isStaticRegionsPerPageForAccessibility()) {
                PageSequenceStructElem pageSequenceStructElem = (PageSequenceStructElem) parent;
                if (pageSequenceStructElem.sect == null) {
                    pageSequenceStructElem.sect = super.build(parent, attributes, pdfFactory, eventBroadcaster);
                }
                return pageSequenceStructElem.sect;
            }
            return super.build(parent, attributes, pdfFactory, eventBroadcaster);
        }
    }

    private static class LanguageHolderBuilder extends DefaultStructureElementBuilder {

        LanguageHolderBuilder(StructureType structureType) {
            super(structureType);
        }

        @Override
        protected void setAttributes(PDFStructElem structElem, Attributes attributes) {
            String xmlLang = attributes.getValue(XMLConstants.XML_NS_URI, "lang");
            if (xmlLang != null) {
                Locale locale = LanguageTags.toLocale(xmlLang);
                structElem.setLanguage(locale);
            }
        }

    }

    private static class InlineHolderBuilder extends DefaultStructureElementBuilder {

        InlineHolderBuilder() {
            super(StandardStructureTypes.InlineLevelStructure.SPAN);
        }

        @Override
        protected void setAttributes(PDFStructElem structElem, Attributes attributes) {
            String text = attributes.getValue(ExtensionElementMapping.URI, "abbreviation");
            if (text != null && !text.equals("")) {
                structElem.put("E", text);
            }
        }
    }

    private static class ImageBuilder extends DefaultStructureElementBuilder {

        ImageBuilder() {
            super(StandardStructureTypes.Illustration.FIGURE);
        }

        @Override
        protected void setAttributes(PDFStructElem structElem, Attributes attributes) {
            String altTextNode = attributes.getValue(ExtensionElementMapping.URI, "alt-text");
            if (altTextNode == null) {
                altTextNode = "No alternate text specified";
            }
            structElem.put("Alt", altTextNode);
        }

    }

    private static class LinkBuilder extends DefaultStructureElementBuilder {
        LinkBuilder() {
            super(StandardStructureTypes.InlineLevelStructure.LINK);
        }

        @Override
        protected void setAttributes(PDFStructElem structElem, Attributes attributes) {
            super.setAttributes(structElem, attributes);
            String altTextNode = attributes.getValue(ExtensionElementMapping.URI, "alt-text");
            if (altTextNode == null) {
                altTextNode = "No alternate text specified";
            }
            structElem.put("Alt", altTextNode);
        }
    }

    private static class TableBuilder extends DefaultStructureElementBuilder {

        TableBuilder() {
            super(StandardStructureTypes.Table.TABLE);
        }

        @Override
        protected PDFStructElem createStructureElement(StructureHierarchyMember parent,
                StructureType structureType) {
            return new TableStructElem(parent, structureType);
        }
    }

    private static class TableFooterBuilder extends DefaultStructureElementBuilder {

        public TableFooterBuilder() {
            super(StandardStructureTypes.Table.TFOOT);
        }

        @Override
        protected void addKidToParent(PDFStructElem kid, StructureHierarchyMember parent,
                Attributes attributes) {
            ((TableStructElem) parent).addTableFooter(kid);
        }
    }

    private static class TableCellBuilder extends DefaultStructureElementBuilder {

        TableCellBuilder() {
            super(StandardStructureTypes.Table.TD);
        }

        @Override
        protected void registerStructureElement(PDFStructElem structureElement, PDFFactory pdfFactory,
                Attributes attributes) {
            if (structureElement.getStructureType() == Table.TH) {
                String scopeAttribute = attributes.getValue(InternalElementMapping.URI,
                        InternalElementMapping.SCOPE);
                Scope scope = (scopeAttribute == null)
                        ? Scope.COLUMN
                        : Scope.valueOf(scopeAttribute.toUpperCase(Locale.ENGLISH));
                pdfFactory.getDocument().registerStructureElement(structureElement, scope);
            } else {
                pdfFactory.getDocument().registerStructureElement(structureElement);
            }
        }

        @Override
        protected void setAttributes(PDFStructElem structElem, Attributes attributes) {
            String columnSpan = attributes.getValue("number-columns-spanned");
            if (columnSpan != null) {
                structElem.setTableAttributeColSpan(Integer.parseInt(columnSpan));
            }
            String rowSpan = attributes.getValue("number-rows-spanned");
            if (rowSpan != null) {
                structElem.setTableAttributeRowSpan(Integer.parseInt(rowSpan));
            }
        }

    }

    private static class PlaceholderBuilder implements StructureElementBuilder {

        public PDFStructElem build(StructureHierarchyMember parent, Attributes attributes,
                PDFFactory pdfFactory, EventBroadcaster eventBroadcaster) {
            PDFStructElem elem = new PDFStructElem.Placeholder(parent);
            parent.addKid(elem);
            return elem;
        }

    }

    private PDFFactory pdfFactory;

    private EventBroadcaster eventBroadcaster;

    private LinkedList<StructureTreeElement> ancestors = new LinkedList<>();

    private PDFStructElem rootStructureElement;

    void setPdfFactory(PDFFactory pdfFactory) {
        this.pdfFactory = pdfFactory;
    }

    void setEventBroadcaster(EventBroadcaster eventBroadcaster) {
        this.eventBroadcaster = eventBroadcaster;
    }

    void setLogicalStructureHandler(PDFLogicalStructureHandler logicalStructureHandler) {
        createRootStructureElement(logicalStructureHandler);
    }

    private void createRootStructureElement(PDFLogicalStructureHandler logicalStructureHandler) {
        assert rootStructureElement == null;
        PDFParentTree parentTree = logicalStructureHandler.getParentTree();
        PDFStructTreeRoot structTreeRoot = pdfFactory.getDocument().makeStructTreeRoot(parentTree);
        rootStructureElement = createStructureElement("root", structTreeRoot,
                new AttributesImpl(), pdfFactory, eventBroadcaster);
    }

    public static PDFStructElem createStructureElement(String name, StructureHierarchyMember parent,
                Attributes attributes, PDFFactory pdfFactory, EventBroadcaster eventBroadcaster) {
            StructureElementBuilder builder = BUILDERS.get(name);
            if (builder == null) {
                // TODO is a fallback really necessary?
                builder = DEFAULT_BUILDER;
            }
            return builder.build(parent, attributes, pdfFactory, eventBroadcaster);
        }

    public void startPageSequence(Locale language, String role) {
        ancestors = new LinkedList<>();
        AttributesImpl attributes = new AttributesImpl();
        attributes.addAttribute("", ROLE, ROLE, XMLUtil.CDATA, role);
        PDFStructElem structElem = createStructureElement("page-sequence",
                rootStructureElement, attributes, pdfFactory, eventBroadcaster);
        if (language != null) {
            structElem.setLanguage(language);
        }
        ancestors.add(structElem);
    }

    public void endPageSequence() {
    }

    public StructureTreeElement startNode(String name, Attributes attributes, StructureTreeElement parent) {
        if (!isPDFA1Safe(name)) {
            return null;
        }
        StructureTreeElement parentElem;
        if (parent == null) {
            parentElem = ancestors.getFirst();
        } else {
            parentElem = parent;
        }
        StructureTreeElement structElem;
        if (pdfFactory.getDocument().isStaticRegionsPerPageForAccessibility()) {
            structElem = new Factory(name, parentElem, attributes);
        } else {
            structElem = createStructureElement(
                    name, (PDFStructElem)parentElem, attributes, pdfFactory, eventBroadcaster);
        }
        ancestors.addFirst(structElem);
        return structElem;
    }

    public class Factory implements StructureTreeElement {
        private String name;
        private StructureTreeElement parentElem;
        private Attributes attributes;
        private PDFStructElem cache;
        private int cachePage = -1;

        Factory(String name, StructureTreeElement parentElem, Attributes attributes) {
            this.name = name;
            this.parentElem = parentElem;
            this.attributes = new AttributesImpl(attributes);
        }

        public PDFStructElem createStructureElement() {
            if (cache != null) {
                return cache;
            }
            return createStructureElement(1);
        }

        public PDFStructElem createStructureElement(int pageNumber) {
            if (cachePage == pageNumber) {
                return cache;
            }
            StructureHierarchyMember parent;
            if (parentElem instanceof Factory) {
                parent = ((Factory) parentElem).createStructureElement(pageNumber);
            } else {
                parent = (StructureHierarchyMember) parentElem;
            }
            cache = PDFStructureTreeBuilder.createStructureElement(
                    name, parent, attributes, pdfFactory, eventBroadcaster);
            cachePage = pageNumber;
            return cache;
        }
    }

    public void endNode(String name) {
        if (isPDFA1Safe(name)) {
            ancestors.removeFirst();
        }
    }

    private boolean isPDFA1Safe(String name) {
        return !((pdfFactory.getDocument().getProfile().getPDFAMode().isPart1()
                || pdfFactory.getDocument().getProfile().getPDFUAMode().isEnabled())
                && (name.equals("table-body")
                || name.equals("table-header")
                || name.equals("table-footer")));
    }

    public StructureTreeElement startImageNode(String name, Attributes attributes, StructureTreeElement parent) {
        return startNode(name, attributes, parent);
    }

    public StructureTreeElement startReferencedNode(String name, Attributes attributes, StructureTreeElement parent) {
        return startNode(name, attributes, parent);
    }

}
