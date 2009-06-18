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

package org.apache.fop.traits;

import java.awt.Color;

import junit.framework.TestCase;

import org.apache.fop.util.ColorUtil;

/**
 * Tests the Trait.Color class.
 * TODO: This actually tests the ColorUtil class now.
 */
public class TraitColorTestCase extends TestCase {

    /**
     * Test serialization to String.
     * @throws Exception if an error occurs
     */
    public void testSerialization() throws Exception {
        Color col = new Color(1.0f, 1.0f, 0.5f, 1.0f);
        String s = ColorUtil.colorTOsRGBString(col);
        
        //This is what the old color spit out. Now it is 80 due to rounding 
        //assertEquals("#ffff7f", s);
        assertEquals("#ffff80", s);
        
        col = new Color(1.0f, 0.0f, 0.0f, 0.8f);
        s = ColorUtil.colorTOsRGBString(col);
        assertEquals("#ff0000cc", s);
    }
    
    /**
     * Test deserialization from String.
     * @throws Exception if an error occurs
     */
    public void testDeserialization() throws Exception {
        Color col = ColorUtil.parseColorString("#ffff7f");
        assertEquals(255, col.getRed());
        assertEquals(255, col.getGreen());
        assertEquals(127, col.getBlue());
        assertEquals(255, col.getAlpha());

        col = ColorUtil.parseColorString("#ff0000cc");
        assertEquals(255, col.getRed());
        assertEquals(0, col.getGreen());
        assertEquals(0, col.getBlue());
        assertEquals(204, col.getAlpha());
    }
    
    /**
     * Test equals().
     * @throws Exception if an error occurs
     */
    public void testEquals() throws Exception {
        Color col1 = ColorUtil.parseColorString("#ff0000cc");
        Color col2 = ColorUtil.parseColorString("#ff0000cc");
        assertTrue(col1 != col2);
        assertEquals(col1, col2);
    }
    
}
