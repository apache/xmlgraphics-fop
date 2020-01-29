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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.pdf.PDFFactory;
import org.apache.fop.pdf.PDFResources;
import org.apache.fop.pdf.PDFStructElem;
import org.apache.fop.pdf.StandardStructureTypes;

public class PDFStructureTreeBuilderTestCase {
    private PDFFactory pdfFactory;

    @Test
    public void testAddImageContentItem() throws IOException {
        PDFStructElem structElem = new PDFStructElem(null, StandardStructureTypes.Illustration.FIGURE);
        structElem.setDocument(pdfFactory.getDocument());
        PDFLogicalStructureHandler logicalStructureHandler = new PDFLogicalStructureHandler(null);
        logicalStructureHandler.startPage(pdfFactory.makePage(new PDFResources(pdfFactory.getDocument()), 0, 0));
        logicalStructureHandler.addImageContentItem(structElem);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        structElem.output(bos);
        assertEquals(bos.toString(), "<< /S /Figure /K [<<\n"
                + "  /Type /MCR\n"
                + "  /Pg 1 0 R\n"
                + "  /MCID 0\n"
                + ">>] >>");
    }

    @Before
    public void setUp() {
        PDFDocument doc = new PDFDocument("");
        pdfFactory = new PDFFactory(doc);
    }
}
