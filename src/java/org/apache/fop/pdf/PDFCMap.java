/*
 * $Id: PDFCMap.java,v 1.6 2003/03/07 08:25:47 jeremias Exp $
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
 * Class representing the CMap encodings.
 *
 * CMaps are defined in the "Predefined CJK CMap names" table.
 * In section 5.6.4 of PDF reference 1.4.
 */
public class PDFCMap extends PDFStream {

    /*
     * Chinese (simplified)
     */

    /**
     * GB-EUC-H Microsoft Code Page 936 (lfCharSet 0x86), GB 2312-80
     * character set, EUC-CN encoding
     */
    public static final String ENC_GB_EUC_H = "GB-EUC-H";

    /**
     * GB-EUC-V Vertical version of GB-EUC-H
     */
    public static final String ENC_GB_EUC_V = "GB_EUC_V";

    /**
     * GBpc-EUC-H Mac OS, GB 2312-80 character set, EUC-CN encoding, Script Manager code 19
     */
    public static final String ENC_GBPC_EUC_H = "GBpc-EUC-H";

    /**
     * GBpc-EUC-V Vertical version of GBpc-EUC-H
     */
    public static final String ENC_GBPC_EUC_V = "GBpc-EUC-V";

    /**
     * GBK-EUC-H Microsoft Code Page 936 (lfCharSet 0x86), GBK character set, GBK encoding
     */
    public static final String ENC_GBK_EUC_H = "GBK-EUC-H";

    /**
     * GBK-EUC-V Vertical version of GBK-EUC-H
     */
    public static final String ENC_GBK_EUC_V = "GBK-EUC-V";

    /**
     * GBKp-EUC-H Same as GBK-EUC-H, but replaces half-width
     * Latin characters with proportional forms and maps character
     * code 0x24 to a dollar sign ($) instead of a yuan symbol
     */
    public static final String ENC_GBKP_EUC_H = "GBKp-EUC-H";

    /**
     * GBKp-EUC-V Vertical version of GBKp-EUC-H
     */
    public static final String ENC_GBKP_EUC_V = "GBKp-EUC-V";

    /**
     * GBK2K-H GB 18030-2000 character set, mixed 1-, 2-, and 4-byte encoding
     */
    public static final String ENC_GBK2K_H = "GBK2K-H";

    /**
     * GBK2K-V Vertical version of GBK2K-H
     */
    public static final String ENC_GBK2K_V = "GBK2K-V";

    /**
     * UniGB-UCS2-H Unicode (UCS-2) encoding for the Adobe-GB1 character collection
     */
    public static final String ENC_UNIGB_UCS2_H = "UniGB-UCS2-H";

    /**
     * UniGB-UCS2-V Vertical version of UniGB-UCS2-H
     */
    public static final String ENC_UNIGB_UCS2_V = "UniGB-UCS2-V";


    /*
     * Chinese (Traditional)
     */

    /**
     * B5pc-H Mac OS, Big Five character set, Big Five encoding, Script Manager code 2
     */
    public static final String ENC_B5PC_H = "B5pc-H";

    /**
     * B5pc-V Vertical version of B5pc-H
     */
    public static final String ENC_B5PC_V = "B5pc-V";

    /**
     * HKscs-B5-H Hong Kong SCS, an extension to the Big Five
     * character set and encoding
     */
    public static final String ENC_HKSCS_B5_H = "HKscs-B5-H";

    /**
     * HKscs-B5-V Vertical version of HKscs-B5-H
     */
    public static final String ENC_HKSCS_B5_V = "HKscs-B5-V";

    /**
     * ETen-B5-H Microsoft Code Page 950 (lfCharSet 0x88), Big Five
     * character set with ETen extensions
     */
    public static final String ENC_ETEN_B5_H = "ETen-B5-H";

    /**
     * ETen-B5-V Vertical version of ETen-B5-H
     */
    public static final String ENC_ETEN_B5_V = "ETen-B5-V";

