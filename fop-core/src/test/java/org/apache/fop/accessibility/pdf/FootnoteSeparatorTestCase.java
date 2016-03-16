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

package org.apache.fop.accessibility.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;

import org.xml.sax.helpers.AttributesImpl;
import static org.junit.Assert.assertEquals;

import org.apache.fop.pdf.PDFArray;
import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.pdf.PDFFactory;
import org.apache.fop.pdf.PDFParentTree;
import org.apache.fop.pdf.PDFStructElem;
import org.apache.fop.pdf.PDFStructTreeRoot;
import org.apache.fop.render.pdf.PDFStructureTreeBuilder;

public class FootnoteSeparatorTestCase {

    @Test
    public void testFootNoteSeparatorText() throws IOException {
        PDFParentTree  tree = new  PDFParentTree();
        AttributesImpl attributes = new AttributesImpl();
        attributes.addAttribute("", "role", "role", "CDATA", null);
        PDFDocument doc = new PDFDocument("");
        PDFStructTreeRoot strucRoot =  doc.makeStructTreeRoot(tree);
        PDFFactory factory = new PDFFactory(doc);
        PDFStructElem part  = PDFStructureTreeBuilder.createStructureElement("page-sequence", strucRoot, attributes,
                factory, null);
        AttributesImpl att = new AttributesImpl();
        att.addAttribute("", "flow-name", "flow-name", "CDATA", "xsl-footnote-separator");
        PDFStructElem staticSection = PDFStructureTreeBuilder.createStructureElement("static-content", part, att,
                factory, null);
        PDFStructElem block = PDFStructureTreeBuilder.createStructureElement("block", part, new AttributesImpl(),
                factory, null);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        part.output(bos);
        PDFArray array  = (PDFArray)part.get("K");
        PDFStructElem elem1 = (PDFStructElem)array.get(0);
        String test = elem1.getStructureType().getName().getName();
        String expected = "P";
        assertEquals(test, expected);
        PDFStructElem  elem2 =  (PDFStructElem)array.get(1);
        test = elem2.getStructureType().getName().getName();
        expected = "Sect";
        assertEquals(test, expected);
    }
}
