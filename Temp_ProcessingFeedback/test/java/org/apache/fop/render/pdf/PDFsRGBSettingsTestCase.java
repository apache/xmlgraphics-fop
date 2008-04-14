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

import java.io.File;

import org.apache.fop.apps.FOUserAgent;

/**
 * Tests the disables-srgb-colorspace setting.
 */
public class PDFsRGBSettingsTestCase extends BasePDFTestCase {

    private File foBaseDir = new File("test/xml/pdf-a");

    /**
     * Main constructor
     * @param name name of the test case
     */
    public PDFsRGBSettingsTestCase(String name) {
        super(name);
    }

    private FOUserAgent getUserAgent(boolean enablePDFA) {
        final FOUserAgent a = fopFactory.newFOUserAgent();
        if (enablePDFA) {
            a.getRendererOptions().put("pdf-a-mode", "PDF/A-1b");
        }
        a.getRendererOptions().put("disable-srgb-colorspace", Boolean.TRUE);
        return a;
    }
    
    /**
     * Verify that the PDFRenderer complains if PDF/A or PDF/X is used when sRGB is disabled.
     * @throws Exception if the test fails
     */
    public void testPDFAWithDisabledSRGB() throws Exception {
        File foFile = new File(foBaseDir, "minimal-pdf-a.fo");
        try {
            convertFO(foFile, getUserAgent(true), false);
            fail("PDFRenderer must fail if PDF/A is active!");
        } catch (IllegalStateException e) {
            //exception expected!
        }
    }
    
}
