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

import org.apache.fop.fo.Constants;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;

/**
 * GridUnit subclass for empty grid units.
 */
public class EmptyGridUnit extends GridUnit {

    private TableRow row;
    private TableBody body;

    /**
     * @param table the containing table
     * @param startRow index of the row this grid unit belongs to, 0-based
     * @param startCol column index, 0-based
     */
    public EmptyGridUnit(Table table, int startRow, int startCol) {
        super(table, table.getColumn(startCol), startCol, 0, 0);
    }

    /** {@inheritDoc} */
    protected void setBorder(int side) {
        resolvedBorders[side] = new BorderSpecification(
                new CommonBorderPaddingBackground.BorderInfo(Constants.EN_NONE, null, null),
                Constants.FO_TABLE_CELL);
    }

    /** {@inheritDoc} */
    public PrimaryGridUnit getPrimary() {
        throw new UnsupportedOperationException();
//        return this; TODO
    }

    /** {@inheritDoc} */
    public boolean isPrimary() {
        return true;
    }

    /** {@inheritDoc} */
    public TableBody getBody() {
        return this.body;
    }

    /** {@inheritDoc} */
    public TableRow getRow() {
        return this.row;
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
