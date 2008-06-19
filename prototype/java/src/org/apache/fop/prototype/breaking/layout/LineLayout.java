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

package org.apache.fop.prototype.breaking.layout;

import java.util.Collection;

import org.apache.fop.prototype.knuth.KnuthElement;


/**
 * TODO javadoc
 */
public class LineLayout extends Layout {

    private Layout lineLayout;

    private LineLayout() { }

    /**
     * Creates an empty layout starting on line 0.
     * 
     * @param blockLayout the block-level layout preceding this one. That is, the layout
     * containing the elements preceding this line-level element.
     */
    public LineLayout(Layout blockLayout) {
        super(blockLayout);
        previous = blockLayout;
        lineLayout = new Layout();
    }

    public static LineLayout createLayoutForNewLine(LineLayout previous, double demerits,
            Collection<Layout> alternatives) {
        LineLayout l = new LineLayout(previous);
        l.lineLayout = Layout.createLayoutForNewPart(previous, demerits, null);
        l.alternatives = alternatives;
        return l;
    }

    public static LineLayout createLayoutForNewPart(LineLayout previous, double demerits,
            Collection<Layout> alternatives) {
        LineLayout l = new LineLayout();
        l.previous = previous;
        l.demerits = demerits;
        l.progress.setPartNumber(previous.progress.getPartNumber() + 1);
        l.alternatives = alternatives;
        l.lineLayout = previous.lineLayout.copy();
        return l;
    }

    public Layout getLineLayout() {
        return lineLayout;
    }

    String getLineLabel() {
        StringBuilder label = new StringBuilder();
        label.append("[ ");
        for (KnuthElement e: lineLayout.getElements()) {
            label.append(e.toString());
        }
        label.append(" ]");
        return label.toString();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder(super.toString());
        s.append('[');
        for (KnuthElement e: lineLayout.getElements()) {
            s.append(e.getLabel());
        }
        s.append(']');
        return s.toString();
    }

}
