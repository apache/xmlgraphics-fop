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

package org.apache.fop.prototype;

import java.util.LinkedList;
import java.util.List;

import org.apache.fop.prototype.breaking.PageDimensions;
import org.apache.fop.prototype.breaking.layout.Layout;
import org.apache.fop.prototype.fo.Paragraph;
import org.apache.fop.prototype.font.Font;
import org.apache.fop.prototype.knuth.Box;
import org.apache.fop.prototype.knuth.Glue;
import org.apache.fop.prototype.knuth.Penalty;
import org.apache.fop.prototype.layoutmgr.BlockLayoutManager;
import org.apache.fop.prototype.layoutmgr.LayoutManager;
import org.apache.fop.prototype.layoutmgr.PageLayoutManager;
import org.apache.fop.prototype.layoutmgr.ParagraphLayoutManager;


/**
 * A layout engine that typesets various kinds of typographical material.
 */
public class LayoutEngine {

    @SuppressWarnings("unused")
    private static void addSimpleContent(List<PageDimensions> pageDims,
            List<LayoutManager<Layout>> layoutManagers) {
        pageDims.add(new PageDimensions(13000, 8000));
        pageDims.add(new PageDimensions(16000, 8000));
        pageDims.add(new PageDimensions(13000, 8000));
        pageDims.add(new PageDimensions(16000, 8000));
        layoutManagers.add(new ParagraphLayoutManager(new Paragraph(Font.TIMES_FONT, "In", "olden",
                "times", "when", "wishing", "still", "helped", "one,", "there", "lived", "a",
                "king", "whose", "daughters", "were", "all", "beautiful,", "soooo", "much",
                "beautiful.")));
        layoutManagers.add(new BlockLayoutManager(
                Penalty.DEFAULT_PENALTY, new Glue(1000, 1000, 0)));
        layoutManagers.add(new ParagraphLayoutManager(new Paragraph(Font.TIMES_FONT, "In", "olden",
                "times", "when", "wishing", "still", "helped", "one,", "there", "lived", "a",
                "king", "whose", "daughters", "were", "all", "beautiful,", "but", "the",
                "youngest", "was", "so", "beautiful", "that", "the", "sun", "itself,", "which",
                "has", "seen", "so", "much,", "was", "astonished", "whenever", "it", "shone", "in",
                "her", "face.")));
//        layoutManagers.add(new BlockLayoutManager(
//                Penalty.DEFAULT_PENALTY, new Glue(2000, 0, 1000)));
//        layoutManagers.add(new ParagraphLayoutManager(new Paragraph(Font.TIMES_FONT, "And", "now",
//                "I", "am", "about", "to", "start", "the", "next", "paragraph", "and", "the",
//                "goal", "is", "to", "check", "that", "the", "algorithm", "is", "working",
//                "properly,", "a", "thing", "I", "am", "not", "quite", "sure", "of.")));
//        layoutManagers.add(new BlockLayoutManager(
//                Penalty.DEFAULT_PENALTY, new Glue(2000, 0, 1000)));
//        layoutManagers.add(new ParagraphLayoutManager(new Paragraph(Font.TIMES_FONT, "In", "olden",
//                "times", "when", "wishing", "still", "helped", "one,", "there", "lived", "a",
//                "king", "whose", "daughters", "were", "all", "beautiful,", "but", "the",
//                "youngest", "was", "so", "beautiful", "that", "the", "sun", "itself,", "which",
//                "has", "seen", "so", "much,", "was", "astonished", "whenever", "it", "shone", "in",
//                "her", "face.")));
    }


    @SuppressWarnings("unused")
    private static void addContentWithBlock(List<PageDimensions> pageDims,
            List<LayoutManager<Layout>> layoutManagers) {
        pageDims.add(new PageDimensions(13000, 8000));
        pageDims.add(new PageDimensions(16000, 8000));
        pageDims.add(new PageDimensions(13000, 8000));
        pageDims.add(new PageDimensions(16000, 8000));
        layoutManagers.add(new ParagraphLayoutManager(new Paragraph(Font.TIMES_FONT, "In", "olden",
                "times", "when", "wishing", "still", "helped", "one,", "there", "lived", "a",
                "king", "whose", "daughters", "were", "all", "beautiful,", "soooo", "much",
                "beautiful.")));
        layoutManagers.add(new BlockLayoutManager(Penalty.DEFAULT_PENALTY, new Glue(1000, 3000, 0),
                new Box(1000, "           BLOCK"), Penalty.DEFAULT_PENALTY,
                new Box(1000, "           BLOCK"), Penalty.DEFAULT_PENALTY,
                new Box(1000, "           BLOCK"), Penalty.DEFAULT_PENALTY,
                new Box(1000, "           BLOCK"), Penalty.DEFAULT_PENALTY,
                new Box(1000, "           BLOCK"), Penalty.DEFAULT_PENALTY));
        layoutManagers.add(new ParagraphLayoutManager(new Paragraph(Font.TIMES_FONT, "In", "olden",
                "times", "when", "wishing", "still", "helped", "one,", "there", "lived", "a",
                "king", "whose", "daughters", "were", "all", "beautiful,", "but", "the",
                "youngest", "was", "so", "beautiful", "that", "the", "sun", "itself,", "which",
                "has", "seen", "so", "much,", "was", "astonished", "whenever", "it", "shone", "in",
                "her", "face.")));
    }

    public static void main(String[] args) {
        List<PageDimensions> pageDims = new LinkedList<PageDimensions>();
        List<LayoutManager<Layout>> layoutManagers = new LinkedList<LayoutManager<Layout>>();
        addSimpleContent(pageDims, layoutManagers);
//        addContentWithBlock(pageDims, layoutManagers);
        layoutManagers.add(new BlockLayoutManager(new Glue(0, 1000000, 0),
                new Penalty(0, -Penalty.INFINITE)));

        new PageLayoutManager(layoutManagers, pageDims).findBreaks();
    }
}
