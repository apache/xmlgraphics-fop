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

import org.xml.sax.Locator;

import org.apache.fop.events.EventBroadcaster;
import org.apache.fop.events.EventProducer;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.expr.PropertyException;

/**
 * Event producer interface for table-specific XSL-FO validation messages.
 */
public interface TableEventProducer extends EventProducer {

    /** Provider class for the event producer. */
    class Provider {

        /**
         * Returns an event producer.
         * @param broadcaster the event broadcaster to use
         * @return the event producer
         */
        public static TableEventProducer get(EventBroadcaster broadcaster) {
            return (TableEventProducer)broadcaster.getEventProducerFor(
                    TableEventProducer.class);
        }
    }

    /**
     * A value other than "auto" has been specified on fo:table.
     * @param source the event source
     * @param loc the location of the error or null
     * @event.severity WARN
     */
    void nonAutoBPDOnTable(Object source, Locator loc);

    /**
     * Padding on fo:table is ignored if the collapsing border model is active.
     * @param source the event source
     * @param loc the location of the error or null
     * @event.severity WARN
     */
    void noTablePaddingWithCollapsingBorderModel(Object source, Locator loc);

    /**
     * No mixing of table-rows and table-cells is allowed for direct children of table-body.
     * @param source the event source
     * @param elementName the name of the context node
     * @param loc the location of the error or null
     * @throws ValidationException the validation error provoked by the method call
     * @event.severity FATAL
     */
    void noMixRowsAndCells(Object source, String elementName, Locator loc)
            throws ValidationException;

    /**
     * The table-footer was found after the table-body. FOP cannot recover with collapsed border
     * model.
     * @param source the event source
     * @param elementName the name of the context node
     * @param loc the location of the error or null
     * @throws ValidationException the validation error provoked by the method call
     * @event.severity FATAL
     */
    void footerOrderCannotRecover(Object source, String elementName, Locator loc)
            throws ValidationException;

    /**
     * starts-row/ends-row for fo:table-cells non-applicable for children of an fo:table-row
     * @param source the event source
     * @param loc the location of the error or null
     * @event.severity WARN
     */
    void startEndRowUnderTableRowWarning(Object source, Locator loc);

    /**
     * Column-number or number of cells in the row overflows the number of fo:table-column
     * specified for the table.
     * @param source the event source
     * @param loc the location of the error or null
     * @throws ValidationException the validation error provoked by the method call
     * @event.severity FATAL
     */
    void tooManyCells(Object source, Locator loc) throws ValidationException;

    /**
     * Property value must be 1 or bigger.
     * @param source the event source
     * @param propName the property name
     * @param actualValue the actual value
     * @param loc the location of the error or null
     * @throws PropertyException the property error provoked by the method call
     * @event.severity FATAL
     */
    void valueMustBeBiggerGtEqOne(Object source, String propName,
            int actualValue, Locator loc) throws PropertyException;

    /**
     * table-layout=\"fixed\" and column-width unspecified
     * => falling back to proportional-column-width(1)
     * @param source the event source
     * @param loc the location of the error or null
     * @event.severity WARN
     */
    void warnImplicitColumns(Object source, Locator loc);

    /**
     * padding-* properties are not applicable.
     * @param source the event source
     * @param elementName the name of the context node
     * @param loc the location of the error or null
     * @event.severity WARN
     */
    void paddingNotApplicable(Object source, String elementName, Locator loc);

    /**
     * Cell overlap.
     * @param source the event source
     * @param elementName the name of the context node
     * @param column the column index of the overlapping cell
     * @param loc the location of the error or null
     * @throws PropertyException the property error provoked by the method call
     * @event.severity FATAL
     */
    void cellOverlap(Object source, String elementName, int column,
            Locator loc) throws PropertyException;

    /**
     * @param source the event source
     * @param elementName the name of the context node
     * @param propValue the user-specified value of the column-number property
     * @param columnNumber the generated value for the column-number property
     * @param loc the location of the error or null
     * @event.severity WARN
     */
    void forceNextColumnNumber(Object source, String elementName, Number propValue,
                               int columnNumber, Locator loc);

    /**
     * Break ignored due to row spanning.
     * @param source the event source
     * @param elementName the name of the context node
     * @param breakBefore true for "break-before", false for "break-after"
     * @param loc the location of the error or null
     * @event.severity WARN
     */
    void breakIgnoredDueToRowSpanning(Object source, String elementName, boolean breakBefore,
            Locator loc);


}
