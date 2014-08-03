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

import java.awt.font.FontRenderContext;
import java.text.AttributedCharacterIterator;
import java.text.CharacterIterator;

import org.apache.batik.gvt.text.GVTAttributedCharacterIterator;

import org.apache.fop.fonts.GlyphMapping;

class ComplexGlyphVector extends FOPGVTGlyphVector {

    public static final AttributedCharacterIterator.Attribute WRITING_MODE
        = GVTAttributedCharacterIterator.TextAttribute.WRITING_MODE;

    public static final Integer WRITING_MODE_RTL
        = GVTAttributedCharacterIterator.TextAttribute.WRITING_MODE_RTL;

    ComplexGlyphVector(FOPGVTFont font, final CharacterIterator iter, FontRenderContext frc) {
        super(font, iter, frc);
    }

    public void performDefaultLayout() {
        super.performDefaultLayout();
    }

    protected void maybeReverse(GlyphMapping mapping) {
        if (charIter instanceof AttributedCharacterIterator) {
            AttributedCharacterIterator aci = (AttributedCharacterIterator) charIter;
            aci.first();
            if (aci.getAttribute(WRITING_MODE) == WRITING_MODE_RTL) {
                mapping.reverse();
            }
        }
    }

}
