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

import org.xml.sax.Attributes;

import org.apache.fop.accessibility.StructureTreeElement;
import org.apache.fop.accessibility.StructureTreeEventHandler;
import org.apache.fop.events.EventBroadcaster;
import org.apache.fop.fo.extensions.ExtensionElementMapping;
import org.apache.fop.pdf.PDFFactory;
import org.apache.fop.pdf.PDFObject;
import org.apache.fop.pdf.PDFParentTree;
import org.apache.fop.pdf.PDFStructElem;
import org.apache.fop.pdf.PDFStructTreeRoot;
import org.apache.fop.pdf.StandardStructureAttributes.Table.Scope;
import org.apache.fop.pdf.StandardStructureTypes.Table;
import org.apache.fop.pdf.StructureType;

class PDFStructureTreeBuilder implements StructureTreeEventHandler {

    private PDFFactory pdfFactory;

    private EventBroadcaster eventBroadcaster;

    private LinkedList<PDFStructElem> ancestors = new LinkedList<PDFStructElem>();

    private PDFStructElem rootStructureElement;

    void setPdfFactory(PDFFactory pdfFactory) {
        this.pdfFactory = pdfFactory;
    }

    void setLogicalStructureHandler(PDFLogicalStructureHandler logicalStructureHandler) {
        createRootStructureElement(logicalStructureHandler);
    }

    private void createRootStructureElement(PDFLogicalStructureHandler logicalStructureHandler) {
        assert rootStructureElement == null;
        PDFParentTree parentTree = logicalStructureHandler.getParentTree();
        PDFStructTreeRoot structTreeRoot = pdfFactory.getDocument().makeStructTreeRoot(parentTree);
        rootStructureElement = createStructureElement("root", structTreeRoot, null);
        structTreeRoot.addKid(rootStructureElement);
    }

    void setEventBroadcaster(EventBroadcaster eventBroadcaster) {
        this.eventBroadcaster = eventBroadcaster;
    }

    public void startPageSequence(Locale language, String role) {
        ancestors = new LinkedList<PDFStructElem>();
        PDFStructElem structElem = createStructureElement("page-sequence", rootStructureElement, role);
        if (language != null) {
            structElem.setLanguage(language);
        }
        rootStructureElement.addKid(structElem);
        ancestors.add(structElem);
    }

    private PDFStructElem createStructureElement(String name, PDFObject parent, String role) {
        StructureType structureType = FOToPDFRoleMap.mapFormattingObject(name, role, parent,
                eventBroadcaster);
        if (structureType == Table.TH) {
            return pdfFactory.getDocument().makeStructureElement(structureType, parent, Scope.COLUMN);
        } else {
            return pdfFactory.getDocument().makeStructureElement(structureType, parent);
        }
    }

    public void endPageSequence() {
    }

    public StructureTreeElement startNode(String name, Attributes attributes) {
        PDFStructElem parent = ancestors.getFirst();
        String role = attributes.getValue("role");
        PDFStructElem structElem = createStructureElement(name, parent, role);
        setSpanAttributes(structElem, attributes);
        parent.addKid(structElem);
        ancestors.addFirst(structElem);
        return structElem;
    }

    private void setSpanAttributes(PDFStructElem structElem, Attributes attributes) {
        String columnSpan = attributes.getValue("number-columns-spanned");
        if (columnSpan != null) {
            structElem.setTableAttributeColSpan(Integer.parseInt(columnSpan));
        }
        String rowSpan = attributes.getValue("number-rows-spanned");
        if (rowSpan != null) {
            structElem.setTableAttributeRowSpan(Integer.parseInt(rowSpan));
        }
    }

    public void endNode(String name) {
        removeFirstAncestor();
    }

    private void removeFirstAncestor() {
        ancestors.removeFirst();
    }

    public StructureTreeElement startImageNode(String name, Attributes attributes) {
        PDFStructElem parent = ancestors.getFirst();
        String role = attributes.getValue("role");
        PDFStructElem structElem = createStructureElement(name, parent, role);
        parent.addKid(structElem);
        String altTextNode = attributes.getValue(ExtensionElementMapping.URI, "alt-text");
        if (altTextNode != null) {
            structElem.put("Alt", altTextNode);
        } else {
            structElem.put("Alt", "No alternate text specified");
        }
        ancestors.addFirst(structElem);
        return structElem;
    }

    public StructureTreeElement startReferencedNode(String name, Attributes attributes) {
        PDFStructElem parent = ancestors.getFirst();
        String role = attributes.getValue("role");
        PDFStructElem structElem;
        if ("#PCDATA".equals(name)) {
            structElem = new PDFStructElem.Placeholder(parent);
        } else {
            structElem = createStructureElement(name, parent, role);
        }
        parent.addKid(structElem);
        ancestors.addFirst(structElem);
        return structElem;
    }

}
