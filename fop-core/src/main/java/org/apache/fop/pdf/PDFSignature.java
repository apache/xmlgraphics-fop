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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;

import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.CountingOutputStream;

import org.apache.xmlgraphics.io.TempResourceURIGenerator;

import org.apache.fop.apps.FOUserAgent;

public class PDFSignature {
    private static final int SIZE_OF_CONTENTS = 18944;
    private static final TempResourceURIGenerator TEMP_URI_GENERATOR = new TempResourceURIGenerator("pdfsign2");
    private Perms perms;
    private PDFRoot root;
    private PrivateKey privateKey;
    private long startOfDocMDP;
    private long startOfContents;
    private FOUserAgent userAgent;
    private URI tempURI;
    private PDFSignParams signParams;

    static class TransformParams extends PDFDictionary {
        TransformParams() {
            put("Type", new PDFName("TransformParams"));
            put("P", 2);
            put("V", new PDFName("1.2"));
        }
    }

    static class SigRef extends PDFDictionary {
        SigRef() {
            put("Type", new PDFName("SigRef"));
            put("TransformMethod", new PDFName("DocMDP"));
            put("DigestMethod", new PDFName("SHA1"));
            put("TransformParams", new TransformParams());
        }
    }

    class Contents extends PDFObject {
        protected String toPDFString() {
            return PDFText.toHex(new byte[SIZE_OF_CONTENTS / 2]);
        }

        public int output(OutputStream stream) throws IOException {
            CountingOutputStream countingOutputStream = (CountingOutputStream) stream;
            startOfContents = startOfDocMDP + countingOutputStream.getByteCount();
            return super.output(stream);
        }
    }

    class DocMDP extends PDFDictionary {
        DocMDP() {
            put("Type", new PDFName("Sig"));
            put("Filter", new PDFName("Adobe.PPKLite"));
            put("SubFilter", new PDFName("adbe.pkcs7.detached"));
            if (signParams.getName() != null) {
                put("Name", signParams.getName());
            }
            if (signParams.getLocation() != null) {
                put("Location", signParams.getLocation());
            }
            if (signParams.getReason() != null) {
                put("Reason", signParams.getReason());
            }
            put("M", PDFInfo.formatDateTime(new Date()));
            PDFArray array = new PDFArray();
            array.add(new SigRef());
            put("Reference", array);
            put("Contents", new Contents());
            put("ByteRange", new PDFArray(0, 1000000000, 1000000000, 1000000000));
        }

        public int output(OutputStream stream) throws IOException {
            if (stream instanceof CountingOutputStream) {
                CountingOutputStream countingOutputStream = (CountingOutputStream) stream;
                startOfDocMDP = countingOutputStream.getByteCount();
                return super.output(stream);
            }
            throw new IOException("Disable pdf linearization and use-object-streams");
        }
    }

    static class Perms extends PDFDictionary {
        DocMDP docMDP;
        Perms(PDFRoot root, DocMDP docMDP) {
            this.docMDP = docMDP;
            root.getDocument().registerObject(docMDP);
            put("DocMDP", docMDP);
        }
    }

    static class SigField extends PDFDictionary {
        SigField(Perms perms, PDFPage page, PDFRoot root) {
            root.getDocument().registerObject(this);
            put("FT", new PDFName("Sig"));
            put("Type", new PDFName("Annot"));
            put("Subtype", new PDFName("Widget"));
            put("F", 132);
            put("T", "Signature1");
            put("TU", "Signature1");
            put("Rect", new PDFRectangle(0, 0, 0, 0));
            put("V", perms.docMDP);
            put("P", new PDFReference(page));
            put("AP", new AP(root));
        }
    }

    static class AP extends PDFDictionary {
        AP(PDFRoot root) {
            put("N", new FormXObject(root));
        }
    }

    static class FormXObject extends PDFStream {
        FormXObject(PDFRoot root) {
            root.getDocument().registerObject(this);
            put("Length", 0);
            put("Type", new PDFName("XObject"));
            put("Subtype", new PDFName("Form"));
            put("BBox", new PDFRectangle(0, 0, 0, 0));
        }
    }

    static class AcroForm extends PDFDictionary {
        AcroForm(SigField sigField) {
            PDFArray fields = new PDFArray();
            fields.add(sigField);
            put("Fields", fields);
            put("SigFlags", 3);
        }
    }

    public PDFSignature(PDFRoot root, FOUserAgent userAgent, PDFSignParams signParams) {
        this.root = root;
        this.userAgent = userAgent;
        this.signParams = signParams;
        perms = new Perms(root, new DocMDP());
        root.put("Perms", perms);
        tempURI = TEMP_URI_GENERATOR.generate();
    }

