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
import org.apache.fop.datatypes.CompoundDatatype;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;

/**
 * Superclass for properties that wrap Keep values
 */
public class KeepProperty extends Property implements CompoundDatatype {
    private Property withinLine;
    private Property withinColumn;
    private Property withinPage;

    /**
     * Inner class for creating instances of KeepProperty
     */
    public static class Maker extends CompoundPropertyMaker {

        /**
         * @param name name of property for which Maker should be created
         */
        public Maker(int propId) {
            super(propId);
        }

        /**
         * Create a new empty instance of KeepProperty.
         * @return the new instance. 
         */
        public Property makeNewProperty() {
            return new KeepProperty();
        }

        /**
         * @see CompoundPropertyMaker#convertProperty
         */        
        public Property convertProperty(Property p, PropertyList propertyList, FObj fo)
            throws FOPException
        {
            if (p instanceof KeepProperty) {
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
        if (cmpId == CP_WITHIN_LINE) {
            setWithinLine(cmpnValue, bIsDefault);
        } else if (cmpId == CP_WITHIN_COLUMN) {
            setWithinColumn(cmpnValue, bIsDefault);
        } else if (cmpId == CP_WITHIN_PAGE) {
            setWithinPage(cmpnValue, bIsDefault);
        }
    }

    /**
     * @see org.apache.fop.datatypes.CompoundDatatype#getComponent(int)
     */
    public Property getComponent(int cmpId) {
        if (cmpId == CP_WITHIN_LINE) {
            return getWithinLine();
        } else if (cmpId == CP_WITHIN_COLUMN) {
            return getWithinColumn();
        } else if (cmpId == CP_WITHIN_PAGE) {
            return getWithinPage();
        } else {
            return null;
        }
    }

    /**
     * @param withinLine withinLine property to set
     * @param bIsDefault not used (??)
     */
    public void setWithinLine(Property withinLine, boolean bIsDefault) {
        this.withinLine = withinLine;
    }

    /**
     * @param withinColumn withinColumn property to set
     * @param bIsDefault not used (??)
     */
    protected void setWithinColumn(Property withinColumn,
                                   boolean bIsDefault) {
        this.withinColumn = withinColumn;
    }

    /**
     * @param withinPage withinPage property to set
     * @param bIsDefault not used (??)
     */
    public void setWithinPage(Property withinPage, boolean bIsDefault) {
        this.withinPage = withinPage;
    }

    /**
     * @return the withinLine property
     */
    public Property getWithinLine() {
        return this.withinLine;
    }

    /**
     * @return the withinColumn property
     */
    public Property getWithinColumn() {
        return this.withinColumn;
    }

    /**
     * @return the withinPage property
     */
    public Property getWithinPage() {
        return this.withinPage;
    }

    /**
     * Not sure what to do here. There isn't really a meaningful single value.
     * @return String representation
     */
    public String toString() {
        return "Keep[" + 
            "withinLine:" + getWithinLine().getObject() + 
            ", withinColumn:" + getWithinColumn().getObject() + 
            ", withinPage:" + getWithinPage().getObject() + "]";
    }

    /**
     * @return this.keep
     */
    public KeepProperty getKeep() {
        return this;
    }

    /**
     * @return this.keep cast as Object
     */
    public Object getObject() {
        return this;
    }

}
