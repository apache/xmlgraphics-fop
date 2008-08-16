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

import java.util.Arrays;
import java.util.Collection;

import org.apache.fop.prototype.breaking.ActiveLayouts;
import org.apache.fop.prototype.breaking.CompletedPart;
import org.apache.fop.prototype.breaking.KnuthIterator;
import org.apache.fop.prototype.breaking.LegalBreakHandler;
import org.apache.fop.prototype.breaking.layout.Layout;
import org.apache.fop.prototype.knuth.KnuthElement;

/**
 * TODO javadoc
 */
public class BlockLayoutManager extends AbstractLayoutManager<Layout> implements
        LayoutManager<Layout> {

    KnuthIterator<Layout> knuthIterator;

    public BlockLayoutManager(KnuthElement... content) {
        knuthIterator = new KnuthIterator<Layout>(Arrays.asList(content),
                new LegalBreakHandler<Layout>());
    }

    @Override
    public void initialize(NonLeafLayoutManager<Layout> parent, ActiveLayouts<Layout> previousLayouts) {
        this.parent = parent;
        this.layouts = previousLayouts;
        knuthIterator.initialize(previousLayouts);
    }

    @Override
    public boolean hasNext() {
        return knuthIterator.hasNext();
    }

    @Override
    public void next() {
        Collection<CompletedPart> parts = knuthIterator.next();
        if (parts != null) {
            for (CompletedPart part: parts) {
                Layout newLayout = parent.partCompleted(part);
                layouts.add(newLayout);
            }
        }
    }

}
