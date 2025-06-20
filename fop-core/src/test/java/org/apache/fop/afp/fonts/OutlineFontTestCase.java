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
package org.apache.fop.afp.fonts;

import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Test;

import org.apache.fop.fonts.Font;

public class OutlineFontTestCase {
    @Test
    public void testWidth() {
        CharacterSet characterSet = getCharacterSet();
        OutlineFont outlineFont = new OutlineFont(null, true, characterSet, null);
        Font font = new Font(null, null, outlineFont, 26000);
        Assert.assertEquals(font.getWidth(' '), 0);
    }

    public static CharacterSet getCharacterSet() {
        CharacterSet characterSet = new CharacterSet("00000000", StandardCharsets.UTF_8.name(),
                CharacterSetType.SINGLE_BYTE, "", null, null);
        characterSet.addCharacterSetOrientation(new CharacterSetOrientation(0, 0, 0, 0));
        return characterSet;
    }
}
