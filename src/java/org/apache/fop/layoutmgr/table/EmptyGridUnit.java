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

import org.apache.fop.fo.flow.TableBody;
import org.apache.fop.fo.flow.TableColumn;
import org.apache.fop.fo.flow.TableRow;

/**
 * GridUnit subclass for empty grid units.
 */
public class EmptyGridUnit extends GridUnit {

    private TableRow row;
    private TableBody body;
    
    /**
     * @param row Optional table-row instance
     * @param column table-column instance
     * @param body table-body the grid unit belongs to
     * @param startCol column index 
     */
    public EmptyGridUnit(TableRow row, TableColumn column, TableBody body, 
            int startCol) {
        super(null, null, column, startCol, 0);
        this.row = row;
        this.body = body;
    }
    
    /** @see org.apache.fop.layoutmgr.table.GridUnit#isPrimary() */
    public boolean isPrimary() {
        return true;
    }
    
    /** @see org.apache.fop.layoutmgr.table.GridUnit#getBody() */
    public TableBody getBody() {
        return this.body;
    }

    /** @see org.apache.fop.layoutmgr.table.GridUnit#getRow() */
    public TableRow getRow() {
        return this.row;
    }
}
