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

package org.apache.fop.fonts.truetype;

import junit.framework.TestCase;

/**
 * This class tests the enum org.apache.fop.fonts.truetype.TTFTableName
 *
 */
public class TTFTableNameTest extends TestCase {
    /**
     * Test getName() - tests that the getName() method returns the expected String as expected in
     * the Directory Table.
     * @exception IllegalAccessException error
     */
    public void testGetName() throws IllegalAccessException {
        assertEquals("dirTable", TTFTableName.DIRECTORY_TABLE.getName());
        assertEquals("EBDT", TTFTableName.EBDT.getName());
        assertEquals("EBLC", TTFTableName.EBLC.getName());
        assertEquals("EBSC", TTFTableName.EBSC.getName());
        assertEquals("FFTM", TTFTableName.FFTM.getName());
        assertEquals("GDEF", TTFTableName.GDEF.getName());
        assertEquals("GPOS", TTFTableName.GPOS.getName());
        assertEquals("GSUB", TTFTableName.GSUB.getName());
        assertEquals("LTSH", TTFTableName.LTSH.getName());
        assertEquals("OS/2", TTFTableName.OS2.getName());
        assertEquals("PCLT", TTFTableName.PCLT.getName());
        assertEquals("VDMX", TTFTableName.VDMX.getName());
        assertEquals("cmap", TTFTableName.CMAP.getName());
        assertEquals("cvt ", TTFTableName.CVT.getName());
        assertEquals("fpgm", TTFTableName.FPGM.getName());
        assertEquals("gasp", TTFTableName.GASP.getName());
        assertEquals("glyf", TTFTableName.GLYF.getName());
        assertEquals("hdmx", TTFTableName.HDMX.getName());
        assertEquals("head", TTFTableName.HEAD.getName());
        assertEquals("hhea", TTFTableName.HHEA.getName());
        assertEquals("hmtx", TTFTableName.HMTX.getName());
        assertEquals("kern", TTFTableName.KERN.getName());
        assertEquals("loca", TTFTableName.LOCA.getName());
        assertEquals("maxp", TTFTableName.MAXP.getName());
        assertEquals("name", TTFTableName.NAME.getName());
        assertEquals("post", TTFTableName.POST.getName());
        assertEquals("prep", TTFTableName.PREP.getName());
        assertEquals("vhea", TTFTableName.VHEA.getName());
        assertEquals("vmtx", TTFTableName.VMTX.getName());
        // make sure it works with other table names
        TTFTableName test = TTFTableName.getValue("test");
        assertEquals("test", test.getName());
    }

    /**
     * Test getValue(String) - tests that the getValue(String) method returns the expected
     * TTFTableNames value when it is given a String (name of a table).
     * @exception IllegalAccessException error
     */
    public void testGetValue() throws IllegalAccessException {
        assertEquals(TTFTableName.EBDT, TTFTableName.getValue("EBDT"));
        assertEquals(TTFTableName.EBLC, TTFTableName.getValue("EBLC"));
        assertEquals(TTFTableName.EBSC, TTFTableName.getValue("EBSC"));
        assertEquals(TTFTableName.FFTM, TTFTableName.getValue("FFTM"));
        assertEquals(TTFTableName.LTSH, TTFTableName.getValue("LTSH"));
        assertEquals(TTFTableName.OS2, TTFTableName.getValue("OS/2"));
        assertEquals(TTFTableName.PCLT, TTFTableName.getValue("PCLT"));
        assertEquals(TTFTableName.VDMX, TTFTableName.getValue("VDMX"));
        assertEquals(TTFTableName.CMAP, TTFTableName.getValue("cmap"));
        assertEquals(TTFTableName.CVT, TTFTableName.getValue("cvt "));
        assertEquals(TTFTableName.FPGM, TTFTableName.getValue("fpgm"));
        assertEquals(TTFTableName.GASP, TTFTableName.getValue("gasp"));
        assertEquals(TTFTableName.GLYF, TTFTableName.getValue("glyf"));
        assertEquals(TTFTableName.HDMX, TTFTableName.getValue("hdmx"));
        assertEquals(TTFTableName.HEAD, TTFTableName.getValue("head"));
        assertEquals(TTFTableName.HHEA, TTFTableName.getValue("hhea"));
        assertEquals(TTFTableName.HMTX, TTFTableName.getValue("hmtx"));
        assertEquals(TTFTableName.KERN, TTFTableName.getValue("kern"));
        assertEquals(TTFTableName.LOCA, TTFTableName.getValue("loca"));
        assertEquals(TTFTableName.MAXP, TTFTableName.getValue("maxp"));
        assertEquals(TTFTableName.NAME, TTFTableName.getValue("name"));
        assertEquals(TTFTableName.POST, TTFTableName.getValue("post"));
        assertEquals(TTFTableName.PREP, TTFTableName.getValue("prep"));
        assertEquals(TTFTableName.VHEA, TTFTableName.getValue("vhea"));
        assertEquals(TTFTableName.VMTX, TTFTableName.getValue("vmtx"));
        // Test that we can store a random table name and it will not fail or throw an error.
        TTFTableName test = TTFTableName.getValue("random");
        assertTrue(test instanceof TTFTableName);
    }

    /**
     * This class overrides hashCode() - we need to ensure it works properly by instantiating two
     * objects and comparing their hash-codes.
     * @exception IllegalAccessException error
     */
    public void testHashCode() throws IllegalAccessException {
        TTFTableName a = TTFTableName.getValue("testObject");
        TTFTableName b = TTFTableName.getValue("testObject");
        assertTrue(a.hashCode() == b.hashCode());
        TTFTableName c = TTFTableName.getValue("fail");
        assertFalse(a.hashCode() == c.hashCode());
    }

    /**
     * This class overrides equals(object) - we need to test:
     * 1) Reflexivity
     * 2) Symmetry
     * 3) Transitivity
     * 4) Consistency
     * 5) check it fails if you put in a null value
     * @throws IllegalAccessException error
     */
    public void testEquals() throws IllegalAccessException {
        // Reflexivity
        TTFTableName a = TTFTableName.getValue("test");
        assertTrue(a.equals(a));
        // Symmetry
        TTFTableName b = TTFTableName.getValue("test");
        assertTrue(a.equals(b));
        assertTrue(b.equals(a));
        // Transitivity (tested with symmetry)
        // Consistency (test that a == b is true and that a == c fails)
        TTFTableName c = TTFTableName.getValue("fail");
        for (int i = 0; i < 100; i++) {
            assertTrue(a.equals(b));
            assertFalse(a.equals(c));
        }
        // check with null value
        assertFalse(a.equals(null));
    }
}
