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

package org.apache.fop.util.text;

import java.util.Map;

import org.apache.xmlgraphics.fonts.Glyphs;

import org.apache.fop.util.text.AdvancedMessageFormat.Part;
import org.apache.fop.util.text.AdvancedMessageFormat.PartFactory;

/**
 * Function formatting a character to a glyph name.
 */
public class GlyphNameFieldPart implements Part {

    private String fieldName;

    /**
     * Creates a new glyph name field part
     * @param fieldName the field name
     */
    public GlyphNameFieldPart(String fieldName) {
        this.fieldName = fieldName;
    }

    /** {@inheritDoc} */
    public boolean isGenerated(Map params) {
        Object obj = params.get(fieldName);
        return obj != null && getGlyphName(obj).length() > 0;
    }

    private String getGlyphName(Object obj) {
        if (obj instanceof Character) {
            return Glyphs.charToGlyphName(((Character)obj).charValue());
        } else {
            throw new IllegalArgumentException(
                    "Value for glyph name part must be a Character but was: "
                        + obj.getClass().getName());
        }
    }

    /** {@inheritDoc} */
    public void write(StringBuffer sb, Map params) {
        if (!params.containsKey(fieldName)) {
            throw new IllegalArgumentException(
                    "Message pattern contains unsupported field name: " + fieldName);
        }
        Object obj = params.get(fieldName);
        sb.append(getGlyphName(obj));
    }

    /** {@inheritDoc} */
    public String toString() {
        return "{" + this.fieldName + ",glyph-name}";
    }

    /** Factory for {@link GlyphNameFieldPart}. */
    public static class Factory implements PartFactory {

        /** {@inheritDoc} */
        public Part newPart(String fieldName, String values) {
            return new GlyphNameFieldPart(fieldName);
        }

        /** {@inheritDoc} */
        public String getFormat() {
            return "glyph-name";
        }

    }
}
