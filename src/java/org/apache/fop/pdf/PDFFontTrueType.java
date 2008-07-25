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

package org.apache.fop.pdf;

import org.apache.fop.fonts.FontType;

/**
 * Class representing a TrueType font.
 * <p>
 * In fact everything already done in the superclass.
 * Must only define the not default constructor.
 */
public class PDFFontTrueType extends PDFFontNonBase14 {

    /**
     * create the /Font object
     *
     * @param fontname the internal name for the font
     * @param basefont the base font name
     * @param encoding the character encoding schema used by the font
     */
    public PDFFontTrueType(String fontname,
                           String basefont,
                           Object encoding) {
        super(fontname, FontType.TRUETYPE, basefont, encoding);
    }

}
