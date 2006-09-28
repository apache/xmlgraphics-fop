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
/* $Id$ */

 
package org.apache.fop.pdf;

import java.io.IOException;
import java.io.OutputStream;

/**
 * class representing the CMap encodings.
 *
 * CMaps are defined on page 215 and onwards.
 * The predefined CMap names are drawn from Table 7.20
 * on pages 215, 216 and 217 .
 */
public class PDFCMap extends PDFStream {

    /**
     * Chinese (simplified)
     */
    public static final String GB_EUC_H = "GB-EUC-H";
    public static final String GB_EUC_V = "GB_EUC-V";
    public static final String GBPC_EUC_H = "GBpc-EUC-H";
    public static final String GBPC_EUC_V = "GBpc-EUC-V";
    public static final String GBK_EUC_H = "GBK-EUC-H";
    public static final String GBK_EUC_V = "GBK-EUC-V";
    public static final String UNIGB_UCS2_H = "UniGB-UCS2-H";
    public static final String UNIGB_UCS2_V = "UniGB-UCS2-V";

    /**
     * Chinese (traditional)
     */
    public static final String B5PC_H = "B5pc-H";
    public static final String B5PC_V = "B5pc-V";
    public static final String ETEN_B5_H = "ETen-B5-H";
    public static final String ETEN_B5_V = "ETen-B5-V";
    public static final String ETENMS_B5_H = "ETenms-B5-H";
    public static final String ETENMS_B5_V = "ETenms-B5-V";
    public static final String CNS_EUC_H = "CNS-EUC-H";
    public static final String CNS_EUC_V = "CNS-EUC-V";
    public static final String UNICNS_UCS2_H = "UniCNS-UCS2-H";
    public static final String UNICNS_UCS2_V = "UniCNS-UCS2-V";

    /**
     * Japanese
     */
    public static final String J83PV_RKSJ_H = "83pv-RKSJ-H";    // no V version
    public static final String J90MS_RKSJ_H = "90ms-RKSJ-H";
    public static final String J90MS_RKSJ_V = "90ms-RKSJ-V";
    public static final String J90MSP_RKSJ_H = "90msp-RKSJ-H";
    public static final String J90MSP_RKSJ_V = "90msp-RKSJ-V";
    public static final String J90PV_RKSJ_H = "90pv-RKSJ-H";    // no V version
    public static final String ADD_RKSJ_H = "Add-RKSJ-H";
    public static final String ADD_RKSJ_V = "Add-RKSJ-V";
    public static final String EUC_H = "EUC-H";
    public static final String EUC_V = "EUC-V";
    public static final String EXT_RKSJ_H = "Ext-RKSJ-H";
    public static final String EXT_RKSJ_V = "Ext-RKSJ-V";
    public static final String H = "H";
    public static final String V = "V";
    public static final String UNIJIS_UCS2_H = "UniJIS-UCS2-H";
    public static final String UNIJIS_UCS2_V = "UniJIS-UCS2-V";
    public static final String UNIJIS_UCS2_HW_H = "UniJIS-UCS2-HW-H";
    public static final String UNIJIS_UCS2_HW_V = "UniJIS-UCS2-HW-V";

    /**
     * Korean
     */
    public static final String KSC_EUC_H = "KSC-EUC-H";
    public static final String KSC_EUC_V = "KSC-EUC-V";
    public static final String KSCMS_UHC_H = "KSCms-UHC-H";
    public static final String KSCMS_UHC_V = "KSCms-UHC-V";
    public static final String KSCMS_UHC_HW_H = "KSCms-UHC-HW-H";
    public static final String KSCMS_UHC_HW_V = "KSCms-UHC-HW-V";
    public static final String KSCPC_EUC_H = "KSCpc-EUC-H";    // no V version
    public static final String UNIKSC_UCS2_H = "UniKSC-UCS2-H";
    public static final String UNIKSC_UCS2_V = "UniKSC-UCS2-V";

    /**
     * Generic
     */
    public static final String IDENTITY_H = "Identity-H";
    public static final String IDENTITY_V = "Identity-V";

    /**
     * horizontal writing direction
     */
    public static final byte WMODE_HORIZONTAL = 0;

    /**
     * vertical writing direction
     */
    public static final byte WMODE_VERTICAL = 1;

    /**
     * /CMapName attribute, one of the predefined constants
     */
    protected String name;

    /**
     * /CIDSystemInfo attribute
     */
    protected PDFCIDSystemInfo sysInfo;

    /**
     * font's writing direction
     */
    protected byte wMode = WMODE_HORIZONTAL;

    /**
     * base CMap (String or PDFStream)
     */
    protected Object base;

