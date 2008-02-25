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


/**
 * GridUnit subclass for empty grid units.
 */
public class EmptyGridUnit extends GridUnit {

    private TableBody body;

    /**
     * @param table the containing table
     * @param row the table-row element this grid unit belongs to (if any)
     * @param colIndex column index, 0-based
     */
    EmptyGridUnit(Table table, TableRow row, int colIndex) {
        super(table, 0, 0);
        setRow(row);
    }

    /** {@inheritDoc} */
    protected void setBordersFromCell() {
        borderBefore = ConditionalBorder.getDefaultBorder(collapsingBorderModel);
        borderAfter = ConditionalBorder.getDefaultBorder(collapsingBorderModel);
        borderStart = BorderSpecification.getDefaultBorder();
        borderEnd = BorderSpecification.getDefaultBorder();
    }

    /** {@inheritDoc} */
    public PrimaryGridUnit getPrimary() {
        throw new UnsupportedOperationException();
//        return this; TODO
    }

    /** {@inheritDoc} */
    public boolean isPrimary() {
        return false;
    }

    /** {@inheritDoc} */
    public TableBody getBody() {
        return this.body;
    }

    /** {@inheritDoc} */
    public boolean isLastGridUnitColSpan() {
        return true;
    }

    /** {@inheritDoc} */
    public boolean isLastGridUnitRowSpan() {
        return true;
    }
}
