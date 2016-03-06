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

/* $Id:$ */

package org.apache.fop.layoutmgr.table;

import java.util.HashMap;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.fop.area.PageViewport;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.flow.Marker;
import org.apache.fop.fo.flow.RetrieveTableMarker;
import org.apache.fop.fo.flow.table.Table;
import org.apache.fop.layoutmgr.BlockLayoutManager;
import org.apache.fop.layoutmgr.Page;
import org.apache.fop.layoutmgr.PageSequenceLayoutManager;

public class TableLayoutManagerTestCase {

    @Test
    public void testSavedTableCellLayoutManagersFunctionality() {
        Table t = mock(Table.class);
        TableCellLayoutManager tclm1 = mock(TableCellLayoutManager.class);
        TableLayoutManager tlm = new TableLayoutManager(t);
        tlm.saveTableHeaderTableCellLayoutManagers(tclm1);
        tlm.repeatAddAreasForSavedTableHeaderTableCellLayoutManagers();
        verify(tclm1).repeatAddAreas(); // called once
        // test that after the first repeatAddAreas() call the list closes to new additions
        TableCellLayoutManager tclm2 = mock(TableCellLayoutManager.class);
        tlm.saveTableHeaderTableCellLayoutManagers(tclm2);
        tlm.repeatAddAreasForSavedTableHeaderTableCellLayoutManagers();
        verify(tclm1, times(2)).repeatAddAreas(); // called twice
        verify(tclm2, never()).repeatAddAreas(); // never called
    }

    @Test
    public void testResolveRetrieveTableMarker() {
        // mock
        Table t = mock(Table.class);
        // mock
        Marker m = mock(Marker.class);
        // mock
        RetrieveTableMarker rtm = mock(RetrieveTableMarker.class);
        when(rtm.getRetrieveClassName()).thenReturn("A");
        when(rtm.getPosition()).thenReturn(Constants.EN_FIRST_STARTING);
        // mock
        PageViewport pv = mock(PageViewport.class);
        when(pv.resolveMarker(rtm)).thenReturn(m);
        // mock
        Page p = mock(Page.class);
        when(p.getPageViewport()).thenReturn(pv);
        // mock
        PageSequenceLayoutManager pslm = mock(PageSequenceLayoutManager.class);
        when(pslm.getPSLM()).thenReturn(pslm);
        when(pslm.getCurrentPage()).thenReturn(p);
        // mock
        BlockLayoutManager blm = mock(BlockLayoutManager.class);
        blm.setParent(pslm);
        when(blm.getPSLM()).thenReturn(pslm);
        // real TLM, not mock
        TableLayoutManager tlm = new TableLayoutManager(t);
        tlm.setParent(blm);
        // register a marker
        HashMap<String, Marker> markers1 = new HashMap<String, Marker>();
        Marker m1 = mock(Marker.class);
        markers1.put("A", m1);
        tlm.registerMarkers(markers1, true, true, true);
        tlm.registerMarkers(markers1, false, true, true);
        // check that if there is a marker at table fragment level the RTM is returned
        assertEquals(rtm, tlm.resolveRetrieveTableMarker(rtm));
        verify(rtm, never()).getBoundary();
        // check that if there is no marker at table fragment level and that is the boundary
        // we get a null return value
        when(rtm.getBoundary()).thenReturn(Constants.EN_TABLE_FRAGMENT);
        when(rtm.getRetrieveClassName()).thenReturn("B");
        assertNull(tlm.resolveRetrieveTableMarker(rtm));
        verify(rtm).getBoundary();
        verify(rtm, never()).changePositionTo(Constants.EN_LAST_ENDING);
        // check that if there is no marker at table fragment level and the boundary is page
        // then we try to do the resolution at page level; test the case a marker is found
        when(rtm.getBoundary()).thenReturn(Constants.EN_PAGE);
        assertEquals(rtm, tlm.resolveRetrieveTableMarker(rtm));
        verify(rtm).changePositionTo(Constants.EN_LAST_ENDING);
        verify(rtm).changePositionTo(Constants.EN_FIRST_STARTING);
        verify(pv).resolveMarker(rtm);
        // test the same situation but in this case the marker is not found
        when(pv.resolveMarker(rtm)).thenReturn(null);
        assertNull(tlm.resolveRetrieveTableMarker(rtm));
        // test the situation where the marker is not found at page level but the boundary is table
        when(rtm.getBoundary()).thenReturn(Constants.EN_TABLE);
        assertNull(tlm.resolveRetrieveTableMarker(rtm));
    }

}
