/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.CompoundDatatype;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;

/**
 * Superclass for properties wrapping a LengthPair value
 */
public class LengthPairProperty extends Property implements CompoundDatatype {
    private Property ipd;
    private Property bpd;

    /**
     * Inner class for creating instances of LengthPairProperty
     */
    public static class Maker extends CompoundPropertyMaker {

        /**
         * @param name name of property for which this Maker should be created
         */
        public Maker(int propId) {
            super(propId);
        }

        /**
         * Create a new empty instance of LengthPairProperty.
         * @return the new instance. 
         */
        public Property makeNewProperty() {
            return new LengthPairProperty();
        }

        /**
         * @see CompoundPropertyMaker#convertProperty
         */        
        public Property convertProperty(Property p, PropertyList propertyList, FObj fo)
            throws FOPException
        {
            if (p instanceof LengthPairProperty) {
                return p;
            }
            return super.convertProperty(p, propertyList, fo);
        }
    }

    /**
     * @see org.apache.fop.datatypes.CompoundDatatype#setComponent(int, Property, boolean)
     */
    public void setComponent(int cmpId, Property cmpnValue,
                             boolean bIsDefault) {
        if (cmpId == CP_BLOCK_PROGRESSION_DIRECTION) {
            bpd = cmpnValue;
        } else if (cmpId == CP_INLINE_PROGRESSION_DIRECTION) {
            ipd = cmpnValue;
        }
    }

    /**
     * @see org.apache.fop.datatypes.CompoundDatatype#getComponent(int)
     */
    public Property getComponent(int cmpId) {
        if (cmpId == CP_BLOCK_PROGRESSION_DIRECTION) {
            return getBPD();
        } else if (cmpId == CP_INLINE_PROGRESSION_DIRECTION) {
            return getIPD();
        } else {
            return null;    // SHOULDN'T HAPPEN
        }
    }

    /**
     * @return Property holding the ipd length
     */
    public Property getIPD() {
        return this.ipd;
    }

    /**
     * @return Property holding the bpd length
     */
    public Property getBPD() {
        return this.bpd;
    }

    public String toString() {
        return "LengthPair[" + 
        "ipd:" + getIPD().getObject() + 
        ", bpd:" + getBPD().getObject() + "]";
    }

    /**
     * @return this.lengthPair
     */
    public LengthPairProperty getLengthPair() {
        return this;
    }

    /**
     * @return this.lengthPair cast as an Object
     */
    public Object getObject() {
        return this;
    }

}
