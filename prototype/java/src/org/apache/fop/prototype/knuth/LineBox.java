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

package org.apache.fop.prototype.knuth;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * A box representing a line of a paragraph.
 */
public class LineBox extends KnuthElement {

    private List<KnuthElement> elements;

    private int difference;

    private int spaceCharWidth;

    public LineBox(int length, List<KnuthElement> elements, int difference, int spaceCharWidth) {
        super(length);
        this.elements = elements;
        this.difference = difference;
        this.spaceCharWidth = spaceCharWidth;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isBox() {
        return true;
    }

    private void createLabel(StringBuilder label, Iterator<KnuthElement> iter, int spaceNumber, int spaceSize) {
        if (spaceNumber > 0) {
            char[] space = new char[spaceSize];
            Arrays.fill(space, ' ');
            do {
                KnuthElement e = iter.next();
                label.append(e.getLabel());
                if (e.isGlue()) {
                    label.append(space);
                    spaceNumber--;
                }
            } while (spaceNumber > 0);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getLabel() {
        StringBuilder label = new StringBuilder();
        if (difference > 0) {
            int nbGlues = 0;
            for (KnuthElement e: elements) {
                if (e.isGlue()) {
                    nbGlues++;
                }
            }
            if (nbGlues > 1) {
                int nbSpaceChars = difference / spaceCharWidth;
                int q = nbSpaceChars / nbGlues;
                int r = nbSpaceChars % nbGlues;
                Iterator<KnuthElement> iter = elements.iterator();
                createLabel(label, iter, nbGlues - r, q);
                createLabel(label, iter, r, q + 1);
                do {
                    label.append(iter.next().getLabel());
                } while (iter.hasNext());
            }
        } else {
            for (KnuthElement e: elements) {
                label.append(e.getLabel());
            }
        }
        return label.toString();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (KnuthElement e: elements) {
            str.append(e.getLabel());
        }
        str.append("\\n");
        return str.toString();
    }
}
