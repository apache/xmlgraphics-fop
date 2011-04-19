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


/**
 * Tests the {@linkplain BlockViewport} class.
 */
public class BlockViewportTestCase extends ViewportTestCase {

    public void testNonClip() throws Exception {
        BlockViewport bv = new BlockViewport();
        bv.setIPD(100);
        bv.setBPD(50);
        checkNonClip(bv);
    }

    public void testClip() throws Exception {
        BlockViewport bv = new BlockViewport();
        int ipd = 100;
        int bpd = 50;
        bv.setIPD(ipd);
        bv.setBPD(bpd);
        bv.setClip(true);
        checkClip(bv, ipd, bpd);
    }
}
