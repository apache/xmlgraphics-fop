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

import org.apache.fop.datatypes.CompoundDatatype;
import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.PercentBaseContext;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.expr.PropertyException;

/**
 * Superclass for properties that contain LengthRange values
 */
public class LengthRangeProperty extends Property implements CompoundDatatype {
    private Property minimum;
    private Property optimum;
    private Property maximum;
    private static final int MINSET = 1;
    private static final int OPTSET = 2;
    private static final int MAXSET = 4;
    private int bfSet = 0;    // bit field
    private boolean consistent = false;

    /**
     * Inner class for a Maker for LengthProperty objects
     */
    public static class Maker extends CompoundPropertyMaker {

        /**
         * @param propId the id of the property for which a Maker should be created
         */
        public Maker(int propId) {
            super(propId);
        }

        /**
         * Create a new empty instance of LengthRangeProperty.
         * @return the new instance. 
         */
        public Property makeNewProperty() {
            return new LengthRangeProperty();
        }

        private boolean isNegativeLength(Length len) {
            return ((len instanceof PercentLength
                        && ((PercentLength) len).getPercentage() < 0)
                    || (len.isAbsolute() && len.getValue() < 0));
        }
        
        /**
         * @see CompoundPropertyMaker#convertProperty
         */        
        public Property convertProperty(Property p, 
                                PropertyList propertyList, FObj fo)
                        throws PropertyException {
            
            if (p instanceof LengthRangeProperty) {
                return p;
            }
            
            if (this.propId == PR_BLOCK_PROGRESSION_DIMENSION
                    || this.propId == PR_INLINE_PROGRESSION_DIMENSION) {
                Length len = p.getLength();
                if (len != null) {
                    if (isNegativeLength(len)) {
                        log.warn(FObj.decorateWithContextInfo(
                                "Replaced negative value (" + len + ") for " + getName()
                                + " with 0mpt", fo));
                        p = new FixedLength(0);
                    }
                }
            }
            
            return super.convertProperty(p, propertyList, fo);
        }
        
        
        /**
         * @see org.apache.fop.fo.properties.PropertyMaker#getSubprop(Property, int, Property)
         */
        protected Property setSubprop(Property baseProperty, int subpropertyId,
                                        Property subproperty) {
            CompoundDatatype val = (CompoundDatatype) baseProperty.getObject();
            if (this.propId == PR_BLOCK_PROGRESSION_DIMENSION
                    || this.propId == PR_INLINE_PROGRESSION_DIMENSION) {
                Length len = subproperty.getLength();
                if (len != null) {
                    if (isNegativeLength(len)) {
                        log.warn("Replaced negative value (" + len + ") for " + getName()
                                + " with 0mpt");
                        val.setComponent(subpropertyId,
                                new FixedLength(0), false);
                        return baseProperty;
                    }
                }
            }
            val.setComponent(subpropertyId, subproperty, false);
            return baseProperty;
        }

    }



    /**
     * @see org.apache.fop.datatypes.CompoundDatatype#setComponent(int, Property, boolean)
     */
    public void setComponent(int cmpId, Property cmpnValue,
                             boolean bIsDefault) {
        if (cmpId == CP_MINIMUM) {
            setMinimum(cmpnValue, bIsDefault);
        } else if (cmpId == CP_OPTIMUM) {
            setOptimum(cmpnValue, bIsDefault);
        } else if (cmpId == CP_MAXIMUM) {
            setMaximum(cmpnValue, bIsDefault);
        }
    }

    /**
     * @see org.apache.fop.datatypes.CompoundDatatype#getComponent(int)
     */
    public Property getComponent(int cmpId) {
        if (cmpId == CP_MINIMUM) {
            return getMinimum(null);
        } else if (cmpId == CP_OPTIMUM) {
            return getOptimum(null);
        } else if (cmpId == CP_MAXIMUM) {
            return getMaximum(null);
        } else {
            return null;    // SHOULDN'T HAPPEN
        }
    }

