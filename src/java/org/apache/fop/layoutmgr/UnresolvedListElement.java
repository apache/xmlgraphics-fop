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

package org.apache.fop.layoutmgr;

/**
 * This class represents an unresolved list element.
 */
public abstract class UnresolvedListElement extends ListElement {

    /**
     * Main constructor
     * @param position the Position instance needed by the addAreas stage of the LMs.
     */
    public UnresolvedListElement(Position position) {
        super(position);
    }
    
    /** @return true if the element is conditional (conditionality="discard") */
    public abstract boolean isConditional();
    
    /** @return the layout manager that created this ListElement */
    protected LayoutManager getOriginatingLayoutManager() {
        Position pos = getPosition();
        while (pos instanceof NonLeafPosition && pos.getPosition() != null) {
            pos = pos.getPosition();
        }
        return pos.getLM();
    }
    
}
