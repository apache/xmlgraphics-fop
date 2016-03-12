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

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FObj.FObjIterator;
import org.apache.fop.fo.flow.RetrieveTableMarker;
import org.apache.fop.layoutmgr.LayoutManagerMapping.RetrieveTableMarkerLayoutManagerMaker;

public class RetrieveTableMarkerLayoutManagerMakerTestCase {

    @Test
    public void testMake() throws FOPException {
        // mock
        FObjIterator foi = mock(FObjIterator.class);
        when(foi.hasNext()).thenReturn(true).thenReturn(false);
        // mock
        RetrieveTableMarker rtm = mock(RetrieveTableMarker.class);
        // real RTMLMM, not mock
        List l = new ArrayList();
        LayoutManagerMapping lmm = new LayoutManagerMapping();
        RetrieveTableMarkerLayoutManagerMaker rtmlmm = lmm.new RetrieveTableMarkerLayoutManagerMaker();
        // test the case rtm has no child nodes
        when(rtm.getChildNodes()).thenReturn(null);
        rtmlmm.make(rtm, l);
        assertTrue(l.size() == 1);
        assertTrue(l.get(0) instanceof RetrieveTableMarkerLayoutManager);
    }

}
