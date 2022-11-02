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

package org.apache.fop.traits;

/**
 * This interface provides read and assignment access to FO traits related to writing mode.
 */
public interface WritingModeTraitsSetter extends WritingModeTraitsGetter {

    /**
     * Set value of inline-progression-direction trait.
     * @param direction the "inline-progression-direction" trait
     */
    void setInlineProgressionDirection(Direction direction);

    /**
     * Set value of block-progression-direction trait.
     * @param direction the "block-progression-direction" trait
     */
    void setBlockProgressionDirection(Direction direction);

    /**
     * Set value of column-progression-direction trait.
     * @param direction the "column-progression-direction" trait
     */
    void setColumnProgressionDirection(Direction direction);

    /**
     * Set value of row-progression-direction trait.
     * @param direction the "row-progression-direction" trait
     */
    void setRowProgressionDirection(Direction direction);

    /**
     * Set value of shift-direction trait.
     * @param direction the "shift-direction" trait
     */
    void setShiftDirection(Direction direction);

    /**
     * Set value of writing-mode trait.
     * @param writingMode the "writing-mode" trait
     */
    void setWritingMode(WritingMode writingMode, boolean explicit);

    /**
     * Collectivelly assign values to all writing mode traits based upon a specific
     * writing mode.
     * @param writingMode the "writing-mode" trait
     * @param explicit true if writing mode explicitly specified
     */
    void assignWritingModeTraits(WritingMode writingMode, boolean explicit);

}
