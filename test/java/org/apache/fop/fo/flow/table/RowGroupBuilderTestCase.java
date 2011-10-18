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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;

import org.junit.Test;

/**
 * Tests that RowGroupBuilder returns, for each part of a table, the expected number of
 * row-groups with the expected number or rows in each.
 */
public class RowGroupBuilderTestCase extends AbstractTableTestCase {

    public RowGroupBuilderTestCase() throws Exception {
        super();
    }

    /**
     * Checks that the given table-body(header,footer) will return row groups as expected.
     * More precisely, checks that the number of row groups corresponds to the size of the
     * given array, and that the number of rows inside each row group is equal to the
     * corresponding integer in the array.
     *
     * @param part a table part whose row groups are to be checked
     * @param expectedRowLengths expected lengths of all the row groups of this part of
     * the table
     */
    private void checkTablePartRowGroups(TablePart part, int[] expectedRowLengths) {
        Iterator rowGroupIter = part.getRowGroups().iterator();
        for (int i = 0; i < expectedRowLengths.length; i++) {
            assertTrue(rowGroupIter.hasNext());
            List rowGroup = (List) rowGroupIter.next();
            assertEquals(expectedRowLengths[i], rowGroup.size());
        }
        assertFalse(rowGroupIter.hasNext());
    }

    /**
     * Gets the next table and checks its row-groups.
     * @param tableIter an iterator over the tables to check
     * @param expectedHeaderRowLengths expected row-group sizes for the header. If null
     * the table is not expected to have a header
     * @param expectedFooterRowLengths expected row-group sizes for the footer. If null
     * the table is not expected to have a footer
     * @param expectedBodyRowLengths expected row-group sizes for the body(-ies)
     */
    private void checkNextTableRowGroups(Iterator tableIter,
            int[] expectedHeaderRowLengths, int[] expectedFooterRowLengths, int[][] expectedBodyRowLengths) {
        Table table = (Table) tableIter.next();
        if (expectedHeaderRowLengths == null) {
            assertNull(table.getTableHeader());
        } else {
            checkTablePartRowGroups(table.getTableHeader(), expectedHeaderRowLengths);
        }
        if (expectedFooterRowLengths == null) {
            assertNull(table.getTableFooter());
        } else {
            checkTablePartRowGroups(table.getTableFooter(), expectedFooterRowLengths);
        }
        Iterator bodyIter = table.getChildNodes();
        for (int i = 0; i < expectedBodyRowLengths.length; i++) {
            assertTrue(bodyIter.hasNext());
            checkTablePartRowGroups((TableBody) bodyIter.next(), expectedBodyRowLengths[i]);
        }
    }

    public void checkSimple(String filename) throws Exception {
        setUp(filename);
        Iterator tableIter = getTableIterator();

        // Table 1: no header, no footer, one body (1 row)
        checkNextTableRowGroups(tableIter, null, null, new int[][] {{1}});

        // Table 2: no header, no footer, one body (2 rows)
        checkNextTableRowGroups(tableIter, null, null, new int[][] {{1, 1}});

        // Table 3: no header, no footer, two bodies (1 row, 1 row)
        checkNextTableRowGroups(tableIter, null, null, new int[][] {{1}, {1}});

        // Table 4: no header, no footer, two bodies (2 rows, 3 rows)
        checkNextTableRowGroups(tableIter, null, null, new int[][] {{1, 1}, {1, 1, 1}});

        // Table 5: one header (1 row), no footer, one body (1 row)
        checkNextTableRowGroups(tableIter, new int[] {1}, null, new int[][] {{1}});

        // Table 6: no header, one footer (1 row), one body (1 row)
        checkNextTableRowGroups(tableIter, null, new int[] {1}, new int[][] {{1}});

        // Table 7: one header (1 row), one footer (1 row), one body (1 row)
        checkNextTableRowGroups(tableIter, new int[] {1}, new int[] {1}, new int[][] {{1}});

        // Table 8: one header (2 rows), one footer (3 rows), one body (2 rows)
        checkNextTableRowGroups(tableIter, new int[] {1, 1}, new int[] {1, 1, 1}, new int[][] {{1, 1}});

        // Table 9: one header (3 rows), one footer (2 rows), three bodies (2 rows, 1 row, 3 rows)
        checkNextTableRowGroups(tableIter, new int[] {1, 1, 1}, new int[] {1, 1}, new int[][] {{1, 1}, {1}, {1, 1, 1}});
    }

    public void checkSpans(String filename) throws Exception {
        setUp(filename);
        Iterator tableIter = getTableIterator();

        // Table 1: no header, no footer, one body (1 row with column-span)
        checkNextTableRowGroups(tableIter, null, null, new int[][] {{1}});

        // Table 2: no header, no footer, one body (1 row-group of 2 rows)
        checkNextTableRowGroups(tableIter, null, null, new int[][] {{2}});

        // Table 3: no header, no footer, one body (1 row-group of 2 rows, 1 row)
        checkNextTableRowGroups(tableIter, null, null, new int[][] {{2, 1}});

        // Table 4: no header, no footer, one body (1 row, 1 row-group of 2 rows)
        checkNextTableRowGroups(tableIter, null, null, new int[][] {{1, 2}});

        // Table 5: no header, no footer, one body (1 row, 1 row-group of 3 rows, 1 row)
        checkNextTableRowGroups(tableIter, null, null, new int[][] {{1, 3, 1}});

        // Table 6: one header (1 row-group of 2 rows), one footer (1 row, 1 row-group of 3 rows),
        // one body (1 row-group of 2 rows, 1 row, 1 row-group of 3 rows)
        checkNextTableRowGroups(tableIter, new int[] {2}, new int[] {1, 3}, new int[][] {{2, 1, 3}});
    }

    @Test
    public void testWithRowsSimple() throws Exception {
        checkSimple("table/RowGroupBuilder_simple.fo");
    }

    @Test
    public void testWithRowsSpans() throws Exception {
        checkSpans("table/RowGroupBuilder_spans.fo");
    }

    @Test
    public void testNoRowSimple() throws Exception {
        checkSimple("table/RowGroupBuilder_no-row_simple.fo");
    }

    @Test
    public void testNoRowSpans() throws Exception {
        checkSpans("table/RowGroupBuilder_no-row_spans.fo");
    }

    @Test
    public void testNoColWithRowsSimple() throws Exception {
        checkSimple("table/RowGroupBuilder_no-col_simple.fo");
    }

    @Test
    public void testNoColWithRowsSpans() throws Exception {
        checkSpans("table/RowGroupBuilder_no-col_spans.fo");
    }

    @Test
    public void testNoColNoRowSimple() throws Exception {
        checkSimple("table/RowGroupBuilder_no-col_no-row_simple.fo");
    }

    @Test
    public void testNoColNoRowSpans() throws Exception {
        checkSpans("table/RowGroupBuilder_no-col_no-row_spans.fo");
    }
}
