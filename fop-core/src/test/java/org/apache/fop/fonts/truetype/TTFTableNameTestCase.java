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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * This class tests the enum org.apache.fop.fonts.truetype.TTFTableName
 *
 */
public class TTFTableNameTestCase {
    /**
     * Test getName() - tests that the getName() method returns the expected String as expected in
     * the Directory Table.
     * @exception IllegalAccessException error
     */
    @Test
    public void testGetName() throws IllegalAccessException {
        assertEquals("tableDirectory", OFTableName.TABLE_DIRECTORY.getName());
        assertEquals("EBDT", OFTableName.EBDT.getName());
        assertEquals("EBLC", OFTableName.EBLC.getName());
        assertEquals("EBSC", OFTableName.EBSC.getName());
        assertEquals("FFTM", OFTableName.FFTM.getName());
        assertEquals("GDEF", OFTableName.GDEF.getName());
        assertEquals("GPOS", OFTableName.GPOS.getName());
        assertEquals("GSUB", OFTableName.GSUB.getName());
        assertEquals("LTSH", OFTableName.LTSH.getName());
        assertEquals("OS/2", OFTableName.OS2.getName());
        assertEquals("PCLT", OFTableName.PCLT.getName());
        assertEquals("VDMX", OFTableName.VDMX.getName());
        assertEquals("cmap", OFTableName.CMAP.getName());
        assertEquals("cvt ", OFTableName.CVT.getName());
        assertEquals("fpgm", OFTableName.FPGM.getName());
        assertEquals("gasp", OFTableName.GASP.getName());
        assertEquals("glyf", OFTableName.GLYF.getName());
        assertEquals("hdmx", OFTableName.HDMX.getName());
        assertEquals("head", OFTableName.HEAD.getName());
        assertEquals("hhea", OFTableName.HHEA.getName());
        assertEquals("hmtx", OFTableName.HMTX.getName());
        assertEquals("kern", OFTableName.KERN.getName());
        assertEquals("loca", OFTableName.LOCA.getName());
        assertEquals("maxp", OFTableName.MAXP.getName());
        assertEquals("name", OFTableName.NAME.getName());
        assertEquals("post", OFTableName.POST.getName());
        assertEquals("prep", OFTableName.PREP.getName());
        assertEquals("vhea", OFTableName.VHEA.getName());
        assertEquals("vmtx", OFTableName.VMTX.getName());
        // make sure it works with other table names
        OFTableName test = OFTableName.getValue("test");
        assertEquals("test", test.getName());
    }

    /**
     * Test getValue(String) - tests that the getValue(String) method returns the expected
     * TTFTableNames value when it is given a String (name of a table).
     * @exception IllegalAccessException error
     */
    @Test
    public void testGetValue() throws IllegalAccessException {
        assertEquals(OFTableName.EBDT, OFTableName.getValue("EBDT"));
        assertEquals(OFTableName.EBLC, OFTableName.getValue("EBLC"));
        assertEquals(OFTableName.EBSC, OFTableName.getValue("EBSC"));
        assertEquals(OFTableName.FFTM, OFTableName.getValue("FFTM"));
        assertEquals(OFTableName.LTSH, OFTableName.getValue("LTSH"));
        assertEquals(OFTableName.OS2, OFTableName.getValue("OS/2"));
        assertEquals(OFTableName.PCLT, OFTableName.getValue("PCLT"));
        assertEquals(OFTableName.VDMX, OFTableName.getValue("VDMX"));
        assertEquals(OFTableName.CMAP, OFTableName.getValue("cmap"));
        assertEquals(OFTableName.CVT, OFTableName.getValue("cvt "));
        assertEquals(OFTableName.FPGM, OFTableName.getValue("fpgm"));
        assertEquals(OFTableName.GASP, OFTableName.getValue("gasp"));
        assertEquals(OFTableName.GLYF, OFTableName.getValue("glyf"));
        assertEquals(OFTableName.HDMX, OFTableName.getValue("hdmx"));
        assertEquals(OFTableName.HEAD, OFTableName.getValue("head"));
        assertEquals(OFTableName.HHEA, OFTableName.getValue("hhea"));
        assertEquals(OFTableName.HMTX, OFTableName.getValue("hmtx"));
        assertEquals(OFTableName.KERN, OFTableName.getValue("kern"));
        assertEquals(OFTableName.LOCA, OFTableName.getValue("loca"));
        assertEquals(OFTableName.MAXP, OFTableName.getValue("maxp"));
        assertEquals(OFTableName.NAME, OFTableName.getValue("name"));
        assertEquals(OFTableName.POST, OFTableName.getValue("post"));
        assertEquals(OFTableName.PREP, OFTableName.getValue("prep"));
        assertEquals(OFTableName.VHEA, OFTableName.getValue("vhea"));
        assertEquals(OFTableName.VMTX, OFTableName.getValue("vmtx"));
        // Test that we can store a random table name and it will not fail or throw an error.
        OFTableName test = OFTableName.getValue("random");
        assertTrue(test instanceof OFTableName);
    }

    /**
     * This class overrides hashCode() - we need to ensure it works properly by instantiating two
     * objects and comparing their hash-codes.
     * @exception IllegalAccessException error
     */
    @Test
    public void testHashCode() throws IllegalAccessException {
        OFTableName a = OFTableName.getValue("testObject");
        OFTableName b = OFTableName.getValue("testObject");
        assertTrue(a.hashCode() == b.hashCode());
        OFTableName c = OFTableName.getValue("fail");
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
    @Test
    public void testEquals() throws IllegalAccessException {
        // Reflexivity
        OFTableName a = OFTableName.getValue("test");
        assertTrue(a.equals(a));
        // Symmetry
        OFTableName b = OFTableName.getValue("test");
        assertTrue(a.equals(b));
        assertTrue(b.equals(a));
        // Transitivity (tested with symmetry)
        // Consistency (test that a == b is true and that a == c fails)
        OFTableName c = OFTableName.getValue("fail");
        for (int i = 0; i < 100; i++) {
            assertTrue(a.equals(b));
            assertFalse(a.equals(c));
        }
        // check with null value
        assertFalse(a.equals(null));
    }
}
