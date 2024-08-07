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

import java.util.List;

import org.apache.fop.layoutmgr.LayoutManager;
import org.apache.fop.layoutmgr.ListElement;
import org.apache.fop.layoutmgr.Position;

/**
 * This class represents a Position specific to TableContentLayoutManager. Used for table
 * headers and footers at the beginning and end of a table.
 */
class TableHeaderFooterPosition extends Position {

    /** True indicates a position for a header, false for a footer. */
    protected boolean header;
    /** Element list representing the header/footer */
    protected List<ListElement> nestedElements;

    /**
     * Creates a new TableHeaderFooterPosition.
     * @param lm applicable layout manager
     * @param header True indicates a position for a header, false for a footer.
     * @param nestedElements Element list representing the header/footer
     */
    protected TableHeaderFooterPosition(LayoutManager lm,
            boolean header, List<ListElement> nestedElements) {
        super(lm);
        this.header = header;
        this.nestedElements = nestedElements;
    }

    /** {@inheritDoc} */
    public boolean generatesAreas() {
        return true;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("Table");
        sb.append(header ? "Header" : "Footer");
        sb.append("Position:");
        sb.append(getIndex()).append("(");
        sb.append(nestedElements);
        sb.append(")");
        return sb.toString();
    }
}
