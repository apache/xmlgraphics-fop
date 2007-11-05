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

package org.apache.fop.fo.flow;

import java.util.List;

import org.apache.fop.layoutmgr.table.EmptyGridUnit;

/**
 * A row group builder accommodating a variable number of columns. More flexible, but less
 * efficient.
 */
class VariableColRowGroupBuilder extends RowGroupBuilder {

    VariableColRowGroupBuilder(Table t) {
        super(t);
        numberOfColumns = 1;
    }

    /**
     * Fills the given row group with empty grid units if necessary, so that it matches
     * the given number of columns.
     * 
     * @param rowGroup a List of List of GridUnit
     * @param numberOfColumns the number of columns that the row group must have
     */
    static void fillWithEmptyGridUnits(List rowGroup, int numberOfColumns) {
        for (int i = 0; i < rowGroup.size(); i++) {
            List effRow = (List) rowGroup.get(i);
            for (int j = effRow.size(); j < numberOfColumns; j++) {
                effRow.add(new EmptyGridUnit(null, null, null, j));
            }
        }
    }

    /**
     * Updates the current row group to match the given number of columns, by adding empty
     * grid units if necessary.
     * 
     * @param numberOfColumns new number of columns
     */
    void ensureNumberOfColumns(int numberOfColumns) {
        this.numberOfColumns = numberOfColumns;
        fillWithEmptyGridUnits(rows, numberOfColumns);
    }

}
