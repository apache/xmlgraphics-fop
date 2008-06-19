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
import java.util.List;

import org.apache.fop.prototype.breaking.FeasibleBreaks.BestBreak;
import org.apache.fop.prototype.breaking.layout.Layout;
import org.apache.fop.prototype.breaking.layout.LineLayout;


/**
 * TODO javadoc
 */
public class ParagraphBreakHandler extends BlockLevelBreakHandler<LineLayout> {

    /**
     * @param pageDims
     */
    public ParagraphBreakHandler(List<PageDimensions> pageDims) {
        super(pageDims);
    }

    /** {@inheritDoc} */
    @Override
    protected double getDemerits(LineLayout layout) {
        return layout.getDemerits() + layout.getLineLayout().getDemerits();//TODO
    }

    /** {@inheritDoc} */
    @Override
    protected LineLayout createLayout(BestBreak<LineLayout> best, Collection<Layout> alternatives) {
        return LineLayout.createLayoutForNewPart(best.layout, best.demerits, alternatives);
    }

}
