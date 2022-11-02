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

/**
 * Standard attributes, as defined in section 10.7.5 of the PDF Reference, Fourth edition (PDF 1.5).
 */
public final class StandardStructureAttributes {

    public static final class Table {

        /**
         * The name to use as an attribute owner. This is the value of the 'O' entry in
         * the attribute's dictionary.
         */
        public static final PDFName NAME = new PDFName("Table");

        public static enum Scope {
            ROW("Row"),
            COLUMN("Column"),
            BOTH("Both");

            private final PDFName name;

            private Scope(String name) {
                this.name = new PDFName(name);
            }

            /**
             * Returns the name of this attribute.
             *
             * @return a name suitable for use as a value in the attribute's dictionary
             */
            public PDFName getName() {
                return name;
            }

            /**
             * Sets the given scope on the given table header element.
             */
            static void addScopeAttribute(PDFStructElem th, Scope scope) {
                PDFDictionary scopeAttribute = new PDFDictionary();
                scopeAttribute.put("O", Table.NAME);
                scopeAttribute.put("Scope", scope.getName());
                th.put("A", scopeAttribute);
            }
        }
    }

    private StandardStructureAttributes() { }

}
