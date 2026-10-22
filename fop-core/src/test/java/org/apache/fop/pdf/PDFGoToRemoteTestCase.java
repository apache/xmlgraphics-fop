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

package org.apache.fop.pdf;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PDFGoToRemoteTestCase {

    private static final String DEST = "secretDestination";

    private PDFGoToRemote createAction(PDFDocument document) {
        PDFFileSpec fileSpec = new PDFFileSpec("other.pdf");
        fileSpec.setObjectNumber(2);
        PDFGoToRemote action = new PDFGoToRemote(fileSpec, DEST, false);
        action.setDocument(document);
        action.setObjectNumber(1);
        return action;
    }

    @Test
    public void testToPDFStringShouldEncryptDestination() {
        PDFDocument document = new PDFDocument("<test />");
        document.setEncryption(new PDFEncryptionParams("user", null, false,
                true, false, true, true));
        String pdfString = createAction(document).toPDFString();
        assertFalse(pdfString.contains(DEST));
        assertTrue(pdfString.contains("/D <"));
    }

    @Test
    public void testToPDFStringUnencrypted() {
        String pdfString = createAction(new PDFDocument("<test />")).toPDFString();
        assertEquals("<<\n/S /GoToR\n/F 2 0 R\n/D (" + DEST + ")\n>>", pdfString);
    }

    @Test
    public void testToPDFStringEscapesDestination() {
        PDFFileSpec fileSpec = new PDFFileSpec("other.pdf");
        fileSpec.setObjectNumber(2);
        PDFGoToRemote action = new PDFGoToRemote(fileSpec, "a)b", false);
        action.setDocument(new PDFDocument("<test />"));
        assertTrue(action.toPDFString().contains("/D (a\\)b)"));
    }
}
