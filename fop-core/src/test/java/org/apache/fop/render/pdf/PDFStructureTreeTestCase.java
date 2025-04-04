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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Collection;

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
import org.apache.fop.pdf.PDFLinearizationTestCase;

public class PDFStructureTreeTestCase {
    @Test
    public void testStaticRegionPerPage() throws Exception {
        ByteArrayOutputStream bos = foToOutput("<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\">\n"
                + "  <fo:layout-master-set>\n"
                + "    <fo:simple-page-master master-name=\"simple\" page-width=\"8.5in\" page-height=\"11in\">\n"
                + "      <fo:region-body/>\n"
                + "      <fo:region-after region-name=\"Footer\" extent=\"1in\"/>\n"
                + "    </fo:simple-page-master>\n"
                + "  </fo:layout-master-set>\n"
                + "  <fo:page-sequence master-reference=\"simple\">\n"
                + "    <fo:static-content flow-name=\"Footer\">\n"
                + "      <fo:block>footer</fo:block>\n"
                + "    </fo:static-content>\n"
                + "    <fo:flow flow-name=\"xsl-region-body\">\n"
                + "       <fo:block>test</fo:block> \n"
                + "      <fo:block break-before=\"page\">test2</fo:block>\n"
                + "    </fo:flow>\n"
                + "  </fo:page-sequence>\n"
                + "</fo:root>\n");

        Collection<StringBuilder> objs =
                PDFLinearizationTestCase.readObjs(new ByteArrayInputStream(bos.toByteArray())).values();
        int count = 0;
        for (StringBuilder sb : objs) {
            String obj = sb.toString();
            if (obj.contains("/Type /MCR")) {
                Assert.assertEquals(obj, obj.split("/Pg ").length, 2);
                count++;
            }
        }
        Assert.assertEquals(count, 4);
    }

    private ByteArrayOutputStream foToOutput(String fo) throws Exception {
        FopFactory fopFactory = getFopFactory();
        FOUserAgent userAgent = fopFactory.newFOUserAgent();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Fop fop = fopFactory.newFop("application/pdf", userAgent, bos);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        Source src = new StreamSource(new ByteArrayInputStream(fo.getBytes()));
        Result res = new SAXResult(fop.getDefaultHandler());
        transformer.transform(src, res);
        return bos;
    }

    private FopFactory getFopFactory() throws Exception {
        String fopxconf =
                "<fop version=\"1.0\"><accessibility static-region-per-page=\"true\">true</accessibility></fop>";
        return FopFactory.newInstance(new File(".").toURI(), new ByteArrayInputStream(fopxconf.getBytes()));
    }
}
