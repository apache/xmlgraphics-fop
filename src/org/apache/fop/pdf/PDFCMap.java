/*
 * $Id$
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 * 
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */ 
package org.apache.fop.pdf;

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
    public static final String GB_EUC_V = "GB_EUC_V";
    public static final String GBpc_EUC_H = "GBpc-EUC-H";
    public static final String GBpc_EUC_V = "GBpc-EUC-V";
    public static final String GBK_EUC_H = "GBK-EUC-H";
    public static final String GBK_EUC_V = "GBK-EUC-V";
    public static final String UniGB_UCS2_H = "UniGB-UCS2-H";
    public static final String UniGB_UCS2_V = "UniGB-UCS2-V";

    /**
     * Chinese (traditional)
     */
    public static final String B5pc_H = "B5pc-H";
    public static final String B5pc_V = "B5pc-V";
    public static final String ETen_B5_H = "ETen-B5-H";
    public static final String ETen_B5_V = "ETen-B5-V";
    public static final String ETenms_B5_H = "ETenms-B5-H";
    public static final String ETenms_B5_V = "ETenms-B5-V";
    public static final String CNS_EUC_H = "CNS-EUC-H";
    public static final String CNS_EUC_V = "CNS-EUC-V";
    public static final String UniCNS_UCS2_H = "UniCNS-UCS2-H";
    public static final String UniCNS_UCS2_V = "UniCNS-UCS2-V";

    /**
     * Japanese
     */
    public static final String _83pv_RKSJ_H = "83pv-RKSJ-H";    // no V version
    public static final String _90ms_RKSJ_H = "90ms-RKSJ-H";
    public static final String _90ms_RKSJ_V = "90ms-RKSJ-V";
    public static final String _90msp_RKSJ_H = "90msp-RKSJ-H";
    public static final String _90msp_RKSJ_V = "90msp-RKSJ-V";
    public static final String _90pv_RKSJ_H = "90pv-RKSJ-H";    // no V version
    public static final String Add_RKSJ_H = "Add-RKSJ-H";
    public static final String Add_RKSJ_V = "Add-RKSJ-V";
    public static final String EUC_H = "EUC-H";
    public static final String EUC_V = "EUC-V";
    public static final String Ext_RKSJ_H = "Ext-RKSJ-H";
    public static final String Ext_RKSJ_V = "Ext-RKSJ-V";
    public static final String H = "H";
    public static final String V = "V";
    public static final String UniJIS_UCS2_H = "UniJIS-UCS2-H";
    public static final String UniJIS_UCS2_V = "UniJIS-UCS2-V";
    public static final String UniJIS_UCS2_HW_H = "UniJIS-UCS2-HW-H";
    public static final String UniJIS_UCS2_HW_V = "UniJIS-UCS2-HW-V";

    /**
     * Korean
     */
    public static final String KSC_EUC_H = "KSC-EUC-H";
    public static final String KSC_EUC_V = "KSC-EUC-V";
    public static final String KSCms_UHC_H = "KSCms-UHC-H";
    public static final String KSCms_UHC_V = "KSCms-UHC-V";
    public static final String KSCms_UHC_HW_H = "KSCms-UHC-HW-H";
    public static final String KSCms_UHC_HW_V = "KSCms-UHC-HW-V";
    public static final String KSCpc_EUC_H = "KSCpc-EUC-H";    // no V version
    public static final String UniKSC_UCS2_H = "UniKSC-UCS2-H";
    public static final String UniKSC_UCS2_V = "UniKSC-UCS2-V";

    /**
     * Generic
     */
    public static final String Identity_H = "Identity-H";
    public static final String Identity_V = "Identity-V";

    /**
     * /CMapName attribute, one of the predefined constants
     */
    protected String name;

    /**
     * /CIDSystemInfo attribute
     */
    protected PDFCIDSystemInfo sysInfo;

    /**
     * horizontal writing direction
     */
    public static final byte WMODE_HORIZONTAL = 0;

    /**
     * vertical writing direction
     */
    public static final byte WMODE_VERTICAL = 1;

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
    public PDFCMap(int number, String name, PDFCIDSystemInfo sysInfo) {
        super(number);
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
    public void setWMode(byte mode) {
        this.wMode = mode;
    }

    public void addContents() {
        StringBuffer p = new StringBuffer();
        fillInPDF(p);
        add(p.toString());
    }

    /**
     * set the base CMap
     *
     * @param base the name of the base CMap (see Table 7.20)
     */
    public void setUseCMap(String base) {
        this.base = base;
    }

    /**
     * set the base CMap
     *
     * @param base the stream to be used as base CMap
     */
    public void setUseCMap(PDFStream base) {
        this.base = base;
    }

    public void fillInPDF(StringBuffer p) {
        // p.append("/Type /CMap\n");
        // p.append(sysInfo.toPDFString());
        // p.append("/CMapName /" + name);
        // p.append("\n");
        p.append("%!PS-Adobe-3.0 Resource-CMap\n");
        p.append("%%DocumentNeededResources: ProcSet (CIDInit)\n");
        p.append("%%IncludeResource: ProcSet (CIDInit)\n");
        p.append("%%BeginResource: CMap (" + name + ")\n");
        p.append("%%EndComments\n");

        p.append("/CIDInit /ProcSet findresource begin\n");
        p.append("12 dict begin\n");
        p.append("begincmap\n");

        p.append("/CIDSystemInfo 3 dict dup begin\n");
        p.append("  /Registry (Adobe) def\n");
        p.append("  /Ordering (Identity) def\n");
        p.append("  /Supplement 0 def\n");
        p.append("end def\n");

        p.append("/CMapVersion 1 def\n");
        p.append("/CMapType 1 def\n");
        p.append("/CMapName /" + name + " def\n");

        p.append("1 begincodespacerange\n");
        p.append("<0000> <FFFF>\n");
        p.append("endcodespacerange\n");
        p.append("1 begincidrange\n");
        p.append("<0000> <FFFF> 0\n");
        p.append("endcidrange\n");

        // p.append("1 beginbfrange\n");
        // p.append("<0020> <0100> <0000>\n");
        // p.append("endbfrange\n");

        p.append("endcmap\n");
        p.append("CMapName currentdict /CMap defineresource pop\n");
        p.append("end\n");
        p.append("end\n");
        p.append("%%EndResource\n");
        p.append("%%EOF\n");
        /*
         * p.append(" /Type /CMap\n/CMapName /" + name);
         * p.append("\n");
         * p.append("\n/WMode "); p.append(wMode);
         * if (base != null) {
         * p.append("\n/UseCMap ");
         * if (base instanceof String) {
         * p.append("/"+base);
         * } else { // base instanceof PDFStream
         * p.append(((PDFStream)base).referencePDF());
         * }
         * }
         */
    }

}
