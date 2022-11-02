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

import java.util.LinkedList;
import java.util.List;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fo.FOEventHandler;

public class TableHandler extends FOEventHandler {

    /** All the tables encountered in the FO file. List of Table objects. */
    private List tables = new LinkedList();

    TableHandler(FOUserAgent foUserAgent) {
        super(foUserAgent);
    }

    public void endTable(Table tbl) {
        tables.add(tbl);
    }

    List getTables() {
        return tables;
    }
}
