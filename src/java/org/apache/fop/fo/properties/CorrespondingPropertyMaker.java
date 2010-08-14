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

package org.apache.fop.fo.properties;

import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.expr.PropertyException;

/**
 * Maker class for handling corresponding properties.
 */
public class CorrespondingPropertyMaker {
    /** base property maker */
    protected PropertyMaker baseMaker;
    /** corresponding property for lr-tb writing mode */
    protected int lrtb;
    /** corresponding property for rl-tb writing mode */
    protected int rltb;
    /** corresponding property for tb-rl writing mode */
    protected int tbrl;
    /** user parent property list */
    protected boolean useParent;
    private boolean relative;

    /**
     * Construct a corresponding property maker.
     * @param baseMaker the base property maker
     */
    public CorrespondingPropertyMaker(PropertyMaker baseMaker) {
        this.baseMaker = baseMaker;
        baseMaker.setCorresponding(this);
    }


    /**
     * Set corresponding property identifiers.
     * @param lrtb the property that corresponds with lr-tb writing mode
     * @param rltb the property that corresponds with rl-tb writing mode
     * @param tbrl the property that corresponds with tb-lr writing mode
     */
    public void setCorresponding(int lrtb, int rltb, int tbrl) {
        this.lrtb = lrtb;
        this.rltb = rltb;
        this.tbrl = tbrl;
    }

    /**
     * Controls whether the PropertyMaker accesses the parent property list or the current
     * property list for determining the writing mode.
     * @param useParent true if the parent property list should be used.
     */
    public void setUseParent(boolean useParent) {
        this.useParent = useParent;
    }

    /**
     * Set relative flag.
     * @param relative true if relative direction
     */
    public void setRelative(boolean relative) {
        this.relative = relative;
    }

    /**
     * For properties that operate on a relative direction (before, after,
     * start, end) instead of an absolute direction (top, bottom, left,
     * right), this method determines whether a corresponding property
     * is specified on the corresponding absolute direction. For example,
     * the border-start-color property in a lr-tb writing-mode specifies
     * the same thing that the border-left-color property specifies. In this
     * example, if the Maker for the border-start-color property is testing,
     * and if the border-left-color is specified in the properties,
     * this method should return true.
     * @param propertyList collection of properties to be tested
     * @return true iff 1) the property operates on a relative direction,
     * AND 2) the property has a corresponding property on an absolute
     * direction, AND 3) the corresponding property on that absolute
     * direction has been specified in the input properties
     */
    public boolean isCorrespondingForced(PropertyList propertyList) {

        if (!relative) {
            return false;
        }

        PropertyList pList = getWMPropertyList(propertyList);
        if (pList != null) {
            int correspondingId = pList.getWritingMode(lrtb, rltb, tbrl);

            if (pList.getExplicit(correspondingId) != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return a Property object representing the value of this property,
     * based on other property values for this FO.
     * A special case is properties which inherit the specified value,
     * rather than the computed value.
     * @param propertyList The PropertyList for the FO.
     * @return Property A computed Property value or null if no rules
     * are specified (in foproperties.xml) to compute the value.
     * @throws PropertyException if a property exception occurs
     */
    public Property compute(PropertyList propertyList) throws PropertyException {
        PropertyList pList = getWMPropertyList(propertyList);
        if (pList == null) {
            return null;
        }
        int correspondingId = pList.getWritingMode(lrtb, rltb, tbrl);

        Property p = propertyList.getExplicitOrShorthand(correspondingId);
        if (p != null) {
            FObj parentFO = propertyList.getParentFObj();
            p = baseMaker.convertProperty(p, propertyList, parentFO);
        }
        return p;
    }

    /**
     * Return the property list to use for fetching writing mode depending property
     * ids.
     * @param pList a property list
     * @return the property list to use
     */
    protected PropertyList getWMPropertyList(PropertyList pList) {
        if (useParent) {
            return pList.getParentPropertyList();
        } else {
            return pList;
        }
    }
}

