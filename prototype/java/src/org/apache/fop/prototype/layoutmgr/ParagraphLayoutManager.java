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

package org.apache.fop.prototype.layoutmgr;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.fop.prototype.Log;
import org.apache.fop.prototype.breaking.ActiveLayouts;
import org.apache.fop.prototype.breaking.Alternative;
import org.apache.fop.prototype.breaking.CompletedPart;
import org.apache.fop.prototype.breaking.KnuthIterator;
import org.apache.fop.prototype.breaking.LegalBreakHandler;
import org.apache.fop.prototype.breaking.layout.Layout;
import org.apache.fop.prototype.breaking.layout.LineLayout;
import org.apache.fop.prototype.fo.Paragraph;
import org.apache.fop.prototype.font.Font;
import org.apache.fop.prototype.knuth.KnuthElement;
import org.apache.fop.prototype.knuth.LineBox;
import org.apache.fop.prototype.knuth.Penalty;

/**
 * TODO javadoc
 */
public class ParagraphLayoutManager extends AbstractLayoutManager<Layout> implements
        LayoutManager<Layout> {

    private KnuthIterator<LineLayout> lineIterator;

    private ActiveLayouts<LineLayout> lineLayouts;

    private ActiveLayouts<Layout> newLayouts;

    private LegalBreakHandler<Layout> blockLevelBreakHandler;

    private boolean finalized;

    public ParagraphLayoutManager(Paragraph paragraph) {
        lineIterator = new KnuthIterator<LineLayout>(paragraph.getKnuthElements(),
                new LegalBreakHandler<LineLayout>(7.6));
        blockLevelBreakHandler = new LegalBreakHandler<Layout>();
    }

    /** {@inheritDoc} */
    @Override
    public void initialize(NonLeafLayoutManager<Layout> parent, ActiveLayouts<Layout> previousLayouts) {
        this.parent = parent;
        this.layouts = previousLayouts;
        lineLayouts = new ActiveLayouts<LineLayout>();
        for (Layout l: previousLayouts) {
            lineLayouts.add(new LineLayout(l));
        }
        lineIterator.initialize(lineLayouts);
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasNext() {
        if (lineIterator.hasNext()) {
            return true;
        } else if (!finalized) {
            finalized = true;
            layouts.clear();
            for (LineLayout l: lineLayouts) {
                layouts.add(l.getBlockLayout());
            }
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public void next() {
        Collection<CompletedPart> lines;
        do {
            lines = lineIterator.next();
        } while (lines.isEmpty());
        newLayouts = new ActiveLayouts<Layout>();
        for (CompletedPart line: lines) {
            LineLayout completedLayout = (LineLayout) line.getLayout(); // TODO cast
            Log.LOG.info("completed line:\n" + completedLayout);
            List<KnuthElement> elements = new ArrayList<KnuthElement>(completedLayout.getElements());
            LineBox lineBox = new LineBox(1000/* TODO line bpd */, elements, line.getDifference(),
                    Font.TIMES_FONT.getCharWidth(' ')/* TODO */);
            List<Alternative> alternatives = new ArrayList<Alternative>(line.getAlternatives().size());
            for (Alternative a: line.getAlternatives()) {
                alternatives.add(new Alternative(((LineLayout) a.getLayout()).getBlockLayout(),
                        a.getDemerits()));
            }
            Layout newBlockLayout = completedLayout.getBlockLayout().clone(alternatives);
            newBlockLayout.setPrevious(completedLayout.getBlockLayout());
            newBlockLayout.setDemerits(line.getDemerits());
            newBlockLayout.addElement(lineBox);
            newLayouts.add(newBlockLayout);
        }
        // Don't try to break if the end of the paragraph has been reached
        if (lineIterator.hasNext()) {
            Collection<CompletedPart> blocks = blockLevelBreakHandler.considerBreak(
                    Penalty.DEFAULT_PENALTY, newLayouts);
            for (CompletedPart block: blocks) {
                Layout newBlockLayout = parent.partCompleted(block);
                LineLayout newLineLayout = new LineLayout(newBlockLayout);
                lineLayouts.add(newLineLayout);
            }
        }
        for (Layout blockLayout: newLayouts) {
            lineLayouts.add(new LineLayout(blockLayout)); // TODO line number
        }
    }
}
