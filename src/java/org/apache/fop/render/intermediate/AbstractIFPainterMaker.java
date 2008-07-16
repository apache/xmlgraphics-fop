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

package org.apache.fop.render.intermediate;

import org.apache.fop.apps.FOUserAgent;

/**
 * Base class for factory classes which instantiate {@code IFPainter}s and provide information
 * about them.
 */
public abstract class AbstractIFPainterMaker {

    /**
     * Instantiates a new {@code IFPainter}.
     * @param userAgent the user agent
     * @return the newly instantiated painter
     */
    public abstract IFPainter makePainter(FOUserAgent userAgent);

    /**
     * @return Indicates whether this painter requires an OutputStream to work with.
     */
    public abstract boolean needsOutputStream();

    /**
     * @return an array of MIME types the painter supports.
     */
    public abstract String[] getSupportedMimeTypes();

    /**
     * Returns a renderer config object that can be used to
     * configure the painter.
     * @param userAgent user agent
     * @return a config object that can be used to configure the painter
     */
    /*
    public RendererConfigurator getConfigurator(FOUserAgent userAgent) {
        return null;
    }*/

    /**
     * Indicates whether a specific MIME type is supported by this painter.
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
