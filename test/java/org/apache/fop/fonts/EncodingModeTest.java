package org.apache.fop.fonts;

import junit.framework.TestCase;

public class EncodingModeTest extends TestCase {
    public void testGetName() {
        assertEquals("auto", EncodingMode.AUTO.getName());
        assertEquals("single-byte", EncodingMode.SINGLE_BYTE.getName());
        assertEquals("cid", EncodingMode.CID.getName());
    }

    public void testGetValue() {
        assertEquals(EncodingMode.AUTO, EncodingMode.getEncodingMode("auto"));
        assertEquals(EncodingMode.SINGLE_BYTE, EncodingMode.getEncodingMode("single-byte"));
        assertEquals(EncodingMode.CID, EncodingMode.getEncodingMode("cid"));
    }
}
