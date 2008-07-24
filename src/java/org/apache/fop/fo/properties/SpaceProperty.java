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

import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.expr.PropertyException;

/**
 * Base class used for handling properties of the fo:space-before and
 * fo:space-after variety. It is extended by org.apache.fop.fo.properties.GenericSpace,
 * which is extended by many other properties.
 */
public class SpaceProperty extends LengthRangeProperty {
    private Property precedence;
    private Property conditionality;

    /**
     * Inner class used to create new instances of SpaceProperty
     */
    public static class Maker extends CompoundPropertyMaker {

        /**
         * @param propId the id of the property for which a Maker should be created
         */
        public Maker(int propId) {
            super(propId);
        }

        /**
         * Create a new empty instance of SpaceProperty.
         * @return the new instance.
         */
        public Property makeNewProperty() {
            return new SpaceProperty();
        }

        /**
         * {@inheritDoc}
         */
        public Property convertProperty(Property p,
                                        PropertyList propertyList,
                                        FObj fo) throws PropertyException {
            if (p instanceof SpaceProperty) {
                return p;
            }
            return super.convertProperty(p, propertyList, fo);
        }
    }



    /**
     * {@inheritDoc}
     */
    public void setComponent(int cmpId, Property cmpnValue,
                             boolean bIsDefault) {
        if (cmpId == CP_PRECEDENCE) {
            setPrecedence(cmpnValue, bIsDefault);
        } else if (cmpId == CP_CONDITIONALITY) {
            setConditionality(cmpnValue, bIsDefault);
        } else {
            super.setComponent(cmpId, cmpnValue, bIsDefault);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Property getComponent(int cmpId) {
        if (cmpId == CP_PRECEDENCE) {
            return getPrecedence();
        } else if (cmpId == CP_CONDITIONALITY) {
            return getConditionality();
        } else {
            return super.getComponent(cmpId);
        }
    }

    /**
     *
     * @param precedence precedence Property to set
     * @param bIsDefault (is not used anywhere)
     */
    protected void setPrecedence(Property precedence, boolean bIsDefault) {
        this.precedence = precedence;
    }

    /**
     *
     * @param conditionality conditionality Property to set
     * @param bIsDefault (is not used anywhere)
     */
    protected void setConditionality(Property conditionality,
                                     boolean bIsDefault) {
        this.conditionality = conditionality;
    }

    /**
     * @return precedence Property
     */
    public Property getPrecedence() {
        return this.precedence;
    }

    /**
     * @return conditionality Property
     */
    public Property getConditionality() {
        return this.conditionality;
    }

    /**
     * Indicates if the length can be discarded on certain conditions.
     * @return true if the length can be discarded.
     */
    public boolean isDiscard() {
        return this.conditionality.getEnum() == Constants.EN_DISCARD;
    }

    public String toString() {
        return "Space[" +
        "min:" + getMinimum(null).getObject() +
        ", max:" + getMaximum(null).getObject() +
        ", opt:" + getOptimum(null).getObject() +
        ", precedence:" + precedence.getObject() +
        ", conditionality:" + conditionality.getObject() + "]";
    }

    /**
     * @return the Space (datatype) object contained here
     */
    public SpaceProperty getSpace() {
        return this;
    }

    /**
     * Space extends LengthRange.
     * @return the Space (datatype) object contained here
     */
    public LengthRangeProperty getLengthRange() {
        return this;
    }

    /**
     * @return the Space (datatype) object contained here
     */
    public Object getObject() {
        return this;
    }

}
