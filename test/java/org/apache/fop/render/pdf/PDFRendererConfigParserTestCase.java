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

package org.apache.fop.render.pdf;

import org.junit.Test;

import org.apache.fop.apps.AbstractRendererConfigParserTester;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FopConfBuilder;
import org.apache.fop.apps.PDFRendererConfBuilder;
import org.apache.fop.pdf.PDFAMode;
import org.apache.fop.pdf.PDFXMode;
import org.apache.fop.pdf.Version;
import org.apache.fop.render.pdf.PDFRendererConfig.PDFRendererConfigParser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PDFRendererConfigParserTestCase
        extends AbstractRendererConfigParserTester<PDFRendererConfBuilder, PDFRendererConfig> {

    public PDFRendererConfigParserTestCase() {
        super(new PDFRendererConfigParser(), PDFRendererConfBuilder.class);
    }

    @Test
    public void testUserPassword() throws Exception {
        String testPassword = "this is a password purely for test purposes";
        parseConfig(createRenderer()
                .startEncryptionParams()
                    .setUserPassword(testPassword)
                .endEncryptionParams());
        assertEquals(testPassword, conf.getEncryptionParameters().getUserPassword());
    }

    private void testRestrictEncryptionParameter(PDFRendererConfigOption option)
            throws Exception {
        parseConfig(createRenderer().startEncryptionParams()
                                .setAllowParam(option)
                            .endEncryptionParams());
        assertFalse(testEncryptionParameter(option));
        parseConfig(createRenderer().startEncryptionParams()
                            .endEncryptionParams());
        assertTrue(testEncryptionParameter(option));
    }

    public boolean testEncryptionParameter(PDFRendererConfigOption option) throws Exception {
        switch (option) {
        case NO_PRINT:
            return conf.getEncryptionParameters().isAllowPrint();
        case NO_ACCESSCONTENT:
            return conf.getEncryptionParameters().isAllowAccessContent();
        case NO_ANNOTATIONS:
            return conf.getEncryptionParameters().isAllowEditAnnotations();
        case NO_ASSEMBLEDOC:
            return conf.getEncryptionParameters().isAllowAssembleDocument();
        case NO_COPY_CONTENT:
            return conf.getEncryptionParameters().isAllowCopyContent();
        case NO_EDIT_CONTENT:
            return conf.getEncryptionParameters().isAllowEditContent();
        case NO_FILLINFORMS:
            return conf.getEncryptionParameters().isAllowFillInForms();
        case NO_PRINTHQ:
            return conf.getEncryptionParameters().isAllowPrintHq();
        default:
            throw new IllegalStateException("Wrong parameter given");
        }

    }

    @Test
    public void testAllEncryptionRestrictions() throws Exception {
        testRestrictEncryptionParameter(PDFRendererConfigOption.NO_PRINT);
        testRestrictEncryptionParameter(PDFRendererConfigOption.NO_ACCESSCONTENT);
        testRestrictEncryptionParameter(PDFRendererConfigOption.NO_ANNOTATIONS);
        testRestrictEncryptionParameter(PDFRendererConfigOption.NO_ASSEMBLEDOC);
        testRestrictEncryptionParameter(PDFRendererConfigOption.NO_COPY_CONTENT);
        testRestrictEncryptionParameter(PDFRendererConfigOption.NO_EDIT_CONTENT);
        testRestrictEncryptionParameter(PDFRendererConfigOption.NO_FILLINFORMS);
        testRestrictEncryptionParameter(PDFRendererConfigOption.NO_PRINTHQ);
    }

    @Test
    public void testOwnerPassword() throws Exception {
        String testPassword = "this is a password purely for test purposes";
        parseConfig(createRenderer()
                .startEncryptionParams()
                    .setOwnerPassword(testPassword)
                .endEncryptionParams());
        assertEquals(testPassword, conf.getEncryptionParameters().getOwnerPassword());
    }

    @Test
    public void testFilterListDefaultFlate() throws Exception {
        parseConfig(createRenderer().createFilterList(null, "flate"));
        assertEquals("flate", conf.getFilterMap().get("default").get(0));
    }

    @Test
    public void testFilterListDefaultNull() throws Exception {
        parseConfig(createRenderer().createFilterList(null, "null"));
        assertEquals("null", conf.getFilterMap().get("default").get(0));
    }

    @Test
    public void testFilterListImage() throws Exception {
        parseConfig(createRenderer().createFilterList("image", "flate", "ascii-85"));
        assertEquals("flate", conf.getFilterMap().get("image").get(0));
        assertEquals("ascii-85", conf.getFilterMap().get("image").get(1));
    }

    @Test
    public void testPDFAMode() throws Exception {
        parseConfig(createRenderer().setPDFAMode(PDFAMode.PDFA_1A.getName()));
        assertEquals(PDFAMode.PDFA_1A, conf.getPDFAMode());

        parseConfig(createRenderer().setPDFAMode(PDFAMode.PDFA_1B.getName()));
        assertEquals(PDFAMode.PDFA_1B, conf.getPDFAMode());

        parseConfig(createRenderer().setPDFAMode(PDFAMode.DISABLED.getName()));
        assertEquals(null, conf.getPDFAMode());
    }

    @Test
    public void testPDFXMode() throws Exception {
        parseConfig(createRenderer().setPDFXMode(PDFXMode.PDFX_3_2003.getName()));
        assertEquals(PDFXMode.PDFX_3_2003, conf.getPDFXMode());

        parseConfig(createRenderer().setPDFXMode(PDFXMode.DISABLED.getName()));
        assertEquals(null, conf.getPDFXMode());
    }

    @Test
    public void testEncryptionLength() throws Exception {
        for (int i = 0; i <= 40; i++) {
            parseConfig(createRenderer()
                    .startEncryptionParams()
                        .setEncryptionLength(i)
                    .endEncryptionParams());
            assertEquals(40, conf.getEncryptionParameters().getEncryptionLengthInBits());
        }

        for (int i = 40; i <= 128; i++) {
            parseConfig(createRenderer()
                    .startEncryptionParams()
                        .setEncryptionLength(i)
                    .endEncryptionParams());
            int expectedLen = Math.round(i / 8.0f) * 8;
            assertEquals(expectedLen, conf.getEncryptionParameters()
                                                 .getEncryptionLengthInBits());
        }

        for (int i = 128; i < 1000; i += 50) {
            parseConfig(createRenderer()
                    .startEncryptionParams()
                        .setEncryptionLength(i)
                    .endEncryptionParams());
            assertEquals(128, conf.getEncryptionParameters().getEncryptionLengthInBits());
        }
    }

    @Test
    public void testPDFVersions() throws Exception {
        for (int i = 0; i <= 7; i++) {
            pdfVersionTester("1." + i);
        }
    }

    private void pdfVersionTester(String version) throws Exception {
        parseConfig(createRenderer().setPDFVersion(version));
        assertEquals(Version.getValueOf(version), conf.getPDFVersion());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testErroneousPDFVersions18() throws Exception {
        pdfVersionTester("1.8");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testErroneousPDFVersionsLessThan1() throws Exception {
        pdfVersionTester("0.9");
    }

    @Test(expected = FOPException.class)
    public void testErroneousPDFVersionsNotSet() throws Exception {
        pdfVersionTester("");
    }
}
