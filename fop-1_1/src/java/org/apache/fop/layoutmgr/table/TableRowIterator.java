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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FONode.FONodeIterator;
import org.apache.fop.fo.flow.table.EffRow;
import org.apache.fop.fo.flow.table.Table;
import org.apache.fop.fo.flow.table.TableBody;

/**
 * Iterator that lets the table layout manager step over all the rows of a part of the
 * table (table-header, table-footer or table-body).
 * <p>Note: This class is not thread-safe.</p>
 */
public class TableRowIterator {

    /** Selects the table-body elements for iteration. */
    public static final int BODY = 0;
    /** Selects the table-header elements for iteration. */
    public static final int HEADER = 1;
    /** Selects the table-footer elements for iteration. */
    public static final int FOOTER = 2;

    /** The table on which this instance operates. */
    protected Table table;

    /** Part of the table over which to iterate. One of BODY, HEADER or FOOTER. */
    private int tablePart;

    private Iterator rowGroupsIter;

    private int rowIndex = 0;

    /**
     * Creates a new TableRowIterator.
     * @param table the table to iterate over
     * @param tablePart indicates what part of the table to iterate over (HEADER, FOOTER, BODY)
     */
    public TableRowIterator(Table table, int tablePart) {
        this.table = table;
        this.tablePart = tablePart;
        switch(tablePart) {
            case HEADER:
                rowGroupsIter = table.getTableHeader().getRowGroups().iterator();
                break;
            case FOOTER:
                rowGroupsIter = table.getTableFooter().getRowGroups().iterator();
                break;
            case BODY:
                List rowGroupsList = new LinkedList();
                // TODO this is ugly
                for (FONodeIterator iter = table.getChildNodes(); iter.hasNext();) {
                    FONode node = iter.nextNode();
                    if (node instanceof TableBody) {
                        rowGroupsList.addAll(((TableBody) node).getRowGroups());
                    }
                }
                rowGroupsIter = rowGroupsList.iterator();
                break;
            default:
                throw new IllegalArgumentException("Unrecognised TablePart: " + tablePart);
        }
    }

    /**
     * Returns the next row group if any. A row group in this context is the minimum number of
     * consecutive rows which contains all spanned grid units of its cells.
     * @return the next row group, or null
     */
    EffRow[] getNextRowGroup() {
        if (!rowGroupsIter.hasNext()) {
            return null;
        }
        List rowGroup = (List) rowGroupsIter.next();
        EffRow[] effRowGroup = new EffRow[rowGroup.size()];
        int i = 0;
        for (Iterator rowIter = rowGroup.iterator(); rowIter.hasNext();) {
            List gridUnits = (List) rowIter.next();
            effRowGroup[i++] = new EffRow(rowIndex++, tablePart, gridUnits);
        }
        return effRowGroup;
    }

}
