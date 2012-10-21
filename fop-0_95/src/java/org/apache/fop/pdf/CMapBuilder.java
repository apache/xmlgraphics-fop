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

import java.io.IOException;
import java.io.Writer;

public class CMapBuilder {
    
    protected String name;
    protected Writer writer;
    
    public CMapBuilder(Writer writer, String name) {
        this.writer = writer;
        this.name = name;
    }
    
    /**
     * Writes the CMap to a Writer.
     * @throws IOException if an I/O error occurs
     */
    public void writeCMap() throws IOException {
        writePreStream();
        writeStreamComments();
        writeCIDInit();
        writeCIDSystemInfo();
        writeVersion("1");
        writeType("1");
        writeName(name);
        writeCodeSpaceRange();
        writeCIDRange();
        writeBFEntries();
        writeWrapUp();
        writeStreamAfterComments();
        writeUseCMap();
    }
    
    protected void writePreStream() throws IOException {
        // writer.write("/Type /CMap\n");
        // writer.write(sysInfo.toPDFString());
        // writer.write("/CMapName /" + name + EOL);
    }

    protected void writeStreamComments() throws IOException {
        writer.write("%!PS-Adobe-3.0 Resource-CMap\n");
        writer.write("%%DocumentNeededResources: ProcSet (CIDInit)\n");
        writer.write("%%IncludeResource: ProcSet (CIDInit)\n");
        writer.write("%%BeginResource: CMap (" + name + ")\n");
        writer.write("%%EndComments\n");
    }

    protected void writeCIDInit() throws IOException {
        writer.write("/CIDInit /ProcSet findresource begin\n");
        writer.write("12 dict begin\n");
        writer.write("begincmap\n");
    }

    protected void writeCIDSystemInfo(String registry, String ordering, int supplement)
                throws IOException {
        writer.write("/CIDSystemInfo 3 dict dup begin\n");
        writer.write("  /Registry (");
        writer.write(registry);
        writer.write(") def\n");
        writer.write("  /Ordering (");
        writer.write(ordering);
        writer.write(") def\n");
        writer.write("  /Supplement ");
        writer.write(Integer.toString(supplement));
        writer.write(" def\n");
        writer.write("end def\n");
    }
    
    protected void writeCIDSystemInfo() throws IOException {
        writeCIDSystemInfo("Adobe", "Identity", 0);
    }

    protected void writeVersion(String version) throws IOException {
        writer.write("/CMapVersion ");
        writer.write(version);
        writer.write(" def\n");
    }

    protected void writeType(String type) throws IOException {
        writer.write("/CMapType ");
        writer.write(type);
        writer.write(" def\n");
    }

    protected void writeName(String name) throws IOException {
        writer.write("/CMapName /");
        writer.write(name);
        writer.write(" def\n");
    }

    protected void writeCodeSpaceRange() throws IOException {
        writer.write("1 begincodespacerange\n");
        writer.write("<0000> <FFFF>\n");
        writer.write("endcodespacerange\n");
    }

    protected void writeCIDRange() throws IOException {
        writer.write("1 begincidrange\n");
        writer.write("<0000> <FFFF> 0\n");
        writer.write("endcidrange\n");
    }

    protected void writeBFEntries() throws IOException {
        // writer.write("1 beginbfrange\n");
        // writer.write("<0020> <0100> <0000>\n");
        // writer.write("endbfrange\n");
    }

    protected void writeWrapUp() throws IOException {
        writer.write("endcmap\n");
        writer.write("CMapName currentdict /CMap defineresource pop\n");
        writer.write("end\n");
        writer.write("end\n");
    }

    protected void writeStreamAfterComments() throws IOException {
        writer.write("%%EndResource\n");
        writer.write("%%EOF\n");
    }

    protected void writeUseCMap() {
        /*
         * writer.write(" /Type /CMap");
         * writer.write("/CMapName /" + name + EOL);
         * writer.write("/WMode " + wMode + EOL);
         * if (base != null) {
         *     writer.write("/UseCMap ");
         * if (base instanceof String) {
         * writer.write("/"+base);
         * } else {// base instanceof PDFStream
         * writer.write(((PDFStream)base).referencePDF());
         * }
         * }
         */
    }
}