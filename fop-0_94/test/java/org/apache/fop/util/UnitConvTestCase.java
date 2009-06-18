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

package org.apache.fop.util;

import junit.framework.TestCase;

/**
 * Test class for UnitConv.
 */
public class UnitConvTestCase extends TestCase {

    /**
     * Test all kinds of unit conversions.
     * @throws Exception if the test fails
     */
    public void testConversions() throws Exception {
        assertEquals("in2mm", 25.4, UnitConv.in2mm(1), 0.00001);
        assertEquals("mm2in", 1.0, UnitConv.mm2in(25.4), 0.00001);
        assertEquals("mm2pt", 841.890, UnitConv.mm2pt(297), 0.001 / 2); //height of an A4 page
        assertEquals("mm2mpt", 841890, UnitConv.mm2mpt(297), 1.0 / 2);
        assertEquals("pt2mm", 297, UnitConv.pt2mm(841.890), 0.0001);
        assertEquals("in2mpt", 792000, UnitConv.in2mpt(11.0), 1.0 / 2); //height of a letter page
        assertEquals("mpt2in", 11.0, UnitConv.mpt2in(792000), 0.01 / 2); //height of a letter page
        
        assertEquals("mm2px/72dpi", 841.8897764234434, UnitConv.mm2px(297.0, 72), 0);
        assertEquals("mm2px/300dpi", 3507.8740684310146, UnitConv.mm2px(297.0, 300), 0);
    }
    
}