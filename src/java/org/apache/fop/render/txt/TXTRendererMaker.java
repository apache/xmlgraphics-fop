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

package org.apache.fop.render.txt;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.render.AbstractRendererMaker;
import org.apache.fop.render.Renderer;

/**
 * RendererMaker for the Plain Text Renderer.
 */
public class TXTRendererMaker extends AbstractRendererMaker {

    private static final String[] MIMES = new String[] {MimeConstants.MIME_PLAIN_TEXT};
    
    
    /**@see org.apache.fop.render.AbstractRendererMaker */
    public Renderer makeRenderer(FOUserAgent ua) {
        return new TXTRenderer();
    }

    /** @see org.apache.fop.render.AbstractRendererMaker#needsOutputStream() */
    public boolean needsOutputStream() {
        return true;
    }

    /** @see org.apache.fop.render.AbstractRendererMaker#getSupportedMimeTypes() */
    public String[] getSupportedMimeTypes() {
        return MIMES;
    }

}
