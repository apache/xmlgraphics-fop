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

package org.apache.fop.fo.flow;

// FOP
import org.apache.fop.fo.FONode;

import org.xml.sax.Locator;
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.ValidationException;

/**
 * Class modelling the fo:table-footer object.
 */
public class TableFooter extends TableBody {

    /**
     * @param parent FONode that is the parent of this object
     */
    public TableFooter(FONode parent) {
        super(parent);
    }

    private boolean tableRowsFound = false;
    private boolean tableColumnsFound = false;

    /**
     * @see org.apache.fop.fo.FONode#startOfNode
     */
    protected void startOfNode() throws FOPException {
//      getFOEventHandler().startBody(this);
    }

    /**
     * @see org.apache.fop.fo.FONode#validateChildNode(Locator, String, String)
     * XSL Content Model: marker* (table-row+|table-cell+)
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName) 
        throws ValidationException {
        if (nsURI == FO_URI) {
            if (localName.equals("marker")) {
                if (tableRowsFound || tableColumnsFound) {
                   nodesOutOfOrderError(loc, "fo:marker", "(table-row+|table-cell+)");
                }
            } else if (localName.equals("table-row")) {
                tableRowsFound = true;
                if (tableColumnsFound) {
                    invalidChildError(loc, nsURI, localName, "Either fo:table-rows" +
                      " or fo:table-columns may be children of an fo:table-footer" +
                      " but not both");
                }
            } else if (localName.equals("table-column")) {
                tableColumnsFound = true;
                if (tableRowsFound) {
                    invalidChildError(loc, nsURI, localName, "Either fo:table-rows" +
                      " or fo:table-columns may be children of an fo:table-footer" +
                      " but not both");
                }  
            } else {
                invalidChildError(loc, nsURI, localName);
            }
        } else {
            invalidChildError(loc, nsURI, localName);
        }
    }

    /**
     * @see org.apache.fop.fo.FONode#endOfNode
     */
    protected void endOfNode() throws FOPException {
//      getFOEventHandler().endFooter(this);
        if (!(tableRowsFound || tableColumnsFound)) {
            missingChildElementError("marker* (table-row+|table-cell+)");
        }
//      convertCellsToRows();
    }

    /**
     * @see org.apache.fop.fo.FObj#getName()
     */
    public String getName() {
        return "fo:table-footer";
    }

    /**
     * @see org.apache.fop.fo.FObj#getNameId()
     */
    public int getNameId() {
        return FO_TABLE_FOOTER;
    }
}
