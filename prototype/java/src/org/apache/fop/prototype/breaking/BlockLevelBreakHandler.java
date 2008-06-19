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

import java.util.Iterator;
import java.util.List;

import org.apache.fop.prototype.breaking.layout.Layout;
import org.apache.fop.prototype.breaking.layout.ProgressInfo;
import org.apache.fop.prototype.knuth.Penalty;


/**
 * TODO javadoc
 */
abstract class BlockLevelBreakHandler<L extends Layout> extends LegalBreakHandler<L> {

    private List<PageDimensions> pageDims;

    public BlockLevelBreakHandler(List<PageDimensions> pageDims) {
        this.pageDims = pageDims;
    }

    /** {@inheritDoc} */
    @Override
    protected Iterator<Iterator<L>> getClassIter(ActiveLayouts<L> layouts) {
        return layouts.getBlockClassIterator();
    }

    /** {@inheritDoc} */
    @Override
    protected int computeDifference(L layout, Penalty p) {
        return pageDims.get(layout.getProgress().getPartNumber()).getBpd()
                - layout.getProgress().getTotalLength();
    }

    /** {@inheritDoc} */
    @Override
    protected ProgressInfo getProgress(L layout) {
        return layout.getProgress();
    }

}
