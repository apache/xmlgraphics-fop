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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.StringTokenizer;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.Assert;
import org.junit.Test;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.fo.pagination.LayoutMasterSetTestCase;

public class PDFSigningTestCase {
    @Test
    public void textFO() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        foToOutput(out, MimeConstants.MIME_PDF);
        StringTokenizer byteRange = new StringTokenizer(out.toString().split("/ByteRange ")[1]);
        byteRange.nextToken();
        int startOfContents = Integer.parseInt(byteRange.nextToken());
        int endOfContents = Integer.parseInt(byteRange.nextToken());
        int sizeOfEnd = Integer.parseInt(byteRange.nextToken().replace("]", ""));
        int sizeOfContents = 18944;
        Assert.assertEquals(endOfContents, startOfContents + sizeOfContents + 2);
        Assert.assertEquals(out.size(), startOfContents + sizeOfContents + 2 + sizeOfEnd);
        ByteArrayInputStream bis = new ByteArrayInputStream(out.toByteArray());
        bis.skip(startOfContents);
        Assert.assertEquals(bis.read(), '<');
        bis.skip(sizeOfContents);
        Assert.assertEquals(bis.read(), '>');
        byte[] end = new byte[200];
        bis.read(end);
        String endStr = new String(end);
        Assert.assertTrue(endStr.contains(
                "/ByteRange [0 " + startOfContents + " " + endOfContents + " " + sizeOfEnd + "]"));
        Assert.assertTrue(endStr.contains("/FT /Sig\n"
                + "  /Type /Annot\n"
                + "  /Subtype /Widget\n"
                + "  /F 132\n"
                + "  /T (Signature1)\n"
                + "  /Rect [0 0 0 0]"));
    }

    private void foToOutput(ByteArrayOutputStream out, String mimeFopIf) throws Exception {
        FopFactory fopFactory = getFopFactory();
        FOUserAgent userAgent = fopFactory.newFOUserAgent();
        Fop fop = fopFactory.newFop(mimeFopIf, userAgent, out);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        Source src = new StreamSource(LayoutMasterSetTestCase.class.getResourceAsStream("side-regions.fo"));
        Result res = new SAXResult(fop.getDefaultHandler());
        transformer.transform(src, res);
    }

    private FopFactory getFopFactory() throws Exception {
        String pkcs = PDFSigningTestCase.class.getResource("keystore.pkcs12").toString();
        String fopxconf = "<fop version=\"1.0\">\n"
                + "  <renderers>\n"
                + "    <renderer mime=\"application/pdf\">\n"
                + "    <sign-params>\n"
                + "      <keystore>" + pkcs + "</keystore>\n"
                + "    </sign-params>\n"
                + "    </renderer>\n"
                + "  </renderers>\n"
                + "</fop>\n";
        return FopFactory.newInstance(new File(".").toURI(),
                new ByteArrayInputStream(fopxconf.getBytes()));
    }
}
