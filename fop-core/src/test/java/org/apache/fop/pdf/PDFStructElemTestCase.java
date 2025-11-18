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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.apache.fop.pdf.StandardStructureAttributes.Table.Scope;

public class PDFStructElemTestCase {

    @Test
    public void testSetTableAttributeColumnSpanHeader() throws IOException {
        defaultSetTableAttributeRowColumnSpan(Scope.COLUMN, StandardStructureTypes.Table.THEAD);
        defaultSetTableAttributeRowColumnSpan(Scope.COLUMN, StandardStructureTypes.Table.TH);
    }

    @Test
    public void testSetTableAttributeRowSpanHeader() throws IOException {
        defaultSetTableAttributeRowColumnSpan(Scope.ROW, StandardStructureTypes.Table.THEAD);
        defaultSetTableAttributeRowColumnSpan(Scope.ROW, StandardStructureTypes.Table.TH);
    }

    @Test
    public void testSetTableAttributeRowSpanNonHeaderType() throws IOException {
        defaultSetTableAttributeRowColumnSpan(Scope.ROW, StandardStructureTypes.Table.TABLE);
        defaultSetTableAttributeRowColumnSpan(Scope.ROW, StandardStructureTypes.Table.TBODY);
        defaultSetTableAttributeRowColumnSpan(Scope.ROW, StandardStructureTypes.Table.TR);
        defaultSetTableAttributeRowColumnSpan(Scope.ROW, StandardStructureTypes.Table.TD);
        defaultSetTableAttributeRowColumnSpan(Scope.ROW, StandardStructureTypes.Table.TFOOT);
    }

    @Test
    public void testSetTableAttributeColumnSpanNonHeaderType() throws IOException {
        defaultSetTableAttributeRowColumnSpan(Scope.COLUMN, StandardStructureTypes.Table.TABLE);
        defaultSetTableAttributeRowColumnSpan(Scope.COLUMN, StandardStructureTypes.Table.TBODY);
        defaultSetTableAttributeRowColumnSpan(Scope.COLUMN, StandardStructureTypes.Table.TR);
        defaultSetTableAttributeRowColumnSpan(Scope.COLUMN, StandardStructureTypes.Table.TD);
        defaultSetTableAttributeRowColumnSpan(Scope.COLUMN, StandardStructureTypes.Table.TFOOT);
    }

    private void defaultSetTableAttributeRowColumnSpan(Scope scope, StructureType type) throws IOException {
        String span;
        PDFStructElem structElem = new PDFStructElem(null, type);
        if (scope == Scope.COLUMN) {
            structElem.setTableAttributeColSpan(2);
            span = "ColSpan";
        } else {
            structElem.setTableAttributeRowSpan(2);
            span = "RowSpan";
        }

        structElem.writeDictionary(new ByteArrayOutputStream(), new StringBuilder());

        PDFDictionary dict = (PDFDictionary) structElem.get("A");
        assertEquals("Must add the table name", "Table", ((PDFName) dict.get("O")).getName());
        assertEquals("Must add the span set by the setTableAttribute method", 2, dict.get(span));

        if (type == StandardStructureTypes.Table.THEAD || type == StandardStructureTypes.Table.TH) {
            assertEquals("Must return the scope added initially", scope.getName(), dict.get("Scope"));
        } else {
            assertNull("Must only add the scope for table headers", dict.get("Scope"));
        }
    }
}
