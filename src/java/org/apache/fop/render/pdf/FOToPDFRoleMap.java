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

import org.apache.fop.pdf.PDFName;
import org.apache.fop.pdf.PDFObject;
import org.apache.fop.pdf.PDFStructElem;

/**
 * This class provides the standard mappings from Formatting Objects to PDF structure types.
 */
public final class FOToPDFRoleMap {

    private static final Map STANDARD_MAPPINGS = new java.util.HashMap();

    private static final PDFName TFOOT = new PDFName("TFoot");
    private static final PDFName THEAD = new PDFName("THead");

    static {
        addMapping("block",                     "P");
        addMapping("block-container",           "Div");

        PDFName st = new PDFName("Span");
        addMapping("inline",                    st);
        addMapping("wrapper",                   st);
        addMapping("character",                 st);

        addMapping("root",                      "Document");
        addMapping("page-sequence",             "Part");
        addMapping("flow",                      "Sect");
        addMapping("static-content",            "Sect");

        st = new PDFName("Quote");
        addMapping("page-number",               st);
        addMapping("page-number-citation",      st);
        addMapping("page-number-citation-last", st);

        st = new PDFName("Figure");
        addMapping("external-graphic",          st);
        addMapping("instream-foreign-object",   st);

        addMapping("table",                     "Table");
        addMapping("table-body",                "TBody");
        addMapping("table-header",              THEAD);
        addMapping("table-footer",              TFOOT);
        addMapping("table-row",                 "TR");
        addMapping("table-cell",                new TableCellMapper());

        addMapping("list-block",                "L");
        addMapping("list-item",                 "LI");
        addMapping("list-item-label",           "Lbl");
        addMapping("list-item-body",            "LBody");

        addMapping("basic-link",                "Link");
        addMapping("footnote",                  "Note");
        addMapping("footnote-body",             "Sect");
        addMapping("marker",                    "Private");
    }

    private static void addMapping(String fo, String pdfName) {
        addMapping(fo, new PDFName(pdfName));
    }

    private static void addMapping(String fo, PDFName pdfName) {
        addMapping(fo, new SimpleMapper(pdfName));
    }

    private static void addMapping(String fo, Mapper mapper) {
        STANDARD_MAPPINGS.put(fo, mapper);
    }

    /**
     * Maps a Formatting Object to a PDFName representing the associated structure type.
     * @param fo the formatting object's local name
     * @param parent the parent of the structure element to be mapped
     * @return the structure type or null if no match could be found
     */
    public static PDFName mapFormattingObject(String fo, PDFObject parent) {
        Mapper mapper = (Mapper)STANDARD_MAPPINGS.get(fo);
        if (mapper != null) {
            return mapper.getStructureType(parent);
        }
        return null;
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

        private static final PDFName TD = new PDFName("TD");
        private static final PDFName TH = new PDFName("TH");

        public PDFName getStructureType(PDFObject parent) {
            PDFStructElem grandParent = (PDFStructElem)
                ((PDFStructElem)parent).getParentStructElem();
            //TODO What to do with cells from table-footer? Currently they are mapped on TD.
            if (THEAD.equals(grandParent.getStructureType())) {
               return TH;
            } else {
               return TD;
            }
        }

    }

    private FOToPDFRoleMap() { }
}
