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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.PDDocument;

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
            throws SAXException, TransformerException {
        FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI());
        FOUserAgent userAgent = fopFactory.newFOUserAgent();
        userAgent.setAccessibility(true);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, userAgent, bos);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        Source src = new StreamSource(new ByteArrayInputStream(fo.getBytes(StandardCharsets.UTF_8)));
        Result res = new SAXResult(fop.getDefaultHandler());
        transformer.transform(src, res);
        return bos;
    }

    @Test
    public void testTableHeadersAttribute() throws Exception {
        String fo = "<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\""
                + " xmlns:fox=\"http://xmlgraphics.apache.org/fop/extensions\">\n"
                + "  <fo:layout-master-set>\n"
                + "    <fo:simple-page-master master-name=\"simple\" page-height=\"27.9cm\" page-width=\"21.6cm\">\n"
                + "      <fo:region-body />\n"
                + "    </fo:simple-page-master>\n"
                + "  </fo:layout-master-set>\n"
                + "  <fo:page-sequence master-reference=\"simple\">\n"
                + "    <fo:flow flow-name=\"xsl-region-body\">\n"
                + "      <fo:table table-layout=\"fixed\" width=\"100%\">\n"
                + "        <fo:table-column column-width=\"50%\"/>\n"
                + "        <fo:table-column column-width=\"50%\"/>\n"
                + "        <fo:table-header>\n"
                + "          <fo:table-row>\n"
                + "            <fo:table-cell id=\"h-product\"><fo:block>Product</fo:block></fo:table-cell>\n"
                + "            <fo:table-cell id=\"h-price\"><fo:block>Price</fo:block></fo:table-cell>\n"
                + "          </fo:table-row>\n"
                + "        </fo:table-header>\n"
                + "        <fo:table-body>\n"
                + "          <fo:table-row>\n"
                + "            <fo:table-cell fox:headers=\"h-product\"><fo:block>Widget</fo:block></fo:table-cell>\n"
                + "            <fo:table-cell fox:headers=\"h-product h-price\" number-rows-spanned=\"1\">"
                + "<fo:block>10</fo:block></fo:table-cell>\n"
                + "          </fo:table-row>\n"
                + "        </fo:table-body>\n"
                + "      </fo:table>\n"
                + "    </fo:flow>\n"
                + "  </fo:page-sequence>\n"
                + "</fo:root>\n";
        ByteArrayOutputStream bos = foToOutput(fo);
        Map<String, String> idToType = new HashMap<>();
        List<String> headersEntries = new ArrayList<>();
        try (PDDocument pdfDocument = Loader.loadPDF(bos.toByteArray())) {
            COSDictionary structTreeRoot = pdfDocument.getDocumentCatalog().getCOSObject()
                    .getCOSDictionary(COSName.getPDFName("StructTreeRoot"));
            collectStructureInfo(structTreeRoot, idToType, headersEntries);
        }
        Assert.assertEquals("header cells must carry an ID entry", "TH", idToType.get("h-product"));
        Assert.assertEquals("header cells must carry an ID entry", "TH", idToType.get("h-price"));
        Assert.assertTrue("data cells must reference their header cells",
                headersEntries.contains("h-product"));
        Assert.assertTrue("multiple headers must all be referenced",
                headersEntries.contains("h-product h-price"));
    }

    /**
     * Walks the structure tree collecting for every element with an ID entry
     * its structure type, and for every Table attribute dictionary with a
     * Headers entry the referenced ids as a space-separated string.
     */
    private void collectStructureInfo(COSBase node, Map<String, String> idToType, List<String> headersEntries) {
        if (node instanceof COSObject) {
            collectStructureInfo(((COSObject) node).getObject(), idToType, headersEntries);
        } else if (node instanceof COSArray) {
            for (COSBase item : (COSArray) node) {
                collectStructureInfo(item, idToType, headersEntries);
            }
        } else if (node instanceof COSDictionary) {
            COSDictionary dict = (COSDictionary) node;
            String id = dict.getString(COSName.getPDFName("ID"));
            if (id != null) {
                idToType.put(id, dict.getNameAsString(COSName.S));
            }
            collectHeaders(dict.getDictionaryObject(COSName.A), headersEntries);
            collectStructureInfo(dict.getDictionaryObject(COSName.K), idToType, headersEntries);
        }
    }

    private void collectHeaders(COSBase attributes, List<String> headersEntries) {
        if (attributes instanceof COSObject) {
            collectHeaders(((COSObject) attributes).getObject(), headersEntries);
        } else if (attributes instanceof COSArray) {
            for (COSBase item : (COSArray) attributes) {
                collectHeaders(item, headersEntries);
            }
        } else if (attributes instanceof COSDictionary) {
            COSBase headers = ((COSDictionary) attributes).getDictionaryObject(COSName.getPDFName("Headers"));
            if (headers instanceof COSArray) {
                List<String> ids = new ArrayList<>();
                for (COSBase id : (COSArray) headers) {
                    if (id instanceof COSString) {
                        ids.add(((COSString) id).getString());
                    }
                }
                headersEntries.add(String.join(" ", ids));
            }
        }
    }

    @Test
    public void testFootnote() throws Exception {
        String fo = "<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\">\n"
                + "  <fo:layout-master-set>\n"
                + "    <fo:simple-page-master master-name=\"simple\" page-height=\"27.9cm\" page-width=\"21.6cm\">\n"
                + "      <fo:region-body />\n"
                + "    </fo:simple-page-master>\n"
                + "  </fo:layout-master-set>\n"
                + "  <fo:page-sequence master-reference=\"simple\">\n"
                + "    <fo:flow flow-name=\"xsl-region-body\">\n"
                + "      <fo:block>Text<fo:footnote>\n"
                + "      <fo:inline>1</fo:inline>\n"
                + "      <fo:footnote-body>\n"
                + "         <fo:block>footnote</fo:block>\n"
                + "          </fo:footnote-body>\n"
                + "        </fo:footnote>\n"
                + "      </fo:block>\n"
                + "    </fo:flow>\n"
                + "  </fo:page-sequence>\n"
                + "</fo:root>\n";
        ByteArrayOutputStream bos = foToOutput(fo);
        String pdf = bos.toString();
        Assert.assertTrue(pdf.contains("/ID (Note ID "));
        Assert.assertTrue(pdf.contains("/S /Note"));
        Assert.assertTrue(pdf.contains("/S /Reference"));
    }
}
