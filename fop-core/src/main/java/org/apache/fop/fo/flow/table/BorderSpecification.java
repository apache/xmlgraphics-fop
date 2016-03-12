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
import org.apache.fop.fo.properties.CommonBorderPaddingBackground.BorderInfo;

/**
 * A border's informations, along with the FO element which declared it. Used for border
 * resolution in the collapsing-border model.
 */
public/*TODO*/ class BorderSpecification {

    private static BorderSpecification defaultBorder;

    private BorderInfo borderInfo;

    private int holder;

    /**
     * Creates a new border specification.
     *
     * @param borderInfo the border's informations
     * @param holder the FO element declaring this border
     */
    public/*TODO*/ BorderSpecification(BorderInfo borderInfo, int holder) {
        this.borderInfo = borderInfo;
        this.holder = holder;
    }

    static synchronized BorderSpecification getDefaultBorder() {
        if (defaultBorder == null) {
            defaultBorder = new BorderSpecification(CommonBorderPaddingBackground
                    .getDefaultBorderInfo(), Constants.FO_TABLE_CELL);
        }
        return defaultBorder;
    }

    /**
     * Returns this border's informations.
     *
     * @return this border's informations
     */
    public/*TODO*/ BorderInfo getBorderInfo() {
        return borderInfo;
    }

    /**
     * Returns the FO element declaring this border.
     *
     * @return one of {@link Constants#FO_TABLE}, {@link Constants#FO_TABLE_COLUMN},
     * {@link Constants#FO_TABLE_HEADER}, {@link Constants#FO_TABLE_FOOTER},
     * {@link Constants#FO_TABLE_BODY}, {@link Constants#FO_TABLE_ROW},
     * {@link Constants#FO_TABLE_CELL}
     */
    public/*TODO*/ int getHolder() {
        return holder;
    }

    /** {@inheritDoc} */
    public String toString() {
        String holderName = "";
        switch (holder) {
        case Constants.FO_TABLE: holderName = "table"; break;
        case Constants.FO_TABLE_COLUMN: holderName = "table-column"; break;
        case Constants.FO_TABLE_HEADER: holderName = "table-header"; break;
        case Constants.FO_TABLE_FOOTER: holderName = "table-footer"; break;
        case Constants.FO_TABLE_BODY: holderName = "table-body"; break;
        case Constants.FO_TABLE_ROW: holderName = "table-row"; break;
        case Constants.FO_TABLE_CELL: holderName = "table-cell"; break;
        default: assert false;
        }
        return "{" + borderInfo + ", " + holderName + "}";
    }
}
