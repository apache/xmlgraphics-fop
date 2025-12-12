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
import java.util.List;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.Test;
import org.xml.sax.helpers.AttributesImpl;
import static org.junit.Assert.assertEquals;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.pdf.PDFLinearizationTestCase;
import org.apache.fop.pdf.PDFStructElem;
import org.apache.fop.pdf.StandardStructureTypes;

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
                assertEquals(obj, obj.split("/Pg ").length, 2);
                count++;
            }
        }
        assertEquals(count, 4);
    }

    @Test
    public void testTableHeaderDuplicatedIfStaticRegionsPerPageTrue() throws Exception {
        List<PDFStructElem> elems = getPDFStructElems("<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\" "
                + "font-family=\"arial\" font-size=\"16pt\" xml:lang=\"en\">\n"
                + "  <fo:layout-master-set>\n"
                + "    <fo:simple-page-master master-name=\"A5-Page\" page-width=\"148mm\" page-height=\"210mm\">\n"
                + "      <fo:region-body background-color=\"#efefef\" margin=\"10mm\"/>\n"
                + "    </fo:simple-page-master>\n"
                + "    <fo:page-sequence-master master-name=\"A5\">\n"
                + "      <fo:repeatable-page-master-alternatives>\n"
                + "        <fo:conditional-page-master-reference master-reference=\"A5-Page\"/>\n"
                + "      </fo:repeatable-page-master-alternatives>\n"
                + "    </fo:page-sequence-master>\n"
                + "  </fo:layout-master-set>\n"
                + "  <fo:page-sequence master-reference=\"A5\">\n"
                + "    <fo:flow flow-name=\"xsl-region-body\">\n"
                + "      <fo:block>\n"
                + "        <fo:block padding-bottom=\"160mm\">Table overflow</fo:block>\n"
                + "        <fo:table table-layout=\"fixed\" width=\"100%\">\n"
                + "          <fo:table-header>\n"
                + "            <fo:table-row>\n"
                + "              <fo:table-cell number-columns-spanned=\"2\">\n"
                + "                <fo:block font-weight=\"bold\" font-style=\"italic\" text-align=\"left\" "
                + "text-align-last=\"center\"> Table Title </fo:block>\n"
                + "              </fo:table-cell>\n"
                + "            </fo:table-row>\n"
                + "          </fo:table-header>\n"
                + "          <fo:table-body>\n"
                + "            <fo:table-row>\n"
                + "              <fo:table-cell>\n"
                + "                <fo:block>Row 1 Column A</fo:block>\n"
                + "              </fo:table-cell>\n"
                + "              <fo:table-cell>\n"
                + "                <fo:block>Row 1 Column B</fo:block>\n"
                + "              </fo:table-cell>\n"
                + "            </fo:table-row>\n"
                + "            <fo:table-row>\n"
                + "              <fo:table-cell>\n"
                + "                <fo:block>Row 2 Column A</fo:block>\n"
                + "              </fo:table-cell>\n"
                + "              <fo:table-cell>\n"
                + "                <fo:block>Row 2 Column B</fo:block>\n"
                + "              </fo:table-cell>\n"
                + "            </fo:table-row>\n"
                + "            <fo:table-row>\n"
                + "              <fo:table-cell>\n"
                + "                <fo:block>Row 2 Column A</fo:block>\n"
                + "              </fo:table-cell>\n"
                + "              <fo:table-cell>\n"
                + "                <fo:block>Row 2 Column B</fo:block>\n"
                + "              </fo:table-cell>\n"
                + "            </fo:table-row>\n"
                + "            <fo:table-row>\n"
                + "              <fo:table-cell>\n"
                + "                <fo:block>Row 2 Column A</fo:block>\n"
                + "              </fo:table-cell>\n"
                + "              <fo:table-cell>\n"
                + "                <fo:block>Row 2 Column B</fo:block>\n"
                + "              </fo:table-cell>\n"
                + "            </fo:table-row>\n"
                + "          </fo:table-body>\n"
                + "        </fo:table>\n"
                + "      </fo:block>\n"
                + "    </fo:flow>\n"
                + "  </fo:page-sequence>\n"
                + "</fo:root>");

        int count = 0;
        for (PDFStructElem elem : elems) {
            if (elem.getStructureType().equals(StandardStructureTypes.Table.THEAD)) {
                count++;
            }
        }

        assertEquals("The static region per page conf must apply to static regions only", 1, count);
    }

    private List<PDFStructElem> getPDFStructElems(String fo) throws Exception {
        FopFactory fopFactory = getFopFactory();
        FOUserAgent userAgent = fopFactory.newFOUserAgent();
        foToOutput(fo, fopFactory, userAgent);

        PDFStructElem block = (PDFStructElem) userAgent
                .getStructureTreeEventHandler().startNode("block", new AttributesImpl(), null);

        return block.getDocument().getStructureTreeElements();
    }

    private ByteArrayOutputStream foToOutput(String fo) throws Exception {
        FopFactory fopFactory = getFopFactory();
        return foToOutput(fo, fopFactory, fopFactory.newFOUserAgent());
    }

    private ByteArrayOutputStream foToOutput(String fo, FopFactory fopFactory, FOUserAgent userAgent) throws Exception {
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
