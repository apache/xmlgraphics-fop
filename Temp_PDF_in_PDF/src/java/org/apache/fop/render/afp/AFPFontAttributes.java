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

package org.apache.fop.render.afp;

import org.apache.fop.render.afp.fonts.AFPFont;
import org.apache.fop.render.afp.tools.BinaryUtils;

/**
 * This class encapsulates the font atributes that need to be included
 * in the AFP data stream. This class does not assist in converting the
 * font attributes to AFP code pages and character set values.
 *
 */
public class AFPFontAttributes {

    /**
     * The font reference byte
     */
    private byte _fontReference;

    /**
     * The font key
     */
    private String _fontKey;

    /**
     * The font
     */
    private AFPFont _font;

    /**
     * The point size
     */
    private int _pointSize;

    /**
     * Constructor for the AFPFontAttributes
     * @param fontKey the font key
     * @param font the font
     * @param pointSize the point size
     */
    public AFPFontAttributes(

        String fontKey,
        AFPFont font,
        int pointSize) {

        _fontKey = fontKey;
        _font = font;
        _pointSize = pointSize;

    }
    /**
     * @return the font
     */
    public AFPFont getFont() {
        return _font;
    }

    /**
     * @return the FontKey attribute
     */
    public String getFontKey() {

        return _fontKey + _pointSize;

    }

    /**
     * @return the point size attribute
     */
    public int getPointSize() {
        return _pointSize;
    }

    /**
     * @return the FontReference attribute
     */
    public byte getFontReference() {
        return _fontReference;
    }

    /**
     * Sets the FontReference attribute
     * @param fontReference the FontReference to set
     */
    public void setFontReference(int fontReference) {

        String id = String.valueOf(fontReference);
        _fontReference = BinaryUtils.convert(id)[0];

    }

}
