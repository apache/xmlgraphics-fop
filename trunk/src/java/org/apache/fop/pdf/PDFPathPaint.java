/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

/**
 * Base class for PDF painting operations.
 *
 */
public abstract class PDFPathPaint extends PDFObject {

    /**
     * The color space for this paint
     */
    protected PDFColorSpace colorSpace;

    /**
     * Get the PDF string for setting the path paint.
     *
     * @param fillNotStroke if true fill otherwise stroke
     * @return the PDF instruction string
     */
    public String getColorSpaceOut(boolean fillNotStroke) {
        return ("");
    }

    /**
     * Set the color space for this paint.
     *
     * @param theColorSpace the color space value
     */
    public void setColorSpace(int theColorSpace) {
        this.colorSpace.setColorSpace(theColorSpace);
    }

    /**
     * Get the current color space value for this paint.
     *
     * @return the color space value
     */
    public int getColorSpace() {
        return this.colorSpace.getColorSpace();
    }

}

