/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

// based on work by Takayuki Takeuchi

/**
 * class representing a "character identifier" font (p 210 and onwards).
 */
public class PDFCIDFont extends PDFObject {

    public static final byte CID_TYPE0 = 0;
    public static final byte CID_TYPE2 = 1;
    protected static final String[] TYPE_NAMES = {
        "CIDFontType0", "CIDFontType2"
    };

    protected String basefont;

    protected String cidtype;
    protected Integer dw;
    protected PDFWArray w;
    protected int[] dw2;
    protected PDFWArray w2;
    protected PDFCIDSystemInfo systemInfo;
    protected PDFCIDFontDescriptor descriptor;
    protected PDFCMap cmap;

    /**
     * /CIDToGIDMap (only for CIDFontType2, see p 212)
     * can be either "Identity" (default) or a PDFStream
     */
    protected PDFStream cidMap;

    // compatibility with Takayuki Takeuchi

    /**
     * create the /Font object
     */
    public PDFCIDFont(int number, String basefont, byte cidtype, int dw,
                      int[] w, String registry, String ordering,
                      int supplement, PDFCIDFontDescriptor descriptor) {

        super(number);

        this.basefont = basefont;
        this.cidtype = TYPE_NAMES[(int)cidtype];
        this.dw = new Integer(dw);
        this.w = new PDFWArray();
        this.w.addEntry(0, w);
        this.dw2 = null;
        this.w2 = null;
        this.systemInfo = new PDFCIDSystemInfo(registry, ordering,
                                               supplement);
        this.descriptor = descriptor;
        this.cidMap = null;
        this.cmap = null;
    }

    /**
     * create the /Font object
     */
    public PDFCIDFont(int number, String basefont, byte cidtype, int dw,
                      PDFWArray w, PDFCIDSystemInfo systemInfo,
                      PDFCIDFontDescriptor descriptor) {

        super(number);

        this.basefont = basefont;
        this.cidtype = TYPE_NAMES[(int)cidtype];
        this.dw = new Integer(dw);
        this.w = w;
        this.dw2 = null;
        this.w2 = null;
        this.systemInfo = systemInfo;
        this.descriptor = descriptor;
        this.cidMap = null;
        this.cmap = null;
    }

    /**
     * set the /DW attribute
     */
    public void setDW(int dw) {
        this.dw = new Integer(dw);
    }

    /**
     * set the /W array
     */
    public void setW(PDFWArray w) {
        this.w = w;
    }

    /**
     * set the (two elements) /DW2 array
     */
    public void setDW2(int[] dw2) {
        this.dw2 = dw2;
    }

    /**
     * set the two elements of the /DW2 array
     */
    public void setDW2(int posY, int displacementY) {
        this.dw2 = new int[] {
            posY, displacementY
        };
    }

    /**
     * Set the CMap used as /ToUnicode cmap
     */
    public void setCMAP(PDFCMap cmap) {
        this.cmap = cmap;
    }

    /**
     * set the /W2 array
     */
    public void setW2(PDFWArray w2) {
        this.w2 = w2;
    }

    /**
     * set the /CIDToGIDMap (to be used only for CIDFontType2)
     */
    public void setCIDMap(PDFStream map) {
        this.cidMap = map;
    }

    /**
     * set the /CIDToGIDMap (to be used only for CIDFontType2) to "Identity"
     */
    public void setCIDMapIdentity() {
        this.cidMap = null;    // not an error here, simply use the default
    }

    /**
     * produce the PDF representation for the object
     *
     * @return the PDF
     */
    public byte[] toPDF() {
        return toPDFString().getBytes();
    }

    public String toPDFString() {
        StringBuffer p = new StringBuffer();
        p.append(this.number);
        p.append(" ");
        p.append(this.generation);
        p.append(" obj\n<< /Type /Font");
        p.append("\n/BaseFont /");
        p.append(this.basefont);
        if (cidMap != null) {
            p.append(" \n/CIDToGIDMap ");
            p.append(cidMap.referencePDF());
        }
        p.append(" \n/Subtype /");
        p.append(this.cidtype);
        p.append("\n");
        p.append(systemInfo.toPDFString());
        p.append("\n/FontDescriptor ");
        p.append(this.descriptor.referencePDF());

        if (cmap != null) {
            p.append("\n/ToUnicode ");
            p.append(cmap.referencePDF());
        }
        if (dw != null) {
            p.append("\n/DW ");
            p.append(this.dw);
        }
        if (w != null) {
            p.append("\n/W ");
            p.append(w.toPDFString());
        }
        if (dw2 != null) {
            p.append("\n/DW2 [");    // always two values, see p 211
            p.append(this.dw2[0]);
            p.append(this.dw2[1]);
            p.append("] \n>>\nendobj\n");
        }
        if (w2 != null) {
            p.append("\n/W2 ");
            p.append(w2.toPDFString());
            p.append(" \n>>\nendobj\n");
        }
        p.append(" \n>>\nendobj\n");
        return p.toString();
    }

}

