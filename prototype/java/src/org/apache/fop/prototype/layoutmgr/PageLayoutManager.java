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

import org.apache.fop.prototype.Dot;
import org.apache.fop.prototype.breaking.ActiveLayouts;
import org.apache.fop.prototype.breaking.Alternative;
import org.apache.fop.prototype.breaking.CompletedPart;
import org.apache.fop.prototype.breaking.PageDimensions;
import org.apache.fop.prototype.breaking.layout.BlockLayout;
import org.apache.fop.prototype.breaking.layout.Layout;

/**
 * TODO javadoc
 */
public class PageLayoutManager extends DefaultLayoutManager {

    private List<PageDimensions> pageDims;

    /**
     * @param childLMs
     */
    public PageLayoutManager(List<LayoutManager<Layout>> childLMs, final List<PageDimensions> pageDims) {
        super(childLMs);
        this.pageDims = pageDims;
        ActiveLayouts<Layout> layouts = new ActiveLayouts<Layout>();
        layouts.add(new BlockLayout(pageDims.get(0)));
        initialize(this, layouts);
    }

    /** {@inheritDoc} */
    @Override
    public Layout partCompleted(CompletedPart part) {
        Collection<Alternative> alts = part.getAlternatives();
        List<Alternative> alternatives = null;
        if (alts != null) { // TODO should this ever happen?
            alternatives = new ArrayList<Alternative>(alts.size());
            for (Alternative a: alts) {
                alternatives.add(new Alternative(a.getLayout().getPrevious(), a.getDemerits()));
            }
        }
        Layout previous = part.getLayout().clone(alternatives);
        previous.setPage();
        previous.setDemerits(part.getDemerits());
        int partNumber = previous.getProgress().getPartNumber() + 1;
        Layout newLayout = new BlockLayout(previous, partNumber, pageDims.get(partNumber));
        return newLayout;
    }

    public void findBreaks() {
        while (hasNext()) {
            next();
        }
        try {
            Dot.createGraph(layouts);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
