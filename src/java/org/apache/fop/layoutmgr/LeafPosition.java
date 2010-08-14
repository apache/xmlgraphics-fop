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

/** A leaf position. */
public class LeafPosition extends Position {

    private int leafPos;

    /**
     * Construct a leaf position.
     * @param layoutManager the associated layout manager
     * @param pos the leaf position
     */
    public LeafPosition(LayoutManager layoutManager, int pos) {
        super(layoutManager);
        leafPos = pos;
    }

    /**
     * Construct a leaf position.
     * @param layoutManager the associated layout manager
     * @param pos the leaf position
     * @param index the index
     */
    public LeafPosition(LayoutManager layoutManager, int pos, int index) {
        super(layoutManager, index);
        leafPos = pos;
    }

    /** @return leaf position */
    public int getLeafPos() {
        return leafPos;
    }

    /** {@inheritDoc} */
    public boolean generatesAreas() {
        return getLM() != null;
    }

    /** {@inheritDoc} */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("LeafPos:").append(getIndex()).append("(");
        sb.append("pos=").append(getLeafPos());
        sb.append(", lm=").append(getShortLMName()).append(")");
        return sb.toString();
    }
}

