/*
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id: MainReference.java,v 1.2 2004/02/27 17:41:26 jeremias Exp $ */

package org.apache.fop.area;

import java.util.List;
import java.util.Iterator;

/**
 * The main body reference area.
 * This area that contains the flow via the span areas.
 */
public class MainReference extends Area {
    private List spanAreas = new java.util.ArrayList();
    private int columnGap;
    private int width;
    private boolean isEmpty = true;

    /**
     * Constructor
     */
    public MainReference() {
        addTrait(Trait.IS_REFERENCE_AREA, Boolean.TRUE);
    }
      
    /**
     * Add a span area to this area.
     *
     * @param span the span area to add
     */
    public void addSpan(Span span) {
        spanAreas.add(span);
    }

    /**
     * Get the span areas from this area.
     *
     * @return the list of span areas
     */
    public List getSpans() {
        return spanAreas;
    }

    /**
     * indicates whether any child areas have been added to this reference area
     * this is achieved by looping through each span
     *
     * @return
     */
    public boolean isEmpty() {
        if (isEmpty) {
            int areaCount = 0;
            if (spanAreas != null) {
                for (Iterator spaniter = spanAreas.iterator(); spaniter.hasNext(); ) {
                    Span spanArea = (Span) spaniter.next();
                    for (int i = 0; i < spanArea.getColumnCount(); i++) {
                        Flow flow = spanArea.getFlow(i);
                        if (flow != null) {
                            if (flow.getChildAreas() != null) {
                                areaCount += flow.getChildAreas().size();
                            }
                        }
                    }
                }
            }

            isEmpty = (areaCount == 0);
        }
        return isEmpty;
    }

    /**
     * Get the column gap in millipoints.
     *
     * @return the column gap in millioints
     */
    public int getColumnGap() {
        return columnGap;
    }

    /**
     * Get the width of this reference area.
     *
     * @return the width
     */
    public int getWidth() {
        return width;
    }

}

