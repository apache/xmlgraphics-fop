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

package org.apache.fop.area.inline;

import org.apache.fop.area.ViewportTestCase;

/**
 * Tests the {@linkplain Viewport} class.
 */
public class InlineViewportTestCase extends ViewportTestCase {

    public void testNonClip() throws Exception {
        Viewport v = new Viewport(null);
        v.setIPD(50);
        v.setBPD(25);
        checkNonClip(v);
    }

    public void testClip() throws Exception {
        Viewport v = new Viewport(null);
        int ipd = 50;
        int bpd = 25;
        v.setIPD(ipd);
        v.setBPD(bpd);
        v.setClip(true);
        checkClip(v, ipd, bpd);
    }

}
