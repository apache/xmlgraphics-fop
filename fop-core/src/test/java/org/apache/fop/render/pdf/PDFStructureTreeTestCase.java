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
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
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

import org.apache.fop.accessibility.StructureTreeElement;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.pdf.PDFLinearizationTestCase;
import org.apache.fop.pdf.PDFStructElem;
import org.apache.fop.pdf.StandardStructureTypes;
import org.apache.fop.pdf.StructureType;
import static org.apache.fop.pdf.StandardStructureTypes.Grouping.DIV;
import static org.apache.fop.pdf.StandardStructureTypes.Grouping.DOCUMENT;
import static org.apache.fop.pdf.StandardStructureTypes.Grouping.PART;
import static org.apache.fop.pdf.StandardStructureTypes.Grouping.SECT;
import static org.apache.fop.pdf.StandardStructureTypes.Paragraphlike.P;
import static org.apache.fop.pdf.StandardStructureTypes.Table.TABLE;
import static org.apache.fop.pdf.StandardStructureTypes.Table.TBODY;
import static org.apache.fop.pdf.StandardStructureTypes.Table.TD;
import static org.apache.fop.pdf.StandardStructureTypes.Table.TFOOT;
import static org.apache.fop.pdf.StandardStructureTypes.Table.TH;
import static org.apache.fop.pdf.StandardStructureTypes.Table.THEAD;
import static org.apache.fop.pdf.StandardStructureTypes.Table.TR;

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
    public void testReadingOrder() throws Exception {
        checkReadingOrder(Arrays.asList(DOCUMENT, PART, SECT, DIV, P, P, DIV, P, DIV, P, P, DIV, P),
                "test/fo/reading_order.fo");
    }

    @Test
    public void testReadingOrderWithTableInHeader() throws Exception {
        checkReadingOrder(Arrays.asList(DOCUMENT, PART, SECT, DIV, P, TABLE, THEAD, TR, TH, P, TBODY, TR, TD, P, TFOOT,
                        TR, TD, P, P, DIV, P, DIV, P, TABLE, THEAD, TR, TH, P, TBODY, TR, TD, P, TFOOT,
                        TR, TD, P, P, DIV, P),
                "test/fo/reading_order_table_in_header.fo");
    }

    @Test
    public void testReadingOrderBlockSpannedOverPage() throws Exception {
        checkReadingOrder(Arrays.asList(DOCUMENT, PART, SECT, DIV, P, P, DIV, P, DIV, P, P, DIV, P, DIV, P, P, DIV, P),
                "test/fo/reading_order_block_spanned_over_page.fo");
    }

    @Test
    public void testReadingOrderTableInBody() throws Exception {
        checkReadingOrder(Arrays.asList(DOCUMENT, PART, SECT, DIV, P, P, TABLE, THEAD, TR, TH, P, TBODY, TR, TD, P, TD,
                        P, TFOOT, TR, TD, P, DIV, P, DIV, P, P, TABLE, THEAD, TR, TH, P, TBODY, TR, TD, P, TD, P, TFOOT,
                        TR, TD, P, DIV, P),
                "test/fo/reading_order_table_in_body.fo");
    }

    private void checkReadingOrder(List<StructureType> orderedTypes,  String filePath) throws Exception {
        List<PDFStructElem> elems = getPDFStructElems(filePath, true);

        int index = 0;
        for (PDFStructElem elem : elems) {
            assertEquals("Reading order must be preserved when static-region-per-page is true",
                    orderedTypes.get(index), elem.getStructureType());
            index++;
        }

        assertEquals("Must verify all the PDFStructElements", orderedTypes.size(), elems.size());
    }

    @Test
    public void testTableDuplicatedIfStaticRegionsPerPageTrue() throws Exception {
        checkTableBodyCount(true, "test/fo/reading_order_table_in_body.fo",
                "A table element should only have one respective structure element", 2);
    }

    @Test
    public void testTableBodyNotDuplicatedIfStaticRegionsPerPageFalse() throws Exception {
        checkTableBodyCount(false, "test/fo/reading_order_table_in_body.fo",
                "A table element should only have one respective structure element", 1);
    }

    @Test
    public void testTableBodyDuplicatedIfInsideStaticContent() throws Exception {
        checkTableBodyCount(true, "test/fo/reading_order_table_in_header.fo",
                "If the conf is set to true, a table element must be duplicated like any other fo element", 2);
    }

    private void checkTableBodyCount(boolean staticRegionPerPage, String filePath, String assertionMessage,
                                     int expectedCount) throws Exception {
        List<PDFStructElem> elems = getPDFStructElems(filePath, staticRegionPerPage);

        int count = countTableElements(elems, TABLE);
        assertEquals(assertionMessage, expectedCount, count);

        count = countTableElements(elems, StandardStructureTypes.Table.TBODY);
        assertEquals(assertionMessage, expectedCount, count);

        count = countTableElements(elems, THEAD);
        assertEquals(assertionMessage, expectedCount, count);

        count = countTableElements(elems, StandardStructureTypes.Table.TFOOT);
        assertEquals(assertionMessage, expectedCount, count);
    }

    private int countTableElements(List<PDFStructElem> elems, StructureType elementType) {
        int count = 0;
        for (PDFStructElem elem : elems) {
            if (elem.getStructureType().equals(elementType)) {
                count++;
            }
        }

        return count;
    }

    private List<PDFStructElem> getPDFStructElems(String foFileName, boolean staticRegionPerPage) throws Exception {
        FopFactory fopFactory = getFopFactory(staticRegionPerPage, true);
            FOUserAgent userAgent = fopFactory.newFOUserAgent();
        foToOutput(new FileInputStream(foFileName), fopFactory, userAgent);

        StructureTreeElement block = userAgent.getStructureTreeEventHandler()
                .startNode("#PCDATA", new AttributesImpl(), null);

        PDFStructElem blockElem;
        if (block instanceof PDFStructElem) {
            blockElem = (PDFStructElem) block;
        } else {
            blockElem = ((PDFStructureTreeBuilder.Factory) block).createStructureElement(1);
        }

        return blockElem.getDocument().getStructureTreeElements();
    }

    private ByteArrayOutputStream foToOutput(String fo) throws Exception {
        FopFactory fopFactory = getFopFactory(true, false);
        return foToOutput(new ByteArrayInputStream(fo.getBytes()), fopFactory, fopFactory.newFOUserAgent());
    }


    private ByteArrayOutputStream foToOutput(InputStream inputStream, FopFactory fopFactory, FOUserAgent userAgent)
            throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Fop fop = fopFactory.newFop("application/pdf", userAgent, bos);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();

        Source src = new StreamSource(inputStream);
        Result res = new SAXResult(fop.getDefaultHandler());
        transformer.transform(src, res);
        return bos;
    }

    private FopFactory getFopFactory(boolean staticRegionPerPage, boolean useObjectsStreams) throws Exception {
        String fopxconf = "<fop version=\"1.0\">"
                + "     <accessibility static-region-per-page=\"" + staticRegionPerPage + "\">true</accessibility>"
                + "         <renderers>\n"
                + "             <renderer mime=\"application/pdf\">\n"
                + "                   <use-object-streams>" + useObjectsStreams + "</use-object-streams>"
                + "             </renderer>\n"
                + "         </renderers>\n"
                + "</fop>";
        return FopFactory.newInstance(new File(".").toURI(), new ByteArrayInputStream(fopxconf.getBytes()));
    }
}
