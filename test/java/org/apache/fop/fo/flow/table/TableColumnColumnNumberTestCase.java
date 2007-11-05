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

package org.apache.fop.fo.flow.table;

import java.util.Iterator;

import org.apache.fop.datatypes.PercentBaseContext;
import org.apache.fop.fo.FObj;


public class TableColumnColumnNumberTestCase extends AbstractTableTestCase {

    /**
     * A percentBaseContext that mimics the behaviour of TableLM for computing the widths
     * of columns. All what needs to be known is the width of a table unit (as in
     * proportional-column-width()).
     */
    private class TablePercentBaseContext implements PercentBaseContext {

        private int unitaryWidth;

        void setUnitaryWidth(int unitaryWidth) {
            this.unitaryWidth = unitaryWidth;
        }

        public int getBaseLength(int lengthBase, FObj fobj) {
            return unitaryWidth;
        }
    }

    private TablePercentBaseContext percentBaseContext = new TablePercentBaseContext();

    public TableColumnColumnNumberTestCase() throws Exception {
        super();
    }

    private void checkColumn(Table t, int number, boolean isImplicit, int spans, int repeated, int width) {
        TableColumn c = t.getColumn(number - 1);
        // TODO a repeated column has a correct number only for its first occurrence 
//        assertEquals(number, c.getColumnNumber());
        assertEquals(isImplicit, c.isImplicitColumn());
        assertEquals(spans, c.getNumberColumnsSpanned());
        assertEquals(repeated, c.getNumberColumnsRepeated());
        assertEquals(width, c.getColumnWidth().getValue(percentBaseContext));
    }

    public void testColumnNumber() throws Exception {
        setUp("table/table-column_column-number.fo");
        Iterator tableIter = getTableIterator();
        Table t = (Table) tableIter.next();
        assertEquals(2, t.getNumberOfColumns());
        checkColumn(t, 1, false, 1, 2, 100000);
        checkColumn(t, 2, false, 1, 2, 100000);

        t = (Table) tableIter.next();
        assertEquals(2, t.getNumberOfColumns());
        checkColumn(t, 1, false, 1, 1, 200000);
        checkColumn(t, 2, false, 1, 1, 100000);

        t = (Table) tableIter.next();
        assertEquals(3, t.getNumberOfColumns());
        checkColumn(t, 1, false, 1, 1, 100000);
        checkColumn(t, 2, false, 1, 1, 150000);
        checkColumn(t, 3, false, 1, 1, 200000);

        t = (Table) tableIter.next();
        percentBaseContext.setUnitaryWidth(125000);
        assertEquals(4, t.getNumberOfColumns());
        checkColumn(t, 1, false, 1, 1, 100000);
        checkColumn(t, 2, true, 1, 1, 125000);
        checkColumn(t, 3, false, 1, 1, 150000);
        checkColumn(t, 4, false, 1, 1, 175000);
    }

    private void checkImplicitColumns(Iterator tableIter, int columnNumber) {
        Table t = (Table) tableIter.next();
        assertEquals(columnNumber, t.getNumberOfColumns());
        for (int i = 1; i <= columnNumber; i++) {
            checkColumn(t, i, true, 1, 1, 100000);
        }
    }

    public void testImplicitColumns() throws Exception {
        setUp("table/implicit_columns_column-number.fo");
        percentBaseContext.setUnitaryWidth(100000);
        Iterator tableIter = getTableIterator();

        checkImplicitColumns(tableIter, 2);
        checkImplicitColumns(tableIter, 2);
        checkImplicitColumns(tableIter, 2);
        checkImplicitColumns(tableIter, 2);
        checkImplicitColumns(tableIter, 3);
        checkImplicitColumns(tableIter, 4);
        checkImplicitColumns(tableIter, 3);
    }
}
