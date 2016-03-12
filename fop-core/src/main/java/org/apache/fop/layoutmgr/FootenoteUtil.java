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

package org.apache.fop.layoutmgr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import org.apache.fop.layoutmgr.inline.KnuthInlineBox;

public final class FootenoteUtil {

    private FootenoteUtil() {
    }

    /**
     * Returns the footnotes contained in the given element list.
     */
    public static List<FootnoteBodyLayoutManager> getFootnotes(List<ListElement> elemenList) {
        return getFootnotes(elemenList, 0, elemenList.size() - 1);
    }

    /**
     * Returns the footnotes contained in the given element list.
     *
     * @param startIndex index in the element list from which to start the scan, inclusive
     * @param endIndex index in the element list at which to stop the scan, inclusive
     */
    public static List<FootnoteBodyLayoutManager> getFootnotes(
            List<ListElement> elemenList, int startIndex, int endIndex) {
        ListIterator<ListElement> iter = elemenList.listIterator(startIndex);
        List<FootnoteBodyLayoutManager> footnotes = null;
        while (iter.nextIndex() <= endIndex) {
            ListElement element = iter.next();
            if (element instanceof KnuthInlineBox && ((KnuthInlineBox) element).isAnchor()) {
                footnotes = getFootnoteList(footnotes);
                footnotes.add(((KnuthInlineBox) element).getFootnoteBodyLM());
            } else if (element instanceof KnuthBlockBox && ((KnuthBlockBox) element).hasAnchors()) {
                footnotes = getFootnoteList(footnotes);
                footnotes.addAll(((KnuthBlockBox) element).getFootnoteBodyLMs());
            }
        }
        if (footnotes == null) {
            return Collections.emptyList();
        } else {
            return footnotes;
        }
    }

    private static <T> List<T> getFootnoteList(List<T> footnotes) {
        if (footnotes == null) {
            return new ArrayList<T>();
        } else {
            return footnotes;
        }
    }

}
