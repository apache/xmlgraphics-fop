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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class PDFPageLabelsTestCase {

    @Test
    public void testAddPageLabel() throws IOException {
        PDFDocument pdfDoc = mock(PDFDocument.class);
        PDFPageLabels pageLabels = new PDFPageLabels();
        pageLabels.setDocument(pdfDoc);
        int index = 0;
        StringBuilder expected = new StringBuilder();
        expected.append("[");
        expected.append(index + " << /S /r >>");
        pageLabels.addPageLabel(index++, "i");
        pageLabels.addPageLabel(index++, "ii");
        pageLabels.addPageLabel(index++, "iii");
        expected.append(" " + index + " << /S /D >>");
        pageLabels.addPageLabel(index++, "1");
        pageLabels.addPageLabel(index++, "2");
        pageLabels.addPageLabel(index++, "3");
        pageLabels.addPageLabel(index++, "4");
        pageLabels.addPageLabel(index++, "5");
        pageLabels.addPageLabel(index++, "6");
        pageLabels.addPageLabel(index++, "7");
        pageLabels.addPageLabel(index++, "8");
        pageLabels.addPageLabel(index++, "9");
        pageLabels.addPageLabel(index++, "10");
        expected.append(" " + index + " << /S /A >>");
        pageLabels.addPageLabel(index++, "A");
        pageLabels.addPageLabel(index++, "B");
        expected.append(" " + index + " << /S /R /St 100 >>");
        pageLabels.addPageLabel(index++, "C");
        expected.append(" " + index + " << /S /R /St 500 >>");
        pageLabels.addPageLabel(index++, "D");
        expected.append(" " + index + " << /S /A /St 5 >>");
        pageLabels.addPageLabel(index++, "E");
        pageLabels.addPageLabel(index++, "F");
        pageLabels.addPageLabel(index++, "G");
        expected.append(" " + index + " << /P (aa) >>");
        pageLabels.addPageLabel(index++, "aa");
        expected.append(" " + index + " << /P (ab) >>");
        pageLabels.addPageLabel(index++, "ab");
        expected.append(" " + index + " << /P (ac) >>");
        pageLabels.addPageLabel(index++, "ac");
        expected.append(" " + index + " << /S /a >>");
        pageLabels.addPageLabel(index++, "a");
        pageLabels.addPageLabel(index++, "b");
        expected.append(" " + index + " << /S /R /St 2 >>");
        pageLabels.addPageLabel(index++, "II");
        expected.append(" " + index + " << /S /R /St 12 >>");
        pageLabels.addPageLabel(index++, "XII");
        expected.append(" " + index + " <<\n  /P (00)\n  /S /D\n  /St 9\n>>");
        pageLabels.addPageLabel(index++, "009");
        expected.append(" " + index + " <<\n  /P (0)\n  /S /D\n  /St 10\n>>");
        pageLabels.addPageLabel(index++, "010");
        pageLabels.addPageLabel(index++, "011");
        expected.append("]");

        PDFNumsArray nums = pageLabels.getNums();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        nums.output(baos);
        assertEquals(expected.toString(), baos.toString());
        baos.close();
    }

}
