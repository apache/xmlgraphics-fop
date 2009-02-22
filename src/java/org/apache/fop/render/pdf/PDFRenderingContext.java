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

import org.apache.xmlgraphics.util.MimeConstants;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.pdf.PDFPage;
import org.apache.fop.render.AbstractRenderingContext;

/**
 * Rendering context for PDF production.
 */
public class PDFRenderingContext extends AbstractRenderingContext {

    private PDFContentGenerator generator;
    private FontInfo fontInfo;
    private PDFPage page;

    /**
     * Main constructor.
     * @param userAgent the user agent
     * @param generator the PDF content generator
     * @param page the current PDF page
     * @param fontInfo the font list
     */
    public PDFRenderingContext(FOUserAgent userAgent,
            PDFContentGenerator generator, PDFPage page, FontInfo fontInfo) {
        super(userAgent);
        this.generator = generator;
        this.page = page;
        this.fontInfo = fontInfo;
    }

    /** {@inheritDoc} */
    public String getMimeType() {
        return MimeConstants.MIME_PDF;
    }

    /**
     * Returns the PDF content generator.
     * @return the PDF content generator
     */
    public PDFContentGenerator getGenerator() {
        return this.generator;
    }

    /**
     * Returns the current PDF page.
     * @return the PDF page
     */
    public PDFPage getPage() {
        return this.page;
    }

    /**
     * Returns the font list.
     * @return the font list
     */
    public FontInfo getFontInfo() {
        return this.fontInfo;
    }

}
