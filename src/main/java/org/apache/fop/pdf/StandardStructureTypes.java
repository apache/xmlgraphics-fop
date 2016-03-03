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

package org.apache.fop.pdf;

import java.util.HashMap;
import java.util.Map;

/**
 * Standard structure types, as defined in section 10.7.4 of the PDF Reference, Fourth Edition (PDF 1.5).
 */
public final class StandardStructureTypes {

    public static final class Grouping {

        public static final StructureType DOCUMENT = new StructureTypeImpl("Document");
        public static final StructureType PART = new StructureTypeImpl("Part");
        public static final StructureType ART = new StructureTypeImpl("Art");
        public static final StructureType SECT = new StructureTypeImpl("Sect");
        public static final StructureType DIV = new StructureTypeImpl("Div");
        public static final StructureType BLOCK_QUOTE = new StructureTypeImpl("BlockQuote");
        public static final StructureType CAPTION = new StructureTypeImpl("Caption");
        public static final StructureType TOC = new StructureTypeImpl("TOC");
        public static final StructureType TOCI = new StructureTypeImpl("TOCI");
        public static final StructureType INDEX = new StructureTypeImpl("Index");
        public static final StructureType NON_STRUCT = new StructureTypeImpl("NonStruct");
        public static final StructureType PRIVATE = new StructureTypeImpl("Private");
    }

    public static final class Paragraphlike {
        public static final StructureType H = new StructureTypeImpl("H");
        public static final StructureType H1 = new StructureTypeImpl("H1");
        public static final StructureType H2 = new StructureTypeImpl("H2");
        public static final StructureType H3 = new StructureTypeImpl("H3");
        public static final StructureType H4 = new StructureTypeImpl("H4");
        public static final StructureType H5 = new StructureTypeImpl("H5");
        public static final StructureType H6 = new StructureTypeImpl("H6");
        public static final StructureType P = new StructureTypeImpl("P");
    }

    public static final class List {
        public static final StructureType L = new StructureTypeImpl("L");
        public static final StructureType LI = new StructureTypeImpl("LI");
        public static final StructureType LBL = new StructureTypeImpl("Lbl");
        public static final StructureType LBODY = new StructureTypeImpl("LBody");
    }

    public static final class Table {
        public static final StructureType TABLE = new StructureTypeImpl("Table");
        public static final StructureType TR = new StructureTypeImpl("TR");
        public static final StructureType TH = new StructureTypeImpl("TH");
        public static final StructureType TD = new StructureTypeImpl("TD");
        public static final StructureType THEAD = new StructureTypeImpl("THead");
        public static final StructureType TBODY = new StructureTypeImpl("TBody");
        public static final StructureType TFOOT = new StructureTypeImpl("TFoot");
    }

    public static final class InlineLevelStructure {
        public static final StructureType SPAN = new StructureTypeImpl("Span");
        public static final StructureType QUOTE = new StructureTypeImpl("Quote");
        public static final StructureType NOTE = new StructureTypeImpl("Note");
        public static final StructureType REFERENCE = new StructureTypeImpl("Reference");
        public static final StructureType BIB_ENTRY = new StructureTypeImpl("BibEntry");
        public static final StructureType CODE = new StructureTypeImpl("Code");
        public static final StructureType LINK = new StructureTypeImpl("Link");
        public static final StructureType ANNOT = new StructureTypeImpl("Annot");
    }

    public static final class RubyOrWarichu {
        public static final StructureType RUBY = new StructureTypeImpl("Ruby");
        public static final StructureType RB = new StructureTypeImpl("RB");
        public static final StructureType RT = new StructureTypeImpl("RT");
        public static final StructureType RP = new StructureTypeImpl("RP");
        public static final StructureType WARICHU = new StructureTypeImpl("Warichu");
        public static final StructureType WT = new StructureTypeImpl("WT");
        public static final StructureType WP = new StructureTypeImpl("WP");
    }

    public static final class Illustration {
        public static final StructureType FIGURE = new StructureTypeImpl("Figure");
        public static final StructureType FORMULA = new StructureTypeImpl("Formula");
        public static final StructureType FORM = new StructureTypeImpl("Form");
    }

    private static class StructureTypeImpl implements StructureType {

        private final PDFName name;

        protected StructureTypeImpl(String name) {
            this.name = new PDFName(name);
            StandardStructureTypes.STRUCTURE_TYPES.put(name, this);
        }

        public PDFName getName() {
            return name;
        }

        @Override
        public String toString() {
            return name.toString().substring(1);
        }

    }

    private static final Map<String, StructureType> STRUCTURE_TYPES = new HashMap<String, StructureType>();

    private StandardStructureTypes() { }

    /**
     * Returns the standard structure type of the given name.
     *
     * @param name the name of a structure type, case sensitive. For example, Document,
     * Sect, H1, etc.
     * @return the corresponding {@code StructureType} instance, or {@code null} if the given
     * name does not correspond to a standard structure type
     */
    public static StructureType get(String name) {
        return STRUCTURE_TYPES.get(name);
    }

}
