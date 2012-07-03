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
        assertEquals(testPassword, conf.getConfigOptions().getEncryptionParameters().getUserPassword());
    }

    private void testRestrictEncryptionParameter(PDFEncryptionOption option) throws Exception {
        parseConfig(createRenderer().startEncryptionParams()
                                .setAllowParam(option)
                            .endEncryptionParams());
        assertFalse(testEncryptionParameter(option));
        parseConfig(createRenderer().startEncryptionParams()
                            .endEncryptionParams());
        assertTrue(testEncryptionParameter(option));
    }

    public boolean testEncryptionParameter(PDFEncryptionOption option) throws Exception {
        switch (option) {
        case NO_PRINT:
            return conf.getConfigOptions().getEncryptionParameters().isAllowPrint();
        case NO_ACCESSCONTENT:
            return conf.getConfigOptions().getEncryptionParameters().isAllowAccessContent();
        case NO_ANNOTATIONS:
            return conf.getConfigOptions().getEncryptionParameters().isAllowEditAnnotations();
        case NO_ASSEMBLEDOC:
            return conf.getConfigOptions().getEncryptionParameters().isAllowAssembleDocument();
        case NO_COPY_CONTENT:
            return conf.getConfigOptions().getEncryptionParameters().isAllowCopyContent();
        case NO_EDIT_CONTENT:
            return conf.getConfigOptions().getEncryptionParameters().isAllowEditContent();
        case NO_FILLINFORMS:
            return conf.getConfigOptions().getEncryptionParameters().isAllowFillInForms();
        case NO_PRINTHQ:
            return conf.getConfigOptions().getEncryptionParameters().isAllowPrintHq();
        default:
            throw new IllegalStateException("Wrong parameter given");
        }

    }

    @Test
    public void testAllEncryptionRestrictions() throws Exception {
        testRestrictEncryptionParameter(PDFEncryptionOption.NO_PRINT);
        testRestrictEncryptionParameter(PDFEncryptionOption.NO_ACCESSCONTENT);
        testRestrictEncryptionParameter(PDFEncryptionOption.NO_ANNOTATIONS);
        testRestrictEncryptionParameter(PDFEncryptionOption.NO_ASSEMBLEDOC);
        testRestrictEncryptionParameter(PDFEncryptionOption.NO_COPY_CONTENT);
        testRestrictEncryptionParameter(PDFEncryptionOption.NO_EDIT_CONTENT);
        testRestrictEncryptionParameter(PDFEncryptionOption.NO_FILLINFORMS);
        testRestrictEncryptionParameter(PDFEncryptionOption.NO_PRINTHQ);
    }

    @Test
    public void testOwnerPassword() throws Exception {
        String testPassword = "this is a password purely for test purposes";
        parseConfig(createRenderer()
                .startEncryptionParams()
                    .setOwnerPassword(testPassword)
                .endEncryptionParams());
        assertEquals(testPassword, conf.getConfigOptions().getEncryptionParameters().getOwnerPassword());
    }

    @Test
    public void testFilterListDefaultFlate() throws Exception {
        parseConfig(createRenderer().createFilterList(null, "flate"));
        assertEquals("flate", conf.getConfigOptions().getFilterMap().get("default").get(0));
    }

    @Test
    public void testFilterListDefaultNull() throws Exception {
        parseConfig(createRenderer().createFilterList(null, "null"));
        assertEquals("null", conf.getConfigOptions().getFilterMap().get("default").get(0));
    }

    @Test
    public void testFilterListImage() throws Exception {
        parseConfig(createRenderer().createFilterList("image", "flate", "ascii-85"));
        assertEquals("flate", conf.getConfigOptions().getFilterMap().get("image").get(0));
        assertEquals("ascii-85", conf.getConfigOptions().getFilterMap().get("image").get(1));
    }

    @Test
    public void testPDFAMode() throws Exception {
        parseConfig(createRenderer().setPDFAMode(PDFAMode.PDFA_1A.getName()));
        assertEquals(PDFAMode.PDFA_1A, conf.getConfigOptions().getPDFAMode());

        parseConfig(createRenderer().setPDFAMode(PDFAMode.PDFA_1B.getName()));
        assertEquals(PDFAMode.PDFA_1B, conf.getConfigOptions().getPDFAMode());

        parseConfig(createRenderer().setPDFAMode(PDFAMode.DISABLED.getName()));
        assertEquals(null, conf.getConfigOptions().getPDFAMode());
    }

    @Test
    public void testPDFXMode() throws Exception {
        parseConfig(createRenderer().setPDFXMode(PDFXMode.PDFX_3_2003.getName()));
        assertEquals(PDFXMode.PDFX_3_2003, conf.getConfigOptions().getPDFXMode());

        parseConfig(createRenderer().setPDFXMode(PDFXMode.DISABLED.getName()));
        assertEquals(null, conf.getConfigOptions().getPDFXMode());
    }

    @Test
    public void testEncryptionLength() throws Exception {
        for (int i = 0; i <= 40; i++) {
            parseConfig(createRenderer()
                    .startEncryptionParams()
                        .setEncryptionLength(i)
                    .endEncryptionParams());
            assertEquals(40, conf.getConfigOptions().getEncryptionParameters().getEncryptionLengthInBits());
        }

        for (int i = 40; i <= 128; i++) {
            parseConfig(createRenderer()
                    .startEncryptionParams()
                        .setEncryptionLength(i)
                    .endEncryptionParams());
            int expectedLen = Math.round(i / 8.0f) * 8;
            assertEquals(expectedLen, conf.getConfigOptions().getEncryptionParameters()
                                                 .getEncryptionLengthInBits());
        }

        for (int i = 128; i < 1000; i += 50) {
            parseConfig(createRenderer()
                    .startEncryptionParams()
                        .setEncryptionLength(i)
                    .endEncryptionParams());
            assertEquals(128, conf.getConfigOptions().getEncryptionParameters().getEncryptionLengthInBits());
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
        assertEquals(Version.getValueOf(version), conf.getConfigOptions().getPDFVersion());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testErroneousPDFVersions18() throws Exception {
        pdfVersionTester("1.8");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testErroneousPDFVersionsLessThan1() throws Exception {
        pdfVersionTester("0.9");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testErroneousPDFVersionsNotSet() throws Exception {
        pdfVersionTester("");
    }
}