    /**
     * create the /CMap object
     *
     * @param name one the registered names (see Table 7.20 on p 215)
     * @param sysInfo the attributes of the character collection of the CIDFont
     */
    public PDFCMap(final PDFDocument doc, final String name,
            final PDFCIDSystemInfo sysInfo) {
        this.name = name;
        this.sysInfo = sysInfo;
        this.base = null;
    }

    /**
     * set the writing direction
     *
     * @param mode is either <code>WMODE_HORIZONTAL</code>
     * or <code>WMODE_VERTICAL</code>
     */
    public void setWMode(final byte mode) {
        this.wMode = mode;
    }

    /**
     * set the base CMap
     *
     * @param base the name of the base CMap (see Table 7.20)
     */
    public void setUseCMap(final String base) {
        this.base = base;
    }

    /**
     * set the base CMap
     *
     * @param base the stream to be used as base CMap
     */
    public void setUseCMap(final PDFStream base) {
        this.base = base;
    }

    protected int output(final OutputStream stream) throws IOException {
        fillInPDF(new StringBuffer());
        return super.output(stream);
    }

    public void fillInPDF(final StringBuffer p) {
        writePreStream(p);
        writeStreamComments(p);
        writeCIDInit(p);
        writeCIDSystemInfo(p);
        writeVersionTypeName(p);
        writeCodeSpaceRange(p);
        writeCIDRange(p);
        writeBFEntries(p);
        writeWrapUp(p);
        writeStreamAfterComments(p);
        writeUseCMap(p);
        add(p.toString());
    }

    protected void writePreStream(final StringBuffer p) {
        // p.append("/Type /CMap" + EOL);
        // p.append(sysInfo.toPDFString());
        // p.append("/CMapName /" + name + EOL);
    }

    protected void writeStreamComments(final StringBuffer p) {
        p.append("%!PS-Adobe-3.0 Resource-CMap" + EOL);
        p.append("%%DocumentNeededResources: ProcSet (CIDInit)" + EOL);
        p.append("%%IncludeResource: ProcSet (CIDInit)" + EOL);
        p.append("%%BeginResource: CMap (" + name + ")" + EOL);
        p.append("%%EndComments" + EOL);
    }

    protected void writeCIDInit(final StringBuffer p) {
        p.append("/CIDInit /ProcSet findresource begin" + EOL);
        p.append("12 dict begin" + EOL);
        p.append("begincmap" + EOL);
    }

    protected void writeCIDSystemInfo(final StringBuffer p) {
        p.append("/CIDSystemInfo 3 dict dup begin" + EOL);
        p.append("  /Registry (Adobe) def" + EOL);
        p.append("  /Ordering (Identity) def" + EOL);
        p.append("  /Supplement 0 def" + EOL);
        p.append("end def" + EOL);
    }

    protected void writeVersionTypeName(final StringBuffer p) {
        p.append("/CMapVersion 1 def" + EOL);
        p.append("/CMapType 1 def" + EOL);
        p.append("/CMapName /" + name + " def" + EOL);
    }

    protected void writeCodeSpaceRange(final StringBuffer p) {
        p.append("1 begincodespacerange" + EOL);
        p.append("<0000> <FFFF>" + EOL);
        p.append("endcodespacerange" + EOL);
    }

    protected void writeCIDRange(final StringBuffer p) {
        p.append("1 begincidrange" + EOL);
        p.append("<0000> <FFFF> 0" + EOL);
        p.append("endcidrange" + EOL);
    }

    protected void writeBFEntries(final StringBuffer p) {
        // p.append("1 beginbfrange" + EOL);
        // p.append("<0020> <0100> <0000>" + EOL);
        // p.append("endbfrange" + EOL);
    }

    protected void writeWrapUp(final StringBuffer p) {
        p.append("endcmap" + EOL);
        p.append("CMapName currentdict /CMap defineresource pop" + EOL);
        p.append("end" + EOL);
        p.append("end" + EOL);
    }

    protected void writeStreamAfterComments(final StringBuffer p) {
        p.append("%%EndResource" + EOL);
        p.append("%%EOF" + EOL);
    }

    protected void writeUseCMap(final StringBuffer p) {
        /*
         * p.append(" /Type /CMap");
         * p.append("/CMapName /" + name + EOL);
         * p.append("/WMode " + wMode + EOL);
         * if (base != null) {
         *     p.append("/UseCMap ");
         *     if (base instanceof String) {
         *         p.append("/"+base);
         *     } else { // base instanceof PDFStream
         *         p.append(((PDFStream)base).referencePDF());
         *     }
         * }
         */
    }

}
