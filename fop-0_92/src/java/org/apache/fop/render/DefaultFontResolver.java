/*
 * Copyright 2006 The Apache Software Foundation.
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

import javax.xml.transform.Source;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fonts.FontResolver;

/**
 * Default FontResolver implementation which uses the FOUserAgent to resolve font URIs.
 */
public class DefaultFontResolver implements FontResolver {

    private FOUserAgent userAgent;
    
    /**
     * Main constructor.
     * @param userAgent the user agent
     */
    public DefaultFontResolver(FOUserAgent userAgent) {
        this.userAgent = userAgent;
    }
    
    /** @see org.apache.fop.fonts.FontResolver#resolve(java.lang.String) */
    public Source resolve(String href) {
        return userAgent.resolveURI(href, userAgent.getFontBaseURL());
    }
    
}
