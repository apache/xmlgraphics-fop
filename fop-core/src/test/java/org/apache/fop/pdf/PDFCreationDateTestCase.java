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
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.Assert;
import org.junit.Test;

import org.apache.xmlgraphics.util.DateFormatUtil;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;

/**
 * Checks that the creation date set on the user agent is used for the XMP metadata dates
 * and, with the file-id-from-creation-date option, for the file ID.
 */
public class PDFCreationDateTestCase {

    private static final Date CREATION_DATE = new Date(1000L * 1793491658L);

    @Test
    public void testMetadataDateUsesCreationDate() throws Exception {
        String pdf = new String(render(false), "ISO-8859-1");
        String date = DateFormatUtil.formatISO8601(CREATION_DATE, TimeZone.getDefault());
        Assert.assertTrue(pdf, pdf.contains("<xmp:MetadataDate>" + date + "</xmp:MetadataDate>"));
        Assert.assertTrue(pdf, pdf.contains("<xmp:CreateDate>" + date + "</xmp:CreateDate>"));
    }

    @Test
    public void testFileIDFromCreationDateGivesSameFile() throws Exception {
        byte[] first = render(true);
        Thread.sleep(50);
        byte[] second = render(true);
        Assert.assertArrayEquals(first, second);
    }

    @Test
    public void testFileIDUsesCurrentTimeByDefault() throws Exception {
        byte[] first = render(false);
        Thread.sleep(50);
        byte[] second = render(false);
        Assert.assertFalse(Arrays.equals(first, second));
    }

    private byte[] render(boolean fileIDFromCreationDate) throws Exception {
        String fopxconf = "<fop version=\"1.0\"><renderers><renderer mime=\"application/pdf\">"
                + "<file-id-from-creation-date>" + fileIDFromCreationDate + "</file-id-from-creation-date>"
                + "</renderer></renderers></fop>";
        FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI(),
                new ByteArrayInputStream(fopxconf.getBytes("UTF-8")));
        FOUserAgent userAgent = fopFactory.newFOUserAgent();
        userAgent.setCreationDate(CREATION_DATE);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, userAgent, out);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        Source src = new StreamSource(PDFCreationDateTestCase.class.getResource("PDFPageXMP.fo").openStream());
        Result res = new SAXResult(fop.getDefaultHandler());
        transformer.transform(src, res);
        return out.toByteArray();
    }
}
