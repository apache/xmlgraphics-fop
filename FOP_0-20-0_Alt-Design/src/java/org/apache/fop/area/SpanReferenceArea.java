/*
   Copyright 1999-2004 The Apache Software Foundation.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 * $Id$
 */ 
package org.apache.fop.area;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.fop.apps.FOPException;
import org.apache.fop.datastructs.Node;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.flow.FoPageSequence;

/**
 * The span reference areas are children of the main-reference-area
 * of a region-body.
 * This is a reference area block area with 0 border and padding
 */
public class SpanReferenceArea
extends BlockReferenceArea
implements ReferenceArea, Serializable {
    
    // the list of normal-flow-reference-areas in this span area
    private List flowAreas = new ArrayList();
    
    private NormalFlowRefArea currentFlowArea = null;
    
    /**
     * Number of columns in this span.  Derived from the <code>span</code>
     * property on the fo:flow and the column-count prooperty on the
     * region-body-reference-area.  Defaults to 1.
     */
    private int columnCount = 1;

    /**
     * Create a span area with the number of columns for .
     * SpanReferenceArea-reference-areas are children of main-reference-areas.
     */
    public SpanReferenceArea(
            FoPageSequence pageSeq,
            FONode generatedBy,
            Node parent,
            Object sync) {
        super(pageSeq, generatedBy, parent, sync);
    }

    /**
     * Create a span area with the number of columns for .
     * SpanReferenceArea-reference-areas are children of main-reference-areas.
     */
    public SpanReferenceArea(
            int columnCount,
            FoPageSequence pageSeq,
            FONode generatedBy,
            Node parent,
            Object sync) {
        super(pageSeq, generatedBy, parent, sync);
        this.columnCount =  columnCount;
    }

    /**
     * Creates and returns a <code>SpanReferenceArea</code> with no rectangular
     * area and only the default column count.  The span created references a
     * null <code>NormalFlowRefArea</code>.  <b>N.B.</b> this is a
     * <code>static</code> method.
     * @param pageSeq the <code>page-sequence</code> to which this area belongs
     * @param generatedBy the node which generated this span; in this case, the
     * <code>page-sequence</code>
     * @param parent the <code>main-reference-area</code>
     * @param sync
     * @return the created reference area
     */
    public static SpanReferenceArea nullSpanArea(
            FoPageSequence pageSeq, FONode generatedBy,
            Node parent, Object sync) {
        SpanReferenceArea span = new SpanReferenceArea(
                pageSeq, generatedBy, parent, sync);
        span.addNormalFlowRef(
                new NormalFlowRefArea(pageSeq, generatedBy, span, sync));
        return span;
    }

    /**
     * @return the column count
     */
    public int getColumnCount() {
        synchronized (sync) {
            return columnCount;
        }
    }
    /**
     * Set spanning condition, only if no main-reference-area exists
     */
    public void setColumnCount(int columnCount) throws FOPException {
        if (flowAreas == null) {
            this.columnCount = columnCount;
        }
        else {
            throw new FOPException("normal-flow-reference-areas exist");
        }
    }
    
    public boolean activeFlowRefAreas() {
        if (flowAreas == null && currentFlowArea == null) {
            return false;
        }
        return true;
    }

    public void addNormalFlowRef(NormalFlowRefArea normal) {
        flowAreas.add(normal);
        currentFlowArea = normal;
    }

    public NormalFlowRefArea getCurrNormalFlowRefArea() {
        return currentFlowArea;
    }

}

