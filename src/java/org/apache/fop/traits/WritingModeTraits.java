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
 * This class provides a reusable implementation of the WritingModeTraitsSetter
 * interface.
 */
public class WritingModeTraits implements WritingModeTraitsSetter {

    private Direction inlineProgressionDirection;
    private Direction blockProgressionDirection;
    private Direction columnProgressionDirection;
    private Direction rowProgressionDirection;
    private Direction shiftDirection;
    private WritingMode writingMode;

    /**
     * Default writing mode traits constructor.
     */
    public WritingModeTraits() {
        this (WritingMode.LR_TB);
    }

    /**
     * Construct writing mode traits using the specified writing mode.
     * @param writingMode a writing mode traits object
     */
    public WritingModeTraits (WritingMode writingMode) {
        assignWritingModeTraits (writingMode);
    }

    /**
     * @return the "inline-progression-direction" trait.
     */
    public Direction getInlineProgressionDirection() {
        return inlineProgressionDirection;
    }

    /**
     * @param direction the "inline-progression-direction" trait.
     */
    public void setInlineProgressionDirection (Direction direction) {
        this.inlineProgressionDirection = direction;
    }

    /**
     * @return the "block-progression-direction" trait.
     */
    public Direction getBlockProgressionDirection() {
        return blockProgressionDirection;
    }

    /**
     * @param direction the "block-progression-direction" trait.
     */
    public void setBlockProgressionDirection (Direction direction) {
        this.blockProgressionDirection = direction;
    }

    /**
     * @return the "column-progression-direction" trait.
     */
    public Direction getColumnProgressionDirection() {
        return columnProgressionDirection;
    }

    /**
     * @param direction the "column-progression-direction" trait.
     */
    public void setColumnProgressionDirection (Direction direction) {
        this.columnProgressionDirection = direction;
    }

    /**
     * @return the "row-progression-direction" trait.
     */
    public Direction getRowProgressionDirection() {
        return rowProgressionDirection;
    }

    /**
     * @param direction the "row-progression-direction" trait.
     */
    public void setRowProgressionDirection (Direction direction) {
        this.rowProgressionDirection = direction;
    }

    /**
     * @return the "shift-direction" trait.
     */
    public Direction getShiftDirection() {
        return shiftDirection;
    }

    /**
     * @param direction the "shift-direction" trait.
     */
    public void setShiftDirection (Direction direction) {
        this.shiftDirection = direction;
    }

    /**
     * @return the "writing-mode" trait.
     */
    public WritingMode getWritingMode() {
        return writingMode;
    }

    /**
     * @param writingMode the "writing-mode" trait.
     */
    public void setWritingMode (WritingMode writingMode) {
        this.writingMode = writingMode;
    }

    /**
     * @param writingMode the "writing-mode" trait.
     */
    public void assignWritingModeTraits (WritingMode writingMode) {
        writingMode.assignWritingModeTraits (this);
    }

    /**
     * Helper function to find the writing mode traits getter (if any) that applies for
     * a given FO node.
     * @param fn the node to start searching from
     * @return the applicable writing mode traits getter, or null if none applies
     */
    public static WritingModeTraitsGetter
        getWritingModeTraitsGetter (org.apache.fop.fo.FONode fn) {
        for (org.apache.fop.fo.FONode n = fn; n != null; n = n.getParent()) {
            if (n instanceof WritingModeTraitsGetter) {
                return (WritingModeTraitsGetter) n;
            }
        }
        return null;
    }

}
