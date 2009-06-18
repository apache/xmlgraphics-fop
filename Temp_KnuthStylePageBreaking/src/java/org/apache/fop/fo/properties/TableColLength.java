/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

package org.apache.fop.fo.properties;

import org.apache.fop.datatypes.PercentBase;
import org.apache.fop.fo.FObj;

/**
 * A table-column width specification, possibly including some
 * number of proportional "column-units". The absolute size of a
 * column-unit depends on the fixed and proportional sizes of all
 * columns in the table, and on the overall size of the table.
 * It can't be calculated until all columns have been specified and until
 * the actual width of the table is known. Since this can be specified
 * as a percent of its parent containing width, the calculation is done
 * during layout.
 * NOTE: this is only supposed to be allowed if table-layout=fixed.
 */
public class TableColLength extends LengthProperty {
    /**
     * Number of table-column proportional units
     */
    private double tcolUnits;

    /**
     * The column the this column-units are defined on. 
     */
    private FObj column;

    /**
     * Construct an object with tcolUnits of proportional measure.
     * @param tcolUnits number of table-column proportional units
     */
    public TableColLength(double tcolUnits, FObj column) {
        this.tcolUnits = tcolUnits;
        this.column = column;
    }

    /**
     * Override the method in Length
     * @return the number of specified proportional table-column units.
     */
    public double getTableUnits() {
        return tcolUnits;
    }

    /**
     * Return false because table-col-units are a relative numeric.
     * @see org.apache.fop.datatypes.Numeric#isAbsolute()
     */
    public boolean isAbsolute() {
        return false;
    }

    /**
     * Return the value as a numeric value.
     * @see org.apache.fop.datatypes.Numeric#getNumericValue()
     */
    public double getNumericValue() {
        return tcolUnits * column.getLayoutDimension(PercentBase.TABLE_UNITS).floatValue();
    }

    /**
     * Return the value as a length.
     * @see org.apache.fop.datatypes.Length#getValue()
     */
    public int getValue() {
        return (int) (tcolUnits * column.getLayoutDimension(PercentBase.TABLE_UNITS).floatValue());
    }

    /**
     * Convert this to a String
     * @return the string representation of this
     */
    public String toString() {
        return (Double.toString(tcolUnits) + " table-column-units");
    }

}
