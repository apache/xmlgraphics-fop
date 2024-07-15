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

package org.apache.fop.svg;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SimpleSVGUserAgentTestCase {

    @Test
    public void testMediumFontResolution72() {
        checkGetMediumFontSize(72f, 12f);
    }

    @Test
    public void testMediumFontResolution96() {
        checkGetMediumFontSize(96f, 16f);
    }

    private void checkGetMediumFontSize(float sourceRes, float expectedSize) {
        SimpleSVGUserAgent adapter = new SimpleSVGUserAgent(null, null, sourceRes);

        // Size must be calculated based on the dpi settings
        assertEquals(expectedSize, adapter.getMediumFontSize(), 0.01);
    }
}