    /**
     * ETenms-B5-H Same as ETen-B5-H, but replaces half-width
     * Latin characters with proportional forms
     */
    public static final String ENC_ETENMS_B5_H = "ETenms-B5-H";

    /**
     * ETenms-B5-V Vertical version of ETenms-B5-H
     */
    public static final String ENC_ETENMS_B5_V = "ETenms-B5-V";

    /**
     * CNS-EUC-H CNS 11643-1992 character set, EUC-TW encoding
     */
    public static final String ENC_CNS_EUC_H = "CNS-EUC-H";

    /**
     * CNS-EUC-V Vertical version of CNS-EUC-H
     */
    public static final String ENC_CNS_EUC_V = "CNS-EUC-V";

    /**
     * UniCNS-UCS2-H Unicode (UCS-2) encoding for the
     * Adobe-CNS1 character collection
     */
    public static final String ENC_UNICNS_UCS2_H = "UniCNS-UCS2-H";

    /**
     * UniCNS-UCS2-V Vertical version of UniCNS-UCS2-H
     */
    public static final String ENC_UNICNS_UCS2_V = "UniCNS-UCS2-V";

    /*
     * Japanese
     */

    /**
     * 83pv-RKSJ-H Mac OS, JIS X 0208 character set with KanjiTalk6
     * extensions, Shift-JIS encoding, Script Manager code 1
     */
    public static final String ENC_83PV_RKSJ_H = "83pv-RKSJ-H";    // no V version

    /**
     * 90ms-RKSJ-H Microsoft Code Page 932 (lfCharSet 0x80), JIS X 0208
     * character set with NEC and IBM extensions
     */
    public static final String ENC_90MS_RKSJ_H = "90ms-RKSJ-H";

    /**
     * 90ms-RKSJ-V Vertical version of 90ms-RKSJ-H
     */
    public static final String ENC_90MS_RKSJ_V = "90ms-RKSJ-V";

    /**
     * 90msp-RKSJ-H Same as 90ms-RKSJ-H, but replaces half-width Latin
     * characters with proportional forms
     */
    public static final String ENC_90MSP_RKSJ_H = "90msp-RKSJ-H";

    /**
     * 90msp-RKSJ-V Vertical version of 90msp-RKSJ-H
     */
    public static final String ENC_90MSP_RKSJ_V = "90msp-RKSJ-V";

    /**
     * 90pv-RKSJ-H Mac OS, JIS X 0208 character set with KanjiTalk7
     * extensions, Shift-JIS encoding, Script Manager code 1
     */
    public static final String ENC_90PV_RKSJ_H = "90pv-RKSJ-H";    // no V version

    /**
     * Add-RKSJ-H JIS X 0208 character set with Fujitsu FMR
     * extensions, Shift-JIS encoding
     */
    public static final String ENC_ADD_RKSJ_H = "Add-RKSJ-H";

    /**
     * Add-RKSJ-V Vertical version of Add-RKSJ-H
     */
    public static final String ENC_ADD_RKSJ_V = "Add-RKSJ-V";

    /**
     * EUC-H JIS X 0208 character set, EUC-JP encoding
     */
    public static final String ENC_EUC_H = "EUC-H";

    /**
     * EUC-V Vertical version of EUC-H
     */
    public static final String ENC_EUC_V = "EUC-V";

    /**
     * Ext-RKSJ-H JIS C 6226 (JIS78) character set with
     * NEC extensions, Shift-JIS encoding
     */
    public static final String ENC_EXT_RKSJ_H = "Ext-RKSJ-H";

    /**
     * Ext-RKSJ-V Vertical version of Ext-RKSJ-H
     */
    public static final String ENC_EXT_RKSJ_V = "Ext-RKSJ-V";

    /**
     * H JIS X 0208 character set, ISO-2022-JP encoding
     */
    public static final String ENC_H = "H";

    /**
     * V Vertical version of H
     */
    public static final String ENC_V = "V";

