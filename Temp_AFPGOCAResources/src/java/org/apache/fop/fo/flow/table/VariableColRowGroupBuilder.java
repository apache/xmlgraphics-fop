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
import java.util.LinkedList;
import java.util.List;

import org.apache.fop.fo.ValidationException;


/**
 * A row group builder accommodating a variable number of columns. More flexible, but less
 * efficient.
 */
class VariableColRowGroupBuilder extends RowGroupBuilder {

    VariableColRowGroupBuilder(Table t) {
        super(t);
    }

    /**
     * Each event is recorded and will be played once the table is finished, and the final
     * number of columns known.
     */
    private static interface Event {
        /**
         * Plays this event
         *
         * @param rowGroupBuilder the delegate builder which will actually create the row
         * groups
         * @throws ValidationException if a row-spanning cell overflows its parent body
         */
        void play(RowGroupBuilder rowGroupBuilder) throws ValidationException;
    }

    /** The queue of events sent to this builder. */
    private List events = new LinkedList();

    /** {@inheritDoc} */
    void addTableCell(final TableCell cell) {
        events.add(new Event() {
            public void play(RowGroupBuilder rowGroupBuilder) {
                rowGroupBuilder.addTableCell(cell);
            }
        });
    }

    /** {@inheritDoc} */
    void startTableRow(final TableRow tableRow) {
        events.add(new Event() {
            public void play(RowGroupBuilder rowGroupBuilder) {
                rowGroupBuilder.startTableRow(tableRow);
            }
        });
    }

    /** {@inheritDoc} */
    void endTableRow() {
        events.add(new Event() {
            public void play(RowGroupBuilder rowGroupBuilder) {
                rowGroupBuilder.endTableRow();
            }
        });
    }

    /** {@inheritDoc} */
    void endRow(final TablePart part) {
        events.add(new Event() {
            public void play(RowGroupBuilder rowGroupBuilder) {
                rowGroupBuilder.endRow(part);
            }
        });
    }

    /** {@inheritDoc} */
    void startTablePart(final TablePart part) {
        events.add(new Event() {
            public void play(RowGroupBuilder rowGroupBuilder) {
                rowGroupBuilder.startTablePart(part);
            }
        });
    }

    /** {@inheritDoc} */
    void endTablePart() throws ValidationException {
        // TODO catch the ValidationException sooner?
        events.add(new Event() {
            public void play(RowGroupBuilder rowGroupBuilder) throws ValidationException {
                rowGroupBuilder.endTablePart();
            }
        });
    }

    /** {@inheritDoc} */
    void endTable() throws ValidationException {
        RowGroupBuilder delegate = new FixedColRowGroupBuilder(table);
        for (Iterator eventIter = events.iterator(); eventIter.hasNext();) {
            ((Event) eventIter.next()).play(delegate);
        }
        delegate.endTable();
    }
}
