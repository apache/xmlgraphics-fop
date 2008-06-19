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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.fop.prototype.breaking.LineBreakHandler;
import org.apache.fop.prototype.breaking.LineBreaker;
import org.apache.fop.prototype.breaking.PageBreakHandler;
import org.apache.fop.prototype.breaking.PageBreaker;
import org.apache.fop.prototype.breaking.PageDimensions;
import org.apache.fop.prototype.breaking.ParagraphBreakHandler;
import org.apache.fop.prototype.fo.Paragraph;
import org.apache.fop.prototype.font.Font;
import org.apache.fop.prototype.knuth.Glue;
import org.apache.fop.prototype.knuth.Penalty;


/**
 * A layout engine that typesets various kinds of typographical material.
 */
public class LayoutEngine {

    private PageBreakHandler pageBreakHandler;

    private ParagraphBreakHandler paragraphBreakHandler;

    private LineBreakHandler lineBreakHandler;

    public LayoutEngine(List<PageDimensions> pageDims) {
        pageBreakHandler = new PageBreakHandler(pageDims);
        paragraphBreakHandler = new ParagraphBreakHandler(pageDims);
        lineBreakHandler = new LineBreakHandler(pageDims);
    }

    public void typeset(List<? extends TypographicElement> content) {
        LineBreaker lineBreaker = new LineBreaker(lineBreakHandler, paragraphBreakHandler);
        PageBreaker pageBreaker = new PageBreaker(pageBreakHandler, lineBreaker);
        pageBreaker.findBreaks(content);
    }

    public static void main(String[] args) {
        List<TypographicElement> paragraphs = new LinkedList<TypographicElement>();
        paragraphs.add(new Paragraph(Arrays.asList(new String[] { "In", "olden", "times", "when",
                "wishing", "still", "helped", "one,", "there", "lived", "a", "king", "whose",
                "daughters", "were", "all", "beautiful,", "soooo", "much", "beautiful." }),
                Font.TIMES_FONT));
        paragraphs.add(Penalty.DEFAULT_PENALTY);
        paragraphs.add(new Glue(1, 1, 0));
        paragraphs.add(new Paragraph(Arrays.asList(new String[] { "In", "olden", "times", "when",
                "wishing", "still", "helped", "one,", "there", "lived", "a", "king", "whose",
                "daughters", "were", "all", "beautiful,", "but", "the", "youngest", "was", "so",
                "beautiful", "that", "the", "sun", "itself,", "which", "has", "seen", "so",
                "much,", "was", "astonished", "whenever", "it", "shone", "in", "her", "face." }),
                Font.TIMES_FONT));
//        paragraphs.add(Penalty.DEFAULT_PENALTY);
//        paragraphs.add(new Glue(2, 0, 1));
//        paragraphs.add(new Paragraph(Arrays.asList(new String[] { "And", "now", "I", "am", "about",
//                "to", "start", "the", "next", "paragraph", "and", "the", "goal", "is", "to",
//                "check", "that", "the", "algorithm", "is", "working", "properly,", "a", "thing",
//                "I", "am", "not", "quite", "sure", "of." }), Font.TIMES_FONT));
//        paragraphs.add(Penalty.DEFAULT_PENALTY);
//        paragraphs.add(new Glue(2, 0, 1));
//        paragraphs.add(new Paragraph(Arrays.asList(new String[] { "In", "olden", "times", "when",
//                "wishing", "still", "helped", "one,", "there", "lived", "a", "king", "whose",
//                "daughters", "were", "all", "beautiful,", "but", "the", "youngest", "was", "so",
//                "beautiful", "that", "the", "sun", "itself,", "which", "has", "seen", "so",
//                "much,", "was", "astonished", "whenever", "it", "shone", "in", "her", "face." }),
//                Font.TIMES_FONT));
        paragraphs.add(new Glue(0, 1000000, 0));
        paragraphs.add(new Penalty(0, -Penalty.INFINITE));

        List<PageDimensions> pageDims = new LinkedList<PageDimensions>();
        pageDims.add(new PageDimensions(13000, 8));
        pageDims.add(new PageDimensions(16000, 8));
        pageDims.add(new PageDimensions(13000, 8));
        pageDims.add(new PageDimensions(13000, 8));
        pageDims.add(new PageDimensions(13000, 8));

        new LayoutEngine(pageDims).typeset(paragraphs);
    }
}

