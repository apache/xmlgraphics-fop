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

package org.apache.fop.layoutmgr.inline;

import java.util.List;

import org.apache.fop.layoutmgr.LayoutManager;
import org.apache.fop.layoutmgr.Position;

/**
 * The interface for LayoutManagers which generate inline areas
 */
public interface InlineLevelLayoutManager extends LayoutManager {

    /**
     * Tell the LM to modify its data, adding a letter space
     * to the word fragment represented by the given elements,
     * and returning the corrected elements
     *
     * @param oldList the elements which must be given one more letter space
     * @return        the new elements replacing the old ones
     */
    List addALetterSpaceTo(List oldList);

    /**
     * Tell the LM to modify its data, removing the word space
     * represented by the given elements
     *
     * @param oldList the elements representing the word space
     */
    void removeWordSpace(List oldList);

    /**
     * Get the word chars corresponding to the given position
     *
     * @param sbChars the StringBuffer used to append word chars
     * @param pos     the Position referring to the needed word chars
     */
    void getWordChars(StringBuffer sbChars, Position pos);

    /**
     * Tell the LM to hyphenate a word
     *
     * @param pos the Position referring to the word
     * @param hc  the HyphContext storing hyphenation information
     */
    void hyphenate(Position pos, HyphContext hc);

    /**
     * Tell the LM to apply the changes due to hyphenation
     *
     * @param oldList the list of the old elements the changes refer to
     * @return        true if the LM had to change its data, false otherwise
     */
    boolean applyChanges(List oldList);

}
