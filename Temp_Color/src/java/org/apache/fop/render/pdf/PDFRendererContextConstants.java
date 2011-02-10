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

import org.apache.fop.render.RendererContextConstants;

/**
 * Defines a number of standard constants (keys) for use by the RendererContext class.
 */
public interface PDFRendererContextConstants extends RendererContextConstants {

    /** The PDF document that this image is being drawn into. */
    String PDF_DOCUMENT = "pdfDoc";

    /** The current PDF page for page renference and as a resource context. */
    String PDF_PAGE = "pdfPage";

    /** The current PDF page for page renference and as a resource context. */
    String PDF_CONTEXT = "pdfContext";

    /** The current PDF stream to draw directly to. */
    String PDF_STREAM = "pdfStream";

    /** The current font information for the pdf renderer. */
    String PDF_FONT_INFO = "fontInfo";

    /** The current pdf font name. */
    String PDF_FONT_NAME = "fontName";

    /** The current pdf font size. */
    String PDF_FONT_SIZE = "fontSize";

}
