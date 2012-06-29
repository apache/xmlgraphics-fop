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

import java.util.HashMap;
import java.util.Map;

import org.apache.fop.events.EventBroadcaster;
import org.apache.fop.pdf.PDFName;
import org.apache.fop.pdf.PDFObject;
import org.apache.fop.pdf.PDFStructElem;

/**
 * This class provides the standard mappings from Formatting Objects to PDF structure types.
 */
final class FOToPDFRoleMap {

    /**
     * Standard structure types defined by the PDF Reference, Fourth Edition (PDF 1.5).
     */
    private static final Map<String, PDFName> STANDARD_STRUCTURE_TYPES
            = new HashMap<String, PDFName>();

    private static final Map<String, Mapper> DEFAULT_MAPPINGS
            = new java.util.HashMap<String, Mapper>();

    private static final PDFName THEAD;
    private static final PDFName NON_STRUCT;

    static {
        // Create PDFNames for the standard structure types
        // Table 10.18: Grouping elements
        addStructureType("Document");
        addStructureType("Part");
        addStructureType("Art");
        addStructureType("Sect");
        addStructureType("Div");
        addStructureType("BlockQuote");
        addStructureType("Caption");
        addStructureType("TOC");
        addStructureType("TOCI");
        addStructureType("Index");
        addStructureType("NonStruct");
        addStructureType("Private");
        // Table 10.20: Paragraphlike elements
        addStructureType("H");
        addStructureType("H1");
        addStructureType("H2");
        addStructureType("H3");
        addStructureType("H4");
        addStructureType("H5");
        addStructureType("H6");
        addStructureType("P");
        // Table 10.21: List elements
        addStructureType("L");
        addStructureType("LI");
        addStructureType("Lbl");
        addStructureType("LBody");
        // Table 10.22: Table elements
        addStructureType("Table");
        addStructureType("TR");
        addStructureType("TH");
        addStructureType("TD");
        addStructureType("THead");
        addStructureType("TBody");
        addStructureType("TFoot");
        // Table 10.23: Inline-level structure elements
        addStructureType("Span");
        addStructureType("Quote");
        addStructureType("Note");
        addStructureType("Reference");
        addStructureType("BibEntry");
        addStructureType("Code");
        addStructureType("Link");
        addStructureType("Annot");
        // Table 10.24: Ruby and Warichu elements
        addStructureType("Ruby");
        addStructureType("RB");
        addStructureType("RT");
        addStructureType("RP");
        addStructureType("Warichu");
        addStructureType("WT");
        addStructureType("WP");
        // Table 10.25: Illustration elements
        addStructureType("Figure");
        addStructureType("Formula");
        addStructureType("Form");

        NON_STRUCT = STANDARD_STRUCTURE_TYPES.get("NonStruct");
        assert NON_STRUCT != null;
        THEAD = STANDARD_STRUCTURE_TYPES.get("THead");
        assert THEAD != null;

        // Create the standard mappings
        // Declarations and Pagination and Layout Formatting Objects
        addMapping("root",                      "Document");
        addMapping("page-sequence",             "Part");
        addMapping("flow",                      "Sect");
        addMapping("static-content",            "Sect");
        // Block-level Formatting Objects
        addMapping("block",                     "P");
        addMapping("block-container",           "Div");
        // Inline-level Formatting Objects
        addMapping("character",                 "Span");
        addMapping("external-graphic",          "Figure");
        addMapping("instream-foreign-object",   "Figure");
        addMapping("inline",                    "Span");
        addMapping("inline-container",          "Div");
        addMapping("page-number",               "Quote");
        addMapping("page-number-citation",      "Quote");
        addMapping("page-number-citation-last", "Quote");
        // Formatting Objects for Tables
        addMapping("table-and-caption",         "Div");
        addMapping("table",                     "Table");
        addMapping("table-caption",             "Caption");
        addMapping("table-header",              "THead");
        addMapping("table-footer",              "TFoot");
        addMapping("table-body",                "TBody");
        addMapping("table-row",                 "TR");
        addMapping("table-cell",                new TableCellMapper());
        // Formatting Objects for Lists
        addMapping("list-block",                "L");
        addMapping("list-item",                 "LI");
        addMapping("list-item-body",            "LBody");
        addMapping("list-item-label",           "Lbl");
        // Dynamic Effects: Link and Multi Formatting Objects
        addMapping("basic-link",                "Link");
        // Out-of-Line Formatting Objects
        addMapping("float",                     "Div");
        addMapping("footnote",                  "Note");
        addMapping("footnote-body",             "Sect");
        addMapping("wrapper",                   "Span");
        addMapping("marker",                    "Private");
    }

    private static void addStructureType(String structureType) {
        STANDARD_STRUCTURE_TYPES.put(structureType, new PDFName(structureType));
    }

    private static void addMapping(String fo, String structureType) {
        PDFName type = STANDARD_STRUCTURE_TYPES.get(structureType);
        assert type != null;
        addMapping(fo, new SimpleMapper(type));
    }

    private static void addMapping(String fo, Mapper mapper) {
        DEFAULT_MAPPINGS.put(fo, mapper);
    }


    /**
     * Maps a Formatting Object to a PDFName representing the associated structure type.
     * @param fo the formatting object's local name
     * @param role the value of the formatting object's role property
     * @param parent the parent of the structure element to be mapped
     * @param eventBroadcaster the event broadcaster
     * @return the structure type or null if no match could be found
     */
    public static PDFName mapFormattingObject(String fo, String role,
            PDFObject parent, EventBroadcaster eventBroadcaster) {
        PDFName type = null;
        if (role == null) {
            type = getDefaultMappingFor(fo, parent);
        } else {
            type = STANDARD_STRUCTURE_TYPES.get(role);
            if (type == null) {
                type = getDefaultMappingFor(fo, parent);
                PDFEventProducer.Provider.get(eventBroadcaster).nonStandardStructureType(fo,
                        fo, role, type.toString().substring(1));
            }
        }
        assert type != null;
        return type;
    }

    /**
     * Maps a Formatting Object to a PDFName representing the associated structure type.
     * @param fo the formatting object's local name
     * @param parent the parent of the structure element to be mapped
     * @return the structure type or NonStruct if no match could be found
     */
    private static PDFName getDefaultMappingFor(String fo, PDFObject parent) {
        Mapper mapper = DEFAULT_MAPPINGS.get(fo);
        if (mapper != null) {
            return mapper.getStructureType(parent);
        } else {
            return NON_STRUCT;
        }
    }

    private interface Mapper {
        PDFName getStructureType(PDFObject parent);
    }

    private static class SimpleMapper implements Mapper {

        private PDFName structureType;

        public SimpleMapper(PDFName structureType) {
            this.structureType = structureType;
        }

        public PDFName getStructureType(PDFObject parent) {
            return structureType;
        }

    }

    private static class TableCellMapper implements Mapper {

        public PDFName getStructureType(PDFObject parent) {
            PDFStructElem grandParent = ((PDFStructElem) parent).getParentStructElem();
            //TODO What to do with cells from table-footer? Currently they are mapped on TD.
            PDFName type;
            if (THEAD.equals(grandParent.getStructureType())) {
               type = STANDARD_STRUCTURE_TYPES.get("TH");
            } else {
                type = STANDARD_STRUCTURE_TYPES.get("TD");
            }
            assert type != null;
            return type;
        }

    }

    private FOToPDFRoleMap() { }
}
