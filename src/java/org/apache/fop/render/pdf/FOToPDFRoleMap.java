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

import java.util.Map;

import org.apache.fop.events.EventBroadcaster;
import org.apache.fop.pdf.PDFObject;
import org.apache.fop.pdf.PDFStructElem;
import org.apache.fop.pdf.StandardStructureTypes;
import org.apache.fop.pdf.StructureType;

/**
 * This class provides the standard mappings from Formatting Objects to PDF structure types.
 */
final class FOToPDFRoleMap {

    private static final Map<String, Mapper> DEFAULT_MAPPINGS = new java.util.HashMap<String, Mapper>();

    static {
        // Create the standard mappings
        // Declarations and Pagination and Layout Formatting Objects
        addMapping("root",                      StandardStructureTypes.Grouping.DOCUMENT);
        addMapping("page-sequence",             StandardStructureTypes.Grouping.PART);
        addMapping("flow",                      StandardStructureTypes.Grouping.SECT);
        addMapping("static-content",            StandardStructureTypes.Grouping.SECT);
        // Block-level Formatting Objects
        addMapping("block",                     StandardStructureTypes.Paragraphlike.P);
        addMapping("block-container",           StandardStructureTypes.Grouping.DIV);
        // Inline-level Formatting Objects
        addMapping("character",                 StandardStructureTypes.InlineLevelStructure.SPAN);
        addMapping("external-graphic",          StandardStructureTypes.Illustration.FIGURE);
        addMapping("instream-foreign-object",   StandardStructureTypes.Illustration.FIGURE);
        addMapping("inline",                    StandardStructureTypes.InlineLevelStructure.SPAN);
        addMapping("inline-container",          StandardStructureTypes.Grouping.DIV);
        addMapping("page-number",               StandardStructureTypes.InlineLevelStructure.QUOTE);
        addMapping("page-number-citation",      StandardStructureTypes.InlineLevelStructure.QUOTE);
        addMapping("page-number-citation-last", StandardStructureTypes.InlineLevelStructure.QUOTE);
        // Formatting Objects for Tables
        addMapping("table-and-caption",         StandardStructureTypes.Grouping.DIV);
        addMapping("table",                     StandardStructureTypes.Table.TABLE);
        addMapping("table-caption",             StandardStructureTypes.Grouping.CAPTION);
        addMapping("table-header",              StandardStructureTypes.Table.THEAD);
        addMapping("table-footer",              StandardStructureTypes.Table.TFOOT);
        addMapping("table-body",                StandardStructureTypes.Table.TBODY);
        addMapping("table-row",                 StandardStructureTypes.Table.TR);
        addMapping("table-cell",                new TableCellMapper());
        // Formatting Objects for Lists
        addMapping("list-block",                StandardStructureTypes.List.L);
        addMapping("list-item",                 StandardStructureTypes.List.LI);
        addMapping("list-item-body",            StandardStructureTypes.List.LBODY);
        addMapping("list-item-label",           StandardStructureTypes.List.LBL);
        // Dynamic Effects: Link and Multi Formatting Objects
        addMapping("basic-link",                StandardStructureTypes.InlineLevelStructure.LINK);
        // Out-of-Line Formatting Objects
        addMapping("float",                     StandardStructureTypes.Grouping.DIV);
        addMapping("footnote",                  StandardStructureTypes.InlineLevelStructure.NOTE);
        addMapping("footnote-body",             StandardStructureTypes.Grouping.SECT);
        addMapping("wrapper",                   StandardStructureTypes.InlineLevelStructure.SPAN);
        addMapping("marker",                    StandardStructureTypes.Grouping.PRIVATE);
    }

    private static void addMapping(String fo, StructureType structureType) {
        addMapping(fo, new SimpleMapper(structureType));
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
    public static StructureType mapFormattingObject(String fo, String role,
            PDFObject parent, EventBroadcaster eventBroadcaster) {
        StructureType type = null;
        if (role == null) {
            type = getDefaultMappingFor(fo, parent);
        } else {
            type = StandardStructureTypes.get(role);
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
    private static StructureType getDefaultMappingFor(String fo, PDFObject parent) {
        Mapper mapper = DEFAULT_MAPPINGS.get(fo);
        if (mapper != null) {
            return mapper.getStructureType(parent);
        } else {
            return StandardStructureTypes.Grouping.NON_STRUCT;
        }
    }

    private interface Mapper {
        StructureType getStructureType(PDFObject parent);
    }

    private static class SimpleMapper implements Mapper {

        private StructureType structureType;

        public SimpleMapper(StructureType structureType) {
            this.structureType = structureType;
        }

        public StructureType getStructureType(PDFObject parent) {
            return structureType;
        }

    }

    private static class TableCellMapper implements Mapper {

        public StructureType getStructureType(PDFObject parent) {
            PDFStructElem grandParent = ((PDFStructElem) parent).getParentStructElem();
            //TODO What to do with cells from table-footer? Currently they are mapped on TD.
            if (grandParent.getStructureType() == StandardStructureTypes.Table.THEAD) {
               return StandardStructureTypes.Table.TH;
            } else {
                return StandardStructureTypes.Table.TD;
            }
        }

    }

    private FOToPDFRoleMap() { }
}
