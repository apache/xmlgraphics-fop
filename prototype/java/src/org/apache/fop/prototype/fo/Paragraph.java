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

package org.apache.fop.prototype.fo;

import java.util.ArrayList;
import java.util.List;

import org.apache.fop.prototype.TypographicElement;
import org.apache.fop.prototype.font.Font;
import org.apache.fop.prototype.knuth.Box;
import org.apache.fop.prototype.knuth.Glue;
import org.apache.fop.prototype.knuth.KnuthElement;
import org.apache.fop.prototype.knuth.Penalty;


/**
 * A paragraph
 */
public class Paragraph implements TypographicElement {

    private String[] words;

    private Font font;

    public Paragraph(Font font, String... words) {
        this.words = words;
        this.font = font;
    }

    public List<KnuthElement> getKnuthElements() {
        List<KnuthElement> elements = new ArrayList<KnuthElement>(3 * words.length + 2);
        int spaceWidth = font.getCharWidth(' ');
        for (String word: words) {
            elements.add(new Box(getWordWidth(word), word));
            elements.add(Penalty.DEFAULT_PENALTY);
            elements.add(new Glue(spaceWidth, spaceWidth / 2, spaceWidth / 3, " "));
        }
        elements.set(elements.size() - 2, new Glue(0, 1000000, 0));
        elements.set(elements.size() - 1, new Penalty(0, -Penalty.INFINITE));
        return elements;
    }

    private int getWordWidth(String word) {
        int w = 0;
        for (int i = 0; i < word.length(); i++) {
            w += font.getCharWidth(word.charAt(i));
        }
        return w;
    }
}
