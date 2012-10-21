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

import org.apache.fop.fonts.FontType;

/**
 * A common ancestor for Type1, TrueType, MMType1 and Type3 fonts
 * (all except base 14 fonts).
 */
public abstract class PDFFontNonBase14 extends PDFFont {

    /**
     * Create the /Font object
     *
     * @param fontname the internal name for the font
     * @param subtype the font's subtype
     * @param basefont the base font name
     * @param encoding the character encoding schema used by the font
     */
    public PDFFontNonBase14(String fontname, FontType subtype,
                            String basefont,
                            Object encoding) {

        /* generic creation of PDF object */
        super(fontname, subtype, basefont, encoding);
    }

    /**
     * Set the width metrics for the font
     *
     * @param firstChar the first character code in the font
     * @param lastChar the last character code in the font
     * @param widths an array of size (lastChar - firstChar +1)
     */
    public void setWidthMetrics(int firstChar, int lastChar,
                                PDFArray widths) {
        put("FirstChar", new Integer(firstChar));
        put("LastChar", new Integer(lastChar));
        put("Widths", widths);
    }

    /**
     * Set the font descriptor (unused for the Type3 fonts)
     *
     * @param descriptor the descriptor for other font's metrics
     */
    public void setDescriptor(PDFFontDescriptor descriptor) {
        put("FontDescriptor", descriptor);
    }

    /** @return the FontDescriptor or null if there is none */
    public PDFFontDescriptor getDescriptor() {
        return (PDFFontDescriptor)get("FontDescriptor");
    }

    /** {@inheritDoc} */
    protected void validate() {
        if (getDocumentSafely().getProfile().isFontEmbeddingRequired()) {
            if (this.getDescriptor().getFontFile() == null) {
                throw new PDFConformanceException("For " + getDocumentSafely().getProfile()
                    + ", all fonts have to be embedded! Offending font: " + getBaseFont());
            }
        }
    }

}
