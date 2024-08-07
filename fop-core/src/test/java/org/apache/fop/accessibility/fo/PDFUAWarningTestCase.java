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

package org.apache.fop.accessibility.fo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.xml.sax.helpers.AttributesImpl;

import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.pdf.PDFFactory;
import org.apache.fop.pdf.PDFParentTree;
import org.apache.fop.pdf.PDFStructElem;
import org.apache.fop.pdf.PDFUAMode;
import org.apache.fop.render.pdf.PDFStructureTreeBuilder;

public class PDFUAWarningTestCase {

    private PDFFactory pdfFactory;

    @Before
    public void setUp() {
        PDFParentTree tree = new  PDFParentTree();
        PDFDocument doc = new PDFDocument("");
        doc.makeStructTreeRoot(tree);
        doc.getProfile().setPDFUAMode(PDFUAMode.PDFUA_1);
        pdfFactory = new PDFFactory(doc);
    }

    @Test
    public void nestedTableWarningTestCase() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PDFStructElem emptyElem = new PDFStructElem();
        emptyElem.setDocument(pdfFactory.getDocument());
        PDFStructElem block  = PDFStructureTreeBuilder.createStructureElement("block", emptyElem,
                new AttributesImpl(), pdfFactory, null);
        PDFStructElem table =
                PDFStructureTreeBuilder.createStructureElement("table", block, new AttributesImpl(), pdfFactory, null);
        pdfFactory.getDocument().assignObjectNumber(table);
        block.output(bos);
        Assert.assertEquals("Div", block.getStructureType().toString());
    }

    @Test
    public void testNoStructureType() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PDFStructElem structElem = new PDFStructElem();
        structElem.setDocument(pdfFactory.getDocument());
        PDFStructElem structElem2 = new PDFStructElem();
        structElem2.setDocument(pdfFactory.getDocument());
        structElem.addKid(structElem2);
        structElem.output(bos);
        Assert.assertEquals(bos.toString(), "<< /K [<< >>] >>");
    }
}
