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

package org.apache.fop.area;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

import org.junit.Test;

/**
 * Tests the {@linkplain RegionViewport} class.
 */
public class RegionViewportTestCase extends ViewportTest {

    private RegionViewport createRegionViewport(int x, int y, int ipd, int bpd) {
        Rectangle2D v = new Rectangle(x, y, ipd, bpd);
        RegionViewport viewport = new RegionViewport(v);
        viewport.setIPD(ipd);
        viewport.setBPD(bpd);
        return viewport;
    }

    @Test
    public void testNonClip() throws Exception {
        RegionViewport viewport = createRegionViewport(10, 10, 100, 20);
        checkNonClip(viewport);
    }

    @Test
    public void testClip() throws Exception {
        int ipd = 150;
        int bpd = 20;
        RegionViewport viewport = createRegionViewport(10, 10, ipd, bpd);
        viewport.setClip(true);
        checkClip(viewport, ipd, bpd);
    }
}
