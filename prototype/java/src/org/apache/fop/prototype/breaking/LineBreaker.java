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

import org.apache.fop.prototype.breaking.layout.Layout;
import org.apache.fop.prototype.breaking.layout.LineLayout;
import org.apache.fop.prototype.knuth.Penalty;

/**
 * A breaker for paragraphs.
 */
public class LineBreaker extends Breaker<LineLayout> {

    private LineBreakHandler lineBreakHandler;

    private BlockLevelBreakHandler<LineLayout> pageBreakHandler;


    /**
     * @param lineBreakHandler
     * @param pageBreakHandler
     */
    public LineBreaker(LineBreakHandler lineBreakHandler, BlockLevelBreakHandler<LineLayout> pageBreakHandler) {
        this.lineBreakHandler = lineBreakHandler;
        this.pageBreakHandler = pageBreakHandler;
    }

    protected BlockLevelBreakHandler<LineLayout> getPageBreakHandler() {
        return pageBreakHandler;
    }

    void setLayouts(ActiveLayouts<LineLayout> layouts) {
        this.layouts = layouts;
    }

    void integrateBlockLayouts(ActiveLayouts<LineLayout> blockLayouts) {
        for (LineLayout l: blockLayouts) {
            layouts.add(l);
        }
    }

    /** {@inheritDoc} */
    @Override
    Layout getLayout(LineLayout layout) {
        return layout.getLineLayout();
    }

    /** {@inheritDoc} */
    @Override
    protected void considerLegalBreak(Penalty p) {
        ActiveLayouts<LineLayout> newLayouts = lineBreakHandler.considerBreak(p, layouts);
        if (!newLayouts.isEmpty()) {
            getParentBreaker().newLayoutsFound(this, newLayouts);
        }
    }

}
