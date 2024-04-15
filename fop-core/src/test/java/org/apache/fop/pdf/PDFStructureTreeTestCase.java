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

// $Id$
package org.apache.fop.pdf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;

public class PDFStructureTreeTestCase {
    @Test
    public void testRemoveUnusedStructs() throws Exception {
        String fo = "<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\">\n"
                + "  <fo:layout-master-set>\n"
                + "    <fo:simple-page-master page-width=\"8.5in\" page-height=\"11in\" master-name=\"First\">\n"
                + "      <fo:region-body region-name=\"Body\"/>\n"
                + "      <fo:region-before extent=\"1in\" region-name=\"Header\"/>\n"
                + "    </fo:simple-page-master>\n"
                + "    <fo:simple-page-master page-width=\"8.5in\" page-height=\"11in\" master-name=\"Rest\">\n"
                + "      <fo:region-body region-name=\"Body\"/>\n"
                + "      <fo:region-before extent=\"1in\" region-name=\"Header Rest\"/>\n"
                + "    </fo:simple-page-master>\n"
                + "    <fo:page-sequence-master master-name=\"PSM\">\n"
                + "      <fo:repeatable-page-master-alternatives>\n"
                + "        <fo:conditional-page-master-reference page-position=\"first\" master-reference=\"First\"/>\n"
                + "        <fo:conditional-page-master-reference page-position=\"rest\" master-reference=\"Rest\"/>\n"
                + "      </fo:repeatable-page-master-alternatives>\n"
                + "    </fo:page-sequence-master>\n"
                + "  </fo:layout-master-set>\n"
                + "  <fo:page-sequence master-reference=\"PSM\">\n"
                + "    <fo:static-content flow-name=\"Header\">\n"
                + "      <fo:block><fo:external-graphic src=\"test/resources/fop/image/logo.jpg\" /></fo:block>\n"
                + "    </fo:static-content>\n"
                + "    <fo:static-content flow-name=\"Header Rest\">\n"
                + "      <fo:block><fo:external-graphic src=\"test/resources/fop/svg/logo.jpg\" /></fo:block>\n"
                + "    </fo:static-content>\n"
                + "    <fo:flow flow-name=\"Body\">\n"
                + "      <fo:block>test</fo:block>\n"
                + "    </fo:flow>\n"
                + "  </fo:page-sequence>\n"
                + "</fo:root>";
        ByteArrayOutputStream bos = foToOutput(fo);
        String pdf = bos.toString();
        Assert.assertEquals(pdf.split("/S /Figure").length, 2);
        Assert.assertEquals(pdf.split("/S /").length, 11);
    }

    private ByteArrayOutputStream foToOutput(String fo)
            throws IOException, SAXException, TransformerException {
        FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI());
        FOUserAgent userAgent = fopFactory.newFOUserAgent();
        userAgent.setAccessibility(true);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, userAgent, bos);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        Source src = new StreamSource(new ByteArrayInputStream(fo.getBytes("UTF-8")));
        Result res = new SAXResult(fop.getDefaultHandler());
        transformer.transform(src, res);
        return bos;
    }
}
