/*
 * $Id: LengthPairProperty.java,v 1.3 2003/03/05 21:48:02 jeremias Exp $
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
package org.apache.fop.fo;

import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.CompoundDatatype;
import org.apache.fop.fo.properties.CompoundPropertyMaker;

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
        protected Maker(int propId) {
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
