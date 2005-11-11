/*
 * Copyright 2005 The Apache Software Foundation.
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

package org.apache.fop.render;

import org.apache.fop.apps.FOUserAgent;

/**
 * Base class for factory classes which instantiate Renderers and provide information
 * about them.
 */
public abstract class AbstractRendererMaker {
    
    /**
     * Instantiates a new renderer.
     * @param ua the user agent
     * @return the newly instantiated renderer
     */
    public abstract Renderer makeRenderer(FOUserAgent ua);

    /**
     * @return Indicates whether this renderer requires an OutputStream to work with.
     */
    public abstract boolean needsOutputStream();
    
    /**
     * @return an array of MIME types the renderer supports.
     */
    public abstract String[] getSupportedMimeTypes();

    /**
     * Indicates whether a specific MIME type is supported by this renderer.
     * @param mimeType the MIME type (ex. "application/pdf")
     * @return true if the MIME type is supported
     */
    public boolean isMimeTypeSupported(String mimeType) {
        String[] mimes = getSupportedMimeTypes();
        for (int i = 0; i < mimes.length; i++) {
            if (mimes[i].equals(mimeType)) {
                return true;
            }
        }
        return false;
    }
    
}