    /**
     * Set minimum value to min.
     * @param minimum A Length value specifying the minimum value for this
     * LengthRange.
     * @param bIsDefault If true, this is set as a "default" value
     * and not a user-specified explicit value.
     */
    protected void setMinimum(Property minimum, boolean bIsDefault) {
        this.minimum = minimum;
        if (!bIsDefault) {
            bfSet |= MINSET;
        }
        consistent = false;
    }


    /**
     * Set maximum value to max if it is >= optimum or optimum isn't set.
     * @param max A Length value specifying the maximum value for this
     * @param bIsDefault If true, this is set as a "default" value
     * and not a user-specified explicit value.
     */
    protected void setMaximum(Property max, boolean bIsDefault) {
        maximum = max;
        if (!bIsDefault) {
            bfSet |= MAXSET;
        }
        consistent = false;
    }


    /**
     * Set the optimum value.
     * @param opt A Length value specifying the optimum value for this
     * @param bIsDefault If true, this is set as a "default" value
     * and not a user-specified explicit value.
     */
    protected void setOptimum(Property opt, boolean bIsDefault) {
        optimum = opt;
        if (!bIsDefault) {
            bfSet |= OPTSET;
        }
        consistent = false;
    }
    
    // Minimum is prioritaire, if explicit
    private void checkConsistency(PercentBaseContext context) {
        if (consistent) {
            return;
        }
        if (context == null) {
            return;
        }
        // Make sure max >= min
        // Must also control if have any allowed enum values!

        if (!minimum.isAuto() && !maximum.isAuto() 
                && minimum.getLength().getValue(context) > maximum.getLength().getValue(context)) {
            if ((bfSet & MINSET) != 0) {
                // if minimum is explicit, force max to min
                if ((bfSet & MAXSET) != 0) {
                    // Warning: min>max, resetting max to min
                    log.error("forcing max to min in LengthRange");
                }
                maximum = minimum;
            } else {
                minimum = maximum; // minimum was default value
            }
        }
        // Now make sure opt <= max and opt >= min
        if (!optimum.isAuto() && !maximum.isAuto() 
                && optimum.getLength().getValue(context) > maximum.getLength().getValue(context)) {
            if ((bfSet & OPTSET) != 0) {
                if ((bfSet & MAXSET) != 0) {
                    // Warning: opt > max, resetting opt to max
                    log.error("forcing opt to max in LengthRange");
                    optimum = maximum;
                } else {
                    maximum = optimum; // maximum was default value
                }
            } else {
                // opt is default and max is explicit or default
                optimum = maximum;
            }
        } else if (!optimum.isAuto() && !minimum.isAuto() 
                    && optimum.getLength().getValue(context) 
                        < minimum.getLength().getValue(context)) {
            if ((bfSet & MINSET) != 0) {
                // if minimum is explicit, force opt to min
                if ((bfSet & OPTSET) != 0) {
                    log.error("forcing opt to min in LengthRange");
                }
                optimum = minimum;
            } else {
                minimum = optimum; // minimum was default value
            }
        }
        
        consistent = true;
    }

    /**
     * @param context Percentage evaluation context
     * @return minimum length
     */
    public Property getMinimum(PercentBaseContext context) {
        checkConsistency(context);
        return this.minimum;
    }

    /**
     * @param context Percentage evaluation context
     * @return maximum length
     */
    public Property getMaximum(PercentBaseContext context) {
        checkConsistency(context);
        return this.maximum;
    }

    /**
     * @param context Percentage evaluation context
     * @return optimum length
     */
    public Property getOptimum(PercentBaseContext context) {
        checkConsistency(context);
        return this.optimum;
    }

    /** @see java.lang.Object#toString() */
    public String toString() {
        return "LengthRange["
            + "min:" + getMinimum(null).getObject() 
            + ", max:" + getMaximum(null).getObject() 
            + ", opt:" + getOptimum(null).getObject() + "]";
    }

    /**
     * @return this.lengthRange
     */
    public LengthRangeProperty getLengthRange() {
        return this;
    }

    /**
     * @return this.lengthRange cast as an Object
     */
    public Object getObject() {
        return this;
    }

}
