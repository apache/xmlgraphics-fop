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

import java.util.Iterator;
import java.util.List;

import org.apache.fop.prototype.breaking.ActiveLayouts;
import org.apache.fop.prototype.breaking.CompletedPart;
import org.apache.fop.prototype.breaking.layout.Layout;

/**
 * TODO javadoc
 */
public class DefaultLayoutManager extends AbstractLayoutManager<Layout> implements
        NonLeafLayoutManager<Layout> {

    Iterator<LayoutManager<Layout>> childLMIter;

    LayoutManager<Layout> currentChildLM;

    protected DefaultLayoutManager() { }

    /**
     * @param childLMs
     */
    public DefaultLayoutManager(List<LayoutManager<Layout>> childLMs) {
        childLMIter = childLMs.iterator();
    }

    /** {@inheritDoc} */
    @Override
    public void initialize(NonLeafLayoutManager<Layout> parent, ActiveLayouts<Layout> previousLayouts) {
        this.parent = parent;
        this.layouts = previousLayouts;
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasNext() {
        if (currentChildLM != null && currentChildLM.hasNext()) {
            return true;
        } else if (!childLMIter.hasNext()) {
            return false;
        } else {
            currentChildLM = childLMIter.next();
            currentChildLM.initialize(this, layouts);
            return true;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void next() {
        assert hasNext();
        currentChildLM.next();
    }

    @Override
    public Layout partCompleted(CompletedPart part) {
        return parent.partCompleted(part);
    }

}