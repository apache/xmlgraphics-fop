/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layout;

public interface FontDescriptor {

    // Required
    public int getAscender();     // Ascent in pdf spec
    public int getCapHeight();
    public int getDescender();    // Descent in pdf spec
    public int getFlags();
    public int[] getFontBBox();
    public String fontName();     // should be getFontName(). not?
    public int getItalicAngle();
    public int getStemV();

    public boolean hasKerningInfo();
    public java.util.HashMap getKerningInfo();
    public boolean isEmbeddable();
    public byte getSubType();
    public org.apache.fop.pdf.PDFStream getFontFile(int objNum);
    // Optional - but needed to calculate font-size-adjust...
    // public int getXHeight();

}


