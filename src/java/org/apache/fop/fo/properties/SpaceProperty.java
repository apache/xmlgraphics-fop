/*
 * $Id$
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 *
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 *
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */
package org.apache.fop.fo.properties;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;

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
         * @param name name of the property whose Maker is to be created
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
         * @see CompoundPropertyMaker#convertProperty
         */
        public Property convertProperty(Property p,
                                        PropertyList propertyList,
                                        FObj fo) throws FOPException {
            if (p instanceof SpaceProperty) {
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
        if (cmpId == CP_PRECEDENCE) {
            setPrecedence(cmpnValue, bIsDefault);
        } else if (cmpId == CP_CONDITIONALITY) {
            setConditionality(cmpnValue, bIsDefault);
        } else {
            super.setComponent(cmpId, cmpnValue, bIsDefault);
        }
    }

    /**
     * @see org.apache.fop.datatypes.CompoundDatatype#getComponent(int)
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

    public String toString() {
        return "Space[" +
        "min:" + getMinimum().getObject() + 
        ", max:" + getMaximum().getObject() + 
        ", opt:" + getOptimum().getObject() + 
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
