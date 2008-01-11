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
import org.apache.fop.layoutmgr.Position;

/**
 * This class represents a Position specific to TableContentLayoutManager. Used for table
 * headers and footers at breaks.
 */
class TableHFPenaltyPosition extends Position {

    /** Element list for the header */
    protected List headerElements;
    /** Element list for the footer */
    protected List footerElements;

    /**
     * Creates a new TableHFPenaltyPosition
     * @param lm applicable layout manager
     */
    protected TableHFPenaltyPosition(LayoutManager lm) {
        super(lm);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("TableHFPenaltyPosition:");
        sb.append(getIndex()).append("(");
        sb.append("header:");
        sb.append(headerElements);
        sb.append(", footer:");
        sb.append(footerElements);
        sb.append(")");
        return sb.toString();
    }
}
