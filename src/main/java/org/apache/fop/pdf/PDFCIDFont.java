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
import java.util.Set;

import org.apache.fop.fonts.CIDFontType;

// based on work by Takayuki Takeuchi

/**
 * Class representing a "character identifier" font (p 210 and onwards).
 */
public class PDFCIDFont extends PDFObject {

    private String basefont;
    private CIDFontType cidtype;
    private Integer dw;
    private PDFWArray w;
    private int[] dw2;
    private PDFWArray w2;
    private PDFCIDSystemInfo systemInfo;
    private PDFCIDFontDescriptor descriptor;
    private PDFCMap cmap;

    /**
     * /CIDToGIDMap (only for CIDFontType2, see p 212)
     * can be either "Identity" (default) or a PDFStream
     */
    private PDFStream cidMap;


    /**
     * Create the /Font object
     * @param basefont Name of the basefont
     * @param cidtype CID type
     * @param dw default width
     * @param w array of character widths
     * @param registry name of the issuer
     * @param ordering Unique name of the font
     * @param supplement Supplement number
     * @param descriptor CID font descriptor
     */
    public PDFCIDFont(String basefont, CIDFontType cidtype, int dw,
            int[] w, String registry, String ordering,
            int supplement, PDFCIDFontDescriptor descriptor) {

        this(basefont, cidtype, dw,
                new PDFWArray(w),
                new PDFCIDSystemInfo(registry, ordering, supplement),
                descriptor);
    }

    /**
     * Create the /Font object
     * @param basefont Name of the basefont
     * @param cidtype CID type
     * @param dw default width
     * @param w array of character widths
     * @param systemInfo CID system info
     * @param descriptor CID font descriptor
     */
    public PDFCIDFont(String basefont, CIDFontType cidtype, int dw,
                      int[] w, PDFCIDSystemInfo systemInfo,
                      PDFCIDFontDescriptor descriptor) {

        this(basefont, cidtype, dw,
                new PDFWArray(w),
                systemInfo,
                descriptor);
    }

    /**
     * Create the /Font object
     * @param basefont Name of the basefont
     * @param cidtype CID type
     * @param dw default width
     * @param w array of character widths
     * @param systemInfo CID system info
     * @param descriptor CID font descriptor
     */
    public PDFCIDFont(String basefont, CIDFontType cidtype, int dw,
                      PDFWArray w, PDFCIDSystemInfo systemInfo,
                      PDFCIDFontDescriptor descriptor) {

        super();

        this.basefont = basefont;
        this.cidtype = cidtype;
        this.dw = new Integer(dw);
        this.w = w;
        this.dw2 = null;
        this.w2 = null;
        systemInfo.setParent(this);
        this.systemInfo = systemInfo;
        this.descriptor = descriptor;
        this.cidMap = null;
        this.cmap = null;
    }

    /**
     * Set the /DW attribute
     * @param dw the default width
     */
    public void setDW(int dw) {
        this.dw = new Integer(dw);
    }

    /**
     * Set the /W array
     * @param w the width array
     */
    public void setW(PDFWArray w) {
        this.w = w;
    }

    /**
     * Set the (two elements) /DW2 array
     * @param dw2 the default metrics for vertical writing
     */
    public void setDW2(int[] dw2) {
        this.dw2 = dw2;
    }

    /**
     * Set the two elements of the /DW2 array
     * @param posY position vector
     * @param displacementY displacement vector
     */
    public void setDW2(int posY, int displacementY) {
        this.dw2 = new int[] {
            posY, displacementY
        };
    }

    /**
     * Set the CMap used as /ToUnicode cmap
     * @param cmap character map
     */
    public void setCMAP(PDFCMap cmap) {
        this.cmap = cmap;
    }

    /**
     * Set the /W2 array
     * @param w2 array of metrics for vertical writing
     */
    public void setW2(PDFWArray w2) {
        this.w2 = w2;
    }

    /**
     * Set the /CIDToGIDMap (to be used only for CIDFontType2)
     * @param map mapping information
     */
    public void setCIDMap(PDFStream map) {
        this.cidMap = map;
    }