    /**
     * UniJIS-UCS2-H Unicode (UCS-2) encoding for the
     * Adobe-Japan1 character collection
     */
    public static final String ENC_UNIJIS_UCS2_H = "UniJIS-UCS2-H";

    /**
     * UniJIS-UCS2-V Vertical version of UniJIS-UCS2-H
     */
    public static final String ENC_UNIJIS_UCS2_V = "UniJIS-UCS2-V";

    /**
     * UniJIS-UCS2-HW-H Same as UniJIS-UCS2-H, but replaces proportional
     * Latin characters with half-width forms
     */
    public static final String ENC_UNIJIS_UCS2_HW_H = "UniJIS-UCS2-HW-H";

    /**
     * UniJIS-UCS2-HW-V Vertical version of UniJIS-UCS2-HW-H
     */
    public static final String ENC_UNIJIS_UCS2_HW_V = "UniJIS-UCS2-HW-V";

    /*
     * Korean
     */

    /**
     * KSC-EUC-H KS X 1001:1992 character set, EUC-KR encoding
     */
    public static final String ENC_KSC_EUC_H = "KSC-EUC-H";

    /**
     * KSC-EUC-V Vertical version of KSC-EUC-H
     */
    public static final String ENC_KSC_EUC_V = "KSC-EUC-V";

    /**
     * KSCms-UHC-H Microsoft Code Page 949 (lfCharSet 0x81), KS X 1001:1992
     * character set plus 8822 additional hangul,
     * Unified Hangul Code (UHC) encoding
     */
    public static final String ENC_KSCMS_UHC_H = "KSCms-UHC-H";

    /**
     * KSCms-UHC-V Vertical version of KSCms-UHC-H
     */
    public static final String ENC_KSCMS_UHC_V = "KSCms-UHC-V";

    /**
     * KSCms-UHC-HW-H Same as KSCms-UHC-H, but replaces proportional
     * Latin characters with half-width forms
     */
    public static final String ENC_KSCMS_UHC_HW_H = "KSCms-UHC-HW-H";

    /**
     * KSCms-UHC-HW-V Vertical version of KSCms-UHC-HW-H
     */
    public static final String ENC_KSCMS_UHC_HW_V = "KSCms-UHC-HW-V";

    /**
     * KSCpc-EUC-H Mac OS, KS X 1001:1992 character set with
     * Mac OS KH extensions, Script Manager Code 3
     */
    public static final String ENC_KSCPC_EUC_H = "KSCpc-EUC-H";    // no V version

    /**
     * UniKS-UCS2-H Unicode (UCS-2) encoding for the
     * Adobe-Korea1 character collection
     */
    public static final String ENC_UNIKSC_UCS2_H = "UniKSC-UCS2-H";

    /**
     * UniKS-UCS2-V Vertical version of UniKS-UCS2-H
     */
    public static final String ENC_UNIKSC_UCS2_V = "UniKSC-UCS2-V";

    /*
     * Generic
     */

    /**
     * Identity-H The horizontal identity mapping for 2-byte CIDs;
     * may be used with CIDFonts using any Registry, Ordering, and
     * Supplement values. It maps 2-byte character codes ranging from
     * 0 to 65,535 to the same 2-byte CID value, interpreted
     * high-order byte first.
     */
    public static final String ENC_IDENTITY_H = "Identity-H";

    /**
     * Identity-V Vertical version of Identity-H. The mapping
     * is the same as for Identity-H.
     */
    public static final String ENC_IDENTTITY_V = "Identity-V";

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
     * @param number the pdf object number
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

    /**
     * Add the contents of this pdf object to the PDF stream.
     */
    public void addContents() {
        StringBuffer p = new StringBuffer();
        fillInPDF(p);
        add(p.toString());
    }

    /**
     * set the base CMap
     *
     * @param base the name of the base CMap
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

    /**
     * Fill in the pdf string for this CMap.
     *
     * @param p the string buffer to add the pdf data to
     */
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
         * } else {// base instanceof PDFStream
         * p.append(((PDFStream)base).referencePDF());
         * }
         * }
         */
    }

}
