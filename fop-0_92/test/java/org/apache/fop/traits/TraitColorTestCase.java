/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import org.apache.fop.area.Trait;

import junit.framework.TestCase;

/**
 * Tests the Trait.Color class.
 */
public class TraitColorTestCase extends TestCase {

    /**
     * Test serialization to String.
     * @throws Exception if an error occurs
     */
    public void testSerialization() throws Exception {
        Trait.Color col = new Trait.Color(1.0f, 1.0f, 0.5f, 1.0f);
        String s = col.toString();
        assertEquals("#ffff7f", s);
        
        col = new Trait.Color(1.0f, 0.0f, 0.0f, 0.8f);
        s = col.toString();
        assertEquals("#ff0000cc", s);
    }
    
    /**
     * Test deserialization from String.
     * @throws Exception if an error occurs
     */
    public void testDeserialization() throws Exception {
        float tolerance = 0.5f / 255; //due to color value conversion

        Trait.Color col = Trait.Color.valueOf("#ffff7f");
        assertEquals(1.0f, col.getRed(), 0.0f);
        assertEquals(1.0f, col.getGreen(), 0.0f);
        assertEquals(0.5f, col.getBlue(), tolerance);
        assertEquals(1.0f, col.getAlpha(), 0.0f);

        col = Trait.Color.valueOf("#ff0000cc");
        assertEquals(1.0f, col.getRed(), 0.0f);
        assertEquals(0.0f, col.getGreen(), 0.0f);
        assertEquals(0.0f, col.getBlue(), 0.0f);
        assertEquals(0.8f, col.getAlpha(), tolerance);
    }
    
    /**
     * Test equals().
     * @throws Exception if an error occurs
     */
    public void testEquals() throws Exception {
        Trait.Color col1 = Trait.Color.valueOf("#ff0000cc");
        Trait.Color col2 = Trait.Color.valueOf("#ff0000cc");
        assertTrue(col1 != col2);
        assertEquals(col1, col2);
    }
    
}
