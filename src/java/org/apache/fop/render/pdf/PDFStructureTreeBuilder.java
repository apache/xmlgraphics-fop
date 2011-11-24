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

import org.apache.fop.accessibility.StructureTreeEventHandler;
import org.apache.fop.events.EventBroadcaster;
import org.apache.fop.fo.extensions.ExtensionElementMapping;
import org.apache.fop.fo.extensions.InternalElementMapping;
import org.apache.fop.pdf.PDFFactory;
import org.apache.fop.pdf.PDFStructElem;

class PDFStructureTreeBuilder implements StructureTreeEventHandler {

    private PDFFactory pdfFactory;

    private PDFLogicalStructureHandler logicalStructureHandler;

    private EventBroadcaster eventBroadcaster;

    private LinkedList<PDFStructElem> ancestors = new LinkedList<PDFStructElem>();

    void setPdfFactory(PDFFactory pdfFactory) {
        this.pdfFactory = pdfFactory;
    }

    void setLogicalStructureHandler(PDFLogicalStructureHandler logicalStructureHandler) {
        this.logicalStructureHandler = logicalStructureHandler;
    }

    void setEventBroadcaster(EventBroadcaster eventBroadcaster) {
        this.eventBroadcaster = eventBroadcaster;
    }

    public void startPageSequence(Locale locale) {
        ancestors = new LinkedList<PDFStructElem>();
        ancestors.add(logicalStructureHandler.createPageSequence(locale));
    }

    public void endPageSequence() {
    }

    public void startNode(String name, Attributes attributes) {
        PDFStructElem parent = ancestors.getFirst();
        String role = attributes.getValue("role");
        PDFStructElem created = pdfFactory.makeStructureElement(
                FOToPDFRoleMap.mapFormattingObject(name, role, parent,
                        eventBroadcaster), parent);
        if (ancestors.size() <= 2) { // TODO remove
            parent.addKid(created);
        }
        String ptr = attributes.getValue(InternalElementMapping.URI, "ptr");
        if (ptr != null) {
            logicalStructureHandler.addStructurePointer(ptr, created);
        }

        if (name.equals("external-graphic") || name.equals("instream-foreign-object")) {
            String altTextNode = attributes.getValue(ExtensionElementMapping.URI, "alt-text");
            if (altTextNode != null) {
                created.put("Alt", altTextNode);
            } else {
                created.put("Alt", "No alternate text specified");
            }
        }
        ancestors.addFirst(created);
    }

    public void endNode(String name) {
        ancestors.removeFirst();
    }

}
