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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import org.apache.fop.pdf.PDFAMode;
import org.apache.fop.pdf.PDFEncryptionParams;
import org.apache.fop.pdf.PDFXMode;
import org.apache.fop.pdf.Version;

import static org.apache.fop.render.pdf.PDFRendererOption.DISABLE_SRGB_COLORSPACE;
import static org.apache.fop.render.pdf.PDFRendererOption.FILTER_LIST;
import static org.apache.fop.render.pdf.PDFRendererOption.OUTPUT_PROFILE;
import static org.apache.fop.render.pdf.PDFRendererOption.PDF_A_MODE;
import static org.apache.fop.render.pdf.PDFRendererOption.PDF_X_MODE;
import static org.apache.fop.render.pdf.PDFRendererOption.VERSION;

public class PDFRendererOptionsConfigTestCase {

    private static final  Map<PDFRendererOption, Object> DEFAULT_OPTIONS;

    private static final  PDFEncryptionParams DEFAULT_ENCRYPTION_PARAMS = null;

    static {
        final EnumMap<PDFRendererOption, Object> props
        = new EnumMap<PDFRendererOption, Object>(PDFRendererOption.class);
        for (PDFRendererOption option : PDFRendererOption.values()) {
            props.put(option, option.getDefaultValue());
        }
        DEFAULT_OPTIONS = Collections.unmodifiableMap(props);
    }

    @Test
    public void testDefaults() {
        assertConfigEquals(DEFAULT_OPTIONS, DEFAULT_ENCRYPTION_PARAMS, PDFRendererOptionsConfig.DEFAULT);
    }

    @Test
    public void testConstructor() throws URISyntaxException {
        final Map<PDFRendererOption, Object> options = createNonDefaultOptions();
        final PDFEncryptionParams encryptionParams = new PDFEncryptionParams();
        assertConfigEquals(options, encryptionParams, new PDFRendererOptionsConfig(options, encryptionParams));
    }

    @Test
    public void testMerge() throws URISyntaxException {
        final PDFRendererOptionsConfig defaults = PDFRendererOptionsConfig.DEFAULT;
        final PDFEncryptionParams encryptionParams = new PDFEncryptionParams();
        final Map<PDFRendererOption, Object> options = createNonDefaultOptions();
        final PDFRendererOptionsConfig otherConfig = new PDFRendererOptionsConfig(options, encryptionParams);
        assertConfigEquals(options, encryptionParams, defaults.merge(otherConfig));
        assertEquals(defaults, defaults.merge(null));
    }

    private Map<PDFRendererOption, Object> createNonDefaultOptions() throws URISyntaxException {
        final EnumMap<PDFRendererOption, Object> options
        = new EnumMap<PDFRendererOption, Object>(PDFRendererOption.class);
        options.put(DISABLE_SRGB_COLORSPACE, Boolean.TRUE);
        options.put(FILTER_LIST, new HashMap<String, List<String>>());
        options.put(OUTPUT_PROFILE, new URI("xxx"));
        options.put(PDF_A_MODE, PDFAMode.PDFA_1A);
        options.put(PDF_X_MODE, PDFXMode.PDFX_3_2003);
        options.put(VERSION, Version.V1_0);
        return Collections.unmodifiableMap(options);
    }

    private static void assertConfigEquals(Map<PDFRendererOption, Object> expectedOptions,
            PDFEncryptionParams expectedEncryptionParams, PDFRendererOptionsConfig actual) {
        assertEquals((Boolean) expectedOptions.get(DISABLE_SRGB_COLORSPACE), actual.getDisableSRGBColorSpace());
        assertEquals((Map) expectedOptions.get(FILTER_LIST), actual.getFilterMap());
        assertEquals((URI) expectedOptions.get(OUTPUT_PROFILE), actual.getOutputProfileURI());
        assertEquals((PDFAMode) expectedOptions.get(PDF_A_MODE), actual.getPDFAMode());
        assertEquals((PDFXMode) expectedOptions.get(PDF_X_MODE), actual.getPDFXMode());
        assertEquals((Version) expectedOptions.get(VERSION), actual.getPDFVersion());
        assertEncryptionParamsEquals(expectedEncryptionParams, actual.getEncryptionParameters());
    }

    private static void assertEncryptionParamsEquals(PDFEncryptionParams expected,
            PDFEncryptionParams actual) {
        assertEquals(expected == null, actual == null);
        if (actual != null) {
            assertEquals(expected.getEncryptionLengthInBits(), actual.getEncryptionLengthInBits());
            assertEquals(expected.getOwnerPassword(), actual.getOwnerPassword());
            assertEquals(expected.getUserPassword(), actual.getUserPassword());
            assertEquals(expected.isAllowAccessContent(), actual.isAllowAccessContent());
            assertEquals(expected.isAllowAssembleDocument(), actual.isAllowAssembleDocument());
            assertEquals(expected.isAllowCopyContent(), actual.isAllowCopyContent());
            assertEquals(expected.isAllowEditAnnotations(), actual.isAllowEditAnnotations());
            assertEquals(expected.isAllowEditContent(), actual.isAllowEditContent());
            assertEquals(expected.isAllowFillInForms(), actual.isAllowFillInForms());
            assertEquals(expected.isAllowPrint(), actual.isAllowPrint());
            assertEquals(expected.isAllowPrintHq(), actual.isAllowPrintHq());
        }
    }

}
