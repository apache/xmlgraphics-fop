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
import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;

/**
 * Superclass for properties wrapping a Length value.
 */
abstract public class LengthProperty extends Property 
    implements Length, Numeric
{
    /**
     * Inner class for making instances of LengthProperty
     */
    public static class Maker extends PropertyMaker {
        private boolean autoOk = false;

        /**
         * @param name name of property for which Maker should be created
         */
        public Maker(int propId) {
            super(propId);
        }

        /**
         * protected Property checkPropertyKeywords(String value) {
         * if (isAutoLengthAllowed() && value.equals("auto")) {
         * return new LengthProperty(Length.AUTO);
         * }
         * return null;
         * }
         */

        /**
         * @return false (auto-length is not allowed for Length values)
         */
        protected boolean isAutoLengthAllowed() {
            return autoOk;
        }

        /**
         * Set the auto length flag.
         * @param inherited
         */
        public void setAutoOk(boolean autoOk) {
            this.autoOk = autoOk;
        }

        /**
         * @see PropertyMaker#convertProperty
         */
        public Property convertProperty(Property p,
                                        PropertyList propertyList,
                                        FObj fo) throws FOPException {
            Property prop = super.convertProperty(p, propertyList, fo);
            if (prop != null) {
                return prop;
            }
            if (isAutoLengthAllowed()) {
                String pval = p.getString();
                if (pval != null && pval.equals("auto")) {
                    return new AutoLength();
                }
            }
            if (p instanceof LengthProperty) {
                return p;
            }
            Length val = p.getLength();
            if (val != null) {
                return (Property) val;
            }
            return convertPropertyDatatype(p, propertyList, fo);
        }

    }

    /**
     * Indicates if the length has the "auto" value.
     * @return True if the length is set to "auto"
     */
    public boolean isAuto() {
        return false;
    }

    /**
     * Return the number of table units which are included in this
     * length specification.
     * This will always be 0 unless the property specification used
     * the proportional-column-width() function (only only table
     * column FOs).
     * <p>If this value is not 0, the actual value of the Length cannot
     * be known without looking at all of the columns in the table to
     * determine the value of a "table-unit".
     * @return The number of table units which are included in this
     * length specification.
     */
    public double getTableUnits() {
        return 0.0;
    }

    /**
     * Return the numeric dimension. Length always a dimension of 1.
     */
    public int getDimension() {
        return 1;
    }

    /**
     * @return this.length cast as a Numeric
     */
    public Numeric getNumeric() {
        return this;
    }

    /**
     * @return this.length
     */
    public Length getLength() {
        return this;
    }

    /**
     * @return this.length cast as an Object
     */
    public Object getObject() {
        return this;
    }

}

