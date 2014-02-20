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

package org.apache.fop.svg.font;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.batik.bridge.FontFace;
import org.apache.batik.gvt.font.FontFamilyResolver;
import org.apache.batik.gvt.font.GVTFontFamily;

public class AggregatingFontFamilyResolver implements FontFamilyResolver {

    private final List<FontFamilyResolver> resolvers;

    public AggregatingFontFamilyResolver(FontFamilyResolver... resolvers) {
        this.resolvers = Arrays.<FontFamilyResolver>asList(resolvers);
    }

    public GVTFontFamily resolve(String familyName) {
        for (FontFamilyResolver resolver : resolvers) {
            GVTFontFamily family = resolver.resolve(familyName);
            if (family != null) {
                return family;
            }
        }
        return null;
    }

    public GVTFontFamily resolve(String familyName, FontFace fontFace) {
        for (FontFamilyResolver resolver : resolvers) {
            GVTFontFamily family = resolver.resolve(familyName, fontFace);
            if (family != null) {
                return family;
            }
        }
        return null;
    }

    public GVTFontFamily loadFont(InputStream in, FontFace fontFace) throws Exception {
        for (FontFamilyResolver resolver : resolvers) {
            try {
                return resolver.loadFont(in, fontFace);
            } catch (Exception e) {
                // Try the next one
            }
        }
        return null;
    }

    public GVTFontFamily getDefault() {
        return resolve("any");
    }

    public GVTFontFamily getFamilyThatCanDisplay(char c) {
        for (FontFamilyResolver resolver : resolvers) {
            GVTFontFamily family = resolver.getFamilyThatCanDisplay(c);
            if (family != null) {
                return family;
            }
        }
        return null;
    }

}
