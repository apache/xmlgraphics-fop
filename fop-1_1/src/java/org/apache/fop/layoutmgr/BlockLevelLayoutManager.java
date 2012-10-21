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

import org.apache.fop.fo.properties.KeepProperty;

/**
 * The interface for LayoutManagers which generate block areas
 */
public interface BlockLevelLayoutManager extends LayoutManager {

    /**
     * Negotiate BPD adjustment.
     * @param adj amount to adjust
     * @param lastElement the last knuth element
     * @return the resulting adjusted BPD
     */
    int negotiateBPDAdjustment(int adj, KnuthElement lastElement);

    /**
     * Discard space.
     * @param spaceGlue the space
     */
    void discardSpace(KnuthGlue spaceGlue);

    /**
     * Returns the keep-together strength for this element.
     * @return the keep-together strength
     */
    Keep getKeepTogether();

    /**
     * @return true if this element must be kept together
     */
    boolean mustKeepTogether();

    /**
     * Returns the keep-with-previous strength for this element.
     * @return the keep-with-previous strength
     */
    Keep getKeepWithPrevious();

    /**
     * @return true if this element must be kept with the previous element.
     */
    boolean mustKeepWithPrevious();

    /**
     * Returns the keep-with-next strength for this element.
     * @return the keep-with-next strength
     */
    Keep getKeepWithNext();

    /**
     * @return true if this element must be kept with the next element.
     */
    boolean mustKeepWithNext();

    /**
     * Returns the keep-together property specified on the FObj.
     * @return the keep-together property
     */
    KeepProperty getKeepTogetherProperty();

    /**
     * Returns the keep-with-previous property specified on the FObj.
     * @return the keep-together property
     */
    KeepProperty getKeepWithPreviousProperty();

    /**
     * Returns the keep-with-next property specified on the FObj.
     * @return the keep-together property
     */
    KeepProperty getKeepWithNextProperty();
}
