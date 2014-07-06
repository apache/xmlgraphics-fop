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

package org.apache.fop.fo.flow;

import java.util.Iterator;
import java.util.Stack;

import org.apache.fop.apps.FOPException;
import org.apache.fop.complexscripts.bidi.DelimitedTextRange;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.properties.Property;
import org.apache.fop.util.CharUtilities;

/**
 * Class modelling the <a href="http://www.w3.org/TR/xsl/#fo_bidi-override">
 * <code>fo:bidi-override</code></a> object.
 */
public class BidiOverride extends Inline {

    // The value of FO traits (refined properties) that apply to fo:bidi-override
    // (that are not implemented by InlineLevel).
    private Property letterSpacing;
    private Property wordSpacing;
    private int direction;
    private int unicodeBidi;
    // private int scoreSpaces;
    // End of trait values

    /**
     * Base constructor
     *
     * @param parent FONode that is the parent of this object
     */
    public BidiOverride(FONode parent) {
        super(parent);
    }

    /** {@inheritDoc} */
    public void bind(PropertyList pList) throws FOPException {
        super.bind(pList);
        letterSpacing = pList.get(PR_LETTER_SPACING);
        wordSpacing = pList.get(PR_WORD_SPACING);
        direction = pList.get(PR_DIRECTION).getEnum();
        unicodeBidi = pList.get(PR_UNICODE_BIDI).getEnum();
    }

    /** @return the "letter-spacing" trait */
    public Property getLetterSpacing() {
        return letterSpacing;
    }

    /** @return the "word-spacing" trait */
    public Property getWordSpacing() {
        return wordSpacing;
    }

    /** @return the "direction" trait */
    public int getDirection() {
        return direction;
    }

    /** @return the "unicodeBidi" trait */
    public int getUnicodeBidi() {
        return unicodeBidi;
    }

    /** {@inheritDoc} */
    public String getLocalName() {
        return "bidi-override";
    }

    /**
     * {@inheritDoc}
     * @return {@link org.apache.fop.fo.Constants#FO_BIDI_OVERRIDE}
     */
    public int getNameId() {
        return FO_BIDI_OVERRIDE;
    }

    @Override
    protected Stack<DelimitedTextRange> collectDelimitedTextRanges(Stack<DelimitedTextRange> ranges,
        DelimitedTextRange currentRange) {
        char pfx = 0;
        char sfx = 0;
        int unicodeBidi = getUnicodeBidi();
        int direction = getDirection();
        if (unicodeBidi == Constants.EN_BIDI_OVERRIDE) {
            pfx = (direction == Constants.EN_RTL) ? CharUtilities.RLO : CharUtilities.LRO;
            sfx = CharUtilities.PDF;
        } else if (unicodeBidi == Constants.EN_EMBED) {
            pfx = (direction == Constants.EN_RTL) ? CharUtilities.RLE : CharUtilities.LRE;
            sfx = CharUtilities.PDF;
        }
        if (currentRange != null) {
            if (pfx != 0) {
                currentRange.append(pfx, this);
            }
            for (Iterator it = getChildNodes(); (it != null) && it.hasNext();) {
                ranges = ((FONode) it.next()).collectDelimitedTextRanges(ranges);
            }
            if (sfx != 0) {
                currentRange.append(sfx, this);
            }
        }
        return ranges;
    }

}
