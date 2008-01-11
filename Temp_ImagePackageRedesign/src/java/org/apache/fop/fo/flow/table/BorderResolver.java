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

import java.util.List;

/**
 * A class dedicated to the resolution of borders in tables. It receives a series of
 * events as the table is parsed and performs border resolution accordingly.
 */
interface BorderResolver {

    /**
     * Receives notification of the end of a row.
     * 
     * @param row the row that has just been finished
     * @param container the FO element holding the given row
     */
    void endRow(List/*<GridUnit>*/ row, TableCellContainer container);

    /**
     * Receives notification of the start of a table-header/footer/body.
     * 
     * @param part the part that has started
     */
    void startPart(TableBody part);

    /**
     * Receives notification of the end of a table-header/footer/body.
     */
    void endPart();

    /**
     * Receives notification of the end of the table.
     */
    void endTable();
}
