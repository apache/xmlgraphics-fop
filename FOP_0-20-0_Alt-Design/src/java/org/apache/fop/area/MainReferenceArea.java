/*
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * $Id$
 */ 
package org.apache.fop.area;

import java.util.ArrayList;
import java.util.List;

import org.apache.fop.datastructs.Node;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.flow.FoPageSequence;

/**
 * The main body reference area.
 * This is the primary child of the region-body-reference-area.
 * The complementary children are the optional
 * before-float-reference-area and footnote-reference-area.
 * The children of this area are span-reference-areas.
 */
public class MainReferenceArea
extends BlockReferenceArea
implements ReferenceArea {
    private List spanAreas = new ArrayList();
    private SpanReferenceArea currentSpan = null;
    /**
     * @param parent
     * @param sync
     * @throws IndexOutOfBoundsException
     */
    public MainReferenceArea(
            FoPageSequence pageSeq,
            FONode generatedBy,
            Node parent,
            Object sync) {
        super(pageSeq, generatedBy, parent, sync);
    }


    /**
     * Creates and returns a <code>MainReferenceArea</code> with no rectangular
     * area. The span created references a null <code>NormalFlowRefArea</code>.
     * <b>N.B.</b> this is a <code>static</code> method.
     * @param pageSeq the <code>page-sequence</code> to which this area belongs
     * @param generatedBy the node which generated this reference area; in this
     * case, the <code>page-sequence</code>
     * @param parent the <code>region-body-reference-area</code>
     * @param sync
     * @return the created reference area
     */
    public static MainReferenceArea nullMainRefArea(
            FoPageSequence pageSeq, FONode generatedBy,
            Node parent, Object sync) {
        MainReferenceArea main =
            new MainReferenceArea(pageSeq, generatedBy, parent, sync);
        SpanReferenceArea span =
            SpanReferenceArea.nullSpanArea(pageSeq, generatedBy, main, sync);
        return main;
    }
    /**
     * Add a span area to this area.
     *
     * @param span the span area to add
     */
    public void addSpan(SpanReferenceArea span) {
        spanAreas.add(span);
        currentSpan = span;
    }

    public SpanReferenceArea getCurrSpanRefArea() {
        return currentSpan;
    }
    /**
     * Get the span areas from this area.
     *
     * @return the list of span areas
     */
    public List getSpans() {
        return spanAreas;
    }

}

