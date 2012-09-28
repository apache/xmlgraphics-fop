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

import java.awt.Color;

import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.fop.fo.flow.table.PrimaryGridUnit;
import org.apache.fop.fo.flow.table.Table;
import org.apache.fop.fo.flow.table.TableCell;
import org.apache.fop.fo.flow.table.TableColumn;
import org.apache.fop.fo.flow.table.TableHeader;
import org.apache.fop.fo.flow.table.TableRow;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground.BorderInfo;
import org.apache.fop.fo.properties.CondLengthProperty;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.PageSequenceLayoutManager;
import org.apache.fop.layoutmgr.PositionIterator;
import org.apache.fop.layoutmgr.RetrieveTableMarkerLayoutManager;

public class TableCellLayoutManagerTestCase {

    // this test aims to check that the first call to addAreas() calls
    // TLM.saveTableHeaderTableCellLayoutManagers() but the second call, through repeatAddAreas()
    // does not call it again; there are a lot of mocks here, but just the necessary so that the
    // addAreas() call can run to completion without NPE; also, the mocking needs to be done so
    // the methods isDecendantOfTableHeaderOrFooter() and hasRetrieveTableMarker() return true.
    @Test
    public void testRepeatAddAreas() {
        LayoutContext lc = LayoutContext.newInstance();
        // mock background
        CommonBorderPaddingBackground cbpb = mock(CommonBorderPaddingBackground.class);
        // mock conditional length property
        CondLengthProperty clp = mock(CondLengthProperty.class);
        when(clp.getLengthValue()).thenReturn(0);
        // real border info
        BorderInfo bi = BorderInfo.getInstance(0, clp, Color.BLACK);
        // mock column
        TableColumn tcol = mock(TableColumn.class);
        when(tcol.getCommonBorderPaddingBackground()).thenReturn(cbpb);
        // mock table
        Table t = mock(Table.class);
        when(t.getColumn(0)).thenReturn(tcol);
        // mock header
        TableHeader th = mock(TableHeader.class);
        when(th.getCommonBorderPaddingBackground()).thenReturn(cbpb);
        // mock row
        TableRow tr = mock(TableRow.class);
        when(tr.getParent()).thenReturn(th);
        // mock cell
        TableCell tc = mock(TableCell.class);
        when(tc.hasRetrieveTableMarker()).thenReturn(true);
        when(tc.getTable()).thenReturn(t);
        when(tc.getId()).thenReturn("cellId");
        when(tc.getCommonBorderPaddingBackground()).thenReturn(cbpb);
        when(tc.getParent()).thenReturn(tr);
        // mock PGU
        PrimaryGridUnit pgu = mock(PrimaryGridUnit.class);
        when(pgu.getCell()).thenReturn(tc);
        when(pgu.getColIndex()).thenReturn(0);
        when(pgu.getBorderBefore(0)).thenReturn(bi);
        when(pgu.getBorderAfter(0)).thenReturn(bi);
        when(pgu.getBorderEnd()).thenReturn(bi);
        when(pgu.getBorderStart()).thenReturn(bi);
        when(pgu.getTablePart()).thenReturn(th);
        // mock RTMLM
        RetrieveTableMarkerLayoutManager rtmlm = mock(RetrieveTableMarkerLayoutManager.class);
        when(rtmlm.isFinished()).thenReturn(true); // avoids infinite loop
        // mock PSLM
        PageSequenceLayoutManager pslm = mock(PageSequenceLayoutManager.class);
        // mock TLM
        TableLayoutManager tlm = mock(TableLayoutManager.class);
        when(tlm.getPSLM()).thenReturn(pslm);
        // mock PI
        PositionIterator pi = mock(PositionIterator.class);
        // mock RP
        RowPainter rp = mock(RowPainter.class);

        // real TCLM, not a mock!
        TableCellLayoutManager tclm = new TableCellLayoutManager(tc, pgu);
        tclm.addChildLM(rtmlm);
        tclm.setParent(tlm);
        // lets call addAreas
        int[] n = {};
        tclm.addAreas(pi, lc, n, 0, 0, 0, 0, true, true, rp, 0);
        // check the TCLM is added to the TLM
        verify(tlm).saveTableHeaderTableCellLayoutManagers(tclm);
        // call the repeat
        tclm.repeatAddAreas();
        // check the TCLM was not added again
        verify(tlm).saveTableHeaderTableCellLayoutManagers(tclm);
    }
}
