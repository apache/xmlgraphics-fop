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

 * $Id: Span.java,v 1.8 2003/03/05 15:19:31 jeremias Exp $
 */ 
package org.apache.fop.area;

import java.io.Serializable;
import java.util.List;

import org.apache.fop.datastructs.Node;

/**
 * The span reference areas are children of the main-reference-area
 * of a region-body.
 * This is a reference area block area with 0 border and padding
 */
public class Span
extends AbstractReferenceArea
implements ReferenceArea, Serializable {
    // the list of flow reference areas in this span area
    private List flowAreas;
    
    private Integer cols;

    /**
     * Create a span area with the number of columns for this span area.
     *
     * @param cols the number of columns in the span
     */
    public Span(Node parent, Object sync, Integer cols) {
        super(parent, sync);
        this.cols = cols;
    }

    /**
     * Get the column count for this span area.
     *
     * @return the number of columns in this span area
     */
    public Integer getColumnCount() {
        return cols;
    }

}

