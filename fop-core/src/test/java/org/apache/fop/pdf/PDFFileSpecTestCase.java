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

/* $Id: PDFFactoryTestCase.java 1823552 2018-02-08 12:26:33Z ssteiner $ */

package org.apache.fop.pdf;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PDFFileSpecTestCase {

    @Test
    public void testPDFFileSpec() {
        String germanAe = "\u00E4";
        String filename = "test";
        String unicodeFilename = "t" + germanAe + "st";
        PDFFileSpec fileSpec = new PDFFileSpec(filename, unicodeFilename);
        assertEquals(fileSpec.getUnicodeFilename(), unicodeFilename);
        assertEquals(fileSpec.getFilename(), filename);
    }
}
