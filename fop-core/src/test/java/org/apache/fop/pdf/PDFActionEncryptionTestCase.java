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

package org.apache.fop.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PDFActionEncryptionTestCase {

    @Test
    public void testGoToRemoteDestinationEncrypted() throws IOException {
        String out = outputGoToRemote(createDocument(true), "secret(dest");
        assertFalse(out.contains("secret"));
        assertTrue(out.contains("/D <"));
    }

    @Test
    public void testGoToRemoteDestinationEscaped() throws IOException {
        String out = outputGoToRemote(createDocument(false), "secret(dest");
        assertTrue(out.contains("/D (secret\\(dest)"));
    }

    @Test
    public void testJavaScriptEncrypted() throws IOException {
        String out = output(createDocument(true), new PDFJavaScriptLaunchAction("app.alert(\"secret\");"));
        assertFalse(out.contains("secret"));
        assertTrue(out.contains("/JS <"));
    }

    @Test
    public void testJavaScriptEscaped() throws IOException {
        String out = output(createDocument(false), new PDFJavaScriptLaunchAction("app.alert(\"secret\");"));
        assertTrue(out.contains("/JS (app.alert\\(\"secret\"\\);)"));
    }

    @Test
    public void testUriEncrypted() throws IOException {
        String out = output(createDocument(true), new PDFUri("http://secret.org", null));
        assertFalse(out.contains("secret"));
        assertTrue(out.contains("/URI <"));
    }

    @Test
    public void testUriEscaped() throws IOException {
        String out = output(createDocument(false), new PDFUri("http://secret.org/a(b", null));
        assertTrue(out.contains("/URI (http://secret.org/a\\(b)"));
    }

    @Test
    public void testUriNotEncryptedInObjectStream() throws IOException {
        PDFUri uri = new PDFUri("http://secret.org", null);
        uri.bypassStringEncryption();
        String out = output(createDocument(true), uri);
        assertTrue(out.contains("/URI (http://secret.org)"));
    }

    @Test
    public void testObjectStreamBypassesStringEncryption() throws IOException {
        PDFDocument doc = createDocument(true);
        PDFUri uri = new PDFUri("http://secret.org", null);
        doc.registerObject(uri);
        ObjectStream objectStream = new ObjectStream();
        doc.registerObject(objectStream);
        objectStream.addObject(uri);
        objectStream.outputRawStreamData(new ByteArrayOutputStream());
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        uri.output(bos);
        assertTrue(bos.toString(StandardCharsets.ISO_8859_1.name()).contains("/URI (http://secret.org)"));
    }

    private PDFDocument createDocument(boolean encrypted) {
        PDFDocument doc = new PDFDocument("");
        if (encrypted) {
            PDFEncryptionParams params = new PDFEncryptionParams();
            params.setEncryptionLengthInBits(256);
            doc.setEncryption(params);
        }
        return doc;
    }

    private String outputGoToRemote(PDFDocument doc, String dest) throws IOException {
        PDFFileSpec fileSpec = new PDFFileSpec("other.pdf");
        doc.registerObject(fileSpec);
        return output(doc, new PDFGoToRemote(fileSpec, dest, false));
    }

    private String output(PDFDocument doc, PDFObject action) throws IOException {
        doc.registerObject(action);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        action.output(bos);
        return bos.toString(StandardCharsets.ISO_8859_1.name());
    }
}
