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

package org.apache.fop.layoutmgr.table;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import junit.framework.TestCase;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.fo.flow.Table;
import org.xml.sax.XMLReader;

/**
 * Tests that TableRowIterator returns, for each part of a table, the expected number of
 * row-groups with the expected number or rows in each.
 */
public class TableRowIteratorTestCase extends TestCase {

    private XMLReader foReader;

    private TableHandler tableHandler;

    /** Returns Table instances. */
    private Iterator tableIterator;

    /** Returns ColumnSetup instances. */
    private Iterator columnSetupIterator;

    /**
     * Creates the SAX parser for the FO file and a FO user agent with an overriden
     * FOEventHandler and sets them up.
     */
    public void setUp() throws Exception {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        spf.setValidating(false);
        SAXParser parser;
        parser = spf.newSAXParser();
        foReader = parser.getXMLReader();

        FopFactory fopFactory = FopFactory.newInstance(); 
        FOUserAgent ua = fopFactory.newFOUserAgent();
        tableHandler = new TableHandler(ua);
        ua.setFOEventHandlerOverride(tableHandler);

        Fop fop = fopFactory.newFop(ua);

        foReader.setContentHandler(fop.getDefaultHandler());
        foReader.setDTDHandler(fop.getDefaultHandler());
        foReader.setErrorHandler(fop.getDefaultHandler());
        foReader.setEntityResolver(fop.getDefaultHandler());
    }

    /**
     * Prepares the iterators over the tables contained in the given FO file.
     * 
     * @param filename basename of a test FO file
     * @throws Exception
     */
    private void setUp(String filename) throws Exception {
        foReader.parse(new File("test/layoutmgr/table/" + filename).toURL().toExternalForm());
        List tables = tableHandler.getTables();
        List columnSetups = new LinkedList();
        tableIterator = tables.iterator();
        for (Iterator i = tables.iterator(); i.hasNext();) {
            columnSetups.add(new ColumnSetup((Table) i.next()));
        }
        columnSetupIterator = columnSetups.iterator();
    }

    /**
     * Checks that the given iterator will return row groups as expected. More precisely,
     * checks that the number of row groups corresponds to the size of the given array,
     * and that the number of rows inside each row group is equal to the corresponding
     * integer in the array.
     * 
     * @param tri an iterator over a given part of a table (HEADER, FOOTER, BODY)
     * @param expectedRowLengths expected lengths of all the row groups of this part of
     * the table
     */
    private void checkTablePartRowGroups(TableRowIterator tri, int[] expectedRowLengths) {
        for (int i = 0; i < expectedRowLengths.length; i++) {
            EffRow[] row = tri.getNextRowGroup();
            assertTrue(row.length == expectedRowLengths[i]);
        }
        assertNull(tri.getNextRowGroup());        
    }

    /**
     * Gets the next table and checks its row-groups.
     * 
     * @param expectedHeaderRowLengths expected row-group sizes for the header. If null
     * the table is not expected to have a header
     * @param expectedFooterRowLengths expected row-group sizes for the footer. If null
     * the table is not expected to have a footer
     * @param expectedBodyRowLengths expected row-group sizes for the body(-ies)
     */
    private void checkNextTableRowGroups(int[] expectedHeaderRowLengths,
            int[] expectedFooterRowLengths, int[] expectedBodyRowLengths) {
        Table table = (Table) tableIterator.next();
        ColumnSetup columnSetup = (ColumnSetup) columnSetupIterator.next();
        TableRowIterator tri;
        if (expectedHeaderRowLengths != null) {
            tri = new TableRowIterator(table, columnSetup, TableRowIterator.HEADER);
            checkTablePartRowGroups(tri, expectedHeaderRowLengths);
        }
        if (expectedFooterRowLengths != null) {
            tri = new TableRowIterator(table, columnSetup, TableRowIterator.FOOTER);
            checkTablePartRowGroups(tri, expectedFooterRowLengths);
        }
        tri = new TableRowIterator(table, columnSetup, TableRowIterator.BODY);
        checkTablePartRowGroups(tri, expectedBodyRowLengths);
    }

    public void checkSimple(String filename) throws Exception {
        setUp(filename);

        // Table 1: no header, no footer, one body (1 row)
        checkNextTableRowGroups(null, null, new int[] {1});

        // Table 2: no header, no footer, one body (2 rows)
        checkNextTableRowGroups(null, null, new int[] {1, 1});

        // Table 3: no header, no footer, two bodies (1 row, 1 row)
        checkNextTableRowGroups(null, null, new int[] {1, 1});

        // Table 4: no header, no footer, two bodies (2 rows, 3 rows)
        checkNextTableRowGroups(null, null, new int[] {1, 1, 1, 1, 1});

        // Table 5: one header (1 row), no footer, one body (1 row)
        checkNextTableRowGroups(new int[] {1}, null, new int[] {1});

        // Table 6: no header, one footer (1 row), one body (1 row)
        checkNextTableRowGroups(null, new int[] {1}, new int[] {1});

        // Table 7: one header (1 row), one footer (1 row), one body (1 row)
        checkNextTableRowGroups(new int[] {1}, new int[] {1}, new int[] {1});

        // Table 8: one header (2 rows), one footer (3 rows), one body (2 rows)
        checkNextTableRowGroups(new int[] {1, 1}, new int[] {1, 1, 1}, new int[] {1, 1});

        // Table 9: one header (3 rows), one footer (2 rows), three bodies (2 rows, 1 row, 3 rows)
        checkNextTableRowGroups(new int[] {1, 1, 1}, new int[] {1, 1}, new int[] {1, 1, 1, 1, 1, 1});
    }

    public void checkSpans(String filename) throws Exception {
        setUp(filename);

        // Table 1: no header, no footer, one body (1 row with column-span)
        checkNextTableRowGroups(null, null, new int[] {1});

        // Table 2: no header, no footer, one body (1 row-group of 2 rows)
        checkNextTableRowGroups(null, null, new int[] {2});

        // Table 3: no header, no footer, one body (1 row-group of 2 rows, 1 row) 
        checkNextTableRowGroups(null, null, new int[] {2, 1});

        // Table 4: no header, no footer, one body (1 row, 1 row-group of 2 rows)
        checkNextTableRowGroups(null, null, new int[] {1, 2});

        // Table 5: no header, no footer, one body (1 row, 1 row-group of 3 rows, 1 row) 
        checkNextTableRowGroups(null, null, new int[] {1, 3, 1});

        // Table 6: one header (1 row-group of 2 rows), one footer (1 row, 1 row-group of 3 rows),
        // one body (1 row-group of 2 rows, 1 row, 1 row-group of 3 rows) 
        checkNextTableRowGroups(new int[] {2}, new int[] {1, 3}, new int[] {2, 1, 3});
    }

    public void testWithRowsSimple() throws Exception {
        checkSimple("TableRowIterator_simple.fo");
    }

    public void testWithRowsSpans() throws Exception {
        checkSpans("TableRowIterator_spans.fo");
    }

    public void testNoRowSimple() throws Exception {
        checkSimple("TableRowIterator_no-row_simple.fo");
    }

    public void testNoRowSpans() throws Exception {
        checkSpans("TableRowIterator_no-row_spans.fo");
    }
}