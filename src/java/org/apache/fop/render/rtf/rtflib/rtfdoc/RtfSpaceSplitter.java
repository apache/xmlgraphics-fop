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

package org.apache.fop.render.rtf.rtflib.rtfdoc;

/**
 * This class splits block attributes into space-before attribute, space-after
 * attribute and common attributes.
 */
public class RtfSpaceSplitter {

    /** Common attributes for all text. */
    private RtfAttributes commonAttributes;

    /** Space-before attributes of a block. */
    private int spaceBefore;

    /** Space-after attributes of a block. */
    private int spaceAfter;

    /** Indicate that we can update candidate for space-before. */
    private boolean updatingSpaceBefore;

    /** Candidate for adding space-before. */
    private RtfAttributes spaceBeforeCandidate;

    /** Candidate for adding space-before. */
    private RtfAttributes spaceAfterCandidate;

    /**
     * Create RtfSpaceSplitter with given RtfAttributes.
     * 
     * @param attrs  RtfAttributes for splitting
     * @param previousSpace  integer, representing accumulated spacing
     */
    public RtfSpaceSplitter(RtfAttributes attrs, int previousSpace) {
        commonAttributes = attrs;
        updatingSpaceBefore = true;
        spaceBeforeCandidate = null;
        spaceAfterCandidate = null;

        spaceBefore = split(RtfText.SPACE_BEFORE) + previousSpace;
        spaceAfter = split(RtfText.SPACE_AFTER);
    }

    /**
     * Remove attributes with name <code>key</code> from
     * <code>commonAttributes</code> and return it as int.
     * 
     * @param key  attributes name to extract
     * @return integer, representing value of extracted attributes
     */
    public int split(String key) {
        Integer i = (Integer) commonAttributes.getValue(key);
        if (i == null) {
            i = new Integer(0);
        }

        commonAttributes.unset(key);
        return i.intValue();
    }

    /** @return attributes, applicable to whole block. */
    public RtfAttributes getCommonAttributes() {
        return commonAttributes;
    }

    /** @return space-before value. */
    public int getSpaceBefore() {
        return spaceBefore;
    }

    /** 
     * Sets a candidate for space-before property.
     * 
     * @param candidate  instance of <code>RtfAttributes</code>, considered as 
     *        a candidate for space-before adding
     */
    public void setSpaceBeforeCandidate(RtfAttributes candidate) {
        if (updatingSpaceBefore) {
            this.spaceBeforeCandidate = candidate;
        }
    }

    /** 
     * Sets a candidate for space-after property.
     * 
     * @param candidate  instance of <code>RtfAttributes</code>, considered as 
     *        a candidate for space-after adding
     */
    public void setSpaceAfterCandidate(RtfAttributes candidate) {
        this.spaceAfterCandidate = candidate;
    }

    /** @return true, if candidate for space-before is set. */
    public boolean isBeforeCadidateSet() {
        return spaceBeforeCandidate != null;
    }

    /** @return true, if candidate for space-after is set. */
    public boolean isAfterCadidateSet() {
        return spaceAfterCandidate != null;
    }
    
    /**
     * Stops updating candidates for space-before attribute.
     */
    public void stopUpdatingSpaceBefore() {
        updatingSpaceBefore = false;
    } 

    /**
     * Adds corresponding attributes to their candidates.
     * 
     * @return integer, representing value of space-before/space-after 
     *   attributes, that can't be added anywhere (i.e. these attributes 
     *   hasn't their candidates)
     */
    public int flush() {
        int accumulatingSpace = 0;
        if (!isBeforeCadidateSet()) {
            accumulatingSpace += spaceBefore;
        } else {
            spaceBeforeCandidate.addIntegerValue(spaceBefore,
                    RtfText.SPACE_BEFORE);
        }

        if (!isAfterCadidateSet()) {
            accumulatingSpace += spaceAfter;
        } else {
            spaceAfterCandidate.addIntegerValue(spaceAfter, 
                            RtfText.SPACE_AFTER);
        }

        return accumulatingSpace;
    }
}
