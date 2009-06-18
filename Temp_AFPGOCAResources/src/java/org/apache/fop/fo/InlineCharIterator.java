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

package org.apache.fop.fo;

import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.util.CharUtilities;
import java.util.NoSuchElementException;

/**
 * A recursive char iterator that indicates boundaries by returning
 * an EOT char.
 */
public class InlineCharIterator extends RecursiveCharIterator {
    private boolean startBoundary = false;
    private boolean endBoundary = false;

    /**
     * @param fobj the object for whose character contents and for whose
     * descendant's character contents should be iterated
     * @param bpb the CommonBorderPaddingBackground properties to be applied
     */
    public InlineCharIterator(FObj fobj, CommonBorderPaddingBackground bpb) {
        super(fobj);
        checkBoundaries(bpb);
    }


    private void checkBoundaries(CommonBorderPaddingBackground bpb) {
        /* Current understanding is that an <fo:inline> is always a boundary for
         * whitespace collapse if it has a border or not
        startBoundary = (bpb.getBorderStartWidth(false) > 0
                       || bpb.getPaddingStart(false, null) > 0); // TODO do we need context here?
        endBoundary = (bpb.getBorderEndWidth(false) > 0
                     || bpb.getPaddingEnd(false, null) > 0); // TODO do we need context here?
         */
        startBoundary = true;
        endBoundary = true;
    }

    /**
     * @return true if there are more characters
     */
    public boolean hasNext() {
        if (startBoundary) {
            return true;
        }
        return (super.hasNext() || endBoundary);
        /* If super.hasNext() returns false,
         * we return true if we are going to return a "boundary" signal
         * else false.
         */
    }

    /**
     * @return the next character
     * @throws NoSuchElementException if there are no more characters
     */
    public char nextChar() throws NoSuchElementException {
        if (startBoundary) {
            startBoundary = false;
            return CharUtilities.CODE_EOT;
        }
        try {
            return super.nextChar();
        } catch (NoSuchElementException e) {
            // Underlying has nothing more to return
            // Check end boundary char
            if (endBoundary) {
                endBoundary = false;
                return CharUtilities.CODE_EOT;
            } else {
                throw e;
            }
        }
    }
}

