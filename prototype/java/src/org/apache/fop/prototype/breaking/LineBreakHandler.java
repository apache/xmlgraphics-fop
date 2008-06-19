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

package org.apache.fop.prototype.breaking;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.fop.prototype.breaking.FeasibleBreaks.BestBreak;
import org.apache.fop.prototype.breaking.layout.Layout;
import org.apache.fop.prototype.breaking.layout.LineLayout;
import org.apache.fop.prototype.breaking.layout.ProgressInfo;
import org.apache.fop.prototype.font.Font;
import org.apache.fop.prototype.knuth.KnuthElement;
import org.apache.fop.prototype.knuth.LineBox;
import org.apache.fop.prototype.knuth.Penalty;


/**
 * TODO javadoc
 */
public class LineBreakHandler extends LegalBreakHandler<LineLayout> {

    private List<PageDimensions> pageDims;

    public LineBreakHandler(List<PageDimensions> pageDims) {
        this.pageDims = pageDims;
    }

    /** {@inheritDoc} */
    @Override
    protected Iterator<Iterator<LineLayout>> getClassIter(ActiveLayouts<LineLayout> layouts) {
        return layouts.getLineClassIterator();
    }

    /** {@inheritDoc} */
    @Override
    protected double getThreshold() {
        return 7.6;
    }

    /** {@inheritDoc} */
    @Override
    protected int computeDifference(LineLayout layout, Penalty p) {
        return pageDims.get(layout.getProgress().getPartNumber()).getIpd()
                - layout.getLineLayout().getProgress().getTotalLength();
    }

    /** {@inheritDoc} */
    @Override
    protected ProgressInfo getProgress(LineLayout layout) {
        return layout.getLineLayout().getProgress();
    }

    /** {@inheritDoc} */
    @Override
    protected double getDemerits(LineLayout layout) {
        return layout.getLineLayout().getDemerits();
    }

    /** {@inheritDoc} */
    @Override
    protected LineLayout createLayout(BestBreak<LineLayout> best, Collection<Layout> alternatives) {
        List<KnuthElement> elements = new LinkedList<KnuthElement>(best.layout.getLineLayout()
                .getElements());
        LineBox lineBox = new LineBox(1/*TODO line bpd*/, elements, best.difference, Font.TIMES_FONT
                .getCharWidth(' ')/*TODO*/);
        LineLayout newLayout = LineLayout.createLayoutForNewLine(best.layout, best.demerits,
                alternatives);
        newLayout.addElement(lineBox);

        return newLayout;
    }

}
