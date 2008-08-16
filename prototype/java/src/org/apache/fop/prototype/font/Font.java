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

package org.apache.fop.prototype.font;

import java.util.HashMap;
import java.util.Map;

/**
 * A font.
 */
public class Font {

    public final static Font TIMES_FONT;

    static {
        Map<Character, Integer> m = new HashMap<Character, Integer>();
        m.put(' ', 250);
        m.put('!', 333);
        m.put('#', 500);
        m.put('$', 500);
        m.put('%', 833);
        m.put('&', 778);
        m.put('”', 333);
        m.put('(', 333);
        m.put(')', 333);
        m.put('*', 500);
        m.put('+', 564);
        m.put(',', 250);
        m.put('-', 333);
        m.put('.', 250);
        m.put('/', 278);
        m.put('0', 500);
        m.put('1', 500);
        m.put('2', 500);
        m.put('3', 500);
        m.put('4', 500);
        m.put('5', 500);
        m.put('6', 500);
        m.put('7', 500);
        m.put('8', 500);
        m.put('9', 500);
        m.put(':', 278);
        m.put(';', 278);
        m.put('<', 564);
        m.put('=', 564);
        m.put('>', 564);
        m.put('?', 444);
        m.put('@', 921);
        m.put('A', 722);
        m.put('B', 667);
        m.put('C', 667);
        m.put('D', 722);
        m.put('E', 611);
        m.put('F', 556);
        m.put('G', 722);
        m.put('H', 722);
        m.put('I', 333);
        m.put('J', 389);
        m.put('K', 722);
        m.put('L', 611);
        m.put('M', 889);
        m.put('N', 722);
        m.put('O', 722);
        m.put('P', 556);
        m.put('Q', 722);
        m.put('R', 667);
        m.put('S', 556);
        m.put('T', 611);
        m.put('U', 722);
        m.put('V', 722);
        m.put('W', 944);
        m.put('X', 722);
        m.put('Y', 722);
        m.put('Z', 611);
        m.put('_', 500);
        m.put('“', 333);
        m.put('a', 444);
        m.put('b', 500);
        m.put('c', 444);
        m.put('d', 500);
        m.put('e', 444);
        m.put('f', 333);
        m.put('g', 500);
        m.put('h', 500);
        m.put('i', 278);
        m.put('j', 278);
        m.put('k', 500);
        m.put('l', 278);
        m.put('m', 778);
        m.put('n', 500);
        m.put('o', 500);
        m.put('p', 500);
        m.put('q', 500);
        m.put('r', 333);
        m.put('s', 389);
        m.put('t', 278);
        m.put('u', 500);
        m.put('v', 500);
        m.put('w', 722);
        m.put('x', 500);
        m.put('y', 500);
        m.put('z', 444);
        TIMES_FONT = new Font(m);
    }

    private Map<Character, Integer> charWidths;

    private Font(Map<Character, Integer> charWidths) {
        this.charWidths = charWidths;
    }

    public int getCharWidth(char ch) {
        return charWidths.get(ch);
    }
}
