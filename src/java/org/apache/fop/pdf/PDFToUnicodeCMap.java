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

/* Based on code from the FOray project, used with permission */
/* $Id: PDFObject.java 426576 2006-07-28 15:44:37Z jeremias $ */

package org.apache.fop.pdf;

import org.axsl.fontR.FontUse;
import org.axsl.fontR.output.FontPDF;

/**
 * Class representing ToUnicode CMaps.
 * Here are some documentation resources:
 * <ul>
 * <li>PDF Reference, Second Edition, Section 5.6.4, for general information
 * about CMaps in PDF Files.</li>
 * <li>PDF Reference, Second Edition, Section 5.9, for specific information
 * about ToUnicodeCMaps in PDF Files.</li>
 * <li>
 * <a href="http://partners.adobe.com/asn/developer/pdfs/tn/5411.ToUnicode.pdf">
 * Adobe Technical Note #5411, "ToUnicode Mapping File Tutorial"</a>.
 * </ul>
 */
public class PDFToUnicodeCMap extends PDFCMap {

    FontUse fsFont;

    /**
     * Constructor.
     *
     * @param name One of the registered names found in Table 5.14 in PDF
     * Reference, Second Edition.
     * @param sysInfo The attributes of the character collection of the CIDFont.
     */
    public PDFToUnicodeCMap(final PDFDocument doc, final String name,
            final PDFCIDSystemInfo sysInfo, final FontUse fsFont) {
        super(doc, name, sysInfo);
        this.fsFont = fsFont;
    }

    /** TODO remove this method, used for simulation only */
    public void DISABLED_fillInPDF(final StringBuffer p) {
        // Just a fixed simulated cmap to test the basic mechanism,
        // won't work correctly for many chars, of course
        final String simulatedIdentityCmap = 
        "/CIDInit /ProcSet findresource begin"
        +"\n12 dict begin"
        +"\nbegincmap"
        +"\n/CIDSystemInfo"
        +"\n<</Registry (Adobe)"
        +"\n/Ordering(UCS)"
        +"\n/Supplement 0"
        +"\n>>def"
        +"\n/CMapName /Adobe−Identity−UCS def"
        +"\n/CMapType 2 def"
        +"\n1 begincodespacerange"
        +"\n<0000> <FFFF>"
        +"\nendcodespacerange"
        +"\n2 beginbfrange"
        +"\n<000a> <0040> <0029>"
        +"\n<0043> <005f> <0062>"
        //+"\n<0000> <005E> <001F>"
        +"\nendbfrange"
        +"\nendcmap"
        +"\nCMapName currentdict /CMapdefineresource pop"
        +"\nend"
        +"\nend"
        ;
        add(simulatedIdentityCmap);
    }

    public void fillInPDF(final StringBuffer p) {
        writeCIDInit(p);
        writeCIDSystemInfo(p);
        writeVersionTypeName(p);
        writeCodeSpaceRange(p);
        final FontPDF fontPDF = (FontPDF) this.fsFont.getFontOutput(
                "application/pdf");
        final String bfEntries = fontPDF.getToUnicodeBF();
        p.append(bfEntries);
        writeBFEntries(p);
        writeWrapUp(p);
        add(p.toString());
    }

    protected void writeCIDSystemInfo(final StringBuffer p) {
        p.append("/CIDSystemInfo" + EOL);
        p.append("<< /Registry (Adobe)" + EOL);
        p.append("/Ordering (UCS)" + EOL);
        p.append("/Supplement 0" + EOL);
        p.append(">> def" + EOL);
    }

    protected void writeVersionTypeName(final StringBuffer p) {
        p.append("/CMapName /Adobe-Identity-UCS def" + EOL);
        p.append("/CMapType 2 def" + EOL);
    }

}
