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

package org.apache.fop.complexscripts.scripts;

import org.junit.Test;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertSame;

/**
 * Kerning disabled removes the kern feature from a processor's positioning features and
 * nothing else: the marks are still positioned (FOP-3343).
 */
public class PositioningWithoutKerningTestCase {

    @Test
    public void testKernIsRemovedAndMarksStay() {
        assertArrayEquals(new String[] {"mark", "mkmk"},
                ScriptProcessor.withoutKerning(new String[] {"kern", "mark", "mkmk"}));
    }

    @Test
    public void testListWithoutKernIsReturnedAsIs() {
        String[] features = {"abvm", "blwm", "dist"};
        assertSame(features, ScriptProcessor.withoutKerning(features));
    }
}
