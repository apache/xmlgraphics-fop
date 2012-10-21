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

import static org.junit.Assert.assertEquals;

import java.io.StringWriter;

import org.apache.fop.pdf.CMapBuilder;
import org.junit.Test;

/** Simple sanity test of the PDFCmap class */
public class PDFCMapTestCase {
    private static final String EOL = "\n";

    @Test
    public void testPDFCMapFillInPDF() throws Exception {
        final String expected
            = "%!PS-Adobe-3.0 Resource-CMap" + EOL
            + "%%DocumentNeededResources: ProcSet (CIDInit)" + EOL
            + "%%IncludeResource: ProcSet (CIDInit)" + EOL
            + "%%BeginResource: CMap (test)" + EOL
            + "%%EndComments" + EOL
            + "/CIDInit /ProcSet findresource begin" + EOL
            + "12 dict begin" + EOL
            + "begincmap" + EOL
            + "/CIDSystemInfo 3 dict dup begin" + EOL
            + "  /Registry (Adobe) def" + EOL
            + "  /Ordering (Identity) def" + EOL
            + "  /Supplement 0 def" + EOL
            + "end def" + EOL
            + "/CMapVersion 1 def" + EOL
            + "/CMapType 1 def" + EOL
            + "/CMapName /test def" + EOL
            + "1 begincodespacerange" + EOL
            + "<0000> <FFFF>" + EOL
            + "endcodespacerange" + EOL
            + "1 begincidrange" + EOL
            + "<0000> <FFFF> 0" + EOL
            + "endcidrange" + EOL
            + "endcmap" + EOL
            + "CMapName currentdict /CMap defineresource pop" + EOL
            + "end" + EOL
            + "end" + EOL
            + "%%EndResource" + EOL
            + "%%EOF" + EOL
        ;

        final StringWriter w = new StringWriter();
        final CMapBuilder builder = new CMapBuilder(w, "test");
        builder.writeCMap();
        final String actual = w.getBuffer().toString();
        assertEquals("PDFCMap output matches expected PostScript code", expected, actual);
    }

}
