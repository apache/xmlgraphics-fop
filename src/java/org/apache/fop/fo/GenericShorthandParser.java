/*
 * $Id: GenericShorthandParser.java,v 1.4 2003/03/05 21:48:01 jeremias Exp $
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

import java.util.Vector;
import java.util.Enumeration;
import org.apache.fop.fo.properties.FOPropertyMapping;

public class GenericShorthandParser implements ShorthandParser {

    /** Vector of Property objects */
    protected Vector list;

    /**
     * Constructor.
     * @param listprop the ListProperty object that should be parsed
     */
    public GenericShorthandParser(ListProperty listprop) {
        this.list = listprop.getList();
    }

    /**
     * @param index the index into the List of properties
     * @return the property from the List of properties at the index parameter
     */
    protected Property getElement(int index) {
        if (list.size() > index) {
            return (Property) list.elementAt(index);
        } else {
            return null;
        }
    }

    /**
     * @return the size of the list of properties
     */
    protected int count() {
        return list.size();
    }

    // Stores 1 to 3 values for border width, style, color
    // Used for: border, border-top, border-right etc
    public Property getValueForProperty(int propId,
                                        Property.Maker maker,
                                        PropertyList propertyList) {
        Property prop = null;
        // Check for keyword "inherit"
        if (count() == 1) {
            String sval = ((Property)list.elementAt(0)).getString();
            if (sval != null && sval.equals("inherit")) {
                String name = FOPropertyMapping.getPropertyName(propId);
                return propertyList.getFromParent(name);
            }
        }
        return convertValueForProperty(propId, maker, propertyList);
    }


    /**
     * Converts a property name into a Property
     * @param propId the property ID in the Constants interface
     * @param maker the Property.Maker to be used in the conversion
     * @param propertyList the PropertyList from which the Property should be
     * extracted
     * @return the Property matching the parameters, or null if not found
     */
    protected Property convertValueForProperty(int propId,
                                               Property.Maker maker,
                                               PropertyList propertyList) {
        Property prop = null;
        // Try each of the stored values in turn
        Enumeration eprop = list.elements();
        while (eprop.hasMoreElements() && prop == null) {
            Property p = (Property) eprop.nextElement();
            prop = maker.convertShorthandProperty(propertyList, p, null);
        }
        return prop;
    }

}

