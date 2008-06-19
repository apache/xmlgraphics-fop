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

import org.apache.fop.prototype.Dot;
import org.apache.fop.prototype.TypographicElement;
import org.apache.fop.prototype.breaking.layout.Layout;
import org.apache.fop.prototype.breaking.layout.LineLayout;
import org.apache.fop.prototype.fo.Paragraph;
import org.apache.fop.prototype.knuth.Penalty;

/**
 * A breaker that builds pages.
 */
public class PageBreaker extends BlockLevelBreaker<Layout> {

    private PageBreakHandler pageBreakHandler;

    private LineBreaker lineBreaker;

    /**
     * @param pageBreakHandler
     * @param lineBreaker
     */
    public PageBreaker(PageBreakHandler pageBreakHandler, LineBreaker lineBreaker) {
        this.pageBreakHandler = pageBreakHandler;
        this.lineBreaker = lineBreaker;
        this.lineBreaker.setParentBreaker(this);
    }

    /** {@inheritDoc} */
    @Override
    protected void initBreaking() {
        layouts = new ActiveLayouts<Layout>();
        layouts.add(new Layout());
    }

    /** {@inheritDoc} */
    @Override
    protected void considerLegalBreak(Penalty p) {
        pageBreakHandler.considerBreak(p, layouts);
    }

    /** {@inheritDoc} */
    @Override
    void newLayoutsFound(LineBreaker breaker, ActiveLayouts<LineLayout> newLayouts) {
        newLayouts = breaker.getPageBreakHandler().considerBreak(
                Penalty.DEFAULT_PENALTY, newLayouts);
        breaker.integrateBlockLayouts(newLayouts);
    }

    /** {@inheritDoc} */
    @Override
    protected void handleElement(TypographicElement e) {
        if (e instanceof Paragraph) {
            ActiveLayouts<LineLayout> lineLayouts = new ActiveLayouts<LineLayout>();
            for (Layout l: layouts) {
                lineLayouts.add(new LineLayout(l));
            }
            lineBreaker.setLayouts(lineLayouts);
            lineBreaker.findBreaks(((Paragraph) e).getKnuthElements());
            layouts.setLayoutsFrom(lineLayouts);
        }
    }

    /** Creates a graph of all the layouts created by this breaker, in PDF format. */
    @Override
    protected void endBreaking() {
        try {
            Dot.createGraph(layouts);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
