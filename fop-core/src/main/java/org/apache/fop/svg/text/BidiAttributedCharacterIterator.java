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

package org.apache.fop.svg.text;

import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.Map;
import java.util.Set;

import org.apache.batik.gvt.text.GVTAttributedCharacterIterator;

import org.apache.fop.complexscripts.bidi.UnicodeBidiAlgorithm;
import org.apache.fop.traits.Direction;

public class BidiAttributedCharacterIterator implements AttributedCharacterIterator {

    private AttributedCharacterIterator aci;

    protected BidiAttributedCharacterIterator(AttributedCharacterIterator aci) {
        this.aci = aci;
    }

    public BidiAttributedCharacterIterator(AttributedCharacterIterator aci, int defaultBidiLevel) {
        this(annotateBidiLevels(aci, defaultBidiLevel));
    }

    private static AttributedCharacterIterator
        annotateBidiLevels(AttributedCharacterIterator aci, int defaultBidiLevel) {
        int start = aci.getBeginIndex();
        int end = aci.getEndIndex();
        int numChars = end - start;
        StringBuffer sb = new StringBuffer(numChars);
        for (int i = 0; i < numChars; ++i) {
            char ch = aci.setIndex(i);
            assert ch != AttributedCharacterIterator.DONE;
            sb.append(ch);
        }
        int[] levels =
            UnicodeBidiAlgorithm.resolveLevels(sb, (defaultBidiLevel & 1) == 1 ? Direction.RL : Direction.LR);
        if (levels != null) {
            assert levels.length == numChars;
            AttributedString as = new AttributedString(aci, start, end);
            int runStart = 0;
            int runEnd = runStart;
            int nextRunLevel = -1;
            int currRunLevel = -1;
            for (int i = 0, n = levels.length; i < n; ++i) {
                nextRunLevel = levels[i];
                if (currRunLevel < 0) {
                    currRunLevel = nextRunLevel;
                } else if (nextRunLevel != currRunLevel) {
                    as.addAttribute(GVTAttributedCharacterIterator.TextAttribute.BIDI_LEVEL,
                                    new Integer(currRunLevel), runStart, i);
                    runStart = i;
                    runEnd = runStart;
                    currRunLevel = nextRunLevel;
                }
            }
            if ((currRunLevel >= 0) && (end > runStart)) {
                as.addAttribute(GVTAttributedCharacterIterator.TextAttribute.BIDI_LEVEL,
                                new Integer(currRunLevel), runStart, end);
            }
            return as.getIterator();
        } else {
            return aci;
        }
    }

    // CharacterIterator

    public char first() {
        return aci.first();
    }

    public char last() {
        return aci.last();
    }

    public char current() {
        return aci.current();
    }

    public char next() {
        return aci.next();
    }

    public char previous() {
        return aci.previous();
    }

    public char setIndex(int position) {
       return aci.setIndex(position);
    }

    public int getBeginIndex() {
        return aci.getBeginIndex();
    }

    public int getEndIndex() {
        return aci.getEndIndex();
    }

    public int getIndex() {
        return aci.getIndex();
    }

    // @SuppressFBWarnings("CN_IDIOM_NO_SUPER_CALL")
    public Object clone() {
        return new BidiAttributedCharacterIterator((AttributedCharacterIterator)aci.clone());
    }

    // AttributedCharacterIterator

    public int getRunStart() {
        return aci.getRunStart();
    }

    public int getRunStart(AttributedCharacterIterator.Attribute attribute) {
        return aci.getRunStart(attribute);
    }

    public int getRunStart(Set<? extends AttributedCharacterIterator.Attribute> attributes) {
        return aci.getRunStart(attributes);
    }

    public int getRunLimit() {
        return aci.getRunLimit();
    }

    public int getRunLimit(AttributedCharacterIterator.Attribute attribute) {
        return aci.getRunLimit(attribute);
    }

    public int getRunLimit(Set<? extends AttributedCharacterIterator.Attribute> attributes) {
        return aci.getRunLimit(attributes);
    }

    public Map<AttributedCharacterIterator.Attribute, Object> getAttributes() {
        return aci.getAttributes();
    }

    public Object getAttribute(AttributedCharacterIterator.Attribute attribute) {
        return aci.getAttribute(attribute);
    }

    public Set<AttributedCharacterIterator.Attribute> getAllAttributeKeys() {
        return aci.getAllAttributeKeys();
    }

}
