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

package org.apache.fop.render.afp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;

import org.apache.fop.afp.AFPConstants;
import org.apache.fop.afp.parser.MODCAParser;
import org.apache.fop.afp.parser.UnparsedStructuredField;
import org.apache.fop.apps.FOUserAgent;

/**
 * Tests generation of afp:no-operation (NOPs).
 */
public class NoOperationTestCase extends AbstractAFPTestCase {

    /**
     * Tests afp:no-operation.
     * @throws Exception if an error occurs
     */
    public void testNoOperation() throws Exception {
        FOUserAgent ua = fopFactory.newFOUserAgent();
        File outputFile = renderFile(ua, "nops.fo", "");

        InputStream in = new java.io.FileInputStream(outputFile);
        try {
            MODCAParser parser = new MODCAParser(in);
            UnparsedStructuredField field = skipTo(parser, 0xD3A8A8); //Begin Document

            //NOP in fo:declarations
            field = parser.readNextStructuredField();
            assertEquals(0xD3EEEE, field.getSfTypeID());
            assertEquals("fo:declarations", getNopText(field));

            field = parser.readNextStructuredField();
            assertEquals(0xD3A8AD, field.getSfTypeID()); //Begin Named Page Group

            //NOPs in fo:page-sequence
            field = parser.readNextStructuredField();
            assertEquals(0xD3EEEE, field.getSfTypeID());
            assertEquals("fo:page-sequence: start", getNopText(field));
            field = parser.readNextStructuredField();
            assertEquals(0xD3EEEE, field.getSfTypeID());
            assertEquals("fo:page-sequence: end", getNopText(field));

            field = parser.readNextStructuredField();
            assertEquals(0xD3A8AF, field.getSfTypeID()); //Begin Page

            field = skipTo(parser, 0xD3A9C9); //End Active Environment Group
            field = parser.readNextStructuredField();
            assertEquals(0xD3EEEE, field.getSfTypeID());
            assertEquals("fo:simple-page-master: first", getNopText(field));

            field = skipTo(parser, 0xD3A9C9); //End Active Environment Group
            field = parser.readNextStructuredField();
            assertEquals(0xD3EEEE, field.getSfTypeID());
            assertEquals("fo:simple-page-master: rest", getNopText(field));
        } finally {
            IOUtils.closeQuietly(in);
        }

        int counter = 0;
        in = new java.io.FileInputStream(outputFile);
        try {
            MODCAParser parser = new MODCAParser(in);
            while (true) {
                UnparsedStructuredField field = parser.readNextStructuredField();
                if (field == null) {
                    break;
                }
                if (field.getSfTypeID() == 0xD3EEEE) {
                    counter++;
                }
            }
        } finally {
            IOUtils.closeQuietly(in);
        }
        assertEquals(6, counter); //decl, 2 * ps, 3 * page/spm
    }

    private String getNopText(UnparsedStructuredField field) throws UnsupportedEncodingException {
        byte[] data = field.getData();
        String text = new String(data, AFPConstants.EBCIDIC_ENCODING);
        return text;
    }

    private UnparsedStructuredField skipTo(MODCAParser parser, int typeID) throws IOException {
        UnparsedStructuredField field = null;
        do {
            field = parser.readNextStructuredField();
            if (field.getSfTypeID() == typeID) {
                return field;
            }
        } while (field != null);
        Assert.fail("Structured field not found: " + Integer.toHexString(typeID));
        return null;
    }

}
