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

import java.io.IOException;

import org.apache.fop.image.FopImage;
import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.pdf.PDFXObject;

/**
 * This interface is used for handling all sorts of image type for PDF output.
 */
public interface PDFImageHandler {

    /**
     * Returns the MIME type supported by this instance.
     * @return the MIME type
     */
    String getSupportedMimeType();
    
    /**
     * Generates the PDF objects for the given FopImage instance and returns
     * the resulting XObject.
     * @param image the image to be handled
     * @param uri the URI of the image
     * @param pdfDoc the target PDF document
     * @return the generated XObject
     * @throws IOException if an I/O error occurs
     */
    PDFXObject generateImage(FopImage image, String uri, PDFDocument pdfDoc) throws IOException;
    
}
