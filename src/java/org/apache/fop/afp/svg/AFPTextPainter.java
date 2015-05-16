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

package org.apache.fop.afp.svg;

import java.awt.Graphics2D;

import org.apache.batik.bridge.FontFamilyResolver;
import org.apache.batik.bridge.StrokingTextPainter;

import org.apache.fop.afp.AFPGraphics2D;
import org.apache.fop.svg.AbstractFOPTextPainter;
import org.apache.fop.svg.FOPTextHandler;

/**
 * Renders the attributed character iterator of some text.
 * This class draws the text directly into the AFPGraphics2D so that
 * the text is not drawn using shapes.
 * If the text is simple enough to draw then it sets the font and calls
 * drawString. If the text is complex or the cannot be translated
 * into a simple drawString the StrokingTextPainter is used instead.
 */
public class AFPTextPainter extends AbstractFOPTextPainter {

    /**
     * Create a new text painter with the given font information.
     * @param nativeTextHandler the NativeTextHandler instance used for text painting
     * @param fopFontFamilyResolver the font resolver
     */
    public AFPTextPainter(FOPTextHandler nativeTextHandler, FontFamilyResolver fopFontFamilyResolver) {
        super(nativeTextHandler, new FOPStrokingTextPainter(fopFontFamilyResolver));
    }

    /** {@inheritDoc} */
    protected boolean isSupportedGraphics2D(Graphics2D g2d) {
        return g2d instanceof AFPGraphics2D;
    }

    private static class FOPStrokingTextPainter extends StrokingTextPainter {

        private final FontFamilyResolver fopFontFontFamily;

        FOPStrokingTextPainter(FontFamilyResolver fopFontFontFamily) {
            this.fopFontFontFamily = fopFontFontFamily;
        }

        @Override
        protected FontFamilyResolver getFontFamilyResolver() {
            return fopFontFontFamily;
        }
    }

}
