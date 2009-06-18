/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;


/**
 * Class modelling the fo:table-header object.
 */
public class TableHeader extends TableBody {

    /**
     * @param parent FONode that is the parent of this object
     */
    public TableHeader(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FONode#startOfNode
     */
    protected void startOfNode() throws FOPException {
        initPendingSpans();
        //getFOEventHandler().startHeader(this);
    }

    /**
     * @see org.apache.fop.fo.FONode#endOfNode
     */
    protected void endOfNode() throws FOPException {
//      getFOEventHandler().endHeader(this);
        if (!(tableRowsFound || tableCellsFound)) {
            missingChildElementError("marker* (table-row+|table-cell+)");
        }
//      convertCellsToRows();
    }

    /** @see org.apache.fop.fo.FObj#getLocalName() */
    public String getLocalName() {
        return "table-header";
    }

    /** @see org.apache.fop.fo.FObj#getNameId() */
    public int getNameId() {
        return FO_TABLE_HEADER;
    }
}
