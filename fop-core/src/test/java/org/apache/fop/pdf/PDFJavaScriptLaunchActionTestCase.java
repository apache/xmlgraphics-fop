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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PDFJavaScriptLaunchActionTestCase {

    @Test
    public void testToPDFStringShouldEncrypt() {
        String jsScript = "this.exportDataObject({cName:\"some.pdf\", nLaunch:2});";
        PDFJavaScriptLaunchAction action = new PDFJavaScriptLaunchAction(jsScript);
        PDFDocument document = new PDFDocument("<test />");
        document.setEncryption(new PDFEncryptionParams(null, null, false,
                true, false, true, true));
        action.setDocument(document);
        action.setObjectNumber(1);

        String pdfString = action.toPDFString();

        assertTrue(pdfString.startsWith("<<\n/S /JavaScript\n/JS <"));
        assertFalse(pdfString.contains(jsScript));
        assertTrue(pdfString.endsWith(">\n>>"));
    }
}
