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

import java.util.Iterator;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fo.FOEventHandler;
import org.apache.fop.fotreetest.FOTreeUnitTester;

/**
 * Superclass for testcases related to tables, factoring the common stuff.
 */
abstract class AbstractTableTest extends FOTreeUnitTester {

    private FOTreeUnitTester.FOEventHandlerFactory tableHandlerFactory;

    private TableHandler tableHandler;

    public AbstractTableTest() throws Exception {
        super();
        tableHandlerFactory = new FOEventHandlerFactory() {
            public FOEventHandler createFOEventHandler(FOUserAgent foUserAgent) {
                tableHandler = new TableHandler(foUserAgent);
                return tableHandler;
            }
        };
    }

    protected void setUp(String filename) throws Exception {
        setUp(filename, tableHandlerFactory);
    }

    protected TableHandler getTableHandler() {
        return tableHandler;
    }

    protected Iterator getTableIterator() {
        return tableHandler.getTables().iterator();
    }
}
