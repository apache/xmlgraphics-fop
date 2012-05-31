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

package org.apache.fop.render.adobe;

import java.util.ArrayList;
import java.util.List;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fonts.FontCollection;
import org.apache.fop.fonts.FontManager;
import org.apache.fop.fonts.base14.Base14FontCollection;
import org.apache.fop.render.PrintRendererConfigurator;
import org.apache.fop.render.RendererConfig.RendererConfigParser;

public class AdobeRendererConfigurator extends PrintRendererConfigurator {

    public AdobeRendererConfigurator(FOUserAgent userAgent, RendererConfigParser rendererConfigParser) {
        super(userAgent, rendererConfigParser);
    }

    protected List<FontCollection> getDefaultFontCollection() {
        FontManager fontManager = userAgent.getFontManager();
        List<FontCollection> fontCollection = new ArrayList<FontCollection>();
        fontCollection.add(new Base14FontCollection(fontManager.isBase14KerningEnabled()));
        return fontCollection;
    }

}
