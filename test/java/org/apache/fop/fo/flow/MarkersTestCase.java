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

package org.apache.fop.fo.flow;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.fop.fo.Constants;
import org.apache.fop.fo.flow.Marker;
import org.apache.fop.fo.flow.Markers;
import org.apache.fop.fo.flow.RetrieveMarker;
import org.apache.fop.fo.flow.RetrieveTableMarker;

public class MarkersTestCase {

    @Test
    public void testRegisterAndResolve() {
        // consider 3 regions, and a boundary; the first region starts before the boundary and ends inside
        // the boundary, the second region is fully inside the boundary, and the third region starts inside
        // the boundary and ends after the boundary. in every region there are 2 markers, A and B.
        // ======== region 1
        Map<String, Marker> markers_region_1 = new HashMap<String, Marker>();
        Marker marker_1A = mock(Marker.class);
        Marker marker_1B = mock(Marker.class);
        markers_region_1.put("A", marker_1A);
        markers_region_1.put("B", marker_1B);
        // ======== region 2
        Map<String, Marker> markers_region_2 = new HashMap<String, Marker>();
        Marker marker_2A = mock(Marker.class);
        Marker marker_2B = mock(Marker.class);
        markers_region_2.put("A", marker_2A);
        markers_region_2.put("B", marker_2B);
        // ======== region 3
        Map<String, Marker> markers_region_3 = new HashMap<String, Marker>();
        Marker marker_3A = mock(Marker.class);
        Marker marker_3B = mock(Marker.class);
        markers_region_3.put("A", marker_3A);
        markers_region_3.put("B", marker_3B);
        // instantiate markers for the boundary
        Markers markers = new Markers();
        // register the markers for the different regions
        // region 1
        markers.register(markers_region_1, true, false, true);
        markers.register(markers_region_1, false, false, true);
        // region 2
        markers.register(markers_region_2, true, true, true);
        markers.register(markers_region_2, false, true, true);
        // region 3
        markers.register(markers_region_3, true, true, false);
        markers.register(markers_region_3, false, true, false);
        // now prepare a RetrieveMarker
        RetrieveMarker rm = mock(RetrieveMarker.class);
        when(rm.getRetrieveClassName()).thenReturn("A");
        when(rm.getLocalName()).thenReturn("retrieve-marker");
        when(rm.getPositionLabel()).thenReturn("position-label"); // not relevant for the test
        // and resolve the marker for different positions
        // EN_FSWP
        when(rm.getPosition()).thenReturn(Constants.EN_FSWP);
        // expect marker_2A
        assertEquals(marker_2A, markers.resolve(rm));
        // EN_LSWP
        when(rm.getPosition()).thenReturn(Constants.EN_LSWP);
        // expect marker_3A
        assertEquals(marker_3A, markers.resolve(rm));
        // EN_LEWP
        when(rm.getPosition()).thenReturn(Constants.EN_LEWP);
        // expect marker_2A
        assertEquals(marker_2A, markers.resolve(rm));
        // EN_FIC
        when(rm.getPosition()).thenReturn(Constants.EN_FIC);
        // expect marker_1A
        assertEquals(marker_1A, markers.resolve(rm));
        // now prepare a RetrieveTableMarker
        RetrieveTableMarker rtm = mock(RetrieveTableMarker.class);
        when(rtm.getRetrieveClassName()).thenReturn("B");
        when(rtm.getLocalName()).thenReturn("retrieve-table-marker");
        when(rtm.getPositionLabel()).thenReturn("position-label"); // not relevant for the test
        // and resolve the marker for different positions
        // EN_FIRST_STARTING
        when(rtm.getPosition()).thenReturn(Constants.EN_FIRST_STARTING);
        // expect marker_2B
        assertEquals(marker_2B, markers.resolve(rtm));
        // EN_LAST_STARTING
        when(rtm.getPosition()).thenReturn(Constants.EN_LAST_STARTING);
        // expect marker_3B
        assertEquals(marker_3B, markers.resolve(rtm));
        // EN_LAST_ENDING
        when(rtm.getPosition()).thenReturn(Constants.EN_LAST_ENDING);
        // expect marker_2B
        assertEquals(marker_2B, markers.resolve(rtm));
        // EN_FIRST_INCLUDING_CARRYOVER
        when(rtm.getPosition()).thenReturn(Constants.EN_FIRST_INCLUDING_CARRYOVER);
        // expect marker_1B
        assertEquals(marker_1B, markers.resolve(rtm));
        // test also an invalid position
        when(rm.getPosition()).thenReturn(Constants.EN_ABSOLUTE);
        try {
            Marker m = markers.resolve(rm);
            fail("Expected an exception... instead got:" + m.toString());
        } catch (RuntimeException re) {
            // do nothing
        }
    }
}
