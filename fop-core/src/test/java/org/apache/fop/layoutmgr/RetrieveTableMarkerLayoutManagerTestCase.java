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

package org.apache.fop.layoutmgr;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.fop.fo.Constants;
import org.apache.fop.fo.flow.RetrieveTableMarker;
import org.apache.fop.fo.flow.table.Table;
import org.apache.fop.layoutmgr.inline.TextLayoutManager;
import org.apache.fop.layoutmgr.table.TableLayoutManager;

public class RetrieveTableMarkerLayoutManagerTestCase {

    @Test
    public void testGetNextKnuthElementsLayoutContextInt() {
        LayoutContext lc = LayoutContext.newInstance();
        // mock
        Table t = mock(Table.class);
        // mock
        RetrieveTableMarker rtm = mock(RetrieveTableMarker.class);
        when(rtm.getRetrieveClassName()).thenReturn("A");
        when(rtm.getPosition()).thenReturn(Constants.EN_FIRST_STARTING);
        when(rtm.getBoundary()).thenReturn(Constants.EN_TABLE_FRAGMENT);
        // mock
        TextLayoutManager tlm = mock(TextLayoutManager.class);
        // mock
        LayoutManagerMapping lmm = mock(LayoutManagerMapping.class);
        when(lmm.makeLayoutManager(rtm)).thenReturn(tlm);
        // mock
        PageSequenceLayoutManager pslm = mock(PageSequenceLayoutManager.class);
        when(pslm.getPSLM()).thenReturn(pslm);
        when(pslm.getLayoutManagerMaker()).thenReturn(lmm);
        // mock
        TableLayoutManager tablelm = mock(TableLayoutManager.class);
        when(tablelm.getTable()).thenReturn(t);
        // mock
        BlockLayoutManager blm = mock(BlockLayoutManager.class);
        when(blm.getPSLM()).thenReturn(pslm);
        when(blm.getParent()).thenReturn(tablelm);
        // real RTMLM, not mock
        RetrieveTableMarkerLayoutManager rtmlm = new RetrieveTableMarkerLayoutManager(rtm);
        rtmlm.setParent(blm);
        // test the case where resolution returns null
        when(tablelm.resolveRetrieveTableMarker(rtm)).thenReturn(null);
        assertNull(rtmlm.getNextKnuthElements(lc, 0));
        // test the case where resolution returns non null
        List l = new ArrayList();
        when(tablelm.resolveRetrieveTableMarker(rtm)).thenReturn(rtm);
        when(tlm.getNextKnuthElements(lc, 0)).thenReturn(l);
        assertEquals(l, rtmlm.getNextKnuthElements(lc, 0));
        verify(tlm).setParent(blm);
        verify(tlm).initialize();
    }

}