    /**
     * Set the /CIDToGIDMap (to be used only for CIDFontType2) to "Identity"
     */
    public void setCIDMapIdentity() {
        this.cidMap = null;    // not an error here, simply use the default
    }

    /**
     * Returns the PDF name for a certain CID font type.
     * @param cidFontType CID font type
     * @return corresponding PDF name
     */
    protected String getPDFNameForCIDFontType(CIDFontType cidFontType) {
        if (cidFontType == CIDFontType.CIDTYPE0) {
            return cidFontType.getName();
        } else if (cidFontType == CIDFontType.CIDTYPE2) {
            return cidFontType.getName();
        } else {
            throw new IllegalArgumentException("Unsupported CID font type: "
                        + cidFontType.getName());
        }
    }

    /**
     * {@inheritDoc}
     */
    public String toPDFString() {
        StringBuffer p = new StringBuffer(128);
        p.append("<< /Type /Font");
        p.append("\n/BaseFont /");
        p.append(this.basefont);
        p.append(" \n/CIDToGIDMap ");
        if (cidMap != null) {
            p.append(cidMap.referencePDF());
        } else {
            p.append("/Identity");
            //This is the default. We still write it because PDF/A requires it.
        }
        p.append(" \n/Subtype /");
        p.append(getPDFNameForCIDFontType(this.cidtype));
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
            p.append("]");
        }
        if (w2 != null) {
            p.append("\n/W2 ");
            p.append(w2.toPDFString());
        }
        p.append("\n>>");
        return p.toString();
    }

    /**
     * {@inheritDoc}
     */
    public byte[] toPDF() {
        ByteArrayOutputStream bout = new ByteArrayOutputStream(128);
        try {
            bout.write(encode("<< /Type /Font\n"));
            bout.write(encode("/BaseFont /"));
            bout.write(encode(this.basefont));
            bout.write(encode(" \n"));
            bout.write(encode("/CIDToGIDMap "));
            bout.write(encode(cidMap != null ? cidMap.referencePDF() : "/Identity"));
            bout.write(encode(" \n"));
            bout.write(encode("/Subtype /"));
            bout.write(encode(getPDFNameForCIDFontType(this.cidtype)));
            bout.write(encode("\n"));
            bout.write(encode("/CIDSystemInfo "));
            bout.write(systemInfo.toPDF());
            bout.write(encode("\n"));
            bout.write(encode("/FontDescriptor "));
            bout.write(encode(this.descriptor.referencePDF()));
            bout.write(encode("\n"));
            if (cmap != null) {
                bout.write(encode("/ToUnicode "));
                bout.write(encode(cmap.referencePDF()));
                bout.write(encode("\n"));
            }
            if (dw != null) {
                bout.write(encode("/DW "));
                bout.write(encode(this.dw.toString()));
                bout.write(encode("\n"));
            }
            if (w != null) {
                bout.write(encode("/W "));
                bout.write(encode(w.toPDFString()));
                bout.write(encode("\n"));
            }
            if (dw2 != null) {
                bout.write(encode("/DW2 [")); // always two values, see p 211
                bout.write(encode(Integer.toString(this.dw2[0])));
                bout.write(encode(Integer.toString(this.dw2[1])));
                bout.write(encode("]\n"));
            }
            if (w2 != null) {
                bout.write(encode("/W2 "));
                bout.write(encode(w2.toPDFString()));
                bout.write(encode("\n"));
            }
            bout.write(encode(">>"));
        } catch (IOException ioe) {
            log.error("Ignored I/O exception", ioe);
        }
        return bout.toByteArray();
    }

    @Override
    public void getChildren(Set<PDFObject> children) {
        super.getChildren(children);
        if (cidMap != null) {
            children.add(cidMap);
            cidMap.getChildren(children);
        }
        children.add(descriptor);
        descriptor.getChildren(children);
        if (cmap != null) {
            children.add(cmap);
            cmap.getChildren(children);
        }
    }

}

