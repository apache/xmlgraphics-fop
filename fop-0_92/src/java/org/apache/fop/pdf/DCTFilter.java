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
 * DCT Filter class. Right now it is just used as a dummy filter flag so
 * we can write JPG images to the PDF. The encode method just returns the
 * data passed to it. In the future an actual JPEG compression should be
 * added to the encode method so other images can be compressed.
 *
 * @author Eric Dalquist
 */
public class DCTFilter extends NullFilter {

    /**
     * Get filter name.
     * @return the pdf name for the DCT filter
     */
    public String getName() {
        return "/DCTDecode";
    }

    /**
     * Get the decode params for this filter.
     * @return the DCT filter has no decode params
     */
    public String getDecodeParms() {
        return null;
    }

}

