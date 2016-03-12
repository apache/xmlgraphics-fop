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

import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.fop.fo.FONode.FONodeIterator;
import org.apache.fop.fo.flow.table.Table;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.PositionIterator;

public class TableContentLayoutManagerTestCase {

    @Test
    public void testAddAreas() {
        LayoutContext lc = LayoutContext.newInstance();
        // mock
        ColumnSetup cs = mock(ColumnSetup.class);
        when(cs.getColumnCount()).thenReturn(3);
        // mock
        FONodeIterator foni = mock(FONodeIterator.class);
        when(foni.hasNext()).thenReturn(false);
        // mock
        Table t = mock(Table.class);
        when(t.getChildNodes()).thenReturn(foni);
        when(t.getMarkers()).thenReturn(null);
        // mock
        TableLayoutManager tlm = mock(TableLayoutManager.class);
        when(tlm.getTable()).thenReturn(t);
        when(tlm.getColumns()).thenReturn(cs);
        // mock
        PositionIterator pi = mock(PositionIterator.class);
        when(pi.hasNext()).thenReturn(false);
        // real TCLM, not a mock
        TableContentLayoutManager tclm = new TableContentLayoutManager(tlm);
        // check that addAreas() calls the clearTableFragments() on the table and the
        // repeatAddAreasForSavedTableHeaderTableCellLayoutManagers on the TLM
        tclm.addAreas(pi, lc);
        verify(tlm).clearTableFragmentMarkers();
        verify(tlm).repeatAddAreasForSavedTableHeaderTableCellLayoutManagers();
    }

}