    public void add(PDFPage page) {
        SigField sigField = new SigField(perms, page, root);
        root.put("AcroForm", new AcroForm(sigField));
        page.addAnnotation(sigField);
    }

    public void signPDF(URI uri, OutputStream os) throws IOException {
        try (InputStream pdfIS = getTempIS(uri)) {
            pdfIS.mark(Integer.MAX_VALUE);
            String byteRangeValues = "0 1000000000 1000000000 1000000000";
            String byteRange = "\n  /ByteRange [" + byteRangeValues + "]";
            int pdfLength = pdfIS.available();
            long offsetToPDFEnd = startOfContents + SIZE_OF_CONTENTS + 2 + byteRange.length();
            long endOfPDFSize = pdfLength - offsetToPDFEnd;
            String byteRangeValues2 = String.format("0 %s %s %s", startOfContents,
                    startOfContents + SIZE_OF_CONTENTS + 2, byteRange.length() + endOfPDFSize);
            byteRange = "\n  /ByteRange [" + byteRangeValues2 + "]";
            String byteRangePadding = new String(new char[byteRangeValues.length() - byteRangeValues2.length()])
                    .replace("\0", " ");
            try (OutputStream editedPDF = getTempOS()) {
                IOUtils.copyLarge(pdfIS, editedPDF, 0, startOfContents);
                editedPDF.write(byteRange.getBytes("UTF-8"));
                editedPDF.write(byteRangePadding.getBytes("UTF-8"));
                IOUtils.copyLarge(pdfIS, editedPDF, offsetToPDFEnd - startOfContents, Long.MAX_VALUE);
            }
            pdfIS.reset();
            IOUtils.copyLarge(pdfIS, os, 0, startOfContents);
            try (InputStream is = getTempIS(tempURI)) {
                byte[] signed = readPKCS(is);
                String signedHexPadding = new String(new char[SIZE_OF_CONTENTS - (signed.length * 2)])
                        .replace("\0", "0");
                String signedHex = "<" + PDFText.toHex(signed, false) + signedHexPadding + ">";
                os.write(signedHex.getBytes("UTF-8"));
            }
            os.write(byteRange.getBytes("UTF-8"));
            os.write(byteRangePadding.getBytes("UTF-8"));
            IOUtils.copyLarge(pdfIS, os, offsetToPDFEnd - startOfContents, Long.MAX_VALUE);
        }
    }

    private OutputStream getTempOS() throws IOException {
        return new BufferedOutputStream(userAgent.getResourceResolver().getOutputStream(tempURI));
    }

    private InputStream getTempIS(URI uri) throws IOException {
        return new BufferedInputStream(userAgent.getResourceResolver().getResource(uri));
    }

    private byte[] readPKCS(InputStream pdf) throws IOException {
        try {
            char[] password = signParams.getPassword().toCharArray();
            KeyStore keystore = KeyStore.getInstance("PKCS12");
            try (InputStream is = userAgent.getResourceResolver().getResource(signParams.getPkcs12())) {
                keystore.load(is, password);
            }
            Certificate[] certificates = readKeystore(keystore, password);
            return sign(pdf, certificates);
        } catch (GeneralSecurityException | URISyntaxException | OperatorException | CMSException e) {
            throw new RuntimeException(e);
        }
    }

    private Certificate[] readKeystore(KeyStore keystore, char[] password)
            throws GeneralSecurityException, IOException {
        Enumeration<String> aliases = keystore.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            privateKey = (PrivateKey) keystore.getKey(alias, password);
            Certificate[] certChain = keystore.getCertificateChain(alias);
            if (certChain != null) {
                Certificate cert = certChain[0];
                if (cert instanceof X509Certificate) {
                    ((X509Certificate) cert).checkValidity();
                }
                return certChain;
            }
        }
        throw new IOException("Could not find certificate");
    }

    private byte[] sign(InputStream content, Certificate[] certChain)
            throws GeneralSecurityException, OperatorException, CMSException, IOException {
        CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
        X509Certificate cert = (X509Certificate) certChain[0];
        ContentSigner sha2Signer = new JcaContentSignerBuilder("SHA256WithRSA").build(privateKey);
        gen.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(
                new JcaDigestCalculatorProviderBuilder().build()).build(sha2Signer, cert));
        gen.addCertificates(new JcaCertStore(Arrays.asList(certChain)));
        CMSProcessableInputStream msg = new CMSProcessableInputStream(content);
        CMSSignedData signedData = gen.generate(msg, false);
        return signedData.getEncoded();
    }
}
